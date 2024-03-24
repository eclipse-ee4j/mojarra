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

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    // ------------------------------------------------- Individual Test Methods
    @Test
    public void testMessageOK() {
        BeanValidator validator = new BeanValidator();
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);
        UIInput component = new UIInput();
        request.setAttribute("test", new TestBean());
        component.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{test.message}", String.class));

        validator.validate(facesContext, component, "something");
    }

    @Test
    public void testMessageKO() {
        BeanValidator validator = new BeanValidator();
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);
        UIInput component = new UIInput();
        request.setAttribute("test", new TestBean());
        component.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{test.message}", String.class));

        try {
            validator.validate(facesContext, component, "");
            Assertions.fail("ValidatorException expected");
        } catch (ValidatorException e) {
            Assertions.assertEquals("size must be between 1 and 64", e.getMessage());
        }
    }

    @Test
    public void testNoBase() {
        BeanValidator validator = new BeanValidator();
        Locale.setDefault(Locale.US);
        facesContext.getViewRoot().setLocale(Locale.US);
        UIInput component = new UIInput();
        component.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{something}", String.class));

        validator.validate(facesContext, component, "something");
    }
}
