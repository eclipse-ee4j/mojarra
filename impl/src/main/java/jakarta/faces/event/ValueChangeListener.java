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
 * A listener interface for receiving {@link ValueChangeEvent}s. A class that is interested in receiving such events
 * implements this interface, and then registers itself with the source {@link UIComponent} of interest, by calling
 * <code>addValueChangeListener()</code>.
 * </p>
 */

public interface ValueChangeListener extends FacesListener {

    /**
     * <p>
     * Invoked when the value change described by the specified {@link ValueChangeEvent} occurs.
     * </p>
     *
     * @param event The {@link ValueChangeEvent} that has occurred
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     */
    void processValueChange(ValueChangeEvent event) throws AbortProcessingException;

}
