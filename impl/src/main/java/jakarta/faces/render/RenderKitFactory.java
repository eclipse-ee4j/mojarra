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

import java.util.Iterator;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_3">RenderKitFactory</strong> is a factory object that
 * registers and returns {@link RenderKit} instances. Implementations of Jakarta Faces must provide at least a
 * default implementation of {@link RenderKit}. Advanced implementations (or external third party libraries) may provide
 * additional {@link RenderKit} implementations (keyed by render kit identifiers) for performing different types of
 * rendering for the same components.
 * </p>
 *
 * <p>
 * There must be one {@link RenderKitFactory} instance per web application that is utilizing Jakarta Faces. This
 * instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * RenderKitFactory factory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
 * </pre>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 */

public abstract class RenderKitFactory implements FacesWrapper<RenderKitFactory> {

    private RenderKitFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public RenderKitFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public RenderKitFactory(RenderKitFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public RenderKitFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p>
     * The render kit identifier of the default {@link RenderKit} instance for this Jakarta Faces implementation.
     * </p>
     */
    public static final String HTML_BASIC_RENDER_KIT = "HTML_BASIC";

    /**
     * <p>
     * Register the specified {@link RenderKit} instance, associated with the specified <code>renderKitId</code>, to be
     * supported by this {@link RenderKitFactory}, replacing any previously registered {@link RenderKit} for this
     * identifier.
     * </p>
     *
     * @param renderKitId Identifier of the {@link RenderKit} to register
     * @param renderKit {@link RenderKit} instance that we are registering
     *
     * @throws NullPointerException if <code>renderKitId</code> or <code>renderKit</code> is <code>null</code>
     */
    public abstract void addRenderKit(String renderKitId, RenderKit renderKit);

    /**
     * <p>
     * Return a {@link RenderKit} instance for the specified render kit identifier, possibly customized based on dynamic
     * characteristics of the specified {@link FacesContext}, if non-<code>null</code>. If there is no registered
     * {@link RenderKit} for the specified identifier, return <code>null</code>. The set of available render kit identifiers
     * is available via the <code>getRenderKitIds()</code> method.
     * </p>
     *
     * @param context FacesContext for the request currently being processed, or <code>null</code> if none is available.
     * @param renderKitId Render kit identifier of the requested {@link RenderKit} instance
     *
     * @throws IllegalArgumentException if no {@link RenderKit} instance can be returned for the specified identifier
     * @throws NullPointerException if <code>renderKitId</code> is <code>null</code>
     *
     * @return a {@link RenderKit} instance
     */
    public abstract RenderKit getRenderKit(FacesContext context, String renderKitId);

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of render kit identifiers registered with this factory. This set must
     * include the value specified by <code>RenderKitFactory.HTML_BASIC_RENDER_KIT</code>.
     * </p>
     *
     * @return an <code>Iterator</code> over the set of render kit identifiers
     */
    public abstract Iterator<String> getRenderKitIds();

}
