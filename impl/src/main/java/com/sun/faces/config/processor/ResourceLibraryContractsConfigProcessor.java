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

package com.sun.faces.config.processor;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

public class ResourceLibraryContractsConfigProcessor extends AbstractConfigProcessor {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();
    /**
     * <code>/faces-config/resource-library-contracts</code>
     */
    private static final String RESOURCE_LIBRARY_CONTRACTS = "resource-library-contracts";

    /**
     * Constructor.
     */
    public ResourceLibraryContractsConfigProcessor() {
    }

    /**
     * Process the configuration documents.
     *
     * @param servletContext the servlet context.
     * @param documentInfos the document info(s).
     * @throws Exception when an error occurs.
     */
    @Override
    public void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        HashMap<String, List<String>> map = new HashMap<>();
        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, MessageFormat.format("Processing factory elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList resourceLibraryContracts = document.getDocumentElement().getElementsByTagNameNS(namespace, RESOURCE_LIBRARY_CONTRACTS);
            if (resourceLibraryContracts != null && resourceLibraryContracts.getLength() > 0) {
                processResourceLibraryContracts(resourceLibraryContracts, map);
            }

        }

        if (!map.isEmpty()) {
            ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
            associate.setResourceLibraryContracts(map);
        }

    }

    /**
     * Process the resource library contracts.
     *
     * @param resourceLibraryContracts the resource library contracts.
     * @param map the set of resource library contracts.
     */
    private void processResourceLibraryContracts(NodeList resourceLibraryContracts, HashMap<String, List<String>> map) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new FacesConfigNamespaceContext());
        for (int c = 0; c < resourceLibraryContracts.getLength(); c++) {
            Node node = resourceLibraryContracts.item(c);
            try {
                NodeList mappings = (NodeList) xpath.evaluate(".//ns1:contract-mapping", node, XPathConstants.NODESET);
                if (mappings != null) {
                    for (int m = 0; m < mappings.getLength(); m++) {
                        Node contractMapping = mappings.item(m);
                        NodeList urlPatterns = (NodeList) xpath.evaluate(".//ns1:url-pattern/text()", contractMapping, XPathConstants.NODESET);
                        if (urlPatterns != null) {
                            for (int p = 0; p < urlPatterns.getLength(); p++) {
                                String urlPattern = urlPatterns.item(p).getNodeValue().trim();

                                if (LOGGER.isLoggable(INFO)) {
                                    LOGGER.log(INFO, "Processing resource library contract mapping for url-pattern: {0}", urlPattern);
                                }

                                if (!map.containsKey(urlPattern)) {
                                    /*
                                     * If there is no urlPattern then add it to the list,
                                     */
                                    ArrayList<String> list = new ArrayList<>();
                                    NodeList contracts = (NodeList) xpath.evaluate(".//ns1:contracts/text()", contractMapping, XPathConstants.NODESET);
                                    if (contracts != null && contracts.getLength() > 0) {
                                        for (int j = 0; j < contracts.getLength(); j++) {
                                            String[] contractStrings = contracts.item(j).getNodeValue().trim().split(",");
                                            for (String contractString : contractStrings) {
                                                if (!list.contains(contractString)) {
                                                    if (LOGGER.isLoggable(INFO)) {
                                                        LOGGER.log(INFO, "Added contract: {0} for url-pattern: {1}", new Object[]{contractString, urlPattern});
                                                    }
                                                    list.add(contractString);
                                                } else {
                                                    /*
                                                     * We found the contract again in the list for the specified url-pattern.
                                                     */
                                                    if (LOGGER.isLoggable(INFO)) {
                                                        LOGGER.log(INFO, "Duplicate contract: {0} found for url-pattern: {1}", new Object[]{contractString, urlPattern});
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (!list.isEmpty()) {
                                        /*
                                         * Now add the url-pattern and its contracts.
                                         */
                                        map.put(urlPattern, list);
                                    } else {
                                        /*
                                         * The list was empty, log there were no contracts specified.
                                         */
                                        LOGGER.log(INFO, "No contracts found for url-pattern: {0}", urlPattern);
                                    }
                                } else {
                                    /*
                                     * Otherwise log there is a duplicate url-pattern found.
                                     */
                                    LOGGER.log(INFO, "Duplicate url-patern found: {0}, ignoring it", urlPattern);
                                }
                            }
                        }
                    }
                }
            } catch (XPathExpressionException exception) {
                /*
                 * This particular exception will never happen since the above valid XPath expressions never change, but the XPath
                 * runtime defines it as a checked exception so we have to deal with it.
                 */
                LOGGER.log(FINEST, "Unable to parse XPath expression", exception);
            }
        }
    }
}
