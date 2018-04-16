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
 * $Id: MockObjectsTestCase.java,v 1.1 2005/10/18 17:47:59 edburns Exp $
 */



package com.sun.faces.mock;


import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIViewRoot;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * <p>Simple unit tests for Mock Objects that have behavior.</p>
 */


public class MockObjectsTestCase extends TestCase {


    // ------------------------------------------------------------ Constructors


    // Construct a new instance of this test case.
    public MockObjectsTestCase(String name) {
        super(name);
    }


    // ---------------------------------------------------- Overall Test Methods


    // Set up instance variables required by this test case.
    public void setUp() {

        // Set up Servlet API Objects
        servletContext = new MockServletContext();
        servletContext.addInitParameter("appParamName", "appParamValue");
        servletContext.setAttribute("appScopeName", "appScopeValue");
        servletContext.setAttribute("sameKey", "sameKeyAppValue");
        config = new MockServletConfig(servletContext);
        session = new MockHttpSession();
        session.setAttribute("sesScopeName", "sesScopeValue");
        session.setAttribute("sameKey", "sameKeySesValue");
        request = new MockHttpServletRequest(session);
        request.setAttribute("reqScopeName", "reqScopeValue");
        request.setAttribute("sameKey", "sameKeyReqValue");
        response = new MockHttpServletResponse();

        // Set up Faces API Objects
	FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
				 "javax.faces.mock.MockApplicationFactory");
	FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
				 "javax.faces.mock.MockRenderKitFactory");

        externalContext =
            new MockExternalContext(servletContext, request, response);
        lifecycle = new MockLifecycle();
        facesContext = new MockFacesContext(externalContext, lifecycle);
	UIViewRoot root = new UIViewRoot();
	root.setViewId("/viewId");
        facesContext.setViewRoot(root);
        ApplicationFactory applicationFactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = (MockApplication) applicationFactory.getApplication();
        facesContext.setApplication(application);
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                                          renderKit);
        } catch (IllegalArgumentException e) {
            ;
        }

        request.setAttribute("test", new TestMockBean());

    }


    // Return the tests included in this test case.
    public static Test suite() {

        return (new TestSuite(MockObjectsTestCase.class));

    }


    // Tear down instance variables required by this test case.
    public void tearDown() {

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


    // ------------------------------------------------------ Instance Variables


    // Mock object instances for our tests
    protected MockApplication         application = null;
    protected MockServletConfig       config = null;
    protected MockExternalContext     externalContext = null;
    protected MockFacesContext        facesContext = null;
    protected MockLifecycle           lifecycle = null;
    protected MockHttpServletRequest  request = null;
    protected MockHttpServletResponse response = null;
    protected MockServletContext      servletContext = null;
    protected MockHttpSession         session = null;


    // ------------------------------------------------- Individual Test Methods


    public void testMethodBindingGetTypePositive() throws Exception {

        Class argsString[] = new Class[] { String.class };
        Class argsNone[] = new Class[0];

        checkMethodBindingGetType("test.getCommand", argsNone, String.class);
        checkMethodBindingGetType("test.setCommand", argsString,  null);
        checkMethodBindingGetType("test.getInput", argsNone, String.class);
        checkMethodBindingGetType("test.setInput", argsString, null);
        checkMethodBindingGetType("test.getOutput", argsNone, String.class);
        checkMethodBindingGetType("test.setOutput", argsString, null);
        checkMethodBindingGetType("test.combine", argsNone, String.class);

    }


    public void testMethodBindingInvokePositive() throws Exception {

        TestMockBean bean = (TestMockBean) request.getAttribute("test");
        MethodBinding mb = null;
        Class argsString[] = new Class[] { String.class };
        Class argsNone[] = new Class[0];
        assertEquals("::", bean.combine());

        mb = application.createMethodBinding("test.setCommand", argsString);
        mb.invoke(facesContext, new String[] { "command" });
        assertEquals("command", bean.getCommand());
        mb = application.createMethodBinding("test.setInput", argsString);
        mb.invoke(facesContext, new String[] { "input" });
        assertEquals("input", bean.getInput());
        mb = application.createMethodBinding("test.setOutput", argsString);
        mb.invoke(facesContext, new String[] { "output" });
        assertEquals("output", bean.getOutput());
        mb = application.createMethodBinding("test.combine", null);
        assertEquals("command:input:output", bean.combine());
        assertEquals("command:input:output", mb.invoke(facesContext, null));

    }


    // Positive tests for ValueBinding.getValue()
    public void testValueBindingGetValuePositive() throws Exception {

        // Implicit search
        checkValueBindingGetValue("appScopeName", "appScopeValue");
        checkValueBindingGetValue("sesScopeName", "sesScopeValue");
        checkValueBindingGetValue("reqScopeName", "reqScopeValue");
        checkValueBindingGetValue("sameKey", "sameKeyReqValue"); // Req scope

        // Explicit scope search
        checkValueBindingGetValue("applicationScope.appScopeName",
                                  "appScopeValue");
        checkValueBindingGetValue("applicationScope.sameKey",
                                  "sameKeyAppValue");
        checkValueBindingGetValue("sessionScope.sesScopeName",
                                  "sesScopeValue");
        checkValueBindingGetValue("sessionScope.sameKey",
                                  "sameKeySesValue");
        checkValueBindingGetValue("requestScope.reqScopeName",
                                  "reqScopeValue");
        checkValueBindingGetValue("requestScope.sameKey",
                                  "sameKeyReqValue");

    }


    // Positive tests for ValueBinding.putValue()
    public void testValueBindingPutValuePositive() throws Exception {

        ValueBinding vb = null;

        // New top-level variable
        assertNull(request.getAttribute("newSimpleName"));
        assertNull(session.getAttribute("newSimpleName"));
        assertNull(servletContext.getAttribute("newSimpleName"));
        vb = application.createValueBinding("newSimpleName");
        vb.setValue(facesContext, "newSimpleValue");
        assertEquals("newSimpleValue", request.getAttribute("newSimpleName"));
        assertNull(session.getAttribute("newSimpleName"));
        assertNull(servletContext.getAttribute("newSimpleName"));

        // New request-scope variable
        assertNull(request.getAttribute("newReqName"));
        assertNull(session.getAttribute("newReqName"));
        assertNull(servletContext.getAttribute("newReqName"));
        vb = application.createValueBinding("requestScope.newReqName");
        vb.setValue(facesContext, "newReqValue");
        assertEquals("newReqValue", request.getAttribute("newReqName"));
        assertNull(session.getAttribute("newReqName"));
        assertNull(servletContext.getAttribute("newReqName"));

        // New session-scope variable
        assertNull(request.getAttribute("newSesName"));
        assertNull(session.getAttribute("newSesName"));
        assertNull(servletContext.getAttribute("newSesName"));
        vb = application.createValueBinding("sessionScope.newSesName");
        vb.setValue(facesContext, "newSesValue");
        assertNull(request.getAttribute("newSesName"));
        assertEquals("newSesValue", session.getAttribute("newSesName"));
        assertNull(servletContext.getAttribute("newSesName"));

        // New application-scope variable
        assertNull(request.getAttribute("newAppName"));
        assertNull(session.getAttribute("newAppName"));
        assertNull(servletContext.getAttribute("newAppName"));
        vb = application.createValueBinding("applicationScope.newAppName");
        vb.setValue(facesContext, "newAppValue");
        assertNull(request.getAttribute("newAppName"));
        assertNull(session.getAttribute("newAppName"));
        assertEquals("newAppValue", servletContext.getAttribute("newAppName"));

        // Old top-level variable (just created)
        assertEquals("newSimpleValue", request.getAttribute("newSimpleName"));
        assertNull(session.getAttribute("newSimpleName"));
        assertNull(servletContext.getAttribute("newSimpleName"));
        vb = application.createValueBinding("newSimpleName");
        vb.setValue(facesContext, "newerSimpleValue");
        assertEquals("newerSimpleValue", request.getAttribute("newSimpleName"));
        assertNull(session.getAttribute("newSimpleName"));
        assertNull(servletContext.getAttribute("newSimpleName"));

        // Old hierarchically found variable
        assertEquals("sameKeyAppValue", servletContext.getAttribute("sameKey"));
        assertEquals("sameKeySesValue", session.getAttribute("sameKey"));
        assertEquals("sameKeyReqValue", request.getAttribute("sameKey"));
        vb = application.createValueBinding("sameKey");
        vb.setValue(facesContext, "sameKeyNewValue");
        assertEquals("sameKeyAppValue", servletContext.getAttribute("sameKey"));
        assertEquals("sameKeySesValue", session.getAttribute("sameKey"));
        assertEquals("sameKeyNewValue", request.getAttribute("sameKey"));


    }


    // --------------------------------------------------------- Private Methods


    private void checkMethodBindingGetType(String ref, Class params[],
                                           Class expected) throws Exception {

        MethodBinding mb = application.createMethodBinding(ref, params);
        assertNotNull("MethodBinding[" + ref + "] exists", mb);
        assertEquals("MethodBinding[" + ref + "] type",
                     expected,
                     mb.getType(facesContext));

    }


    private void checkValueBindingGetValue(String ref, Object expected) {

        ValueBinding vb = application.createValueBinding(ref);
        assertNotNull("ValueBinding[" + ref + "] exists", vb);
        assertEquals("ValueBinding[" + ref + "] value",
                     expected,
                     vb.getValue(facesContext));

    }


}
