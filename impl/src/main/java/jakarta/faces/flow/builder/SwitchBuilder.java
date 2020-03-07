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
 * Create a switch node in the current {@link jakarta.faces.flow.Flow}.
 * </p>
 *
 * @since 2.2
 */

public abstract class SwitchBuilder implements NodeBuilder {

    /**
     * <p class="changed_added_2_2">
     * Create a switch case in the current switch.
     * </p>
     *
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract SwitchCaseBuilder switchCase();

    /**
     * <p class="changed_added_2_2">
     * Set the default outcome of the current switch.
     * </p>
     *
     * @param outcome A {@code ValueExpression} {@code String} that will be the default outcome of the switch.
     * @since 2.2
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @return the builder instance
     */

    public abstract SwitchCaseBuilder defaultOutcome(String outcome);

    /**
     * <p class="changed_added_2_2">
     * Set the default outcome of the current switch.
     * </p>
     *
     * @param outcome A {@code ValueExpression} that will be the default outcome of the switch.
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract SwitchCaseBuilder defaultOutcome(ValueExpression outcome);

    @Override
    public abstract SwitchBuilder markAsStartNode();

}
