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

package com.sun.faces.lifecycle;

import static com.sun.faces.context.SessionMap.getMutex;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.CLIENT_WINDOW_PARAM;

import java.util.Map;

import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.ResponseStateManager;

public class ClientWindowImpl extends ClientWindow {

    String id;

    public ClientWindowImpl() {
    }

    @Override
    public Map<String, String> getQueryURLParameters(FacesContext context) {
        return null;
    }

    @Override
    public void decode(FacesContext context) {
        Map<String, String> requestParamMap = context.getExternalContext().getRequestParameterMap();
        if (isClientWindowRenderModeEnabled(context)) {
            id = requestParamMap.get(ResponseStateManager.CLIENT_WINDOW_URL_PARAM);
        }
        // The hidden field always takes precedence, if present.
        String paramName = CLIENT_WINDOW_PARAM.getName(context);
        if (requestParamMap.containsKey(paramName)) {
            id = requestParamMap.get(paramName);
        }
        if (null == id) {
            id = calculateClientWindow(context);
        }
    }

    private String calculateClientWindow(FacesContext context) {
        synchronized (getMutex(context.getExternalContext().getSession(true))) {
            final String clientWindowCounterKey = "com.sun.faces.lifecycle.ClientWindowCounterKey";
            ExternalContext extContext = context.getExternalContext();
            Map<String, Object> sessionAttrs = extContext.getSessionMap();
            Integer counter = (Integer) sessionAttrs.get(clientWindowCounterKey);
            if (null == counter) {
                counter = Integer.valueOf(0);
            }
            char sep = UINamingContainer.getSeparatorChar(context);
            id = extContext.getSessionId(true) + sep + +counter;

            sessionAttrs.put(clientWindowCounterKey, ++counter);
        }
        return id;
    }

    @Override
    public String getId() {
        return id;
    }
}
