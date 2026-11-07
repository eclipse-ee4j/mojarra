/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue6138;

import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;

/**
 * Visits the whole form of the validated input, like a "fill at least one" check does.
 */
@FacesValidator(value = "formVisitValidator")
public class FormVisitValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        Issue6138 bean = context.getApplication().evaluateExpressionGet(context, "#{issue6138}", Issue6138.class);
        bean.getValidated().put(component.getClientId(context), value);
        UIComponent form = component;
        while (!(form instanceof UIForm)) {
            form = form.getParent();
        }
        form.visitTree(VisitContext.createVisitContext(context), (visitContext, target) -> {
            if (target instanceof EditableValueHolder) {
                ((EditableValueHolder) target).getSubmittedValue();
            }
            return VisitResult.ACCEPT;
        });
    }
}
