/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates.
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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

package com.sun.faces.test.servlet30.ajaxnamespace;

import static com.sun.faces.context.ExternalContextFactoryImpl.DEFAULT_EXTERNAL_CONTEXT_KEY;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sun.faces.context.ExternalContextImpl;

public class ExternalContextFactoryImpl extends ExternalContextFactory {

    public ExternalContextFactoryImpl(ExternalContextFactory parent) {
        super(parent);
    }

    @Override
    public ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException {
        ExternalContext extContext = new ExternalContextNamespaceImpl(
                new ExternalContextImpl(
                        (ServletContext) context, (ServletRequest) request, (ServletResponse) response));

        if (request instanceof ServletRequest) {
            ((ServletRequest) request).setAttribute(DEFAULT_EXTERNAL_CONTEXT_KEY, extContext);
        }

        return extContext;
    }

}
