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

package com.sun.faces.test.servlet30.initrequestparametermap;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;

public class CustomExternalContextFactory extends ExternalContextFactory {

    private ExternalContextFactory delegate;

    public CustomExternalContextFactory(ExternalContextFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public ExternalContext getExternalContext(Object o, Object o2, Object o3) throws FacesException {
        ExternalContext externalContext = new CustomExternalContext(delegate.getExternalContext(o, o2, o3));
        externalContext.getRequestParameterMap().get("request-type");
        return externalContext;
    }

    @Override
    public ExternalContextFactory getWrapped() {
        return delegate;
    }
}
