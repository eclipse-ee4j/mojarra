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

import jakarta.faces.FacesException;
import jakarta.faces.FacesWrapper;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_3">ExternalContextFactory</strong> is a factory object that
 * creates (if needed) and returns new {@link ExternalContext} instances, initialized for the processing of the
 * specified request and response objects.
 * </p>
 *
 * <p>
 * There must be one <code>ExternalContextFactory</code> instance per web application that is utilizing Jakarta Server
 * Faces. This instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * ExternalContextFactory factory = (ExternalContextFactory) FactoryFinder.getFactory(FactoryFinder.EXTERNAL_CONTEXT_FACTORY);
 * </pre>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 */

public abstract class ExternalContextFactory implements FacesWrapper<ExternalContextFactory> {

    private ExternalContextFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ExternalContextFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public ExternalContextFactory(ExternalContextFactory wrapped) {
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
    public ExternalContextFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p>
     * <span class="changed_added_2_0">Create</span> (if needed) and return an {@link ExternalContext} instance that is
     * initialized for the processing of the specified request and response objects, for this web application.
     * </p>
     *
     * @param context In Jakarta Servlet environments, the <code>ServletContext</code> that is associated with this web
     * application
     * @param request In Jakarta Servlet environments, the <code>ServletRequest</code> that is to be processed
     * @param response In Jakarta Servlet environments, the <code>ServletResponse</code> that is to be processed
     *
     * @return the instance of <code>ExternalContext</code>.
     *
     * @throws FacesException if a {@link ExternalContext} cannot be constructed for the specified parameters
     * @throws NullPointerException if any of the parameters are <code>null</code>
     *
     */
    public abstract ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException;

}
