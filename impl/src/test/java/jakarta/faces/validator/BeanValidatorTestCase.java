/*
 * Copyright (c) 2022, 2022 Contributors to Eclipse Foundation.
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

import jakarta.faces.component.UIInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * <p>Unit tests for {@link BeanValidator}.</p>
 *
 * @author rmartinc
 */
public class BeanValidatorTestCase extends ValidatorTestCase {

    /**
     * Test class for the bean validator.
     */
    public static class TestBean {

        @NotNull
        @Size(min = 1, max = 64)
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public BeanValidatorTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(BeanValidatorTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testMessageOK() {
        BeanValidator validator = new BeanValidator();
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);
        UIInput component = new UIInput();
        request.setAttribute("test", new TestBean());
        component.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{test.message}", String.class));

        validator.validate(facesContext, component, "something");
    }

    public void testMessageKO() {
        BeanValidator validator = new BeanValidator();
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);
        UIInput component = new UIInput();
        request.setAttribute("test", new TestBean());
        component.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{test.message}", String.class));

        try {
            validator.validate(facesContext, component, "");
            Assert.fail("ValidatorException expected");
        } catch (ValidatorException e) {
            Assert.assertEquals("size must be between 1 and 64", e.getMessage());
        }
    }

    public void testNoBase() {
        BeanValidator validator = new BeanValidator();
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);
        UIInput component = new UIInput();
        component.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{something}", String.class));

        validator.validate(facesContext, component, "something");
    }
}
