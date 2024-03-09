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

package com.sun.faces.mock;

import jakarta.el.ELContext;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class MockFacesContextFactory extends FacesContextFactory {
    public MockFacesContextFactory(FacesContextFactory oldImpl) {
        super(oldImpl);
        System.setProperty(FactoryFinder.FACES_CONTEXT_FACTORY, this.getClass().getName());
    }
    public MockFacesContextFactory() {}
    

    @Override
    public FacesContext getFacesContext(Object context, Object request,
					Object response,
					Lifecycle lifecycle) throws FacesException {
	MockFacesContext result = new MockFacesContext();

        ExternalContext externalContext =
                new MockExternalContext((ServletContext) context,
                (ServletRequest) request, (ServletResponse) response);
        result.setExternalContext(externalContext);
        ApplicationFactory applicationFactory = (ApplicationFactory)
                FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = applicationFactory.getApplication();
        result.setApplication(application);

	ELContext elContext = new MockELContext(new MockELResolver());
	elContext.putContext(FacesContext.class, result);
        result.setELContext(elContext);

        return result;
    }
}

