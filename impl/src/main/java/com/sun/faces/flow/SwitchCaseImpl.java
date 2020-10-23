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

package com.sun.faces.flow;

import java.io.Serializable;

import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.SwitchCase;

public class SwitchCaseImpl extends SwitchCase implements Serializable {

    private static final long serialVersionUID = -8982500105361921446L;

    // This is the id of the <return> or <switch>
    private String enclosingId;
    private String fromOutcome;
    private String condition;
    private ValueExpression conditionExpr;

    public ValueExpression getConditionExpression() {
        return conditionExpr;
    }

    @Override
    public Boolean getCondition(FacesContext context) {
        if (conditionExpr == null && condition != null) {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            conditionExpr = factory.createValueExpression(context.getELContext(), condition, Boolean.class);
        }

        return conditionExpr != null ? (Boolean) conditionExpr.getValue(context.getELContext()) : Boolean.FALSE;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setConditionExpression(ValueExpression conditionExpression) {
        conditionExpr = conditionExpression;
    }

    @Override
    public String getFromOutcome() {
        return fromOutcome;
    }

    public void setFromOutcome(String fromOutcome) {
        this.fromOutcome = fromOutcome;
    }

    public String getEnclosingId() {
        return enclosingId;
    }

    public void setEnclosingId(String returnId) {
        enclosingId = returnId;
    }

}
