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

package com.sun.faces.ext.component;

import static com.sun.faces.ext.component.MessageFactory.getLabel;
import static com.sun.faces.ext.component.MessageFactory.getMessage;
import static com.sun.faces.util.BeanValidation.getBeanValidator;
import static com.sun.faces.util.ReflectionUtils.setProperties;
import static com.sun.faces.util.Util.getValueExpressionNullSafe;
import static com.sun.faces.util.copier.CopierUtils.getCopier;
import static jakarta.faces.component.visit.VisitContext.createVisitContext;
import static jakarta.faces.component.visit.VisitResult.ACCEPT;
import static jakarta.faces.validator.BeanValidator.MESSAGE_ID;
import static jakarta.faces.validator.BeanValidator.VALIDATOR_ID;
import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.validation.ConstraintViolation;

class WholeBeanValidator implements Validator<Object> {
    private static final Logger LOGGER = Logger.getLogger("jakarta.faces.validator", "jakarta.faces.LogStrings");

    private static final String ERROR_MISSING_FORM = "f:validateWholeBean must be nested directly in an UIForm.";

    static final String MULTI_FIELD_VALIDATION_CANDIDATES = VALIDATOR_ID + ".MULTI_FIELD_VALIDATION_CANDIDATES";

    /**
     * <p class="changed_added_2_3">
     * Special value to indicate the proposed value for a property failed field-level validation. This prevents any attempt
     * to perform class level validation.
     * </p>
     */
    static final String FAILED_FIELD_LEVEL_VALIDATION = VALIDATOR_ID + ".FAILED_FIELD_LEVEL_VALIDATION";

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        validate(context, (UIValidateWholeBean) component, value);
    }

    public void validate(FacesContext context, UIValidateWholeBean component, Object value) throws ValidatorException {

        // Get parent and check if the parent of this f:validateWholeBean is a form
        UIForm form = getParentForm(component);

        ValueExpression wholeBeanVE = getValueExpressionNullSafe(component, "value");

        // The "whole" bean that we're going to validate at the class level
        Object wholeBean = wholeBeanVE.getValue(context.getELContext());

        // A shortened or fully qualified class name, or EL expression pointing
        // to a type that copies the target bean for validation
        String copierType = (String) component.getAttributes().get("copierType");

        // Inspect the status of field level validation
        if (hasAnyBeanPropertyFailedValidation(context, wholeBean)) {
            return;
        }

        AddRemainingCandidateFieldsCallback addRemainingCandidateFieldsCallback = new AddRemainingCandidateFieldsCallback(context, wholeBean);

        form.visitTree(createVisitContext(context), addRemainingCandidateFieldsCallback);

        Map<String, Map<String, Object>> validationCandidate = addRemainingCandidateFieldsCallback.getCandidate();

        if (validationCandidate.isEmpty()) {
            return;
        }

        // Perform the actual bean validation on a copy of the whole bean
        Set<ConstraintViolation<?>> violations = doBeanValidation(getBeanValidator(context),
                copyBeanAndPopulateWithCandidateValues(context, wholeBeanVE, wholeBean, copierType, validationCandidate), component.getValidationGroupsArray(),
                wholeBeanVE);

        // If there are any violations, transform them into a Faces validator exception
        if (violations != null && !violations.isEmpty()) {
            ValidatorException toThrow;

            if (violations.size() == 1) {
                ConstraintViolation<?> violation = violations.iterator().next();
                toThrow = new ValidatorException(getMessage(context, MESSAGE_ID, violation.getMessage(), getLabel(context, component)));
            } else {
                Set<FacesMessage> messages = new LinkedHashSet<>(violations.size());
                for (ConstraintViolation<?> violation : violations) {
                    messages.add(getMessage(context, MESSAGE_ID, violation.getMessage(), getLabel(context, component)));
                }
                toThrow = new ValidatorException(messages);
            }

            // Mark the components as invalid to prevent them from receiving
            // values during updateModelValues
            for (Entry<String, Map<String, Object>> validationCandidateEntry : validationCandidate.entrySet()) {
                invalidateComponent(validationCandidateEntry);
            }

            throw toThrow;
        }
    }

    private UIForm getParentForm(UIComponent component) {
        UIComponent parent = component.getParent();
        if (!(parent instanceof UIForm)) {
            throw new IllegalArgumentException(ERROR_MISSING_FORM);
        }

        return (UIForm) parent;
    }

    private boolean isFailedFieldLevelValidation(Entry<String, Map<String, Object>> wholeBeanPropertyEntry) {
        return FAILED_FIELD_LEVEL_VALIDATION.equals(wholeBeanPropertyEntry.getValue().get("value"));
    }

    private void invalidateComponent(Entry<String, Map<String, Object>> wholeBeanPropertyEntry) {
        ((EditableValueHolder) wholeBeanPropertyEntry.getValue().get("component")).setValid(false);
    }

    private boolean hasAnyBeanPropertyFailedValidation(FacesContext context, Object wholeBean) {
        Map<Object, Map<String, Map<String, Object>>> validationCandidates = getMultiFieldValidationCandidates(context, false);

        if (context.isValidationFailed()) {
            return true;
        }

        if (!validationCandidates.isEmpty() && validationCandidates.containsKey(wholeBean)) {
            // Verify that none of the field level properties failed validation
            for (Entry<String, Map<String, Object>> wholeBeanPropertyEntry : validationCandidates.get(wholeBean).entrySet()) {
                if (isFailedFieldLevelValidation(wholeBeanPropertyEntry)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Object copyBeanAndPopulateWithCandidateValues(FacesContext context, ValueExpression wholeBeanVE, Object wholeBean, String copierType,
            Map<String, Map<String, Object>> candidate) {

        // Populate the bean copy with the validated values from the candidate
        Map<String, Object> propertiesToSet = new HashMap<>();
        for (Entry<String, Map<String, Object>> propertyEntry : candidate.entrySet()) {
            propertiesToSet.put(propertyEntry.getKey(), propertyEntry.getValue().get("value"));
        }

        // Copy the whole bean so that class-level validation can be performed
        // without corrupting the real whole bean

        Object wholeBeanCopy = getCopier(context, copierType).copy(wholeBean);

        if (wholeBeanCopy == null) {
            throw new FacesException("Unable to copy bean from " + wholeBeanVE.getExpressionString());
        }

        setProperties(wholeBeanCopy, propertiesToSet);

        return wholeBeanCopy;
    }

    private Set<ConstraintViolation<?>> doBeanValidation(jakarta.validation.Validator beanValidator, Object wholeBeanCopy, Class<?>[] validationGroupArray,
            ValueExpression wholeBeanVE) {

        @SuppressWarnings("rawtypes")
        Set violationsRaw = null;

        try {
            violationsRaw = beanValidator.validate(wholeBeanCopy, validationGroupArray);
        } catch (IllegalArgumentException iae) {
            LOGGER.fine("Unable to validate expression " + wholeBeanVE.getExpressionString() + " using Bean Validation.  Unable to get value of expression. "
                    + " Message from Bean Validation: " + iae.getMessage());
        }

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> violations = violationsRaw;

        return violations;
    }

    private static class AddRemainingCandidateFieldsCallback implements VisitCallback {

        private final FacesContext context;
        private final Object base;
        private final Map<String, Map<String, Object>> candidate = new HashMap<>();

        public AddRemainingCandidateFieldsCallback(final FacesContext context, final Object base) {
            this.context = context;
            this.base = base;
        }

        final Map<String, Map<String, Object>> getCandidate() {
            return candidate;
        }

        @Override
        public VisitResult visit(VisitContext visitContext, UIComponent component) {
            if (component instanceof EditableValueHolder && component.isRendered() && !(component instanceof UIValidateWholeBean)) {
                ValueExpression valueExpression = component.getValueExpression("value");

                if (valueExpression != null) {

                    ValueReference valueReference = valueExpression.getValueReference(context.getELContext());

                    if (valueReference != null && valueReference.getBase().equals(base)) {
                        Map<String, Object> tuple = new HashMap<>();
                        tuple.put("component", component);
                        tuple.put("value", getComponentValue(component));

                        candidate.put(valueReference.getProperty().toString(), tuple);
                    }
                }
            }

            return ACCEPT;
        }

        private static Object getComponentValue(UIComponent component) {
            UIInput inputComponent = (UIInput) component;

            return inputComponent.getSubmittedValue() != null ? inputComponent.getSubmittedValue() : inputComponent.getLocalValue();
        }
    }

    /*
     * <p class="changed_added_2_3">Returns a data structure that stores the information necessary to perform class-level
     * validation by <code>&lt;f:validateWholeBean &gt;</code> components elsewhere in the tree. The lifetime of this data
     * structure does not extend beyond the current {@code FacesContext}. The data structure must conform to the following
     * specification.</p>
     *
     * <div class="changed_added_2_3">
     *
     * <ul>
     *
     * <li><p>It is a non-thread-safe {@code Map}.</p></li>
     *
     * <li><p>Keys are CDI bean instances that are referenced by the {@code value} attribute of
     * <code>&lt;f:validateWholeBean &gt;</code> components.</p></li>
     *
     * <li>
     *
     * <p>Values are {@code Map}s that represent the properties to be stored on the CDI bean instance that is the current
     * key. The inner {@code Map} must conform to the following specification.</p>
     *
     * <ul>
     *
     * <li><p>It is a non-thread-safe {@code Map}.</p></li>
     *
     * <li><p>Keys are property names.</p></li>
     *
     * <li><p>Values are {@code Map} instances. In this innermost map, the following keys are supported.</p>
     *
     * <p>component: Object that is the EditableValueHolder</p> <p>value: Object that is the value of the property</p>
     *
     * </li>
     *
     * </ul>
     *
     * </li>
     *
     *
     *
     * </ul>
     *
     * </div>
     *
     * @param context the {@link FacesContext} for this request
     *
     * @param create if {@code true}, the data structure must be created if not present. If {@code false} the data structure
     * must not be created and {@code Collections.emptyMap()} must be returned.
     *
     * @return the data structure representing the multi-field validation candidates
     *
     * @since 2.3
     */
    static Map<Object, Map<String, Map<String, Object>>> getMultiFieldValidationCandidates(FacesContext context, boolean create) {
        Map<Object, Object> attrs = context.getAttributes();

        @SuppressWarnings("unchecked")
        Map<Object, Map<String, Map<String, Object>>> multiFieldValidationCandidates = (Map<Object, Map<String, Map<String, Object>>>) attrs.get(MULTI_FIELD_VALIDATION_CANDIDATES);
        if (multiFieldValidationCandidates == null) {
            if (create) {
                multiFieldValidationCandidates = new HashMap<>();
                attrs.put(MULTI_FIELD_VALIDATION_CANDIDATES, multiFieldValidationCandidates);
            } else {
                multiFieldValidationCandidates = emptyMap();
            }
        }

        return multiFieldValidationCandidates;
    }

}
