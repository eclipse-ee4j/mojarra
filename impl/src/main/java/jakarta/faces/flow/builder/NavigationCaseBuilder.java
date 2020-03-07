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

package jakarta.faces.flow.builder;

import jakarta.el.ValueExpression;

/**
 * <p class="changed_added_2_2">
 * Create a navigation case in the current {@link jakarta.faces.flow.Flow}.
 * </p>
 *
 * @since 2.2
 */
public abstract class NavigationCaseBuilder {

    /**
     *
     * <p class="changed_added_2_2">
     * Set the from-view-id of the current navigation case.
     * </p>
     *
     * @param fromViewId the from-view-id
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder fromViewId(String fromViewId);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the from-action of the current navigation case.
     * </p>
     *
     * @param fromAction the from-action
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder fromAction(String fromAction);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the from-outcome of the current navigation case.
     * </p>
     *
     * @param fromOutcome the from-outcome
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder fromOutcome(String fromOutcome);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the to-view-id of the current navigation case.
     * </p>
     *
     * @param toViewId the to-view-id
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder toViewId(String toViewId);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the to-flow-document-id of the current navigation case.
     * </p>
     *
     * @param toFlowDocumentId the to-flow-document-id
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder toFlowDocumentId(String toFlowDocumentId);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the if of the current navigation case.
     * </p>
     *
     * @param condition the &lt;if&gt;
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder condition(String condition);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the if of the current navigation case.
     * </p>
     *
     * @param condition the &lt;if&gt;
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract NavigationCaseBuilder condition(ValueExpression condition);

    /**
     *
     * <p class="changed_added_2_2">
     * Create a redirect within this navigation case.
     * </p>
     *
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract RedirectBuilder redirect();

    /**
     *
     * <p class="changed_added_2_2">
     * Allows populating the redirect with parameters and setting the includeViewParams option.
     * </p>
     *
     * @since 2.2
     *
     */

    public abstract class RedirectBuilder {

        /**
         * <p class="changed_added_2_2">
         * Add a parameter to the redirect.
         * </p>
         *
         * @param name the name of the redirect parameter
         * @param value the value of the redirect parameter. May not be a {@code ValueExpression}.
         * @throws NullPointerException if any of the parameters are {@code null}
         * @since 2.2
         *
         * @return the builder instance
         */

        public abstract RedirectBuilder parameter(String name, String value);

        /**
         * <p class="changed_added_2_2">
         * Indicates the current redirect should include view parameters.
         * </p>
         *
         * @since 2.2
         *
         * @return the builder instance
         */

        public abstract RedirectBuilder includeViewParams();

    }

}
