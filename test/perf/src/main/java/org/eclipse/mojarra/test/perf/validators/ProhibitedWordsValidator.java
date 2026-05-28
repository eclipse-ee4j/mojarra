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
package org.eclipse.mojarra.test.perf.validators;

import org.eclipse.mojarra.test.perf.beans.AppConfig;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;

/** CDI-managed validator looked up by id, exercising an @Inject chain. */
@FacesValidator(value = "prohibitedWordsValidator", managed = true)
public class ProhibitedWordsValidator implements Validator<String> {

    @Inject
    private AppConfig appConfig;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return;
        }
        // @Inject is null on Mojarra 4.0.18 (issue #5708, fixed in PR 5752):
        // InstanceFactory cached a non-CDI path for managed=true validators.
        // Null-guarded so the bench runs against both 4.0.18 and the perf branch.
        if (appConfig != null) {
            appConfig.getAppName();
        }
        if (value.equalsIgnoreCase("forbidden")) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Prohibited word", "This value is not allowed"));
        }
    }
}
