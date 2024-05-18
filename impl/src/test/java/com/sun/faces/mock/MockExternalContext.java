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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;


public class MockExternalContext extends ExternalContext {

    public MockExternalContext(ServletContext context,
                               ServletRequest request,
                               ServletResponse response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }

    private ServletContext context = null;
    private ServletRequest request = null;
    private ServletResponse response = null;
    private Map<String,String> initParams;

    @Override
    public Object getSession(boolean create) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getContext() {
        return (context);
    }

    @Override
    public String getContextName() {
	return context.getServletContextName();
    }

    @Override
    public String getApplicationContextPath() {
        return context.getContextPath();
    }

    @Override
    public Object getRequest() {
        return (request);
    }

    @Override
    public void setRequest(Object request) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object getResponse() {
        return (response);
    }

    @Override
    public void setResponse(Object response) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
	throw new UnsupportedOperationException();
    }

    private Map<String, Object> applicationMap = null;
    @Override
    public Map<String, Object> getApplicationMap() {
        if (applicationMap == null) {
            applicationMap = new MockApplicationMap(context);
        }
        return applicationMap;
    }

    private Map<String, Object> sessionMap = null;
    @Override
    public Map<String, Object> getSessionMap() {
        if (sessionMap == null) {
            sessionMap = new MockSessionMap
                (((HttpServletRequest) request).getSession(true));
        }
        return sessionMap;
    }

    private Map<String, Object> requestMap = null;
    @Override
    public Map<String, Object> getRequestMap() {
        if (requestMap == null) {
            requestMap = new MockRequestMap(request);
        }
        return requestMap;
    }

    private Map<String, String> requestParameterMap = null;
    @Override
    public Map<String, String> getRequestParameterMap() {
        if (requestParameterMap != null) {
            return requestParameterMap;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void setRequestParameterMap(Map<String, String> requestParameterMap) {
        this.requestParameterMap = requestParameterMap;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getRequestParameterNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getRequestCookieMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getRequestLocale() {
        return (request.getLocale());
    }

    @Override
    public Iterator<Locale> getRequestLocales() {
        return (new LocalesIterator(request.getLocales()));
    }

    @Override
    public String getRequestPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestContextPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestServletPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRequestContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResponseCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResponseContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInitParameter(String name) {
        if (name
              .equals(jakarta.faces.application.StateManager.STATE_SAVING_METHOD_PARAM_NAME)) {
            return null;
        }
        if (name.equals(jakarta.faces.webapp.FacesServlet.LIFECYCLE_ID_ATTR)) {
            return null;
        }
        return ((initParams == null) ? null : initParams.get(name));
    }

    public void addInitParameter(String name, String value) {
        if (initParams == null) {
            initParams = new HashMap<>();
        }
        initParams.put(name, value);
    }

    @Override
    public void addResponseHeader(String arg0, String arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setResponseHeader(String arg0, String arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSessionMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getInitParameterMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getResourcePaths(String path) {
       return context.getResourcePaths(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSessionMaxInactiveInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeActionURL(String sb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeResourceURL(String sb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeWebsocketURL(String url) {
        return null;
    }

    @Override
    public String encodeNamespace(String aValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispatch(String requestURI)
        throws IOException, FacesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void redirect(String requestURI)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String message) {
        context.log(message);
    }

    @Override
    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }

    @Override
    public String getAuthType() {
        return (((HttpServletRequest) request).getAuthType());
    }

    @Override
    public String getRemoteUser() {
        return (((HttpServletRequest) request).getRemoteUser());
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return (((HttpServletRequest) request).getUserPrincipal());
    }

    @Override
    public boolean isUserInRole(String role) {
        return (((HttpServletRequest) request).isUserInRole(role));
    }

    @Override
    public void release() {
    }

    private class LocalesIterator implements Iterator<Locale> {
        public LocalesIterator(Enumeration<Locale> locales) {
            this.locales = locales;
        }

        private Enumeration<Locale> locales;

        @Override
        public boolean hasNext() {
            return locales.hasMoreElements();
        }

        @Override
        public Locale next() {
            return locales.nextElement();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
