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
import java.util.List;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.MessageUtils;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

final class EndElementInstruction implements Instruction {

    private static final String HEAD_ELEMENT = "head";
    private static final String BODY_ELEMENT = "body";

    private final String element;

    public EndElementInstruction(String element) {
        this.element = element;
    }

    @Override
    public void write(FacesContext context) throws IOException {
        if (HEAD_ELEMENT.equalsIgnoreCase(element)) {
            warnUnhandledResources(context, HEAD_ELEMENT);
        }
        if (BODY_ELEMENT.equalsIgnoreCase(element)) {
            warnUnhandledResources(context, BODY_ELEMENT);
            RenderKitUtils.renderUnhandledMessages(context);
        }
        context.getResponseWriter().endElement(element);
    }

    @Override
    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    // --------------------------------------------------------- Private Methods

    private void warnUnhandledResources(FacesContext ctx, String target) {

        UIViewRoot root = ctx.getViewRoot();
        if (root != null) {
            List<UIComponent> headResources = root.getComponentResources(ctx, target);
            if (headResources != null && !headResources.isEmpty()) {
                FacesMessage m = MessageUtils.getExceptionMessage(MessageUtils.NO_RESOURCE_TARGET_AVAILABLE, target);
                ctx.addMessage(null, m);
            }
        }

    }
}
