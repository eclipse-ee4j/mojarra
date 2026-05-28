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
package org.eclipse.mojarra.test.perf;

import static java.lang.Integer.getInteger;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;

/**
 * Drives every scenario page through GET and (where applicable) POST cycles,
 * lets the {@link PhaseTimingListener} accumulate per-phase timings server-side,
 * then dumps the table from {@code /perf-stats} to stdout and to
 * {@code target/perf-stats-<timestamp>.txt}.
 *
 * <p>Iteration counts are tunable:
 * <ul>
 *   <li>{@code -Dperf.warmup=N} (default 50)</li>
 *   <li>{@code -Dperf.runs=N}   (default 500)</li>
 * </ul>
 *
 * <p>Gated behind {@code -Dperf=true} so a normal {@code mvn install} does not run it.
 */
@EnabledIfSystemProperty(named = "perf", matches = "true")
class PerfBenchIT extends BaseIT {

    private static final int WARMUP = getInteger("perf.warmup", 50);
    private static final int RUNS = getInteger("perf.runs", 500);

    /** Skip the BaseIT ChromeDriver bootstrap — this bench drives the server with HttpClient. */
    @Override
    public void setup() {
        // intentionally empty: browser is unused
    }

    /** Pair with overridden {@link #setup()}: the parent's teardown would NPE on a null driver. */
    @Override
    public void teardown() {
        WebDriver driver = this.browser;
        if (driver != null) {
            driver.quit();
        }
    }

    private static final List<String> GET_ONLY = List.of(
            "index",
            "table-readonly",
            "repeat-readonly",
            "composite-readonly",
            "table-readonly-heavy",
            "repeat-readonly-heavy");

    private static final List<String> POSTBACK = List.of(
            "form-inputs",
            "table-inputs",
            "repeat-inputs",
            "repeat-nested",
            "composite-inputs",
            "composite-nested",
            "table-inputs-heavy",
            "repeat-inputs-heavy");

    private static final Pattern VIEW_STATE = Pattern.compile(
            "name=\"jakarta\\.faces\\.ViewState\"[^>]*value=\"([^\"]+)\"" +
            "|value=\"([^\"]+)\"[^>]*name=\"jakarta\\.faces\\.ViewState\"");
    private static final Pattern INPUT_TAG = Pattern.compile("<input\\b([^>]*)/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR = Pattern.compile("\\b(\\w+)\\s*=\\s*\"([^\"]*)\"");

    @Test
    void runBenchmark() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .connectTimeout(ofSeconds(10))
                .build();

        // bootstrap each postback scenario once (GET → extract ViewState)
        Map<String, FormSpec> forms = new LinkedHashMap<>();
        for (String scenario : POSTBACK) {
            String html = get(client, scenario + ".xhtml");
            forms.put(scenario, parseForm(html, scenario + ".xhtml"));
        }

        get(client, "perf-stats?reset=1");

        // warmup: lets JIT settle and primes any caches the perf branches add
        for (int i = 0; i < WARMUP; i++) {
            for (String scenario : GET_ONLY) {
                get(client, scenario + ".xhtml");
            }
            for (Map.Entry<String, FormSpec> e : forms.entrySet()) {
                postAndRefresh(client, e.getValue());
            }
        }

        get(client, "perf-stats?reset=1");

        long startWall = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            for (String scenario : GET_ONLY) {
                get(client, scenario + ".xhtml");
            }
            for (Map.Entry<String, FormSpec> e : forms.entrySet()) {
                postAndRefresh(client, e.getValue());
            }
        }
        long elapsedMs = (System.nanoTime() - startWall) / 1_000_000L;

        String stats = get(client, "perf-stats");
        String header = String.format(
                "# warmup=%d runs=%d scenarios=%d elapsed=%d ms%n",
                WARMUP, RUNS, GET_ONLY.size() + POSTBACK.size(), elapsedMs);
        String report = header + stats;
        System.out.println();
        System.out.println(report);

        Path out = Path.of("target", "perf-stats-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt");
        Files.createDirectories(out.getParent());
        Files.writeString(out, report);
        System.out.println("Wrote " + out.toAbsolutePath());

        // Sanity: every scenario should have at least RUNS RENDER_RESPONSE samples.
        int minimumExpected = RUNS;
        for (String scenario : GET_ONLY) {
            assertTrue(scenarioCount(stats, scenario, "RENDER_RESPONSE") >= minimumExpected,
                    scenario + " RENDER_RESPONSE count below " + minimumExpected);
        }
        for (String scenario : POSTBACK) {
            assertTrue(scenarioCount(stats, scenario, "RENDER_RESPONSE") >= minimumExpected,
                    scenario + " RENDER_RESPONSE count below " + minimumExpected);
        }
    }

    private static long scenarioCount(String stats, String scenario, String phase) {
        Pattern p = Pattern.compile("^" + Pattern.quote(scenario) + "\\s+" + Pattern.quote(phase) + "\\s+(\\d+)", Pattern.MULTILINE);
        Matcher m = p.matcher(stats);
        return m.find() ? Long.parseLong(m.group(1)) : 0L;
    }

    private void postAndRefresh(HttpClient client, FormSpec form) throws IOException, InterruptedException {
        String body = post(client, form);
        Matcher vs = VIEW_STATE.matcher(body);
        if (vs.find()) {
            form.fields.put("jakarta.faces.ViewState", vs.group(1) != null ? vs.group(1) : vs.group(2));
        }
    }

    private String get(HttpClient client, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseURL + path)).GET().build();
        HttpResponse<String> response = client.send(request, ofString(UTF_8));
        assertEquals(200, response.statusCode(), "GET " + path);
        return response.body();
    }

    private String post(HttpClient client, FormSpec form) throws IOException, InterruptedException {
        String body = encodeForm(form.fields);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseURL + form.action))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(BodyPublishers.ofString(body, UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, ofString(UTF_8));
        assertEquals(200, response.statusCode(), "POST " + form.action);
        return response.body();
    }

    private static String encodeForm(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder(fields.size() * 16);
        boolean first = true;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(URLEncoder.encode(e.getKey(), UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), UTF_8));
        }
        return sb.toString();
    }

    /**
     * Pulls every named &lt;input&gt; tag (hidden, text, submit, etc.) plus the
     * jakarta.faces.ViewState marker. Good enough for the deterministic markup
     * Mojarra emits for our perf pages.
     */
    private static FormSpec parseForm(String html, String action) {
        Map<String, String> fields = new LinkedHashMap<>();
        Matcher m = INPUT_TAG.matcher(html);
        boolean submitSeen = false;
        while (m.find()) {
            String attrs = m.group(1);
            String name = attribute(attrs, "name");
            if (name == null) {
                continue;
            }
            String type = attribute(attrs, "type");
            String value = attribute(attrs, "value");
            if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
                if (attrs.contains("checked")) {
                    fields.put(name, value == null ? "on" : value);
                }
                continue;
            }
            if ("submit".equalsIgnoreCase(type)) {
                if (!submitSeen) {
                    fields.put(name, value == null ? "" : value);
                    submitSeen = true;
                }
                continue;
            }
            fields.put(name, value == null ? "" : value);
        }
        Matcher vs = VIEW_STATE.matcher(html);
        if (vs.find()) {
            String v = vs.group(1) != null ? vs.group(1) : vs.group(2);
            fields.put("jakarta.faces.ViewState", v);
        }
        return new FormSpec(action, fields);
    }

    private static String attribute(String attrs, String name) {
        Matcher m = ATTR.matcher(attrs);
        while (m.find()) {
            if (m.group(1).equalsIgnoreCase(name)) {
                return m.group(2);
            }
        }
        return null;
    }

    private static final class FormSpec {
        final String action;
        final Map<String, String> fields;
        FormSpec(String action, Map<String, String> fields) {
            this.action = action;
            this.fields = fields;
        }
    }
}
