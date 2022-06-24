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

package jakarta.faces;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_3">Any</span> wrapper class in Jakarta Faces that must provide access to the
 * object it wraps must implement this interface.
 * </p>
 *
 * <p class="changed_added_2_3">
 * The expected usage of all subclasses is to provide a constructor that takes an instance of type <code>T</code>, which
 * sets the instance variable that is returned from the {@link #getWrapped} method.
 * </p>
 *
 * @param <T> the wrapped type.
 * @since 2.0
 */
public interface FacesWrapper<T> {

    /**
     * <p class="changed_added_2_0">
     * A class that implements this interface uses this method to return an instance of the class being wrapped.
     * </p>
     *
     * @return the wrapped instance.
     * @since 2.0
     */
    T getWrapped();

}
