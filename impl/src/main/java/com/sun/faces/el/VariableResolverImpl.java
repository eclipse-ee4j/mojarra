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

package com.sun.faces.el;

import com.sun.faces.util.MessageUtils;

import jakarta.el.ELException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.VariableResolver;

/**
 * <p>
 * Concrete implementation of <code>VariableResolver</code>.
 * </p>
 */
@SuppressWarnings("deprecation")
public class VariableResolverImpl extends VariableResolver {

    private VariableResolver delegate;

    // ------------------------------------------- Methods from VariableResolver

    // Specified by jakarta.faces.el.VariableResolver.resolveVariable()
    @Override
    public Object resolveVariable(FacesContext context, String name) throws EvaluationException {

        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }
        if (name == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "name");
            throw new NullPointerException(message);
        }

        Object result;

        if (delegate != null) {
            result = delegate.resolveVariable(context, name);
        } else {
            try {
                result = context.getApplication().getELResolver().getValue(context.getELContext(), null, name);
            } catch (ELException elex) {
                throw new EvaluationException(elex);
            }
        }
        return result;

    }

    // ---------------------------------------------------------- Public Methods

    public void setDelegate(VariableResolver delegate) {

        this.delegate = delegate;

    }

    public VariableResolver getDelegate() {
        return delegate;
    }
}
