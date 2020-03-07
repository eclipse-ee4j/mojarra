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

import java.util.List;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.flow.Parameter;

/**
 * <p class="changed_added_2_2">
 * Create a method call node in the current {@link jakarta.faces.flow.Flow}.
 * </p>
 *
 * @since 2.2
 */
public abstract class MethodCallBuilder implements NodeBuilder {

    /**
     * <p class="changed_added_2_2">
     * Set the method expression of this method call node. The method signature of the argument {@code methodExpression}
     * must match the number and type of the parameters passed in the {@link #parameters} method.
     * </p>
     *
     * @param methodExpression The {@code MethodExpression} to invoke.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder expression(MethodExpression methodExpression);

    /**
     * <p class="changed_added_2_2">
     * Set the method expression of this method call node. The method signature of the argument {@code methodExpression}
     * must match the number and type of the parameters passed in the {@link #parameters} method.
     * </p>
     *
     * @param methodExpression The {@code MethodExpression} String to invoke.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder expression(String methodExpression);

    /**
     * <p class="changed_added_2_2">
     * Set the method expression of this method call node. The method signature of the argument {@code methodExpression}
     * must match the number and type of the parameters passed in the {@link #parameters} method.
     * </p>
     *
     * @param methodExpression The {@code MethodExpression} to invoke.
     * @param paramTypes the types of the parameters to the method.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder expression(String methodExpression, Class[] paramTypes);

    /**
     * <p class="changed_added_2_2">
     * Set the parameters of the method call node.
     * </p>
     *
     * @param parameters the parameters to pass to the method when it is invoked.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder parameters(List<Parameter> parameters);

    /**
     * <p class="changed_added_2_2">
     * If the method is a void method, or the method returns {@code null}, this can be used to specify what value should be
     * passed to runtime when the method returns.
     * </p>
     *
     * @param outcome A {@code ValueExpression} String representing the default outcome, only used if the method is a void
     * method or returns {@code null}.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder defaultOutcome(String outcome);

    /**
     * <p class="changed_added_2_2">
     * If the method is a void method, or the method returns {@code null}, this can be used to specify what value should be
     * passed to runtime when the method returns.
     * </p>
     *
     * @param outcome A {@code ValueExpression} representing the default outcome, only used if the method is a void method
     * or returns {@code null}.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder defaultOutcome(ValueExpression outcome);

    @Override
    public abstract MethodCallBuilder markAsStartNode();

}
