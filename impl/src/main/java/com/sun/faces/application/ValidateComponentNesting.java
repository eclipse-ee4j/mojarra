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

import java.util.EnumSet;

import com.sun.faces.util.MessageUtils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewAction;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * <p>
 * ValidateFormComponentNesting performs component tree validation to assure {@link ActionSource}
 * and {@link EditableValueHolder} components are placed inside a form.
 * ValidateFormComponentNesting is installed automatically if {@link ProjectStage#Development} is active.
 * </p>
 *
 * @author dueni
 *
 */
public class ValidateComponentNesting implements SystemEventListener {

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof UIViewRoot;
    }

    /**
     * Process PostAddToViewEvent on UIViewRoot to validate form - action/input nesting.
     */
    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        UIComponent root = (UIComponent) event.getSource();
        FacesContext ctx = FacesContext.getCurrentInstance();
        EnumSet<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
        VisitContext visitContext = VisitContext.createVisitContext(ctx, null, hints);

        root.visitTree(visitContext, new ValidateFormNestingCallback());
    }

    static class ValidateFormNestingCallback implements VisitCallback {

        // report missing form problem only once
        boolean reportedOmittedFormOnce = false;

        // report missing metadata problem only once
        boolean reportedOmittedMetadataOnce = false;

        @Override
        public VisitResult visit(VisitContext context, UIComponent target) {
            // default result: continue tree walk
            VisitResult result = VisitResult.ACCEPT;

            if (target instanceof UIForm || target.getFamily().endsWith("Form") || UIViewRoot.METADATA_FACET_NAME.equals(target.getId())) {
                // stop tree walk if component is of type UIForm or component family ends on "Form"
                // or if the component is the UIPanel with id METADATA_FACET_NAME
                result = VisitResult.REJECT;
            } else if (target instanceof UIViewParameter || target instanceof UIViewAction) {
                if (reportedOmittedMetadataOnce) {
                    // report first detected problem only, then stop tree walk
                    result = VisitResult.COMPLETE;
                }
                addOmittedMessage(context.getFacesContext(), target.getClientId(context.getFacesContext()), MessageUtils.MISSING_METADATA_ERROR);
                reportedOmittedMetadataOnce = true;

            } else if (target instanceof EditableValueHolder || target instanceof ActionSource) {
                if (reportedOmittedFormOnce) {
                    // report first detected problem only, then stop tree walk
                    result = VisitResult.COMPLETE;
                }
                // if we find ActionSource or EditableValueHolder, that component
                // must be outside of a form add warning message
                addOmittedMessage(context.getFacesContext(), target.getClientId(context.getFacesContext()), MessageUtils.MISSING_FORM_ERROR);
                reportedOmittedFormOnce = true;

            }
            return result;
        }
    }

    /**
     * method for adding a message regarding missing ancestor to context
     *
     * @param ctx
     * @param clientId
     */
    private static void addOmittedMessage(FacesContext ctx, String clientId, String key) {
        Object[] params = new Object[] {};

        FacesMessage m = MessageUtils.getExceptionMessage(key, params);
        m.setSeverity(FacesMessage.SEVERITY_WARN);
        ctx.addMessage(clientId, m);
    }

}
