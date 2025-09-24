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

// CommandScriptRenderer.java

package com.sun.faces.renderkit.html_basic;

import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static jakarta.faces.component.behavior.ClientBehaviorContext.createClientBehaviorContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIWebsocket;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;

import com.sun.faces.push.WebsocketChannelManager;
import com.sun.faces.push.WebsocketFacesListener;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * <b>WebsocketRenderer</b> is a class that renders the <code>faces.push.init()</code> script and decodes any client
 * behaviors triggered by {@link UIWebsocket}.
 *
 * @author Bauke Scholtz
 * @since 2.3
 * @see UIWebsocket
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class WebsocketRenderer extends HtmlBasicRenderer implements ComponentSystemEventListener {

    // Constants ------------------------------------------------------------------------------------------------------

    public static final String RENDERER_TYPE = "jakarta.faces.Websocket";

    private static final String SCRIPT_INIT = "faces.push.init('%s','%s','%s',%s,%s,%s);";

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * After adding component to view, subscribe {@link WebsocketFacesListener} if necessary.
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        WebsocketFacesListener.subscribeIfNecessary(event.getFacesContext());
    }

    /**
     * Decode all client behaviors.
     */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        decodeBehaviors(context, component);
    }

    /**
     * Render <code>faces.push.init()</code> function if necessary.
     */
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        UIWebsocket websocket = (UIWebsocket) component;

        if (WebsocketFacesListener.isNew(context, websocket)) {
            WebsocketChannelManager websocketChannelManager = getBeanReference(WebsocketChannelManager.class);

            String clientId = websocket.getClientId(context);
            String channel = websocket.getChannel();
            String url = websocketChannelManager.register(context, channel, websocket.getScope(), websocket.getUser());
            String functions = websocket.getOnopen() + "," + websocket.getOnmessage() + "," + websocket.getOnerror() + "," + websocket.getOnclose();
            String behaviors = getBehaviorScripts(context, websocket);
            boolean connected = websocket.isConnected();

            RenderKitUtils.renderFacesJsIfNecessary(context);

            RenderKitUtils.renderScript(context, component, clientId, String.format(SCRIPT_INIT, clientId, url, channel, functions, behaviors, connected));
        }
    }

    // Helpers --------------------------------------------------------------------------------------------------------

    /**
     * Helper to collect all client behavior scripts of websocket into a string representing a JS object.
     */
    private static String getBehaviorScripts(FacesContext context, UIWebsocket websocket) {
        Map<String, List<ClientBehavior>> clientBehaviorsByEvent = websocket.getClientBehaviors();

        if (clientBehaviorsByEvent.isEmpty()) {
            return "{}";
        }

        String clientId = websocket.getClientId(context);
        StringBuilder scripts = new StringBuilder("{");

        for (Entry<String, List<ClientBehavior>> entry : clientBehaviorsByEvent.entrySet()) {
            String event = entry.getKey();
            List<ClientBehavior> clientBehaviors = entry.getValue();
            scripts.append(scripts.length() > 1 ? "," : "").append(event).append(":[");

            for (int i = 0; i < clientBehaviors.size(); i++) {
                scripts.append(i > 0 ? "," : "").append("function(event){");
                scripts.append(clientBehaviors.get(i).getScript(createClientBehaviorContext(context, websocket, event, clientId, null)));
                scripts.append("}");
            }

            scripts.append("]");
        }

        return scripts.append("}").toString();
    }

}
