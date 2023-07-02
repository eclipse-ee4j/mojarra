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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/protected-views</code>.
 *
 */
public class ProtectedViewsConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <code>/faces-config/protected-views</code>
     */
    private static final String PROTECTED_VIEWS = "protected-views";

    /**
     * <code>/faces-config/protected-views/url-pattern</code>
     */
    private static final String URL_PATTERN = "url-pattern";

    // ------------------------------------------------------------ Constructors

    public ProtectedViewsConfigProcessor() {
    }

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {
        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing protected-views element for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList protectedViews = document.getDocumentElement().getElementsByTagNameNS(namespace, PROTECTED_VIEWS);

            if (protectedViews != null && protectedViews.getLength() > 0) {
                processProtectedViews(facesContext, protectedViews, namespace, documentInfo);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void processProtectedViews(FacesContext context, NodeList protectedViews, String namespace, DocumentInfo info) {
        WebConfiguration config = null;
        ViewHandler viewHandler = null;

        for (int i = 0, size = protectedViews.getLength(); i < size; i++) {
            Node urlPatterns = protectedViews.item(i);
            NodeList children = ((Element) urlPatterns).getElementsByTagNameNS(namespace, "*");
            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                String urlPattern = null;
                if (URL_PATTERN.equals(n.getLocalName())) {
                    urlPattern = getNodeText(n);
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, MessageFormat.format(
                                "Processing protected-views elements for document: ''{0}'', encountered unexpected configuration ''{1}'', ignoring and continuing",
                                info.getSourceURI(), getNodeText(n)));
                    }
                }

                if (urlPattern != null) {
                    if (config == null) {
                        config = WebConfiguration.getInstance();
                    }

                    if (viewHandler == null) {
                        viewHandler = context.getApplication().getViewHandler();
                    }

                    viewHandler.addProtectedView(urlPattern);

                } else {
                    if (LOGGER.isLoggable(WARNING)) {
                        LOGGER.log(WARNING,
                                format("Processing protected-views elements for document: ''{0}'', encountered <url-pattern> element without expected children",
                                        info.getSourceURI()));
                    }
                }
            }
        }

    }

}
