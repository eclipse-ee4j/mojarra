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

package jakarta.faces.webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.servlet.http.HttpServletResponse;

public class FacesServletTestCase extends JUnitFacesTestCaseBase {

    // this is private in FacesServlet to not break backwards compatibility
    private static final String ALLOWED_HTTP_METHODS_ATTR_COPY
            = "com.sun.faces.allowedHttpMethods";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        // Set up Servlet API Objects
        servletContext.addInitParameter("appParamName", "appParamValue");
        servletContext.setAttribute("appScopeName", "appScopeValue");
        session.setAttribute("sesScopeName", "sesScopeValue");
        request.setAttribute("reqScopeName", "reqScopeValue");

        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setViewId("/viewId");
        facesContext.setViewRoot(root);
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                    renderKit);
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testPositiveInitWithNoContextParams() throws Exception {
        FacesServlet me = new FacesServlet();
        me.init(config);
        this.sendRequest(me, "OPTIONS");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "HEAD");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "POST");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "PUT");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "TRACE");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "CONNECT");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void testPositiveInitWithContextParamsOfKnownHttpMethods() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter(ALLOWED_HTTP_METHODS_ATTR_COPY, "GET   POST");
        me.init(config);
        this.sendRequest(me, "OPTIONS");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "HEAD");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "POST");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "PUT");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "TRACE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "CONNECT");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testNegativeInitWithContextParamsOfKnownHttpMethods() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter(ALLOWED_HTTP_METHODS_ATTR_COPY, "GET   POST GET  POST");
        me.init(config);
        this.sendRequest(me, "OPTIONS");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "HEAD");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "POST");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "PUT");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "TRACE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "CONNECT");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testPositiveInitWithContextParamsOfWildcardHttpMethods() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter(ALLOWED_HTTP_METHODS_ATTR_COPY, "*");
        me.init(config);
        this.sendRequest(me, "OPTIONS");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "HEAD");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "POST");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "PUT");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "TRACE");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "CONNECT");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "BOO_YA");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void testNegativeInitWithContextParamsOfWildcardHttpMethods() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter(ALLOWED_HTTP_METHODS_ATTR_COPY, "* * * *");
        me.init(config);
        this.sendRequest(me, "OPTIONS");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "HEAD");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "POST");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "PUT");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "TRACE");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "CONNECT");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "BOO_YA");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void testPositiveInitWithContextParamsOfUnknownAndKnownHttpMethods() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter(ALLOWED_HTTP_METHODS_ATTR_COPY, "GET\tPOST\tGETAAAAA");
        me.init(config);
        this.sendRequest(me, "OPTIONS");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "HEAD");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "POST");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "PUT");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "TRACE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "CONNECT");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "BOO_YA");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        this.sendRequest(me, "GETAAAAA");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    private void sendRequest(FacesServlet me, String method) throws Exception {
        request.setMethod(method);
        request.setPathElements("/test", "/test", "/test", "");
        me.service(request, response);
    }
}
