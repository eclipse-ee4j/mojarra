/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class WrapHttpServletRequestToAddParams extends HttpServletRequestWrapper {

    private Map<String, ValueExpression> params = Collections.emptyMap();

    public WrapHttpServletRequestToAddParams(FacesContext facesContext, Map<String, ValueExpression> toCopy, HttpServletRequest request) {
        super(request);
        if (!toCopy.isEmpty()) {
            params = new HashMap<String, ValueExpression>();
            ELContext elContext = facesContext.getELContext();
            ExpressionFactory ef = facesContext.getApplication().getExpressionFactory();
            for (String cur : toCopy.keySet()) {
                params.put(cur, ef.createValueExpression(elContext, toCopy.get(cur).getExpressionString(), Object.class));
            }
        }
    }

    @Override
    public String getParameter(String name) {
        return (String) this.getAttribute(name);
    }



    @Override
    public Object getAttribute(String name) {
        Object result = null;
        if (params.containsKey(name)) {
            ValueExpression ve = params.get(name);
            result = ve.getValue(FacesContext.getCurrentInstance().getELContext());
        } else {
            result = super.getAttribute(name);
        }
        return result;
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(params.keySet());
    }
}
