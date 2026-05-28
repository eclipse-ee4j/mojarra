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

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/** CDI-managed validator looked up by id.
 *  {@code @Dependent} is the bean-defining annotation that makes this class
 *  discoverable under the CDI 4.0 default {@code bean-discovery-mode=annotated}. */
@FacesValidator(value = "lengthRangeValidator", managed = true)
@Dependent
public class LengthRangeValidator implements Validator<String> {

    private static final int MIN = 1;
    private static final int MAX = 200;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return;
        }
        int length = value.length();
        if (length < MIN || length > MAX) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Length out of range", "Length must be between " + MIN + " and " + MAX));
        }
    }
}
