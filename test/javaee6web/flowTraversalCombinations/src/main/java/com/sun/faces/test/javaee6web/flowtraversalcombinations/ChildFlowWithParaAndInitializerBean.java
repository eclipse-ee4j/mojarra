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

package com.sun.faces.test.javaee6web.flowtraversalcombinations;

import java.io.Serializable;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Named;

@Named
@FlowScoped(value = "child-flow-with-parameters-and-initializer")
public class ChildFlowWithParaAndInitializerBean implements Serializable {
    private String strTodisplayOfPara;
    
    public String getStrTodisplayOfPara() {
        return strTodisplayOfPara;
    }
    
    public void initializerCalled() {
        this.strTodisplayOfPara = evalAsString("#{flowScope.Para1}");
    }
    
    public static String evalAsString(String p_expression)
    {
        FacesContext context = FacesContext.getCurrentInstance();
        ExpressionFactory expressionFactory = context.getApplication().getExpressionFactory();
        ELContext elContext = context.getELContext();
        ValueExpression vex = expressionFactory.createValueExpression(elContext, p_expression, String.class);
        
        String result = (String) vex.getValue(elContext);
        
        return result;
    }
}
