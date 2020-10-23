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

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * The system event facility will create an instance of this class whenever
 * {@link jakarta.faces.application.Application#publishEvent} is called with <code>ExceptionQueuedEvent.class</code> as
 * <code>systemEventClass</code> argument. In this case, an instance of {@link ExceptionQueuedEventContext} must be
 * passed as the <code>source</code> argument. The specification for <code>publishEvent()</code>, requires the
 * instantiation of the <code>systemEventClass</code> argument, passing the <code>source</code> argument to the
 * constructor.
 * </p>
 *
 * @since 2.0
 */
public class ExceptionQueuedEvent extends SystemEvent {

    private static final long serialVersionUID = -3413872714571466618L;

    /**
     * <p class="changed_added_2_0">
     * Instantiate a new <code>ExceptionQueuedEvent</code> that indicates the argument
     * <code>ExceptionQueuedEventContext</code> occurred.
     * </p>
     *
     * @param eventContext the <code>ExceptionQueuedEventContext</code> that contextualizes this
     * <code>ExceptionQueuedEvent</code>.
     *
     * @since 2.0
     */

    public ExceptionQueuedEvent(ExceptionQueuedEventContext eventContext) {
        super(eventContext);
    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>ExceptionQueuedEvent</code> that indicates the argument
     * <code>ExceptionQueuedEventContext</code> occurred.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param eventContext the <code>ExceptionQueuedEventContext</code> that contextualizes this
     * <code>ExceptionQueuedEvent</code>.
     *
     * @since 2.0
     */
    public ExceptionQueuedEvent(FacesContext facesContext, ExceptionQueuedEventContext eventContext) {
        super(facesContext, eventContext);
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>ExceptionQueuedEventContext</code> for this event instance.
     * </p>
     *
     * @since 2.0
     *
     * @return the context
     */

    public ExceptionQueuedEventContext getContext() {
        return (ExceptionQueuedEventContext) getSource();
    }

}
