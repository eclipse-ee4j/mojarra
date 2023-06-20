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

import static com.sun.faces.util.MessageUtils.RENDERER_CANNOT_BE_REGISTERED_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ClientBehaviorRenderer;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;
import jakarta.servlet.ServletContext;

/**
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/render-kit</code>.
 */
public class RenderKitConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /faces-config/render-kit
     * </p>
     */
    private static final String RENDERKIT = "render-kit";

    /**
     * <p>
     * /faces-config/render-kit/render-kit-id
     * </p>
     */
    private static final String RENDERKIT_ID = "render-kit-id";

    /**
     * <p>
     * /faces-config/render-kit/render-kit-class
     * </p>
     */
    private static final String RENDERKIT_CLASS = "render-kit-class";

    /**
     * <p>
     * /faces-config/render-kit/renderer
     * </p>
     */
    private static final String RENDERER = "renderer";

    /**
     * <p>
     * /faces-config/render-kit/renderer/component-family
     * </p>
     */
    private static final String RENDERER_FAMILY = "component-family";

    /**
     * <p>
     * /faces-config/render-kit/renderer/renderer-type
     * </p>
     */
    private static final String RENDERER_TYPE = "renderer-type";

    /**
     * <p>
     * /faces-config/render-kit/renderer/renderer-class
     * </p>
     */
    private static final String RENDERER_CLASS = "renderer-class";

    /**
     * <p>
     * /faces-config/render-kit/client-behavior-renderer
     * </p>
     */
    private static final String CLIENT_BEHAVIOR_RENDERER = "client-behavior-renderer";

    /**
     * <p>
     * /faces-config/render-kit/client-behavior-renderer/client-behavior-renderer-type
     * </p>
     */
    private static final String CLIENT_BEHAVIOR_RENDERER_TYPE = "client-behavior-renderer-type";

    /**
     * <p>
     * /faces-config/render-kit/client-behavior-renderer/client-behavior-renderer-class
     * </p>
     */
    private static final String CLIENT_BEHAVIOR_RENDERER_CLASS = "client-behavior-renderer-class";

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        Map<String, Map<Document, List<Node>>> renderers = new LinkedHashMap<>();
        Map<String, Map<Document, List<Node>>> behaviorRenderers = new LinkedHashMap<>();
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing render-kit elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList renderkits = document.getDocumentElement().getElementsByTagNameNS(namespace, RENDERKIT);

            if (renderkits != null && renderkits.getLength() > 0) {
                addRenderKits(servletContext, facesContext, renderkits, document, renderers, behaviorRenderers, renderKitFactory);
            }
        }

        // Process annotated Renderers, ClientBehaviorRenderers first as Renderers configured
        // via config files take precedence
        processAnnotations(facesContext, FacesRenderer.class);
        processAnnotations(facesContext, FacesBehaviorRenderer.class);

        // Now add the accumulated renderers to the RenderKits
        for (Map.Entry<String, Map<Document, List<Node>>> entry : renderers.entrySet()) {
            RenderKit renderKit = renderKitFactory.getRenderKit(null, entry.getKey());
            if (renderKit == null) {
                throw new ConfigurationException(getExceptionMessageString(RENDERER_CANNOT_BE_REGISTERED_ID, entry.getKey()));
            }

            for (Map.Entry<Document, List<Node>> renderEntry : entry.getValue().entrySet()) {
                addRenderers(servletContext, facesContext, renderKit, renderEntry.getKey(), renderEntry.getValue());
            }
        }

        // Now add the accumulated behavior renderers to the RenderKits
        for (Map.Entry<String, Map<Document, List<Node>>> entry : behaviorRenderers.entrySet()) {
            RenderKit renderKit = renderKitFactory.getRenderKit(null, entry.getKey());
            if (renderKit == null) {
                throw new ConfigurationException(getExceptionMessageString(RENDERER_CANNOT_BE_REGISTERED_ID, entry.getKey()));
            }

            for (Map.Entry<Document, List<Node>> renderEntry : entry.getValue().entrySet()) {
                addClientBehaviorRenderers(servletContext, facesContext, renderKit, renderEntry.getKey(), renderEntry.getValue());
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void addRenderKits(ServletContext sc, FacesContext facesContext, NodeList renderKits, Document owningDocument,
            Map<String, Map<Document, List<Node>>> renderers, Map<String, Map<Document, List<Node>>> behaviorRenderers, RenderKitFactory renderKitFactory) {

        String namespace = owningDocument.getDocumentElement().getNamespaceURI();
        for (int i = 0, size = renderKits.getLength(); i < size; i++) {

            Node renderKitNode = renderKits.item(i);
            NodeList children = ((Element) renderKitNode).getElementsByTagNameNS(namespace, "*");
            String renderKitId = null;
            String renderKitClass = null;
            List<Node> renderersList = new ArrayList<>(children.getLength());
            List<Node> behaviorRenderersList = new ArrayList<>(children.getLength());

            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                switch (n.getLocalName()) {
                case RENDERKIT_ID:
                    renderKitId = getNodeText(n);
                    break;
                case RENDERKIT_CLASS:
                    renderKitClass = getNodeText(n);
                    break;
                case RENDERER:
                    renderersList.add(n);
                    break;
                case CLIENT_BEHAVIOR_RENDERER:
                    behaviorRenderersList.add(n);
                    break;
                }
            }

            renderKitId = renderKitId == null ? RenderKitFactory.HTML_BASIC_RENDER_KIT : renderKitId;

            if (renderKitClass != null) {
                RenderKit previousRenderKit = renderKitFactory.getRenderKit(facesContext, renderKitId);
                RenderKit renderKit = (RenderKit) createInstance(sc, facesContext, renderKitClass, RenderKit.class, previousRenderKit, renderKitNode);

                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, format("Calling RenderKitFactory.addRenderKit({0}, {1})", renderKitId, renderKitClass));
                }

                renderKitFactory.addRenderKit(renderKitId, renderKit);
            }

            Map<Document, List<Node>> existingRenderers = renderers.get(renderKitId);
            if (existingRenderers != null) {
                List<Node> list = existingRenderers.get(owningDocument);
                if (list != null) {
                    list.addAll(renderersList);
                } else {
                    existingRenderers.put(owningDocument, renderersList);
                }
            } else {
                existingRenderers = new LinkedHashMap<>();
                existingRenderers.put(owningDocument, renderersList);
            }
            renderers.put(renderKitId, existingRenderers);

            Map<Document, List<Node>> existingBehaviorRenderers = behaviorRenderers.get(renderKitId);
            if (existingBehaviorRenderers != null) {
                List<Node> list = existingBehaviorRenderers.get(owningDocument);
                if (list != null) {
                    list.addAll(behaviorRenderersList);
                } else {
                    existingBehaviorRenderers.put(owningDocument, behaviorRenderersList);
                }
            } else {
                existingBehaviorRenderers = new LinkedHashMap<>();
                existingBehaviorRenderers.put(owningDocument, behaviorRenderersList);
            }
            behaviorRenderers.put(renderKitId, existingBehaviorRenderers);

        }

    }

    private void addRenderers(ServletContext sc, FacesContext facesContext, RenderKit renderKit, Document owningDocument, List<Node> renderers) {

        String namespace = owningDocument.getDocumentElement().getNamespaceURI();
        for (Node rendererNode : renderers) {

            NodeList children = ((Element) rendererNode).getElementsByTagNameNS(namespace, "*");
            String rendererFamily = null;
            String rendererType = null;
            String rendererClass = null;

            for (int i = 0, size = children.getLength(); i < size; i++) {
                Node n = children.item(i);
                switch (n.getLocalName()) {
                case RENDERER_FAMILY:
                    rendererFamily = getNodeText(n);
                    break;
                case RENDERER_TYPE:
                    rendererType = getNodeText(n);
                    break;
                case RENDERER_CLASS:
                    rendererClass = getNodeText(n);
                    break;
                }
            }

            if (rendererFamily != null && rendererType != null && rendererClass != null) {

                Renderer renderer = (Renderer) createInstance(sc, facesContext, rendererClass, Renderer.class, null, rendererNode);

                if (renderer != null) {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling RenderKit.addRenderer({0},{1}, {2}) for RenderKit ''{3}''", rendererFamily, rendererType,
                                rendererClass, renderKit.getClass()));
                    }

                    renderKit.addRenderer(rendererFamily, rendererType, renderer);
                }
            }
        }

    }

    private void addClientBehaviorRenderers(ServletContext servletContext, FacesContext facesContext, RenderKit renderKit, Document owningDocument,
            List<Node> behaviorRenderers) {

        String namespace = owningDocument.getDocumentElement().getNamespaceURI();
        for (Node behaviorRendererNode : behaviorRenderers) {

            NodeList children = ((Element) behaviorRendererNode).getElementsByTagNameNS(namespace, "*");
            String behaviorRendererType = null;
            String behaviorRendererClass = null;

            for (int i = 0, size = children.getLength(); i < size; i++) {
                Node n = children.item(i);
                switch (n.getLocalName()) {
                case CLIENT_BEHAVIOR_RENDERER_TYPE:
                    behaviorRendererType = getNodeText(n);
                    break;
                case CLIENT_BEHAVIOR_RENDERER_CLASS:
                    behaviorRendererClass = getNodeText(n);
                    break;
                }
            }

            if (behaviorRendererType != null && behaviorRendererClass != null) {
                ClientBehaviorRenderer behaviorRenderer = (ClientBehaviorRenderer) createInstance(servletContext, facesContext, behaviorRendererClass,
                        ClientBehaviorRenderer.class, null, behaviorRendererNode);

                if (behaviorRenderer != null) {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling RenderKit.addClientBehaviorRenderer({0},{1}, {2}) for RenderKit ''{2}''", behaviorRendererType,
                                behaviorRendererClass, renderKit.getClass()));
                    }

                    renderKit.addClientBehaviorRenderer(behaviorRendererType, behaviorRenderer);
                }
            }
        }

    }

}
