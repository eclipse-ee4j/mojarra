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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.faces.test.servlet30.facelets;

import com.sun.faces.component.visit.FullVisitContext;
import java.util.ArrayList;
import java.util.List;
import javax.el.ValueExpression;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

@ManagedBean(name = "repeatTooManyBean")
@RequestScoped
public class RepeatTooManyBean {

    private List<String> _list;
    private StringBuilder iterations = new StringBuilder();

    public RepeatTooManyBean() {
        _list = new ArrayList();
        for (int i = 1; i <= 10; i++) {
            _list.add(String.valueOf(i));
        }
    }

    public String getIterations() {
        return iterations.toString();
    }

    public List<String> getList() {
        return _list;
    }

    public void visitChildren() {
        final FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().visitTree(new FullVisitContext(context), new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext visitContext, UIComponent target) {
                if (target instanceof ValueHolder
                        && target.getId().equals("out")) {
                    ValueExpression expr = target.getValueExpression("value");
                    Object value = expr.getValue(context.getELContext());
                    iterations.append(value);
                    return VisitResult.REJECT;
                }
                return VisitResult.ACCEPT;
            }
        });
    }
}
