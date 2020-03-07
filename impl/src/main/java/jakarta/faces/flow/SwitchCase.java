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
 * Represents a case within a switch node in the flow graph. See {@link SwitchNode}.
 * </p>
 *
 * @since 2.2
 */
public abstract class SwitchCase {

    /**
     * <p class="changed_added_2_2">
     * Return the outcome to be used if {@link #getCondition} return {@code true}.
     * </p>
     *
     * @since 2.2
     *
     * @return the outcome
     */
    public abstract String getFromOutcome();

    /**
     * <p class="changed_added_2_2">
     * Return {@code true} if this case should be taken, {@code false} otherwise.
     * </p>
     *
     * @since 2.2
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @return a value indicating whether or not this condition should be taken
     */
    public abstract Boolean getCondition(FacesContext context);

}
