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
 * <p><strong>MethodBinding</strong> is an object that can be used
 * to call an arbitrary public method, on an instance that is acquired by
 * evaluatng the leading portion of a method binding expression via a
 * {@link ValueBinding}.  An immutable {@link MethodBinding} for a particular
 * method binding expression can be acquired by calling the
 * <code>createMethodBinding()</code> method of the
 * {@link javax.faces.application.Application} instance for this web
 * application.</p>
 *
 *
 * @deprecated This has been replaced by {@link javax.el.MethodExpression}.
 */

public abstract class MethodBinding {


    /**
     * <p>Return the return value (if any) resulting from a call to the
     * method identified by this method binding expression, passing it
     * the specified parameters, relative to the specified {@link FacesContext}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param params Array of parameters to be passed to the called method,
     *  or <code>null</code> for no parameters
     *
     * @throws EvaluationException if an exception is thrown
     *  by the called method (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws MethodNotFoundException if no suitable method can be found
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     *
     * @return the result of the invocation
     */
    public abstract Object invoke(FacesContext context, Object params[])
        throws EvaluationException, MethodNotFoundException;


    /**
     * <p>Return the Java class representing the return type from the
     * method identified by this method binding expression.  </p>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws MethodNotFoundException if no suitable method can be found
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     *
     * @return the type of the return value
     */
    public abstract Class getType(FacesContext context)
        throws MethodNotFoundException;

    /**
     * <p>Return the (possibly <code>null</code>) expression String,
     * with leading and trailing delimiters, from which this
     * <code>MethodBinding</code> was built.  The default implementation
     * returns <code>null</code>.</p>
     *
     * @return the expression string
     */
    public String getExpressionString() {
	return null;
    }



}
