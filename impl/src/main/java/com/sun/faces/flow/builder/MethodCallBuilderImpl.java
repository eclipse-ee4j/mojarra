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

package com.sun.faces.flow.builder;

import java.util.List;

import com.sun.faces.flow.MethodCallNodeImpl;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.flow.Parameter;
import jakarta.faces.flow.builder.MethodCallBuilder;

public class MethodCallBuilderImpl extends MethodCallBuilder {

    private FlowBuilderImpl root;
    private String methodCallId;
    private MethodCallNodeImpl methodCallNode;
    private static final Class<?>[] EMPTY_ARGS = new Class[0];

    public MethodCallBuilderImpl(FlowBuilderImpl root, String id) {
        this.root = root;
        methodCallId = id;
        methodCallNode = new MethodCallNodeImpl(id);
        this.root._getFlow()._getMethodCalls().add(methodCallNode);

    }

    @Override
    public MethodCallBuilder defaultOutcome(String outcome) {
        ELContext elc = root.getELContext();
        ValueExpression ve = root.getExpressionFactory().createValueExpression(elc, outcome, String.class);
        methodCallNode.setOutcome(ve);
        return this;
    }

    @Override
    public MethodCallBuilder defaultOutcome(ValueExpression ve) {
        methodCallNode.setOutcome(ve);
        return this;
    }

    @Override
    public MethodCallBuilder expression(String methodExpression) {
        ELContext elc = root.getELContext();
        MethodExpression me = root.getExpressionFactory().createMethodExpression(elc, methodExpression, null, EMPTY_ARGS);
        methodCallNode.setMethodExpression(me);
        return this;
    }

    @Override
    public MethodCallBuilder expression(String methodExpression, Class[] paramTypes) {
        ELContext elc = root.getELContext();
        MethodExpression me = root.getExpressionFactory().createMethodExpression(elc, methodExpression, null, paramTypes);
        methodCallNode.setMethodExpression(me);
        return this;
    }

    @Override
    public MethodCallBuilder parameters(List<Parameter> parameters) {
        methodCallNode._getParameters().addAll(parameters);
        return this;
    }

    @Override
    public MethodCallBuilder expression(MethodExpression me) {
        methodCallNode.setMethodExpression(me);
        return this;
    }

    @Override
    public MethodCallBuilder markAsStartNode() {
        root._getFlow().setStartNodeId(methodCallId);
        return this;
    }

}
