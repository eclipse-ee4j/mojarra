/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.el;


import javax.faces.context.FacesContext;


/**
 * <p><strong>ValueBinding</strong> is an object that can be used
 * to access the property represented by an action or value binding
 * expression.  An immutable {@link ValueBinding} for a particular value binding
 * can be acquired by calling the <code>createValueBinding()</code> method of
 * the {@link javax.faces.application.Application} instance for this web
 * application.</p>
 *
 * @deprecated This has been replaced by {@link javax.el.ValueExpression}.
 */

public abstract class ValueBinding {


    /**
     * <p>Return the value of the property represented by this
     * {@link ValueBinding}, relative to the specified {@link FacesContext}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws EvaluationException if an exception is thrown while getting
     *  the value (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     * @throws PropertyNotFoundException if a specified property name
     *  does not exist, or is not readable
     *
     * @return the value of this expression
     */
    public abstract Object getValue(FacesContext context)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Set the value of the property represented by this
     * {@link ValueBinding}, relative to the specified {@link FacesContext}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param value The new value to be set
     *
     * @throws EvaluationException if an exception is thrown while setting
     *  the value (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     * @throws PropertyNotFoundException if a specified property name
     *  does not exist, or is not writeable
     */
    public abstract void setValue(FacesContext context, Object value)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Return <code>true</code> if the specified property of the specified
     * property is known to be immutable; otherwise, return
     * <code>false</code>.</p>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws EvaluationException if an exception is thrown while getting
     *  the description of the property (the thrown exception must be
     *  included as the <code>cause</code> property of this exception)
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     * @throws PropertyNotFoundException if a specified property name
     *  does not exist
     *
     * @return whether or not this expression is read only
     */
    public abstract boolean isReadOnly(FacesContext context)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Return the type of the property represented by this
     * {@link ValueBinding}, relative to the specified {@link FacesContext}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws EvaluationException if an exception is thrown while getting
     *  the description of the property (the thrown exception must be
     *  included as the <code>cause</code> property of this exception)
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     * @throws PropertyNotFoundException if a specified property name
     *  does not exist
     *
     * @return the Java type of this expression
     */
    public abstract Class getType(FacesContext context)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Return the (possibly <code>null</code>) expression String,
     * including the delimiters, from which this
     * <code>ValueBinding</code> was built.</p>
     *
     * @return the expression string
     */
    public String getExpressionString() {
	return null;
    }




}
