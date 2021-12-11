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

package jakarta.faces.application;

import static jakarta.faces.application.SharedUtils.evaluateExpressions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.context.ExternalContext;
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
public class NavigationCase {

    private final String fromViewId;
    private final String fromAction;
    private final String fromOutcome;
    private final String condition;
    private final String toViewId;
    private final String toFlowDocumentId;
    private final Map<String, List<String>> parameters;
    private final boolean redirect;
    private final boolean includeViewParams;

    private ValueExpression toViewIdExpr;
    private ValueExpression conditionExpr;
    private String toString;
    private int hashCode;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Construct a new <code>NavigationCase</code> based on the provided arguments. 
     * See section 7.4.2 "Default NavigationHandler Algorithm" of the Jakarta Faces Specification Document for how a
     * <code>NavigationCase</code> is used by the standard {@link ConfigurableNavigationHandler}
     * </p>
     *
     * @param fromViewId return from {@link #getFromViewId}
     * @param fromAction return from {@link #getFromAction}
     * @param fromOutcome return from {@link #getFromOutcome}
     * @param condition A string to be interpreted as a <code>ValueExpression</code> by a call to {@link #getCondition}
     * @param toViewId return from {@link #getToViewId}
     * @param parameters return from {@link #getParameters}
     * @param redirect return from {@link #isRedirect}
     * @param includeViewParams return {@link #isIncludeViewParams}
     */
    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId, Map<String, List<String>> parameters,
            boolean redirect, boolean includeViewParams) {

        this.fromViewId = fromViewId;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.condition = condition;
        this.toViewId = toViewId;
        toFlowDocumentId = null;
        this.parameters = parameters;
        this.redirect = redirect;
        this.includeViewParams = includeViewParams;

    }

    /**
     * <p class="changed_added_2_0">
     * Construct a new <code>NavigationCase</code> based on the provided arguments. 
     * See section 7.4.2 "Default NavigationHandler Algorithm" of the Jakarta Faces Specification Document 
     * for how a <code>NavigationCase</code> is used by the standard {@link ConfigurableNavigationHandler}
     * </p>
     *
     * @param fromViewId return from {@link #getFromViewId}
     * @param fromAction return from {@link #getFromAction}
     * @param fromOutcome return from {@link #getFromOutcome}
     * @param condition A string to be interpreted as a <code>ValueExpression</code> by a call to {@link #getCondition}
     * @param toViewId return from {@link #getToViewId}
     * @param toFlowDocumentId the toFlow documentId.
     * @param parameters return from {@link #getParameters}
     * @param redirect return from {@link #isRedirect}
     * @param includeViewParams return {@link #isIncludeViewParams}
     */
    public NavigationCase(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId, String toFlowDocumentId,
            Map<String, List<String>> parameters, boolean redirect, boolean includeViewParams) {

        this.fromViewId = fromViewId;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.condition = condition;
        this.toViewId = toViewId;
        this.toFlowDocumentId = toFlowDocumentId;
        this.parameters = parameters;
        this.redirect = redirect;
        this.includeViewParams = includeViewParams;

    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p class="changed_added_2_0">
     * Construct an absolute URL to this <code>NavigationCase</code> instance using
     * {@link jakarta.faces.application.ViewHandler#getActionURL} on the path portion of the url.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the action URL.
     * @throws MalformedURLException if the process of constructing the URL causes this exception to be thrown.
     */
    public URL getActionURL(FacesContext context) throws MalformedURLException {
        ExternalContext extContext = context.getExternalContext();

        return new URL(extContext.getRequestScheme(), extContext.getRequestServerName(), extContext.getRequestServerPort(),
                context.getApplication().getViewHandler().getActionURL(context, getToViewId(context)));
    }

    /**
     * <p class="changed_added_2_0">
     * Construct an absolute URL to this <code>NavigationCase</code> instance using
     * {@link jakarta.faces.application.ViewHandler#getResourceURL} on the path portion of the url.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the resource URL.
     * @throws MalformedURLException if the process of constructing the URL causes this exception to be thrown.
     */
    public URL getResourceURL(FacesContext context) throws MalformedURLException {
        ExternalContext extContext = context.getExternalContext();

        return new URL(extContext.getRequestScheme(), extContext.getRequestServerName(), extContext.getRequestServerPort(),
                context.getApplication().getViewHandler().getResourceURL(context, getToViewId(context)));
    }

    /**
     * <p class="changed_added_2_0">
     * Construct an absolute URL suitable for a "redirect" to this <code>NavigationCase</code> instance using
     * {@link jakarta.faces.application.ViewHandler#getRedirectURL} on the path portion of the url.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the redirect URL.
     * @throws MalformedURLException if the process of constructing the URL causes this exception to be thrown.
     */
    public URL getRedirectURL(FacesContext context) throws MalformedURLException {
        ExternalContext extContext = context.getExternalContext();

        return new URL(extContext.getRequestScheme(), extContext.getRequestServerName(), extContext.getRequestServerPort(), context.getApplication()
                .getViewHandler().getRedirectURL(context, getToViewId(context), evaluateExpressions(context, getParameters()), isIncludeViewParams()));
    }

    /**
     * <p class="changed_added_2_0">
     * Construct an absolute URL suitable for a bookmarkable link to this <code>NavigationCase</code> instance using
     * {@link jakarta.faces.application.ViewHandler#getBookmarkableURL} on the path portion of the url. This URL may include
     * view parameters specified as metadata within the view.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the bookmarkable URL.
     * @throws MalformedURLException if the process of constructing the URL causes this exception to be thrown.
     */
    public URL getBookmarkableURL(FacesContext context) throws MalformedURLException {
        ExternalContext extContext = context.getExternalContext();
        
        return new URL(extContext.getRequestScheme(), extContext.getRequestServerName(), extContext.getRequestServerPort(),
                context.getApplication().getViewHandler().getBookmarkableURL(context, getToViewId(context), getParameters(), isIncludeViewParams()));
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>&lt;from-view-id&gt;</code> of the <code>&lt;navigation-rule&gt;</code> inside which this
     * <code>&lt;navigation-case&gt;</code> is nested.
     * </p>
     *
     * @return the from viedId.
     */
    public String getFromViewId() {
        return fromViewId;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>&lt;from-action&gt;</code> for this <code>&lt;navigation-case&gt;</code>
     * </p>
     *
     * @return the from action.
     */
    public String getFromAction() {
        return fromAction;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>&lt;from-outcome&gt;</code> for this <code>&lt;navigation-case&gt;</code>
     * </p>
     *
     * @return the from outcome.
     */
    public String getFromOutcome() {
        return fromOutcome;
    }

    /**
     * <p class="changed_added_2_0">
     * Evaluates the <code>&lt;to-view-id&gt;</code> for this <code>&lt;navigation-case&gt;</code>
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     *
     * @return the view ID that should be navigated to
     */
    public String getToViewId(FacesContext context) {
        if (toViewIdExpr == null) {
            toViewIdExpr = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), toViewId, String.class);
        }

        String newToViewId = (String) toViewIdExpr.getValue(context.getELContext());
        if (newToViewId.charAt(0) != '/') {
            newToViewId = '/' + newToViewId;
        }

        return newToViewId;
    }

    /**
     * <p class="changed_added_2_2">
     * If this navigation case represents a flow invocation, this property is the documentId in which the flow whose id is
     * given by the return from {@link #getFromOutcome()} is defined. Implementations must override this method to return
     * the value defined in the corresponding application configuration resources element. The base implementation returns
     * the empty string.
     * </p>
     *
     * @return the toFlow documentId.
     * @since 2.2
     */
    public String getToFlowDocumentId() {
        return toFlowDocumentId;
    }

    /**
     * <p class="changed_added_2_0">
     * Test if this navigation case has an associated <code>&lt;if&gt;</code> element.
     *
     * @return <code>true</code> if there's an <code>&lt;if&gt;</code> element associated with this
     * <code>&lt;navigation-case&gt;</code>, otherwise <code>false</code>
     */
    public boolean hasCondition() {
        return condition != null;
    }

    /**
     * <p class="changed_added_2_0">
     * Evaluates the <code>&lt;if&gt;</code> for this <code>&lt;navigation-case&gt;</code>, if any. The expression to be
     * evaluated is passed into the constructor as a string. When the expression is evaluated, its value must be coerced
     * into a <code>boolean</code> per the normal Jakarta Expression Language coercion rules.
     * </p>
     *
     * Note throws any exceptions encountered during the process of evaluating the expression or obtaining its value.
     *
     * @param context the {@link FacesContext} for the current request
     *
     * @return <code>null</code> if there is no <code>&lt;if&gt;</code> element associated with this
     * <code>&lt;navigation-case&gt;</code>, otherwise return the evaluation result of the condition
     */
    public Boolean getCondition(FacesContext context) {
        if (conditionExpr == null && condition != null) {
            conditionExpr = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), condition, Boolean.class);
        }

        return conditionExpr != null ? (Boolean) conditionExpr.getValue(context.getELContext()) : null;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the parameters to be included for navigation cases requiring a redirect. If no parameters are defined,
     * <code>null</code> will be returned. The keys in the <code>Map</code> are parameter names. For each key, the
     * corresponding value is a <code>List</code> of unconverted values.
     * </p>
     *
     * @return the list of parameters, or <code>null</code>
     */
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>&lt;redirect&gt;</code> value for this <code>&lt;navigation-case&gt;</code>. This will be
     * <code>true</code> if the new view should be navigated to via a
     * {@link jakarta.faces.context.ExternalContext#redirect(String)}
     * </p>
     *
     * @return <code>true</code> if this is a redirect, <code>false</code> otherwise.
     */
    public boolean isRedirect() {
        return redirect;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>&lt;redirect&gt;</code> value for this <code>&lt;navigation-case&gt;</code>. This will be
     * <code>true</code> if the view parametets should be encoded into the redirect URL (only applies to redirect case)
     * </p>
     *
     * @return <code>true</code> if view parameters are to be included, <code>false</code> otherwise.
     */
    public boolean isIncludeViewParams() {
        return includeViewParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NavigationCase that = (NavigationCase) o;

        return redirect == that.redirect && !(fromAction != null ? !fromAction.equals(that.fromAction) : that.fromAction != null)
                && !(fromOutcome != null ? !fromOutcome.equals(that.fromOutcome) : that.fromOutcome != null)
                && !(condition != null ? !condition.equals(that.condition) : that.condition != null)
                && !(fromViewId != null ? !fromViewId.equals(that.fromViewId) : that.fromViewId != null)
                && !(toViewId != null ? !toViewId.equals(that.toViewId) : that.toViewId != null)
                && !(toFlowDocumentId != null ? !toFlowDocumentId.equals(that.toFlowDocumentId) : that.toFlowDocumentId != null)
                && !(parameters != null ? !parameters.equals(that.parameters) : that.parameters != null);

    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = fromViewId != null ? fromViewId.hashCode() : 0;
            result = 31 * result + (fromAction != null ? fromAction.hashCode() : 0);
            result = 31 * result + (fromOutcome != null ? fromOutcome.hashCode() : 0);
            result = 31 * result + (condition != null ? condition.hashCode() : 0);
            result = 31 * result + (toViewId != null ? toViewId.hashCode() : 0);
            result = 31 * result + (toFlowDocumentId != null ? toFlowDocumentId.hashCode() : 0);
            result = 31 * result + (redirect ? 1 : 0);
            result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
            hashCode = result;
        }
        
        return hashCode;

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
