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

package com.sun.faces.application;

import static com.sun.faces.application.SharedUtils.evaluateExpressions;
import static com.sun.faces.flow.FlowHandlerImpl.FLOW_RETURN_DEPTH_PARAM_NAME;
import static com.sun.faces.util.Util.notNull;
import static jakarta.faces.component.UIViewAction.isProcessingBroadcast;
import static jakarta.faces.flow.FlowHandler.FLOW_ID_REQUEST_PARAM_NAME;
import static jakarta.faces.flow.FlowHandler.NULL_FLOW;
import static jakarta.faces.flow.FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME;
import static java.util.logging.Level.FINE;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.faces.RIConstants;
import com.sun.faces.config.InitFacesContext;
import com.sun.faces.flow.FlowHandlerImpl;
import com.sun.faces.flow.FlowImpl;
import com.sun.faces.flow.builder.MutableNavigationCase;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.FlowNode;
import jakarta.faces.flow.MethodCallNode;
import jakarta.faces.flow.Parameter;
import jakarta.faces.flow.ReturnNode;
import jakarta.faces.flow.SwitchCase;
import jakarta.faces.flow.SwitchNode;
import jakarta.faces.flow.ViewNode;

/**
 * <p>
 * <strong>NavigationHandlerImpl</strong> is the class that implements default navigation handling. Refer to section
 * 7.4.2 of the specification for more details. PENDING: Make independent of ApplicationAssociate.
 */

public class NavigationHandlerImpl extends ConfigurableNavigationHandler {

    // Private Constants
    private static final String RESET_FLOW_HANDLER_STATE_KEY = NavigationHandlerImpl.class.getName() + "_RESET_FLOW_HANDLER_STATE_KEY";

    public static boolean isResetFlowHandlerState(FacesContext facesContext) {

        Boolean obtainingNavigationCase = (Boolean) FacesContext.getCurrentInstance().getAttributes().get(RESET_FLOW_HANDLER_STATE_KEY);
        return obtainingNavigationCase != null && obtainingNavigationCase;
    }

    public static void setResetFlowHandlerStateIfUnset(FacesContext facesContext, boolean resetFlowHandlerState) {
        Map<Object, Object> attributes = facesContext.getAttributes();

        if (!attributes.containsKey(RESET_FLOW_HANDLER_STATE_KEY)) {
            attributes.put(RESET_FLOW_HANDLER_STATE_KEY, resetFlowHandlerState);
        }
    }

    public static void unsetResetFlowHandlerState(FacesContext facesContext) {
        facesContext.getAttributes().remove(RESET_FLOW_HANDLER_STATE_KEY);
    }

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * <code>Map</code> containing configured navigation cases.
     */
    private volatile Map<String, NavigationInfo> navigationMaps;

    /**
     * Flag indicated the current mode.
     */
    private boolean development;
    private static final Pattern REDIRECT_EQUALS_TRUE = Pattern.compile("(.*)(faces-redirect=true)(.*)");
    private static final Pattern INCLUDE_VIEW_PARAMS_EQUALS_TRUE = Pattern.compile("(.*)(includeViewParams=true)(.*)");

    // ------------------------------------------------------------ Constructors

    /**
     * This constructor uses the current <code>ApplicationAssociate</code> instance to obtain the navigation mappings used
     * to make navigational decisions.
     */
    public NavigationHandlerImpl() {

        super();

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "Created NavigationHandler instance ");
        }

        ApplicationAssociate associate = ApplicationAssociate.getInstance(FacesContext.getCurrentInstance().getExternalContext());
        if (associate != null) {
            development = associate.isDevModeEnabled();
        }

    }

    // ------------------------------ Methods from ConfigurableNavigationHandler

    /**
     * @see jakarta.faces.application.ConfigurableNavigationHandler#getNavigationCase(jakarta.faces.context.FacesContext,
     * String, String)
     */
    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
        return getNavigationCase(context, fromAction, outcome, "");
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        Util.notNull("context", context);
        Util.notNull("toFlowDocumentId", toFlowDocumentId);

        setResetFlowHandlerStateIfUnset(context, true);
        try {
            CaseStruct caseStruct = getViewId(context, fromAction, outcome, toFlowDocumentId);
            if (null != caseStruct) {
                return caseStruct.navCase;
            }

            return null;
        } finally {
            unsetResetFlowHandlerState(context);
        }
    }

    /**
     * @see jakarta.faces.application.ConfigurableNavigationHandler#getNavigationCases()
     */
    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        FacesContext context = FacesContext.getCurrentInstance();
        setResetFlowHandlerStateIfUnset(context, true);
        try {
            Map<String, Set<NavigationCase>> result = getNavigationMap(context);

            return result;
        } finally {
            unsetResetFlowHandlerState(context);
        }
    }

    @Override
    public void inspectFlow(FacesContext context, Flow flow) {
        initializeNavigationFromFlow(context, flow);
    }

    // ------------------------------------------ Methods from NavigationHandler

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome) {
        this.handleNavigation(context, fromAction, outcome, "");
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
        } else if (flow1 == null || flow2 == null) {
            result = false;
        } else {
            result = flow1.equals(flow2);
        }

        return result;
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {

        notNull("context", context);

        CaseStruct caseStruct = getViewId(context, fromAction, outcome, toFlowDocumentId);
        if (caseStruct != null) {
            ExternalContext extContext = context.getExternalContext();
            ViewHandler viewHandler = Util.getViewHandler(context);
            assert null != viewHandler;
            Flash flash = extContext.getFlash();
            boolean isUIViewActionBroadcastAndViewdsDiffer = false;

            if (isProcessingBroadcast(context)) {
                flash.setKeepMessages(true);
                String viewIdBefore = context.getViewRoot().getViewId();
                viewIdBefore = null == viewIdBefore ? "" : viewIdBefore;
                String viewIdAfter = caseStruct.navCase.getToViewId(context);
                viewIdAfter = null == viewIdAfter ? "" : viewIdAfter; // NOPMD
                isUIViewActionBroadcastAndViewdsDiffer = !viewIdBefore.equals(viewIdAfter);
            }

            if (caseStruct.navCase.isRedirect() || isUIViewActionBroadcastAndViewdsDiffer) {
                Map<String, List<String>> parameters = caseStruct.navCase.getParameters();

                // perform a 302 redirect.
                // If this a redirect that starts causes a flow transition, ensure
                // the necessary metadata is appended to the query string

                // If at least one of newFlow and currentFlow is not null
                if (caseStruct.newFlow != null || caseStruct.currentFlow != null) { // NOPMD
                    if (!flowsEqual(caseStruct.newFlow, caseStruct.currentFlow)) { // NOPMD
                        if (parameters == null) { // NOPMD
                            parameters = new HashMap<>();
                        }

                        // If we are exiting all flows
                        if (caseStruct.newFlow == null) { // NOPMD
                            parameters.put(TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, List.of(NULL_FLOW));
                            parameters.put(FLOW_ID_REQUEST_PARAM_NAME, List.of(""));
                            FlowHandler flowHandler = context.getApplication().getFlowHandler();

                            if (flowHandler instanceof FlowHandlerImpl) {
                                FlowHandlerImpl flowHandlerImpl = (FlowHandlerImpl) flowHandler;
                                List<String> flowReturnDepthValues = new ArrayList<>();
                                flowReturnDepthValues.add(Integer.toString(flowHandlerImpl.getAndClearReturnModeDepth(context)));
                                parameters.put(FLOW_RETURN_DEPTH_PARAM_NAME, flowReturnDepthValues);
                            }
                        } else {
                            // If either of the two pieces of flow metadata are present in
                            // the caseStruct
                            if (!parameters.containsKey(TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME) || !parameters.containsKey(FLOW_ID_REQUEST_PARAM_NAME)) {

                                // Overwrite both of them.
                                List<String> toFlowDocumentIdParam = Collections.singletonList(caseStruct.navCase.getToFlowDocumentId());
                                parameters.put(TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, toFlowDocumentIdParam);

                                List<String> flowIdParam = Collections.singletonList(caseStruct.newFlow.getId());
                                parameters.put(FLOW_ID_REQUEST_PARAM_NAME, flowIdParam);
                            }
                        }
                    }
                }

                String redirectUrl = viewHandler.getRedirectURL(context, caseStruct.viewId, evaluateExpressions(context, parameters),
                        caseStruct.navCase.isIncludeViewParams());
                try {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, "Redirecting to path {0} for outcome {1}and viewId {2}", new Object[] { redirectUrl, outcome, caseStruct.viewId });
                    }

                    // Encode the redirect to ensure session state
                    // is maintained
                    updateRenderTargets(context, caseStruct.viewId);
                    flash.setRedirect(true);
                    extContext.redirect(redirectUrl);
                } catch (java.io.IOException ioe) {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, "faces.redirect_failed_error", redirectUrl);
                    }

                    throw new FacesException(ioe.getMessage(), ioe);
                }

                context.responseComplete();

                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "Response complete for {0}", caseStruct.viewId);
                }
            } else {
                UIViewRoot newRoot = viewHandler.createView(context, caseStruct.viewId);
                updateRenderTargets(context, caseStruct.viewId);
                context.setViewRoot(newRoot);
                FlowHandler flowHandler = context.getApplication().getFlowHandler();
                if (flowHandler != null && !isDidTransition(context)) {
                    flowHandler.transition(context, caseStruct.currentFlow, caseStruct.newFlow, caseStruct.facesFlowCallNode, caseStruct.viewId);
                    setDidTransition(context, false);
                }

                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "Set new view in FacesContext for {0}", caseStruct.viewId);
                }
            }

            clearViewMapIfNecessary(context, caseStruct.viewId);
        }
    }

    // --------------------------------------------------------- Private Methods
    private static final String ROOT_NAVIGATION_MAP_ID = NavigationHandlerImpl.class.getName() + ".NAVIGATION_MAP";

    private Map<String, Set<NavigationCase>> getRootNaviMap() {
        Map<String, Set<NavigationCase>> result = null;
        NavigationInfo info = null;
        if (null == navigationMaps) {
            createNavigationMaps();
            result = navigationMaps.get(ROOT_NAVIGATION_MAP_ID).ruleSet;
        } else {
            info = navigationMaps.get(ROOT_NAVIGATION_MAP_ID);
            if (null == info.ruleSet) {
                result = Collections.emptyMap();
            } else {
                result = info.ruleSet;
            }

        }
        return result;
    }

    private Map<String, Set<NavigationCase>> getNavigationMap(FacesContext context) {
        Map<String, Set<NavigationCase>> result = null;
        NavigationInfo info = null;
        if (null == navigationMaps) {
            createNavigationMaps();
            result = navigationMaps.get(ROOT_NAVIGATION_MAP_ID).ruleSet;
        } else {
            FlowHandler fh = context.getApplication().getFlowHandler();
            if (null != fh) {
                Flow currentFlow = fh.getCurrentFlow(context);
                if (null != currentFlow) {
                    info = navigationMaps.get(currentFlow.getDefiningDocumentId() + currentFlow.getId());
                    // We are in a flow, but there are no navigation rules for
                    // this flow.
                    if (null == info) {
                        return Collections.emptyMap();
                    }
                }
            }
            if (null == info) {
                info = navigationMaps.get(ROOT_NAVIGATION_MAP_ID);
            }
            if (null == info.ruleSet) {
                result = Collections.emptyMap();
            } else {
                result = info.ruleSet;
            }
        }

        return result;
    }

    private void createNavigationMaps() {
        if (null == navigationMaps) {
            Map<String, NavigationInfo> maps = new ConcurrentHashMap<>();
            NavigationMap result = new NavigationMap();
            NavigationInfo info = new NavigationInfo();
            info.ruleSet = result;
            maps.put(ROOT_NAVIGATION_MAP_ID, info);
            navigationMaps = maps;
        }
    }

    private Map<String, Set<NavigationCase>> getRootNavigationMap() {
        createNavigationMaps();
        return navigationMaps.get(ROOT_NAVIGATION_MAP_ID).ruleSet;
    }

    private Set<String> getWildCardMatchList(FacesContext context) {
        Set<String> result = Collections.emptySet();
        NavigationInfo info = null;
        FlowHandler fh = context.getApplication().getFlowHandler();
        if (null != fh) {
            Flow currentFlow = fh.getCurrentFlow(context);
            if (null != currentFlow) {
                info = navigationMaps.get(currentFlow.getDefiningDocumentId() + currentFlow.getId());
            }
        }
        if (null == info) {
            info = navigationMaps.get(ROOT_NAVIGATION_MAP_ID);
        }
        if (null != info.ruleSet && null != info.ruleSet.wildcardMatchList) {
            result = info.ruleSet.wildcardMatchList;
        }
        return result;
    }

    private NavigationInfo getNavigationInfo(FacesContext context, String toFlowDocumentId, String flowId) {
        NavigationInfo result = null;
        assert null != navigationMaps;
        result = navigationMaps.get(toFlowDocumentId + flowId);
        if (null == result) {
            FlowHandler fh = context.getApplication().getFlowHandler();
            if (null != fh) {
                Flow currentFlow = fh.getCurrentFlow(context);
                if (null != currentFlow) {
                    result = navigationMaps.get(currentFlow.getDefiningDocumentId() + currentFlow.getId());
                }
            }
        }

        return result;
    }

    private void initializeNavigationFromAssociate() {

        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        if (associate != null) {
            Map<String, Set<NavigationCase>> m = associate.getNavigationCaseListMappings();
            Map<String, Set<NavigationCase>> rootMap = getRootNavigationMap();
            if (m != null) {
                rootMap.putAll(m);
            }
        }

    }

    private void initializeNavigationFromFlow(FacesContext context, Flow toInspect) {

        if (context instanceof InitFacesContext) {
            createNavigationMaps();
            initializeNavigationFromFlowNonThreadSafe(toInspect);
        } else {
            // PENDING: When JAVASERVERFACES-2580 is done, the eager case will
            // no longer be necessary and can be removed.

            assert null != navigationMaps;
            initializeNavigationFromFlowThreadSafe(toInspect);
        }

    }

    private void initializeNavigationFromFlowNonThreadSafe(Flow toInspect) {
        String fullyQualifiedFlowId = toInspect.getDefiningDocumentId() + toInspect.getId();
        // Is there an existing NavigationMap for this flowId
        if (navigationMaps.containsKey(fullyQualifiedFlowId)) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "PENDING(edburns): merge existing map");
            }

        } else {
            Map<String, Set<NavigationCase>> navRules = toInspect.getNavigationCases();
            Map<String, SwitchNode> switches = toInspect.getSwitches();

            if (!navRules.isEmpty() || !switches.isEmpty()) {
                NavigationInfo info = new NavigationInfo();
                if (!switches.isEmpty()) {
                    info.switches = new ConcurrentHashMap<>();
                    for (Map.Entry<String, SwitchNode> cur : switches.entrySet()) {
                        info.switches.put(cur.getKey(), cur.getValue());
                    }
                }
                if (!navRules.isEmpty()) {
                    info.ruleSet = new NavigationMap();
                    info.ruleSet.putAll(navRules);
                }
                navigationMaps.put(fullyQualifiedFlowId, info);
            }
        }

    }

    private void initializeNavigationFromFlowThreadSafe(Flow toInspect) {
        synchronized (this) {
            initializeNavigationFromFlowNonThreadSafe(toInspect);
        }
    }

    /**
     * Calls <code>clear()</code> on the ViewMap (if available) if the view ID of the UIViewRoot differs from
     * <code>newId</code>
     */
    private void clearViewMapIfNecessary(FacesContext facesContext, String newId) {
        UIViewRoot root = facesContext.getViewRoot();

        if (root != null && !root.getViewId().equals(newId)) {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null) {
                viewMap.clear();
            }
        }
    }

    private void updateRenderTargets(FacesContext ctx, String newId) {

        if (ctx.getViewRoot() == null || !ctx.getViewRoot().getViewId().equals(newId)) {
            PartialViewContext pctx = ctx.getPartialViewContext();
            if (!pctx.isRenderAll()) {
                pctx.setRenderAll(true);
            }
        }

    }

    /**
     * This method uses helper methods to determine the new <code>view</code> identifier. Refer to section 7.4.2 of the
     * specification for more details.
     *
     * @param ctx the @{link FacesContext} for the current request
     * @param fromAction The action reference string
     * @param outcome The outcome string
     * @return The <code>view</code> identifier.
     */
    private CaseStruct getViewId(FacesContext ctx, String fromAction, String outcome, String toFlowDocumentId) {

        if (navigationMaps == null) {
            synchronized (this) {
                initializeNavigationFromAssociate();
            }
        }

        UIViewRoot root = ctx.getViewRoot();

        String viewId = root != null ? root.getViewId() : null;

        // if viewIdToTest is not null, use its value to find
        // a navigation match, otherwise look for a match
        // based soley on the fromAction and outcome
        CaseStruct caseStruct = null;
        Map<String, Set<NavigationCase>> navMap = getNavigationMap(ctx);

        if (viewId != null) {
            caseStruct = findExactMatch(ctx, viewId, fromAction, outcome, toFlowDocumentId, navMap);

            if (caseStruct == null) {
                caseStruct = findWildCardMatch(ctx, viewId, fromAction, outcome, toFlowDocumentId, navMap);
            }
        }

        if (caseStruct == null) {
            caseStruct = findDefaultMatch(ctx, fromAction, outcome, toFlowDocumentId, navMap);
        }

        FlowHandler fh = ctx.getApplication().getFlowHandler();
        // If the preceding steps found a match, but it was a flow call...
        if (null != caseStruct && caseStruct.isFlowEntryFromExplicitRule) {
            // Override the toFlowDocumentId with the value from the navigation-case, if present
            toFlowDocumentId = null != caseStruct.navCase.getToFlowDocumentId() ? caseStruct.navCase.getToFlowDocumentId() : toFlowDocumentId;
            // and try to call into the flow
            caseStruct = findFacesFlowCallMatch(ctx, fromAction, convertToViewIdToFlowOrNodeId(ctx, caseStruct), toFlowDocumentId);
        } else if (null != caseStruct && fh != null && fh.getCurrentFlow() != null) {
            String nodeId = convertToViewIdToFlowOrNodeId(ctx, caseStruct);
            FlowNode node = fh.getCurrentFlow().getNode(nodeId);
            if (node != null) {
                caseStruct = null;
                outcome = nodeId;
            }
        }

        // If we still don't have a match, see if this is a viewNode
        if (null == caseStruct && null != fromAction && null != outcome) {
            caseStruct = findViewNodeMatch(ctx, fromAction, outcome);
        }

        // If we still don't have a match, see if this is a switch
        if (null == caseStruct && null != fromAction && null != outcome) {
            caseStruct = findSwitchMatch(ctx, fromAction, outcome, toFlowDocumentId);
        }

        // If we still don't have a match, see if this is a method-call
        if (null == caseStruct && null != fromAction && null != outcome) {
            caseStruct = findMethodCallMatch(ctx, fromAction, outcome, toFlowDocumentId);
        }

        // If we still don't have a match, see if this is a faces-flow-call
        if (null == caseStruct && null != outcome) {
            caseStruct = findFacesFlowCallMatch(ctx, fromAction, outcome, toFlowDocumentId);
        }

        // If we still don't have a match, see if this is a flow-return
        if (null == caseStruct && null != outcome) {
            caseStruct = findReturnMatch(ctx, fromAction, outcome);
        }

        // If the navigation rules do not have a match...
        if (caseStruct == null && outcome != null && viewId != null) {
            // Treat empty string equivalent to null outcome. Faces 2.0 Rev a
            // Changelog issue C063.
            if (0 == outcome.length()) {
                outcome = null;
            } else {
                caseStruct = findImplicitMatch(ctx, viewId, fromAction, outcome, toFlowDocumentId);
            }
        }

        // If we still don't have a match...
        if (caseStruct == null && outcome != null && viewId != null) {

            FlowHandler flowHandler = ctx.getApplication().getFlowHandler();
            if (null != flowHandler) {

                Flow currentFlow = null;
                currentFlow = flowHandler.getCurrentFlow(ctx);
                if (null != currentFlow) {
                    caseStruct = findRootNavigationMapAbandonedFlowMatch(ctx, viewId, fromAction, outcome, toFlowDocumentId);
                }
            }

        }

        // no navigation case found
        if (caseStruct == null && outcome != null && development) {
            String key;
            Object[] params;
            if (fromAction == null) {
                key = MessageUtils.NAVIGATION_NO_MATCHING_OUTCOME_ID;
                params = new Object[] { viewId, outcome };
            } else {
                key = MessageUtils.NAVIGATION_NO_MATCHING_OUTCOME_ACTION_ID;
                params = new Object[] { viewId, fromAction, outcome };
            }
            FacesMessage m = MessageUtils.getExceptionMessage(key, params);
            m.setSeverity(FacesMessage.SEVERITY_WARN);
            ctx.addMessage(null, m);
        }
        return caseStruct;
    }

    /**
     * This method finds the List of cases for the current <code>view</code> identifier. After the cases are found, the
     * <code>from-action</code> and <code>from-outcome</code> values are evaluated to determine the new <code>view</code>
     * identifier. Refer to section 7.4.2 of the specification for more details.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param viewId The current <code>view</code> identifier.
     * @param fromAction The action reference string.
     * @param outcome The outcome string.
     * @return The <code>view</code> identifier.
     */
    private CaseStruct findExactMatch(FacesContext ctx, String viewId, String fromAction, String outcome, String toFlowDocumentId,
            Map<String, Set<NavigationCase>> navMap) {

        Set<NavigationCase> caseSet = navMap.get(viewId);

        if (caseSet == null) {
            return null;
        }

        // We've found an exact match for the viewIdToTest. Now we need to evaluate
        // from-action/outcome in the following order:
        // 1) elements specifying both from-action and from-outcome
        // 2) elements specifying only from-outcome
        // 3) elements specifying only from-action
        // 4) elements where both from-action and from-outcome are null

        CaseStruct result = determineViewFromActionOutcome(ctx, caseSet, fromAction, outcome, toFlowDocumentId);
        if (null != result) {
            FlowHandler flowHandler = ctx.getApplication().getFlowHandler();
            if (null != flowHandler) {
                result.currentFlow = flowHandler.getCurrentFlow(ctx);
                result.newFlow = result.currentFlow;
            }
        }

        return result;
    }

    /**
     * This method traverses the wild card match List (containing <code>from-view-id</code> strings and finds the List of
     * cases for each <code>from-view-id</code> string. Refer to section 7.4.2 of the specification for more details.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param viewId The current <code>view</code> identifier.
     * @param fromAction The action reference string.
     * @param outcome The outcome string.
     * @return The <code>view</code> identifier.
     */

    private CaseStruct findWildCardMatch(FacesContext ctx, String viewId, String fromAction, String outcome, String toFlowDocumentId,
            Map<String, Set<NavigationCase>> navMap) {
        CaseStruct result = null;

        StringBuilder sb = new StringBuilder(32);
        for (String fromViewId : getWildCardMatchList(ctx)) {
            // See if the entire wildcard string (without the trailing "*" is
            // contained in the incoming viewIdToTest.
            // Ex: /foobar is contained with /foobarbaz
            // If so, then we have found our largest pattern match..
            // If not, then continue on to the next case;

            if (!viewId.startsWith(fromViewId)) {
                continue;
            }

            // Append the trailing "*" so we can do our map lookup;

            if (sb.length() != 0) {
                sb.delete(0, sb.length());
            }
            String wcFromViewId = sb.append(fromViewId).append('*').toString();
            Set<NavigationCase> ccaseSet = navMap.get(wcFromViewId);

            if (ccaseSet == null) {
                return null;
            }

            // If we've found a match, then we need to evaluate
            // from-action/outcome in the following order:
            // 1) elements specifying both from-action and from-outcome
            // 2) elements specifying only from-outcome
            // 3) elements specifying only from-action
            // 4) elements where both from-action and from-outcome are null

            result = determineViewFromActionOutcome(ctx, ccaseSet, fromAction, outcome, toFlowDocumentId);
            if (result != null) {
                break;
            }
        }
        if (null != result) {
            FlowHandler flowHandler = ctx.getApplication().getFlowHandler();
            if (null != flowHandler) {
                result.currentFlow = flowHandler.getCurrentFlow(ctx);
                result.newFlow = result.currentFlow;
            }
        }

        return result;
    }

    /**
     * This method will extract the cases for which a <code>from-view-id</code> is an asterisk "*". Refer to section 7.4.2
     * of the specification for more details.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param fromAction The action reference string.
     * @param outcome The outcome string.
     * @return The <code>view</code> identifier.
     */

    private CaseStruct findDefaultMatch(FacesContext ctx, String fromAction, String outcome, String toFlowDocumentId, Map<String, Set<NavigationCase>> navMap) {

        Set<NavigationCase> caseSet = navMap.get("*");

        if (caseSet == null) {
            return null;
        }

        // We need to evaluate from-action/outcome in the follow
        // order: 1)elements specifying both from-action and from-outcome
        // 2) elements specifying only from-outcome
        // 3) elements specifying only from-action
        // 4) elements where both from-action and from-outcome are null

        CaseStruct result = determineViewFromActionOutcome(ctx, caseSet, fromAction, outcome, toFlowDocumentId);

        if (null != result) {
            FlowHandler flowHandler = ctx.getApplication().getFlowHandler();
            if (null != flowHandler) {
                result.currentFlow = flowHandler.getCurrentFlow(ctx);
                result.newFlow = result.currentFlow;
            }
        }

        return result;
    }

    private CaseStruct findRootNavigationMapAbandonedFlowMatch(FacesContext ctx, String viewId, String fromAction, String outcome, String toFlowDocumentId) {
        CaseStruct caseStruct = null;
        Map<String, Set<NavigationCase>> navMap = getRootNaviMap();

        if (viewId != null) {
            caseStruct = findExactMatch(ctx, viewId, fromAction, outcome, toFlowDocumentId, navMap);

            if (caseStruct == null) {
                caseStruct = findWildCardMatch(ctx, viewId, fromAction, outcome, toFlowDocumentId, navMap);
            }
        }

        if (caseStruct == null) {
            caseStruct = findDefaultMatch(ctx, fromAction, outcome, toFlowDocumentId, navMap);
        }

        if (null != caseStruct) {
            caseStruct.newFlow = FlowImpl.ABANDONED_FLOW;
        }

        return caseStruct;
    }

    /**
     * <p>
     * Create a navigation case based on content within the outcome.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @param viewId of the {@link UIViewRoot} for the current request
     * @param fromAction the navigation action
     * @param outcome the navigation outcome
     * @return a CaseStruct representing the the navigation result based on the provided input
     */
    private CaseStruct findImplicitMatch(FacesContext context, String viewId, String fromAction, String outcome, String flowDefiningDocumentId) {

        // look for an implicit match.
        String viewIdToTest = outcome;
        String currentViewId = viewId;
        Map<String, List<String>> parameters = null;
        boolean isRedirect = false;
        boolean isIncludeViewParams = false;
        CaseStruct result = null;

        int questionMark = viewIdToTest.indexOf('?');
        String queryString;
        if (-1 != questionMark) {
            int viewIdLen = viewIdToTest.length();
            if (viewIdLen <= questionMark + 1) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.navigation_invalid_query_string", viewIdToTest);
                }
                if (development) {
                    String key;
                    Object[] params;
                    key = MessageUtils.NAVIGATION_INVALID_QUERY_STRING_ID;
                    params = new Object[] { viewIdToTest };
                    FacesMessage m = MessageUtils.getExceptionMessage(key, params);
                    m.setSeverity(FacesMessage.SEVERITY_WARN);
                    context.addMessage(null, m);
                }
                queryString = null;
                viewIdToTest = viewIdToTest.substring(0, questionMark);
            } else {
                queryString = viewIdToTest.substring(questionMark + 1);
                viewIdToTest = viewIdToTest.substring(0, questionMark);

                Matcher m = REDIRECT_EQUALS_TRUE.matcher(queryString);
                if (m.find()) {
                    isRedirect = true;
                    queryString = queryString.replace(m.group(2), "");
                }
                m = INCLUDE_VIEW_PARAMS_EQUALS_TRUE.matcher(queryString);
                if (m.find()) {
                    isIncludeViewParams = true;
                    queryString = queryString.replace(m.group(2), "");
                }
            }

            if (queryString != null && queryString.length() > 0) {
                Map<String, Object> appMap = context.getExternalContext().getApplicationMap();

                String[] queryElements = Util.split(appMap, queryString, "&amp;|&");
                for (String queryElement : queryElements) {
                    String[] elements = Util.split(appMap, queryElement, "=", 2);
                    if (elements.length == 2) {
                        String rightHandSide = elements[1];
                        String sanitized = null != rightHandSide && 2 < rightHandSide.length() ? rightHandSide.trim() : "";
                        if (sanitized.contains("#{") || sanitized.contains("${")) {
                            if (LOGGER.isLoggable(Level.INFO)) {
                                LOGGER.log(Level.INFO, "faces.navigation_invalid_query_string", rightHandSide);
                            }
                            rightHandSide = "";
                        }

                        if (parameters == null) {
                            parameters = new LinkedHashMap<>(queryElements.length / 2, 1.0f);
                        }

                        parameters.computeIfAbsent(elements[0], k -> new ArrayList<>(2))
                                  .add(rightHandSide);
                    }
                }
            }
        }

        // If the viewIdToTest needs an extension, take one from the currentViewId.
        StringBuilder viewIdBuf = new StringBuilder(viewIdToTest);
        if (viewIdToTest.lastIndexOf('.') == -1) {
            int idx = currentViewId.lastIndexOf('.');
            if (idx != -1) {
                viewIdBuf.append(currentViewId.substring(idx));
            }
        }

        if (viewIdToTest.charAt(0) != '/') {
            int lastSlash = currentViewId.lastIndexOf('/');
            if (lastSlash != -1) {
                currentViewId = currentViewId.substring(0, lastSlash + 1);
                viewIdBuf.insert(0, currentViewId);
            } else {
                viewIdBuf.insert(0, "/");
            }
        }
        viewIdToTest = viewIdBuf.toString();

        ViewHandler viewHandler = Util.getViewHandler(context);
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        Flow currentFlow = null;
        Flow newFlow = null;

        if (null != flowHandler) {

            currentFlow = flowHandler.getCurrentFlow(context);
            newFlow = currentFlow;
            // If we are in a flow, use the implicit rules to ensure the view
            // is within that flow. This means viewIdToTest must start with
            // the current flow id
            if (null != currentFlow && null != viewIdToTest && !viewIdToTest.startsWith("/" + currentFlow.getId())) {
                // ... it must be out of the current flow.
                // If this is a flow return...
                if (FlowHandler.NULL_FLOW.equals(flowDefiningDocumentId)) {
                    // treat it as such
                    newFlow = null;
                    viewIdToTest = null;
                } else {
                    // otherwise Make sure the current flow is marked as abandoned.
                    newFlow = FlowImpl.ABANDONED_FLOW;
                }

            }
        }
        if (null != viewIdToTest) {
            viewIdToTest = viewHandler.deriveViewId(context, viewIdToTest);
        }

        if (null == result && null != viewIdToTest) {
            result = new CaseStruct();
            result.viewId = viewIdToTest;
            // If this navigation case does not involve flows, make sure
            // to convey that fact.
            if (null == newFlow && null == currentFlow && !FlowHandlerImpl.NULL_FLOW.equals(flowDefiningDocumentId)) {
                flowDefiningDocumentId = null;
            }
            result.navCase = new NavigationCase(currentViewId, fromAction, outcome, null, viewIdToTest, flowDefiningDocumentId, parameters, isRedirect,
                    isIncludeViewParams);
        }
        if (null != result) {
            result.currentFlow = currentFlow;
            result.newFlow = newFlow;
        }

        return result;

    }

    private CaseStruct findSwitchMatch(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }

        CaseStruct result = null;
        NavigationInfo info = getNavigationInfo(context, toFlowDocumentId, fromAction);
        Flow currentFlow = flowHandler.getCurrentFlow(context);

        if (null != info && null != info.switches && !info.switches.isEmpty()) {
            SwitchNode switchNode = info.switches.get(outcome);
            if (null != switchNode) {
                // Determine the outcome from the switch. The outcome
                // may come from exactly one of two places:
                // 1. the switch statements
                List<SwitchCase> cases = switchNode.getCases();
                boolean matched = false;
                for (SwitchCase cur : cases) {
                    if (cur.getCondition(context)) {
                        outcome = cur.getFromOutcome();
                        matched = true;
                        break;
                    }
                }
                // 2. the default outcome
                if (!matched || outcome == null) {
                    outcome = switchNode.getDefaultOutcome(context);
                }
                if (null != outcome) {
                    if (null != currentFlow) {
                        FlowNode targetNode = currentFlow.getNode(outcome);
                        if (targetNode instanceof MethodCallNode) {
                            result = findMethodCallMatch(context, fromAction, outcome, toFlowDocumentId);
                        } else if (targetNode instanceof SwitchNode) {
                            result = findSwitchMatch(context, fromAction, outcome, toFlowDocumentId);
                        } else if (targetNode instanceof FlowCallNode) {
                            result = findFacesFlowCallMatch(context, fromAction, outcome, toFlowDocumentId);
                        }
                    }
                    if (null == result) {
                        Flow newFlow = flowHandler.getFlow(context, toFlowDocumentId, fromAction);
                        // If this outcome corresponds to an existing flow...
                        if (null != newFlow) {
                            result = synthesizeCaseStruct(context, newFlow, fromAction, outcome);
                        } else {
                            newFlow = flowHandler.getCurrentFlow(context);
                            if (null != newFlow) {
                                result = synthesizeCaseStruct(context, newFlow, fromAction, outcome);
                            }
                        }
                    }
                }
            }

        }
        if (null != result) {
            result.currentFlow = currentFlow;
            if (result.newFlow == FlowImpl.SYNTHESIZED_RETURN_CASE_FLOW) {
                result.newFlow = null;
            } else {
                result.newFlow = currentFlow;
            }
        }

        return result;
    }

    private CaseStruct synthesizeCaseStruct(FacesContext context, Flow flow, String fromAction, String outcome) {
        CaseStruct result = null;

        FlowNode node = flow.getNode(outcome);
        if (null != node) {
            if (node instanceof ViewNode) {
                result = new CaseStruct();
                result.viewId = ((ViewNode) node).getVdlDocumentId();
                result.navCase = new MutableNavigationCase(fromAction, fromAction, outcome, null, result.viewId, flow.getDefiningDocumentId(), null, false,
                        false);
            } else if (node instanceof ReturnNode) {
                String fromOutcome = ((ReturnNode) node).getFromOutcome(context);
                FlowHandler flowHandler = context.getApplication().getFlowHandler();
                try {
                    flowHandler.pushReturnMode(context);
                    result = getViewId(context, fromAction, fromOutcome, FlowHandler.NULL_FLOW);
                    // We are in a return node, but no result can be found from that
                    // node. Show the last displayed viewId from the preceding flow.
                    if (null == result) {
                        Flow precedingFlow = flowHandler.getCurrentFlow(context);
                        if (null != precedingFlow) {
                            String toViewId = flowHandler.getLastDisplayedViewId(context);
                            if (null != toViewId) {
                                result = new CaseStruct();
                                result.viewId = toViewId;
                                result.navCase = new MutableNavigationCase(context.getViewRoot().getViewId(), fromAction, outcome, null, toViewId,
                                        FlowHandler.NULL_FLOW, null, false, false);

                            }
                        }
                    } else {
                        // setDidTransition(context, true);
                        flowHandler.transition(context, flow, null, null, result.viewId);
                        setDidTransition(context, true);
                        result.newFlow = FlowImpl.SYNTHESIZED_RETURN_CASE_FLOW;
                    }
                } finally {
                    flowHandler.popReturnMode(context);
                }

            }
        } else {
            // See if there is an implicit match within this flow, using outcome
            // to derive a view id within this flow.
            String currentViewId = outcome;
            // If the viewIdToTest needs an extension, take one from the currentViewId.
            String currentExtension;
            int idx = currentViewId.lastIndexOf('.');
            if (idx != -1) {
                currentExtension = currentViewId.substring(idx);
            } else {
                // PENDING, don't hard code XHTML here, look it up from configuration
                currentExtension = ".xhtml";
            }
            String viewIdToTest = "/" + flow.getId() + "/" + outcome + currentExtension;
            ViewHandler viewHandler = Util.getViewHandler(context);
            viewIdToTest = viewHandler.deriveViewId(context, viewIdToTest);
            if (null != viewIdToTest) {
                result = new CaseStruct();
                result.viewId = viewIdToTest;
                result.navCase = new MutableNavigationCase(fromAction, fromAction, outcome, null, result.viewId, null, false, false);
            }

        }
        return result;
    }

    private CaseStruct findMethodCallMatch(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }

        CaseStruct result = null;
        Flow currentFlow = flowHandler.getCurrentFlow(context);
        if (null != currentFlow) {
            FlowNode node = currentFlow.getNode(outcome);
            if (node instanceof MethodCallNode) {
                MethodCallNode methodCallNode = (MethodCallNode) node;
                MethodExpression me = methodCallNode.getMethodExpression();
                if (null != me) {
                    List<Parameter> paramList = methodCallNode.getParameters();
                    Object[] params = null;
                    if (null != paramList) {
                        params = new Object[paramList.size()];
                        int i = 0;
                        ELContext elContext = context.getELContext();
                        for (Parameter cur : paramList) {
                            params[i++] = cur.getValue().getValue(elContext);
                        }
                    }

                    Object invokeResult = me.invoke(context.getELContext(), params);
                    if (null == invokeResult) {
                        ValueExpression ve = methodCallNode.getOutcome();
                        if (null != ve) {
                            invokeResult = ve.getValue(context.getELContext());
                        }
                    }
                    outcome = invokeResult.toString();
                    FlowNode targetNode = currentFlow.getNode(outcome);
                    if (targetNode instanceof MethodCallNode) {
                        result = findMethodCallMatch(context, fromAction, outcome, toFlowDocumentId);
                    } else if (targetNode instanceof SwitchNode) {
                        result = findSwitchMatch(context, fromAction, outcome, toFlowDocumentId);
                    } else if (targetNode instanceof FlowCallNode) {
                        result = findFacesFlowCallMatch(context, fromAction, outcome, toFlowDocumentId);
                    } else {
                        result = synthesizeCaseStruct(context, currentFlow, fromAction, outcome);
                        if (null != result) {
                            // Here we need to detect the case when the
                            // synthesizeCaseStruct() ended up hitting a <flow-return>.
                            // In this case, we must ensure the new flow of null
                            // is honored.
                            result.currentFlow = currentFlow;
                            if (result.newFlow == FlowImpl.SYNTHESIZED_RETURN_CASE_FLOW) {
                                result.newFlow = null;
                            } else {
                                result.newFlow = currentFlow;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private CaseStruct findFacesFlowCallMatch(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }

        CaseStruct result = null;
        Flow currentFlow = flowHandler.getCurrentFlow(context);
        Flow newFlow = null;
        FlowCallNode facesFlowCallNode = null;
        if (null != currentFlow) {
            FlowNode node = currentFlow.getNode(outcome);
            if (node instanceof FlowCallNode) {
                facesFlowCallNode = (FlowCallNode) node;
                String flowId = facesFlowCallNode.getCalledFlowId(context);
                String flowDocumentId = facesFlowCallNode.getCalledFlowDocumentId(context);

                if (null != flowId) {
                    newFlow = flowHandler.getFlow(context, flowDocumentId, flowId);
                    if (null != newFlow) {
                        String startNodeId = newFlow.getStartNodeId();
                        result = synthesizeCaseStruct(context, newFlow, fromAction, startNodeId);
                        if (null == result) {
                            assert null != currentFlow;
                            // If no CaseStruct can be synthesized, we must execute the
                            // navigation handler algorithm to try to find the CaseStruct
                            // for the start node. However, in order to do that, we
                            // must enter the new flow. To preserve the intergity
                            // of the state machine, we enter the flow now, and mark
                            // that we must not enter it later.
                            try {
                                setDidTransition(context, true);
                                flowHandler.transition(context, currentFlow, newFlow, facesFlowCallNode, startNodeId);
                                result = getViewId(context, fromAction, startNodeId, toFlowDocumentId);
                            } finally {
                                if (null == result) {
                                    // If we did not find a CaseStruct, preserve the
                                    // integrity of the state machine by transitioning
                                    // out of the flow.
                                    flowHandler.transition(context, newFlow, currentFlow, null, outcome);
                                    setDidTransition(context, false);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // See if we are trying to enter a flow.
            newFlow = flowHandler.getFlow(context, toFlowDocumentId, outcome);
            if (null != newFlow) {
                String startNodeId = newFlow.getStartNodeId();
                result = synthesizeCaseStruct(context, newFlow, fromAction, startNodeId);
                if (null == result) {
                    assert null == currentFlow;
                    // If no CaseStruct can be synthesized, we must execute the
                    // navigation handler algorithm to try to find the CaseStruct
                    // for the start node. However, in order to do that, we
                    // must enter the new flow. To preserve the intergity
                    // of the state machine, we enter the flow now, and mark
                    // that we must not enter it later.
                    try {
                        setDidTransition(context, true);
                        flowHandler.transition(context, null, newFlow, null, startNodeId);
                        result = getViewId(context, fromAction, startNodeId, toFlowDocumentId);
                    } finally {
                        if (null == result) {
                            // If we did not find a CaseStruct, preserve the
                            // integrity of the state machine by transitioning
                            // out of the flow.
                            flowHandler.transition(context, newFlow, null, null, outcome);
                            setDidTransition(context, false);
                        }
                    }
                } else if (!outcome.equals(startNodeId) && null != result.navCase) {
                    ((MutableNavigationCase) result.navCase).setFromOutcome(outcome);
                }
            }
        }
        if (null != result) {
            result.currentFlow = currentFlow;
            result.newFlow = newFlow;
            result.facesFlowCallNode = facesFlowCallNode;
        }

        return result;
    }

    private static final String DID_TRANSITION_FLAG = RIConstants.FACES_PREFIX + "NavigationHandlerDidTransition";

    private boolean isDidTransition(FacesContext context) {
        boolean result = context.getAttributes().containsKey(DID_TRANSITION_FLAG);

        return result;
    }

    private void setDidTransition(FacesContext context, boolean value) {
        Map<Object, Object> contextMap = context.getAttributes();
        if (value) {
            contextMap.put(DID_TRANSITION_FLAG, Boolean.TRUE);
        } else {
            contextMap.remove(DID_TRANSITION_FLAG);
        }

    }

    private CaseStruct findViewNodeMatch(FacesContext context, String fromAction, String outcome) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }

        CaseStruct result = null;
        Flow currentFlow = flowHandler.getCurrentFlow(context);
        if (null != currentFlow) {
            FlowNode node = currentFlow.getNode(outcome);
            if (node instanceof ViewNode) {
                result = synthesizeCaseStruct(context, currentFlow, fromAction, outcome);
            }
        }
        if (null != result) {
            result.currentFlow = currentFlow;
            result.newFlow = currentFlow;
            result.facesFlowCallNode = null;
        }

        return result;
    }

    private CaseStruct findReturnMatch(FacesContext context, String fromAction, String outcome) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }

        CaseStruct result = null;
        Flow currentFlow = flowHandler.getCurrentFlow(context);
        if (null != currentFlow) {
            // If so, see if the outcome is one of this flow's
            // faces-flow-return nodes.
            ReturnNode returnNode = currentFlow.getReturns().get(outcome);
            if (null != returnNode) {
                String fromOutcome = returnNode.getFromOutcome(context);
                try {
                    flowHandler.pushReturnMode(context);
                    result = getViewId(context, fromAction, fromOutcome, FlowHandler.NULL_FLOW);
                    // We are in a return node, but no result can be found from that
                    // node. Show the last displayed viewId from the preceding flow.
                    if (null == result) {
                        Flow precedingFlow = flowHandler.getCurrentFlow(context);
                        if (null != precedingFlow) {
                            String toViewId = flowHandler.getLastDisplayedViewId(context);
                            if (null != toViewId) {
                                result = new CaseStruct();
                                result.viewId = toViewId;
                                result.navCase = new NavigationCase(context.getViewRoot().getViewId(), fromAction, outcome, null, toViewId,
                                        FlowHandler.NULL_FLOW, null, false, false);
                            }
                        }
                    }
                } finally {
                    flowHandler.popReturnMode(context);
                }
            }
        }

        if (null != result && result.facesFlowCallNode == null) {
            result.currentFlow = currentFlow;
            result.newFlow = null;
        }

        return result;
    }

    /**
     * This method will attempt to find the <code>view</code> identifier based on action reference and outcome. Refer to
     * section 7.4.2 of the specification for more details.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param caseSet The list of navigation cases.
     * @param fromAction The action reference string.
     * @param outcome The outcome string.
     * @return The <code>view</code> identifier.
     */
    private CaseStruct determineViewFromActionOutcome(FacesContext ctx, Set<NavigationCase> caseSet, String fromAction, String outcome,
            String toFlowDocumentId) {

        CaseStruct result = new CaseStruct();
        boolean match = false;
        for (NavigationCase cnc : caseSet) {
            String cncFromAction = cnc.getFromAction();
            String cncFromOutcome = cnc.getFromOutcome();
            boolean cncHasCondition = cnc.hasCondition();
            String cncToViewId = cnc.getToViewId(ctx);

            if (cncFromAction != null && cncFromAction.equals(fromAction) && cncFromOutcome != null && cncFromOutcome.equals(outcome)) {
                match = true;
            } else if (cncFromAction == null && cncFromOutcome != null && cncFromOutcome.equals(outcome)) {
                match = true;
            } else if (cncFromAction != null && cncFromAction.equals(fromAction) && cncFromOutcome == null && (outcome != null || cncHasCondition)) {
                match = true;
            } else if (cncFromAction == null && cncFromOutcome == null && (outcome != null || cncHasCondition)) {
                match = true;
            }

            if (match) {
                result.viewId = cncToViewId;
                result.navCase = cnc;
                if (cncHasCondition && Boolean.FALSE.equals(cnc.getCondition(ctx))) {
                    match = false;
                } else {
                    toFlowDocumentId = null != cnc.getToFlowDocumentId() ? cnc.getToFlowDocumentId() : toFlowDocumentId;
                    if (null != toFlowDocumentId) {
                        FlowHandler fh = ctx.getApplication().getFlowHandler();
                        if (null != outcome) {
                            result.isFlowEntryFromExplicitRule = null != fh.getFlow(ctx, toFlowDocumentId, convertToViewIdToFlowOrNodeId(ctx, result));
                        }
                    }
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * To look for the Flow or Node by the id, the '/' in the id got from navCase should be trimmed.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param caseStruct the {@link CaseStruct} to look for the to view id
     * @return id of possible Node or Flow without '/' in the string
     */
    private String convertToViewIdToFlowOrNodeId(FacesContext ctx, CaseStruct caseStruct) {
        String viewId = caseStruct.navCase.getToViewId(ctx);
        return viewId.substring(viewId.lastIndexOf('/') + 1);
    }

    // ---------------------------------------------------------- Nested Classes

    private static class CaseStruct {
        String viewId;
        NavigationCase navCase;
        Flow currentFlow;
        Flow newFlow;
        FlowCallNode facesFlowCallNode;
        boolean isFlowEntryFromExplicitRule;
    }

    private static final class NavigationInfo {
        private NavigationMap ruleSet;
        private Map<String, SwitchNode> switches;
    }

    private static final class NavigationMap extends AbstractMap<String, Set<NavigationCase>> {

        private final HashMap<String, Set<NavigationCase>> mapToLookForNavCase = new HashMap<>();
        private final TreeSet<String> wildcardMatchList = new TreeSet<>((fromViewId1, fromViewId2) -> -fromViewId1.compareTo(fromViewId2));

        // ---------------------------------------------------- Methods from Map

        @Override
        public int size() {
            return mapToLookForNavCase.size();
        }

        @Override
        public boolean isEmpty() {
            return mapToLookForNavCase.isEmpty();
        }

        @Override
        public Set<NavigationCase> put(String key, Set<NavigationCase> value) {
            if (key == null) {
                throw new IllegalArgumentException(key);
            }
            if (value == null) {
                throw new IllegalArgumentException();
            }
            updateWildcards(key);
            Set<NavigationCase> existing = mapToLookForNavCase.get(key);
            if (existing == null) {
                mapToLookForNavCase.put(key, value);
                return null;
            } else {
                existing.addAll(value);
                return existing;
            }

        }

        @Override
        public void putAll(Map<? extends String, ? extends Set<NavigationCase>> m) {
            if (m == null) {
                return;
            }
            for (Map.Entry<? extends String, ? extends Set<NavigationCase>> entry : m.entrySet()) {
                String key = entry.getKey();
                updateWildcards(key);
                Set<NavigationCase> existing = mapToLookForNavCase.get(key);
                if (existing == null) {
                    mapToLookForNavCase.put(key, entry.getValue());
                } else {
                    existing.addAll(entry.getValue());
                }
            }
        }

        @Override
        public Set<String> keySet() {
            return new AbstractSet<>() {

                @Override
                public Iterator<String> iterator() {
                    return new Iterator<>() {

                        Iterator<Map.Entry<String, Set<NavigationCase>>> i = entrySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public String next() {
                            return i.next().getKey();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public int size() {
                    return NavigationMap.this.size();
                }
            };
        }

        @Override
        public Collection<Set<NavigationCase>> values() {
            return new AbstractCollection<>() {

                @Override
                public Iterator<Set<NavigationCase>> iterator() {
                    return new Iterator<>() {

                        Iterator<Map.Entry<String, Set<NavigationCase>>> i = entrySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public Set<NavigationCase> next() {
                            return i.next().getValue();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public int size() {
                    return NavigationMap.this.size();
                }
            };
        }

        @Override
        public Set<Entry<String, Set<NavigationCase>>> entrySet() {
            return new AbstractSet<>() {

                @Override
                public Iterator<Entry<String, Set<NavigationCase>>> iterator() {

                    return new Iterator<>() {

                        Iterator<Entry<String, Set<NavigationCase>>> i = mapToLookForNavCase.entrySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public Entry<String, Set<NavigationCase>> next() {
                            return i.next();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public int size() {
                    return NavigationMap.this.size();
                }
            };
        }

        // ----------------------------------------------------- Private Methods

        private void updateWildcards(String fromViewId) {

            if (!mapToLookForNavCase.containsKey(fromViewId) && fromViewId.endsWith("*")) {
                wildcardMatchList.add(fromViewId.substring(0, fromViewId.lastIndexOf('*')));
            }
        }

    }
}
