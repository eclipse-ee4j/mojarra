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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.lifecycle.ClientWindowFactory;
import jakarta.faces.lifecycle.Lifecycle;

/**
 * <p>
 * <b>LifecycleImpl</b> is the stock implementation of the standard Lifecycle in the Jakarta Faces RI.
 * </p>
 */

public class LifecycleImpl extends Lifecycle {

    // -------------------------------------------------------- Static Variables

    // Log instance for this class
    private static Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    // ------------------------------------------------------ Instance Variables

    // The Phase instance for the render() method
    private Phase response = new RenderResponsePhase();

    // The set of Phase instances that are executed by the execute() method
    // in order by the ordinal property of each phase
    private Phase[] phases = { null, // ANY_PHASE placeholder, not a real Phase
            new RestoreViewPhase(), new ApplyRequestValuesPhase(), new ProcessValidationsPhase(), new UpdateModelValuesPhase(), new InvokeApplicationPhase(),
            response };

    // List for registered PhaseListeners
    private List<PhaseListener> listeners = new CopyOnWriteArrayList<>();

    public LifecycleImpl() {
    }

    public LifecycleImpl(FacesContext context) {
    }


    // ------------------------------------------------------- Lifecycle Methods

    @Override
    public void attachWindow(FacesContext context) {
        if (context == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }

        ExternalContext extContext = context.getExternalContext();
        ClientWindow myWindow = extContext.getClientWindow();
        if (null == myWindow) {
            myWindow = createClientWindow(context);
            if (null != myWindow) {
                myWindow.decode(context);
                extContext.setClientWindow(myWindow);
            }
        }

        // If you need to do the "send down the HTML" trick, be sure to
        // mark responseComplete true after doing so. That way
        // the remaining lifecycle methods will not execute.

    }

    private ClientWindow createClientWindow(FacesContext context) {
        ClientWindowFactory clientWindowFactory = null;

        if (Util.isUnitTestModeEnabled()) {
            clientWindowFactory = new ClientWindowFactoryImpl(false);
        } else {
            clientWindowFactory = (ClientWindowFactory) FactoryFinder.getFactory(FactoryFinder.CLIENT_WINDOW_FACTORY);
        }

        return clientWindowFactory.getClientWindow(context);
    }

    // Execute the phases up to but not including Render Response
    @Override
    public void execute(FacesContext context) throws FacesException {

        if (context == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("execute(" + context + ")");
        }

        for (int i = 1, len = phases.length - 1; i < len; i++) { // Skip ANY_PHASE placeholder

            if (context.getRenderResponse() || context.getResponseComplete()) {
                break;
            }

            phases[i].doPhase(context, this, listeners.listIterator());

        }

    }

    // Execute the Render Response phase
    @Override
    public void render(FacesContext context) throws FacesException {

        if (context == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("render(" + context + ")");
        }

        if (!context.getResponseComplete()) {
            response.doPhase(context, this, listeners.listIterator());
        }

    }

    // Add a new PhaseListener to the set of registered listeners
    @Override
    public void addPhaseListener(PhaseListener listener) {

        if (listener == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "listener"));
        }

        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
        }

        if (listeners.contains(listener)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "faces.lifecycle.duplicate_phase_listener_detected", listener.getClass().getName());
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "addPhaseListener({0},{1})", new Object[] { listener.getPhaseId().toString(), listener.getClass().getName() });
            }
            listeners.add(listener);
        }

    }

    // Return the set of PhaseListeners that have been registered
    @Override
    public PhaseListener[] getPhaseListeners() {

        return listeners.toArray(new PhaseListener[listeners.size()]);

    }

    // Remove a registered PhaseListener from the set of registered listeners
    @Override
    public void removePhaseListener(PhaseListener listener) {

        if (listener == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "listener"));
        }

        if (listeners.remove(listener) && LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "removePhaseListener({0})", new Object[] { listener.getClass().getName() });
        }

    }

}
