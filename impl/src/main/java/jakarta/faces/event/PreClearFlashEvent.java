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

import java.util.Map;

import jakarta.faces.context.FacesContext;

/**
 *
 * <p class="changed_added_2_2">
 * This event must be published by a call to {@link jakarta.faces.application.Application#publishEvent} before the flash
 * is cleared.
 * </p>
 *
 * @since 2.2
 */
public class PreClearFlashEvent extends SystemEvent {

    private static final long serialVersionUID = -6069648757590884651L;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_2">
     * Instantiate a new <code>PreClearFlashEvent</code> that indicates the argument <code>key</code> was just put to the
     * flash.
     * </p>
     *
     * @param source Map containing the values about to be cleared This need not be the actual
     * {@link jakarta.faces.context.Flash} instance.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PreClearFlashEvent(Map<String, Object> source) {
        super(source);
    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>PreClearFlashEvent</code> that indicates the argument <code>key</code> was just put to the
     * flash.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param source Map containing the values about to be cleared This need not be the actual
     * {@link jakarta.faces.context.Flash} instance.
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PreClearFlashEvent(FacesContext facesContext, Map<String, Object> source) {
        super(facesContext, source);
    }
}
