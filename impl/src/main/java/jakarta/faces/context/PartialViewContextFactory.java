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
 * <p class="changed_modified_2_0">
 * <span class="changed_modified_2_3">PartialViewContextFactory</span> is a factory object that creates (if needed) and
 * returns new {@link PartialViewContext} instances.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * There must be one <code>PartialViewContextFactory</code> instance per web application that is utilizing Jakarta
 * Server Faces. This instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * PartialViewContextFactory factory = (PartialViewContextFactory) FactoryFinder.getFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
 * </pre>
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
public abstract class PartialViewContextFactory implements FacesWrapper<PartialViewContextFactory> {

    private PartialViewContextFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public PartialViewContextFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public PartialViewContextFactory(PartialViewContextFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public PartialViewContextFactory getWrapped() {
        return wrapped;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * <span class="changed_modified_2_0">Create</span> (if needed) and return a {@link PartialViewContext} instance that is
     * initialized using the current {@link FacesContext} instance.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request.
     *
     * @return the {@link PartialViewContext} as specified above
     */
    public abstract PartialViewContext getPartialViewContext(FacesContext context);

}
