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

import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/factory</code>.
 * </p>
 */
public class FacesConfigExtensionProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <code>/faces-config/faces-config-extension</code>
     */
    private static final String FACES_CONFIG_EXTENSION = "faces-config-extension";

    /**
     * <code>/faces-config/faces-config-extension/facelets-processing</code>
     */
    private static final String FACELETS_PROCESSING = "facelets-processing";

    /**
     * <code>/faces-config/faces-config-extension/facelets-processing/file-extension</code>
     */
    private static final String FILE_EXTENSION = "file-extension";

    /**
     * <code>/faces-config/faces-config-extension/facelets-processing/process-as</code>
     */
    private static final String PROCESS_AS = "process-as";

    // ------------------------------------------------------------ Constructors

    public FacesConfigExtensionProcessor() {
    }

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing faces-config-extension elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList facesConfigExtensions = document.getDocumentElement().getElementsByTagNameNS(namespace, FACES_CONFIG_EXTENSION);

            if (facesConfigExtensions != null && facesConfigExtensions.getLength() > 0) {
                processFacesConfigExtensions(facesConfigExtensions, namespace, documentInfo);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void processFacesConfigExtensions(NodeList facesConfigExtensions, String namespace, DocumentInfo info) {
        WebConfiguration config = null;

        for (int i = 0, size = facesConfigExtensions.getLength(); i < size; i++) {
            Node facesConfigExtension = facesConfigExtensions.item(i);
            NodeList children = ((Element) facesConfigExtension).getElementsByTagNameNS(namespace, "*");
            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                if (FACELETS_PROCESSING.equals(n.getLocalName())) {
                    Node faceletsProcessing = n;
                    NodeList faceletsProcessingChildren = ((Element) faceletsProcessing).getElementsByTagNameNS(namespace, "*");
                    String fileExtension = null, processAs = null;
                    for (int fp = 0, fpsize = faceletsProcessingChildren.getLength(); fp < fpsize; fp++) {
                        Node childOfInterset = faceletsProcessingChildren.item(fp);
                        if (null == fileExtension && FILE_EXTENSION.equals(childOfInterset.getLocalName())) {
                            fileExtension = getNodeText(childOfInterset);
                        } else if (null == processAs && PROCESS_AS.equals(childOfInterset.getLocalName())) {
                            processAs = getNodeText(childOfInterset);
                        } else {
                            if (LOGGER.isLoggable(WARNING)) {
                                LOGGER.log(WARNING, format(
                                        "Processing faces-config-extension elements for document: ''{0}'', encountered unexpected configuration ''{1}'', ignoring and continuing",
                                        info.getSourceURI(), getNodeText(childOfInterset)));
                            }
                        }

                    }

                    if (null != fileExtension && null != processAs) {
                        if (null == config) {
                            config = WebConfiguration.getInstance();
                        }
                        Map<String, String> faceletsProcessingMappings = config
                                .getFacesConfigOptionValue(WebConfiguration.WebContextInitParameter.FaceletsProcessingFileExtensionProcessAs, true);
                        faceletsProcessingMappings.put(fileExtension, processAs);

                    } else {
                        if (LOGGER.isLoggable(WARNING)) {
                            LOGGER.log(WARNING, MessageFormat.format(
                                    "Processing faces-config-extension elements for document: ''{0}'', encountered <facelets-processing> elemnet without expected children",
                                    info.getSourceURI()));
                        }
                    }
                }
            }
        }

    }

}
