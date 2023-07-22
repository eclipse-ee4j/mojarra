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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.sun.faces.application.NavigationHandlerImpl;
import com.sun.faces.flow.FlowHandlerImpl;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.Util;

import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutcomeTarget;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionListener;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.lifecycle.ClientWindow;

public abstract class OutcomeTargetRenderer extends HtmlBasicRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
    }

    // ------------------------------------------------------- Protected Methods

    protected void renderPassThruAttributes(FacesContext ctx, ResponseWriter writer, UIComponent component, Attribute[] attributes, List excludedAttributes)
            throws IOException {
        RenderKitUtils.renderPassThruAttributes(ctx, writer, component, attributes);
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component, excludedAttributes);

    }

    protected String getLabel(UIComponent component) {

        Object value = ((UIOutcomeTarget) component).getValue();
        return value != null ? value.toString() : "";

    }

    protected String getFragment(UIComponent component) {

        String fragment = (String) component.getAttributes().get("fragment");
        fragment = fragment != null ? fragment.trim() : "";
        if (fragment.length() > 0) {
            fragment = "#" + fragment;
        }
        return fragment;

    }

    @Override
    protected Object getValue(UIComponent component) {

        return ((UIOutcomeTarget) component).getValue();

    }

    protected boolean isIncludeViewParams(UIComponent component, NavigationCase navcase) {

        return ((UIOutcomeTarget) component).isIncludeViewParams() || navcase.isIncludeViewParams();

    }

    /**
     * Invoke the {@link NavigationHandler} preemptively to resolve a {@link NavigationCase} for the outcome declared on the
     * {@link UIOutcomeTarget} component. The current view id is used as the from-view-id when matching navigation cases and
     * the from-action is assumed to be null.
     *
     * @param context the {@link FacesContext} for the current request
     * @param component the target {@link UIComponent}
     *
     * @return the NavigationCase represeting the outcome target
     */
    protected NavigationCase getNavigationCase(FacesContext context, UIComponent component) {
        NavigationHandler navHandler = context.getApplication().getNavigationHandler();
        if (!(navHandler instanceof ConfigurableNavigationHandler)) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "faces.outcome.target.invalid.navigationhandler.type", component.getId());
            }
            return null;
        }

        String outcome = ((UIOutcomeTarget) component).getOutcome();
        if (outcome == null) {
            outcome = context.getViewRoot().getViewId();
            // QUESTION should we avoid the call to getNavigationCase() and instead instantiate one explicitly?
            // String viewId = context.getViewRoot().getViewId();
            // return new NavigationCase(viewId, null, null, null, viewId, false, false);
        }
        String toFlowDocumentId = (String) component.getAttributes().get(ActionListener.TO_FLOW_DOCUMENT_ID_ATTR_NAME);
        NavigationCase navCase = null;
        NavigationHandlerImpl.setResetFlowHandlerStateIfUnset(context, false);
        try {
            if (null == toFlowDocumentId) {
                navCase = ((ConfigurableNavigationHandler) navHandler).getNavigationCase(context, null, outcome);
            } else {
                navCase = ((ConfigurableNavigationHandler) navHandler).getNavigationCase(context, null, outcome, toFlowDocumentId);
            }
        } finally {
            NavigationHandlerImpl.unsetResetFlowHandlerState(context);
        }

        if (navCase == null && logger.isLoggable(Level.WARNING)) {
            String componentId = component.getId();
            String viewId = context.getViewRoot().getViewId();
            Object[] logParams = new Object[] {componentId, outcome, viewId};
            logger.log(Level.WARNING, "faces.outcometarget.navigation.case.not.resolved", logParams);
        }
        return navCase;
    }

    /**
     * <p>
     * Resolve the target view id and then delegate to
     * {@link ViewHandler#getBookmarkableURL(jakarta.faces.context.FacesContext, String, java.util.Map, boolean)} to produce
     * a redirect URL, which will add the page parameters if necessary and properly prioritizing the parameter overrides.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @param component the target {@link UIComponent}
     * @param navCase the target navigation case
     *
     * @return an encoded URL for the provided navigation case
     */
    protected String getEncodedTargetURL(FacesContext context, UIComponent component, NavigationCase navCase) {
        // FIXME getNavigationCase doesn't resolve the target viewId (it is part of CaseStruct)
        String toViewId = navCase.getToViewId(context);
        Map<String, List<String>> params = getParamOverrides(component);
        addNavigationParams(navCase, params);
        String result = null;
        boolean didDisableClientWindowRendering = false;
        ClientWindow cw = null;

        try {
            Map<String, Object> attrs = component.getAttributes();
            Object val = attrs.get("disableClientWindow");
            if (null != val) {
                didDisableClientWindowRendering = "true".equalsIgnoreCase(val.toString());
            }
            if (didDisableClientWindowRendering) {
                cw = context.getExternalContext().getClientWindow();
                if (null != cw) {
                    cw.disableClientWindowRenderMode(context);
                }
            }

            result = Util.getViewHandler(context).getBookmarkableURL(context, toViewId, params, isIncludeViewParams(component, navCase));
        } finally {
            if (didDisableClientWindowRendering && null != cw) {
                cw.enableClientWindowRenderMode(context);
            }
        }

        return result;
    }

    protected void addNavigationParams(NavigationCase navCase, Map<String, List<String>> existingParams) {

        Map<String, List<String>> navParams = navCase.getParameters();
        if (navParams != null && !navParams.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : navParams.entrySet()) {
                String navParamName = entry.getKey();
                // only add the navigation params to the existing params collection
                // if the parameter name isn't already present within the existing
                // collection
                if (!existingParams.containsKey(navParamName)) {
                    if (entry.getValue().size() == 1) {
                        String value = entry.getValue().get(0);
                        String sanitized = null != value && 2 < value.length() ? value.trim() : "";
                        if (sanitized.contains("#{") || sanitized.contains("${")) {
                            FacesContext fc = FacesContext.getCurrentInstance();
                            value = fc.getApplication().evaluateExpressionGet(fc, value, String.class);
                            List<String> values = new ArrayList<>();
                            values.add(value);
                            existingParams.put(navParamName, values);
                        } else {
                            existingParams.put(navParamName, entry.getValue());
                        }
                    } else {
                        existingParams.put(navParamName, entry.getValue());
                    }
                }
            }
        }

        String toFlowDocumentId = navCase.getToFlowDocumentId();
        if (null != toFlowDocumentId) {
            if (FlowHandler.NULL_FLOW.equals(toFlowDocumentId)) {
                List<String> flowDocumentIdValues = new ArrayList<>();
                flowDocumentIdValues.add(FlowHandler.NULL_FLOW);
                existingParams.put(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, flowDocumentIdValues);

                FacesContext context = FacesContext.getCurrentInstance();
                FlowHandler fh = context.getApplication().getFlowHandler();
                if (fh instanceof FlowHandlerImpl) {
                    FlowHandlerImpl fhi = (FlowHandlerImpl) fh;
                    List<String> flowReturnDepthValues = new ArrayList<>();
                    flowReturnDepthValues.add(Integer.toString(fhi.getAndClearReturnModeDepth(context)));
                    existingParams.put(FlowHandlerImpl.FLOW_RETURN_DEPTH_PARAM_NAME, flowReturnDepthValues);
                }

            } else {
                String flowId = navCase.getFromOutcome();
                List<String> flowDocumentIdValues = new ArrayList<>();
                flowDocumentIdValues.add(toFlowDocumentId);
                existingParams.put(FlowHandler.TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME, flowDocumentIdValues);

                List<String> flowIdValues = new ArrayList<>();
                flowIdValues.add(flowId);
                existingParams.put(FlowHandler.FLOW_ID_REQUEST_PARAM_NAME, flowIdValues);
            }
        }

    }

    protected Map<String, List<String>> getParamOverrides(UIComponent component) {
        Map<String, List<String>> params = new LinkedHashMap<>();
        Param[] declaredParams = getParamList(component);
        for (Param candidate : declaredParams) {
            // QUESTION shouldn't the trimming of name should be done elsewhere?
            // null value is allowed as a way to suppress page parameter
            if (candidate.name != null && candidate.name.trim().length() > 0) {
                candidate.name = candidate.name.trim();
                List<String> values = params.get(candidate.name);
                if (values == null) {
                    values = new ArrayList<>();
                    params.put(candidate.name, values);
                }
                values.add(candidate.value);
            }
        }

        return params;
    }

}
