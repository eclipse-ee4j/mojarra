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

package com.sun.faces.application.resource;

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

class ZipDirectoryEntryScanner {

    private static final Logger LOGGER = FacesLogger.RESOURCE.getLogger();
    private static final String PREFIX = "META-INF/resources";
    private static final int PREFIX_LENGTH = PREFIX.length();
    Map<String, Boolean> resourceLibraries;

    ZipDirectoryEntryScanner() {
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        Set<String> webInfLibJars = extContext.getResourcePaths("/WEB-INF/lib");
        resourceLibraries = new ConcurrentHashMap<>();
        ZipEntry ze = null;
        String entryName = null;
        if (webInfLibJars != null) {
            for (String cur : webInfLibJars) {
                try (ZipInputStream zis = new ZipInputStream(extContext.getResourceAsStream(cur))) {
                    while (null != (ze = zis.getNextEntry())) {
                        entryName = ze.getName();
                        if (entryName.startsWith(PREFIX) && PREFIX_LENGTH < entryName.length()) {
                            entryName = entryName.substring(PREFIX_LENGTH + 1);
                            if (!entryName.endsWith("/")) {
                                // Assume this code is only reached if the zip entry
                                // is NOT a 'directory' entry.
                                int i = entryName.lastIndexOf("/");
                                if (-1 != i) {
                                    entryName = entryName.substring(0, i);
                                    if (!resourceLibraries.containsKey(entryName)) {
                                        resourceLibraries.put(entryName, Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(SEVERE)) {
                        LOGGER.log(SEVERE, "Unable to inspect resource library " + cur, ioe);
                    }
                }
            }
        }

        // remove the optional local prefix entries
        Iterator<String> iter = resourceLibraries.keySet().iterator();
        String cur;
        while (iter.hasNext()) {
            cur = iter.next();
            if (cur.contains("/")) {
                iter.remove();
            }
        }
    }

    boolean libraryExists(String libraryName, String localePrefix) {
        String key = localePrefix != null ? localePrefix + "/" + libraryName : libraryName;

        return resourceLibraries.containsKey(key);
    }

}
