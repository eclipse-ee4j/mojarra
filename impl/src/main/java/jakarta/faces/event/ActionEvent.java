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
 * An {@link ActionEvent} represents the activation of a user interface component (such as a <code>UICommand</code>).
 * </p>
 */
public class ActionEvent extends FacesEvent {

    private static final long serialVersionUID = 2391694421423935722L;

    /**
     * <p class="changed_removed_2_3">
     * Construct a new event object from the specified source component and action command.
     * </p>
     *
     * @param component Source {@link UIComponent} for this event
     * @throws IllegalArgumentException if <code>component</code> is <code>null</code>
     */
    public ActionEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new event object from the Faces context, specified source component and action command.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component Source {@link UIComponent} for this event.
     * @throws IllegalArgumentException if <code>component</code> is <code>null</code>
     * @since 2.3
     */
    public ActionEvent(FacesContext facesContext, UIComponent component) {
        super(facesContext, component);
    }

    // ------------------------------------------------- Event Broadcast Methods

    @Override
    public boolean isAppropriateListener(FacesListener listener) {

        return listener instanceof ActionListener;

    }

    /**
     * @throws AbortProcessingException {@inheritDoc}
     */
    @Override
    public void processListener(FacesListener listener) {

        ((ActionListener) listener).processAction(this);

    }

}
