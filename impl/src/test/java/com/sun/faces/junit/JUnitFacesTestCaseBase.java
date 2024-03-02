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

package com.sun.faces.junit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockHttpSession;
import com.sun.faces.mock.MockLifecycle;
import com.sun.faces.mock.MockServletConfig;
import com.sun.faces.mock.MockServletContext;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.LifecycleFactory;

public class JUnitFacesTestCaseBase {

    protected MockApplication application = null;
    protected MockServletConfig config = null;
    protected MockHttpServletRequest request = null;
    protected MockHttpServletResponse response = null;
    protected MockServletContext servletContext = null;
    protected MockExternalContext externalContext = null;
    protected MockFacesContext facesContext = null;
    protected MockLifecycle lifecycle = null;
    protected MockHttpSession session = null;

    @BeforeEach
    public void setUp() throws Exception {
        // Set up Servlet API Objects
        servletContext = new MockServletContext();
        servletContext.addInitParameter("appParamName", "appParamValue");
        servletContext.setAttribute("appScopeName", "appScopeValue");
        config = new MockServletConfig(servletContext);
        session = new MockHttpSession();
        session.setAttribute("sesScopeName", "sesScopeValue");
        request = new MockHttpServletRequest(session);
        request.setAttribute("reqScopeName", "reqScopeValue");
        response = new MockHttpServletResponse();

        // Set up Faces API Objects
        FactoryFinder.releaseFactories();
        Method reInitializeFactoryManager = FactoryFinder.class.getDeclaredMethod("reInitializeFactoryManager", (Class<?>[]) null);
        reInitializeFactoryManager.setAccessible(true);
        reInitializeFactoryManager.invoke(null, (Object[]) null);

        // Create something to stand-in as the InitFacesContext
        new MockFacesContext(new MockExternalContext(servletContext, request, response),
                new MockLifecycle());

        FactoryFinder.setFactory(FactoryFinder.FACES_CONTEXT_FACTORY,
                "com.sun.faces.mock.MockFacesContextFactory");
        FactoryFinder.setFactory(FactoryFinder.LIFECYCLE_FACTORY,
                "com.sun.faces.mock.MockLifecycleFactory");
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
                "com.sun.faces.mock.MockApplicationFactory");
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
                "com.sun.faces.mock.MockRenderKitFactory");
        FacesContextFactory fcFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        lifecycle = (MockLifecycle) lFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        facesContext = (MockFacesContext) fcFactory.getFacesContext(servletContext, request, response, lifecycle);
        externalContext = (MockExternalContext) facesContext.getExternalContext();
        Map map = new HashMap();
        externalContext.setRequestParameterMap(map);

        ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = (MockApplication) applicationFactory.getApplication();
        facesContext.setApplication(application);

    }

    @AfterEach
    public void tearDown() throws Exception {
        FactoryFinder.releaseFactories();
        Method reInitializeFactoryManager = FactoryFinder.class.getDeclaredMethod("reInitializeFactoryManager", (Class<?>[]) null);
        reInitializeFactoryManager.setAccessible(true);
        reInitializeFactoryManager.invoke(null, (Object[]) null);

        application = null;
        config = null;
        externalContext = null;
        facesContext = null;
        lifecycle = null;
        request = null;
        response = null;
        servletContext = null;
        session = null;
    }
}
