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

import jakarta.faces.FacesWrapper;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_3">ApplicationFactory</strong> is a factory object that
 * creates (if needed) and returns {@link Application} instances. Implementations of Jakarta Faces must provide
 * at least a default implementation of {@link Application}.
 * </p>
 *
 * <p>
 * There must be one {@link ApplicationFactory} instance per web application that is utilizing Jakarta Faces.
 * This instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
 * </pre>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 */

public abstract class ApplicationFactory implements FacesWrapper<ApplicationFactory> {

    private ApplicationFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ApplicationFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public ApplicationFactory(ApplicationFactory wrapped) {
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
    public ApplicationFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p>
     * Create (if needed) and return an {@link Application} instance for this web application.
     * </p>
     *
     * @return the application.
     */
    public abstract Application getApplication();

    /**
     * <p>
     * Replace the {@link Application} instance that will be returned for this web application.
     * </p>
     *
     * @param application The replacement {@link Application} instance
     *
     * @throws NullPointerException if <code>application</code> is <code>null</code>.
     */
    public abstract void setApplication(Application application);

}
