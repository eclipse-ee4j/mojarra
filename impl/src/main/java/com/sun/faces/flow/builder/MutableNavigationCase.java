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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <strong class="changed_modified_2_2"> NavigationCase</strong> represents a <code>&lt;navigation-case&gt;</code> in
 * the navigation rule base, as well as the <span class="changed_modified_2_2"><code>&lt;from-view-id&gt;</code> with
 * which this <code>&lt;navigation-case&gt;</code> is a sibling</span>.
 * </p>
 *
 * @since 2.0
 */
public class MutableNavigationCase extends NavigationCase {

    private String fromViewId;
    private String fromAction;
    private String fromOutcome;
    private String condition;
    private String toViewId;
    private String toFlowDocumentId;
    private Map<String, List<String>> parameters;
    private boolean redirect;
    private boolean includeViewParams;

    private ValueExpression toViewIdExpr;
    private ValueExpression conditionExpr;
    private String toString;
    private int hashCode;

    // ------------------------------------------------------------ Constructors

    public MutableNavigationCase() {
        this(null, null, null, null, null, null, null, false, false);
        parameters = new ConcurrentHashMap<>();
    }

    public MutableNavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId, String toFlowDocumentId,
            Map<String, List<String>> parameters, boolean redirect, boolean includeViewParams) {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, toFlowDocumentId, parameters, redirect, includeViewParams);

        this.fromViewId = fromViewId;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.condition = condition;
        this.toViewId = toViewId;
        this.toFlowDocumentId = toFlowDocumentId;
        this.parameters = null != parameters ? parameters : new ConcurrentHashMap<>();
        this.redirect = redirect;
        this.includeViewParams = includeViewParams;

    }

    public MutableNavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId, String toFlowDocumentId,
            boolean redirect, boolean includeViewParams) {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, toFlowDocumentId, Collections.EMPTY_MAP, redirect, includeViewParams);

        this.fromViewId = fromViewId;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.condition = condition;
        this.toViewId = toViewId;
        this.toFlowDocumentId = toFlowDocumentId;
        parameters = Collections.emptyMap();
        this.redirect = redirect;
        this.includeViewParams = includeViewParams;

    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public String getFromViewId() {

        return fromViewId;

    }

    public void setFromViewId(String fromViewId) {
        this.fromViewId = fromViewId;
    }

    @Override
    public String getFromAction() {

        return fromAction;

    }

    public void setFromAction(String fromAction) {
        this.fromAction = fromAction;
    }

    @Override
    public String getFromOutcome() {

        return fromOutcome;

    }

    public void setFromOutcome(String fromOutcome) {
        this.fromOutcome = fromOutcome;
    }

    @Override
    public String getToViewId(FacesContext context) {

        if (toViewIdExpr == null) {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            toViewIdExpr = factory.createValueExpression(context.getELContext(), toViewId, String.class);
        }
        String result = (String) toViewIdExpr.getValue(context.getELContext());
        if (result.charAt(0) != '/') {
            result = '/' + result;
        }

        return result;

    }

    public void setToViewId(String toViewId) {
        this.toViewId = toViewId;
        toViewIdExpr = null;
    }

    @Override
    public String getToFlowDocumentId() {

        return toFlowDocumentId;

    }

    public void setToFlowDocumentId(String toFlowDocumentId) {
        this.toFlowDocumentId = toFlowDocumentId;
    }

    @Override
    public boolean hasCondition() {

        return condition != null;

    }

    @Override
    public Boolean getCondition(FacesContext context) {

        if (conditionExpr == null && condition != null) {
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            conditionExpr = factory.createValueExpression(context.getELContext(), condition, Boolean.class);
        }

        return conditionExpr != null ? (Boolean) conditionExpr.getValue(context.getELContext()) : null;

    }

    public void setCondition(String condition) {
        this.condition = condition;
        conditionExpr = null;
    }

    public void setConditionExpression(ValueExpression conditionExpression) {
        conditionExpr = conditionExpression;
    }

    @Override
    public Map<String, List<String>> getParameters() {

        return parameters;

    }

    @Override
    public boolean isRedirect() {

        return redirect;

    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    @Override
    public boolean isIncludeViewParams() {

        return includeViewParams;

    }

    public void setIncludeViewParams(boolean includeViewParams) {
        this.includeViewParams = includeViewParams;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MutableNavigationCase other = (MutableNavigationCase) obj;
        if (fromViewId == null ? other.fromViewId != null : !fromViewId.equals(other.fromViewId)) {
            return false;
        }
        if (fromAction == null ? other.fromAction != null : !fromAction.equals(other.fromAction)) {
            return false;
        }
        if (fromOutcome == null ? other.fromOutcome != null : !fromOutcome.equals(other.fromOutcome)) {
            return false;
        }
        if (condition == null ? other.condition != null : !condition.equals(other.condition)) {
            return false;
        }
        if (toViewId == null ? other.toViewId != null : !toViewId.equals(other.toViewId)) {
            return false;
        }
        if (toFlowDocumentId == null ? other.toFlowDocumentId != null : !toFlowDocumentId.equals(other.toFlowDocumentId)) {
            return false;
        }
        if (parameters != other.parameters && (parameters == null || !parameters.equals(other.parameters))) {
            return false;
        }
        if (redirect != other.redirect) {
            return false;
        }
        if (includeViewParams != other.includeViewParams) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (fromViewId != null ? fromViewId.hashCode() : 0);
        hash = 29 * hash + (fromAction != null ? fromAction.hashCode() : 0);
        hash = 29 * hash + (fromOutcome != null ? fromOutcome.hashCode() : 0);
        hash = 29 * hash + (condition != null ? condition.hashCode() : 0);
        hash = 29 * hash + (toViewId != null ? toViewId.hashCode() : 0);
        hash = 29 * hash + (toFlowDocumentId != null ? toFlowDocumentId.hashCode() : 0);
        hash = 29 * hash + (parameters != null ? parameters.hashCode() : 0);
        hash = 29 * hash + (redirect ? 1 : 0);
        hash = 29 * hash + (includeViewParams ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {

        if (toString == null) {
            StringBuilder sb = new StringBuilder(64);
            sb.append("NavigationCase{");
            sb.append("fromViewId='").append(fromViewId).append('\'');
            sb.append(", fromAction='").append(fromAction).append('\'');
            sb.append(", fromOutcome='").append(fromOutcome).append('\'');
            sb.append(", if='").append(condition).append('\'');
            sb.append(", toViewId='").append(toViewId).append('\'');
            sb.append(", faces-redirect=").append(redirect);
            sb.append(", includeViewParams=").append(includeViewParams).append('\'');
            sb.append(", parameters=").append(parameters != null ? parameters.toString() : "");
            sb.append('}');
            toString = sb.toString();
        }
        return toString;

    }

}
