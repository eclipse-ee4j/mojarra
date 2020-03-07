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
 * Create a case in the current switch.
 * </p>
 *
 * @since 2.2
 */
public abstract class SwitchCaseBuilder {

    /**
     *
     * <p class="changed_added_2_2">
     * Create a new case in the current switch.
     * </p>
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract SwitchCaseBuilder switchCase();

    /**
     *
     * <p class="changed_added_2_2">
     * Set the if in the previously created switch case.
     * </p>
     *
     * @param expression the {@code ValueExpression} to be evaluated to see if this case is chosen.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract SwitchCaseBuilder condition(ValueExpression expression);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the if in the previously created switch case.
     * </p>
     *
     * @param expression the {@code ValueExpression} String to be evaluated to see if this case is chosen.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract SwitchCaseBuilder condition(String expression);

    /**
     *
     * <p class="changed_added_2_2">
     * Set the outcome in the previously created switch case.
     * </p>
     *
     * @param outcome the outcome to be returned if the condition evaluates to {@code true}.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     *
     */
    public abstract SwitchCaseBuilder fromOutcome(String outcome);

}
