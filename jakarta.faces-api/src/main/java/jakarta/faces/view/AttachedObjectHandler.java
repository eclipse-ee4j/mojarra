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

package jakarta.faces.view;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * The abstract base interface for a handler representing an <em>attached object</em> in a VDL page. Subinterfaces are
 * provided for the common attached objects that expose {@link jakarta.faces.component.behavior.Behavior},
 * {@link jakarta.faces.convert.Converter}s, {@link jakarta.faces.validator.Validator}s,
 * {@link jakarta.faces.event.ValueChangeListener}s, and {@link jakarta.faces.event.ActionListener}s for use by <em>page
 * authors</em>.
 * </p>
 *
 * @since 2.0
 */
public interface AttachedObjectHandler {

    /**
     * <p class="changed_added_2_0">
     * Take the argument <code>parent</code> and apply this attached object to it. The action taken varies with class that
     * implements one of the subinterfaces of this interface.
     * </p>
     *
     * @param context The <code>FacesContext</code> for this request
     * @param parent The <code>UIComponent</code> to which this particular attached object must be applied.
     */
    void applyAttachedObject(FacesContext context, UIComponent parent);

    /**
     * <p class="changed_added_2_0">
     * Return the value of the "for" attribute specified by the <em>page author</em> on the tag for this
     * <code>AttachedObjectHandler</code>.
     * </p>
     *
     * @return the value of the "for" attribute for this attached object
     */
    String getFor();

}
