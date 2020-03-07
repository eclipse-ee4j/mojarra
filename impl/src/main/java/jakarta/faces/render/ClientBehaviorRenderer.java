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

package jakarta.faces.render;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * A <strong class="changed_added_2_0">ClientBehaviorRenderer</strong> produces the client-side script that implements a
 * {@link ClientBehavior}'s client-side logic. It can also enqueue server-side
 * {@link jakarta.faces.event.BehaviorEvent}s that may be processed later by event listeners that have registered an
 * interest.
 * </p>
 *
 * <p>
 * Individual {@link ClientBehaviorRenderer} instances will be instantiated as requested during the rendering process,
 * and will remain in existence for the remainder of the lifetime of a web application. Because each instance may be
 * invoked from more than one request processing thread simultaneously, they MUST be programmed in a thread-safe manner.
 * </p>
 *
 * @since 2.0
 */

public abstract class ClientBehaviorRenderer {

    // ------------------------------------------------------ Rendering Methods

    /**
     * <p class="changed_added_2_0">
     * Return the script that implements this ClientBehavior's client-side logic. The default implementation returns
     * <code>null</code>.
     * </p>
     *
     * <p>
     * ClientBehaviorRenderer.getScript() implementations are allowed to return null to indicate that no script is required
     * for this particular getScript() call. For example, a ClientBehaviorRenderer implementation may return null if the
     * associated ClientBehavior is disabled.
     * </p>
     *
     * @param behaviorContext the {@link ClientBehaviorContext} that provides properties that might influence this
     * getScript() call. Note that ClientBehaviorContext instances are short-lived objects that are only valid for the
     * duration of the call to getScript(). ClientBehaviorRenderer implementations must not hold onto references to
     * ClientBehaviorContexts.
     *
     * @param behavior the ClientBehavior instance that generates script.
     *
     * @return script that provides the client-side behavior, or null if no script is required.
     *
     * @since 2.0
     *
     */
    public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior) {

        return null;
    }

    /**
     * <p class="changed_added_2_0">
     * Decode any new state of this {@link ClientBehavior} from the request contained in the specified {@link FacesContext}.
     * </p>
     *
     * <p>
     * During decoding, events may be enqueued for later processing (by event listeners who have registered an interest), by
     * calling <code>queueEvent()</code>.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @param component {@link UIComponent} the component associated with this
     * {@link jakarta.faces.component.behavior.Behavior}
     * @param behavior {@link ClientBehavior} the behavior instance
     *
     * @throws NullPointerException if <code>context</code>, <code>component</code> <code>behavior</code> is
     * <code>null</code>
     *
     * @since 2.0
     */
    public void decode(FacesContext context, UIComponent component, ClientBehavior behavior) {

        if (null == context || null == component || behavior == null) {
            throw new NullPointerException();
        }

    }

}
