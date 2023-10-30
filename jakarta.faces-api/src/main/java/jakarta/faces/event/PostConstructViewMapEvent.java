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
 *
 * <p class="changed_added_2_0">
 * This event must be published by a call to {jakarta.faces.application.Application#publishEvent} when the view map is
 * first created. This must happen on the first time a call is made to {@link UIViewRoot#getViewMap} on a
 * <code>UIViewRoot</code> instance. The source for this event is the <code>UIViewRoot</code>.
 * </p>
 *
 * @since 2.0
 */
public class PostConstructViewMapEvent extends ComponentSystemEvent {

    private static final long serialVersionUID = 8684338297976265379L;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Instantiate a new <code>PostConstructViewMapEvent</code> that indicates the argument <code>root</code> was just
     * associated with its view map.
     * </p>
     *
     * @param root the <code>UIViewRoot</code> for which a view map has just been created.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostConstructViewMapEvent(UIViewRoot root) {
        super(root);
    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>PostConstructViewMapEvent</code> that indicates the argument <code>root</code> was just
     * associated with its view map.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param root the <code>UIViewRoot</code> for which a view map has just been created.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostConstructViewMapEvent(FacesContext facesContext, UIViewRoot root) {
        super(facesContext, root);
    }
}
