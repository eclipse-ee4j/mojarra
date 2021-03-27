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
import java.io.Writer;

import com.sun.faces.facelets.el.ELText;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.facelets.util.FastWriter;

import jakarta.el.ELException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.view.facelets.FaceletContext;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
final class UITextHandler extends AbstractUIHandler {

    private final ELText txt;

    private final String alias;

    private final int length;

    public UITextHandler(String alias, ELText txt) {
        this.alias = alias;
        this.txt = txt;
        length = txt.toString().length();
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent != null) {
            try {
                ELText nt = txt.apply(ctx.getExpressionFactory(), ctx);
                UIComponent c = new UIText(alias, nt);
                String uid;
                UIComponent ancestorNamingContainer = parent.getNamingContainer();
                if (null != ancestorNamingContainer && ancestorNamingContainer instanceof UniqueIdVendor) {
                    uid = ((UniqueIdVendor) ancestorNamingContainer).createUniqueId(ctx.getFacesContext(), null);
                } else {
                    uid = ComponentSupport.getViewRoot(ctx, parent).createUniqueId();
                }

                c.setId(uid);
                addComponent(ctx, parent, c);
            } catch (Exception e) {
                throw new ELException(alias + ": " + e.getMessage(), e.getCause());
            }
        }
    }

    @Override
    public String toString() {
        return txt.toString();
    }

    @Override
    public String getText() {
        return txt.toString();
    }

    @Override
    public String getText(FaceletContext ctx) {
        Writer writer = new FastWriter(length);
        try {
            txt.apply(ctx.getExpressionFactory(), ctx).write(writer, ctx);
        } catch (IOException e) {
            throw new ELException(alias + ": " + e.getMessage(), e.getCause());
        }
        return writer.toString();
    }
}
