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

package com.sun.faces.facelets.tag.jsp;

import java.io.IOException;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import javax.faces.view.facelets.FaceletException;

@FacesRenderer(componentFamily = "javax.faces.Output", rendererType = "jsp.GetProperty")
public class GetPropertyRenderer extends Renderer {

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent component) throws IOException {
        Map<String, Object> attrs = component.getAttributes();
        String nameVal = (String) attrs.get("name");
        String propVal = (String) attrs.get("property");
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory ef = facesContext.getApplication().getExpressionFactory();

        // Get the bean
        ValueExpression valExpression = ef.createValueExpression(elContext, "#{" + nameVal + "." + propVal + "}", Object.class);
        Object bean = null;
        try {
            bean = valExpression.getValue(elContext);
        } catch (Exception e) {
            throw new FaceletException("Expression " + valExpression.getExpressionString() + " not found.", e);
        }
        ResponseWriter out = facesContext.getResponseWriter();
        out.writeText(bean, component, valExpression.getExpressionString());
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
    }

}
