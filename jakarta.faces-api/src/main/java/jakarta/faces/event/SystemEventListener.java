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
 * By implementing this class, an object indicates that it is a listener for one or more kinds of {@link SystemEvent}s.
 * The exact type of event that will cause the implementing class's {@link #processEvent} method to be called is
 * indicated by the <code>facesEventClass</code> argument passed when the listener is installed using
 * {@link jakarta.faces.application.Application#subscribeToEvent}.
 * </p>
 *
 * @since 2.0
 */
public interface SystemEventListener extends FacesListener {

    /**
     * <p>
     * When called, the listener can assume that any guarantees given in the javadoc for the specific {@link SystemEvent}
     * subclass are true.
     * </p>
     *
     * @param event the <code>SystemEvent</code> instance that is being processed.
     *
     * @throws AbortProcessingException if lifecycle processing should cease for this request.
     */
    void processEvent(SystemEvent event) throws AbortProcessingException;

    /**
     * <p>
     * This method must return <code>true</code> if and only if this listener instance is interested in receiving events
     * from the instance referenced by the <code>source</code> parameter.
     * </p>
     *
     * @param source the source that is inquiring about the appropriateness of sending an event to this listener instance.
     *
     * @return the value as specified above
     */
    boolean isListenerForSource(Object source);

}
