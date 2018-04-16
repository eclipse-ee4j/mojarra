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
 * <p><strong>VariableResolver</strong> represents a pluggable mechanism
 * for resolving a top-level variable reference at evaluation time.</p>
 *
 * @deprecated This has been replaced by {@link javax.el.ELResolver}
 * when operating with a <code>null</code> <code>base</code> argument.
 */

public abstract class VariableResolver {


    /**
     * <p>Resolve the specified variable name, and return the corresponding
     * object, if any; otherwise, return <code>null</code>.</p>
     *
     * @param context {@link FacesContext} against which to resolve
     *  this variable name
     * @param name Name of the variable to be resolved
     *
     * @throws EvaluationException if an exception is thrown while resolving
     *  the variable name (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws NullPointerException if <code>context</code>
     *  or <code>name</code> is <code>null</code>
     *
     * @return the result of the resolution
     */
    public abstract Object resolveVariable(FacesContext context, String name)
        throws EvaluationException;


}
