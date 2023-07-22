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

/**
 * <p>
 * <span class="changed_modified_2_0 changed_modified_2_2">A</span> listener interface for receiving
 * {@link ActionEvent}s. <span class="changed_added_2_0">An implementation of this interface must be thread-safe.</span>
 * A class that is interested in receiving such events implements this interface, and then registers itself with the
 * source {@link UIComponent} of interest, by calling <code>addActionListener()</code>.
 * </p>
 */

public interface ActionListener extends FacesListener {

    /**
     * <p class="changed_added_2_2">
     * The presence of this component attribute on an {@link jakarta.faces.component.ActionSource} component will cause the
     * default {@code ActionListener} to interpret the value of the attribute as the <em>toFlowDocumentId</em> value to pass
     * to
     * {@link jakarta.faces.application.NavigationHandler#handleNavigation(jakarta.faces.context.FacesContext, java.lang.String, java.lang.String, java.lang.String)}.
     * </p>
     *
     */
    String TO_FLOW_DOCUMENT_ID_ATTR_NAME = "to-flow-document-id";

    /**
     * <p>
     * Invoked when the action described by the specified {@link ActionEvent} occurs.
     * </p>
     *
     * @param event The {@link ActionEvent} that has occurred
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     */
    void processAction(ActionEvent event) throws AbortProcessingException;

}
