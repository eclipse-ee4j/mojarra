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

package jakarta.faces.lifecycle;

import java.util.Iterator;

import jakarta.faces.FacesWrapper;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_3">LifecycleFactory</strong> is a factory object that creates
 * (if needed) and returns {@link Lifecycle} instances. Implementations of Jakarta Faces must provide at least a
 * default implementation of {@link Lifecycle}. Advanced implementations (or external third party libraries) MAY provide
 * additional {@link Lifecycle} implementations (keyed by lifecycle identifiers) for performing different types of
 * request processing on a per-request basis.
 * </p>
 *
 * <p>
 * There must be one <code>LifecycleFactory</code> instance per web application that is utilizing Jakarta Faces.
 * This instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
 * </pre>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 */

public abstract class LifecycleFactory implements FacesWrapper<LifecycleFactory> {

    private LifecycleFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public LifecycleFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public LifecycleFactory(LifecycleFactory wrapped) {
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
    public LifecycleFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p>
     * The lifecycle identifier for the default {@link Lifecycle} instance for this Jakarta Faces implementation.
     * </p>
     */
    public static final String DEFAULT_LIFECYCLE = "DEFAULT";

    /**
     * <p>
     * Register a new {@link Lifecycle} instance, associated with the specified <code>lifecycleId</code>, to be supported by
     * this <code>LifecycleFactory</code>. This method may be called at any time, and makes the corresponding
     * {@link Lifecycle} instance available throughout the remaining lifetime of this web application.
     * </p>
     *
     * @param lifecycleId Identifier of the new {@link Lifecycle}
     * @param lifecycle {@link Lifecycle} instance that we are registering
     *
     * @throws IllegalArgumentException if a {@link Lifecycle} with the specified <code>lifecycleId</code> has already been
     * registered
     * @throws NullPointerException if <code>lifecycleId</code> or <code>lifecycle</code> is <code>null</code>
     */
    public abstract void addLifecycle(String lifecycleId, Lifecycle lifecycle);

    /**
     * <p>
     * Create (if needed) and return a {@link Lifecycle} instance for the specified lifecycle identifier. The set of
     * available lifecycle identifiers is available via the <code>getLifecycleIds()</code> method.
     * </p>
     *
     * <p>
     * Each call to <code>getLifecycle()</code> for the same <code>lifecycleId</code>, from within the same web application,
     * must return the same {@link Lifecycle} instance.
     * </p>
     *
     * @param lifecycleId Lifecycle identifier of the requested {@link Lifecycle} instance
     *
     * @throws IllegalArgumentException if no {@link Lifecycle} instance can be returned for the specified identifier
     * @throws NullPointerException if <code>lifecycleId</code> is <code>null</code>
     *
     * @return the {@code Lifecycle} instance
     */
    public abstract Lifecycle getLifecycle(String lifecycleId);

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of lifecycle identifiers supported by this factory. This set must
     * include the value specified by <code>LifecycleFactory.DEFAULT_LIFECYCLE</code>.
     * </p>
     *
     * @return an {@code Iterator} over the set of lifecycle identifiers supported by this factory
     */
    public abstract Iterator<String> getLifecycleIds();

}
