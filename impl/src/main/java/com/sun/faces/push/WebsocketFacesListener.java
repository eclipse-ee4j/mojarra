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

package com.sun.faces.push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UIWebsocket;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.push.Push;

/**
 * <p class="changed_added_2_3">
 * This Faces listener for {@link UIViewRoot} ensures that the necessary JavaScript code to open or close the
 * <code>WebSocket</code> is properly rendered depending on <code>rendered</code> and <code>connected</code> attributes.
 *
 * @author Bauke Scholtz
 * @see Push
 * @since 2.3
 */
public class WebsocketFacesListener implements SystemEventListener {

    // Constants ------------------------------------------------------------------------------------------------------

    private static final String SCRIPT_OPEN = "jsf.push.open('%s');";
    private static final String SCRIPT_CLOSE = "jsf.push.close('%s');";

    // Initialization -------------------------------------------------------------------------------------------------

    public static void subscribeIfNecessary(FacesContext context) {
        UIViewRoot view = context.getViewRoot();
        List<SystemEventListener> listeners = view.getListenersForEventClass(PostAddToViewEvent.class);

        if (listeners == null || !listeners.stream().anyMatch(l -> l instanceof WebsocketFacesListener)) {
            view.subscribeToViewEvent(PreRenderViewEvent.class, new WebsocketFacesListener());
        }
    }

    public static boolean isNew(FacesContext context, UIWebsocket websocket) {
        return getInitializedWebsockets(context).putIfAbsent(websocket.getClientId(context), websocket.isConnected()) == null;
    }

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * Only listens on {@link UIViewRoot}.
     */
    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof UIViewRoot;
    }

    /**
     * If the websocket has just switched its <code>rendered</code> or <code>connected</code> attribute, then render either
     * the <code>open()</code> script or the <code>close()</code> script. During an ajax request with partial rendering,
     * it's added as <code>&lt;eval&gt;</code> by partial response writer, else it's just added as a script component with
     * <code>target="body"</code>.
     */
    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (!(event instanceof PreRenderViewEvent)) {
            return;
        }

        FacesContext context = ((ComponentSystemEvent) event).getFacesContext();
        Map<String, Boolean> initializedWebsockets = getInitializedWebsockets(context);

        if (!context.getPartialViewContext().isAjaxRequest()) {
            initializedWebsockets.clear();
        }

        for (Entry<String, Boolean> initializedWebsocket : initializedWebsockets.entrySet()) {
            String clientId = initializedWebsocket.getKey();
            UIWebsocket websocket = (UIWebsocket) context.getViewRoot().findComponent(clientId);
            boolean connected = websocket.isRendered() && websocket.isConnected();
            boolean previouslyConnected = initializedWebsocket.setValue(connected);

            if (previouslyConnected != connected) {
                String script = String.format(connected ? SCRIPT_OPEN : SCRIPT_CLOSE, clientId);
                PartialViewContext pvc = context.getPartialViewContext();

                if (pvc.isAjaxRequest() && !pvc.isRenderAll()) {
                    context.getPartialViewContext().getEvalScripts().add(script);
                } else {
                    UIOutput outputScript = new UIOutput();
                    outputScript.setRendererType("jakarta.faces.resource.Script");
                    UIOutput content = new UIOutput();
                    content.setValue(script);
                    outputScript.getChildren().add(content);
                    context.getViewRoot().addComponentResource(context, outputScript, "body");
                }
            }
        }
    }

    // Helpers --------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> getInitializedWebsockets(FacesContext context) {
        Map<String, Object> viewScope = context.getViewRoot().getViewMap();
        Map<String, Boolean> initializedWebsockets = (Map<String, Boolean>) viewScope.get(WebsocketFacesListener.class.getName());

        if (initializedWebsockets == null) {
            initializedWebsockets = new HashMap<>();
            viewScope.put(WebsocketFacesListener.class.getName(), initializedWebsockets);
        }

        return initializedWebsockets;
    }

}
