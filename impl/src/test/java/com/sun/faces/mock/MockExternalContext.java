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
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;



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


    public Object getSession(boolean create) {
        throw new UnsupportedOperationException();
    }
    

    public Object getContext() {
        return (context);
    }

    public String getContextName() { 
	return context.getServletContextName();
    }

    @Override
    public String getApplicationContextPath() {
        return context.getContextPath();
    }
    
    
    public Object getRequest() {
        return (request);
    }

    public void setRequest(Object request) {
	throw new UnsupportedOperationException();
    }

    public Object getResponse() {
        return (response);
    }

    public void setResponse(Object response) {
	throw new UnsupportedOperationException();
    }

    public void setResponseCharacterEncoding(String encoding) {
	throw new UnsupportedOperationException();
    }

    private Map applicationMap = null;
    public Map getApplicationMap() {
        if (applicationMap == null) {
            applicationMap = new MockApplicationMap(context);
        }
        return (applicationMap);
    }

    private Map sessionMap = null;
    public Map getSessionMap() {
        if (sessionMap == null) {
            sessionMap = new MockSessionMap
                (((HttpServletRequest) request).getSession(true));
        }
        return (sessionMap);
    }
    

    private Map requestMap = null;
    public Map getRequestMap() {
        if (requestMap == null) {
            requestMap = new MockRequestMap(request);
        }
        return (requestMap);
    }
    

    private Map requestParameterMap = null;
    public Map getRequestParameterMap() {
        if (requestParameterMap != null) {
            return (requestParameterMap);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    public void setRequestParameterMap(Map requestParameterMap) {
        this.requestParameterMap = requestParameterMap;
    }

    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }
    

    public Map getRequestParameterValuesMap() {
        throw new UnsupportedOperationException();        
    }

    
    public Iterator getRequestParameterNames() {
        throw new UnsupportedOperationException();
    }

    
    public Map getRequestHeaderMap() {
        throw new UnsupportedOperationException();
    }


    public Map getRequestHeaderValuesMap() {
        throw new UnsupportedOperationException();
    }


    public Map getRequestCookieMap() {
        throw new UnsupportedOperationException();
    }


    public Locale getRequestLocale() {
        return (request.getLocale());
    }
    

    public Iterator getRequestLocales() {
        return (new LocalesIterator(request.getLocales()));
    }
    

    public String getRequestPathInfo() {
        throw new UnsupportedOperationException();
    }


    public String getRequestContextPath() {
        throw new UnsupportedOperationException();
    }

    public String getRequestServletPath() {
        throw new UnsupportedOperationException();
    }
    
    public String getRequestCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    
    public String getRequestContentType() {
        throw new UnsupportedOperationException();
    }

    public int getRequestContentLength() {
        throw new UnsupportedOperationException();
    }


    public String getResponseCharacterEncoding() {
        throw new UnsupportedOperationException();
    }
    
    public String getResponseContentType() {
        throw new UnsupportedOperationException();
    }


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
            initParams = new HashMap<String,String>();
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

    public Map getInitParameterMap() {
        throw new UnsupportedOperationException();
    }


    public Set getResourcePaths(String path) {
       return context.getResourcePaths(path);
    }


    public URL getResource(String path) throws MalformedURLException {
        throw new UnsupportedOperationException();
    }


    public InputStream getResourceAsStream(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSessionMaxInactiveInterval() {
        throw new UnsupportedOperationException();
    }

    public String encodeActionURL(String sb) {
        throw new UnsupportedOperationException();
    }


    public String encodeResourceURL(String sb) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String encodeWebsocketURL(String url) {
        return null;
    }


    public String encodeNamespace(String aValue) {
        throw new UnsupportedOperationException();
    }


    public void dispatch(String requestURI)
        throws IOException, FacesException {
        throw new UnsupportedOperationException();
    }

    
    public void redirect(String requestURI)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    
    public void log(String message) {
        context.log(message);
    }


    public void log(String message, Throwable throwable) {
        context.log(message, throwable);
    }


    public String getAuthType() {
        return (((HttpServletRequest) request).getAuthType());
    }

    public String getRemoteUser() {
        return (((HttpServletRequest) request).getRemoteUser());
    }



    public java.security.Principal getUserPrincipal() {
        return (((HttpServletRequest) request).getUserPrincipal());
    }

    public boolean isUserInRole(String role) {
        return (((HttpServletRequest) request).isUserInRole(role));
    }

    @Override
    public void release() {

    }


    private class LocalesIterator implements Iterator {

	public LocalesIterator(Enumeration locales) {
	    this.locales = locales;
	}

	private Enumeration locales;

	public boolean hasNext() { return locales.hasMoreElements(); }

	public Object next() { return locales.nextElement(); }

	public void remove() { throw new UnsupportedOperationException(); }

    }


}
