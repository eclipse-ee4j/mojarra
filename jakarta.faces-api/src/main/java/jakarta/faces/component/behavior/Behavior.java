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

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.BehaviorEvent;

/**
 * <p class="changed_added_2_0">
 * The <strong>Behavior</strong> interface is the root API of the component behavior model. Behaviors are objects that
 * are attached to {@link UIComponent}s in order to enhance components with functionality not explicitly defined by the
 * component implementation itself. The component behavior API is intended to support different types of behavior
 * contracts, and possibly different types of interactions between behaviors and components. The first such contract is
 * the {@link ClientBehavior}, which defines a mechanism by which script-producing behaviors attach scripts to
 * components for execution on the client. In the future other types of behavior contracts may be added.
 * </p>
 *
 * <p>
 * Like other attached objects (converters, validators) Behavior instances are created via the
 * {@link jakarta.faces.application.Application} object. See
 * {@link jakarta.faces.application.Application#createBehavior} for more details.
 * </p>
 *
 * @since 2.0
 */
public interface Behavior {

    /**
     * <p class="changed_added_2_0">
     * Broadcast the specified {@link BehaviorEvent} to all registered event listeners who have expressed an interest in
     * events of this type. Listeners are called in the order in which they were added.
     * </p>
     *
     * @param event The {@link BehaviorEvent} to be broadcast
     *
     * @throws jakarta.faces.event.AbortProcessingException Signal the Jakarta Faces implementation that no further
     * processing on the current event should be performed
     * @throws IllegalArgumentException if the implementation class of this {@link BehaviorEvent} is not supported by this
     * component
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     *
     * @since 2.0
     */
    void broadcast(BehaviorEvent event);

}
