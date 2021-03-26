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

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

public class MockStateManager extends StateManager {

    protected Object getTreeStructureToSave(FacesContext context) {
        return null;
    }

    protected Object getComponentStateToSave(FacesContext context) {
        return null;
    }

    public UIViewRoot restoreView(FacesContext context, String viewId,
            String renderKitId) {
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

    @Override
    public String getViewState(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSavingStateInClient(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object saveView(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeState(FacesContext arg0, Object arg1) throws IOException {
        throw new UnsupportedOperationException();
    }

}
