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

package jakarta.faces.validator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIInput;

/**
 * <p>
 * Unit tests for {@link RegexValidator}.</p>
 */
public class RegexValidatorTestCase extends ValidatorTestCase {

    // ------------------------------------------------- Individual Test Methods
    @Test
    public void testPatternMatch() {
        String patternStr = "t.*";
        RegexValidator validator = new RegexValidator();
        validator.setPattern(patternStr);
        UIInput component = new UIInput();
        String checkme = "test";
        try {
            validator.validate(facesContext, component, checkme);
            assertTrue(true);
        } catch (ValidatorException ve) {
            fail("Exception thrown " + ve.getMessage());
        }
    }

    @Test
    public void testPatterMismatch() {
        String patternStr = "t.*";
        facesContext.getViewRoot().setLocale(Locale.US);
        RegexValidator validator = new RegexValidator();
        validator.setPattern(patternStr);
        UIInput component = new UIInput();
        String checkme = "jest";
        try {
            validator.validate(facesContext, component, checkme);
            fail("Exception not thrown when tested " + checkme + " against " + patternStr);
        } catch (ValidatorException ve) {
            String detail = ve.getFacesMessage().getDetail();
            System.out.println("Detail in test: " + detail);
            assertTrue(detail.equalsIgnoreCase("Regex pattern of 't.*' not matched"));
        }
    }
}
