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

package com.sun.faces.application;

import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.util.Map;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p>
 * A <code>StateManager</code> implementation to meet the requirements of the specification.
 * </p>
 */
public class StateManagerImpl extends StateManager {

    /**
     * Save the view.
     *
     * @param context the Faces context.
     * @return the saved view.
     */
    @Override
    public Object saveView(FacesContext context) {
        if (context == null || context.getViewRoot().isTransient()) {
            return null;
        }

        String viewId = context.getViewRoot().getViewId();
        ViewDeclarationLanguage vdl = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, viewId);
        if (vdl == null) {
            return null;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        try {
            contextAttributes.put(IS_SAVING_STATE, TRUE);

            return vdl.getStateManagementStrategy(context, viewId)
                      .saveView(context);
        } finally {
            contextAttributes.remove(IS_SAVING_STATE);
        }
    }

    /**
     * Write out the state.
     *
     * @param context the Faces context.
     * @param state the state.
     * @throws IOException when an I/O error occurs.
     */
    @Override
    public void writeState(FacesContext context, Object state) throws IOException {
        context.getRenderKit()
               .getResponseStateManager()
               .writeState(context, state);
    }

    /**
     * Restores the view.
     *
     * @param context the Faces context.
     * @param viewId the view id.
     * @param renderKitId the render kit id.
     * @return the view root.
     * @see StateManager#restoreView(jakarta.faces.context.FacesContext, java.lang.String, java.lang.String)
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
        ViewDeclarationLanguage vdl = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, viewId);
        if (vdl == null) {
            return null;
        }

        return vdl.getStateManagementStrategy(context, viewId)
                  .restoreView(context, viewId, renderKitId);
    }
}
