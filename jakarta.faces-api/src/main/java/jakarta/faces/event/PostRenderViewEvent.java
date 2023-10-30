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

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * When an instance of this event is passed to {@link SystemEventListener#processEvent} or
 * {@link ComponentSystemEventListener#processEvent}, the listener implementation may assume that the
 * <code>source</code> of this event instance is the {@link UIViewRoot} instance that has just been rendered.
 * </p>
 *
 * @since 2.3
 */
public class PostRenderViewEvent extends ComponentSystemEvent {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 2790603812421768241L;

    /**
     *
     * <p class="changed_added_2_3">
     * Instantiate a new <code>PostRenderViewEvent</code> that indicates the argument <code>root</code> has just been
     * rendered.
     * </p>
     *
     * @param root the <code>UIViewRoot</code> that has just been rendered.
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostRenderViewEvent(UIViewRoot root) {
        super(root);
    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>PostRenderViewEvent</code> that indicates the argument <code>root</code> has just been
     * rendered.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param root the <code>UIViewRoot</code> that has just been rendered.
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostRenderViewEvent(FacesContext facesContext, UIViewRoot root) {
        super(facesContext, root);
    }
}
