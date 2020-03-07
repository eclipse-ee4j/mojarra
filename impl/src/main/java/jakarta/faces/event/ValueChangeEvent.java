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
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_modified_2_3">
 * A {@link ValueChangeEvent} is a notification that the local value of the source component has been change as a result
 * of user interface activity. It is not fired unless validation of the new value was completed successfully.
 * </p>
 */
public class ValueChangeEvent extends FacesEvent {

    private static final long serialVersionUID = 2455861757565618446L;

    /**
     * <p class="changed_removed_2_3">
     * Construct a new event object from the specified source component, old value, and new value.
     * </p>
     *
     * <p>
     * The default {@link PhaseId} for this event is {@link PhaseId#ANY_PHASE}.
     * </p>
     *
     * @param component Source {@link UIComponent} for this event
     * @param oldValue The previous local value of this {@link UIComponent}
     * @param newValue The new local value of thie {@link UIComponent}
     * @throws IllegalArgumentException if <code>component</code> is <code>null</code>
     */
    public ValueChangeEvent(UIComponent component, Object oldValue, Object newValue) {
        super(component);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new event object from the Faces context, specified source component, old value and new value.
     * </p>
     *
     * <p>
     * The default {@link PhaseId} for this event is {@link PhaseId#ANY_PHASE}.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component Source {@link UIComponent} for this event
     * @param oldValue The previous local value of this {@link UIComponent}
     * @param newValue The new local value of thie {@link UIComponent}
     * @throws IllegalArgumentException if <code>component</code> is <code>null</code>
     */
    public ValueChangeEvent(FacesContext facesContext, UIComponent component, Object oldValue, Object newValue) {
        super(facesContext, component);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * The previous local value of the source {@link UIComponent}.
     * </p>
     */
    private Object oldValue = null;

    /**
     * <p>
     * Return the previous local value of the source {@link UIComponent}.
     * </p>
     *
     * @return the previous local value
     */
    public Object getOldValue() {

        return oldValue;

    }

    /**
     * <p>
     * The current local value of the source {@link UIComponent}.
     * </p>
     */
    private Object newValue = null;

    /**
     * <p>
     * Return the current local value of the source {@link UIComponent}.
     * </p>
     *
     * @return the current local value
     */
    public Object getNewValue() {

        return newValue;

    }

    // ------------------------------------------------- Event Broadcast Methods

    @Override
    public boolean isAppropriateListener(FacesListener listener) {

        return listener instanceof ValueChangeListener;

    }

    /**
     * @throws AbortProcessingException {@inheritDoc}
     */
    @Override
    public void processListener(FacesListener listener) {

        ((ValueChangeListener) listener).processValueChange(this);

    }

}
