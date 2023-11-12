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
 *
 * <p class="changed_added_2_0">
 * An enum that specifies the possible results of a call to {@link VisitCallback#visit}.
 * </p>
 *
 * @see VisitCallback#visit VisitCallback.visit()
 *
 * @since 2.0
 */
public enum VisitResult {

    /**
     * <p class="changed_added_2_0">
     * This result indicates that the tree visit should descend into current component's subtree.
     * </p>
     *
     * @since 2.0
     */
    ACCEPT,

    /**
     * <p class="changed_added_2_0">
     * This result indicates that the tree visit should continue, but should skip the current component's subtree.
     * </p>
     */
    REJECT,

    /**
     * <p class="changed_added_2_0">
     * This result indicates that the tree visit should be terminated.
     * </p>
     */
    COMPLETE
}
