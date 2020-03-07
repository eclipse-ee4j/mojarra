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
import java.util.stream.Stream;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_0_rev_a changed_modified_2_2 changed_modified_2_3">Provides</span> a simple
 * implementation of {@link ResourceHandler} that can be subclassed by developers wishing to provide specialized
 * behavior to an existing {@link ResourceHandler} instance. The default implementation of all methods is to call
 * through to the wrapped {@link ResourceHandler}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class ResourceHandlerWrapper extends ResourceHandler implements FacesWrapper<ResourceHandler> {

    private ResourceHandler wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ResourceHandlerWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this resource handler has been decorated, the implementation doing the decorating should push the implementation
     * being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ResourceHandlerWrapper(ResourceHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ResourceHandler getWrapped() {
        return wrapped;
    }

    // -------------------------------------------- Methods from ResourceHandler

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResourceHandler#createResource(String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public Resource createResource(String resourceName) {
        return getWrapped().createResource(resourceName);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ResourceHandler#createResourceFromId(String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Resource createResourceFromId(String resourceId) {
        return getWrapped().createResourceFromId(resourceId);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResourceHandler#createResource(String, String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public Resource createResource(String resourceName, String libraryName) {
        return getWrapped().createResource(resourceName, libraryName);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link ResourceHandler#createViewResource} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public ViewResource createViewResource(FacesContext context, String resourceName) {
        return getWrapped().createViewResource(context, resourceName);
    }

    /**
     * <p class="changed_added_2_3">
     * The default behavior of this method is to call
     * {@link ResourceHandler#getViewResources(FacesContext, String, int, ResourceVisitOption...)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     *
     * @since 2.3
     */
    @Override
    public Stream<String> getViewResources(FacesContext facesContext, String path, int maxDepth, ResourceVisitOption... options) {
        return getWrapped().getViewResources(facesContext, path, maxDepth, options);
    }

    /**
     * <p class="changed_added_2_3">
     * The default behavior of this method is to call
     * {@link ResourceHandler#getViewResources(FacesContext, String, ResourceVisitOption...)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     *
     * @since 2.3
     */
    @Override
    public Stream<String> getViewResources(FacesContext facesContext, String path, ResourceVisitOption... options) {
        return getWrapped().getViewResources(facesContext, path, options);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResourceHandler#createResource(String, String, String)} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType) {
        return getWrapped().createResource(resourceName, libraryName, contentType);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link ResourceHandler#handleResourceRequest(jakarta.faces.context.FacesContext)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void handleResourceRequest(FacesContext context) throws IOException {
        getWrapped().handleResourceRequest(context);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link ResourceHandler#isResourceRequest(jakarta.faces.context.FacesContext)} on the wrapped {@link ResourceHandler}
     * object.
     * </p>
     */
    @Override
    public boolean isResourceRequest(FacesContext context) {
        return getWrapped().isResourceRequest(context);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResourceHandler#isResourceURL} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public boolean isResourceURL(String url) {
        return getWrapped().isResourceURL(url);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResourceHandler#libraryExists(String)} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public boolean libraryExists(String libraryName) {
        return getWrapped().libraryExists(libraryName);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link ResourceHandler#getRendererTypeForResourceName(String)} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getRendererTypeForResourceName(String resourceName) {
        return getWrapped().getRendererTypeForResourceName(resourceName);
    }

    /**
     * <p class="changed_added_2_3">
     * The default behavior of this method is to call
     * {@link ResourceHandler#markResourceRendered(FacesContext, String, String)} on the wrapped {@link ResourceHandler}
     * object.
     * </p>
     *
     * @since 2.3
     */
    @Override
    public void markResourceRendered(FacesContext context, String resourceName, String libraryName) {
        getWrapped().markResourceRendered(context, resourceName, libraryName);
    }

    /**
     * <p class="changed_added_2_3">
     * The default behavior of this method is to call
     * {@link ResourceHandler#isResourceRendered(FacesContext, String, String)} on the wrapped {@link ResourceHandler}
     * object.
     * </p>
     *
     * @since 2.3
     */
    @Override
    public boolean isResourceRendered(FacesContext context, String resourceName, String libraryName) {
        return getWrapped().isResourceRendered(context, resourceName, libraryName);
    }

}
