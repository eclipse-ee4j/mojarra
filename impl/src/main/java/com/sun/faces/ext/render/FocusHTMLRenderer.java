/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.ext.render;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

/**
 * Renderer class that emits HTML and JavaScript to set the focus to a given field.
 *
 * @author driscoll
 */
public class FocusHTMLRenderer extends Renderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        String forID = (String) component.getAttributes().get("for");
        ResponseWriter writer = context.getResponseWriter();
        // XXX - I'd still like to get the parentID, but need to add a check if it's a form or not...
        // UIComponent parentComponent = component.getParent();
        // String parentID = parentComponent.getClientId(context);
        // String targetID = parentID+":"+forID;
        String targetID = forID;
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeText("setFocus('", null);
        writer.writeText(targetID, null);
        writer.writeText("');\n", null);
        writer.writeText("function setFocus(elementId) { var element = " + "document.getElementById(elementId); if (element && element.focus) "
                + "{ element.focus(); } }", null);
        writer.endElement("script");
    }

}
