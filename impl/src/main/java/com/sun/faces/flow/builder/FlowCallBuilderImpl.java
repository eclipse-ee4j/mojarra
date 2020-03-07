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

import java.util.Map;

import com.sun.faces.flow.FlowCallNodeImpl;
import com.sun.faces.flow.ParameterImpl;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.builder.FlowCallBuilder;

public class FlowCallBuilderImpl extends FlowCallBuilder {

    private FlowBuilderImpl root;
    private String flowCallNodeId;
    private String flowDocumentId;
    private String flowId;

    public FlowCallBuilderImpl(FlowBuilderImpl root, String id) {
        this.root = root;
        flowCallNodeId = id;
    }

    @Override
    public FlowCallBuilder flowReference(String flowDocumentId, String flowId) {
        Util.notNull("flowDocumentId", flowDocumentId);
        Util.notNull("flowId", flowId);
        this.flowDocumentId = flowDocumentId;
        this.flowId = flowId;
        getFlowCall();
        return this;
    }

    private FlowCallNodeImpl getFlowCall() {
        Util.notNull("flowCallNodeId", flowCallNodeId);
        Util.notNull("flowwDocumentId", flowDocumentId);
        Util.notNull("flowId", flowId);

        Map<String, FlowCallNode> flowCalls = root._getFlow()._getFlowCalls();
        FlowCallNodeImpl flowCall = (FlowCallNodeImpl) flowCalls.get(flowCallNodeId);
        if (null == flowCall) {
            flowCall = new FlowCallNodeImpl(flowCallNodeId, flowDocumentId, flowId, null);
            flowCalls.put(flowCallNodeId, flowCall);
        }
        return flowCall;
    }

    @Override
    public FlowCallBuilder outboundParameter(String name, ValueExpression value) {
        Util.notNull("name", name);
        Util.notNull("value", value);
        ParameterImpl param = new ParameterImpl(name, value);
        FlowCallNodeImpl flowCall = getFlowCall();
        flowCall._getOutboundParameters().put(name, param);
        return this;
    }

    @Override
    public FlowCallBuilder outboundParameter(String name, String value) {
        Util.notNull("name", name);
        Util.notNull("value", value);
        ValueExpression ve = root.getExpressionFactory().createValueExpression(root.getELContext(), value, Object.class);
        outboundParameter(name, ve);
        return this;
    }

    @Override
    public FlowCallBuilder markAsStartNode() {
        root._getFlow().setStartNodeId(flowCallNodeId);
        return this;
    }

}
