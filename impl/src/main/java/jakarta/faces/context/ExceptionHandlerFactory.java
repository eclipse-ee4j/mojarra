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

import jakarta.faces.FacesWrapper;

/**
 * <p class="changed_added_2_0">
 * <strong class="changed_modified_2_3">ExceptionHandlerFactory</strong> is a factory object that creates (if needed)
 * and returns a new {@link ExceptionHandler} instance.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * There must be one <code>ExceptionHandlerFactory</code> instance per web application that is utilizing Jakarta Server
 * Faces. This instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * <code>
 *   ExceptionHandlerFactory factory = (ExceptionHandlerFactory)
 *    FactoryFinder.getFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY);
 * </code>
 * </pre>
 *
 *
 * </div>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */

public abstract class ExceptionHandlerFactory implements FacesWrapper<ExceptionHandlerFactory> {

    private ExceptionHandlerFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ExceptionHandlerFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public ExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public ExceptionHandlerFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_0">
     * Create and return a A new <code>ExceptionHandler</code> instance. The implementation must return an
     * <code>ExceptionHandler</code> instance suitable for the environment. For example, in some cases it may be desirable
     * for an <code>ExceptionHandler</code> to write error information to the response instead of throwing exceptions as in
     * the case of Ajax applications.
     * </p>
     *
     * @return newly created <code>ExceptionHandler</code>.
     *
     */
    public abstract ExceptionHandler getExceptionHandler();

}
