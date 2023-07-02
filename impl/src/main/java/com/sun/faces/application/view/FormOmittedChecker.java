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

package com.sun.faces.application.view;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.sun.faces.util.MessageUtils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.ActionSource2;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

/**
 * A convenience class that checks for omitted forms.
 */
class FormOmittedChecker {

    /**
     * Stores the skip hint.
     */
    private static final String SKIP_ITERATION_HINT = "jakarta.faces.visit.SKIP_ITERATION";

    /**
     * Constructor.
     */
    private FormOmittedChecker() {
    }

    /**
     * Check if omitted forms are present.
     *
     * @param context the Faces context.
     */
    public static void check(FacesContext context) {
        final FacesContext finalContext = context;
        UIViewRoot viewRoot = context.getViewRoot();
        List<UIComponent> children = viewRoot.getChildren();

        for (UIComponent child : children) {
            try {
                context.getAttributes().put(SKIP_ITERATION_HINT, true);
                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);

                VisitContext visitContext = VisitContext.createVisitContext(context, null, hints);
                child.visitTree(visitContext, (visitContext1, component) -> {
                    VisitResult result = VisitResult.ACCEPT;

                    if (isForm(component)) {
                        result = VisitResult.REJECT;
                    } else if (isInNeedOfForm(component)) {
                        addFormOmittedMessage(finalContext, component);
                    }
                    return result;
                });
            } finally {
                context.getAttributes().remove(SKIP_ITERATION_HINT);
            }
        }
    }

    /**
     * Is the component a form.
     *
     * <p>
     * Note normally a form inherits from UIForm, but there might be some component libraries out there that might not honor
     * that. So we check the component family to avoid warning in cases where 3rd party form component that does not extend
     * UIForm (eg. tr:form) is used.
     * </p>
     *
     * @param component the UI component.
     * @return true if it is a form, false otherwise.
     */
    private static boolean isForm(UIComponent component) {
        return component instanceof UIForm || component.getFamily() != null && component.getFamily().endsWith("Form");
    }

    /**
     * Is the component in need of a form.
     *
     * @param component the UI component.
     * @return true if the component is in need of a form, false otherwise.
     */
    private static boolean isInNeedOfForm(UIComponent component) {
        return component instanceof ActionSource || component instanceof ActionSource2 || component instanceof EditableValueHolder;
    }

    /**
     * Add the form omitted message.
     *
     * @param context the Faces context.
     * @param component the UI component.
     */
    private static void addFormOmittedMessage(FacesContext context, UIComponent component) {
        String key = MessageUtils.MISSING_FORM_ERROR;
        Object[] parameters = new Object[] { component.getClientId(context) };
        boolean missingFormReported = false;

        FacesMessage message = MessageUtils.getExceptionMessage(key, parameters);
        List<FacesMessage> messages = context.getMessageList();
        for (FacesMessage item : messages) {
            if (item.getDetail().equals(message.getDetail())) {
                missingFormReported = true;
                break;
            }
        }
        if (!missingFormReported) {
            message.setSeverity(FacesMessage.SEVERITY_WARN);
            context.addMessage(null, message);
        }
    }
}
