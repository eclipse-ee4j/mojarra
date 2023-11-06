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

import jakarta.faces.component.UIComponent;

/**
 *
 * <p class="changed_added_2_0">
 * A simple callback interface that enables taking action on a specific UIComponent (either facet or child) during a
 * component tree visit.
 * </p>
 *
 * @see UIComponent#visitTree UIComponent.visitTree()
 *
 * @since 2.0
 */
public interface VisitCallback {

    /**
     * <p>
     * This method is called during component tree visits by {@link VisitContext#invokeVisitCallback
     * VisitContext.invokeVisitCallback()} to visit the specified component. At the point in time when this method is
     * called, the argument {@code target} is guaranteed to be in the proper state with respect to its ancestors in the
     * View.
     * </p>
     *
     * @param context the {@link VisitContext} for this tree visit.
     *
     * @param target the {@link UIComponent} to visit
     *
     * @return a {@link VisitResult} that indicates whether to continue visiting the component's subtree, skip visiting the
     * component's subtree or end the visit.
     */
    VisitResult visit(VisitContext context, UIComponent target);
}
