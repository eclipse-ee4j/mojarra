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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.faces.util.Util;

import jakarta.el.MethodExpression;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.FlowNode;
import jakarta.faces.flow.MethodCallNode;
import jakarta.faces.flow.Parameter;
import jakarta.faces.flow.ReturnNode;
import jakarta.faces.flow.SwitchNode;
import jakarta.faces.flow.ViewNode;
import jakarta.faces.lifecycle.ClientWindow;

public class FlowImpl extends Flow implements Serializable {

    private static final long serialVersionUID = 5287030395068302998L;

    public static final Flow SYNTHESIZED_RETURN_CASE_FLOW = new FlowImpl(FlowHandler.NULL_FLOW);

    public static final Flow ABANDONED_FLOW = new FlowImpl(FlowHandlerImpl.ABANDONED_FLOW);

    // <editor-fold defaultstate="collapsed" desc="Instance variables">

    private String id;
    private String definingDocumentId;
    private String startNodeId;
    private final ConcurrentHashMap<String, Set<NavigationCase>> _navigationCases;
    private final Map<String, Set<NavigationCase>> navigationCases;
    private final CopyOnWriteArrayList<ViewNode> _views;
    private final List<ViewNode> views;
    private final CopyOnWriteArrayList<MethodCallNode> _methodCalls;
    private final List<MethodCallNode> methodCalls;
    private final ConcurrentHashMap<String, Parameter> _inboundParameters;
    private final Map<String, Parameter> inboundParameters;
    private final ConcurrentHashMap<String, ReturnNode> _returns;
    private final Map<String, ReturnNode> returns;
    private final ConcurrentHashMap<String, SwitchNode> _switches;
    private final Map<String, SwitchNode> switches;
    private final ConcurrentHashMap<String, FlowCallNode> _facesFlowCalls;
    private final Map<String, FlowCallNode> facesFlowCalls;
    private final ConcurrentHashMap<String, FlowCallNode> _facesFlowCallsByTargetFlowId;
    private MethodExpression initializer;
    private MethodExpression finalizer;
    private boolean hasBeenInitialized = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public FlowImpl() {
        _inboundParameters = new ConcurrentHashMap<>();
        inboundParameters = Collections.unmodifiableMap(_inboundParameters);
        _returns = new ConcurrentHashMap<>();
        returns = Collections.unmodifiableMap(_returns);
        _switches = new ConcurrentHashMap<>();
        switches = Collections.unmodifiableMap(_switches);
        _facesFlowCalls = new ConcurrentHashMap<>();
        facesFlowCalls = Collections.unmodifiableMap(_facesFlowCalls);
        _facesFlowCallsByTargetFlowId = new ConcurrentHashMap<>();
        _views = new CopyOnWriteArrayList<>();
        views = Collections.unmodifiableList(_views);
        _navigationCases = new ConcurrentHashMap<>();
        navigationCases = Collections.unmodifiableMap(_navigationCases);
        _methodCalls = new CopyOnWriteArrayList<>();
        methodCalls = Collections.unmodifiableList(_methodCalls);
    }

    private FlowImpl(String id) {
        this.id = id;
        definingDocumentId = null;
        startNodeId = null;
        _navigationCases = null;
        navigationCases = null;
        _views = null;
        views = null;
        _methodCalls = null;
        methodCalls = null;
        _inboundParameters = null;
        inboundParameters = null;
        _returns = null;
        returns = null;
        _switches = null;
        switches = null;
        _facesFlowCalls = null;
        facesFlowCalls = null;
        _facesFlowCallsByTargetFlowId = null;
        initializer = null;
        finalizer = null;
        hasBeenInitialized = true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Object helpers">

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Flow other = (Flow) obj;
        if (id == null ? other.getId() != null : !id.equals(other.getId())) {
            return false;
        }
        if (startNodeId == null ? other.getStartNodeId() != null : !startNodeId.equals(other.getStartNodeId())) {
            return false;
        }
        if (_views != other.getViews() && (_views == null || !_views.equals(other.getViews()))) {
            return false;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        if (null != context) {
            if (_returns != other.getReturns() && (_returns == null || !_returns.equals(other.getReturns()))) {
                return false;
            }
            if (initializer != other.getInitializer() && (initializer == null || !initializer.equals(other.getInitializer()))) {
                return false;
            }
            if (finalizer != other.getFinalizer() && (finalizer == null || !finalizer.equals(other.getFinalizer()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (id != null ? id.hashCode() : 0);
        hash = 59 * hash + (startNodeId != null ? startNodeId.hashCode() : 0);
        hash = 59 * hash + (_views != null ? _views.hashCode() : 0);
        hash = 59 * hash + (_returns != null ? _returns.hashCode() : 0);
        hash = 59 * hash + (initializer != null ? initializer.hashCode() : 0);
        hash = 59 * hash + (finalizer != null ? finalizer.hashCode() : 0);
        return hash;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Simple properties">

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDefiningDocumentId() {
        return definingDocumentId;
    }

    public void setId(String definingDocumentId, String id) {
        Util.notNull("definingDocumentId", definingDocumentId);
        Util.notNull("flowId", id);
        this.id = id;
        this.definingDocumentId = definingDocumentId;
    }

    @Override
    public String getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(String defaultNodeId) {
        startNodeId = defaultNodeId;
    }

    @Override
    public MethodExpression getFinalizer() {
        return finalizer;
    }

    public void setFinalizer(MethodExpression finalizer) {
        this.finalizer = finalizer;
    }

    @Override
    public MethodExpression getInitializer() {
        return initializer;
    }

    public void setInitializer(MethodExpression initializer) {
        this.initializer = initializer;
    }

    @Override
    public Map<String, Parameter> getInboundParameters() {
        return inboundParameters;
    }

    public Map<String, Parameter> _getInboundParameters() {
        return _inboundParameters;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Graph properties">

    @Override
    public List<ViewNode> getViews() {
        return views;
    }

    public List<ViewNode> _getViews() {
        return _views;
    }

    @Override
    public Map<String, ReturnNode> getReturns() {
        return returns;
    }

    public Map<String, ReturnNode> _getReturns() {
        return _returns;
    }

    @Override
    public Map<String, SwitchNode> getSwitches() {
        return switches;
    }

    public Map<String, SwitchNode> _getSwitches() {
        return _switches;
    }

    @Override
    public Map<String, FlowCallNode> getFlowCalls() {
        return facesFlowCalls;
    }

    public Map<String, FlowCallNode> _getFlowCalls() {
        return _facesFlowCalls;
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        return navigationCases;
    }

    public Map<String, Set<NavigationCase>> _getNavigationCases() {
        return _navigationCases;
    }

    @Override
    public FlowCallNode getFlowCall(Flow targetFlow) {
        String targetFlowId = targetFlow.getId();
        if (!hasBeenInitialized) {
            FacesContext context = FacesContext.getCurrentInstance();
            init(context);
        }
        FlowCallNode result = _facesFlowCallsByTargetFlowId.get(targetFlowId);

        return result;
    }

    @Override
    public List<MethodCallNode> getMethodCalls() {
        return methodCalls;
    }

    public List<MethodCallNode> _getMethodCalls() {
        return _methodCalls;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Graph navigation">

    @Override
    public FlowNode getNode(String nodeId) {
        List<ViewNode> myViews = getViews();
        FlowNode result = null;

        if (null != myViews) {
            for (ViewNode cur : myViews) {
                if (nodeId.equals(cur.getId())) {
                    result = cur;
                    break;
                }
            }
        }
        if (null == result) {
            Map<String, SwitchNode> mySwitches = getSwitches();
            result = mySwitches.get(nodeId);
        }
        if (null == result) {
            List<MethodCallNode> myMethods = getMethodCalls();
            for (MethodCallNode cur : myMethods) {
                if (nodeId.equals(cur.getId())) {
                    result = cur;
                    break;
                }
            }
        }
        if (null == result) {
            Map<String, FlowCallNode> myCalls = getFlowCalls();
            result = myCalls.get(nodeId);
        }

        if (null == result) {
            Map<String, ReturnNode> myReturns = getReturns();
            result = myReturns.get(nodeId);
        }

        return result;

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Outside interaction">

    @Override
    public String getClientWindowFlowId(ClientWindow curWindow) {
        String result = null;

        result = curWindow.getId() + "_" + getId();

        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helpers">

    public void init(FacesContext context) {
        if (hasBeenInitialized) {
            return;
        }
        hasBeenInitialized = true;

        // Populate lookup data structures.
        FlowCallNode curNode = null;
        String curTargetFlowId = null;
        for (Map.Entry<String, FlowCallNode> cur : _facesFlowCalls.entrySet()) {
            curNode = cur.getValue();
            curTargetFlowId = curNode.getCalledFlowId(context);
            _facesFlowCallsByTargetFlowId.put(curTargetFlowId, curNode);
        }
    }

    // </editor-fold>

}
