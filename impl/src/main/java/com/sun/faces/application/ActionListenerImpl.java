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

package com.sun.faces.application;

import static java.util.logging.Level.FINE;

import java.text.MessageFormat;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.MethodNotFoundException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

/**
 * This action listener implementation processes action events during the <em>Apply Request Values</em> or <em>Invoke
 * Application</em> phase of the request processing lifecycle (depending upon the <code>immediate</code> property of the
 * {@link ActionSource} that queued this event. It invokes the specified application action method, and uses the logical
 * outcome value to invoke the default navigation handler mechanism to determine which view should be displayed next.
 * </p>
 */
public class ActionListenerImpl implements ActionListener {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    // --------------------------------------------- Methods From ActionListener

    @SuppressWarnings("deprecation")
    @Override
    public void processAction(ActionEvent event) {

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("processAction({0})", event.getComponent().getId()));
        }

        UIComponent source = event.getComponent();
        FacesContext context = event.getFacesContext();

        MethodBinding binding = ((ActionSource) source).getAction();

        invokeNavigationHandling(context, source, binding, getNavigationOutcome(context, binding));

        // Trigger a switch to Render Response if needed
        context.renderResponse();
    }

    @SuppressWarnings("deprecation")
    private String getNavigationOutcome(FacesContext context, MethodBinding binding) {
        if (binding != null) {
            try {
                Object invokeResult;
                if ((invokeResult = binding.invoke(context, null)) != null) {
                    return invokeResult.toString();
                }
                // else, default to null, as returned at the end
            } catch (MethodNotFoundException e) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, e.getMessage(), e);
                }
                throw new FacesException(binding.getExpressionString() + ": " + e.getMessage(), e);
            } catch (EvaluationException e) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, e.getMessage(), e);
                }
                throw new FacesException(binding.getExpressionString() + ": " + e.getMessage(), e);
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    private void invokeNavigationHandling(FacesContext context, UIComponent source, MethodBinding binding, String outcome) {

        NavigationHandler navHandler = context.getApplication().getNavigationHandler();

        String toFlowDocumentId = (String) source.getAttributes().get(TO_FLOW_DOCUMENT_ID_ATTR_NAME);
        if (toFlowDocumentId == null) {
            navHandler.handleNavigation(context, binding != null ? binding.getExpressionString() : null, outcome);
        } else {
            navHandler.handleNavigation(context, binding != null ? binding.getExpressionString() : null, outcome, toFlowDocumentId);
        }
    }

}
