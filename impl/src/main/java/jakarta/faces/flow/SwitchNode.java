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

package jakarta.faces.flow;

import java.util.List;

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_2">
 * Represents a switch node in the flow graph. When control passes to a switch node, for each of the {@link SwitchCase}s
 * returned from {@link #getCases}, call {@link SwitchCase#getCondition}. If the return is {@code true}, let the return
 * from {@link SwitchCase#getFromOutcome} be used to determine where to go next in the flow graph and terminate the
 * traversal. If none of the cases returned {@code true} let {@link #getDefaultOutcome} be used to determine where to go
 * next in the flow graph.
 * </p>
 *
 * @since 2.2
 */
public abstract class SwitchNode extends FlowNode {

    /**
     * <p class="changed_added_2_2">
     * Return the cases in this switch.
     * </p>
     *
     * @since 2.2
     *
     * @return the cases in this switch
     */
    public abstract List<SwitchCase> getCases();

    /**
     * <p class="changed_added_2_2">
     * Return the default outcome in this switch.
     * </p>
     *
     * @since 2.2
     *
     * @param context the {@link FacesContext} for this request
     *
     * @return the default outcome in this switch
     */
    public abstract String getDefaultOutcome(FacesContext context);

}
