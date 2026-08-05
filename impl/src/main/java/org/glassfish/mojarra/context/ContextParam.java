/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.context;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;

import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.faces.application.ProjectStage;

/**
 * <p class="changed_added_5_0">
 * A context parameter recognized by this implementation, declaring the name it is read under, the type its value is
 * converted to, and the default which applies when it is not set.
 * </p>
 *
 * <p>
 * A declaration is stateless, because the constants declaring it are global to the class loader and this
 * implementation is routinely loaded by one which is shared between deployed applications. The value a parameter
 * resolves to is therefore held per application, by
 * {@link org.glassfish.mojarra.config.WebConfiguration}, and never by the declaration itself.
 * </p>
 *
 * @since 5.0
 */
public interface ContextParam {

    /**
     * @return the name the parameter is declared under.
     */
    String getName();

    /**
     * <p>
     * Returns the expected type of the parameter value. Supported types are:
     * </p>
     * <ul>
     * <li>{@link String}
     * <li>{@link String}{@code []}
     * <li>{@link Character}
     * <li>{@link Boolean}
     * <li>{@link Integer}
     * <li>{@link Enum}
     * </ul>
     *
     * @return the expected type of the parameter value.
     */
    Class<?> getType();

    /**
     * @return what separates the entries of a {@link String}{@code []} valued parameter, or <code>null</code> when the
     * parameter holds a single value.
     */
    Separator getSeparator();

    /**
     * @param <T> the expected return type.
     * @param projectStage the stage the application runs in, which a few parameters derive their default from.
     * @return the default value of the parameter, in the type indicated by {@link #getType()}.
     */
    <T> T getDefaultValue(ProjectStage projectStage);

    /**
     * @return whether the parameter is on its way out, so that an application still declaring it is told so.
     */
    default boolean isDeprecated() {
        return false;
    }

    /**
     * @return the name of the parameter which replaces this one, or <code>null</code> when it has no replacement,
     * which is the case for one whose behaviour goes away rather than moving elsewhere.
     */
    default String getAlternateName() {
        return null;
    }

    /**
     * <p>
     * What a declaration says about being on its way out. This is a type rather than a pair of constructor arguments,
     * because a boolean would be ambiguous with the default value of a boolean parameter and a name with that of a
     * string one.
     * </p>
     *
     * @param alternateName the name of the replacement, or <code>null</code> when there is none.
     */
    record Deprecation(String alternateName) {

        /**
         * Deprecated with nothing to move to, because the behaviour itself goes away.
         */
        public static final Deprecation DEPRECATED = new Deprecation(null);

        /**
         * @param alternateName the name of the parameter which replaces this one.
         * @return the deprecation naming that replacement.
         */
        public static Deprecation replacedBy(String alternateName) {
            return new Deprecation(alternateName);
        }
    }

    /**
     * <p>
     * Converts a declared parameter value to the type indicated by {@link #getType()}.
     * </p>
     *
     * @param <T> the expected return type.
     * @param value the declared value, which may be <code>null</code>.
     * @return the converted value, or empty when there was none.
     * @throws IllegalArgumentException when the value cannot be converted to the expected type.
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> toValue(String value) {
        Class<?> type = getType();

        if (value == null) {
            return Optional.empty();
        }
        else if (type == String.class) {
            return Optional.of((T) value);
        }
        else if (type == String[].class) {
            return Optional.of((T) getSeparator().split(value));
        }
        else if (type == Character.class) {
            if (value.length() == 1) {
                return Optional.of((T) Character.valueOf(value.charAt(0)));
            }
        }
        else if (type == Boolean.class) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(value) || Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                return Optional.of((T) Boolean.valueOf(value));
            }
        }
        else if (type == Integer.class) {
            try {
                return Optional.of((T) Integer.valueOf(value));
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException(getName() + ": invalid value: " + value, e);
            }
        }
        else if (type.isEnum()) {
            for (Object constant : type.getEnumConstants()) {
                if (constant.toString().equalsIgnoreCase(value)) {
                    return Optional.of((T) constant);
                }
            }
        }

        throw new IllegalArgumentException(getName() + ": invalid value: " + value);
    }

    /**
     * <p>
     * What separates the entries of a {@link String}{@code []} valued parameter.
     * </p>
     */
    enum Separator {

        COMMA("\\s*,\\s*"),
        SEMICOLON("\\s*;\\s*"),
        SPACE("\\s+");

        private final Pattern pattern;

        private Separator(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public String[] split(String value) {
            return stream(pattern.split(value)).map(String::trim).filter(not(String::isEmpty)).toArray(String[]::new);
        }
    }
}
