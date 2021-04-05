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

package com.sun.faces.test.servlet31.resourcehandler;

import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.CompressableMimeTypes;
import static com.sun.faces.util.Util.getCurrentLoader;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;
import static java.util.Locale.US;
import static java.util.TimeZone.getTimeZone;
import static javax.faces.FactoryFinder.LIFECYCLE_FACTORY;
import static javax.faces.application.ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE;
import static javax.faces.lifecycle.LifecycleFactory.DEFAULT_LIFECYCLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ResourceWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.resource.ResourceHandlerImpl;
import com.sun.faces.application.resource.ResourceManager;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;


/**
 * This servlet does a kind of in-container unit tests for {@link ResourceHandlerImpl}.
 * <p>
 * These tests have been migrated from cactus based tests and are mostly Mojarra specific.
 * Some of these should be migrated to regular univeral JSF tests.
 * <p>
 * By default a single request runs all tests. If a "false" is written to the response a
 * test has failed. Individual tests can be run via the <code>test</code> request parameter
 * and the name of the method, e.g. <code>/testHandleResourceRequest?test=testAjaxIsAvailable</code>
 * 
 */
@WebServlet("/testHandleResourceRequest")
public class ResourceHandlerTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /* HTTP Date format required by the HTTP/1.1 RFC */
    private static final String RFC1123_DATE_PATTERN =
          "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Run the tests, each one in a newly set up faces environment
        
        run("testAjaxIsAvailable", this::testAjaxIsAvailable, request, response);

        run("testAjaxCompression", this::testAjaxCompression, request, response);
        run("testCreateResource", this::testCreateResource, request, response);
        run("testIsResourceRequestPrefixMapped", this::testIsResourceRequestPrefixMapped, request, response);
        run("testIsResourceRequestExtensionMapped", this::testIsResourceRequestExtensionMapped, request, response);

        run("testHandleResourceRequestExcludesPrefixMapped1", this::testHandleResourceRequestExcludesPrefixMapped1, request, response);
        run("testHandleResourceRequestExcludesPrefixMapped2", this::testHandleResourceRequestExcludesPrefixMapped2, request, response);
        run("testHandleResourceRequestExcludesPrefixMapped3", this::testHandleResourceRequestExcludesPrefixMapped3, request, response);
        run("testHandleResourceRequestExcludesPrefixMapped4", this::testHandleResourceRequestExcludesPrefixMapped4, request, response);

        run("testHandleResourceRequestExcludeExtensionMapped1", this::testHandleResourceRequestExcludeExtensionMapped1, request, response);
        run("testHandleResourceRequestExcludeExtensionMapped2", this::testHandleResourceRequestExcludeExtensionMapped2, request, response);
        run("testHandleResourceRequestExcludeExtensionMapped3", this::testHandleResourceRequestExcludeExtensionMapped3, request, response);
        run("testHandleResourceRequestExcludeExtensionMapped4", this::testHandleResourceRequestExcludeExtensionMapped4, request, response);

        run("testUserSpecifiedResourceExclude1", this::testUserSpecifiedResourceExclude1, request, response);
        run("testUserSpecifiedResourceExclude2", this::testUserSpecifiedResourceExclude2, request, response);
        run("testUserSpecifiedResourceExclude3", this::testUserSpecifiedResourceExclude3, request, response);
        run("testLibraryExistsNegative", this::testLibraryExistsNegative, request, response);
        
        run("testHandleResourceRequest1", this::testHandleResourceRequest1, request, response);
        run("testHandleResourceRequest2", this::testHandleResourceRequest2, request, response);
        run("testHandleResourceRequest3", this::testHandleResourceRequest3, request, response);
        run("testHandleResourceRequest4", this::testHandleResourceRequest4, request, response);
        run("testHandleResourceRequest5", this::testHandleResourceRequest5, request, response);
        run("testHandleResourceRequest6", this::testHandleResourceRequest6, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest7", this::testHandleResourceRequest7, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest8", this::testHandleResourceRequest8, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest9", this::testHandleResourceRequest9, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest10", this::testHandleResourceRequest10, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest11", this::testHandleResourceRequest11, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest12", this::testHandleResourceRequest12, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest13", this::testHandleResourceRequest13, request, response, CompressableMimeTypes, "image/gif");
        run("testHandleResourceRequest14", this::testHandleResourceRequest14, request, response);
        run("testHandleResourceRequest15", this::testHandleResourceRequest15, request, response);
        run("testHandleResourceRequest16", this::testHandleResourceRequest16, request, response);
    }
    
    
    
    // ### Test methods
    
    
    private void testAjaxIsAvailable(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/jsf.js.faces");

        response.getWriter().println(handler != null);
        response.getWriter().println(handler instanceof ResourceHandlerImpl);
        response.getWriter().println(handler.createResource("jsf.js", "jakarta.faces") != null);
    }
    
    private void testAjaxCompression(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/jsf.js.faces");
        
        Resource resource = handler.createResource("jsf-uncompressed.js", "jakarta.faces");

        InputStream stream = resource.getInputStream();

        int origSize = getBytes(stream).length;

        resource = handler.createResource("jsf.js", "jakarta.faces");

        stream = resource.getInputStream();

        int compSize = getBytes(stream).length;

        // If we're not getting 30% compression, something's gone horribly wrong.
        response.getWriter().println(origSize * 0.7 > compSize);
    }
    
    private void testCreateResource(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/jsf.js.faces");
        
        response.getWriter().println(handler != null);
        response.getWriter().println(handler instanceof ResourceHandlerImpl);

        Resource resource = handler.createResource("duke-nv.gif");
        response.getWriter().println(resource != null);
        response.getWriter().println(resource.getLibraryName() == null);
        response.getWriter().println("duke-nv.gif".equals(resource.getResourceName()));
        response.getWriter().println("image/gif".equals(resource.getContentType()));

        resource = handler.createResource("duke-nv.gif", "nvLibrary");
        response.getWriter().println(resource != null);
        response.getWriter().println("nvLibrary".equals(resource.getLibraryName()));
        response.getWriter().println("duke-nv.gif".equals(resource.getResourceName()));
        response.getWriter().println("image/gif".equals(resource.getContentType()));

        resource = handler.createResource("duke-nv.gif", "nvLibrary", "text/xml");
        response.getWriter().println(resource != null);
        response.getWriter().println("nvLibrary".equals(resource.getLibraryName()));
        response.getWriter().println("duke-nv.gif".equals(resource.getResourceName()));
        response.getWriter().println("text/xml".equals(resource.getContentType()));

        resource = handler.createResource("duke-nv.gif", "nvLibrary", null);
        response.getWriter().println(resource != null);
        response.getWriter().println("nvLibrary".equals(resource.getLibraryName()));
        response.getWriter().println("duke-nv.gif".equals(resource.getResourceName()));
        response.getWriter().println("image/gif".equals(resource.getContentType()));

        resource = handler.createResource("foo.jpg");
        response.getWriter().println(resource == null);

        resource = handler.createResource("duke-nv.gif", "nonExistant");
        response.getWriter().println(resource == null);
    }
    
    private void testIsResourceRequestPrefixMapped(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/duke-nv.gif");
        
        response.getWriter().println(handler.isResourceRequest(facesContext));
    }
    
    private void testIsResourceRequestExtensionMapped(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");

        response.getWriter().println(handler.isResourceRequest(facesContext));
    }
    
    private void testHandleResourceRequestExcludesPrefixMapped1(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/test.jsp");
        
        handler.handleResourceRequest(facesContext);
        
        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    private void testHandleResourceRequestExcludesPrefixMapped2(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/test.properties");
        
        handler.handleResourceRequest(facesContext);
        
        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    private void testHandleResourceRequestExcludesPrefixMapped3(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/test.xhtml");
        
        handler.handleResourceRequest(facesContext);
        
        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    private void testHandleResourceRequestExcludesPrefixMapped4(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/test.class");

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    private void testHandleResourceRequestExcludeExtensionMapped1(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/duke-nv.jsp.faces");

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }

    private void testHandleResourceRequestExcludeExtensionMapped2(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/duke-nv.properties.faces");

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    private void testHandleResourceRequestExcludeExtensionMapped3(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/duke-nv.xhtml.faces");

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    private void testHandleResourceRequestExcludeExtensionMapped4(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/faces/");
        testRequest.setPathInfo("/jakarta.faces.resource/duke-nv.class.faces");

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //  The next 5 tests validate a user specified exclude.
    //  In this case, .gif is excluded as a valid resource request.
    //  This should cause the default exclusions of .jsp, .class, .xhtml, and
    //  .properties to now be considered valid
    ////////////////////////////////////////////////////////////////////////////
    
    
    private void testUserSpecifiedResourceExclude1(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");

        // documenting this once - this is hack in order to support dynamic init
        // parameters. Unfortunately, the config object (which one can obtain
        // the ServletContextWrapper from isn't available at the time the
        // 'begin' methods are invoked. So instead, leverage the knowledge that
        // the init parameters are checked when the ResourceHandlerImpl is
        // constructed and set the init parameters in the context before constructing.
        WebConfiguration webconfig = WebConfiguration.getInstance(facesContext.getExternalContext());
        webconfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.ResourceExcludes, ".gif");
        
        ApplicationAssociate associate = ApplicationAssociate.getInstance(facesContext.getExternalContext());
        Application app = facesContext.getApplication();
        ResourceHandler oldResourceHandler = app.getResourceHandler();
        associate.setResourceManager(new ResourceManager(associate.getResourceCache()));
        handler = new ResourceHandlerImpl();
        app.setResourceHandler(handler);

        try {
            handler.handleResourceRequest(facesContext);

            response.getWriter().println(wrapper.getStatus() == 404);

        } finally {
            app.setResourceHandler(oldResourceHandler);
            webconfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.ResourceExcludes, RESOURCE_EXCLUDES_DEFAULT_VALUE);
        }
    }
    
    private void testUserSpecifiedResourceExclude2(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/com.sun.faces.application.ApplicationImpl.class.faces");

        WebConfiguration webconfig = WebConfiguration.getInstance(facesContext.getExternalContext());
        webconfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.ResourceExcludes, ".gif");

        try {
            // TODO: isResourceRequest doesn't seem to take exclusions into account.
            facesContext.getApplication().getResourceHandler().isResourceRequest(facesContext);

            response.getWriter().println(facesContext.getApplication().getResourceHandler().isResourceRequest(facesContext));

        } finally {
            webconfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.ResourceExcludes, RESOURCE_EXCLUDES_DEFAULT_VALUE);
        }
    }
    
    private void testUserSpecifiedResourceExclude3(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/com.sun.faces.LogStrings.properties.faces");

        WebConfiguration webconfig = WebConfiguration.getInstance(facesContext.getExternalContext());
        webconfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.ResourceExcludes, ".gif");

        try {
            facesContext.getApplication().getResourceHandler().isResourceRequest(facesContext);

            response.getWriter().println(facesContext.getApplication().getResourceHandler().isResourceRequest(facesContext));

        } finally {
            webconfig.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.ResourceExcludes, RESOURCE_EXCLUDES_DEFAULT_VALUE);
        }
    }
    
    private void testLibraryExistsNegative(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");

        response.getWriter().println(!facesContext.getApplication().getResourceHandler().libraryExists("oeunhtnhtnhhnhh"));
    }
    
    //==========================================================================
    // Validate a resource streamed from the docroot of a webapp
    //
    private void testHandleResourceRequest1(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        
        byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"));
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();
        
        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
    }
    
    //==========================================================================
    // Validate a resource streamed from a JAR
    //
    private void testHandleResourceRequest2(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getParameterMap().put("ln", new String[] { "nvLibrary-jar" });
        
        byte[] control = getBytes(getCurrentLoader(this).getResource("META-INF/resources/nvLibrary-jar/duke-nv.gif"));
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();
        
        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
    }
    
    //==========================================================================
    // Validate a 304 is returned when a request contains the If-Modified-Since
    // request header and the resource hasn't changed on the server side.
    //
    private void testHandleResourceRequest3(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");

        long currentTime = System.currentTimeMillis(), threeHoursAgo = currentTime - 10800000L;

        SimpleDateFormat format = new SimpleDateFormat(RFC1123_DATE_PATTERN, US);
        format.setTimeZone(GMT);

        testRequest.getHeadersMap().put("If-Modified-Since", new String[] { format.format(new Date(currentTime)) });

        final ExternalContext externalContext = facesContext.getExternalContext();
        final FacesContext oldFacesContext = facesContext;

        try {
            facesContext = new FacesContextWrapper(facesContext) {
                {FacesContext.setCurrentInstance(this);}

                public ExternalContext getExternalContext() {
                    return new ExternalContextWrapper(externalContext) {
                        public URL getResource(String path) throws java.net.MalformedURLException {
                            URL url = getWrapped().getResource(path);
                            return new URL(url, url.toExternalForm(), new URLStreamHandler() {
                                @Override
                                protected URLConnection openConnection(URL u) throws IOException {
                                    return new URLConnectionWrapper(url.openConnection()) {
                                        public long getLastModified() {
                                            return threeHoursAgo;
                                        };
                                    };
                                }
                            });
                        };
                    };
                };
            };

            facesContext.getApplication()
                        .getResourceHandler()
                        .handleResourceRequest(facesContext);

            response.getWriter().println(wrapper.getStatus() == 304);
        } finally {
            facesContext = new FacesContextWrapper(oldFacesContext) {
                {FacesContext.setCurrentInstance(this);}
            };
        }
    }
    
    private void testHandleResourceRequest4(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-v.gif.faces");
        testRequest.getParameterMap().put("ln", new String[] { "nvLibrary" });

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    //==========================================================================
    // Validate a 404 is returned when a request for an excluded resource is made
    //
    private void testHandleResourceRequest5(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.class.faces");
        testRequest.getParameterMap().put("ln", new String[] { "nvLibrary" });

        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);

        response.getWriter().println(wrapper.getStatus() == 404);
    }

    //==========================================================================
    // Validate a resource streamed from the docroot of a webapp is compressed
    //
    private void testHandleResourceRequest6(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getHeadersMap().put("accept-encoding", new String[] { "deflate", "gzip" });
       
        byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"), true);
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();

        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
        response.getWriter().println(wrapper.containsHeader("content-encoding"));
    }

    //==========================================================================
    // Validate a resource streamed from a JAR is compressed
    //
    private void testHandleResourceRequest7(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getParameterMap().put("ln", new String[] { "nvLibrary-jar" });
        testRequest.getHeadersMap().put("accept-encoding", new String[] { "gzip,deflate" });

        byte[] control = getBytes(getCurrentLoader(this).getResource("META-INF/resources/nvLibrary-jar/duke-nv.gif"), true);
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();

        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
        response.getWriter().println(wrapper.containsHeader("content-encoding"));
    }
    
    //==========================================================================
    // Validate a resource streamed from the docroot of a webapp isn't compressed
    // when the client doesn't send the accept-encoding request header
    //
    private void testHandleResourceRequest8(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
         testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
         
         byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"));
         handler.handleResourceRequest(facesContext);
         byte[] test = wrapper.getBytes();
         
         response.getWriter().println(Arrays.equals(control, test));
         response.getWriter().println(wrapper.containsHeader("content-length"));
         response.getWriter().println(wrapper.containsHeader("last-modified"));
         response.getWriter().println(wrapper.containsHeader("cache-control"));
         response.getWriter().println(wrapper.containsHeader("etag"));
         response.getWriter().println(!wrapper.containsHeader("content-encoding"));
    }
    
    private void testHandleResourceRequest9(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
         testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
         testRequest.getParameterMap().put("ln", new String[] {"nvLibrary-jar"});
         
         byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/nvLibrary/duke-nv.gif"));
         handler.handleResourceRequest(facesContext);
         byte[] test = wrapper.getBytes();
         
         response.getWriter().println(Arrays.equals(control, test));
         response.getWriter().println(wrapper.containsHeader("content-length"));
         response.getWriter().println(wrapper.containsHeader("last-modified"));
         response.getWriter().println(wrapper.containsHeader("cache-control"));
         response.getWriter().println(wrapper.containsHeader("etag"));
         response.getWriter().println(!wrapper.containsHeader("content-encoding"));
    }
    
    
    //==========================================================================
    // Validate an accept-encoding of gzip;q=0 means non-compressed content
    // is sent to the user-agent
    //
    private void testHandleResourceRequest10(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getParameterMap().put("accept-encoding", new String[] {"gzip;q=0, deflate"});
        
        byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"), false);
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();
        
        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
        response.getWriter().println(!wrapper.containsHeader("content-encoding"));
    }
         
    
    
    //==========================================================================
    // Validate an accept-encoding of that doesn't include gzip, and includes
    // *;q=0 will not send compressed content to the user-agent
    // is sent to the user-agent
    //
    private void testHandleResourceRequest11(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getParameterMap().put("accept-encoding", new String[] { "deflate", "*;q=0"});
        
        byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"), false);
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();
        
       
        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
        response.getWriter().println(!wrapper.containsHeader("content-encoding"));
    }
    
    //==========================================================================
    // Validate an accept-encoding of that doesn't include gzip, and includes
    // * will send compressed content
    //
    private void testHandleResourceRequest12(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getHeadersMap().put("accept-encoding", new String[] { "identity;q=1.0", "*;q=0.5", "deflate;q=1.0"});
        
        byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"), true);
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();

        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
        response.getWriter().println(wrapper.containsHeader("content-encoding"));
    }
    
    private void testHandleResourceRequest13(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getHeadersMap().put("accept-encoding", new String[] { "identity;q=0.5, deflate;q=1.0" });
        
        byte[] control = getBytes(facesContext.getExternalContext().getResource("/resources/duke-nv.gif"), false);
        handler.handleResourceRequest(facesContext);
        byte[] test = wrapper.getBytes();

        response.getWriter().println(Arrays.equals(control, test));
        response.getWriter().println(wrapper.containsHeader("content-length"));
        response.getWriter().println(wrapper.containsHeader("last-modified"));
        response.getWriter().println(wrapper.containsHeader("cache-control"));
        response.getWriter().println(wrapper.containsHeader("etag"));
        response.getWriter().println(!wrapper.containsHeader("content-encoding"));
    }
    
    //==========================================================================
    // Validate the fix for issue 1162.
    //
    private void testHandleResourceRequest14(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/web.xml.faces");
        testRequest.getParameterMap().put("ln", new String[] { "../WEB-INF" });
        
        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);
                
        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    //==========================================================================
    // Validate the fix for issue 1162.
    //
    private void testHandleResourceRequest15(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/web.xml.faces");
        testRequest.getParameterMap().put("ln", new String[] { "nvLibrary/../../WEB-INF" });
        
        facesContext.getApplication()
                    .getResourceHandler()
                    .handleResourceRequest(facesContext);
                
        response.getWriter().println(wrapper.getStatus() == 404);
    }
    
    //==========================================================================
    // Validate the fix for issue ????.
    //
    private void testHandleResourceRequest16(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException {
        testRequest.setServletPath("/jakarta.faces.resource/duke-nv.gif.faces");
        testRequest.getHeadersMap().put("accept-encoding", new String[] { "gzip;q=0, deflate" });
        
        class TestInputStreamContainingZeroes extends InputStream {
            private boolean open = true;

            @Override
            public int read() throws IOException {
                return 0;
            }
            @Override
            public void close() throws IOException {
                open = false;
            }
            public boolean isOpen() {
                return open;
            }
        };
        final TestInputStreamContainingZeroes resourceInputStream = new TestInputStreamContainingZeroes();
        
        ResourceHandler resourceHandler = new ResourceHandlerWrapper(handler) {
            @Override
            public Resource createResource(String resourceName, String libraryName) {
                return new ResourceWrapper(super.createResource(resourceName, libraryName)) {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return resourceInputStream;
                    }
                };
            }
        };

        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(wrapper) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        throw new IOException("Simulation of broken pipe or connection reset by peer");
                    }
                    @Override
                    public void close() throws IOException {
                        throw new IOException("Simulation of broken pipe or connection reset by peer");
                    }

                    @Override
                    public boolean isReady() {
                        throw new UnsupportedOperationException("Not supported");
                    }

                    @Override
                    public void setWriteListener(WriteListener wl) {
                        throw new UnsupportedOperationException("Not supported");
                    }
                };
            }
        };
        
        facesContext.getExternalContext().setResponse(wrappedResponse);
        facesContext.getApplication().setResourceHandler(resourceHandler);
        
        boolean exceptionOccurred = false;
        try {
            resourceHandler.handleResourceRequest(facesContext);
        } catch (IOException e) {
            exceptionOccurred = true;
        }
        
        response.getWriter().println(!resourceInputStream.isOpen());
        response.getWriter().println(!exceptionOccurred);
        response.getWriter().println(wrapper.getStatus() == 404);
    }
  
    
    
    // ### Helper methods
    
    private FacesContext createFacesContext(HttpServletRequest request, TestRequest testRequest, HttpServletResponse response) {

        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory.getLifecycle(DEFAULT_LIFECYCLE);

        ExternalContext extContext = new ExternalContextImpl(request.getServletContext(), testRequest, response);
        FacesContext facesContext = new FacesContextImpl(extContext, lifecycle) {
            {
                FacesContext.setCurrentInstance(this);
            }
        };

        return facesContext;
    }
    
    interface TestMethod {
        void test(FacesContext facesContext, TestRequest testRequest, TestResponse wrapper, HttpServletResponse response, ResourceHandler handler) throws ServletException, IOException;
    }
    
    private void run(String methodName, TestMethod testMethod, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        run(methodName, testMethod, request, response, null, null);
    }
    
    private void run(String methodName, TestMethod testMethod, HttpServletRequest request, HttpServletResponse response, WebContextInitParameter param, String paramValue) throws ServletException, IOException {
        
        // Check if we need to skip this test
        String testRequestParam = request.getParameter("test");
        if (testRequestParam != null && !testRequestParam.equals(methodName)) {
            return;
        }
        
        TestRequest testRequest = new TestRequest(request);
        FacesContext facesContext = createFacesContext(request, testRequest, response);
        
        WebConfiguration config = WebConfiguration.getInstance();
        String oldParamValue = "";
        
        try {
            if (param != null) {
                oldParamValue = config.getOptionValue(param);
                config.overrideContextInitParameter(param, paramValue);
            }
            
            ApplicationAssociate associate = ApplicationAssociate.getInstance(facesContext.getExternalContext());
            associate.setResourceManager(new ResourceManager(null)); // null for no caching
            ResourceHandler handler = new ResourceHandlerImpl();
            Application app = facesContext.getApplication();
            ResourceHandler oldResourceHandler = app.getResourceHandler();
            app.setResourceHandler(handler);
            
            TestResponse testResponse = new TestResponse((HttpServletResponse) facesContext.getExternalContext().getResponse());
            facesContext.getExternalContext().setResponse(testResponse);
            
            try {
                response.getWriter().println("\n" + methodName);
                
                // Execute the actual test method
                testMethod.test(facesContext, testRequest, testResponse, response, oldResourceHandler);
                
            } finally {
                app.setResourceHandler(oldResourceHandler);
            }
        } finally {
            if (param != null) {
                config.setOptionValue(param, oldParamValue == null? "" : oldParamValue);
            }
        }
        
    }
    
    private byte[] getBytes(URL url) throws IOException {
        return getBytes(url, false);
    }

    private byte[] getBytes(URL url, boolean compress) throws IOException {
        URLConnection c = url.openConnection();
        c.setUseCaches(false);
        InputStream in = c.getInputStream();
        
        return compress ? getCompressedBytes(in) : getBytes(in);
    }

    private byte[] getBytes(InputStream in) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (int i = in.read(); i != -1; i = in.read()) {
            o.write(i);
        }
        in.close();
        
        return o.toByteArray();
    }
    
    private byte[] getCompressedBytes(InputStream in) throws IOException {

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        GZIPOutputStream compress = new GZIPOutputStream(o);
        for (int i = in.read(); i != -1; i = in.read()) {
            compress.write(i);
        }
        compress.flush();
        compress.close();
        
        return o.toByteArray();
    }
    
    
    
    // Helper wrappers
    
    private static class TestRequest extends HttpServletRequestWrapper {

        private static final TimeZone GMT = getTimeZone("GMT");
        private static final String[] datePatterns = { "EE, dd MMM yyyy HH:mm:ss zz", "EEEE, dd-MMM-yy HH:mm:ss zz",
                "EE MMM  d HH:mm:ss yyyy" };

        private List<DateFormat> dateFormats;

        private String servletPath;
        private String pathInfo;
        private Map<String, String[]> parameterMap = new HashMap<>();
        private Map<String, String[]> headersMap = new HashMap<>();

        public TestRequest(HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getParameter(String name) {
            if (parameterMap.containsKey(name)) {
                return parameterMap.get(name)[0];
            }
            return null;
        }
        
        @Override
        public Map<String, String[]> getParameterMap() {
            return parameterMap;
        }
        
        @Override
        public String[] getParameterValues(String name) {
            return parameterMap.get(name);
        }
        
        @Override
        public Enumeration<String> getParameterNames() {
            return enumeration(getParameterMap().keySet());
        }
        
        @Override
        public String getHeader(String name) {
            if (headersMap.containsKey(name)) {
                return headersMap.get(name)[0];
            }
            return null;
        }
        
        public Map<String, String[]> getHeadersMap() {
            return headersMap;
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            return enumeration(headersMap.keySet());
        }
        
        @Override
        public Enumeration<String> getHeaders(String name) {
            
            if (headersMap.containsKey(name)) {
                return enumeration(asList(headersMap.get(name)));
            }
            
            return enumeration(emptyList());
        }
        
        @Override
        public long getDateHeader(String name) {
              String header = getHeader(name);
                
                if (header == null) {
                    // Spec defines we should return -1 if header doesn't exist
                    return -1;
                }
                
                if (dateFormats == null) {
                    dateFormats = new ArrayList<>(datePatterns.length);
                    for (String datePattern : datePatterns) {
                        dateFormats.add(createDateFormat(datePattern));
                    }
                }
                
                for (DateFormat dateFormat : dateFormats) {
                    try {
                        return dateFormat.parse(header).getTime();
                    } catch (ParseException e) {
                        // noop
                    }
                }
                
                // If no conversion is possible, spec says an IllegalArgumentException should be thrown
                throw new IllegalArgumentException("Can't convert " + header + " to a date");
        }
        
        private DateFormat createDateFormat(String pattern) {
            DateFormat dateFormat = new SimpleDateFormat(pattern, US);
            dateFormat.setTimeZone(GMT);
            return dateFormat;
        }
        
        @Override
        public int getIntHeader(String name) {
            String header = getHeader(name);
            
            if (header == null) {
                // Spec defines we should return -1 is header doesn't exist
                return -1;
            }
            
            // If header ain't an integer, spec says a NumberFormatException should be thrown,
            // which is what Integer.parseInt will do.
            return parseInt(header);
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
    

    private static class TestResponse extends HttpServletResponseWrapper {

        private TestServletOutputStream out;
        private Map<String, String> headers = new HashMap<>();
        private int status;
        
        @Override
        public ServletResponse getResponse() {
            return super.getResponse();
        }
        
        public byte[] getBytes() {
            return out.getBytes();
        }

        public TestResponse(HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
        }
        
        @Override
        public void addHeader(String name, String value) {
            headers.put(name.toLowerCase(), value.toLowerCase());
        }
        
        @Override
        public void setHeader(String name, String value) {
            headers.put(name.toLowerCase(), value.toLowerCase());
        }
        
        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }
        
        @Override
        public boolean containsHeader(String name) {
            return headers.containsKey(name);
        }
        
        @Override
        public void setContentType(String type) {
            headers.put("content-type", type);
        }
        
        @Override
        public void setContentLength(int len) {
            headers.put("content-length", len + "");
        }

        @Override
        public void setBufferSize(int size) {
        }
        
        @Override
        public boolean isCommitted() {
            return false;
        }
        
        public ServletOutputStream getOutputStream() throws IOException {
            out = new TestServletOutputStream();
            return out;
        }
        
        @Override
        public void setStatus(int sc) {
            status = sc;
        }
        
        @Override
        public void sendError(int sc) throws IOException {
            status = sc;
        }
        
        @Override
        public void sendError(int sc, String msg) throws IOException {
            status = sc;
        }
        
        @Override
        public int getStatus() {
            return status;
        }
        

        private class TestServletOutputStream extends ServletOutputStream {

            private ByteArrayOutputStream out = new ByteArrayOutputStream();

            public TestServletOutputStream() {
            }
            
            public void write(int b) throws IOException {
                out.write(b);
            }

            public void write(byte b[]) throws IOException {
                out.write(b);
            }

            public void write(byte b[], int off, int len) throws IOException {
                out.write(b, off, len);
            }

            public void flush() throws IOException {
                out.flush();
            }

            public void close() throws IOException {
                out.close();
            }

            public byte[] getBytes() {
                return out.toByteArray();
            }

            @Override
            public boolean isReady() {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public void setWriteListener(WriteListener wl) {
                throw new UnsupportedOperationException("Not supported");
            }
        }
    }

    private static abstract class URLConnectionWrapper extends URLConnection {

        private URLConnection wrapped;

        public URLConnectionWrapper(final URLConnection urlConnection) {
            super(urlConnection.getURL());
            this.wrapped = urlConnection;
        }

        @Override
        public void connect() throws IOException {
            wrapped.connect();
            connected = true;
        }

        @Override
        public boolean getAllowUserInteraction() {
            return wrapped.getAllowUserInteraction();
        }

        @Override
        public Object getContent() throws IOException {
            return wrapped.getContent();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Object getContent(final Class[] types) throws IOException {
            return wrapped.getContent(types);
        }

        @Override
        public String getContentEncoding() {
            return wrapped.getContentEncoding();
        }

        @Override
        public int getContentLength() {
            return wrapped.getContentLength();
        }

        @Override
        public String getContentType() {
            return wrapped.getContentType();
        }

        @Override
        public long getDate() {
            return wrapped.getDate();
        }

        @Override
        public boolean getDefaultUseCaches() {
            return wrapped.getDefaultUseCaches();
        }

        @Override
        public boolean getDoInput() {
            return wrapped.getDoInput();
        }

        @Override
        public boolean getDoOutput() {
            return wrapped.getDoInput();
        }

        @Override
        public long getExpiration() {
            return wrapped.getExpiration();
        }

        @Override
        public String getHeaderField(final int pos) {
            return wrapped.getHeaderField(pos);
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return wrapped.getHeaderFields();
        }

        @Override
        public Map<String, List<String>> getRequestProperties() {
            return wrapped.getRequestProperties();
        }

        @Override
        public void addRequestProperty(final String field, final String newValue) {
            wrapped.addRequestProperty(field, newValue);
        }

        @Override
        public String getHeaderField(final String key) {
            return wrapped.getHeaderField(key);
        }

        @Override
        public long getHeaderFieldDate(final String field, final long defaultValue) {
            return wrapped.getHeaderFieldDate(field, defaultValue);
        }

        @Override
        public int getHeaderFieldInt(final String field, final int defaultValue) {
            return wrapped.getHeaderFieldInt(field, defaultValue);
        }

        @Override
        public String getHeaderFieldKey(final int posn) {
            return wrapped.getHeaderFieldKey(posn);
        }

        @Override
        public long getIfModifiedSince() {
            return wrapped.getIfModifiedSince();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return wrapped.getInputStream();
        }

        @Override
        public long getLastModified() {
            return wrapped.getLastModified();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return wrapped.getOutputStream();
        }

        @Override
        public java.security.Permission getPermission() throws IOException {
            return wrapped.getPermission();
        }

        @Override
        public String getRequestProperty(final String field) {
            return wrapped.getRequestProperty(field);
        }

        @Override
        public URL getURL() {
            return url;
        }

        @Override
        public boolean getUseCaches() {
            return wrapped.getUseCaches();
        }

        @Override
        public void setAllowUserInteraction(final boolean newValue) {
            wrapped.setAllowUserInteraction(newValue);
        }

        @Override
        public void setDefaultUseCaches(final boolean newValue) {
            wrapped.setDefaultUseCaches(newValue);
        }

        @Override
        public void setDoInput(final boolean newValue) {
            wrapped.setDoInput(newValue);
        }

        @Override
        public void setDoOutput(final boolean newValue) {
            wrapped.setDoOutput(newValue);
        }

        @Override
        public void setIfModifiedSince(final long newValue) {
            wrapped.setIfModifiedSince(newValue);
        }

        @Override
        public void setRequestProperty(final String field, final String newValue) {
            wrapped.setRequestProperty(field, newValue);
        }

        @Override
        public void setUseCaches(final boolean newValue) {
            wrapped.setUseCaches(newValue);
        }

        @Override
        public void setConnectTimeout(final int timeout) {
            wrapped.setConnectTimeout(timeout);
        }

        @Override
        public int getConnectTimeout() {
            return wrapped.getConnectTimeout();
        }

        @Override
        public void setReadTimeout(final int timeout) {
            wrapped.setReadTimeout(timeout);
        }

        @Override
        public int getReadTimeout() {
            return wrapped.getReadTimeout();
        }

    }

}
