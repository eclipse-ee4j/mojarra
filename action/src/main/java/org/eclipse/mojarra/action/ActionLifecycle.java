/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.action;

import java.io.IOException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The ActionLifecycle.
 */
@ApplicationScoped
@Named("org.eclipse.mojarra.action.ActionLifecycle")
public class ActionLifecycle extends Lifecycle {

    /**
     * Stores the action mapping matcher.
     */
    @Inject
    private ActionMappingMatcher actionMappingMatcher;

    /**
     * Stores the action method executor.
     */
    @Inject
    private ActionMethodExecutor actionMethodExecutor;
    
    /**
     * Stores the action response handler.
     */
    @Inject
    private ActionResponseHandler actionResponseHandler;

    /**
     * Constructor.
     */
    public ActionLifecycle() {
    }

    /**
     * Add a phase listener.
     *
     * <p>
     * This is ignored by the ActionLifecycle.
     *
     * @param phaseListener the phase listener.
     */
    @Override
    public void addPhaseListener(PhaseListener phaseListener) {
    }

    /**
     * Perform the execute part of ActionLifecycle.
     *
     * @param facesContext the Faces context.
     * @throws FacesException when a serious error occurs.
     */
    @Override
    public void execute(FacesContext facesContext) throws FacesException {
        ActionMappingMatch match = actionMappingMatcher.match(facesContext);
        if (match != null) {
            actionMethodExecutor.execute(facesContext, match);
        } else {
            try {
                facesContext.getExternalContext().responseSendError(404, "Unable to match action");
                facesContext.responseComplete();
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        }
    }

    /**
     * Get the phase listeners.
     *
     * <p>
     * As phase listeners are ignored by the Action lifecycle this will always
     * return a zero length array.
     *
     * @return the empty array of phase listeners.
     */
    @Override
    public PhaseListener[] getPhaseListeners() {
        return new PhaseListener[0];
    }

    /**
     * Remove a phase listener.
     *
     * <p>
     * This is ignored by the Action lifecycle.
     *
     * @param phaseListener the phase listener.
     */
    @Override
    public void removePhaseListener(PhaseListener phaseListener) {
    }

    /**
     * Perform the render part of the Action lifecycle.
     *
     * @param facesContext the Faces context.
     * @throws FacesException when a serious error occurs.
     */
    @Override
    public void render(FacesContext facesContext) throws FacesException {
        if (!facesContext.getResponseComplete()) {
            actionResponseHandler.respond(facesContext);
        }
    }
}
