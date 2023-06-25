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

package com.sun.faces.config.initfacescontext;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableTransitionTimeNoOpFlash;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.sun.faces.context.ApplicationMap;
import com.sun.faces.context.InitParameterMap;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.Flash;
import jakarta.servlet.ServletContext;

public class ServletContextAdapter extends ExternalContext {

    private ServletContext servletContext;
    private ApplicationMap applicationMap;
    private InitParameterMap initMap;
    private boolean isEnableTransitionTimeNoOpFlash = EnableTransitionTimeNoOpFlash.getDefaultValue();

    public ServletContextAdapter(ServletContext servletContext) {
        this.servletContext = servletContext;

        String paramValue = servletContext.getInitParameter(EnableTransitionTimeNoOpFlash.getQualifiedName());

        if (paramValue != null) {
            isEnableTransitionTimeNoOpFlash = parseBoolean(paramValue);
        }
    }

    @Override
    public void dispatch(String path) throws IOException {
    }

    public void release() {
        servletContext = null;
        applicationMap = null;
        initMap = null;
    }

    @Override
    public String encodeActionURL(String url) {
        return null;
    }

    @Override
    public String encodeNamespace(String name) {
        return null;
    }

    @Override
    public String encodeResourceURL(String url) {
        return null;
    }

    @Override
    public String encodeWebsocketURL(String url) {
        return null;
    }

    @Override
    public Map<String, Object> getApplicationMap() {
        if (applicationMap == null) {
            applicationMap = new ApplicationMap(servletContext);
        }

        return applicationMap;
    }

    @Override
    public Flash getFlash() {
        if (isEnableTransitionTimeNoOpFlash) {
            return new NoOpFlash();
        } else {
            return super.getFlash();
        }

    }

    @Override
    public String getApplicationContextPath() {
        return servletContext.getContextPath();
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getMimeType(String file) {
        return servletContext.getMimeType(file);
    }

    @Override
    public Object getContext() {
        return servletContext;
    }

    @Override
    public String getContextName() {
        return servletContext.getServletContextName();
    }

    @Override
    public String getInitParameter(String name) {
        return servletContext.getInitParameter(name);
    }

    @Override
    public Map<String, String> getInitParameterMap() {
        if (initMap == null) {
            initMap = new InitParameterMap(servletContext);
        }

        return initMap;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public void setRequest(Object request) {
    }

    @Override
    public String getRequestContextPath() {
        return null;
    }

    @Override
    public Map<String, Object> getRequestCookieMap() {
        return emptyMap();
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        return emptyMap();
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
        return emptyMap();
    }

    @Override
    public Locale getRequestLocale() {
        return null;
    }

    @Override
    public Iterator<Locale> getRequestLocales() {
        return null;
    }

    @Override
    public Map<String, Object> getRequestMap() {
        return emptyMap();
    }

    @Override
    public Map<String, String> getRequestParameterMap() {
        return emptyMap();
    }

    @Override
    public Iterator<String> getRequestParameterNames() {
        return emptyIterator();
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
        return emptyMap();
    }

    @Override
    public String getRequestPathInfo() {
        return null;
    }

    @Override
    public String getRequestServletPath() {
        return null;
    }

    @Override
    public String getRequestContentType() {
        return null;
    }

    @Override
    public String getResponseContentType() {
        return null;
    }

    @Override
    public int getRequestContentLength() {
        return -1;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return servletContext.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return servletContext.getResourceAsStream(path);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return servletContext.getResourcePaths(path);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public void setResponse(Object response) {
    }

    @Override
    public Object getSession(boolean create) {
        return null;
    }

    @Override
    public Map<String, Object> getSessionMap() {
        return emptyMap();
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public void log(String message) {
        servletContext.log(message);
    }

    @Override
    public void log(String message, Throwable exception) {
        servletContext.log(message, exception);
    }

    @Override
    public void redirect(String url) throws IOException {
    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String requestCharacterEncoding) throws UnsupportedEncodingException {
    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String responseCharacterEncoding) {
    }

    @Override
    public void setResponseHeader(String name, String value) {
    }

    @Override
    public void addResponseHeader(String name, String value) {
    }

    @Override
    public String encodePartialActionURL(String url) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

}
