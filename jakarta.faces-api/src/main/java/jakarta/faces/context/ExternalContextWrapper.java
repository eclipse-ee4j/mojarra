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

package jakarta.faces.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesWrapper;
import jakarta.faces.lifecycle.ClientWindow;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2 changed_modified_2_3">Provides</span> a simple implementation of
 * {@link ExternalContext} that can be subclassed by developers wishing to provide specialized behavior to an existing
 * {@link ExternalContext} instance. The default implementation of all methods is to call through to the wrapped
 * {@link ExternalContext} instance.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class ExternalContextWrapper extends ExternalContext implements FacesWrapper<ExternalContext> {

    private ExternalContext wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ExternalContextWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this external context has been decorated, the implementation doing the decorating should push the implementation
     * being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ExternalContextWrapper(ExternalContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExternalContext getWrapped() {
        return wrapped;
    }

    // -------------------------------------------- Methods from ExternalContext

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#dispatch(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#dispatch(String)
     */
    @Override
    public void dispatch(String path) throws IOException {
        getWrapped().dispatch(path);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#encodeActionURL(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodeActionURL(String)
     */
    @Override
    public String encodeActionURL(String url) {
        return getWrapped().encodeActionURL(url);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#encodeNamespace(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodeNamespace(String)
     */
    @Override
    public String encodeNamespace(String name) {
        return getWrapped().encodeNamespace(name);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#encodePartialActionURL(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodePartialActionURL(String)
     */
    @Override
    public String encodePartialActionURL(String url) {
        return getWrapped().encodePartialActionURL(url);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#encodeResourceURL(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodeResourceURL(String)
     */
    @Override
    public String encodeResourceURL(String url) {
        return getWrapped().encodeResourceURL(url);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#encodeWebsocketURL(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodeWebsocketURL(String)
     */
    @Override
    public String encodeWebsocketURL(String url) {
        return getWrapped().encodeWebsocketURL(url);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getApplicationMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getApplicationMap()
     */
    @Override
    public Map<String, Object> getApplicationMap() {
        return getWrapped().getApplicationMap();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#getApplicationContextPath} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getApplicationContextPath()
     */
    @Override
    public String getApplicationContextPath() {
        return getWrapped().getApplicationContextPath();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getAuthType} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getAuthType()
     */
    @Override
    public String getAuthType() {
        return getWrapped().getAuthType();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getContext} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getContext()
     */
    @Override
    public Object getContext() {
        return getWrapped().getContext();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getInitParameter(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getInitParameter(String)
     */
    @Override
    public String getInitParameter(String name) {
        return getWrapped().getInitParameter(name);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getInitParameterMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getInitParameterMap()
     */
    @Override
    public Map<String, String> getInitParameterMap() {
        return getWrapped().getInitParameterMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRemoteUser} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        return getWrapped().getRemoteUser();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequest} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequest()
     */
    @Override
    public Object getRequest() {
        return getWrapped().getRequest();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestContextPath} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestContextPath()
     */
    @Override
    public String getRequestContextPath() {
        return getWrapped().getRequestContextPath();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestCookieMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestCookieMap()
     */
    @Override
    public Map<String, Object> getRequestCookieMap() {
        return getWrapped().getRequestCookieMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestHeaderMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestHeaderMap()
     */
    @Override
    public Map<String, String> getRequestHeaderMap() {
        return getWrapped().getRequestHeaderMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestHeaderValuesMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestHeaderValuesMap()
     */
    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
        return getWrapped().getRequestHeaderValuesMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestLocale} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestLocale()
     */
    @Override
    public Locale getRequestLocale() {
        return getWrapped().getRequestLocale();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestLocales} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestLocales()
     */
    @Override
    public Iterator<Locale> getRequestLocales() {
        return getWrapped().getRequestLocales();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestMap()
     */
    @Override
    public Map<String, Object> getRequestMap() {
        return getWrapped().getRequestMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestParameterMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestParameterMap()
     */
    @Override
    public Map<String, String> getRequestParameterMap() {
        return getWrapped().getRequestParameterMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestParameterNames} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestParameterNames()
     */
    @Override
    public Iterator<String> getRequestParameterNames() {
        return getWrapped().getRequestParameterNames();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestParameterValuesMap} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestParameterValuesMap()
     */
    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
        return getWrapped().getRequestParameterValuesMap();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestPathInfo} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestPathInfo()
     */
    @Override
    public String getRequestPathInfo() {
        return getWrapped().getRequestPathInfo();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestServletPath} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestServletPath()
     */
    @Override
    public String getRequestServletPath() {
        return getWrapped().getRequestServletPath();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResource(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResource(String)
     */
    @Override
    public URL getResource(String path) throws MalformedURLException {
        return getWrapped().getResource(path);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResourceAsStream(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResourceAsStream(String)
     */
    @Override
    public InputStream getResourceAsStream(String path) {
        return getWrapped().getResourceAsStream(path);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResourcePaths(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResourcePaths(String)
     */
    @Override
    public Set<String> getResourcePaths(String path) {
        return getWrapped().getResourcePaths(path);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponse} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponse()
     */
    @Override
    public Object getResponse() {
        return getWrapped().getResponse();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getSession(boolean)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getSession(boolean)
     */
    @Override
    public Object getSession(boolean create) {
        return getWrapped().getSession(create);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#getSessionId(boolean)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @since 2.2
     *
     * @see jakarta.faces.context.ExternalContext#getSessionId(boolean)
     */
    @Override
    public String getSessionId(boolean create) {
        return getWrapped().getSessionId(create);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getSessionMap()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getSessionMap()
     */
    @Override
    public Map<String, Object> getSessionMap() {
        return getWrapped().getSessionMap();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#getSessionMaxInactiveInterval()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getSessionMaxInactiveInterval()
     */
    @Override
    public int getSessionMaxInactiveInterval() {
        return getWrapped().getSessionMaxInactiveInterval();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#setSessionMaxInactiveInterval(int)} on the
     * wrapped {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setSessionMaxInactiveInterval(int)
     */
    @Override
    public void setSessionMaxInactiveInterval(int interval) {
        getWrapped().setSessionMaxInactiveInterval(interval);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#setClientWindow} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @since 2.2
     *
     * @param window the window associated with this request.
     */

    @Override
    public void setClientWindow(ClientWindow window) {
        getWrapped().setClientWindow(window);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getUserPrincipal} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        return getWrapped().getUserPrincipal();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#getClientWindow} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @since 2.2
     *
     * @see jakarta.faces.context.ExternalContext#getClientWindow()
     */
    @Override
    public ClientWindow getClientWindow() {
        return getWrapped().getClientWindow();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#isUserInRole(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#isUserInRole(String)
     */
    @Override
    public boolean isUserInRole(String role) {
        return getWrapped().isUserInRole(role);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#log(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#log(String)
     */
    @Override
    public void log(String message) {
        getWrapped().log(message);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#log(String, Throwable)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#log(String, Throwable)
     */
    @Override
    public void log(String message, Throwable exception) {
        getWrapped().log(message, exception);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#redirect(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#redirect(String)
     */
    @Override
    public void redirect(String url) throws IOException {
        getWrapped().redirect(url);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#addResponseCookie(String, String, Map)} on the
     * wrapped {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#addResponseCookie(String, String, Map)
     */
    @Override
    public void addResponseCookie(String name, String value, Map<String, Object> properties) {
        getWrapped().addResponseCookie(name, value, properties);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getMimeType(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getMimeType(String)
     */
    @Override
    public String getMimeType(String file) {
        return getWrapped().getMimeType(file);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getContextName} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getContextName()
     */
    @Override
    public String getContextName() {
        return getWrapped().getContextName();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setRequest(Object)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setRequest(Object)
     */
    @Override
    public void setRequest(Object request) {
        getWrapped().setRequest(request);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestScheme} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestScheme()
     */
    @Override
    public String getRequestScheme() {
        return getWrapped().getRequestScheme();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestServerName} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestServerName()
     */
    @Override
    public String getRequestServerName() {
        return getWrapped().getRequestServerName();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestServerPort} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestServerPort()
     */
    @Override
    public int getRequestServerPort() {
        return getWrapped().getRequestServerPort();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setRequestCharacterEncoding(String)} on the
     * wrapped {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setRequestCharacterEncoding(String)
     */
    @Override
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        getWrapped().setRequestCharacterEncoding(encoding);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRealPath(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRealPath(String)
     */
    @Override
    public String getRealPath(String path) {
        return getWrapped().getRealPath(path);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestCharacterEncoding} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestCharacterEncoding()
     */
    @Override
    public String getRequestCharacterEncoding() {
        return getWrapped().getRequestCharacterEncoding();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestContentType} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestContentType()
     */
    @Override
    public String getRequestContentType() {
        return getWrapped().getRequestContentType();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getRequestContentLength} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getRequestContentLength()
     */
    @Override
    public int getRequestContentLength() {
        return getWrapped().getRequestContentLength();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponseCharacterEncoding} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponseCharacterEncoding()
     */
    @Override
    public String getResponseCharacterEncoding() {
        return getWrapped().getResponseCharacterEncoding();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponseContentType} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponseContentType()
     */
    @Override
    public String getResponseContentType() {
        return getWrapped().getResponseContentType();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setResponse(Object)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponse(Object)
     */
    @Override
    public void setResponse(Object response) {
        getWrapped().setResponse(response);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponseOutputStream} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponseOutputStream()
     */
    @Override
    public OutputStream getResponseOutputStream() throws IOException {
        return getWrapped().getResponseOutputStream();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponseOutputWriter} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponseOutputWriter()
     */
    @Override
    public Writer getResponseOutputWriter() throws IOException {
        return getWrapped().getResponseOutputWriter();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponseCharacterEncoding} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponseCharacterEncoding()
     */
    @Override
    public void setResponseCharacterEncoding(String encoding) {
        getWrapped().setResponseCharacterEncoding(encoding);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setResponseContentType(String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponseContentType(String)
     */
    @Override
    public void setResponseContentType(String contentType) {
        getWrapped().setResponseContentType(contentType);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#invalidateSession} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#invalidateSession()
     */
    @Override
    public void invalidateSession() {
        getWrapped().invalidateSession();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setResponseHeader(String,String)} on the
     * wrapped {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponseHeader(String,String)
     */
    @Override
    public void setResponseHeader(String name, String value) {
        getWrapped().setResponseHeader(name, value);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#addResponseHeader(String,String)} on the
     * wrapped {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#addResponseHeader(String,String)
     */
    @Override
    public void addResponseHeader(String name, String value) {
        getWrapped().addResponseHeader(name, value);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setResponseBufferSize(int)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponseBufferSize(int)
     */
    @Override
    public void setResponseBufferSize(int size) {
        getWrapped().setResponseBufferSize(size);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getResponseBufferSize()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getResponseBufferSize()
     */
    @Override
    public int getResponseBufferSize() {
        return getWrapped().getResponseBufferSize();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#isResponseCommitted()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#isResponseCommitted()
     */
    @Override
    public boolean isResponseCommitted() {
        return getWrapped().isResponseCommitted();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ExternalContext#isSecure} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     */
    @Override
    public boolean isSecure() {
        return getWrapped().isSecure();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#responseReset()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#responseReset()
     */
    @Override
    public void responseReset() {
        getWrapped().responseReset();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#responseSendError(int,String)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#responseSendError(int,String)
     */
    @Override
    public void responseSendError(int statusCode, String message) throws IOException {
        getWrapped().responseSendError(statusCode, message);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#setResponseStatus(int)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponseStatus(int)
     */
    @Override
    public void setResponseStatus(int statusCode) {
        getWrapped().setResponseStatus(statusCode);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.context.ExternalContext#responseFlushBuffer()} on
     * the wrapped {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#responseFlushBuffer()
     */
    @Override
    public void responseFlushBuffer() throws IOException {
        getWrapped().responseFlushBuffer();
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link jakarta.faces.context.ExternalContext#setResponseContentLength(int)} on the wrapped {@link ExternalContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponseContentLength(int)
     */
    @Override
    public void setResponseContentLength(int length) {
        getWrapped().setResponseContentLength(length);
    }

    /**
     * <p class="changed_added_4_1">
     * The default behavior of this method is to call
     * {@link jakarta.faces.context.ExternalContext#setResponseContentLengthLong(long)} on the wrapped {@link ExternalContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#setResponseContentLengthLong(long)
     */
    @Override
    public void setResponseContentLengthLong(long length) {
        getWrapped().setResponseContentLengthLong(length);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link jakarta.faces.context.ExternalContext#encodeBookmarkableURL(String, java.util.Map)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodeBookmarkableURL(String, java.util.Map)
     */
    @Override
    public String encodeBookmarkableURL(String baseUrl, Map<String, List<String>> parameters) {
        return getWrapped().encodeBookmarkableURL(baseUrl, parameters);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link jakarta.faces.context.ExternalContext#encodeRedirectURL(String, java.util.Map)} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#encodeRedirectURL(String, java.util.Map)
     */
    @Override
    public String encodeRedirectURL(String baseUrl, Map<String, List<String>> parameters) {
        return getWrapped().encodeRedirectURL(baseUrl, parameters);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#getFlash()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#getFlash()
     */
    @Override
    public Flash getFlash() {
        return getWrapped().getFlash();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ExternalContext#release()} on the wrapped
     * {@link ExternalContext} object.
     * </p>
     *
     * @see jakarta.faces.context.ExternalContext#release()
     */
    @Override
    public void release() {
        getWrapped().release();
    }
}
