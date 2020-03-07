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

package jakarta.faces.render;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;

/**
 * <p class="changed_modified_2_0">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link RenderKit} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link RenderKit} instance. The
 * default implementation of all methods is to call through to the wrapped {@link RenderKit}.
 * </p>
 *
 * <p class="changed_added_2_0">
 * Usage: extend this class and override {@link #getWrapped} to return the instance we are wrapping.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class RenderKitWrapper extends RenderKit implements FacesWrapper<RenderKit> {

    private RenderKit wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public RenderKitWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this render kit has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public RenderKitWrapper(RenderKit wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public RenderKit getWrapped() {
        return wrapped;
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link RenderKit#addRenderer(String, String, Renderer)} on the wrapped
     * {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#addRenderer(String, String, Renderer)
     */
    @Override
    public void addRenderer(String family, String rendererType, Renderer renderer) {
        getWrapped().addRenderer(family, rendererType, renderer);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link RenderKit#createResponseStream(java.io.OutputStream)} on the
     * wrapped {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#createResponseStream(java.io.OutputStream)
     */
    @Override
    public ResponseStream createResponseStream(OutputStream out) {
        return getWrapped().createResponseStream(out);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link RenderKit#createResponseWriter(java.io.Writer, String, String)}
     * on the wrapped {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#createResponseWriter(java.io.Writer, String, String)
     */
    @Override
    public ResponseWriter createResponseWriter(Writer writer, String contentTypeList, String characterEncoding) {
        return getWrapped().createResponseWriter(writer, contentTypeList, characterEncoding);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link RenderKit#getRenderer(String, String)} on the wrapped
     * {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#getRenderer(String, String)
     */
    @Override
    public Renderer getRenderer(String family, String rendererType) {
        return getWrapped().getRenderer(family, rendererType);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.render.RenderKit#getResponseStateManager()} on
     * the wrapped {@link RenderKit} object.
     * </p>
     *
     * @see jakarta.faces.render.RenderKit#getResponseStateManager()
     */
    @Override
    public ResponseStateManager getResponseStateManager() {
        return getWrapped().getResponseStateManager();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.render.RenderKit#getComponentFamilies()} on the
     * wrapped {@link RenderKit} object.
     * </p>
     *
     * @see jakarta.faces.render.RenderKit#getComponentFamilies()
     */
    @Override
    public Iterator<String> getComponentFamilies() {
        return getWrapped().getComponentFamilies();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link RenderKit#getRendererTypes(String)} on the wrapped
     * {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#getRendererTypes(String)
     */
    @Override
    public Iterator<String> getRendererTypes(String componentFamily) {
        return getWrapped().getRendererTypes(componentFamily);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link RenderKit#addClientBehaviorRenderer(String, ClientBehaviorRenderer)} on the wrapped {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#addClientBehaviorRenderer(String, ClientBehaviorRenderer)
     */
    @Override
    public void addClientBehaviorRenderer(String type, ClientBehaviorRenderer renderer) {
        getWrapped().addClientBehaviorRenderer(type, renderer);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link RenderKit#getClientBehaviorRenderer(String)} on the wrapped
     * {@link RenderKit} object.
     * </p>
     *
     * @see RenderKit#getClientBehaviorRenderer(String)
     */
    @Override
    public ClientBehaviorRenderer getClientBehaviorRenderer(String type) {
        return getWrapped().getClientBehaviorRenderer(type);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link jakarta.faces.render.RenderKit#getClientBehaviorRendererTypes()} on the wrapped {@link RenderKit} object.
     * </p>
     *
     * @see jakarta.faces.render.RenderKit#getClientBehaviorRendererTypes()
     */
    @Override
    public Iterator<String> getClientBehaviorRendererTypes() {
        return getWrapped().getClientBehaviorRendererTypes();
    }

}
