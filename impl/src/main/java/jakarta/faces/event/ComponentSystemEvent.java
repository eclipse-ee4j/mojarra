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
 *
 * <p>
 * <strong class="changed_added_2_0 changed_modified_2_2"> ComponentSystemEvent</strong> is the base class for
 * {@link SystemEvent}s that are specific to a {@link UIComponent} instance.
 * </p>
 *
 * @since 2.0
 */
public abstract class ComponentSystemEvent extends SystemEvent {

    private static final long serialVersionUID = -4726746661822507506L;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Pass the argument <code>component</code> to the superclass constructor.
     * </p>
     *
     * @param component the <code>UIComponent</code> reference to be passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     *
     * @since 2.0
     */
    public ComponentSystemEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p class="changed_added_2_3">
     * Pass the argument <code>component</code> to the superclass constructor.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component the <code>UIComponent</code> reference to be passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     *
     * @since 2.0
     */
    public ComponentSystemEvent(FacesContext facesContext, UIComponent component) {
        super(facesContext, component);
    }

    /**
     * <p class="changed_added_2_2">
     * Return <code>true</code> if the argument {@link FacesListener} is an instance of the appropriate listener class that
     * this event supports. The default implementation returns true if the listener is a
     * {@link ComponentSystemEventListener} or if <code>super.isAppropriateListener()</code> returns true.
     * </p>
     *
     * @param listener {@link FacesListener} to evaluate
     * @since 2.2
     */
    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        boolean result = listener instanceof ComponentSystemEventListener;
        if (!result) {
            result = super.isAppropriateListener(listener);
        }
        return result;
    }

    /**
     * <p class="changed_added_2_2">
     * Broadcast this event instance to the specified {@link FacesListener} by calling the superclass's
     * <code>processListener()</code> implementation.
     * </p>
     *
     * @param listener {@link FacesListener} to evaluate
     * @since 2.2
     */
    @Override
    public void processListener(FacesListener listener) {
        UIComponent c = getComponent();
        UIComponent cFromStack;
        boolean didPush = false;
        FacesContext context = FacesContext.getCurrentInstance();
        cFromStack = UIComponent.getCurrentComponent(context);
        if (null == cFromStack) {
            didPush = true;
            c.pushComponentToEL(context, null);
        }
        try {
            if (listener instanceof SystemEventListener) {
                super.processListener(listener);
            } else if (listener instanceof ComponentSystemEventListener) {
                ((ComponentSystemEventListener) listener).processEvent(this);
            }
        } finally {
            if (didPush) {
                c.popComponentFromEL(context);
            }
        }
    }

    // -------------------------------------------------------------- Properties

    /**
     * <p class="changed_added_2_0">
     * the source {@link UIComponent} that sent this event.
     * </p>
     *
     * @since 2.0
     *
     * @return the component for this event
     */
    public UIComponent getComponent() {

        return (UIComponent) getSource();

    }

}
