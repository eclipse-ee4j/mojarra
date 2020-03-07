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

package jakarta.faces.component.behavior;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p class="changed_added_2_0">
 * The <strong>ClientBehaviorHolder</strong> interface may be implemented by any concrete
 * {@link jakarta.faces.component.UIComponent} that wishes to support client behaviors as defined by
 * {@link ClientBehavior}.
 * </p>
 *
 * @since 2.0
 */
public interface ClientBehaviorHolder {

    /**
     * <p class="changed_added_2_0">
     * Attaches a {@link ClientBehavior} to the component implementing this interface for the specified event. Valid event
     * names for a UIComponent implementation are defined by {@code ClientBehaviorHolder.getEventNames()}.
     * </p>
     *
     * @param eventName the logical name of the client-side event to attach the behavior to.
     * @param behavior the {@link ClientBehavior} instance to attach for the specified event name.
     *
     * @since 2.0
     */
    void addClientBehavior(String eventName, ClientBehavior behavior);

    /**
     * <p class="changed_added_2_0">
     * Returns a non-null, unmodifiable <code>Collection</code> containing the names of the logical events supported by the
     * component implementing this interface.
     * </p>
     *
     * @return an unmodifiable collection of event names.
     * @since 2.0
     */
    Collection<String> getEventNames();

    /**
     * <p class="changed_added_2_0">
     * Returns a non-null, unmodifiable <code>Map</code> that contains the the {@link ClientBehavior}s that have been
     * attached to the component implementing this interface. The keys in this <code>Map</code> are event names defined by
     * {@link #getEventNames}.
     * </p>
     *
     * @return an unmodifiable map of client behaviors.
     * @since 2.0
     */
    Map<String, List<ClientBehavior>> getClientBehaviors();

    /**
     * <p class="changed_added_2_0">
     * Returns the default event name for this <code>ClientBehaviorHolder</code> implementation. This must be one of the
     * event names returned by {@link #getEventNames} or null if the component does not have a default event.
     *
     * @return the default event name.
     * @since 2.0
     */
    String getDefaultEventName();
}
