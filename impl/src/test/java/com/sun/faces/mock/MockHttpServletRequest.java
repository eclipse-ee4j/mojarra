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

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

// Mock Object for HttpServletRequest (Version 2.4)
public class MockHttpServletRequest implements HttpServletRequest {

    public MockHttpServletRequest() {
        super();
    }

    public MockHttpServletRequest(HttpSession session) {
        super();
        setHttpSession(session);
    }

    public MockHttpServletRequest(String contextPath, String servletPath,
            String pathInfo, String queryString) {
        super();
        setPathElements(contextPath, servletPath, pathInfo, queryString);
    }

    public MockHttpServletRequest(String contextPath, String servletPath,
            String pathInfo, String queryString,
            HttpSession session) {
        super();
        setPathElements(contextPath, servletPath, pathInfo, queryString);
        setHttpSession(session);
    }

    protected HashMap<String, Object> attributes = new HashMap<>();
    protected String contextPath = null;
    protected Locale locale = null;
    protected HashMap<String, String[]> parameters = new HashMap<>();
    protected String pathInfo = null;
    protected Principal principal = null;
    protected String queryString = null;
    protected String servletPath = null;
    protected HttpSession session = null;
    protected String method = "GET";

    // --------------------------------------------------------- Public Methods
    public void addParameter(String name, String value) {
        String values[] = parameters.get(name);
        if (values == null) {
            String results[] = new String[]{value};
            parameters.put(name, results);
            return;
        }
        String results[] = new String[values.length + 1];
        System.arraycopy(values, 0, results, 0, values.length);
        results[values.length] = value;
        parameters.put(name, results);
    }

    public void setHttpSession(HttpSession session) {
        this.session = session;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPathElements(String contextPath, String servletPath,
            String pathInfo, String queryString) {

        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;

    }

    public void setUserPrincipal(Principal principal) {
        this.principal = principal;
    }

    // --------------------------------------------- HttpServletRequest Methods
    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        return (contextPath);
    }

    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDateHeader(String name) {
        return -1;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(Collections.emptyList());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(Collections.emptyList());
    }

    @Override
    public int getIntHeader(String name) {
        return -1;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return (pathInfo);
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        return (queryString);
    }

    @Override
    public String getRemoteUser() {
        if (principal != null) {
            return (principal.getName());
        } else {
            return (null);
        }
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        StringBuilder sb = new StringBuilder();
        if (contextPath != null) {
            sb.append(contextPath);
        }
        if (servletPath != null) {
            sb.append(servletPath);
        }
        if (pathInfo != null) {
            sb.append(pathInfo);
        }
        if (sb.length() > 0) {
            return (sb.toString());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServletPath() {
        return (servletPath);
    }

    @Override
    public HttpSession getSession() {
        return (getSession(true));
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (create && (session == null)) {
            throw new UnsupportedOperationException();
        }
        return (session);
    }

    @Override
    public Principal getUserPrincipal() {
        return (principal);
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        if ((principal != null) && (principal instanceof MockPrincipal)) {
            return (((MockPrincipal) principal).isUserInRole(role));
        } else {
            return (false);
        }
    }

    // ------------------------------------------------- ServletRequest Methods
    @Override
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return (new MockEnumeration<>(attributes.keySet().iterator()));
    }

    @Override
    public String getCharacterEncoding() {
        return "ISO-8859-1";
    }

    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public ServletInputStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        return (locale);
    }

    //
    // Servlet 2.4 methods
    //
    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        if (values != null) {
            return (values[0]);
        } else {
            return (null);
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return (parameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return (new MockEnumeration<>(parameters.keySet().iterator()));
    }

    @Override
    public String[] getParameterValues(String name) {
        return (parameters.get(name));
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedReader getReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScheme() {
        return ("http");
    }

    @Override
    public String getServerName() {
        return ("localhost");
    }

    @Override
    public int getServerPort() {
        return (8080);
    }

    @Override
    public boolean isSecure() {
        return (false);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    @Override
    public void setCharacterEncoding(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse hsr) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void login(String string, String string1) throws ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Part getPart(String string) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getContentLengthLong() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRequestId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getProtocolRequestId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServletConnection getServletConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
