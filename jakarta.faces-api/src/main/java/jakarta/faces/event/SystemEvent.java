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

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_added_2_0 changed_modified_2_2">SystemEvent</strong> is the base class for non-application
 * specific events that can be fired by arbitrary objects.
 * </p>
 *
 * @since 2.0
 */
public abstract class SystemEvent extends EventObject {

    private static final long serialVersionUID = 2696415667461888462L;

    /**
     * <p class="changed_added_2_3">
     * Stores the Faces context.
     * </p>
     */
    private transient FacesContext facesContext;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Pass the argument <code>source</code> to the superclass constructor.
     * </p>
     *
     * @param source the <code>source</code> reference to be passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public SystemEvent(Object source) {
        super(source);
    }

    /**
     * <p class="changed_added_2_3">
     * Pass the argument <code>source</code> to the superclass constructor.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param source the <code>source</code> reference to be passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public SystemEvent(FacesContext facesContext, Object source) {
        super(source);
        this.facesContext = facesContext;
    }

    // ---------------------------------------------------------- Public Methods

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
        if (facesContext == null) {
            facesContext = FacesContext.getCurrentInstance();
        }
        return facesContext;
    }

    /**
     * <p>
     * <span class="changed_modified_2_2">Return</span> <code>true</code> if this {@link FacesListener} is an instance of a
     * the appropriate listener class that this event supports. <span class="changed_added_2_2">The default implementation
     * returns true if the listener is a {@link ComponentSystemEventListener}.</span>
     * </p>
     *
     * @param listener {@link FacesListener} to evaluate
     *
     * @return the result as specified above
     */
    public boolean isAppropriateListener(FacesListener listener) {

        return listener instanceof SystemEventListener;

    }

    /**
     * <p>
     * Broadcast this event instance to the specified {@link FacesListener}, by whatever mechanism is appropriate.
     * Typically, this will be accomplished by calling an event processing method, and passing this instance as a parameter.
     * </p>
     *
     * @param listener {@link FacesListener} to send this {@link FacesEvent} to
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     */
    public void processListener(FacesListener listener) {

        ((SystemEventListener) listener).processEvent(this);

    }
}
