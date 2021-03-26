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

package com.sun.faces.lifecycle;

import java.util.Iterator;

import com.sun.faces.application.ApplicationAssociate;

import jakarta.faces.FactoryFinder;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;

/**
 * This class is used to register the Faces <code>ELResolver</code> stack with the Jakarta Server Pages container.
 *
 * <p>
 * We overload it a bit to set a bit on the ApplicationAssociate stating we've processed a request to indicate the
 * appliation is fully initialized.
 * </p>
 *
 * <p>
 * After the first request, this <code>PhaseListener</code> will remove itself from all registered lifecycle instances
 * registered with the application.
 * </p>
 *
 * @since 1.2
 */
public class ELResolverInitPhaseListener implements PhaseListener {

    private static final long serialVersionUID = -1430099294315211489L;
    private boolean postInitCompleted;
    private boolean preInitCompleted;

    // ---------------------------------------------- Methods From PhaseListener

    /**
     * <p>
     * Handle a notification that the processing for a particular phase has just been completed.
     * </p>
     *
     * <p>
     * When invoked, this phase listener will remove itself as a registered <code>PhaseListener</code> with all
     * <code>Lifecycle</code> instances.
     */
    @Override
    public synchronized void afterPhase(PhaseEvent event) {
        if (!postInitCompleted && PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
            ApplicationAssociate associate = ApplicationAssociate.getInstance(event.getFacesContext().getExternalContext());
            associate.setRequestServiced();
            LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);

            // Remove ourselves from the list of listeners maintained by
            // the lifecycle instances
            for (Iterator<String> i = factory.getLifecycleIds(); i.hasNext();) {
                Lifecycle lifecycle = factory.getLifecycle(i.next());
                lifecycle.removePhaseListener(this);
            }

            postInitCompleted = true;
        }

    }

    @Override
    public synchronized void beforePhase(PhaseEvent event) {
        if (!preInitCompleted) {
            ApplicationAssociate associate = ApplicationAssociate.getInstance();
            associate.setRequestServiced();
            associate.initializeELResolverChains();
            preInitCompleted = true;
        }
    }

    /**
     * <p>
     * Return the identifier of the request processing phase during which this listener is interested in processing
     * {@link jakarta.faces.event.PhaseEvent} events. Legal values are the singleton instances defined by the
     * {@link jakarta.faces.event.PhaseId} class, including <code>PhaseId.ANY_PHASE</code> to indicate an interest in being
     * notified for all standard phases.
     * </p>
     *
     * <p>
     * We return <code>PhaseId.ANY_PHASE</code>.
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    // ------------------------------------------------------- Protected Methods


    public static void removeELResolverInitPhaseListener() {
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);

        // Remove ourselves from the list of listeners maintained by
        // the lifecycle instances
        for (Iterator<String> i = factory.getLifecycleIds(); i.hasNext();) {
            Lifecycle lifecycle = factory.getLifecycle(i.next());
            for (PhaseListener phaseListener : lifecycle.getPhaseListeners()) {
                if (phaseListener instanceof ELResolverInitPhaseListener) {
                    lifecycle.removePhaseListener(phaseListener);
                }
            }
        }

    }

}
