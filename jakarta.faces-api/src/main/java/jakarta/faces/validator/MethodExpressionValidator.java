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

import static jakarta.faces.component.UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME;

import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput.ValidateEmptyFields;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_modified_2_0_rev_a">MethodExpressionValidator</strong> is a {@link Validator} that wraps a
 * {@link MethodExpression}, and it performs validation by executing a method on an object identified by the
 * {@link MethodExpression}.
 * </p>
 */

public class MethodExpressionValidator implements Validator, StateHolder {

    private static final String BEANS_VALIDATION_AVAILABLE = "jakarta.faces.private.BEANS_VALIDATION_AVAILABLE";

    // ------------------------------------------------------ Instance Variables

    private MethodExpression methodExpression = null;

    private Boolean validateEmptyFields;

    public MethodExpressionValidator() {

    }

    /**
     * <p>
     * Construct a {@link Validator} that contains a {@link MethodExpression}.
     * </p>
     *
     * @param methodExpression the expression to wrap
     */
    public MethodExpressionValidator(MethodExpression methodExpression) {

        this.methodExpression = methodExpression;

    }

    // ------------------------------------------------------- Validator Methods

    /**
     * @throws NullPointerException {@inheritDoc}
     * @throws ValidatorException {@inheritDoc}
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (validateEmptyFields(context) || value != null) {
            try {
                ELContext elContext = context.getELContext();
                methodExpression.invoke(elContext, new Object[] { context, component, value });
            } catch (ELException ee) {
                Throwable e = ee.getCause();
                if (e instanceof ValidatorException) {
                    throw (ValidatorException) e;
                } else {
                    throw ee;
                }
            }
        }
    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        Object values[] = new Object[1];
        values[0] = methodExpression;
        return values;

    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        Object values[] = (Object[]) state;
        methodExpression = (MethodExpression) values[0];
    }

    private boolean transientValue;

    @Override
    public boolean isTransient() {

        return transientValue;

    }

    @Override
    public void setTransient(boolean transientValue) {

        this.transientValue = transientValue;

    }

    private boolean validateEmptyFields(FacesContext ctx) {

        if (validateEmptyFields == null) {
            ValidateEmptyFields val = null;

            if (!ContextParam.VALIDATE_EMPTY_FIELDS.isSet(ctx)) {
                String appVal = (String) ctx.getExternalContext().getApplicationMap().get(VALIDATE_EMPTY_FIELDS_PARAM_NAME);

                if (appVal != null) {
                    val = ValidateEmptyFields.valueOf(appVal.toUpperCase());
                }
            }

            if (val == null) {
                val = ContextParam.VALIDATE_EMPTY_FIELDS.getValue(ctx);
            }

            if (val == ValidateEmptyFields.AUTO) {
                validateEmptyFields = isBeansValidationAvailable(ctx);
            } else {
                validateEmptyFields = val == ValidateEmptyFields.TRUE;
            }
        }

        return validateEmptyFields;

    }

    private boolean isBeansValidationAvailable(FacesContext context) {
        boolean result = false;

        Map<String, Object> appMap = context.getExternalContext().getApplicationMap();

        if (appMap.containsKey(BEANS_VALIDATION_AVAILABLE)) {
            result = (Boolean) appMap.get(BEANS_VALIDATION_AVAILABLE);
        } else {
            try {
                new BeanValidator();
                appMap.put(BEANS_VALIDATION_AVAILABLE, Boolean.TRUE);
                result = true;
            } catch (Throwable t) {
                appMap.put(BEANS_VALIDATION_AVAILABLE, Boolean.FALSE);
            }
        }

        return result;
    }
}
