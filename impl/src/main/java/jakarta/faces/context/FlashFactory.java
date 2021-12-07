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
 * <p>
 * <strong class="changed_added_2_2 changed_modified_2_3">FlashFactory</strong> is a factory object that creates (if
 * needed) and returns {@link Flash} instances. Implementations of Jakarta Faces must provide at least a default
 * implementation of {@link Flash}.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * There must be one {@link FlashFactory} instance per web application that is utilizing Jakarta Faces. This
 * instance can be acquired, in a portable manner, by calling:
 * </p>
 *
 * <pre>
 * FlashFactory factory = (FlashFactory) FactoryFinder.getFactory(FactoryFinder.FLASH_FACTORY);
 * </pre>
 *
 * <p>
 * The common way to access the flash instance from Java code is still via {@link ExternalContext#getFlash}. The common
 * way to access the flash from Faces views is the implicit Jakarta Expression Language object "flash". The runtime must
 * ensure that the <code>FlashFactory</code> is used to instantiate the flash.
 * </p>
 *
 * </div>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */
public abstract class FlashFactory implements FacesWrapper<FlashFactory> {

    private FlashFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public FlashFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public FlashFactory(FlashFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public FlashFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_2">
     * Create (if needed) and return a {@link Flash} instance for this web application.
     * </p>
     *
     * @param create <code>true</code> to create a new instance for this request if necessary; <code>false</code> to return
     * <code>null</code> if there's no instance in the current <code>session</code>.
     *
     * @return the instance of <code>Flash</code>.
     *
     * @since 2.2
     */
    public abstract Flash getFlash(boolean create);

}
