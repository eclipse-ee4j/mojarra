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

package com.sun.faces.facelets.el;

import java.io.Serializable;

import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.MethodNotFoundException;

/**
 * For legacy ActionSources
 *
 * @author Jacob Hookom
 * @version $Id$
 * @deprecated
 */
@Deprecated
public final class LegacyMethodBinding extends MethodBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MethodExpression m;

    public LegacyMethodBinding(MethodExpression m) {
        this.m = m;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.MethodBinding#getType(jakarta.faces.context.FacesContext)
     */
    @Override
    public Class getType(FacesContext context) throws MethodNotFoundException {
        try {
            return m.getMethodInfo(context.getELContext()).getReturnType();
        } catch (jakarta.el.MethodNotFoundException e) {
            throw new MethodNotFoundException(e.getMessage(), e.getCause());
        } catch (ELException e) {
            throw new EvaluationException(e.getMessage(), e.getCause());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.MethodBinding#invoke(jakarta.faces.context.FacesContext, java.lang.Object[])
     */
    @Override
    public Object invoke(FacesContext context, Object[] params) throws EvaluationException, MethodNotFoundException {
        try {
            return m.invoke(context.getELContext(), params);
        } catch (jakarta.el.MethodNotFoundException e) {
            throw new MethodNotFoundException(e.getMessage(), e.getCause());
        } catch (ELException e) {
            throw new EvaluationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String getExpressionString() {
        return m.getExpressionString();
    }
}
