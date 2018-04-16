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

// TestViewHandlerImpl.java

package com.sun.faces.application;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.StateManager;
import javax.faces.application.StateManager.SerializedView;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.cactus.WebRequest;

import com.sun.faces.application.view.FaceletViewHandlingStrategy;
import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.cactus.TestingUtil;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

/**
 * <B>TestViewHandlerImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B>
 * <P>
 *
 */

public class TestViewHandlerImpl extends JspFacesTestCase {

    public static final String TEST_URI = "/greeting.jsp";
    
    private String path = "localhost:8080";

    public static final String ignore[] = {};


    // Constructors and Initializers

    public TestViewHandlerImpl() {
        super("TestViewHandlerImpl");
        initLocalHostPath();
    }

    public TestViewHandlerImpl(String name) {
        super(name);
        initLocalHostPath();
    }

    private void initLocalHostPath() {
        String containerPort = System.getProperty("container.port");
        if (null == containerPort || 0 == containerPort.length()) {
            containerPort = "8080";
        }
        
        path = "localhost:" + containerPort;
    }
    
    public String[] getLinesToIgnore() {
        return ignore;
    }

    public boolean sendResponseToFile() {
        return true;
    }
    
    public String getExpectedOutputFilename() {
        return "TestViewHandlerImpl_correct";
    }

    public void beginRender(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/faces", TEST_URI, null);
    }

    public void beginRender2(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/somepath/greeting.jsf", null, null);
    }

    public void beginTransient(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/faces", TEST_URI, null);
        theRequest.addParameter("javax.faces.ViewState", "j_id1:j_id2");
    }

    public void beginCalculateLocaleLang(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/somepath/greeting.jsf", null, null);
        theRequest.addHeader("Accept-Language", "es-ES,tg-AF,tk-IQ,en-US");
    }

    public void beginCalculateLocaleExact(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/somepath/greeting.jsf", null, null);
        theRequest.addHeader("Accept-Language", "tg-AF,tk-IQ,ps-PS,en-US");
    }

    public void beginCalculateLocaleLowerCase(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/somepath/greeting.jsf", null, null);
        theRequest.addHeader("Accept-Language", "tg-af,tk-iq,ps-ps");
    }

    public void beginCalculateLocaleNoMatch(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/somepath/greeting.jsf", null, null);
        theRequest.addHeader("Accept-Language", "es-ES,tg-AF,tk-IQ");
    }

    public void beginCalculateLocaleFindDefault(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/somepath/greeting.jsf", null, null);
        theRequest.addHeader("Accept-Language", "en,fr");
    }

    public void beginRestoreViewNegative(WebRequest theRequest) {
        theRequest.setURL(path, "/test", "/faces", null, null);
    }

    public void testGetActionURL() {

        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

        // wrap the request so we can control the return value of getServletPath
        TestRequest testRequest = new TestRequest(request);

        ExternalContext extContext = new ExternalContextImpl(config.getServletContext(), testRequest, response);
        FacesContext facesContext = new FacesContextImpl(extContext, lifecycle);
        String contextPath = request.getContextPath();

        ViewHandlerImpl handler = new ViewHandlerImpl();

        // if getServletPath() returns "" then the viewId path returned should
        // be the same as what was passed, prefixed by the context path.
        // testRequest.setServletPath("");
        // testRequest.setAttribute("com.sun.faces.INVOCATION_PATH", null);
        // String path = handler.getActionURL(facesContext, "/test.jsp");
        // System.out.println("VIEW ID PATH 1: " + path);
        // assertEquals(contextPath + "/test.jsp", path);

        // if getServletPath() returns a path prefix, then the viewId path
        // returned must have that path prefixed.
        testRequest.setServletPath("/faces");
        testRequest.setPathInfo("/path/test.jsp");
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        String path = handler.getActionURL(facesContext, "/path/test.jsp");
        System.out.println("VIEW ID PATH 2: " + path);
        String expected = contextPath + "/faces/path/test.jsp";
        assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);

        // if getServletPath() returns a path indicating extension mapping
        // and the viewId passed has no extension, append the extension
        // to the provided viewId
        testRequest.setServletPath("/path/firstRequest.jsf");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/test");
        System.out.println("VIEW ID PATH 3: " + path);
        expected = contextPath + "/path/test";
        assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);

        // if getServletPath() returns a path indicating extension mapping
        // and the viewId passed has an extension, replace the extension with
        // the extension defined in the deployment descriptor
        testRequest.setServletPath("/path/firstRequest.jsf");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/t.est.jsp");
        System.out.println("VIEW ID PATH 4: " + path);
        expected = contextPath + "/path/t.est.jsf";
        // assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);

        // if path info is null, the impl must check to see if
        // there is an exact match on the servlet path, if so, return
        // the servlet path
        // testRequest.setServletPath("/faces");
        // testRequest.setPathInfo(null);
        // RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        // path = handler.getActionURL(facesContext, "/path/t.est");
        System.out.println("VIEW ID PATH 5: " + path);
        System.out.println("End of action test " + path);
        // expected = contextPath + "/faces/path/t.est";
        // assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);

    }

    public void testFullAndPartialStateConfiguration() throws Exception {

        WebConfiguration webConfig = WebConfiguration.getInstance();
        webConfig.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving, true);
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        ApplicationStateInfo info = new ApplicationStateInfo();
        TestingUtil.setPrivateField("applicationStateInfo", ApplicationAssociate.class, associate, info);
        FaceletViewHandlingStrategy strat = new FaceletViewHandlingStrategy();
        getFacesContext().getViewRoot().setViewId("/index.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));
        getFacesContext().getViewRoot().setViewId("/index2.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index2.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

        // ---------------------------------------------

        webConfig.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving, false);
        info = new ApplicationStateInfo();
        TestingUtil.setPrivateField("applicationStateInfo", ApplicationAssociate.class, associate, info);

        getFacesContext().getViewRoot().setViewId("/index.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));
        getFacesContext().getViewRoot().setViewId("/index2.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index2.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

        // ---------------------------------------------

        webConfig.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving, true);
        webConfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.FullStateSavingViewIds, "/index.xhtml");
        info = new ApplicationStateInfo();
        TestingUtil.setPrivateField("applicationStateInfo", ApplicationAssociate.class, associate, info);

        getFacesContext().getViewRoot().setViewId("/index.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

        getFacesContext().getViewRoot().setViewId("/index2.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index2.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

        // ---------------------------------------------

        webConfig.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving, true);
        webConfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.FullStateSavingViewIds,
                "/index.xhtml,/index2.xhtml");
        info = new ApplicationStateInfo();
        TestingUtil.setPrivateField("applicationStateInfo", ApplicationAssociate.class, associate, info);

        getFacesContext().getViewRoot().setViewId("/index.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

        getFacesContext().getViewRoot().setViewId("/index2.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index2.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

        getFacesContext().getViewRoot().setViewId("/index3.xhtml");
        assertNotNull(strat.getStateManagementStrategy(getFacesContext(), "/index3.xhtml"));
        assertNotNull(getFacesContext().getAttributes().remove("com.sun.faces.context.StateContext_KEY"));

    }

    public void testGetActionURLExceptions() throws Exception {
        boolean exceptionThrown = false;
        ViewHandler handler = Util.getViewHandler(getFacesContext());
        try {
            handler.getActionURL(null, "/test.jsp");
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            handler.getActionURL(getFacesContext(), null);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            handler.getActionURL(getFacesContext(), "test.jsp");
        } catch (IllegalArgumentException iae) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testGetResourceURL() throws Exception {

        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        ExternalContext extContext = new ExternalContextImpl(config.getServletContext(), request, response);
        FacesContext context = new FacesContextImpl(extContext, lifecycle);

        // Validate correct calculations
        assertEquals(request.getContextPath() + "/index.jsp", Util.getViewHandler(getFacesContext()).getResourceURL(context, "/index.jsp"));
        assertEquals("index.jsp", Util.getViewHandler(getFacesContext()).getResourceURL(context, "index.jsp"));

    }

    public void testRender() {
//        getFacesContext().setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
//        UIViewRoot newView = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), TEST_URI);
//        // newView.setViewId(TEST_URI);
//        getFacesContext().setViewRoot(newView);
//
//        try {
//            ViewHandler viewHandler = Util.getViewHandler(getFacesContext());
//            viewHandler.renderView(getFacesContext(), getFacesContext().getViewRoot());
//        } catch (IOException e) {
//            System.out.println("ViewHandler IOException:" + e);
//        } catch (FacesException fe) {
//            System.out.println("ViewHandler FacesException: " + fe);
//        }
//
//        assertTrue(!(getFacesContext().getRenderResponse()) && !(getFacesContext().getResponseComplete()));
//
//        assertTrue(verifyExpectedOutput());
    }

    /*
     * public void testRender2() { // Change the viewID to end with .jsf and make sure that // the implementation changes
     * .jsf to .jsp and properly dispatches // the message. UIViewRoot newView =
     * Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null); newView.setViewId(TEST_URI);
     * getFacesContext().setViewRoot(newView);
     * 
     * newView.setViewId("/faces/greeting.jsf"); getFacesContext().setViewRoot(newView); try { ViewHandler viewHandler =
     * Util.getViewHandler(getFacesContext()); viewHandler.renderView(getFacesContext(), getFacesContext().getViewRoot()); }
     * catch (IOException ioe) { System.out.println("ViewHandler IOException: " + ioe); } catch (FacesException fe) {
     * System.out.println("ViewHandler FacesException: " + fe); }
     * 
     * assertTrue(!(getFacesContext().getRenderResponse()) && !(getFacesContext().getResponseComplete()));
     * 
     * assertTrue(verifyExpectedOutput()); }
     */

    public void testCalculateLocaleLang() {
        System.out.println("Testing calculateLocale - Language Match case");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(Locale.ENGLISH));
    }

    public void testCalculateLocaleExact() {
        System.out.println("Testing calculateLocale - Exact Match case ");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(new Locale("ps", "PS")));
    }

    public void testCalculateLocaleNoMatch() {
        System.out.println("Testing calculateLocale - No Match case");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(Locale.US));
    }

    public void testCalculateLocaleFindDefault() {
        System.out.println("Testing calculateLocale - find default");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertEquals(Locale.ENGLISH.toString(), locale.toString());
    }

    public void testCalculateLocaleLowerCase() {
        System.out.println("Testing calculateLocale - case sensitivity");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(new Locale("ps", "PS")));
    }

    public void testTransient() {

        // precreate tree and set it in session and make sure the tree is
        // restored from session.
        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        root.setViewId(TEST_URI);

        UIForm basicForm = new UIForm();
        basicForm.setId("basicForm");
        UIInput userName = new UIInput();

        userName.setId("userName");
        userName.setTransient(true);
        root.getChildren().add(basicForm);
        basicForm.getChildren().add(userName);

        UIPanel panel1 = new UIPanel();
        panel1.setId("panel1");
        basicForm.getChildren().add(panel1);

        UIInput userName1 = new UIInput();
        userName1.setId("userName1");
        userName1.setTransient(true);
        panel1.getChildren().add(userName1);

        UIInput userName2 = new UIInput();
        userName2.setId("userName2");
        panel1.getChildren().add(userName2);

        UIInput userName3 = new UIInput();
        userName3.setTransient(true);
        panel1.getFacets().put("userName3", userName3);

        UIInput userName4 = new UIInput();
        panel1.getFacets().put("userName4", userName4);

        HttpSession session = (HttpSession) getFacesContext().getExternalContext().getSession(false);
        session.setAttribute(TEST_URI, root);

        getFacesContext().setViewRoot(root);

        StateManager stateManager = getFacesContext().getApplication().getStateManager();
        SerializedView viewState = stateManager.saveSerializedView(getFacesContext());
        assertTrue(null != viewState);
        try {
            RenderKit curKit = RenderKitUtils.getCurrentRenderKit(getFacesContext());
            StringWriter writer = new StringWriter();
            ResponseWriter responseWriter = curKit.createResponseWriter(writer, "text/html", "ISO-8859-1");
            getFacesContext().setResponseWriter(responseWriter);
            stateManager.writeState(getFacesContext(), viewState);
            root = stateManager.restoreView(getFacesContext(), TEST_URI, RenderKitFactory.HTML_BASIC_RENDER_KIT);
            getFacesContext().setViewRoot(root);
        } catch (Throwable ioe) {
            ioe.printStackTrace();
            fail();
        }

        // make sure that the transient property is not persisted.
        basicForm = (UIForm) (getFacesContext().getViewRoot()).findComponent("basicForm");
        assertTrue(basicForm != null);

        userName = (UIInput) basicForm.findComponent("userName");
        assertTrue(userName == null);

        panel1 = (UIPanel) basicForm.findComponent("panel1");
        assertTrue(panel1 != null);

        userName1 = (UIInput) panel1.findComponent("userName1");
        assertTrue(userName1 == null);

        userName2 = (UIInput) panel1.findComponent("userName2");
        assertTrue(userName2 != null);

        // make sure facets work correctly when marked transient.
        Map facetList = panel1.getFacets();
        assertTrue(!(facetList.containsKey("userName3")));
        assertTrue(facetList.containsKey("userName4"));
    }

    public void testRestoreViewNegative() throws Exception {

        // make sure the returned view is null if the viewId is the same
        // as the servlet mapping.
        // assertNull(Util.getViewHandler(getFacesContext()).restoreView(getFacesContext(),
        // "/faces"));
    }

    private class TestRequest extends HttpServletRequestWrapper {

        String servletPath;
        String pathInfo;

        public TestRequest(HttpServletRequest request) {
            super(request);
        }

        public String getServletPath() {
            return servletPath;
        }

        public void setServletPath(String servletPath) {
            this.servletPath = servletPath;
        }

        public String getPathInfo() {
            return pathInfo;
        }

        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    }

}
