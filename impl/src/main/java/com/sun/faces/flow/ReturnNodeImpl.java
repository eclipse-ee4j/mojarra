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

import com.sun.faces.util.Util;

import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.ReturnNode;

public class ReturnNodeImpl extends ReturnNode implements Serializable {

    private static final long serialVersionUID = 7159675814039078231L;

    private final String id;
    private ValueExpression fromOutcome;

    public ReturnNodeImpl(String id) {
        this.id = id;
        fromOutcome = null;
    }

    @Override
    public String getFromOutcome(FacesContext context) {
        Util.notNull("context", context);
        String result = null;

        if (null != fromOutcome) {
            Object objResult = fromOutcome.getValue(context.getELContext());
            result = null != objResult ? objResult.toString() : null;
        }
        return result;
    }

    public void setFromOutcome(String fromOutcome) {
        if (null == fromOutcome) {
            this.fromOutcome = null;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        ExpressionFactory eFactory = context.getApplication().getExpressionFactory();
        this.fromOutcome = eFactory.createValueExpression(context.getELContext(), fromOutcome, Object.class);

    }

    public void setFromOutcome(ValueExpression fromOutcome) {
        this.fromOutcome = fromOutcome;
    }

    @Override
    public String getId() {
        return id;
    }

}
