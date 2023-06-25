/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.context;

import static com.sun.faces.RIConstants.PUSH_RESOURCE_URLS_KEY_NAME;
import static com.sun.faces.context.ContextParam.SendPoweredByHeader;
import static com.sun.faces.util.MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.context.flash.ELFlash;
import com.sun.faces.renderkit.html_basic.ScriptRenderer;
import com.sun.faces.renderkit.html_basic.StylesheetRenderer;
import com.sun.faces.util.CollectionsUtils;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.TypedCollections;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.context.FlashFactory;
import jakarta.faces.context.PartialResponseWriter;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.ResponseStateManager;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.PushBuilder;

/**
 * <p>
 * This implementation of {@link ExternalContext} is specific to the servlet implementation.
 */
public class ExternalContextImpl extends ExternalContext {

    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();

    private ServletContext servletContext;
    private ServletRequest request;
    private ServletResponse response;
    private ClientWindow clientWindow = null;

    private Map<String, Object> applicationMap = null;
    private Map<String, Object> sessionMap = null;
    private Map<String, Object> requestMap = null;
    private Map<String, String> requestParameterMap = null;
    private Map<String, String[]> requestParameterValuesMap = null;
    private Map<String, String> requestHeaderMap = null;
    private Map<String, String[]> requestHeaderValuesMap = null;
    private Map<String, Object> cookieMap = null;
    private Map<String, String> initParameterMap = null;

    private Flash flash;
    private final boolean distributable;

    private enum PREDEFINED_COOKIE_PROPERTIES {
        domain, maxAge, path, secure, httpOnly, attribute;

        static PREDEFINED_COOKIE_PROPERTIES of(String key) {
            try {
                return valueOf(key);
            }
            catch (IllegalArgumentException ignore) {
                return attribute;
            }
        }
    }

    // we want exactly the UnmodifiableMap.class (which is private) so do not remove the call to Collections.unmodifiableMap(...)
    static final Class theUnmodifiableMapClass = Collections.unmodifiableMap(Collections.emptyMap()).getClass();

    private static final Map<String, String> fallbackContentTypeMap = Map.of(
            "js", ScriptRenderer.DEFAULT_CONTENT_TYPE,
            "css", StylesheetRenderer.DEFAULT_CONTENT_TYPE,
            "properties", "text/plain");

    // ------------------------------------------------------------ Constructors

    public ExternalContextImpl(ServletContext sc, ServletRequest request, ServletResponse response) {

        // Validate the incoming parameters
        Util.notNull("sc", sc);
        Util.notNull("request", request);
        Util.notNull("response", response);

        // Save references to our context, request, and response
        servletContext = sc;
        this.request = request;
        this.response = response;

        boolean enabled = ContextParamUtils.getValue(servletContext, SendPoweredByHeader, Boolean.class);
        if (enabled) {
            ((HttpServletResponse) response).addHeader("X-Powered-By", "Faces/4.0");
        }

        distributable = ContextParamUtils.getValue(servletContext, ContextParam.EnableDistributable, Boolean.class);

    }

    // -------------------------------------------- Methods from ExternalContext

    /**
     * @see ExternalContext#getSession(boolean)
     */
    @Override
    public Object getSession(boolean create) {
        return ((HttpServletRequest) request).getSession(create);
    }

    @Override
    public String getSessionId(boolean create) {
        String id = null;

        final HttpSession session = (HttpSession) getSession(create);
        if (session != null) {
            id = session.getId();
        }

        return id;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getContext()
     */
    @Override
    public Object getContext() {
        return servletContext;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getContextName()
     */
    @Override
    public String getContextName() {
        return servletContext.getServletContextName();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequest()
     */
    @Override
    public Object getRequest() {
        return request;
    }

    /**
     * @see ExternalContext#setRequest(Object)
     */
    @Override
    public void setRequest(Object request) {
        if (request instanceof ServletRequest) {
            this.request = (ServletRequest) request;
            requestHeaderMap = null;
            requestHeaderValuesMap = null;
            requestMap = null;
            requestParameterMap = null;
            requestParameterValuesMap = null;
        }
    }

    /**
     * @see ExternalContext#setRequestCharacterEncoding(String)
     */
    @Override
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        request.setCharacterEncoding(encoding);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResponse()
     */
    @Override
    public Object getResponse() {
        return response;
    }

    /**
     * @see ExternalContext#setResponse(Object)
     */
    @Override
    public void setResponse(Object response) {
        if (response instanceof ServletResponse) {
            this.response = (ServletResponse) response;
        }
    }

    @Override
    public ClientWindow getClientWindow() {
        return clientWindow;
    }

    @Override
    public void setClientWindow(ClientWindow window) {
        clientWindow = window;
    }

    /**
     * @see ExternalContext#setResponseCharacterEncoding(String)
     */
    @Override
    public void setResponseCharacterEncoding(String encoding) {
        response.setCharacterEncoding(encoding);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getApplicationMap()
     */
    @Override
    public Map<String, Object> getApplicationMap() {
        if (applicationMap == null) {
            applicationMap = new ApplicationMap(servletContext);
        }
        return applicationMap;
    }

    @Override
    public String getApplicationContextPath() {
        return servletContext.getContextPath();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getSessionMap()
     */
    @Override
    public Map<String, Object> getSessionMap() {
        if (sessionMap == null) {
            if (distributable) {
                sessionMap = new AlwaysPuttingSessionMap((HttpServletRequest) request, FacesContext.getCurrentInstance().getApplication().getProjectStage());
            } else {
                sessionMap = new SessionMap((HttpServletRequest) request, FacesContext.getCurrentInstance().getApplication().getProjectStage());
            }
        }
        return sessionMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestMap()
     */
    @Override
    public Map<String, Object> getRequestMap() {
        if (requestMap == null) {
            requestMap = new RequestMap(request);
        }
        return requestMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestHeaderMap()
     */
    @Override
    public Map<String, String> getRequestHeaderMap() {
        if (null == requestHeaderMap) {
            requestHeaderMap = Collections.unmodifiableMap(new RequestHeaderMap((HttpServletRequest) request));
        }
        return requestHeaderMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestHeaderValuesMap()
     */
    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
        if (null == requestHeaderValuesMap) {
            requestHeaderValuesMap = Collections.unmodifiableMap(new RequestHeaderValuesMap((HttpServletRequest) request));
        }
        return requestHeaderValuesMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestCookieMap()
     */
    @Override
    public Map<String, Object> getRequestCookieMap() {
        if (null == cookieMap) {
            cookieMap = Collections.unmodifiableMap(new RequestCookieMap((HttpServletRequest) request));
        }
        return cookieMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getInitParameterMap()
     */
    @Override
    public Map<String, String> getInitParameterMap() {
        if (null == initParameterMap) {
            initParameterMap = Collections.unmodifiableMap(new InitParameterMap(servletContext));
        }
        return initParameterMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestParameterMap()
     */
    @Override
    public Map<String, String> getRequestParameterMap() {
        if (null == requestParameterMap) {
            requestParameterMap = Collections.unmodifiableMap(new RequestParameterMap(request));
        }
        return requestParameterMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestParameterValuesMap()
     */
    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
        if (null == requestParameterValuesMap) {
            requestParameterValuesMap = Collections.unmodifiableMap(new RequestParameterValuesMap(request));
        }
        return requestParameterValuesMap;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestParameterNames()
     */
    @Override
    public Iterator<String> getRequestParameterNames() {
        return CollectionsUtils.unmodifiableIterator( request.getParameterNames() );
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestLocale()
     */
    @Override
    public Locale getRequestLocale() {
        return request.getLocale();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestLocales()
     */
    @Override
    public Iterator<Locale> getRequestLocales() {
        return CollectionsUtils.unmodifiableIterator(request.getLocales());
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestPathInfo()
     */
    @Override
    public String getRequestPathInfo() {
        return ((HttpServletRequest) request).getPathInfo();
    }

    /**
     * @see ExternalContext#getRealPath(String)
     */
    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestContextPath()
     */
    @Override
    public String getRequestContextPath() {
        return ((HttpServletRequest) request).getContextPath();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestServletPath()
     */
    @Override
    public String getRequestServletPath() {
        return ((HttpServletRequest) request).getServletPath();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestCharacterEncoding()
     */
    @Override
    public String getRequestCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestContentType()
     */
    @Override
    public String getRequestContentType() {
        return request.getContentType();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestContentLength()
     */
    @Override
    public int getRequestContentLength() {
        return request.getContentLength();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResponseCharacterEncoding()
     */
    @Override
    public String getResponseCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResponseContentType()
     */
    @Override
    public String getResponseContentType() {
        return response.getContentType();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getInitParameter(String)
     */
    @Override
    public String getInitParameter(String name) {
        if (name == null) {
            throw new NullPointerException("Init parameter name cannot be null");
        }
        return servletContext.getInitParameter(name);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResourcePaths(String)
     */
    @Override
    public Set<String> getResourcePaths(String path) {
        if (null == path) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "path");
            throw new NullPointerException(message);
        }
        return TypedCollections.dynamicallyCastSet(servletContext.getResourcePaths(path), String.class);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResourceAsStream(String)
     */
    @Override
    public InputStream getResourceAsStream(String path) {
        if (null == path) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "path");
            throw new NullPointerException(message);
        }
        return servletContext.getResourceAsStream(path);
    }

    /**
     * @see ExternalContext#getResource(String)
     */
    @Override
    public URL getResource(String path) {
        if (null == path) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "path");
            throw new NullPointerException(message);
        }
        URL url;
        try {
            url = servletContext.getResource(path);
        } catch (MalformedURLException e) {
            return null;
        }
        return url;
    }

    /**
     * @see ExternalContext#encodeActionURL(String)
     */
    @Override
    public String encodeActionURL(String url) {
        Util.notNull("url", url);
        FacesContext context = FacesContext.getCurrentInstance();
        ClientWindow cw = context.getExternalContext().getClientWindow();
        boolean appendClientWindow = false;
        if (null != cw) {
            appendClientWindow = cw.isClientWindowRenderModeEnabled(context);
        }
        if (appendClientWindow && !url.contains(ResponseStateManager.CLIENT_WINDOW_URL_PARAM)) {
            if (null != cw) {
                String clientWindowId = cw.getId();
                StringBuilder builder = new StringBuilder(url);
                int q = url.indexOf(UrlBuilder.QUERY_STRING_SEPARATOR);
                if (-1 == q) {
                    builder.append(UrlBuilder.QUERY_STRING_SEPARATOR);
                } else {
                    builder.append(UrlBuilder.PARAMETER_PAIR_SEPARATOR);
                }
                builder.append(ResponseStateManager.CLIENT_WINDOW_URL_PARAM).append(UrlBuilder.PARAMETER_NAME_VALUE_SEPARATOR).append(clientWindowId);

                Map<String, String> additionalParams = cw.getQueryURLParameters(context);
                if (null != additionalParams) {
                    for (Map.Entry<String, String> cur : additionalParams.entrySet()) {
                        builder.append(UrlBuilder.PARAMETER_NAME_VALUE_SEPARATOR);
                        builder.append(cur.getKey()).append(UrlBuilder.PARAMETER_NAME_VALUE_SEPARATOR).append(cur.getValue());
                    }
                }
                url = builder.toString();
            }
        }
        // If we have a query string, append it
        return ((HttpServletResponse) response).encodeURL(url);
    }

    /**
     * @see ExternalContext#encodeResourceURL(String)
     */
    @Override
    public String encodeResourceURL(String url) {
        if (url == null) {
            throw new NullPointerException(getExceptionMessageString(NULL_PARAMETERS_ERROR_MESSAGE_ID, "url"));
        }

        String result = ((HttpServletResponse) response).encodeURL(url);
        pushIfPossibleAndNecessary(result);

        return result;
    }

    /**
     * @see ExternalContext#encodeWebsocketURL(String)
     */
    @Override
    public String encodeWebsocketURL(String url) {
        if (url == null) {
            throw new NullPointerException(getExceptionMessageString(NULL_PARAMETERS_ERROR_MESSAGE_ID, "url"));
        }

        HttpServletRequest request = (HttpServletRequest) getRequest();
        int port = ContextParamUtils.getValue(servletContext, ContextParam.WebsocketEndpointPort, Integer.class);

        try {
            URL requestURL = new URL(request.getRequestURL().toString());

            if (port <= 0) {
                port = requestURL.getPort();
            }

            String websocketURL = new URL(requestURL.getProtocol(), requestURL.getHost(), port, url).toExternalForm();
            return encodeResourceURL(websocketURL.replaceFirst("http", "ws"));
        } catch (MalformedURLException e) {
            return url;
        }
    }

    /**
     * @see ExternalContext#encodeNamespace(String)
     */
    @Override
    public String encodeNamespace(String name) {
        return name; // Do nothing for servlets
    }

    /**
     * @see ExternalContext#dispatch(String)
     */
    @Override
    public void dispatch(String requestURI) throws IOException, FacesException {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(requestURI);
        if (requestDispatcher == null) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            requestDispatcher.forward(request, response);
        } catch (ServletException se) {
            throw new FacesException(se);
        }
    }

    /**
     * @see ExternalContext#redirect(String)
     */
    @Override
    public void redirect(String requestURI) throws IOException {

        FacesContext ctx = FacesContext.getCurrentInstance();
        doLastPhaseActions(ctx, true);

        if (ctx.getPartialViewContext().isPartialRequest()) {
            if (response instanceof HttpServletResponse && ctx.getResponseComplete()) {
                throw new IllegalStateException();
            }
            PartialResponseWriter pwriter;
            ResponseWriter writer = ctx.getResponseWriter();
            if (writer instanceof PartialResponseWriter) {
                pwriter = (PartialResponseWriter) writer;
            } else {
                pwriter = ctx.getPartialViewContext().getPartialResponseWriter();
            }
            setResponseContentType(RIConstants.TEXT_XML_CONTENT_TYPE);
            setResponseCharacterEncoding("UTF-8");
            addResponseHeader("Cache-Control", "no-cache");
//            pwriter.writePreamble("<?xml version='1.0' encoding='UTF-8'?>\n");
            pwriter.startDocument();
            pwriter.redirect(requestURI);
            pwriter.endDocument();
        } else if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).sendRedirect(requestURI);
        } else {
            throw new IllegalStateException();
        }
        ctx.responseComplete();

    }

    /**
     * @see ExternalContext#log(String)
     */
    @Override
    public void log(String message) {
        if (null == message) {
            String msg = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "message");
            throw new NullPointerException(msg);
        }
        servletContext.log(message);
    }

    /**
     * @see ExternalContext#log(String, Throwable)
     */
    @Override
    public void log(String message, Throwable throwable) {
        if (null == message) {
            String msg = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "message");
            throw new NullPointerException(msg);
        }
        if (null == throwable) {
            String msg = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "throwable");
            throw new NullPointerException(msg);
        }
        servletContext.log(message, throwable);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getAuthType()
     */
    @Override
    public String getAuthType() {
        return ((HttpServletRequest) request).getAuthType();
    }

    /**
     * @see ExternalContext#getMimeType(String)
     */
    @Override
    public String getMimeType(String file) {

        String mimeType = servletContext.getMimeType(file);
        if (mimeType == null) {
            mimeType = getFallbackMimeType(file);
        }

        if (mimeType == null && LOGGER.isLoggable(Level.WARNING) && FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development)) {
            LOGGER.log(Level.WARNING, "faces.externalcontext.no.mime.type.found", new Object[] { file });
        }
        return mimeType;
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        return ((HttpServletRequest) request).getRemoteUser();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getUserPrincipal()
     */
    @Override
    public java.security.Principal getUserPrincipal() {
        return ((HttpServletRequest) request).getUserPrincipal();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#isUserInRole(String)
     */
    @Override
    public boolean isUserInRole(String role) {
        if (null == role) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "role");
            throw new NullPointerException(message);
        }
        return ((HttpServletRequest) request).isUserInRole(role);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#invalidateSession()
     */
    @Override
    public void invalidateSession() {

        HttpSession session = ((HttpServletRequest) request).getSession(false);
        if (session != null) {
            session.invalidate();
        }

    }

    /**
     * @see ExternalContext#addResponseCookie(String, String, java.util.Map)
     */
    @Override
    public void addResponseCookie(String name, String value, Map<String, Object> properties) {

        HttpServletResponse res = (HttpServletResponse) response;

        Cookie cookie = new Cookie(name, value);
        if (properties != null && properties.size() != 0) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                PREDEFINED_COOKIE_PROPERTIES p = PREDEFINED_COOKIE_PROPERTIES.of(key);
                Object v = entry.getValue();
                switch (p) {
                case domain:
                    cookie.setDomain((String) v);
                    break;
                case maxAge:
                    cookie.setMaxAge((Integer) v);
                    break;
                case path:
                    cookie.setPath((String) v);
                    break;
                case secure:
                    cookie.setSecure((Boolean) v);
                    break;
                case httpOnly:
                    cookie.setHttpOnly((Boolean) v);
                    break;
                default:
                    cookie.setAttribute(key, (String) v);
                    break;
                }
            }
        }
        res.addCookie(cookie);

    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResponseOutputStream()
     */
    @Override
    public OutputStream getResponseOutputStream() throws IOException {
        return response.getOutputStream();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResponseOutputWriter()
     */
    @Override
    public Writer getResponseOutputWriter() throws IOException {
        return response.getWriter();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestScheme()
     */
    @Override
    public String getRequestScheme() {
        return request.getScheme();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestServerName()
     */
    @Override
    public String getRequestServerName() {
        return request.getServerName();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getRequestServerPort()
     */
    @Override
    public int getRequestServerPort() {
        return request.getServerPort();
    }

    /**
     * @see ExternalContext#setResponseContentType(String)
     */
    @Override
    public void setResponseContentType(String contentType) {
        response.setContentType(contentType);
    }

    /**
     * @see ExternalContext#setResponseHeader(String, String)
     */
    @Override
    public void setResponseHeader(String name, String value) {
        ((HttpServletResponse) response).setHeader(name, value);
    }

    /**
     * @see ExternalContext#addResponseHeader(String, String)
     */
    @Override
    public void addResponseHeader(String name, String value) {
        ((HttpServletResponse) response).addHeader(name, value);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#setResponseBufferSize(int)
     */
    @Override
    public void setResponseBufferSize(int size) {
        response.setBufferSize(size);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#isResponseCommitted()
     */
    @Override
    public boolean isResponseCommitted() {
        return response.isCommitted();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#responseReset()
     */
    @Override
    public void responseReset() {
        response.reset();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#responseSendError(int, String)
     */
    @Override
    public void responseSendError(int statusCode, String message) throws IOException {
        if (message == null) {
            ((HttpServletResponse) response).sendError(statusCode);
        } else {
            ((HttpServletResponse) response).sendError(statusCode, message);
        }
    }

    /**
     * @see jakarta.faces.context.ExternalContext#setResponseStatus(int)
     */
    @Override
    public void setResponseStatus(int statusCode) {
        ((HttpServletResponse) response).setStatus(statusCode);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#responseFlushBuffer()
     */
    @Override
    public void responseFlushBuffer() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            doLastPhaseActions(facesContext, false);
        }

        response.flushBuffer();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#setResponseContentLength(int)
     */
    @Override
    public void setResponseContentLength(int length) {
        response.setContentLength(length);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#setResponseContentLengthLong(long)
     */
    @Override
    public void setResponseContentLengthLong(long length) {
        response.setContentLengthLong(length);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#setSessionMaxInactiveInterval(int)
     */
    @Override
    public void setSessionMaxInactiveInterval(int interval) {

        HttpSession session = ((HttpServletRequest) request).getSession();
        session.setMaxInactiveInterval(interval);
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getResponseBufferSize()
     */
    @Override
    public int getResponseBufferSize() {
        return response.getBufferSize();
    }

    /**
     * @see jakarta.faces.context.ExternalContext#getSessionMaxInactiveInterval() \
     */
    @Override
    public int getSessionMaxInactiveInterval() {

        HttpSession session = ((HttpServletRequest) request).getSession();
        return session.getMaxInactiveInterval();

    }

    /**
     * @see jakarta.faces.context.ExternalContext#isSecure()
     * @return boolean
     */
    @Override
    public boolean isSecure() {
        return request.isSecure();
    }

    @Override
    public String encodeBookmarkableURL(String baseUrl, Map<String, List<String>> parameters) {
        FacesContext context = FacesContext.getCurrentInstance();
        String encodingFromContext = (String) context.getAttributes().get(RIConstants.FACELETS_ENCODING_KEY);
        if (null == encodingFromContext) {
            encodingFromContext = (String) context.getViewRoot().getAttributes().get(RIConstants.FACELETS_ENCODING_KEY);
        }

        String currentResponseEncoding = null != encodingFromContext ? encodingFromContext : getResponseCharacterEncoding();

        UrlBuilder builder = new UrlBuilder(baseUrl, currentResponseEncoding);
        builder.addParameters(parameters);
        return builder.createUrl();

    }

    @Override
    public String encodeRedirectURL(String baseUrl, Map<String, List<String>> parameters) {
        FacesContext context = FacesContext.getCurrentInstance();
        String encodingFromContext = (String) context.getAttributes().get(RIConstants.FACELETS_ENCODING_KEY);
        if (null == encodingFromContext) {
            encodingFromContext = (String) context.getViewRoot().getAttributes().get(RIConstants.FACELETS_ENCODING_KEY);
        }

        String currentResponseEncoding = null != encodingFromContext ? encodingFromContext : getResponseCharacterEncoding();

        UrlBuilder builder = new UrlBuilder(baseUrl, currentResponseEncoding);
        builder.addParameters(parameters);
        return builder.createUrl();

    }

    /**
     * @see jakarta.faces.context.ExternalContext#encodePartialActionURL(String)
     */
    @Override
    public String encodePartialActionURL(String url) {
        if (null == url) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "url");
            throw new NullPointerException(message);
        }
        FacesContext context = FacesContext.getCurrentInstance();
        String encodingFromContext = (String) context.getAttributes().get(RIConstants.FACELETS_ENCODING_KEY);
        if (null == encodingFromContext) {
            encodingFromContext = (String) context.getViewRoot().getAttributes().get(RIConstants.FACELETS_ENCODING_KEY);
        }

        String currentResponseEncoding = null != encodingFromContext ? encodingFromContext : getResponseCharacterEncoding();

        UrlBuilder builder = new UrlBuilder(url, currentResponseEncoding);
        return ((HttpServletResponse) response).encodeURL(builder.createUrl());
    }

    @Override
    public Flash getFlash() {
        if (null == flash) {
            FlashFactory ff = (FlashFactory) FactoryFinder.getFactory(FactoryFinder.FLASH_FACTORY);
            flash = ff.getFlash(true);
        }
        return flash;
    }

    @Override
    public void release() {
        servletContext = null;
        request = null;
        response = null;
        clientWindow = null;

        applicationMap = null;
        sessionMap = null;
        requestMap = null;
        requestParameterMap = null;
        requestParameterValuesMap = null;
        requestHeaderMap = null;
        requestHeaderValuesMap = null;
        cookieMap = null;
        initParameterMap = null;

        flash = null;
    }

    private void pushIfPossibleAndNecessary(String result) {
        FacesContext context = FacesContext.getCurrentInstance();

        // 1. Don't bother trying to push if we've already pushed this URL for this request
        @SuppressWarnings("unchecked")
        Set<String> resourceUrls = (Set<String>) context.getAttributes().computeIfAbsent(PUSH_RESOURCE_URLS_KEY_NAME,
                k -> new HashSet<>());

        if (resourceUrls.contains(result)) {
            return;
        }
        resourceUrls.add(result);

        // 2. At this point we know we haven't pushed this URL for this request before
        PushBuilder pushBuilder = getPushBuilder(context);
        if (pushBuilder != null) {
            // and now we also know there was no If-Modified-Since header
            pushBuilder.path(result).push();
        }

    }

    private PushBuilder getPushBuilder(FacesContext context) {
        ExternalContext extContext = context.getExternalContext();
        
        if (extContext.getRequest() instanceof HttpServletRequest) {
            HttpServletRequest hreq = (HttpServletRequest) extContext.getRequest();

            if (isEmpty(extContext.getRequestHeaderMap().get("If-Modified-Since"))) {
                return hreq.newPushBuilder();
            }
        }

        return null;
    }

    private void doLastPhaseActions(FacesContext context, boolean outgoingResponseIsRedirect) {
        Map<Object, Object> attrs = context.getAttributes();
        try {
            attrs.put(ELFlash.ACT_AS_DO_LAST_PHASE_ACTIONS, outgoingResponseIsRedirect);
            getFlash().doPostPhaseActions(context);
        } finally {
            attrs.remove(ELFlash.ACT_AS_DO_LAST_PHASE_ACTIONS);
        }

    }

    // --------------------------------------------------------- Private Methods

    private static String getFallbackMimeType(String file) {
        final String extension = Util.fileExtension(file);
        return extension != null ? fallbackContentTypeMap.get(extension) : null;
    }

    // ----------------------------------------------------------- Inner Classes


}
