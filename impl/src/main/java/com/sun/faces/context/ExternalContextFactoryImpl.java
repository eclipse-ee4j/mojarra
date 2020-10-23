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

import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.ExternalContextFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class ExternalContextFactoryImpl extends ExternalContextFactory {

    public static final String DEFAULT_EXTERNAL_CONTEXT_KEY = ExternalContextFactoryImpl.class.getName() + "_KEY";

    public ExternalContextFactoryImpl() {
        super(null);
    }

    // ---------------------------------------- Methods from ExternalContextFactory

    @Override
    public ExternalContext getExternalContext(Object servletContext, Object request, Object response)

            throws FacesException {

        Util.notNull("servletContext", servletContext);
        Util.notNull("request", request);
        Util.notNull("response", response);

        ExternalContext extContext = new ExternalContextImpl((ServletContext) servletContext, (ServletRequest) request, (ServletResponse) response);

        if (request instanceof ServletRequest) {
            ((ServletRequest) request).setAttribute(DEFAULT_EXTERNAL_CONTEXT_KEY, extContext);
        }

        return extContext;

    }

    // The testcase for this class is TestExternalContextFactory.java

}
