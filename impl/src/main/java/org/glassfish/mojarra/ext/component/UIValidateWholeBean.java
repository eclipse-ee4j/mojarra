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

package org.glassfish.mojarra.ext.component;

import static jakarta.faces.validator.BeanValidator.EMPTY_VALIDATION_GROUPS_PATTERN;
import static jakarta.faces.validator.BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME;
import static jakarta.faces.validator.BeanValidator.VALIDATION_GROUPS_DELIMITER;
import static java.lang.Boolean.TRUE;
import static org.glassfish.mojarra.util.Util.reverse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.faces.FacesException;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.validator.Validator;
import jakarta.validation.groups.Default;

public class UIValidateWholeBean extends UIInput implements PartialStateHolder {

    // Precompiled form of EMPTY_VALIDATION_GROUPS_PATTERN; setValidationGroups runs per postback, so avoid recompiling.
    private static final Pattern EMPTY_VALIDATION_GROUPS = Pattern.compile(EMPTY_VALIDATION_GROUPS_PATTERN);

    private static final String ERROR_MISSING_FORM = "f:validateWholeBean must be nested directly in an UIForm.";

    private static final String ERROR_MISPLACED_COMPONENT = "f:validateWholeBean must be placed at the end of UIForm.";

    public static final String FAMILY = "org.glassfish.mojarra.ext.validateWholeBean";

    private transient Class<?>[] cachedValidationGroups;
    private transient String validationGroups = "";

    private boolean transientValue;
    private boolean initialState;

    private enum PropertyKeys {
        ValidatorInstalled
    }

    @Override
    public String getFamily() {
        return FAMILY;
    }

    @Override
    public Object getSubmittedValue() {
        return getFamily();
    }

    // ValueHolder/EditableValueHolder still declare these raw, so the overrides must match the raw erasure.
    @Override
    @SuppressWarnings("rawtypes")
    public void setConverter(Converter converter) {
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final void addValidator(Validator validator) {
        if (validator instanceof WholeBeanValidator) {
            super.addValidator(validator);
            setValidatorInstalled(true);
        }
    }

    public void setValidationGroups(String validationGroups) {
        clearInitialState();
        String newValidationGroups = validationGroups;

        // Treat empty list as null
        if (newValidationGroups != null && EMPTY_VALIDATION_GROUPS.matcher(newValidationGroups).matches()) {
            newValidationGroups = null;
        }
        // Only clear cache of validation group classes if value is changing
        if (newValidationGroups == null && validationGroups != null) {
            cachedValidationGroups = null;
        }
        if (newValidationGroups != null && validationGroups != null && !newValidationGroups.equals(validationGroups)) {
            cachedValidationGroups = null;
        }
        if (newValidationGroups != null && validationGroups == null) {
            cachedValidationGroups = null;
        }
        this.validationGroups = newValidationGroups;
    }

    public String getValidationGroups() {
        return validationGroups;
    }

    @Override
    public void validate(FacesContext context) {
        if (!wholeBeanValidationEnabled(context)) {
            return;
        }

        if (!isValidatorInstalled()) {
            WholeBeanValidator validator = new WholeBeanValidator();
            addValidator(validator);
        }

        super.validate(context);
    }

    @Override
    public void updateModel(FacesContext context) {
        // Don't update the model. #4313
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {

        // Check if the parent of this f:validateWholeBean is a form
        UIForm parent = getClosestParent(this, UIForm.class);
        if (parent == null) {
            throw new IllegalArgumentException(ERROR_MISSING_FORM);
        }

        misplacedComponentCheck(parent, getClientId());
    }

    private static void misplacedComponentCheck(UIComponent parentComponent, String clientId) throws IllegalArgumentException {
        try {
            reverse(parentComponent.getChildren()).stream().forEach((UIComponent childComponent) -> {
                if (childComponent.isRendered()) {
                    if (childComponent instanceof EditableValueHolder && !(childComponent instanceof UIValidateWholeBean)) {
                        throw new IllegalArgumentException(ERROR_MISPLACED_COMPONENT);
                    } else {
                        if (!childComponent.getClientId().equals(clientId)) {
                            misplacedComponentCheck(childComponent, clientId);
                        } else {
                            throw new BreakException();
                        }
                    }
                }
            });
        } catch (BreakException be) {
            // STOP
        }
    }

    public static <C extends UIComponent> C getClosestParent(UIComponent component, Class<C> parentType) {
        UIComponent parent = component.getParent();

        while (parent != null && !parentType.isInstance(parent)) {
            parent = parent.getParent();
        }

        return parentType.cast(parent);
    }

    private boolean isValidatorInstalled() {
        return getStateHelper().eval(PropertyKeys.ValidatorInstalled, false);
    }

    private void setValidatorInstalled(boolean newValue) {
        getStateHelper().put(PropertyKeys.ValidatorInstalled, newValue);
    }

    Class<?>[] getValidationGroupsArray() {

        if (cachedValidationGroups != null) {
            return cachedValidationGroups;
        }

        String validationGroupsStr = getValidationGroups();
        List<Class<?>> validationGroupsList = new ArrayList<>();

        for (String className : validationGroupsStr.split(VALIDATION_GROUPS_DELIMITER)) {
            className = className.trim();
            if (className.length() == 0) {
                continue;
            }

            if (className.equals(Default.class.getName())) {
                validationGroupsList.add(Default.class);
            } else {
                validationGroupsList.add(classForName(className));
            }
        }

        cachedValidationGroups = validationGroupsList.toArray(new Class<?>[validationGroupsList.size()]);

        return cachedValidationGroups;
    }

    private boolean wholeBeanValidationEnabled(FacesContext context) {
        return TRUE.equals(context.getAttributes().get(ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME));
    }

    @Override
    public void markInitialState() {
        initialState = true;
    }

    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    @Override
    public void clearInitialState() {
        initialState = false;
    }

    @Override
    public boolean isTransient() {
        return transientValue;
    }

    @Override
    public void setTransient(boolean transientValue) {
        this.transientValue = transientValue;
    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        Object[] result = null;
        if (!initialStateMarked()) {
            Object[] values = new Object[2];
            values[0] = validationGroups;
            values[1] = super.saveState(context);
            return values;
        }
        return result;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state != null) {
            Object[] values = (Object[]) state;
            validationGroups = (String) values[0];
            Object parentState = values[1];
            super.restoreState(context, parentState);
        }
    }

    // ----------------------------------------------------- Private helper methods

    private Class<?> classForName(String className) {
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e1) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e2) {
                throw new FacesException("Validation group not found: " + className);
            }
        }
    }

    private static class BreakException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
