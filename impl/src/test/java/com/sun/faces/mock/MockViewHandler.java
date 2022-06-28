/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.mock;

import java.io.IOException;
import java.util.Locale;

import jakarta.faces.FacesException;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;

public class MockViewHandler extends ViewHandler {

    protected StateManager stateManager = null;

    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender)
            throws IOException, FacesException {
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return null;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        UIViewRoot result = new UIViewRoot();
        result.setViewId(viewId);
        result.setRenderKitId(calculateRenderKitId(context));
        return result;
    }

    @Override
    public void writeState(FacesContext context) {
    }

    public StateManager getStateManager() {
        if (null == stateManager) {
            stateManager = new StateManager() {
            };
        }
        return stateManager;
    }

    @Override
    public String getActionURL(FacesContext context, String viewId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResourceURL(FacesContext context, String path) {
        if (path.startsWith("/")) {
            return context.getExternalContext().getRequestContextPath() + path;
        } else {
            return (path);
        }
    }

    @Override
    public String getWebsocketURL(FacesContext context, String channel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale calculateLocale(FacesContext context) {
        return Locale.getDefault();
    }

    @Override
    public String calculateRenderKitId(FacesContext context) {
        return RenderKitFactory.HTML_BASIC_RENDER_KIT;
    }

}
