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

package jakarta.faces.component.visit;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_1">An</span> enum that specifies hints that impact the behavior of a component tree
 * visit.
 * </p>
 *
 * @since 2.0
 */
public enum VisitHint {

    /**
     * <p class="changed_added_2_0">
     * Hint that indicates that only the rendered subtrees should be visited.
     * </p>
     *
     * @since 2.0
     */
    SKIP_UNRENDERED,

    /**
     * <p class="changed_added_2_0">
     * Hint that indicates that only non-transient subtrees should be visited.
     * </p>
     *
     * @since 2.0
     */
    SKIP_TRANSIENT,

    /**
     * <p class="changed_added_2_1">
     * Hint that indicates that components that normally visit children multiple times (eg. <code>UIData</code>) in an
     * iterative fashion should instead visit each child only one time.
     * </p>
     *
     * @since 2.1
     */
    SKIP_ITERATION,

    /**
     * <p class="changed_added_2_0 changed_modified_2_3">
     * Hint that indicates that the visit is being performed as part of lifecycle phase execution.
     * </p>
     *
     * @since 2.0
     */
    EXECUTE_LIFECYCLE,

}
