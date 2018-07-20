/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;

import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.sun.faces.util.FacesLogger;

/**
 * This action listener implementation processes action events during the
 * <em>Apply Request Values</em> or <em>Invoke Application</em>
 * phase of the request processing lifecycle (depending upon the
 * <code>immediate</code> property of the {@link ActionSource} that
 * queued this event.
 *
 * <p>
 * It invokes the specified application action method,
 * and uses the logical outcome value to invoke the default navigation handler
 * mechanism to determine which view should be displayed next.</p>
 */
public class ActionListenerImpl implements ActionListener {


    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();


    // --------------------------------------------- Methods From ActionListener


    @Override
    public void processAction(ActionEvent event) {

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(format("processAction({0})", event.getComponent().getId()));
        }

        UIComponent source = event.getComponent();
        FacesContext context = event.getFacesContext();

        MethodExpression expression = ((ActionSource2) source).getActionExpression();

        invokeNavigationHandling(
            context, source, expression,
            getNavigationOutcome(context, expression));

        // Trigger a switch to Render Response if needed
        context.renderResponse();
    }

    private String getNavigationOutcome(FacesContext context, MethodExpression expression) {
        if (expression == null) {
            return null;
        }

        try {
            Object invokeResult = expression.invoke(context.getELContext(), null);
            if (invokeResult == null) {
                return null;
            }

            return invokeResult.toString();
        } catch (ELException | NullPointerException e) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, e.getMessage(), e);
            }

            throw new FacesException(expression.getExpressionString() + ": " + e.getMessage(), e);
        }
    }

    private void invokeNavigationHandling(FacesContext context, UIComponent source, MethodExpression expression, String outcome) {

        NavigationHandler navHandler = context.getApplication().getNavigationHandler();

        String toFlowDocumentId = (String) source.getAttributes().get(TO_FLOW_DOCUMENT_ID_ATTR_NAME);

        if (toFlowDocumentId == null) {
            navHandler.handleNavigation(context,
                    expression != null ?
                    expression.getExpressionString() : null,
                    outcome);
        } else {
            navHandler.handleNavigation(context,
                    expression != null ?
                    expression.getExpressionString() : null,
                    outcome, toFlowDocumentId);
        }
    }

}
