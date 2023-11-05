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

import java.util.Locale;

import jakarta.faces.component.UIInput;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link LengthValidator}.</p>
 */
public class LengthValidatorTestCase extends ValidatorTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public LengthValidatorTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(LengthValidatorTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testLocaleHonored() {
        LengthValidator validator = new LengthValidator();
        validator.setMinimum(1000);
        validator.setMaximum(2000);
        boolean exceptionThrown = false;
        UIInput component = new UIInput();
        String message;
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);

        try {
            validator.validate(facesContext, component,
                    "Not at all long enough");
            fail("Exception not thrown");
        } catch (ValidatorException e) {
            exceptionThrown = true;
            message = e.getMessage();
            assertTrue("message: \"" + message + "\" missing localized chars.",
                    -1 != message.indexOf("1,000"));
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        Locale.setDefault(Locale.GERMAN);
        facesContext.getViewRoot().setLocale(Locale.GERMAN);

        try {
            validator.validate(facesContext, component,
                    "Still not long enough");
            fail("Exception not thrown");
        } catch (ValidatorException e) {
            exceptionThrown = true;
            message = e.getMessage();
            assertTrue("message: \"" + message + "\" missing localized chars.",
                    -1 != message.indexOf("1.000"));
        }
        assertTrue(exceptionThrown);
    }

    public void testHashCode() {
        LengthValidator validator1 = new LengthValidator();
        LengthValidator validator2 = new LengthValidator();

        validator1.setMinimum(10);
        validator1.setMaximum(15);
        validator2.setMinimum(10);
        validator2.setMaximum(15);

        assertTrue(validator1.hashCode() == validator2.hashCode());
        assertTrue(validator1.hashCode() == validator2.hashCode());

        validator2.setMaximum(16);

        assertTrue(validator1.hashCode() != validator2.hashCode());

        validator1 = new LengthValidator();
        validator2 = new LengthValidator();

        validator1.setMinimum(10);
        validator2.setMinimum(10);

        assertTrue(validator1.hashCode() == validator2.hashCode());
        assertTrue(validator1.hashCode() == validator2.hashCode());

        validator1.setMinimum(11);

        assertTrue(validator1.hashCode() != validator2.hashCode());

        validator1.setMinimum(10);
        validator1.setMaximum(10);

        assertTrue(validator1.hashCode() != validator2.hashCode());
    }
}
