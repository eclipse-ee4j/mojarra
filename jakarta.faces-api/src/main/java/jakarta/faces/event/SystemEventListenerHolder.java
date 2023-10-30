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

package jakarta.faces.event;

import java.util.List;

/**
 * <p class="changed_added_2_0">
 * Classes that implement this interface agree to maintain a list of {@link SystemEventListener} instances for each kind
 * of {@link SystemEvent} they can generate. This interface enables arbitrary Objects to act as the source for
 * {@link SystemEvent} instances.
 * </p>
 *
 * <p>
 * If the implementing class is a {@link jakarta.faces.component.UIComponent} or is referenced by a
 * <code>UIComponent</code>, care must be taken to ensure that the implementing class, and all the members of the list
 * returned by {@link #getListenersForEventClass} work correctly with the state management system. One way to ensure
 * this is to have the class and the list members implement {@link jakarta.faces.component.StateHolder} or
 * {@link java.io.Serializable}.
 * </p>
 *
 * @since 2.0
 */

public interface SystemEventListenerHolder {

    /**
     * <div class="changed_added_2_0">
     * <p>
     * Return a <code>List</code> of {@link SystemEventListener} instances that have been installed into the class
     * implementing this interface.
     * </p>
     * </div>
     *
     * @param facesEventClass the class for which listeners are to be returned
     *
     * @return the listeners for the argument class
     *
     */
    List<SystemEventListener> getListenersForEventClass(Class<? extends SystemEvent> facesEventClass);

}
