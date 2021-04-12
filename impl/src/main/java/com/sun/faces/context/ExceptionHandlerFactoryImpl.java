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

package com.sun.faces.context;

import com.sun.faces.application.ApplicationAssociate;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;
import jakarta.faces.context.FacesContext;

/**
 * Default ExceptionHandlerFactory implementation.
 */
public class ExceptionHandlerFactoryImpl extends ExceptionHandlerFactory {

    private ApplicationAssociate associate;

    // ------------------------------------ Methods from ExceptionHandlerFactory

    public ExceptionHandlerFactoryImpl() {
        super(null);
    }

    /**
     * @see jakarta.faces.context.ExceptionHandlerFactory#getExceptionHandler()
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ApplicationAssociate myAssociate = getAssociate(fc);

        ExceptionHandler result = new AjaxNoAjaxExceptionHandler(new AjaxExceptionHandlerImpl(new ExceptionHandlerImpl(Boolean.TRUE)),
                new ExceptionHandlerImpl(myAssociate != null ? myAssociate.isErrorPagePresent() : Boolean.TRUE));
        return result;

    }

    // --------------------------------------------------------- Private Methods

    private ApplicationAssociate getAssociate(FacesContext ctx) {

        if (associate == null) {
            associate = ApplicationAssociate.getCurrentInstance();
            if (associate == null && ctx != null) {
                associate = ApplicationAssociate.getInstance(ctx.getExternalContext());
            }
        }
        return associate;

    }

}
