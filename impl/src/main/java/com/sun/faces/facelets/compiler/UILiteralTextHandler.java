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

package com.sun.faces.facelets.compiler;

import java.io.IOException;

import com.sun.faces.facelets.tag.faces.ComponentSupport;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.view.facelets.FaceletContext;

final class UILiteralTextHandler extends AbstractUIHandler {

    protected final String txtString;

    public UILiteralTextHandler(String txtString) {
        this.txtString = txtString;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent != null) {
            UIComponent c = new UILiteralText(txtString);
            String uid;
            UIComponent ancestorNamingContainer = parent.getNamingContainer();
            if (null != ancestorNamingContainer && ancestorNamingContainer instanceof UniqueIdVendor) {
                uid = ((UniqueIdVendor) ancestorNamingContainer).createUniqueId(ctx.getFacesContext(), null);
            } else {
                uid = ComponentSupport.getViewRoot(ctx, parent).createUniqueId();
            }
            c.setId(uid);
            addComponent(ctx, parent, c);
        }
    }

    @Override
    public String getText() {
        return txtString;
    }

    @Override
    public String getText(FaceletContext ctx) {
        return txtString;
    }
}
