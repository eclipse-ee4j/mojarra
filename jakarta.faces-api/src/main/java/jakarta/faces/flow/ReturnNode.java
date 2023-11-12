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

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_2">
 * Represents a return node in the flow graph. When control passes to a return node, its {@link #getFromOutcome} is
 * called to determine the next node in the flow graph.
 * </p>
 *
 * @since 2.2
 */
public abstract class ReturnNode extends FlowNode {

    /**
     * <p class="changed_added_2_2">
     * Return the {@code fromOutcome} to be used when control passes to this return node.
     * </p>
     *
     * @param context the {@link FacesContext} for this request.
     * @throws NullPointerException if {@code context} is {@code null}.
     * @since 2.2
     *
     * @return the {@code fromOutcome}
     */
    public abstract String getFromOutcome(FacesContext context);

}
