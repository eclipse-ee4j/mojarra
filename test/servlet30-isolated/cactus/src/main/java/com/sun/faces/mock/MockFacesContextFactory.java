/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockFacesContextFactory.java,v 1.1 2005/10/18 17:47:55 edburns Exp $
 */



package com.sun.faces.mock;

import javax.el.ELContext;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockFacesContextFactory extends FacesContextFactory {
    public MockFacesContextFactory(FacesContextFactory oldImpl) {
	System.setProperty(FactoryFinder.FACES_CONTEXT_FACTORY, 
			   this.getClass().getName());
    }
    public MockFacesContextFactory() {}
    
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
        Application application = (MockApplication) applicationFactory.getApplication();
        result.setApplication(application);
        
	ELContext elContext = new MockELContext(new MockELResolver());
	elContext.putContext(FacesContext.class, result);
        result.setELContext(elContext);
        
        return result;
    }
}

