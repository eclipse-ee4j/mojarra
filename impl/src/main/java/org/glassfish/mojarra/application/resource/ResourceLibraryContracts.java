/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.application.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.application.view.FaceletViewHandlingStrategy;
import org.glassfish.mojarra.facelets.util.Classpath;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.Util;

/**
 * <p>
 * Which resource library contracts this application ships, discovered once at bringup from the web application root and from the classpath, and published for
 * the view handling strategy to consult per view.
 * </p>
 */
public final class ResourceLibraryContracts {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    public static final String META_INF_CONTRACTS_DIR = "META-INF/" + ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    private static final int META_INF_CONTRACTS_DIR_LEN = META_INF_CONTRACTS_DIR.length();

    private static final String RESOURCE_CONTRACT_SUFFIX = "/" + ResourceHandler.RESOURCE_CONTRACT_XML;

    private ResourceLibraryContracts() {
        // Hide the constructor of this utility class.
    }

    public static void discover() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContex = context.getExternalContext();
        Set<String> foundContracts = new HashSet<>();
        Set<String> candidates;

        // Scan for "contractMappings" in the web app root
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        String contractsDirName = associate.getResourceManager().getBaseContractsPath();
        assert null != contractsDirName;
        candidates = extContex.getResourcePaths(contractsDirName);
        if (null != candidates) {
            int contractsDirNameLen = contractsDirName.length();
            int end;
            for (String cur : candidates) {
                end = cur.length();
                if (cur.endsWith("/")) {
                    end--;
                }
                foundContracts.add(cur.substring(contractsDirNameLen + 1, end));
            }
        }

        // Scan for "META-INF" contractMappings in the classpath
        try {
            URL[] candidateURLs = Classpath.search(
                Util.getCurrentLoader(ResourceLibraryContracts.class), META_INF_CONTRACTS_DIR, RESOURCE_CONTRACT_SUFFIX,
                Classpath.SearchAdvice.AllMatches
            );
            for (URL curURL : candidateURLs) {
                String cur = curURL.toExternalForm();

                int i = cur.indexOf(META_INF_CONTRACTS_DIR) + META_INF_CONTRACTS_DIR_LEN + 1;
                int j = cur.indexOf(RESOURCE_CONTRACT_SUFFIX);
                if (i < j) {
                    foundContracts.add(cur.substring(i, j));
                }

            }
        }
        catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Unable to scan " + META_INF_CONTRACTS_DIR, ioe);
            }
        }

        if (foundContracts.isEmpty()) {
            return;
        }

        Map<String, List<String>> contractMappings = new HashMap<>();

        Map<String, List<String>> contractsFromConfig = associate.getResourceLibraryContracts();
        List<String> contractsToExpose;

        if (null != contractsFromConfig && !contractsFromConfig.isEmpty()) {
            List<String> contractsFromMapping;
            for (Map.Entry<String, List<String>> cur : contractsFromConfig.entrySet()) {
                // Verify that the contractsToExpose in this mapping actually exist
                // in the application. If not, log a message.
                contractsFromMapping = cur.getValue();
                if (null == contractsFromMapping || contractsFromMapping.isEmpty()) {
                    if (LOGGER.isLoggable(Level.CONFIG)) {
                        LOGGER.log(Level.CONFIG, "resource library contract mapping for pattern {0} has no contracts.", cur.getKey());
                    }
                }
                else {
                    contractsToExpose = new ArrayList<>();
                    for (String curContractFromMapping : contractsFromMapping) {
                        if (foundContracts.contains(curContractFromMapping)) {
                            contractsToExpose.add(curContractFromMapping);
                        }
                        else {
                            if (LOGGER.isLoggable(Level.CONFIG)) {
                                LOGGER.log(
                                    Level.CONFIG,
                                    "resource library contract mapping for pattern {0} exposes contract {1}, but that contract is not available to the application.",
                                    new String[] { cur.getKey(), curContractFromMapping }
                                );
                            }
                        }
                    }
                    if (!contractsToExpose.isEmpty()) {
                        contractMappings.put(cur.getKey(), contractsToExpose);
                    }
                }
            }
        }
        else {
            contractsToExpose = new ArrayList<>(foundContracts);
            contractMappings.put("*", contractsToExpose);
        }
        extContex.getApplicationMap().put(FaceletViewHandlingStrategy.RESOURCE_LIBRARY_CONTRACT_DATA_STRUCTURE_KEY, contractMappings);

    }

}
