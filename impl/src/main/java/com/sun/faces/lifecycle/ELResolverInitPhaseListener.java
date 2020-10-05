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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.el.ELUtils;
import com.sun.faces.el.FacesCompositeELResolver;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;

/**
 * <p>
 * This class is used to register the Faces <code>ELResolver</code> stack with the Jakarta Server Pages container.
 * </p>
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
    private static Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();
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
            // remove ourselves from the list of listeners maintained by
            // the lifecycle instances
            for (Iterator<String> i = factory.getLifecycleIds(); i.hasNext();) {
                Lifecycle lifecycle = factory.getLifecycle(i.next());
                lifecycle.removePhaseListener(this);
            }
            postInitCompleted = true;
        }

    }

    /**
     * <p>
     * Handle a notification that the processing for a particular phase of the request processing lifecycle is about to
     * begin.
     * </p>
     *
     * <p>
     * The implementation of this method currently calls through to
     * {@link #populateFacesELResolverForJsp(jakarta.faces.context.FacesContext)}.
     * <p/>
     */

    @Override
    public synchronized void beforePhase(PhaseEvent event) {

        if (!preInitCompleted) {
            ApplicationAssociate associate = ApplicationAssociate.getInstance(FacesContext.getCurrentInstance().getExternalContext());
            associate.setRequestServiced();
            associate.initializeELResolverChains();
            associate.installProgrammaticallyAddedResolvers();
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

    /**
     * Populate the FacesCompositeELResolver stack registered with JSP if a request is being processed for the very first
     * time. At the application initialiazation time, an empty CompositeELResolver is registered with JSP because
     * ELResolvers can be added until the first request is serviced.
     *
     * @param context - the <code>FacesContext</code> for the current request
     */
    protected void populateFacesELResolverForJsp(FacesContext context) {

        ApplicationAssociate appAssociate = ApplicationAssociate.getInstance(context.getExternalContext());
        populateFacesELResolverForJsp(context.getApplication(), appAssociate);

    }

    public static void populateFacesELResolverForJsp(Application app, ApplicationAssociate appAssociate) {
        FacesCompositeELResolver compositeELResolverForJsp = appAssociate.getFacesELResolverForJsp();
        if (compositeELResolverForJsp == null) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "jsf.lifecycle.initphaselistener.resolvers_not_registered", new Object[] { appAssociate.getContextName() });
            }
            return;
        }

        ELUtils.buildJSPResolver(compositeELResolverForJsp, appAssociate);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "jsf.lifecycle.initphaselistener.resolvers_registered", new Object[] { appAssociate.getContextName() });
        }

    }

    public static void removeELResolverInitPhaseListener() {
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        // remove ourselves from the list of listeners maintained by
        // the lifecycle instances
        for (Iterator<String> i = factory.getLifecycleIds(); i.hasNext();) {
            Lifecycle lifecycle = factory.getLifecycle(i.next());
            for (PhaseListener cur : lifecycle.getPhaseListeners()) {
                if (cur instanceof ELResolverInitPhaseListener) {
                    lifecycle.removePhaseListener(cur);
                }
            }
        }

    }

} // END InitializingPhaseListener
