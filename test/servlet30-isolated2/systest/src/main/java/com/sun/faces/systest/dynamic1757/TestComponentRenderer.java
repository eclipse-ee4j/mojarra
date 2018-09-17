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

package com.sun.faces.systest.dynamic1757;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

@FacesRenderer(componentFamily = "com.sun.faces.systest.dynamic1757.UITestComponent", rendererType = "testcomponent")
public class TestComponentRenderer extends Renderer {
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter().write("<div style=\"border: 1px solid red\">TestComponent::encodeBegin<br/>");

        super.encodeBegin(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        context.getResponseWriter().write("TestComponent::encodeEnd</div>");

        super.encodeEnd(context, component);
    }

}
