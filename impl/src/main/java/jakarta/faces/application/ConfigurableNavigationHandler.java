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

import java.util.Map;
import java.util.Set;

import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;

/**
 * <p class="changed_added_2_0">
 * <strong class="changed_modified_2_2">ConfigurableNavigationHandler</strong> extends the contract of
 * {@link NavigationHandler} to allow runtime inspection of the {@link NavigationCase}s that make up the rule-base for
 * navigation. An implementation compliant with the version of the specification in which this class was introduced (or
 * a later version) must make it so that its <code>NavigationHandler</code> is an extension of this class.
 * </p>
 *
 * @since 2.0
 */
public abstract class ConfigurableNavigationHandler extends NavigationHandler {

    /**
     * <p class="changed_added_2_0">
     * Return the {@link NavigationCase} representing the navigation that would be taken had
     * {@link NavigationHandler#handleNavigation} been called with the same arguments or <code>null</code> if there is no
     * such case.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     * @param fromAction The action binding expression that was evaluated to retrieve the specified outcome, or
     * <code>null</code> if the outcome was acquired by some other means
     * @param outcome The logical outcome returned by a previous invoked application action (which may be <code>null</code>)
     * @return the navigation case, or <code>null</code>.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     * @since 2.0
     */
    public abstract NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome);

    /**
     * <p class="changed_added_2_2">
     * Return the {@link NavigationCase} representing the navigation that would be taken had
     * {@link NavigationHandler#handleNavigation} been called with the same arguments or <code>null</code> if there is no
     * such case. Implementations that comply the version of the specification in which this method was introduced must
     * override this method. For compatibility with decorated implementations that comply with an earlier version of the
     * specification, an implementation is provided that simply calls through to
     * {@link #getNavigationCase(jakarta.faces.context.FacesContext, java.lang.String, java.lang.String)}, ignoring the
     * {@code toFlowDocumentId} parameter.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     * @param fromAction The action binding expression that was evaluated to retrieve the specified outcome, or
     * <code>null</code> if the outcome was acquired by some other means
     * @param outcome The logical outcome returned by a previous invoked application action (which may be <code>null</code>)
     * @param toFlowDocumentId The value of the <code>toFlowDocumentId</code> property for the navigation case (which may be
     * <code>null</code>)
     * @return the navigation case, or <code>null</code>.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     * @since 2.2
     */
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        return getNavigationCase(context, fromAction, outcome);
    }

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Map&lt;String,
     * Set&lt;NavigationCase&gt;&gt;</code> where the keys are <code>&lt;from-view-id&gt;</code> values and the values are
     * <code>Set&lt;NavigationCase&gt;</code> where each element in the Set is a <code>NavigationCase</code> that applies to
     * that <code>&lt;from-view-id&gt;</code>. The implementation must support live modifications to this <code>Map</code>.
     * </p>
     *
     * @return a map with navigation cases.
     * @since 2.0
     */
    public abstract Map<String, Set<NavigationCase>> getNavigationCases();

    /**
     * <p class="changed_added_2_0">
     * A convenience method to signal the Jakarta Faces implementation to perform navigation with the provided
     * outcome. When the NavigationHandler is invoked, the current viewId is treated as the "from viewId" and the "from
     * action" is null.
     * </p>
     *
     * @param outcome the provided outcome.
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public void performNavigation(String outcome) {
        this.handleNavigation(FacesContext.getCurrentInstance(), null, outcome);
    }

    /**
     * <p class="changed_added_2_2">
     * Called by the flow system to cause the flow to be inspected for navigation rules. For backward compatibility with
     * earlier implementations, an empty method is provided.
     * </p>
     *
     * @param context the Faces context.
     * @param flow the flow.
     * @since 2.2
     */
    public void inspectFlow(FacesContext context, Flow flow) {
    }
}
