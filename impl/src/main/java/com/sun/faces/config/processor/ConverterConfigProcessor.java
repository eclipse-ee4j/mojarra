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

import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.Verifier;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/converter</code>.
 * </p>
 */
public class ConverterConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <code>/faces-config/converter</code>
     */
    private static final String CONVERTER = "converter";

    /**
     * <code>/faces-config/converter/converter-id</code> (mutually exclusive with converter-for-class)
     */
    private static final String CONVERTER_ID = "converter-id";

    /**
     * <code>/faces-config/converter/converter-for-class</code> (mutually exclusive with converter-id)
     */
    private static final String CONVERTER_FOR_CLASS = "converter-for-class";

    /**
     * <code>/faces-config/converter/converter-class</code>
     */
    private static final String CONVERTER_CLASS = "converter-class";

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        // Process annotated converters first as converters configured
        // via config files take precedence
        processAnnotations(facesContext, FacesConverter.class);

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing converter elements for document: ''{0}''", documentInfo.getSourceURI()));
            }
            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList nodes = document.getDocumentElement().getElementsByTagNameNS(namespace, CONVERTER);
            if (nodes != null && nodes.getLength() > 0) {
                addConverters(nodes, namespace);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void addConverters(NodeList converters, String namespace) {

        Application application = getApplication();
        Verifier verifier = Verifier.getCurrentInstance();
        for (int i = 0, size = converters.getLength(); i < size; i++) {
            Node converter = converters.item(i);
            NodeList children = ((Element) converter).getElementsByTagNameNS(namespace, "*");
            String converterId = null;
            String converterClass = null;
            String converterForClass = null;
            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                switch (n.getLocalName()) {
                case CONVERTER_ID:
                    converterId = getNodeText(n);
                    break;
                case CONVERTER_CLASS:
                    converterClass = getNodeText(n);
                    break;
                case CONVERTER_FOR_CLASS:
                    converterForClass = getNodeText(n);
                    break;
                }
            }

            if (converterId != null && converterClass != null) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, format("[Converter by ID] Calling Application.addConverter({0}, {1}", converterId, converterClass));
                }
                if (verifier != null) {
                    verifier.validateObject(Verifier.ObjectType.CONVERTER, converterClass, Converter.class);
                }
                application.addConverter(converterId, converterClass);
            } else if (converterClass != null && converterForClass != null) {
                try {
                    Class<?> cfcClass = Util.loadClass(converterForClass, this.getClass());
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("[Converter for Class] Calling Application.addConverter({0}, {1}", converterForClass, converterClass));
                    }
                    if (verifier != null) {
                        verifier.validateObject(Verifier.ObjectType.CONVERTER, converterClass, Converter.class);
                    }
                    application.addConverter(cfcClass, converterClass);
                } catch (ClassNotFoundException cnfe) {
                    throw new ConfigurationException(cnfe);
                }
            }
        }
    }

}
