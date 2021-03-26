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

import jakarta.el.ValueExpression;

/**
 * <p class="changed_added_2_2">
 * Represents a parameter in any of several places where parameters are needed when processing flows.
 * </p>
 *
 * @since 2.2
 */
public abstract class Parameter {

    /**
     * <p class="changed_added_2_2">
     * Return the name of the parameter
     * </p>
     *
     * @since 2.2
     *
     * @return the name of the parameter
     */

    public abstract String getName();

    /**
     * <p class="changed_added_2_2">
     * Return a {@code ValueExpression} for the value of the parameter. Depending on the context, this expression may only
     * ever be evaluated in a "get" operation.
     * </p>
     *
     * @since 2.2
     *
     * @return a {@code ValueExpression} for the value of the parameter
     */
    public abstract ValueExpression getValue();

}
