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
 * <p class="changed_added_2_0">
 * When an instance of this event is passed to {@link SystemEventListener#processEvent} or
 * {@link ComponentSystemEventListener#processEvent}, the listener implementation may assume that the
 * <code>source</code> of this event instance is the {@link UIComponent} instance that is about to be rendered and that
 * it is safe to call {@link UIComponent#getParent}, {@link UIComponent#getClientId}, and other methods that depend upon
 * the component instance being in the view.
 * </p>
 *
 * @since 2.0
 */
public class PreRenderComponentEvent extends ComponentSystemEvent {

    // ------------------------------------------------------------ Constructors

    private static final long serialVersionUID = -938817831518520795L;

    /**
     *
     * <p class="changed_added_2_0">
     * Instantiate a new <code>PreRenderComponentEvent</code> that indicates the argument <code>component</code> is about to
     * be rendered.
     * </p>
     *
     * @param component the <code>UIComponent</code> that is about to be rendered.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PreRenderComponentEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>PreRenderComponentEvent</code> that indicates the argument <code>component</code> is about to
     * be rendered.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component the <code>UIComponent</code> that is about to be rendered.
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PreRenderComponentEvent(FacesContext facesContext, UIComponent component) {
        super(facesContext, component);
    }
}
