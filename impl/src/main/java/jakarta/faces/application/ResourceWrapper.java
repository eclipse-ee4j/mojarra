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

package jakarta.faces.application;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2 changed_modified_2_3">Provides</span> a simple implementation of {@link Resource}
 * that can be subclassed by developers wishing to provide specialized behavior to an existing {@link Resource}
 * instance. The default implementation of all methods is to call through to the wrapped {@link Resource}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class ResourceWrapper extends Resource implements FacesWrapper<Resource> {

    private Resource wrapped;

    /**
     * <p class="changed_added_2_3">
     * If this resource has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ResourceWrapper(Resource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Resource getWrapped() {
        return wrapped;
    }

    // --------------------------------------------------- Methods from Resource

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getInputStream} on the wrapped {@link ResourceHandler}
     * object.
     * </p>
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return getWrapped().getInputStream();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getURL} on the wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public URL getURL() {
        return getWrapped().getURL();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getResponseHeaders} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public Map<String, String> getResponseHeaders() {
        return getWrapped().getResponseHeaders();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getRequestPath} on the wrapped {@link ResourceHandler}
     * object.
     * </p>
     */
    @Override
    public String getRequestPath() {
        return getWrapped().getRequestPath();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#userAgentNeedsUpdate} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public boolean userAgentNeedsUpdate(FacesContext context) {
        return getWrapped().userAgentNeedsUpdate(context);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#getContentType()} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getContentType() {
        return getWrapped().getContentType();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#setContentType(String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void setContentType(String contentType) {
        getWrapped().setContentType(contentType);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#getLibraryName()} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getLibraryName() {
        return getWrapped().getLibraryName();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#setLibraryName(String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void setLibraryName(String libraryName) {
        getWrapped().setLibraryName(libraryName);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#getResourceName()} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getResourceName() {
        return getWrapped().getResourceName();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#setResourceName(String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void setResourceName(String resourceName) {
        getWrapped().setResourceName(resourceName);
    }

    // --------------------------------------------------- Deprecated methods

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ResourceWrapper() {

    }

}
