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

/**
 * <p class="changed_added_2_0">
 * Implementors of this class do not need an <code>isListenerForSource()</code> method because they are only installed
 * on specific component instances, therefore the <code>isListenerForSource()</code> method is implicit. Also, the
 * {@link #processEvent} method on this interface takes a {@link ComponentSystemEvent} because the event will always be
 * associated with a {@link jakarta.faces.component.UIComponent} instance.
 * </p>
 *
 * @since 2.0
 */
public interface ComponentSystemEventListener extends FacesListener {

    /**
     * <p>
     * When called, the listener can assume that any guarantees given in the javadoc for the specific {@link SystemEvent}
     * subclass are true.
     * </p>
     *
     * @param event the <code>ComponentSystemEvent</code> instance that is being processed.
     *
     * @throws AbortProcessingException if lifecycle processing should cease for this request.
     */
    void processEvent(ComponentSystemEvent event) throws AbortProcessingException;

}
