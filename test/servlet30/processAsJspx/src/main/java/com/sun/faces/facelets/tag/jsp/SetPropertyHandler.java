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
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;




public class SetPropertyHandler extends TagHandler {

    private final TagAttribute name;

    private final TagAttribute property;

    private final TagAttribute param;

    private final TagAttribute value;

    public SetPropertyHandler(TagConfig config) {
        super(config);
        this.name = this.getRequiredAttribute("name");
        this.property = this.getRequiredAttribute("property");
        this.param = this.getAttribute("param");
        this.value = this.getAttribute("value");

    }

    public void apply(FaceletContext fc, UIComponent uic) throws IOException {
        FacesContext facesContext = fc.getFacesContext();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory ef = facesContext.getApplication().getExpressionFactory();

        // Get the bean
        String nameVal = this.name.getValue(fc);
        ValueExpression valExpression = ef.createValueExpression(elContext,
                "#{" + nameVal + "}", Object.class);
        Object bean = valExpression.getValue(elContext);
        if (null == bean) {
            throw new FaceletException("Bean " + nameVal + " not found.");
        }

        // Get the name of the property of the bean to set
        String propertyVal = this.property.getValue(fc);
        String lhs = null;
        Object rhs = null;
        if (propertyVal.equals("*")) {
            pushAllRequestParamatersToBeanProperties(facesContext, elContext, ef, bean);
        } else {
            // If both are set, it is a user error.
            if (null != this.param && null != this.value) {
                throw new FaceletException("You cannot use both the param and value attributes in a <jsp:setProperty> element.");
            }
            // if neither param nor value have values, assume the name of
            // the request parameter is equal to propertyVal
            if (null == this.param && null == this.value) {
                lhs = propertyVal;
            } else {
                // one of param or value have a value.
                if (null != this.param) {
                    lhs = this.param.getValue(fc);
                }
                if (null != this.value) {
                    lhs = propertyVal;
                    rhs = this.value.getValue(fc);
                }
            }
            if (null == rhs) {
                rhs = facesContext.getExternalContext().getRequestParameterMap().get(lhs);
            }
            ELResolver resolver = elContext.getELResolver();
            resolver.setValue(elContext, bean, lhs, rhs);
        }

        nextHandler.apply(fc, uic);

    }

    private void pushAllRequestParamatersToBeanProperties(FacesContext facesContext,
            ELContext elContext, ExpressionFactory ef, Object bean) {
        ExternalContext extContext = facesContext.getExternalContext();
        ELResolver resolver = elContext.getELResolver();
        Map<String, String []> requestParamValues =
                extContext.getRequestParameterValuesMap();
        String [] values;
        for (String cur : requestParamValues.keySet()) {
            values = requestParamValues.get(cur);
            for (String curVal : values) {
                resolver.setValue(elContext, bean, cur, curVal);
            }
        }
    }


}
