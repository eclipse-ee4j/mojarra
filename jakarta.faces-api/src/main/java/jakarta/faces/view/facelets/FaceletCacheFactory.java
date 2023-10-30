/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.view.facelets;

import jakarta.faces.FacesWrapper;

/**
 * <p class="changed_added_2_1">
 * <span class="changed_modified_2_3">Allows</span> customization of the implementation of {@link FaceletCache}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.1
 */
public abstract class FaceletCacheFactory implements FacesWrapper<FaceletCacheFactory> {

    private FaceletCacheFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public FaceletCacheFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public FaceletCacheFactory(FaceletCacheFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public FaceletCacheFactory getWrapped() {
        return wrapped;
    }

    /**
     * Returns the facility by which the Facelets {@link jakarta.faces.view.ViewDeclarationLanguage} creates and
     * caches instances of Facelets.
     *
     * @return the facelet cache
     */
    public abstract FaceletCache getFaceletCache();

}
