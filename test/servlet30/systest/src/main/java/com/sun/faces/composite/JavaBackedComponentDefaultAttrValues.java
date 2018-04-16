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

package com.sun.faces.composite;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

@FacesComponent(value="JavaBackedComponentDefaultAttrValues")
public class JavaBackedComponentDefaultAttrValues extends UINamingContainer {

    @Override
    public String getFamily() {
        return "javax.faces.NamingContainer";
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException {
        super.encodeAll(context);
        ResponseWriter responseWriter = context.getResponseWriter();
        Map<String, Object> attrs = this.getAttributes();
        String clientId = this.getClientId(context);
        responseWriter.startElement("p", this);
        responseWriter.writeAttribute("id", clientId, "clientId");
        responseWriter.writeText("attr1 value is " + attrs.get("attr1").toString() + ". ", "attr1");
        responseWriter.writeText("attr2 value is " + attrs.get("attr2").toString() + ". ", "attr2");
        responseWriter.writeText("attr3 value is " + attrs.get("attr3").toString() + ". ", "attr3");
        responseWriter.writeText("action value is " + attrs.get("action").toString() + ". ", "action");
        responseWriter.writeText("actionListener value is " + attrs.get("action").toString() + ". ", "action");
        responseWriter.writeText("validator value is " + attrs.get("validator").toString() + ". ", "validator");
        responseWriter.writeText("valueChangeListener value is " + attrs.get("valueChangeListener").toString() + ". ", "valueChangeListener");
        responseWriter.endElement("p");
    }



}
