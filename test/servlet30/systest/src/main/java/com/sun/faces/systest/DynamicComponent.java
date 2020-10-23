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

package com.sun.faces.systest;


import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * <p>Test <code>UIComponent</code> for sys tests that dynamically creates
 * or removes children UIOutput components with specified ids and values.
 * It pays attention to the following request parameters in the
 * <code>encodeBegin()</code> method:</p>
 * <ul>
 * <li><code>?mode=create&id=foo&value=bar</code> - Create a new
 * <code>UIOutput</code> child with a component identifier of
 * <code>foo</code> and a value of <code>bar</code> (optional).  Set the
 * <code>rendererType</code> property to <code>Text</code>.  The
 * new child will be appended to the child list.</li>
 * <li><code>?mode=delete&id=foo</code> - Remove any child with a
 * component identifier of <code>foo</code>.</li>
 * </ul>
 *
 * <p>In accordance with our current restrictions, this component sets
 * <code>rendersChildren</code> to <code>true</code>, and recursively
 * renders its children in <code>encodeChildren</code>.  This component
 * itself renders "{" at the beginning and "}" at the end, just like
 * <code>ChildrenComponent</code>.</p>
 */

public class DynamicComponent extends UIComponentBase {


    public static final String COMPONENT_FAMILY = "Dynamic";

    // ------------------------------------------------------------ Constructors


    public DynamicComponent() {
        this("dynamic");
    }


    public DynamicComponent(String componentId) {
        super();
        setId(componentId);
    }


    // ----------------------------------------------------- UIComponent Methods

    public String getFamily() {

        return (COMPONENT_FAMILY);

    }


    public boolean getRendersChildren() {
        return (true);
    }


    public void encodeBegin(FacesContext context) throws IOException {
        process(context);
        ResponseWriter writer = context.getResponseWriter();
        writer.write("{ ");
    }


    public void encodeChildren(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        Iterator kids = getChildren().iterator();
        while (kids.hasNext()) {
            encodeRecursive(context, (UIComponent) kids.next());
            writer.write(" ");
        }
    }


    public void encodeEnd(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.write(" }\n");
    }


    // --------------------------------------------------------- Private Methods


    private void encodeRecursive(FacesContext context, UIComponent component)
        throws IOException {

        component.encodeBegin(context);
        if (component.getRendersChildren()) {
            component.encodeChildren(context);
        } else {
            Iterator kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                encodeRecursive(context, kid);
            }
        }
        component.encodeEnd(context);

    }


    private void process(FacesContext context) {
        Map map = context.getExternalContext().getRequestParameterMap();
        String mode = (String) map.get("mode");
        String id = (String) map.get("id");
        String value = (String) map.get("value");
        if (mode == null) {
            return;
        } else if ("create".equals(mode)) {
            UIOutput output = new UIOutput();
            output.setId(id);
            output.setRendererType("jakarta.faces.Text");
            output.setValue(value);
            getChildren().add(output);
        } else if ("delete".equals(mode)) {
            Iterator kids = getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if (id.equals(kid.getId())) {
                    getChildren().remove(kid);
                    break;
                }
            }
        }

    }


}
