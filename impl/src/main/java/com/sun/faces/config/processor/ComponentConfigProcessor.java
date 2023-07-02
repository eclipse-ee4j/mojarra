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

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.Verifier;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.Application;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/component</code>.
 * </p>
 */
public class ComponentConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /faces-config/component
     * </p>
     */
    private static final String COMPONENT = "component";

    /**
     * <p>
     * /faces-config/component/component-type
     * </p>
     */
    private static final String COMPONENT_TYPE = "component-type";

    /**
     * <p>
     * /faces-config/component/component-class
     * </p>
     */
    private static final String COMPONENT_CLASS = "component-class";

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        // Process annotated components first as components configured
        // via config files take precedence
        processAnnotations(facesContext, FacesComponent.class);

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing component elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList components = document.getDocumentElement().getElementsByTagNameNS(namespace, COMPONENT);
            if (components != null && components.getLength() > 0) {
                addComponents(components, namespace);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void addComponents(NodeList components, String namespace) throws XPathExpressionException {
        Application app = getApplication();
        Verifier verifier = Verifier.getCurrentInstance();
        for (int i = 0, size = components.getLength(); i < size; i++) {
            Node componentNode = components.item(i);
            NodeList children = ((Element) componentNode).getElementsByTagNameNS(namespace, "*");
            String componentType = null;
            String componentClass = null;
            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                switch (n.getLocalName()) {
                case COMPONENT_TYPE:
                    componentType = getNodeText(n);
                    break;
                case COMPONENT_CLASS:
                    componentClass = getNodeText(n);
                    break;
                }
            }

            if (componentType != null && componentClass != null) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, MessageFormat.format("Calling Application.addComponent({0},{1})", componentType, componentClass));
                }
                if (verifier != null) {
                    verifier.validateObject(Verifier.ObjectType.COMPONENT, componentClass, UIComponent.class);
                }
                app.addComponent(componentType, componentClass);
            }
        }
    }

}
