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

package com.sun.faces.config.configprovider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.spi.ConfigurationResourceProvider;
import com.sun.faces.util.FacesLogger;

import jakarta.servlet.ServletContext;

/**
 *
 */
public class WebAppFlowConfigResourceProvider implements ConfigurationResourceProvider {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    // ------------------------------ Methods from ConfigurationResourceProvider

    /**
     * @see ConfigurationResourceProvider#getResources(jakarta.servlet.ServletContext)
     */
    @Override
    public Collection<URI> getResources(ServletContext context) {

        List<URI> list = Collections.emptyList();
        Set<String> allPaths = context.getResourcePaths("/");

        if (null == allPaths) {
            return list;
        }
        list = null;

        for (String cur : allPaths) {
            if (!cur.startsWith("/META-INF")) {
                if (cur.equals("/WEB-INF/")) {
                    Set<String> webInfPaths = context.getResourcePaths(cur);
                    if (null != webInfPaths) {
                        for (String webInfCur : webInfPaths) {
                            if (!cur.equals("/WEB-INF/classes/") && webInfCur.endsWith("/")) {
                                list = inspectDirectory(context, webInfCur, list);
                            }
                        }
                    }
                } else if (cur.endsWith("/")) {
                    list = inspectDirectory(context, cur, list);
                }
            }
        }

        return null == list ? Collections.emptyList() : list;

    }

    private List<URI> inspectDirectory(ServletContext context, String toInspect, List<URI> list) {
        URL curUrl = null;

        Set<String> allPaths = context.getResourcePaths(toInspect);
        if (null == allPaths) {
            return list;
        }

        for (String cur : allPaths) {
            if (cur.endsWith(RIConstants.FLOW_DEFINITION_ID_SUFFIX)) {
                int suffixIndex = cur.length() - RIConstants.FLOW_DEFINITION_ID_SUFFIX_LENGTH;
                int slash = cur.lastIndexOf("/", suffixIndex);
                if (-1 == slash) {
                    continue;
                }
                String flowName = cur.substring(slash + 1, suffixIndex);
                int prevSlash = cur.lastIndexOf("/", slash - 1);
                if (-1 == prevSlash) {
                    continue;
                }
                // Ensure cur matches the pattern <flowName>/<flowName>-flow.xml
                String dirName = cur.substring(prevSlash + 1, slash);
                if (dirName.equals(flowName)) {
                    if (null == list) {
                        list = new ArrayList<>();
                    }
                    try {
                        curUrl = context.getResource(cur);
                        list.add(curUrl.toURI());
                    } catch (MalformedURLException ex) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "Unable to get resource for {0}" + cur, ex);
                        }
                    } catch (URISyntaxException use) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "Unable to get URI for {0}" + curUrl.toExternalForm(), use);
                        }

                    }
                }
            }
        }
        return list;
    }

}
