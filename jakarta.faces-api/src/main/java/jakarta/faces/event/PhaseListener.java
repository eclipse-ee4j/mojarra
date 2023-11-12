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

import java.io.Serializable;
import java.util.EventListener;

/**
 * <p>
 * An interface implemented by objects that wish to be notified at the beginning and ending of processing for each
 * standard phase of the request processing lifecycle.
 * </p>
 */

public interface PhaseListener extends EventListener, Serializable {

    /**
     * <p>
     * Handle a notification that the processing for a particular phase has just been completed.
     * </p>
     *
     * @param event the phase event.
     */
    default void afterPhase(PhaseEvent event) {
        
    }

    /**
     * <p>
     * Handle a notification that the processing for a particular phase of the request processing lifecycle is about to
     * begin.
     * </p>
     *
     * @param event the phase event.
     */
    default void beforePhase(PhaseEvent event) {
        
    }

    /**
     * <p>
     * Return the identifier of the request processing phase during which this listener is interested in processing
     * {@link PhaseEvent} events. Legal values are the singleton instances defined by the {@link PhaseId} class, including
     * <code>PhaseId.ANY_PHASE</code> to indicate an interest in being notified for all standard phases.
     * </p>
     *
     * @return the phase id.
     */
    PhaseId getPhaseId();

}
