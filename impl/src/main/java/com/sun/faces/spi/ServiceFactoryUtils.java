/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;

/**
 * <p>
 * Base class for service discovery.
 * </p>
 */
final class ServiceFactoryUtils {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    private static final String[] EMPTY_ARRAY = new String[0];

    // ---------------------------------------------------------- Public Methods

    static Object getProviderFromEntry(String entry, Class<?>[] argumentTypes, Object[] arguments) throws FacesException {

        if (entry == null) {
            return null;
        }

        try {
            Class<?> clazz = Util.loadClass(entry, null);
            Constructor c = clazz.getDeclaredConstructor(argumentTypes);
            if (c == null) {
                throw new FacesException("Unable to find constructor accepting arguments: " + Arrays.toString(arguments));
            }
            return c.newInstance(arguments);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new FacesException(e);
        }

    }

    static String[] getServiceEntries(String key) {

        List<String> results = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            return EMPTY_ARRAY;
        }

        Enumeration<URL> urls = null;
        String serviceName = "META-INF/services/" + key;
        try {
            urls = loader.getResources(serviceName);
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, ioe.toString(), ioe);
            }
        }

        if (urls != null) {
            InputStream input = null;
            BufferedReader reader = null;
            while (urls.hasMoreElements()) {
                try {
                    if (results == null) {
                        results = new ArrayList<>();
                    }
                    URL url = urls.nextElement();
                    URLConnection conn = url.openConnection();
                    conn.setUseCaches(false);
                    input = conn.getInputStream();
                    if (input != null) {
                        try {
                            reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            // The DM_DEFAULT_ENCODING warning is acceptable here
                            // because we explicitly *want* to use the Java runtime's
                            // default encoding.
                            reader = new BufferedReader(new InputStreamReader(input));
                        }
                        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                            String cleanedLine = cleanupServiceLine(line);
                            // Process line only if after removing comment part and trimming
                            // there's still anything left.
                            if (cleanedLine != null) {
                                results.add(cleanedLine);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "faces.spi.provider.cannot_read_service", new Object[] { serviceName });
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Exception e) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.log(Level.FINEST, "Closing stream", e);
                            }
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.log(Level.FINEST, "Closing stream", e);
                            }
                        }
                    }
                }
            }
        }

        return results != null && !results.isEmpty() ? results.toArray(new String[results.size()]) : EMPTY_ARRAY;

    }

    /**
     * Cleans line in service file by processing comments and trimming.
     *
     * @param line
     *         Line to clean.
     * @return The cleaned line, if it isn't empty after removing the comment and trimming,
     *         otherwise <code>null</code>.
     */
    private static String cleanupServiceLine(String line) {
        // Process comments, starting at '#'.
        int commentIndex = line.indexOf('#');
        if (commentIndex == 0) {
            // The whole line is a comment, no need to process it.
            return null;
        } else if (commentIndex > 0) {
            // Cut everything of after '#'.
            line = line.substring(0, commentIndex);
        }
        line = line.trim();
        if (line.isEmpty()) {
            return null;
        } else {
            return line;
        }
    }
}
