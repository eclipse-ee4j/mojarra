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

    public void renderView(FacesContext context, UIViewRoot viewToRender)
            throws IOException, FacesException {
    }

    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return null;
    }

    public UIViewRoot createView(FacesContext context, String viewId) {
        UIViewRoot result = new UIViewRoot();
        result.setViewId(viewId);
        result.setRenderKitId(calculateRenderKitId(context));
        return result;
    }

    public void writeState(FacesContext context) {
    }

    public StateManager getStateManager() {
        if (null == stateManager) {
            stateManager = new StateManager() {
                protected Object getTreeStructureToSave(FacesContext context) {
                    return null;
                }

                protected Object getComponentStateToSave(FacesContext context) {
                    return null;
                }

                public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
                    return null;
                }

                public SerializedView saveSerializedView(FacesContext context) {
                    return null;
                }

                public void writeState(FacesContext context,
                        SerializedView state) throws IOException {
                }

                protected UIViewRoot restoreTreeStructure(FacesContext context,
                        String viewId, String renderKitId) {
                    return null;
                }

                protected void restoreComponentState(FacesContext context, UIViewRoot root, String renderKitId) {
                }
            };
        }
        return stateManager;
    }

    public String getActionURL(FacesContext context, String viewId) {
        throw new UnsupportedOperationException();
    }

    public String getResourceURL(FacesContext context, String path) {
        if (path.startsWith("/")) {
            return context.getExternalContext().getRequestContextPath() + path;
        } else {
            return (path);
        }
    }

    public String getWebsocketURL(FacesContext context, String channel) {
        throw new UnsupportedOperationException();
    }

    public Locale calculateLocale(FacesContext context) {
        return Locale.getDefault();
    }

    public String calculateRenderKitId(FacesContext context) {
        return RenderKitFactory.HTML_BASIC_RENDER_KIT;
    }

}
