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

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <span class="changed_modified_2_0 changed_modified_2_2">A</span> <strong>NavigationHandler</strong> is passed the
 * outcome string returned by an application action invoked for this application, and will use this (along with related
 * state information) to choose the view to be displayed next.
 * </p>
 *
 * <p>
 * A default implementation of <code>NavigationHandler</code> must be provided by the Jakarta Faces
 * implementation, which will be utilized unless <code>setNavigationHandler()</code> is called to establish a different
 * one. <span class="changed_added_2_0">An implementation of this class must be thread-safe.</span> This default
 * instance will compare the view identifier of the current view, the specified action binding, and the specified
 * outcome against any navigation rules provided in <code>faces-config.xml</code> file(s). If a navigation case matches,
 * the current view will be changed by a call to <code>FacesContext.setViewRoot()</code>. Note that a <code>null</code>
 * outcome value will never match any navigation rule, so it can be used as an indicator that the current view should be
 * redisplayed.
 * </p>
 */

public abstract class NavigationHandler {

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> navigation processing based on the state information in the
     * specified {@link FacesContext}, plus the outcome string returned by an executed application action.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If the implementation class also extends {@link ConfigurableNavigationHandler}, the implementation must guarantee
     * that the logic used in a call to {@link ConfigurableNavigationHandler#getNavigationCase} is used in this method to
     * determine the correct navigation.
     * </p>
     *
     * <p class="changed_added_2_0">
     * This method must set the render targets (used in partial rendering) to <code>render all </code> invoking
     * {@link jakarta.faces.context.PartialViewContext#setRenderAll}) if the view identifier has changed as the result of an
     * application action (to take into account <code>Ajax requests</code>).
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     * @param fromAction The action binding expression that was evaluated to retrieve the specified outcome, or
     * <code>null</code> if the outcome was acquired by some other means
     * @param outcome The logical outcome returned by a previous invoked application action (which may be <code>null</code>)
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void handleNavigation(FacesContext context, String fromAction, String outcome);

    /**
     * <p class="changed_added_2_2">
     * Overloaded variant of
     * {@link #handleNavigation(jakarta.faces.context.FacesContext, java.lang.String, java.lang.String)} that allows the
     * caller to provide the defining document id for a flow to be entered by this navigation. For backward compatibility
     * with decorated {@code NavigationHandler} implementations that conform to an earlier version of the specification, an
     * implementation is provided that calls through to
     * {@link #handleNavigation(jakarta.faces.context.FacesContext, java.lang.String, java.lang.String)}, ignoring the
     * {@code toFlowDocumentId} parameter.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     * @param fromAction The action binding expression that was evaluated to retrieve the specified outcome, or
     * <code>null</code> if the outcome was acquired by some other means
     * @param outcome The logical outcome returned by a previous invoked application action (which may be <code>null</code>)
     * @param toFlowDocumentId The defining document id of the flow into which this navigation will cause entry.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public void handleNavigation(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        this.handleNavigation(context, fromAction, outcome);
    }

}
