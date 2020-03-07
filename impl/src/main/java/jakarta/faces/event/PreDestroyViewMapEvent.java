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
 * This event must be published by a call to {@link jakarta.faces.application.Application#publishEvent} when the
 * <code>clear</code> method is called on the map returned from {@link UIViewRoot#getViewMap}.
 *
 * @since 2.0
 */
public class PreDestroyViewMapEvent extends ComponentSystemEvent {

    private static final long serialVersionUID = 4470489935758914483L;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Instantiate a new <code>ViewMapDestroydEvent</code> that indicates the argument <code>root</code> just had its
     * associated view map destroyed.
     * </p>
     *
     * @param root the <code>UIViewRoot</code> for which the view map has just been destroyed.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PreDestroyViewMapEvent(UIViewRoot root) {
        super(root);
    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>ViewMapDestroydEvent</code> that indicates the argument <code>root</code> just had its
     * associated view map destroyed.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param root the <code>UIViewRoot</code> for which the view map has just been destroyed.
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PreDestroyViewMapEvent(FacesContext facesContext, UIViewRoot root) {
        super(facesContext, root);
    }

}
