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

import java.util.EventObject;

import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.Lifecycle;

/**
 * <p>
 * <strong>PhaseEvent</strong> represents the beginning or ending of processing for a particular phase of the request
 * processing lifecycle, for the request encapsulated by the specified {@link FacesContext}.
 * </p>
 */

public class PhaseEvent extends EventObject {

    // ----------------------------------------------------------- Constructors

    private static final long serialVersionUID = 7603034985956521464L;

    /**
     * <p>
     * Construct a new event object from the specified parameters. The specified {@link Lifecycle} will be the source of
     * this event.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param phaseId Identifier of the current request processing lifecycle phase
     * @param lifecycle Lifecycle instance
     *
     * @throws NullPointerException if <code>context</code> or <code>phaseId</code> or <code>Lifecycle</code>is
     * <code>null</code>
     */
    public PhaseEvent(FacesContext context, PhaseId phaseId, Lifecycle lifecycle) {

        super(lifecycle);
        if (phaseId == null || context == null || lifecycle == null) {
            throw new NullPointerException();
        }
        this.phaseId = phaseId;
        this.context = context;

    }

    // ------------------------------------------------------------- Properties

    private final FacesContext context;

    /**
     * <p>
     * Return the {@link FacesContext} for the request being processed.
     * </p>
     *
     * @return the {@code FacesContext} for the current request.
     *
     */
    public FacesContext getFacesContext() {

        return context;

    }

    private final PhaseId phaseId;

    /**
     * <p>
     * Return the {@link PhaseId} representing the current request processing lifecycle phase.
     * </p>
     *
     * @return the phase id
     */
    public PhaseId getPhaseId() {

        return phaseId;

    }

}
