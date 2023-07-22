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

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_added_2_0 changed_modified_2_3">AjaxBehaviorEvent</strong> represents the component behavior
 * specific to <code>Ajax</code>).
 * </p>
 *
 * @since 2.0
 */
public class AjaxBehaviorEvent extends BehaviorEvent {

    private static final long serialVersionUID = -2533217384414744239L;

    /**
     * <p class="changed_added_2_0 changed_removed_2_3">
     * Construct a new event object from the specified source component and Ajax behavior.
     * </p>
     *
     * @param component Source {@link UIComponent} for this event
     * @param behavior {@link Behavior} for this event
     * @throws IllegalArgumentException if <code>component</code> or <code>ajaxBehavior</code> is <code>null</code>
     * @since 2.0
     */
    public AjaxBehaviorEvent(UIComponent component, Behavior behavior) {
        super(component, behavior);
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new event object from the Faces context, specified source component and Ajax behavior.
     * </p>
     *
     * @param facesContext the FacesContext.
     * @param component Source {@link UIComponent} for this event
     * @param behavior {@link Behavior} for this event
     * @throws IllegalArgumentException if <code>component</code> or <code>ajaxBehavior</code> is <code>null</code>
     * @since 2.3
     */
    public AjaxBehaviorEvent(FacesContext facesContext, UIComponent component, Behavior behavior) {
        super(facesContext, component, behavior);
    }

    // ------------------------------------------------- Event Broadcast Methods

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if this {@link FacesListener} is an instance of a the appropriate listener class that this
     * event supports.
     * </p>
     *
     * @param listener {@link FacesListener} to evaluate
     *
     * @since 2.0
     */
    @Override
    public boolean isAppropriateListener(FacesListener listener) {

        return listener instanceof AjaxBehaviorListener;

    }

    /**
     * <p class="changed_added_2_0">
     * Broadcast this event instance to the specified {@link FacesListener}, by whatever mechanism is appropriate.
     * Typically, this will be accomplished by calling an event processing method, and passing this instance as a parameter.
     * </p>
     *
     * @param listener {@link FacesListener} to invoke
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     *
     * @since 2.0
     */
    @Override
    public void processListener(FacesListener listener) {

        ((AjaxBehaviorListener) listener).processAjaxBehavior(this);

    }

}
