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
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.faces.application.NavigationHandlerImpl;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.Parameter;

public class FlowHandlerImpl extends FlowHandler {

    public static final String ABANDONED_FLOW = "jakarta.faces.flow.AbandonedFlow";

    public FlowHandlerImpl() {
        flowFeatureIsEnabled = false;
        flows = new ConcurrentHashMap<>();
        flowsByFlowId = new ConcurrentHashMap<>();
    }

    private boolean flowFeatureIsEnabled;

    // key: definingDocumentId, value: Map<flowId, Flow>
    private final Map<String, Map<String, Flow>> flows;

    // key: flowId, List<Flow>
    private final Map<String, List<Flow>> flowsByFlowId;

    @Override
    public Map<Object, Object> getCurrentFlowScope() {
        return FlowCDIContext.getCurrentFlowScopeAndUpdateSession();
    }

    @Override
    public Flow getFlow(FacesContext context, String definingDocumentId, String id) {
        Util.notNull("context", context);
        Util.notNull("definingDocumentId", definingDocumentId);
        Util.notNull("id", id);
        Flow result = null;
        Map<String, Flow> mapsForDefiningDocument = flows.get(definingDocumentId);

        if (null != mapsForDefiningDocument) {
            result = mapsForDefiningDocument.get(id);
        }

        return result;
    }

    @Override
    public void addFlow(FacesContext context, Flow toAdd) {
        Util.notNull("context", context);
        Util.notNull("toAdd", toAdd);

        String id = toAdd.getId();
        if (null == id || 0 == id.length()) {
            throw new IllegalArgumentException("The id of the flow may not be null or zero-length.");
        }
        String definingDocumentId = toAdd.getDefiningDocumentId();
        if (null == definingDocumentId) {
            throw new IllegalArgumentException("The definingDocumentId of the flow may not be null.");
        }
        Map<String, Flow> mapsForDefiningDocument = flows.get(definingDocumentId);
        if (null == mapsForDefiningDocument) {
            mapsForDefiningDocument = new ConcurrentHashMap<>();
            flows.put(toAdd.getDefiningDocumentId(), mapsForDefiningDocument);
        }

        Flow oldFlow = mapsForDefiningDocument.put(id, toAdd);
        if (null != oldFlow) {
            String message = MessageFormat.format("Flow with id \"{0}\" and definingDocumentId \"{1}\" already exists.", id, definingDocumentId);
            throw new IllegalStateException(message);
        }

        // Make it possible for the "transition" method to map from view nodes
        // to flow instances.
        List<Flow> flowsWithId = flowsByFlowId.get(id);
        if (null == flowsWithId) {
            flowsWithId = new CopyOnWriteArrayList<>();
            flowsByFlowId.put(id, flowsWithId);
        }
        flowsWithId.add(toAdd);

        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        if (navigationHandler instanceof ConfigurableNavigationHandler) {
            ((ConfigurableNavigationHandler) navigationHandler).inspectFlow(context, toAdd);
        }
        flowFeatureIsEnabled = true;
    }

    @Override
    public boolean isActive(FacesContext context, String definingDocumentId, String id) {
        Util.notNull("context", context);
        Util.notNull("definingDocumentId", definingDocumentId);
        Util.notNull("id", id);
        boolean result = false;
        FlowDeque<Flow> flowStack = getFlowStack(context);
        for (Flow cur : flowStack) {
            if (id.equals(cur.getId()) && definingDocumentId.equals(cur.getDefiningDocumentId())) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public Flow getCurrentFlow(FacesContext context) {
        Util.notNull("context", context);

        if (!flowFeatureIsEnabled) {
            return null;
        }
        Flow result = null;
        // If there is no session, there cannot possibly be a flow, so
        // don't create one just to check.
        if (null == context.getExternalContext().getSession(false)) {
            return null;
        }

        FlowDeque<Flow> flowStack = getFlowStack(context);
        int returnDepth = flowStack.getReturnDepth();
        if (flowStack.size() <= returnDepth) {
            return null;
        }
        if (0 < returnDepth) {
            Iterator<Flow> stackIter = flowStack.iterator();
            int i = 0;
            stackIter.next();
            if (stackIter.hasNext()) {
                do {
                    result = stackIter.next();
                    i++;
                } while (i < returnDepth);
            }
        } else {
            result = getFlowStack(context).peekFirst();
        }
        return result;
    }

    @Override
    public String getLastDisplayedViewId(FacesContext context) {
        Util.notNull("context", context);
        String result = null;
        FlowDeque<Flow> flowStack = getFlowStack(context);
        result = flowStack.peekLastDisplayedViewId();
        return result;
    }

    public static final String FLOW_RETURN_DEPTH_PARAM_NAME = "jffrd";

    public int getAndClearReturnModeDepth(FacesContext context) {
        int result = 0;
        FlowDeque<Flow> flowStack = getFlowStack(context);
        result = flowStack.getAndClearMaxReturnDepth(context);

        return result;
    }

    @Override
    public void pushReturnMode(FacesContext context) {
        Util.notNull("context", context);
        FlowDeque<Flow> flowStack = getFlowStack(context);
        flowStack.pushReturnMode();
    }

    @Override
    public void popReturnMode(FacesContext context) {
        Util.notNull("context", context);
        FlowDeque<Flow> flowStack = getFlowStack(context);
        flowStack.popReturnMode();
    }
    // We need a method that takes a view id of a view that is in a flow
    // and makes the system "enter" the flow.

    @Override
    public void transition(FacesContext context, Flow sourceFlow, Flow targetFlow, FlowCallNode outboundCallNode, String toViewId) {
        Util.notNull("context", context);
        Util.notNull("toViewId", toViewId);
        if (!flowFeatureIsEnabled) {
            return;
        }

        // there has to be a better way to structure this logic
        if (!flowsEqual(sourceFlow, targetFlow)) {
            // Do we have an outboundCallNode?
            Map<String, Object> evaluatedParams = null;
            if (null != outboundCallNode) {
                Map<String, Parameter> outboundParameters = outboundCallNode.getOutboundParameters();
                Map<String, Parameter> inboundParameters = targetFlow.getInboundParameters();
                // Are we passing parameters?
                if (null != outboundParameters && !outboundParameters.isEmpty() && null != inboundParameters && !inboundParameters.isEmpty()) {

                    ELContext elContext = context.getELContext();
                    String curName;
                    // for each outbound parameter...
                    for (Map.Entry<String, Parameter> curOutbound : outboundParameters.entrySet()) {
                        curName = curOutbound.getKey();
                        if (inboundParameters.containsKey(curName)) {
                            if (null == evaluatedParams) {
                                evaluatedParams = new HashMap<>();
                            }
                            // Evaluate it and put it in the temporary map.
                            // It is necessary to do this before the flow
                            // transition because EL expressions may refer to
                            // things in the current flow scope.
                            evaluatedParams.put(curName, curOutbound.getValue().getValue().getValue(elContext));
                        }
                    }
                }
            }

            performPops(context, sourceFlow, targetFlow);
            if (null != targetFlow && !targetFlow.equals(FlowImpl.ABANDONED_FLOW)) {
                pushFlow(context, targetFlow, toViewId, evaluatedParams);
            } else {
                assignInboundParameters(context, targetFlow, evaluatedParams);
            }
        }
    }

    private void assignInboundParameters(FacesContext context, Flow calledFlow, Map<String, Object> evaluatedParams) {
        // Now the new flow is active, it's time to evaluate the inbound
        // parameters.
        if (null != evaluatedParams) {
            Map<String, Parameter> inboundParameters = calledFlow.getInboundParameters();
            ELContext elContext = context.getELContext();
            String curName;
            ValueExpression toSet;
            for (Map.Entry<String, Object> curOutbound : evaluatedParams.entrySet()) {
                curName = curOutbound.getKey();
                assert inboundParameters.containsKey(curName);
                toSet = inboundParameters.get(curName).getValue();
                toSet.setValue(elContext, curOutbound.getValue());
            }
        }
    }

    @Override
    public void clientWindowTransition(FacesContext context) {
        Map<String, String> requestParamMap = context.getExternalContext().getRequestParameterMap();
        String toFlowDocumentId = requestParamMap.get(TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME);
        String flowId = requestParamMap.get(FLOW_ID_REQUEST_PARAM_NAME);
        if (null != toFlowDocumentId) {
            // don't use *this*, due to decoration
            FlowHandler fh = context.getApplication().getFlowHandler();
            Flow sourceFlow = fh.getCurrentFlow(context);
            Flow targetFlow = null;
            FlowCallNode flowCallNode = null;
            // if this is not a return...
            if (null != flowId && !FlowHandler.NULL_FLOW.equals(toFlowDocumentId)) {
                targetFlow = fh.getFlow(context, toFlowDocumentId, flowId);
                if (null != targetFlow && null != sourceFlow) {
                    flowCallNode = sourceFlow.getFlowCall(targetFlow);
                }
            } else {
                String maxReturnDepthStr = requestParamMap.get(FLOW_RETURN_DEPTH_PARAM_NAME);
                int maxReturnDepth = Integer.parseInt(maxReturnDepthStr);
                FlowDeque<Flow> flowStack = getFlowStack(context);
                flowStack.setMaxReturnDepth(context, maxReturnDepth);
            }

            fh.transition(context, sourceFlow, targetFlow, flowCallNode, context.getViewRoot().getViewId());

        }
    }

    private void performPops(FacesContext context, Flow sourceFlow, Flow targetFlow) {
        // case 0: sourceFlow is null. There must be nothing to pop.
        if (null == sourceFlow) {
            assert null == peekFlow(context);
            return;
        }

        // case 1: target is null
        if (null == targetFlow) {
            FlowDeque<Flow> flowStack = getFlowStack(context);
            int maxReturns = flowStack.getAndClearMaxReturnDepth(context);
            for (int i = 0; i < maxReturns; i++) {
                popFlow(context);
            }
            return;
        }

        if (FlowImpl.ABANDONED_FLOW.equals(targetFlow)) {
            FlowDeque<Flow> flowStack = getFlowStack(context);
            int depth = flowStack.size();
            for (int i = 0; i < depth; i++) {
                popFlow(context);
            }
            return;
        }

        // case 3: neither source nor target are null. If source does not
        // have a call that calls target, we must pop source.
        if (null == sourceFlow.getFlowCall(targetFlow)) {
            popFlow(context);
        }

    }

    /*
     * The Flow.equals() method alone is insufficient because we need to account for the case where one or the other or both
     * operands may be null.
     *
     */
    private boolean flowsEqual(Flow flow1, Flow flow2) {
        boolean result = false;
        if (flow1 == flow2) {
            result = true;
        } else if (null == flow1 || null == flow2) {
            result = false;
        } else {
            result = flow1.equals(flow2);
        }
        return result;
    }

    // <editor-fold defaultstate="collapsed" desc="Helper Methods">

    private void pushFlow(FacesContext context, Flow toPush, String lastDisplayedViewId, Map<String, Object> evaluatedParams) {
        FlowDeque<Flow> flowStack = getFlowStack(context);
        flowStack.addFirst(toPush, lastDisplayedViewId);
        FlowCDIContext.flowEntered();

        assignInboundParameters(context, toPush, evaluatedParams);

        MethodExpression me = toPush.getInitializer();
        if (null != me) {
            me.invoke(context.getELContext(), null);
        }
        forceSessionUpdateForFlowStack(context, flowStack);
    }

    private Flow peekFlow(FacesContext context) {
        FlowDeque<Flow> flowStack = getFlowStack(context);
        return flowStack.peekFirst();
    }

    private Flow popFlow(FacesContext context) {
        FlowDeque<Flow> flowStack = getFlowStack(context);
        Flow currentFlow = peekFlow(context);
        if (null != currentFlow) {
            callFinalizer(context, currentFlow, flowStack.size());
        }
        Flow result = flowStack.pollFirst();
        forceSessionUpdateForFlowStack(context, flowStack);
        return result;

    }

    private void callFinalizer(FacesContext context, Flow currentFlow, int depth) {
        MethodExpression me = currentFlow.getFinalizer();
        if (null != me) {
            me.invoke(context.getELContext(), null);
        }
        FlowCDIContext.flowExited(currentFlow, depth);
    }

    static FlowDeque<Flow> getFlowStack(FacesContext context) {
        FlowDeque<Flow> result = null;
        ExternalContext extContext = context.getExternalContext();
        String sessionKey = extContext.getClientWindow().getId() + "_flowStack";
        Map<String, Object> sessionMap = extContext.getSessionMap();
        result = (FlowDeque<Flow>) sessionMap.get(sessionKey);
        if (null == result) {
            result = new FlowDeque<>(sessionKey);
            sessionMap.put(sessionKey, result);
        }

        return result;
    }

    private void forceSessionUpdateForFlowStack(FacesContext context, FlowDeque<Flow> stack) {
        ExternalContext extContext = context.getExternalContext();
        Map<String, Object> sessionMap = extContext.getSessionMap();
        sessionMap.put(stack.getSessionKey(), stack);
    }

    static class FlowDeque<E> implements Iterable<E>, Serializable {

        private static final long serialVersionUID = 7915803727932706270L;

        private int returnDepth;
        private ArrayDeque<E> data;

        private static class RideAlong implements Serializable {
            private static final long serialVersionUID = -1899365746835118058L;
            String lastDisplayedViewId;

            public RideAlong(String lastDisplayedViewId) {
                this.lastDisplayedViewId = lastDisplayedViewId;
            }

        }

        private ArrayDeque<RideAlong> rideAlong;
        private final String sessionKey;

        public FlowDeque(final String sessionKey) {
            data = new ArrayDeque<>();
            rideAlong = new ArrayDeque<>();
            this.sessionKey = sessionKey;
        }

        public String getSessionKey() {
            return sessionKey;
        }

        public int size() {
            return data.size();
        }

        @Override
        public Iterator<E> iterator() {
            return data.iterator();
        }

        public void addFirst(E e, String lastDisplayedViewId) {
            rideAlong.addFirst(new RideAlong(lastDisplayedViewId));
            data.addFirst(e);
        }

        public E pollFirst() {
            rideAlong.pollFirst();
            return data.pollFirst();
        }

        public int getCurrentFlowDepth() {
            int returnDepth = this.returnDepth;
            if (data.size() <= returnDepth) {
                return 0;
            }

            int result = data.size();
            if (0 < returnDepth) {
                Iterator<E> stackIter = data.iterator();
                int i = 0;
                while (stackIter.hasNext() && i < returnDepth) {
                    stackIter.next();
                    result--;
                    i++;
                }
            }

            return result;
        }

        public E peekFirst() {
            return data.peekFirst();
        }

        public String peekLastDisplayedViewId() {
            String result = null;
            RideAlong helper = null;
            int myReturnDepth = this.getReturnDepth();
            if (0 < myReturnDepth) {
                Iterator<RideAlong> stackIter = rideAlong.iterator();
                stackIter.next();
                int i = 0;
                if (stackIter.hasNext()) {
                    do {
                        helper = stackIter.next();
                        i++;
                    } while (i < myReturnDepth);
                }
            } else {
                helper = rideAlong.peekFirst();
            }

            if (null != helper) {
                result = helper.lastDisplayedViewId;
            }

            return result;
        }

        public int getReturnDepth() {
            return returnDepth;
        }

        private void setMaxReturnDepth(FacesContext context, int value) {
            Map<Object, Object> attrs = context.getAttributes();
            attrs.put(FLOW_RETURN_DEPTH_PARAM_NAME, value);
        }

        private int getAndClearMaxReturnDepth(FacesContext context) {
            Map<Object, Object> attrs = context.getAttributes();
            int result = 0;
            if (attrs.containsKey(FLOW_RETURN_DEPTH_PARAM_NAME)) {
                result = ((Integer) attrs.remove(FLOW_RETURN_DEPTH_PARAM_NAME)).intValue();
            }
            return result;
        }

        private void incrementMaxReturnDepth() {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<Object, Object> attrs = context.getAttributes();
            if (!attrs.containsKey(FLOW_RETURN_DEPTH_PARAM_NAME)) {
                attrs.put(FLOW_RETURN_DEPTH_PARAM_NAME, 1);
            } else {
                Integer cur = (Integer) attrs.get(FLOW_RETURN_DEPTH_PARAM_NAME);
                attrs.put(FLOW_RETURN_DEPTH_PARAM_NAME, cur + 1);
            }

        }

        private void decrementMaxReturnDepth() {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<Object, Object> attrs = context.getAttributes();
            if (attrs.containsKey(FLOW_RETURN_DEPTH_PARAM_NAME)) {

                Integer cur = (Integer) attrs.get(FLOW_RETURN_DEPTH_PARAM_NAME);

                if (cur > 1) {
                    attrs.put(FLOW_RETURN_DEPTH_PARAM_NAME, cur - 1);
                } else {
                    attrs.remove(FLOW_RETURN_DEPTH_PARAM_NAME);
                }
            }

        }

        public void pushReturnMode() {
            this.incrementMaxReturnDepth();
            this.returnDepth++;
        }

        public void popReturnMode() {

            // Mojarra #4279 If ConfigureableNavigationHandler is attempting to obtain a NavigationCase outside of
            // OutcomeTargetRenderer, then this method must perform exactly the reverse of pushReturnMode() to ensure
            // that ConfigureableNavigationHandler's calls are idempotent. For more details, see
            // https://github.com/javaserverfaces/mojarra/issues/4279
            if (NavigationHandlerImpl.isResetFlowHandlerState(FacesContext.getCurrentInstance())) {
                this.decrementMaxReturnDepth();
            }

            this.returnDepth--;
        }

    }

    // </editor-fold>

}
