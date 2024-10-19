/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.webapp;

import static jakarta.faces.FactoryFinder.FACES_CONTEXT_FACTORY;
import static jakarta.faces.FactoryFinder.LIFECYCLE_FACTORY;
import static jakarta.faces.lifecycle.LifecycleFactory.DEFAULT_LIFECYCLE;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static java.util.Collections.emptySet;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.range;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_0_rev_a changed_modified_2_1 changed_modified_2_2
 * changed_modified_2_3">FacesServlet</strong> is a Jakarta Servlet servlet that manages the request processing
 * lifecycle for web applications that are utilizing Jakarta Faces to construct the user interface.
 * </p>
 *
 * <div class="changed_added_2_1">
 *
 * <p>
 * If the application is running in a Jakarta Servlet 3.0 (and beyond) container, the runtime must provide an
 * implementation of the {@link jakarta.servlet.ServletContainerInitializer} interface that declares the following
 * classes in its {@link jakarta.servlet.annotation.HandlesTypes} annotation.
 * </p>
 *
 * <ul>
 *
 * <li class="changed_added_2_3">{@link jakarta.faces.annotation.FacesConfig}</li>
 *
 * <li>{@link jakarta.faces.application.ResourceDependencies}</li>
 *
 * <li>{@link jakarta.faces.application.ResourceDependency}</li>
 *
 * <li>jakarta.faces.bean.ManagedBean</li>
 *
 * <li>{@link jakarta.faces.component.FacesComponent}</li>
 *
 * <li>{@link jakarta.faces.component.UIComponent}</li>
 *
 * <li>{@link jakarta.faces.convert.Converter}</li>
 *
 * <li>{@link jakarta.faces.convert.FacesConverter}</li>
 *
 * <li>{@link jakarta.faces.event.ListenerFor}</li>
 *
 * <li>{@link jakarta.faces.event.ListenersFor}</li>
 *
 * <li>{@link jakarta.faces.render.FacesBehaviorRenderer}</li>
 *
 * <li>{@link jakarta.faces.render.Renderer}</li>
 *
 * <li>{@link jakarta.faces.validator.FacesValidator}</li>
 *
 * <li>{@link jakarta.faces.validator.Validator}</li>
 *
 * </ul>
 *
 * <p>
 * This Jakarta Servlet servlet must automatically be mapped if it is <strong>not</strong> explicitly mapped in
 * <code>web.xml</code> or <code>web-fragment.xml</code> and one or more of the following conditions are true.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * A <code>faces-config.xml</code> file is found in <code>WEB-INF</code>
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * A <code>faces-config.xml</code> file is found in the <code>META-INF</code> directory of a jar in the application's
 * classpath.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * A filename ending in <code>.faces-config.xml</code> is found in the <code>META-INF</code> directory of a jar in the
 * application's classpath.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * The <code>jakarta.faces.CONFIG_FILES</code> context param is declared in <code>web.xml</code> or
 * <code>web-fragment.xml</code>.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * The <code>Set</code> of classes passed to the <code>onStartup()</code> method of the
 * <code>ServletContainerInitializer</code> implementation is not empty.
 * </p>
 * </li>
 *
 * </ul>
 *
 * <p>
 * If the runtime determines that the servlet must be automatically mapped, it must be mapped to the following
 * &lt;<code>url-pattern</code>&gt; entries.
 * </p>
 *
 * <ul>
 * <li>/faces/*</li>
 * <li>*.jsf</li>
 * <li>*.faces</li>
 * <li class="changed_added_2_3">*.xhtml</li>
 * </ul>
 *
 * </div>
 *
 * <p class="changed_added_2_3">
 * Note that the automatic mapping to {@code *.xhtml} can be disabled with the context param
 * {@link #DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME}.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * This class must be annotated with {@code jakarta.servlet.annotation.MultipartConfig}. This causes the Jakarta Servlet
 * container in which the Jakarta Faces implementation is running to correctly handle multipart form data.
 * </p>
 *
 * <p>
 * <strong>Some security considerations relating to this class</strong>
 * </p>
 *
 * <p>
 * The topic of web application security is a cross-cutting concern and every aspect of the specification address it.
 * However, as with any framework, the application developer needs to pay careful attention to security. Please consider
 * these topics among the rest of the security concerns for the application. This is by no means a complete list of
 * security concerns, and is no substitute for a thorough application level security review.
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * <strong>Prefix mappings and the <code>FacesServlet</code></strong>
 * </p>
 *
 * <p>
 * If the <code>FacesServlet</code> is mapped using a prefix <code>&lt;url-pattern&gt;</code>, such as
 * <code>&lt;url-pattern&gt;/faces/*&lt;/url-pattern&gt;</code>, something must be done to prevent access to the view
 * source without its first being processed by the <code>FacesServlet</code>. One common approach is to apply a
 * &lt;security-constraint&gt; to all facelet files and flow definition files. Please see the <strong>Deployment
 * Descriptor</strong> chapter of the Jakarta Servlet Specification for more information the use of
 * &lt;security-constraint&gt;.
 * </p>
 *
 * <p>
 * <strong>Allowable HTTP Methods</strong>
 * </p>
 *
 * <p>
 * The Jakarta Faces Specification only requires the use of the GET and POST http methods. If your web
 * application does not require any other http methods, such as PUT and DELETE, please consider restricting the
 * allowable http methods using the &lt;http-method&gt; and &lt;http-method-omission&gt; elements. Please see the
 * <strong>Security</strong> sections of the Jakarta Servlet Specification for more information about the use of these
 * elements.
 * </p>
 *
 *
 * </blockquote>
 *
 * </div>
 */
@MultipartConfig
public final class FacesServletImpl implements Servlet {

    /**
     * <p>
     * Context initialization parameter name for a comma delimited list of context-relative resource paths (in addition to
     * <code>/WEB-INF/faces-config.xml</code> which is loaded automatically if it exists) containing Jakarta Faces
     * configuration information.
     * </p>
     */
    public static final String CONFIG_FILES_ATTR = "jakarta.faces.CONFIG_FILES";

    /**
     * <p>
     * Context initialization parameter name for the lifecycle identifier of the {@link Lifecycle} instance to be utilized.
     * </p>
     */
    public static final String LIFECYCLE_ID_ATTR = "jakarta.faces.LIFECYCLE_ID";

    /**
     * <p class="changed_added_2_3">
     * The <code>ServletContext</code> init parameter consulted by the runtime to tell if the automatic mapping of the
     * {@code FacesServlet} to the extension {@code *.xhtml} should be disabled. The implementation must disable this
     * automatic mapping if and only if the value of this parameter is equal, ignoring case, to {@code true}.
     * </p>
     *
     * <p>
     * If this parameter is not specified, this automatic mapping is enabled as specified above.
     * </p>
     */
    public static final String DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME = "jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML";

    /**
     * <p class="changed_added_4_0">
     * The <code>ServletContext</code> init parameter consulted by the runtime to tell if the automatic mapping of the
     * {@code FacesServlet} to the extensionless variant (without {@code *.xhtml}) should be enabled. The implementation
     * must enable this automatic mapping if and only if the value of this parameter is equal, ignoring case, to {@code true}.
     * </p>
     *
     * <p>
     * If this parameter is not specified, this automatic mapping is not enabled.
     * </p>
     */
    public static final String AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME = "jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING";

    /**
     * The <code>Logger</code> for this class.
     */
    private static final Logger LOGGER = Logger.getLogger("jakarta.faces.webapp", "jakarta.faces.LogStrings");

    /**
     * A white space separated list of case sensitive HTTP method names that are allowed to be processed by this servlet. *
     * means allow all
     */
    private static final String ALLOWED_HTTP_METHODS_ATTR = "com.sun.faces.allowedHttpMethods";

    // Http method names must be upper case. http://www.w3.org/Protocols/HTTP/NoteMethodCS.html
    // List of valid methods in Http 1.1 http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9

    private enum HttpMethod {

        OPTIONS("OPTIONS"), GET("GET"), HEAD("HEAD"), POST("POST"), PUT("PUT"), DELETE("DELETE"), TRACE("TRACE"), CONNECT("CONNECT");

        private final String name;

        HttpMethod(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    private final Set<HttpMethod> defaultAllowedHttpMethods = range(HttpMethod.OPTIONS, HttpMethod.CONNECT);

    private Set<String> allowedUnknownHttpMethods;
    private Set<HttpMethod> allowedKnownHttpMethods;
    private Set<HttpMethod> allHttpMethods;

    private boolean allowAllMethods;

    /**
     * <p>
     * Factory for {@link FacesContext} instances.
     * </p>
     */
    private FacesContextFactory facesContextFactory;

    /**
     * <p>
     * The {@link Lifecycle} instance to use for request processing.
     * </p>
     */
    private Lifecycle lifecycle;

    /**
     * <p>
     * The <code>ServletConfig</code> instance for this servlet.
     * </p>
     */
    private ServletConfig servletConfig;

    /**
     * From GLASSFISH-15632. If true, the FacesContext instance left over from startup time has been released.
     */
    private boolean initFacesContextReleased;

    /**
     * <p>
     * Acquire the factory instances we will require.
     * </p>
     *
     * @throws ServletException if, for any reason, the startup of this Faces application failed. This includes errors in
     * the config file that is parsed before or during the processing of this <code>init()</code> method.
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        // Save our ServletConfig instance
        this.servletConfig = servletConfig;

        // Acquire our FacesContextFactory instance
        facesContextFactory = acquireFacesContextFactory();

        // Acquire our Lifecycle instance
        lifecycle = acquireLifecycle();

        initHttpMethodValidityVerificationWithCatch();
    }

    /**
     * <p class="changed_modified_2_0">
     * <span class="changed_modified_2_2">Process</span> an incoming request, and create the corresponding response
     * according to the following specification.
     * </p>
     *
     * <div class="changed_modified_2_0">
     *
     * <p>
     * If the <code>request</code> and <code>response</code> arguments to this method are not instances of
     * <code>HttpServletRequest</code> and <code>HttpServletResponse</code>, respectively, the results of invoking this
     * method are undefined.
     * </p>
     *
     * <p>
     * This method must respond to requests that <span class="changed_modified_2_2">contain</span> the following strings by
     * invoking the <code>sendError</code> method on the response argument (cast to <code>HttpServletResponse</code>),
     * passing the code <code>HttpServletResponse.SC_NOT_FOUND</code> as the argument.
     * </p>
     *
     * <pre>
     * <code>
     * /WEB-INF/
     * /WEB-INF
     * /META-INF/
     * /META-INF
     * </code>
     * </pre>
     *
     * <p>
     * If none of the cases described above in the specification for this method apply to the servicing of this request, the
     * following action must be taken to service the request.
     * </p>
     *
     * <p>
     * Acquire a {@link FacesContext} instance for this request.
     * </p>
     *
     * <p>
     * Acquire the <code>ResourceHandler</code> for this request by calling
     * {@link jakarta.faces.application.Application#getResourceHandler}. Call
     * {@link jakarta.faces.application.ResourceHandler#isResourceRequest}.
     *
     * If this returns <code>true</code> call {@link jakarta.faces.application.ResourceHandler#handleResourceRequest}.
     *
     * If this returns <code>false</code>, <span class="changed_added_2_2">call
     * {@link jakarta.faces.lifecycle.Lifecycle#attachWindow} followed by </span>
     * {@link jakarta.faces.lifecycle.Lifecycle#execute} followed by {@link jakarta.faces.lifecycle.Lifecycle#render}.
     *
     * If a {@link jakarta.faces.FacesException} is thrown in either case, extract the cause from the
     * <code>FacesException</code>.
     *
     * If the cause is <code>null</code> extract the message from the <code>FacesException</code>, put it inside of a new
     * <code>ServletException</code> instance, and pass the <code>FacesException</code> instance as the root cause, then
     * rethrow the <code>ServletException</code> instance. If the cause is an instance of <code>ServletException</code>,
     * rethrow the cause. If the cause is an instance of <code>IOException</code>, rethrow the cause.
     *
     * Otherwise, create a new <code>ServletException</code> instance, passing the message from the cause, as the first
     * argument, and the cause itself as the second argument.
     * </p>
     *
     * <p class="changed_modified_2_0_rev_a">
     * The implementation must make it so {@link jakarta.faces.context.FacesContext#release} is called within a finally
     * block as late as possible in the processing for the Jakarta Faces related portion of this request.
     * </p>
     *
     * </div>
     *
     * @param req The Jakarta Servlet request we are processing
     * @param resp The Jakarta Servlet response we are creating
     *
     * @throws IOException if an input/output error occurs during processing
     * @throws ServletException if a Jakarta Servlet error occurs during processing
     *
     */
    @Override
    public void service(ServletRequest req, ServletResponse resp) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        if (!isHttpMethodValid(request)) {
            response.sendError(SC_BAD_REQUEST);
            return;
        }

        logIfThreadInterrupted();

        // If prefix mapped, then ensure requests for /WEB-INF are not processed.
        if (notProcessWebInfIfPrefixMapped(request, response)) {
            return;
        }

        releaseFacesInitContextIfNeeded();

        // Acquire the FacesContext instance for this request
        FacesContext context = acquireFacesContext(request, response);

        // Execute the request processing lifecycle for this request
        try {
            executeLifecyle(context);
        } finally {
            // Release the FacesContext instance for this request
            context.release();
        }
    }

    /**
     * <p>
     * Release all resources acquired at startup time.
     * </p>
     */
    @Override
    public void destroy() {
        facesContextFactory = null;
        lifecycle = null;
        servletConfig = null;
        uninitHttpMethodValidityVerification();
    }

    /**
     * <p>
     * Return the <code>ServletConfig</code> instance for this servlet.
     * </p>
     */
    @Override
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    /**
     * <p>
     * Return information about this Servlet.
     * </p>
     */
    @Override
    public String getServletInfo() {
        return getClass().getName();
    }

    // --------------------------------------------------------- Private Methods

    private FacesContextFactory acquireFacesContextFactory() throws UnavailableException {
        try {
            return (FacesContextFactory) FactoryFinder.getFactory(FACES_CONTEXT_FACTORY);
        } catch (FacesException e) {
            String msg = LOGGER.getResourceBundle().getString("severe.webapp.facesservlet.init_failed");
            LOGGER.log(SEVERE, msg, e.getCause() != null ? e.getCause() : e);

            throw new UnavailableException(msg);
        }
    }

    private Lifecycle acquireLifecycle() throws ServletException {
        try {
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);

            // First look in the Jakarta Servlet init-param set
            String lifecycleId = servletConfig.getInitParameter(LIFECYCLE_ID_ATTR);

            if (lifecycleId == null) {
                // If not found, look in the context-param set
                lifecycleId = servletConfig.getServletContext().getInitParameter(LIFECYCLE_ID_ATTR);
            }

            if (lifecycleId == null) {
                // If still not found, use the default
                lifecycleId = DEFAULT_LIFECYCLE;
            }

            return lifecycleFactory.getLifecycle(lifecycleId);

        } catch (FacesException e) {
            Throwable rootCause = e.getCause();
            if (rootCause == null) {
                throw e;
            }

            throw new ServletException(e.getMessage(), rootCause);
        }
    }

    private FacesContext acquireFacesContext(HttpServletRequest request, HttpServletResponse response) {
        return facesContextFactory.getFacesContext(servletConfig.getServletContext(), request, response, lifecycle);
    }

    private void initHttpMethodValidityVerificationWithCatch() throws ServletException {
        try {
            initHttpMethodValidityVerification();
        } catch (FacesException e) {
            Throwable rootCause = e.getCause();
            if (rootCause == null) {
                throw e;
            } else {
                throw new ServletException(e.getMessage(), rootCause);
            }
        }
    }

    private void initHttpMethodValidityVerification() {

        allHttpMethods = allOf(HttpMethod.class);

        // Configure our permitted HTTP methods

        allowedUnknownHttpMethods = emptySet();
        allowedKnownHttpMethods = defaultAllowedHttpMethods;

        String allowedHttpMethodsString = servletConfig.getServletContext().getInitParameter(ALLOWED_HTTP_METHODS_ATTR);
        if (allowedHttpMethodsString != null) {
            String[] methods = allowedHttpMethodsString.split("\\s+");

            allowedUnknownHttpMethods = new HashSet<>(methods.length);
            List<String> allowedKnownHttpMethodsStringList = new ArrayList<>();

            // Validate input against allHttpMethods data structure
            for (String httpMethod : methods) {
                if (httpMethod.equals("*")) {
                    allowAllMethods = true;
                    allowedUnknownHttpMethods = emptySet();
                    return;
                }

                if (!isKnownHttpMethod(httpMethod)) {

                    logUnknownHttpMethod(httpMethod);

                    // we use a Set to prevent duplicates
                    allowedUnknownHttpMethods.add(httpMethod);
                } else {
                    // prevent duplicates
                    if (!allowedKnownHttpMethodsStringList.contains(httpMethod)) {
                        allowedKnownHttpMethodsStringList.add(httpMethod);
                    }
                }
            }

            // Optimally initialize allowedKnownHttpMethods
            initializeAllowedKnownHttpMethods(allowedKnownHttpMethodsStringList);
        }
    }

    private void initializeAllowedKnownHttpMethods(List<String> allowedKnownHttpMethodsStringList) {
        if (5 == allowedKnownHttpMethodsStringList.size()) {
            allowedKnownHttpMethods = EnumSet.of(HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                    HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)), HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(2)),
                    HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(3)), HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(4)));

        } else if (4 == allowedKnownHttpMethodsStringList.size()) {
            allowedKnownHttpMethods = EnumSet.of(HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                    HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)), HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(2)),
                    HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(3)));

        } else if (3 == allowedKnownHttpMethodsStringList.size()) {
            allowedKnownHttpMethods = EnumSet.of(HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                    HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)), HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(2)));

        } else if (2 == allowedKnownHttpMethodsStringList.size()) {
            allowedKnownHttpMethods = EnumSet.of(HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                    HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)));

        } else if (1 == allowedKnownHttpMethodsStringList.size()) {
            allowedKnownHttpMethods = EnumSet.of(HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)));

        } else {
            List<HttpMethod> restList = new ArrayList<>(allowedKnownHttpMethodsStringList.size() - 1);

            for (int i = 1; i < allowedKnownHttpMethodsStringList.size() - 1; i++) {
                restList.add(HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(i)));
            }

            HttpMethod first = HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0));
            HttpMethod[] rest = new HttpMethod[restList.size()];
            restList.toArray(rest);
            allowedKnownHttpMethods = EnumSet.of(first, rest);
        }
    }

    private void logIfThreadInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            if (LOGGER.isLoggable(FINER)) {
                LOGGER.log(FINE, "Thread {0} given to FacesServlet.service() in interrupted state", Thread.currentThread().getName());
            }
        }
    }

    private void logUnknownHttpMethod(String httpMethod) {
        if (LOGGER.isLoggable(WARNING)) {
            HttpMethod[] values = HttpMethod.values();
            Object[] arg = new Object[values.length + 1];
            arg[0] = httpMethod;
            System.arraycopy(values, HttpMethod.OPTIONS.ordinal(), arg, 1, values.length);

            LOGGER.log(WARNING, "warning.webapp.facesservlet.init_invalid_http_method", arg);
        }
    }

    private boolean isKnownHttpMethod(String httpMethod) {
        try {
            HttpMethod.valueOf(httpMethod);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void releaseFacesInitContextIfNeeded() {
        if (!initFacesContextReleased) {
            FacesContext initFacesContext = FacesContext.getCurrentInstance();
            if (initFacesContext != null) {
                initFacesContext.release();
            }
            initFacesContextReleased = true;
        }
    }

    private boolean notProcessWebInfIfPrefixMapped(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            pathInfo = pathInfo.toUpperCase();
            if (pathInfo.contains("/WEB-INF/") || pathInfo.contains("/WEB-INF") || pathInfo.contains("/META-INF/") || pathInfo.contains("/META-INF")) {
                response.sendError(SC_NOT_FOUND);
                return true;
            }
        }

        return false;
    }

    private void executeLifecyle(FacesContext context) throws IOException, ServletException {
        try {
            ResourceHandler handler = context.getApplication().getResourceHandler();
            if (handler.isResourceRequest(context)) {
                handler.handleResourceRequest(context);
            } else {
                lifecycle.attachWindow(context);
                lifecycle.execute(context);
                lifecycle.render(context);
            }
        } catch (FacesException e) {
            Throwable t = e.getCause();

            if (t == null) {
                throw new ServletException(e.getMessage(), e);
            }

            if (t instanceof ServletException) {
                throw (ServletException) t;
            }

            if (t instanceof IOException) {
                throw (IOException) t;
            }

            throw new ServletException(t.getMessage(), t);
        }
    }

    private void uninitHttpMethodValidityVerification() {
        assert null != allowedUnknownHttpMethods;
        assert null != defaultAllowedHttpMethods;
        assert null != allHttpMethods;

        allowedUnknownHttpMethods.clear();
        allowedUnknownHttpMethods = null;
        allowedKnownHttpMethods.clear();
        allowedKnownHttpMethods = null;
        allHttpMethods.clear();
        allHttpMethods = null;

    }

    private boolean isHttpMethodValid(HttpServletRequest request) {
        boolean result = false;
        if (allowAllMethods) {
            result = true;
        } else {
            String requestMethodString = request.getMethod();
            HttpMethod requestMethod = null;
            boolean isKnownHttpMethod;
            try {
                requestMethod = HttpMethod.valueOf(requestMethodString);
                isKnownHttpMethod = true;
            } catch (IllegalArgumentException e) {
                isKnownHttpMethod = false;
            }
            if (isKnownHttpMethod) {
                result = allowedKnownHttpMethods.contains(requestMethod);
            } else {
                result = allowedUnknownHttpMethods.contains(requestMethodString);
            }

        }

        return result;
    }

}
