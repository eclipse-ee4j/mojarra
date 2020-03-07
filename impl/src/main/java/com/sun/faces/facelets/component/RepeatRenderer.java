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

package com.sun.faces.facelets.component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

public class RepeatRenderer extends Renderer {

    public RepeatRenderer() {
        super();
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() > 0) {
            Map a = component.getAttributes();
            String tag = (String) a.get("alias.element");
            if (tag != null) {
                ResponseWriter out = context.getResponseWriter();
                out.startElement(tag, component);
                String[] attrs = (String[]) a.get("alias.attributes");
                String attr;
                if (attrs != null) {
                    for (int i = 0; i < attrs.length; i++) {
                        attr = attrs[i];
                        if ("styleClass".equals(attr)) {
                            attr = "class";
                        }
                        out.writeAttribute(attr, a.get(attrs[i]), attrs[i]);
                    }
                }
            }

            Iterator itr = component.getChildren().iterator();
            UIComponent c;
            while (itr.hasNext()) {
                c = (UIComponent) itr.next();
                c.encodeAll(context);
            }

            if (tag != null) {
                context.getResponseWriter().endElement(tag);
            }
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

}
