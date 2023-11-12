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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.faces.FacesException;
import jakarta.faces.FacesWrapper;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p class="changed_modified_2_2">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link ViewHandler} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link ViewHandler} instance. The
 * default implementation of all methods is to call through to the wrapped {@link ViewHandler}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 1.2
 */
public abstract class ViewHandlerWrapper extends ViewHandler implements FacesWrapper<ViewHandler> {

    private ViewHandler wrapped;

    /**
     * <p class="changed_added_2_3">
     * If this view handler has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ViewHandlerWrapper(ViewHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewHandler getWrapped() {
        return wrapped;
    }

    // ------------------------ Methods from jakarta.faces.application.ViewHandler

    /**
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#initView} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#initView
     * @since 1.2
     */
    @Override
    public void initView(FacesContext context) throws FacesException {
        getWrapped().initView(context);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#restoreView(jakarta.faces.context.FacesContext, String)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#restoreView(jakarta.faces.context.FacesContext, String)
     * @since 1.2
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return getWrapped().restoreView(context, viewId);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#createView(jakarta.faces.context.FacesContext, String)} on the wrapped {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#createView(jakarta.faces.context.FacesContext, String)
     * @since 1.2
     */
    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        return getWrapped().createView(context, viewId);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#renderView(jakarta.faces.context.FacesContext, jakarta.faces.component.UIViewRoot)} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#renderView(jakarta.faces.context.FacesContext, jakarta.faces.component.UIViewRoot)
     * @since 1.2
     */
    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
        getWrapped().renderView(context, viewToRender);
    }

    /**
     *
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#calculateCharacterEncoding(jakarta.faces.context.FacesContext)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#calculateCharacterEncoding(jakarta.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public String calculateCharacterEncoding(FacesContext context) {
        return getWrapped().calculateCharacterEncoding(context);
    }

    /**
     *
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#calculateLocale(jakarta.faces.context.FacesContext)} on the wrapped {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#calculateLocale(jakarta.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public Locale calculateLocale(FacesContext context) {
        return getWrapped().calculateLocale(context);
    }

    /**
     *
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#deriveViewId(jakarta.faces.context.FacesContext, String)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#deriveViewId(jakarta.faces.context.FacesContext, String)
     * @since 2.0
     */
    @Override
    public String deriveViewId(FacesContext context, String requestViewId) {
        return getWrapped().deriveViewId(context, requestViewId);
    }

    /**
     *
     * <p class="changed_added_2_1">
     * The default behavior of this method is to call
     * {@link ViewHandler#deriveLogicalViewId(jakarta.faces.context.FacesContext, String)} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#deriveLogicalViewId(jakarta.faces.context.FacesContext, String)
     * @since 2.1
     */
    @Override
    public String deriveLogicalViewId(FacesContext context, String requestViewId) {
        return getWrapped().deriveLogicalViewId(context, requestViewId);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#calculateRenderKitId(jakarta.faces.context.FacesContext)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#calculateRenderKitId(jakarta.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public String calculateRenderKitId(FacesContext context) {
        return getWrapped().calculateRenderKitId(context);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#getActionURL(jakarta.faces.context.FacesContext, String)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#getActionURL(jakarta.faces.context.FacesContext, String)
     * @since 1.2
     */
    @Override
    public String getActionURL(FacesContext context, String viewId) {
        return getWrapped().getActionURL(context, viewId);
    }

    /**
     *
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#getProtectedViewsUnmodifiable} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#getProtectedViewsUnmodifiable
     * @since 2.2
     */
    @Override
    public Set<String> getProtectedViewsUnmodifiable() {
        return getWrapped().getProtectedViewsUnmodifiable();
    }

    /**
     *
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#addProtectedView} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#addProtectedView
     * @since 2.2
     */
    @Override
    public void addProtectedView(String urlPattern) {
        getWrapped().addProtectedView(urlPattern);
    }

    /**
     *
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#removeProtectedView} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#removeProtectedView
     * @since 2.2
     */
    @Override
    public boolean removeProtectedView(String urlPattern) {
        return getWrapped().removeProtectedView(urlPattern);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#getRedirectURL(jakarta.faces.context.FacesContext, String, java.util.Map, boolean)} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#getRedirectURL(jakarta.faces.context.FacesContext, String, java.util.Map, boolean)
     * @since 2.0
     */
    @Override
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        return getWrapped().getRedirectURL(context, viewId, parameters, includeViewParams);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#getBookmarkableURL(jakarta.faces.context.FacesContext, String, java.util.Map, boolean)} on the
     * wrapped {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#getBookmarkableURL(jakarta.faces.context.FacesContext, String, java.util.Map, boolean)
     * @since 2.0
     */
    @Override
    public String getBookmarkableURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        return getWrapped().getBookmarkableURL(context, viewId, parameters, includeViewParams);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#getResourceURL(jakarta.faces.context.FacesContext, String)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @see ViewHandler#getResourceURL(jakarta.faces.context.FacesContext, String)
     * @since 1.2
     */
    @Override
    public String getResourceURL(FacesContext context, String path) {
        return getWrapped().getResourceURL(context, path);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#getWebsocketURL(FacesContext, String)} on the
     * wrapped {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#getWebsocketURL(FacesContext, String)
     * @since 2.3
     */
    @Override
    public String getWebsocketURL(FacesContext context, String channel) {
        return getWrapped().getWebsocketURL(context, channel);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#getViewDeclarationLanguage} on the wrapped
     * {@link ViewHandler} object.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(FacesContext context, String viewId) {
        return getWrapped().getViewDeclarationLanguage(context, viewId);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#getViews(FacesContext, String, ViewVisitOption...)}
     * on the wrapped {@link ViewHandler} object.
     * </p>
     *
     * @since 2.3
     */
    @Override
    public Stream<String> getViews(FacesContext context, String path, ViewVisitOption... options) {
        return getWrapped().getViews(context, path, options);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link ViewHandler#getViews(FacesContext, String, int, ViewVisitOption...)} on the wrapped {@link ViewHandler}
     * object.
     * </p>
     *
     * @since 2.3
     */
    @Override
    public Stream<String> getViews(FacesContext context, String path, int maxDepth, ViewVisitOption... options) {
        return getWrapped().getViews(context, path, maxDepth, options);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link ViewHandler#writeState(jakarta.faces.context.FacesContext)} on
     * the wrapped {@link ViewHandler} object.
     * </p>
     *
     * @see ViewHandler#writeState(jakarta.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public void writeState(FacesContext context) throws IOException {
        getWrapped().writeState(context);
    }

    // ------------------------------------------------------------- Deprecated methods

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ViewHandlerWrapper() {

    }

}
