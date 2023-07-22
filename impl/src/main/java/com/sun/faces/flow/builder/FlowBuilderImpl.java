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

import com.sun.faces.flow.FlowImpl;
import com.sun.faces.flow.ParameterImpl;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.builder.FlowBuilder;
import jakarta.faces.flow.builder.FlowCallBuilder;
import jakarta.faces.flow.builder.MethodCallBuilder;
import jakarta.faces.flow.builder.NavigationCaseBuilder;
import jakarta.faces.flow.builder.ReturnBuilder;
import jakarta.faces.flow.builder.SwitchBuilder;
import jakarta.faces.flow.builder.ViewBuilder;

public class FlowBuilderImpl extends FlowBuilder {

    private FlowImpl flow;
    private ExpressionFactory expressionFactory;
    private ELContext elContext;
    private FacesContext context;
    private boolean didInit;
    private boolean hasId;

    public FlowBuilderImpl(FacesContext context) {
        flow = new FlowImpl();
        this.context = context;
        expressionFactory = context.getApplication().getExpressionFactory();
        elContext = context.getELContext();
        didInit = false;
        hasId = false;

    }

    // <editor-fold defaultstate="collapsed" desc="Create Flow Nodes">

    @Override
    public NavigationCaseBuilder navigationCase() {
        return new NavigationCaseBuilderImpl(this);
    }

    @Override
    public ViewBuilder viewNode(String viewNodeId, String vdlDocumentId) {
        Util.notNull("viewNodeId", viewNodeId);
        Util.notNull("vdlDocumentId", vdlDocumentId);
        ViewBuilder result = new ViewBuilderImpl(this, viewNodeId, vdlDocumentId);
        return result;
    }

    @Override
    public SwitchBuilder switchNode(String switchNodeId) {
        Util.notNull("switchNodeId", switchNodeId);
        return new SwitchBuilderImpl(this, switchNodeId);
    }

    @Override
    public ReturnBuilder returnNode(String returnNodeId) {
        Util.notNull("returnNodeId", returnNodeId);
        return new ReturnBuilderImpl(this, returnNodeId);
    }

    @Override
    public MethodCallBuilder methodCallNode(String methodCallNodeId) {
        Util.notNull("methodCallNodeId", methodCallNodeId);
        return new MethodCallBuilderImpl(this, methodCallNodeId);
    }

    @Override
    public FlowCallBuilder flowCallNode(String flowCallNodeId) {
        return new FlowCallBuilderImpl(this, flowCallNodeId);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Flow-wide Settings">

    @Override
    public FlowBuilder id(String definingDocumentId, String flowId) {
        Util.notNull("definingDocumentId", definingDocumentId);
        Util.notNull("flowId", flowId);
        flow.setId(definingDocumentId, flowId);
        hasId = true;
        return this;
    }

    @Override
    public FlowBuilder initializer(MethodExpression methodExpression) {
        Util.notNull("methodExpression", methodExpression);
        flow.setInitializer(methodExpression);
        return this;
    }

    @Override
    public FlowBuilder initializer(String methodExpression) {
        Util.notNull("methodExpression", methodExpression);
        MethodExpression me = expressionFactory.createMethodExpression(elContext, methodExpression, null, new Class<?>[] {});
        flow.setInitializer(me);
        return this;
    }

    @Override
    public FlowBuilder finalizer(MethodExpression methodExpression) {
        flow.setFinalizer(methodExpression);
        return this;
    }

    @Override
    public FlowBuilder finalizer(String methodExpression) {
        MethodExpression me = expressionFactory.createMethodExpression(elContext, methodExpression, null, new Class<?>[] {});
        flow.setFinalizer(me);
        return this;
    }

    @Override
    public FlowBuilder inboundParameter(String name, ValueExpression value) {
        ParameterImpl param = new ParameterImpl(name, value);
        flow._getInboundParameters().put(name, param);

        return this;
    }

    @Override
    public FlowBuilder inboundParameter(String name, String value) {
        ValueExpression ve = expressionFactory.createValueExpression(elContext, value, Object.class);
        inboundParameter(name, ve);
        return this;
    }

    // </editor-fold>

    @Override
    public Flow getFlow() {
        if (!hasId) {
            throw new IllegalStateException("Flow must have a defining document id and flow id.");
        }
        if (!didInit) {
            flow.init(context);
            String startNodeId = flow.getStartNodeId();
            if (null == startNodeId) {
                String flowId = flow.getId();
                viewNode(flowId, "/" + flowId + "/" + flowId + ".xhtml").markAsStartNode();
            }
            didInit = true;
        }
        return flow;
    }

    public FlowImpl _getFlow() {
        return flow;
    }

    // <editor-fold defaultstate="collapsed" desc="Package private helpers">

    ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    ELContext getELContext() {
        return elContext;
    }

    // </editor-fold>

}
