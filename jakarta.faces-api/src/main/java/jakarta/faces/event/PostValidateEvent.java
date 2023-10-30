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
 * <p class="changed_added_2_0 changed_added_2_1">
 * When an instance of this event is passed to {@link SystemEventListener#processEvent} or
 * {@link ComponentSystemEventListener#processEvent}, the listener implementation may assume that the
 * <code>source</code> of this event instance is the {@link UIComponent} instance that is that has just been validated.
 * </p>
 *
 * <p class="changed_added_2_1">
 * Components with children must publish this event after processing their child nodes in
 * {@link UIComponent#processValidators}. This is especially important for iterating components such as
 * <code>UIData</code>, and form components, such as <code>UIForm</code>.
 * </p>
 *
 * @since 2.0
 */
public class PostValidateEvent extends ComponentSystemEvent {

    // ------------------------------------------------------------ Constructors

    private static final long serialVersionUID = -4213993829669626297L;

    /**
     *
     * <p class="changed_added_2_0">
     * Constructor
     * </p>
     *
     * @param component the <code>UIComponent</code> that is about to be validated.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostValidateEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p class="changed_added_2_3">
     * Constructor
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component the <code>UIComponent</code> that is about to be validated.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostValidateEvent(FacesContext facesContext, UIComponent component) {
        super(facesContext, component);
    }
}
