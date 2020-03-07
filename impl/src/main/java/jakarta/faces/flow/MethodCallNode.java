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

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

/**
 * <p class="changed_added_2_2">
 * Represents a method call node in the flow graph. When control passes to a method call node, its
 * {@code MethodExpression} is invoked, passing any parameters. Let <em>outcome</em> be the value determined by the
 * following algorithm. If there is a {@code null} return from the invocation, {@link #getOutcome} is called. If the
 * result is non-{@code null}, its {@code getValue()} method is called and the value is considered to be
 * <em>outcome</em>. If there is a non-{@code null} return, let it be <em>outcome</em>. Convert <em>outcome</em> to a
 * String by calling its {@code toString} method. Use <em>outcome</em> to determine the next node in the flow graph.
 * </p>
 *
 * @since 2.2
 */

public abstract class MethodCallNode extends FlowNode {

    /**
     * <p class="changed_added_2_2">
     * Return the {@code MethodExpression} to be invoked to when control passes to this node.
     * </p>
     *
     * @since 2.2
     *
     * @return the {@code MethodExpression} to be invoked to when control passes to this node
     */
    public abstract MethodExpression getMethodExpression();

    /**
     * <p class="changed_added_2_2">
     * Return the {@code outcome} to be used in the event of a {@code null} return from the method.
     * </p>
     *
     * @since 2.2
     *
     * @return the {@code outcome}
     */
    public abstract ValueExpression getOutcome();

    /**
     * <p class="changed_added_2_2">
     * Return the parameters to be passed to the method.
     * </p>
     *
     * @since 2.2
     *
     * @return the parameters to be passed to the method
     */
    public abstract List<Parameter> getParameters();

}
