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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;

import jakarta.faces.component.Doctype;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

public class DoctypeRenderer extends Renderer {

    private static final Attribute[] DOCTYPE_ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTPUTDOCTYPE);

    @Override
    public void decode(FacesContext context, UIComponent component) {
        // no-op
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.append(toString((Doctype) component));
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        // no-op
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter();
    }

    public static String toString(Doctype doctype) {
        StringBuilder stringBuilder = new StringBuilder(64);
        stringBuilder.append("<!DOCTYPE ").append(doctype.getRootElement());

        if (doctype.getPublic() != null) {
            stringBuilder.append(" PUBLIC \"").append(doctype.getPublic()).append("\"");
        }

        if (doctype.getSystem() != null) {
            if (doctype.getPublic() == null) {
                stringBuilder.append(" SYSTEM");
            }

            stringBuilder.append(" \"").append(doctype.getSystem()).append("\"");
        }

        stringBuilder.append(">");
        return stringBuilder.toString();
    }

}
