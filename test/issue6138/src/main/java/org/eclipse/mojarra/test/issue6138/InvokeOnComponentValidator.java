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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;

/**
 * Invokes on the input in the first row of each iterating component, which is the validated input with each row index
 * in its client ID replaced by 0.
 */
@FacesValidator(value = "invokeOnComponentValidator")
public class InvokeOnComponentValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        Issue6138 bean = context.getApplication().evaluateExpressionGet(context, "#{issue6138}", Issue6138.class);
        bean.getValidated().put(component.getClientId(context), value);
        String clientId = component.getClientId(context).replaceAll(":\\d+:", ":0:");
        context.getViewRoot().invokeOnComponent(context, clientId, (callbackContext, target) -> target.getClientId(callbackContext));
    }
}
