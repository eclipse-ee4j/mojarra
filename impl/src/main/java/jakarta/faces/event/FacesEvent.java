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

import java.util.EventObject;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewAction;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_modified_2_3">
 * <strong>FacesEvent</strong> is the base class for user interface and application events that can be fired by
 * {@link UIComponent}s. Concrete event classes must subclass {@link FacesEvent} in order to be supported by the request
 * processing lifecycle.
 * </p>
 */
public abstract class FacesEvent extends EventObject {

    private static final long serialVersionUID = -367663885586773794L;

    /**
     * <p class="changed_added_2_3">
     * Stores the Faces context.
     * </p>
     */
    private transient FacesContext facesContext;

    /**
     * <p class="changed_removed_2_3">
     * Construct a new event object from the specified source component.
     * </p>
     *
     * @param component Source {@link UIComponent} for this event
     * @throws IllegalArgumentException if <code>component</code> is <code>null</code>
     */
    public FacesEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new event object from the Faces context and specified source component.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component Source {@link UIComponent} for this event.
     * @throws IllegalArgumentException if <code>component</code> is <code>null</code>
     * @since 2.3
     */
    public FacesEvent(FacesContext facesContext, UIComponent component) {
        this(component);
        this.facesContext = facesContext;
    }

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the source {@link UIComponent} that sent this event.
     *
     * @return the source UI component.
     */
    public UIComponent getComponent() {

        return (UIComponent) getSource();

    }

    /**
     * <p class="changed_added_2_3">
     * Get the Faces context.
     * </p>
     *
     * <p>
     * If the constructor was passed a FacesContext we return it, otherwise we call FacesContext.getCurrentInstance() and
     * return it.
     * </p>
     *
     * @return the Faces context.
     * @since 2.3
     */
    public FacesContext getFacesContext() {
        /*
         * Note because UIViewAction is decorating the FacesContext during the execution of a request we cannot rely on the
         * saved FacesContext as it would be the original FacesContext (which is what we should be able to rely on).
         *
         * TODO - remove UIViewAction dependency on decorating the FacesContext.
         */
        if (!(source instanceof UIViewAction) && facesContext != null) {
            return facesContext;
        }
        return FacesContext.getCurrentInstance();
    }

    private PhaseId phaseId = PhaseId.ANY_PHASE;

    /**
     * <p>
     * Return the identifier of the request processing phase during which this event should be delivered. Legal values are
     * the singleton instances defined by the {@link PhaseId} class, including <code>PhaseId.ANY_PHASE</code>, which is the
     * default value.
     * </p>
     *
     * @return the phase id.
     */
    public PhaseId getPhaseId() {
        return phaseId;
    }

    /**
     * <p>
     * Set the {@link PhaseId} during which this event will be delivered.
     * </p>
     *
     * @param phaseId the phase id.
     * @throws IllegalArgumentException phaseId is null.
     */
    public void setPhaseId(PhaseId phaseId) {
        if (null == phaseId) {
            throw new IllegalArgumentException();
        }
        this.phaseId = phaseId;
    }

    // ------------------------------------------------- Event Broadcast Methods

    /**
     * <p>
     * Convenience method to queue this event for broadcast at the end of the current request processing lifecycle phase.
     * </p>
     *
     * @throws IllegalStateException if the source component for this event is not a descendant of a {@link UIViewRoot}
     */
    public void queue() {

        getComponent().queueEvent(this);

    }

    /**
     * <p>
     * Return <code>true</code> if this {@link FacesListener} is an instance of a listener class that this event supports.
     * Typically, this will be accomplished by an "instanceof" check on the listener class.
     * </p>
     *
     * @param listener {@link FacesListener} to evaluate
     * @return true if it is the appropriate instance, false otherwise.
     */
    public abstract boolean isAppropriateListener(FacesListener listener);

    /**
     * <p>
     * Broadcast this {@link FacesEvent} to the specified {@link FacesListener}, by whatever mechanism is appropriate.
     * Typically, this will be accomplished by calling an event processing method, and passing this {@link FacesEvent} as a
     * paramter.
     * </p>
     *
     * @param listener {@link FacesListener} to send this {@link FacesEvent} to
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     */
    public abstract void processListener(FacesListener listener);

}
