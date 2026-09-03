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

package org.glassfish.mojarra.webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.http.HttpServletResponse;

import org.glassfish.mojarra.config.MojarraContextParam;
import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.glassfish.mojarra.mock.MockRenderKit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FacesServletTestCase extends JUnitFacesTestCaseBase {

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
            renderKitFactory.addRenderKit(
                RenderKitFactory.HTML_BASIC_RENDER_KIT,
                renderKit
            );
        }
        catch (IllegalArgumentException e) {
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
        servletContext.addInitParameter(MojarraContextParam.ALLOWED_HTTP_METHODS.getName(), "GET   POST");
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
        servletContext.addInitParameter(MojarraContextParam.ALLOWED_HTTP_METHODS.getName(), "GET   POST GET  POST");
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
        servletContext.addInitParameter(MojarraContextParam.ALLOWED_HTTP_METHODS.getName(), "*");
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
        servletContext.addInitParameter(MojarraContextParam.ALLOWED_HTTP_METHODS.getName(), "* * * *");
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
        servletContext.addInitParameter(MojarraContextParam.ALLOWED_HTTP_METHODS.getName(), "GET\tPOST\tGETAAAAA");
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

    /**
     * Going through WebConfiguration rather than reading the init parameter directly is what makes the legacy com.sun.faces spelling work here, as it does for
     * every other Mojarra context parameter.
     */
    @Test
    public void testAllowedHttpMethodsHonorsLegacyPrefix() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter("com.sun.faces.allowedHttpMethods", "GET POST");
        me.init(config);

        this.sendRequest(me, "GET");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        this.sendRequest(me, "DELETE");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    /**
     * An OPTIONS request is answered by the servlet itself, with the methods it accepts, and never reaches the lifecycle. Rendering a view would hand the page
     * content to a request which did not ask for it.
     */
    @Test
    public void testOptionsIsAnsweredWithAllowHeaderAndEmptyBody() throws Exception {
        FacesServlet me = new FacesServlet();
        me.init(config);

        this.sendRequest(me, "OPTIONS");

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(0, response.getContentLength());
        assertEquals("OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT", response.getHeader("Allow"));
    }

    /**
     * The Allow header reports what was actually configured, not the full set.
     */
    @Test
    public void testOptionsAllowHeaderReflectsAllowedHttpMethods() throws Exception {
        FacesServlet me = new FacesServlet();
        servletContext.addInitParameter(MojarraContextParam.ALLOWED_HTTP_METHODS.getName(), "GET POST OPTIONS");
        me.init(config);

        this.sendRequest(me, "OPTIONS");

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("OPTIONS, GET, POST", response.getHeader("Allow"));
    }

    /**
     * The protected path check must not depend on the default locale of the deploying JVM. Under a Turkish or Azeri locale {@code i} uppercases to dotted
     * capital {@code I} (U+0130), which would let the lowercase spellings of {@code /WEB-INF} and {@code /META-INF} through a guard that uppercases with the
     * default locale.
     */
    @Test
    public void testProtectedPathsAreRejectedRegardlessOfDefaultLocale() throws Exception {
        Locale defaultLocale = Locale.getDefault();

        try {
            for (Locale locale : List.of(Locale.US, Locale.forLanguageTag("tr-TR"), Locale.forLanguageTag("az-AZ"))) {
                Locale.setDefault(locale);
                FacesServlet me = new FacesServlet();
                me.init(config);

                for (String protectedPath : List.of("/WEB-INF/web.xml", "/web-inf/web.xml", "/META-INF/MANIFEST.MF", "/meta-inf/MANIFEST.MF")) {
                    this.sendRequest(me, "GET", protectedPath);
                    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus(), locale + " must reject " + protectedPath);
                }

                this.sendRequest(me, "GET", "/view.xhtml");
                assertEquals(HttpServletResponse.SC_OK, response.getStatus(), locale + " must accept an ordinary view");
            }
        }
        finally {
            Locale.setDefault(defaultLocale);
        }
    }

    private void sendRequest(FacesServlet me, String method) throws Exception {
        this.sendRequest(me, method, "/test");
    }

    private void sendRequest(FacesServlet me, String method, String pathInfo) throws Exception {
        request.setMethod(method);
        request.setPathElements("/test", "/test", pathInfo, "");
        me.service(request, response);
    }

}
