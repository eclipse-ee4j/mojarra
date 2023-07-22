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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/lifecycle</code>.
 * </p>
 */
public class LifecycleConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /faces-config/lifecycle
     * </p>
     */
    private static final String LIFECYCLE = "lifecycle";

    /**
     * <p>
     * /faces-config/lifecycle/phase-listener
     * </p>
     */
    private static final String PHASE_LISTENER = "phase-listener";
    private final List<PhaseListener> appPhaseListeners;

    public LifecycleConfigProcessor() {
        appPhaseListeners = new CopyOnWriteArrayList<>();
    }

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing lifecycle elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList lifecycles = document.getElementsByTagNameNS(namespace, LIFECYCLE);

            if (lifecycles != null) {
                for (int c = 0, csize = lifecycles.getLength(); c < csize; c++) {
                    Node lifecyleNode = lifecycles.item(c);
                    if (lifecyleNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList listeners = ((Element) lifecyleNode).getElementsByTagNameNS(namespace, PHASE_LISTENER);
                        addPhaseListeners(servletContext, facesContext, factory, listeners);
                    }
                }
            }
        }

    }

    @Override
    public void destroy(ServletContext sc, FacesContext facesContext) {
        destroyInstances(sc, facesContext, appPhaseListeners);
    }

    // --------------------------------------------------------- Private Methods

    private void addPhaseListeners(ServletContext sc, FacesContext facesContext, LifecycleFactory factory, NodeList phaseListeners) {

        if (phaseListeners != null && phaseListeners.getLength() > 0) {
            for (int i = 0, size = phaseListeners.getLength(); i < size; i++) {
                Node phaseListenerNode = phaseListeners.item(i);
                String phaseListenerClassName = getNodeText(phaseListenerNode);

                if (phaseListenerClassName != null) {
                    boolean[] didPerformInjection = { false };

                    PhaseListener phaseListener = (PhaseListener) createInstance(sc, facesContext, phaseListenerClassName, PhaseListener.class, null,
                            phaseListenerNode, true, didPerformInjection);

                    if (phaseListener != null) {
                        if (didPerformInjection[0]) {
                            appPhaseListeners.add(phaseListener);
                        }

                        for (Iterator<String> t = factory.getLifecycleIds(); t.hasNext();) {
                            String lfId = t.next();
                            Lifecycle lifecycle = factory.getLifecycle(lfId);
                            if (LOGGER.isLoggable(FINE)) {
                                LOGGER.log(FINE, format("Adding PhaseListener ''{0}'' to lifecycle ''{0}}", phaseListenerClassName, lfId));
                            }

                            lifecycle.addPhaseListener(phaseListener);
                        }
                    }
                }
            }
        }
    }

    private void destroyInstances(ServletContext sc, FacesContext facesContext, List<?> instances) {
        for (Object instance : instances) {
            destroyInstance(sc, facesContext, instance.getClass().getName(), instance);
        }

        instances.clear();
    }

}
