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

package com.sun.faces.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Build-time identity of this Mojarra build, derived from {@code mojarra.properties}
 * which is token-substituted by maven-resources-plugin at build time. The properties
 * file is shipped on the impl classpath at {@code /com/sun/faces/mojarra.properties}.
 * Reading it via the classpath survives exploded WARs and IDE test runs where the
 * JAR MANIFEST is not available.
 */
public final class MojarraVersion {

    /** Spec version as {@code major.minor} (e.g. {@code "4.0"}); {@code null} if {@code mojarra.properties} could not be loaded. */
    public static final String SPECIFICATION_VERSION;

    /** Full implementation version (e.g. {@code "4.0.19"}); {@code null} if {@code mojarra.properties} could not be loaded. */
    public static final String IMPLEMENTATION_VERSION;

    /** Spec version as packed int: {@code major * 10000 + minor * 100} (e.g. {@code 40000} for {@code "4.0"}). */
    public static final int SPECIFICATION_VERSION_INT;

    /** Implementation incremental version as int (e.g. {@code 18} for {@code "4.0.19"}). */
    public static final int IMPLEMENTATION_VERSION_INT;

    private static final String PROPERTIES_RESOURCE = "/com/sun/faces/mojarra.properties";
    private static final String VERSION_SEPARATOR = "[.\\-_]";

    static {
        IMPLEMENTATION_VERSION = loadVersion();
        SPECIFICATION_VERSION = stripPatch(IMPLEMENTATION_VERSION);
        SPECIFICATION_VERSION_INT = parseSpec(IMPLEMENTATION_VERSION);
        IMPLEMENTATION_VERSION_INT = parseImpl(IMPLEMENTATION_VERSION);
    }

    private static String loadVersion() {
        Properties props = new Properties();
        try (InputStream in = MojarraVersion.class.getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (in == null) {
                return null;
            }
            props.load(in);
        } catch (IOException e) {
            return null;
        }
        return props.getProperty("version");
    }

    private static String stripPatch(String v) {
        if (v == null) {
            return null;
        }
        String[] parts = v.split(VERSION_SEPARATOR);
        if (parts.length < 2) {
            return v;
        }
        return parts[0] + "." + parts[1];
    }

    private static int parseSpec(String v) {
        if (v == null) {
            return 0;
        }
        try {
            String[] parts = v.split(VERSION_SEPARATOR);
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return major * 10000 + minor * 100;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseImpl(String v) {
        if (v == null) {
            return 0;
        }
        try {
            String[] parts = v.split(VERSION_SEPARATOR);
            return parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns an immutable map of {@code version} / {@code specversion} / {@code implversion}
     * suitable for publishing as an application-scoped attribute so EL can read it. {@code version}
     * falls back to {@code "DEV-SNAPSHOT"} when {@link #IMPLEMENTATION_VERSION} is {@code null}
     * (i.e. {@code mojarra.properties} was not loadable from the classpath).
     */
    public static Map<String, Object> toMap() {
        return Map.of(
            "version", IMPLEMENTATION_VERSION != null ? IMPLEMENTATION_VERSION : "DEV-SNAPSHOT",
            "specversion", SPECIFICATION_VERSION_INT,
            "implversion", IMPLEMENTATION_VERSION_INT);
    }

    private MojarraVersion() {
        throw new AssertionError();
    }
}
