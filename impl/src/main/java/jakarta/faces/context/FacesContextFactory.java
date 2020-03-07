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
import jakarta.faces.lifecycle.Lifecycle;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_2 changed_modified_2_3">FacesContextFactory</strong> is a
 * factory object that creates (if needed) and returns new {@link FacesContext} instances, initialized for the
 * processing of the specified request and response objects. Implementations may take advantage of the calls to the
 * <code>release()</code> method of the allocated {@link FacesContext} instances to pool and recycle them, rather than
 * creating a new instance every time.
 * </p>
 *
 * <p>
 * There must be one <code>FacesContextFactory</code> instance per web application that is utilizing Jakarta Server
 * Faces. This instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * FacesContextFactory factory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
 * </pre>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 */
public abstract class FacesContextFactory implements FacesWrapper<FacesContextFactory> {

    private FacesContextFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public FacesContextFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public FacesContextFactory(FacesContextFactory wrapped) {
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
    public FacesContextFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_2">Create</span> (if needed) and return a {@link FacesContext}
     * instance that is initialized for the processing of the specified request and response objects, utilizing the
     * specified {@link Lifecycle} instance, for this web application.
     * </p>
     *
     * <p>
     * The implementation of this method must ensure that calls to the <code>getCurrentInstance()</code> method of
     * {@link FacesContext}, from the same thread that called this method, will return the same {@link FacesContext}
     * instance until the <code>release()</code> method is called on that instance.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The implementation must call {@link ExternalContextFactory#getExternalContext} to produce the {@link ExternalContext}
     * for the {@link FacesContext} instance.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The default implementation must call {@link ExceptionHandlerFactory#getExceptionHandler} and make it so the return
     * from that method is what gets returned from a call to {@link FacesContext#getExceptionHandler} on the returned
     * <code>FacesContext</code> instance.
     * </p>
     *
     * <p class="changed_added_2_2">
     * The default implementation must call {@link jakarta.faces.lifecycle.ClientWindowFactory#getClientWindow} and make it
     * so the return from that method is what gets returned from a call to {@link ExternalContext#getClientWindow()} on the
     * returned <code>ExternalContext</code> instance.
     * </p>
     *
     * @param context In Jakarta Servlet environments, the <code>ServletContext</code> that is associated with this web
     * application
     * @param request In Jakarta Servlet environments, the <code>ServletRequest</code> that is to be processed
     * @param response In Jakarta Servlet environments, the <code>ServletResponse</code> that is to be processed
     * @param lifecycle The {@link Lifecycle} instance being used to process this request
     *
     * @return the instance of <code>FacesContext</code>.
     *
     * @throws FacesException if a {@link FacesContext} cannot be constructed for the specified parameters
     * @throws NullPointerException if any of the parameters are <code>null</code>
     */
    public abstract FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle) throws FacesException;

}
