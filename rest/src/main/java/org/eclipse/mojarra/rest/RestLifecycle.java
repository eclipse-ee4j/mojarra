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
package org.eclipse.mojarra.rest;

import java.io.IOException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The RestLifecycle.
 */
@ApplicationScoped
@Named("org.eclipse.mojarra.rest.RestLifecycle")
public class RestLifecycle extends Lifecycle {

    /**
     * Stores the RestMappingMatcher.
     */
    @Inject
    private RestMappingMatcher restMappingMatcher;

    /**
     * Stores the RestMethodExecutor.
     */
    @Inject
    private RestMethodExecutor restMethodExecutor;
    
    /**
     * Stores the RestResponseMatcher.
     */
    @Inject
    private RestResponseMatcher restResponseMatcher;

    /**
     * Add a phase listener.
     *
     * <p>
     * This is ignored by the REST lifecycle.
     *
     * @param phaseListener the phase listener.
     */
    @Override
    public void addPhaseListener(PhaseListener phaseListener) {
    }

    @Override
    public void execute(FacesContext facesContext) throws FacesException {
        RestMappingMatch match = restMappingMatcher.match(facesContext);
        if (match != null) {
            Object result = restMethodExecutor.execute(facesContext, match);
            facesContext.getAttributes().put(
                    RestLifecycle.class.getPackage().getName() + ".RestResult", result);
        } else {
            try {
                facesContext.getExternalContext().responseSendError(404, "Unable to match request");
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
     * As phase listeners are ignored by the REST life-cycle this will always
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
     * This is ignored by the REST life-cycle.
     *
     * @param phaseListener the phase listener.
     */
    @Override
    public void removePhaseListener(PhaseListener phaseListener) {
    }

    @Override
    public void render(FacesContext facesContext) throws FacesException {
        if (!facesContext.getResponseComplete()) {
            ExternalContext externalContext = facesContext.getExternalContext();
            String responseContentType = externalContext.getResponseContentType();
            if (responseContentType == null) {
                externalContext.setResponseContentType("application/json");
                responseContentType = "application/json";
            }
            restResponseMatcher.getResponseWriter(responseContentType).writeResponse(facesContext);
        }
    }
}
