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
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/behavior</code>.
 * </p>
 */
public class BehaviorConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /faces-config/behavior
     * </p>
     */
    private static final String BEHAVIOR = "behavior";

    /**
     * <p>
     * /faces-config/behavior/behavior-id
     * </p>
     */
    private static final String BEHAVIOR_ID = "behavior-id";

    /**
     * <p>
     * /faces-config/behavior/behavior-class
     * </p>
     */
    private static final String BEHAVIOR_CLASS = "behavior-class";

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        // process annotated Behaviors first as Behaviors configured
        // via config files take precedence
        processAnnotations(facesContext, FacesBehavior.class);

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing behavior elements for document: ''{0}''", documentInfo.getSourceURI()));
            }
            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList behaviors = document.getDocumentElement().getElementsByTagNameNS(namespace, BEHAVIOR);
            if (behaviors != null && behaviors.getLength() > 0) {
                addBehaviors(behaviors, namespace);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void addBehaviors(NodeList behaviors, String namespace) throws XPathExpressionException {
        Application app = getApplication();
        Verifier verifier = Verifier.getCurrentInstance();
        for (int i = 0, size = behaviors.getLength(); i < size; i++) {
            Node behavior = behaviors.item(i);

            NodeList children = ((Element) behavior).getElementsByTagNameNS(namespace, "*");
            String behaviorId = null;
            String behaviorClass = null;
            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getLocalName()) {
                    case BEHAVIOR_ID:
                        behaviorId = getNodeText(n);
                        break;
                    case BEHAVIOR_CLASS:
                        behaviorClass = getNodeText(n);
                        break;
                    }
                }
            }

            if (behaviorId != null && behaviorClass != null) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, MessageFormat.format("Calling Application.addBehavior({0},{1})", behaviorId, behaviorClass));
                }
                if (verifier != null) {
                    verifier.validateObject(Verifier.ObjectType.BEHAVIOR, behaviorClass, Behavior.class);
                }
                app.addBehavior(behaviorId, behaviorClass);
            }

        }
    }

}
