/*
 * Copyright (c) Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.context.ContextParam;

/**
 * Validates <code>CONTEXT-PARAMS.md</code> in the repository root against the sources, so that a context parameter
 * cannot be added, removed, renamed or given another default value without that page being updated.
 * <p>
 * The parameters recognized by this implementation are collected from {@link WebContextInitParameter},
 * {@link BooleanWebContextInitParameter} and {@link ContextParam}, plus a scan of the main sources for
 * <code>getInitParameter()</code> call sites which read a parameter declared outside of those enums.
 * <p>
 * This test only verifies, it never writes the page. When it fails, edit the page by hand.
 */
class ContextParamsMdTest {

    private static final String NONE = "(none)";
    private static final String TYPE = "Type";
    private static final String DEFAULT = "Default";
    private static final String DESCRIPTION = "Description";
    private static final List<String> REQUIRED_COLUMNS = List.of(TYPE, DEFAULT, "Since", "Performance", DESCRIPTION);

    private static final Pattern HEADER_CELL = Pattern.compile("<th>(.*?)</th>");
    private static final Pattern PARAMETER_ROW = Pattern.compile("<th colspan=\"\\d+\"[^>]*>.*?<code>(.*?)</code>.*?</th>");
    private static final Pattern DATA_CELL = Pattern.compile("<td>(.*?)</td>");
    private static final Pattern TAG = Pattern.compile("<[^>]+>");

    private static final Pattern CONSTANT = Pattern.compile("static final String\\s+(\\w+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern INIT_PARAM_READ = Pattern.compile("getInitParameter\\(([^()]*)\\)");
    private static final Pattern STRING_LITERAL = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern IDENTIFIER = Pattern.compile("(?:\\w+\\.)*(\\w+)");

    /**
     * A context parameter as declared by the sources. A <code>null</code> type or default value means the source
     * declares none, in which case the documented cell is not validated. The type of a {@link WebContextInitParameter}
     * is always <code>String</code> and therefore says nothing about the shape of the value it accepts.
     */
    private static final class Param {

        private final String name;
        private final String type;
        private final String defaultValue;
        private final String replacedBy;

        private Param(String name, String type, String defaultValue, String replacedBy) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.replacedBy = replacedBy;
        }
    }

    @Test
    void documentsEveryContextParam() {
        List<String> undocumented = new ArrayList<>(params().keySet());
        undocumented.removeAll(rows().keySet());
        assertTrue(undocumented.isEmpty(), () -> "Not documented in " + markdownFile() + ", add a row: " + undocumented);
    }

    @Test
    void documentsNoUnknownContextParam() {
        List<String> unknown = new ArrayList<>(rows().keySet());
        unknown.removeAll(params().keySet());
        assertTrue(unknown.isEmpty(), () -> "No longer recognized by the implementation, remove the row from " + markdownFile() + ": " + unknown);
    }

    @Test
    void documentsTheDeclaredTypeAndDefaultValue() {
        Map<String, Map<String, String>> rows = rows();
        Map<String, String> wrong = new TreeMap<>();

        for (Param param : params().values()) {
            Map<String, String> row = rows.get(param.name);

            if (row == null) {
                continue; // Reported by documentsEveryContextParam().
            }

            check(wrong, param.name, TYPE, param.type, text(row.get(TYPE)));
            check(wrong, param.name, DEFAULT, expectedDefault(param.defaultValue), text(row.get(DEFAULT)));
        }

        assertTrue(wrong.isEmpty(), () -> "Stale cells in " + markdownFile() + ": " + wrong);
    }

    @Test
    void documentsEveryColumnOfEveryRow() {
        List<String> incomplete = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> row : rows().entrySet()) {
            for (String column : REQUIRED_COLUMNS) {
                if (row.getValue().getOrDefault(column, "").isEmpty()) {
                    incomplete.add(row.getKey() + " has no " + column);
                }
            }
        }

        assertTrue(incomplete.isEmpty(), () -> "Incomplete rows in " + markdownFile() + ": " + incomplete);
    }

    @Test
    void documentsEveryDeprecatedParamWithItsReplacement() {
        Map<String, Map<String, String>> rows = rows();
        List<String> unlinked = new ArrayList<>();

        for (Param param : params().values()) {
            Map<String, String> row = rows.get(param.name);

            if (param.replacedBy != null && row != null && !text(row.get(DESCRIPTION)).contains(param.replacedBy)) {
                unlinked.add(param.name + " is replaced by " + param.replacedBy);
            }
        }

        assertTrue(unlinked.isEmpty(), () -> "Deprecated parameters whose description does not name their replacement in " + markdownFile() + ": " + unlinked);
    }

    private static void check(Map<String, String> wrong, String name, String column, String expected, String actual) {
        if (expected != null && !expected.equals(actual)) {
            wrong.put(name + " " + column, "expected " + expected + " but was " + actual);
        }
    }

    private static String expectedDefault(String defaultValue) {
        return defaultValue == null ? null : defaultValue.isEmpty() ? NONE : defaultValue;
    }

    // Collecting -----------------------------------------------------------------------------------------------------

    private static Map<String, Param> params() {
        Map<String, Param> params = new LinkedHashMap<>();

        for (WebContextInitParameter param : WebContextInitParameter.values()) {
            put(params, new Param(param.getQualifiedName(), null, param.getDefaultValue(), alternateOf(param)));
        }

        for (BooleanWebContextInitParameter param : BooleanWebContextInitParameter.values()) {
            put(params, new Param(param.getQualifiedName(), "boolean", String.valueOf(param.getDefaultValue()), alternateOf(param)));
        }

        for (ContextParam param : ContextParam.values()) {
            put(params, new Param(param.getName(), typeOf(param.getType()), String.valueOf(param.getDefaultValue()), null));
        }

        for (String name : scanForInitParameterReads()) {
            put(params, new Param(name, null, null, null));
        }

        return params;
    }

    private static void put(Map<String, Param> params, Param param) {
        if (!param.name.isEmpty()) {
            params.putIfAbsent(param.name, param);
        }
    }

    private static String typeOf(Class<?> type) {
        return type == Boolean.class ? "boolean" : type == Integer.class ? "int" : type.getSimpleName();
    }

    private static String alternateOf(Enum<?> param) {
        Object alternate = fieldValue(param, "alternate");

        if (alternate == null || !Boolean.TRUE.equals(fieldValue(param, "deprecated"))) {
            return null;
        }

        return String.valueOf(invoke(alternate, "getQualifiedName"));
    }

    private static Object fieldValue(Enum<?> param, String name) {
        try {
            Field field = param.getDeclaringClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(param);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object invoke(Object param, String methodName) {
        try {
            return ((Enum<?>) param).getDeclaringClass().getMethod(methodName).invoke(param);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Collects every context parameter name which the main sources read through <code>getInitParameter()</code>, either
     * as a string literal or as a constant declared anywhere in those same sources. Dynamic reads, such as the ones
     * iterating over the enums, do not resolve to a name and are skipped.
     */
    private static List<String> scanForInitParameterReads() {
        List<String> sources = readSources(markdownFile().toAbsolutePath().getParent().resolve("impl/src/main/java"));
        Map<String, String> constants = new TreeMap<>();

        for (String source : sources) {
            Matcher matcher = CONSTANT.matcher(source);

            while (matcher.find()) {
                constants.put(matcher.group(1), matcher.group(2));
            }
        }

        List<String> names = new ArrayList<>();

        for (String source : sources) {
            Matcher matcher = INIT_PARAM_READ.matcher(source);

            while (matcher.find()) {
                String name = resolveArgument(matcher.group(1).trim(), constants);

                if (name != null && name.contains(".")) {
                    names.add(name);
                }
            }
        }

        return names;
    }

    private static String resolveArgument(String argument, Map<String, String> constants) {
        Matcher literal = STRING_LITERAL.matcher(argument);

        if (literal.matches()) {
            return literal.group(1);
        }

        Matcher identifier = IDENTIFIER.matcher(argument);
        return identifier.matches() ? constants.get(identifier.group(1)) : null;
    }

    private static List<String> readSources(Path mainSources) {
        try (Stream<Path> files = Files.walk(mainSources)) {
            return files.filter(file -> file.toString().endsWith(".java")).map(ContextParamsMdTest::readString).collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readString(Path file) {
        try {
            return Files.readString(file, UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Parsing --------------------------------------------------------------------------------------------------------

    /**
     * Maps each documented parameter name to its row, keyed by the column names of the table it appears in, so that a
     * table can gain a column or be reordered without this test having to change. The page uses raw HTML tables in
     * which the parameter name spans the full width above the row holding its cells.
     */
    private static Map<String, Map<String, String>> rows() {
        Map<String, Map<String, String>> rows = new LinkedHashMap<>();
        List<String> columns = List.of();
        String name = null;

        for (String line : readString(markdownFile()).split("\n")) {
            Matcher parameter = PARAMETER_ROW.matcher(line);

            if (parameter.find()) {
                name = unescape(parameter.group(1));
            } else if (line.contains("<th>")) {
                columns = matchesOf(HEADER_CELL, line);
            } else if (name != null && line.contains("<td>")) {
                List<String> cells = matchesOf(DATA_CELL, line);
                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < Math.min(columns.size(), cells.size()); i++) {
                    row.put(columns.get(i), cells.get(i));
                }

                rows.put(name, row);
                name = null;
            }
        }

        return rows;
    }

    private static List<String> matchesOf(Pattern pattern, String line) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            matches.add(matcher.group(1).trim());
        }

        return matches;
    }

    /**
     * The plain text of a cell, so that this test compares documented values rather than the markup they are dressed
     * in, and the page stays free to change how it renders them.
     */
    private static String text(String html) {
        return html == null ? "" : unescape(TAG.matcher(html).replaceAll("")).trim();
    }

    private static String unescape(String html) {
        return html.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    private static Path markdownFile() {
        String configured = System.getProperty("contextparams.md");
        return (configured != null ? Path.of(configured) : Path.of("..", "CONTEXT-PARAMS.md")).toAbsolutePath().normalize();
    }
}
