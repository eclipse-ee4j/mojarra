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

import static jakarta.faces.validator.MessageFactory.getLabel;
import static jakarta.faces.validator.MessageFactory.getMessage;
import static jakarta.faces.validator.MultiFieldValidationUtils.FAILED_FIELD_LEVEL_VALIDATION;
import static jakarta.faces.validator.MultiFieldValidationUtils.getMultiFieldValidationCandidates;
import static jakarta.faces.validator.MultiFieldValidationUtils.wholeBeanValidationEnabled;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.CompositeComponentExpressionHolder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_0_rev_a changed_modified_2_3">A Validator</span> that delegates validation of the
 * bean property to the Bean Validation API.
 * </p>
 *
 * @since 2.0
 */
public class BeanValidator implements Validator, PartialStateHolder {

    private static final Logger LOGGER = Logger.getLogger("jakarta.faces.validator", "jakarta.faces.LogStrings");

    private String validationGroups;

    private transient Class<?>[] cachedValidationGroups;

    /**
     * <p class="changed_added_2_0">
     * The standard validator id for this validator, as defined by the Jakarta Server Face specification.
     * </p>
     */
    public static final String VALIDATOR_ID = "jakarta.faces.Bean";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a constraint failure is
     * found. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the interpolated message from Bean Validation.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     * <p>
     * The message format string provided by the default implementation should be a the placeholder {0}, thus fully
     * delegating the message handling to Bean Validation. A developer can override this message format string to make it
     * conform to other Jakarta Server Face validator messages (i.e., by including the component label)
     * </p>
     */
    public static final String MESSAGE_ID = "jakarta.faces.validator.BeanValidator.MESSAGE";

    /**
     * <p class="changed_added_2_0">
     * The name of the Jakarta Servlet context attribute which holds the object used by Jakarta Faces to obtain
     * Validator instances. If the Jakarta Servlet context attribute is missing or contains a null value, Jakarta Server
     * Faces is free to use this Jakarta Servlet context attribute to store the ValidatorFactory bootstrapped by this
     * validator.
     * </p>
     */
    public static final String VALIDATOR_FACTORY_KEY = "jakarta.faces.validator.beanValidator.ValidatorFactory";

    /**
     * <p class="changed_added_2_0">
     * The delimiter that is used to separate the list of fully-qualified group names as strings.
     * </p>
     */
    public static final String VALIDATION_GROUPS_DELIMITER = ",";

    /**
     * <p class="changed_added_2_0">
     * The regular expression pattern that identifies an empty list of validation groups.
     * </p>
     */
    public static final String EMPTY_VALIDATION_GROUPS_PATTERN = "^[\\W" + VALIDATION_GROUPS_DELIMITER + "]*$";

    /**
     * <p class="changed_added_2_0">
     * If this param is defined, and calling <code>toLowerCase().equals(&#8220;true&#8221;)</code> on a <code>String</code>
     * representation of its value returns <code>true</code>, the runtime must not automatically add the validator with
     * validator-id equal to the value of the symbolic constant {@link #VALIDATOR_ID} to the list of default validators.
     * Setting this parameter to <code>true</code> will have the effect of disabling the automatic installation of Bean
     * Validation to every input component in every view in the application, though manual installation is still possible.
     * </p>
     *
     */
    public static final String DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME = "jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR";

    /**
     * <p class="changed_added_2_3">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of its value returns
     * {@code true} take the additional actions relating to <code>&lt;validateWholeBean /&gt;</code> specified in
     * {@link #validate}.
     * </p>
     *
     * @since 2.3
     */
    public static final String ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME = "jakarta.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN";

    // ----------------------------------------------------------- multi-field validation

    /**
     * <p class="changed_added_2_0">
     * A comma-separated list of validation groups which are used to filter which validations get checked by this validator.
     * If the validationGroupsArray attribute is omitted or is empty, the validation groups will be inherited from the
     * branch defaults or, if there are no branch defaults, the {@link jakarta.validation.groups.Default} group will be
     * used.
     * </p>
     *
     * @param validationGroups comma-separated list of validation groups (string with only spaces and commas treated as
     * null)
     */

    public void setValidationGroups(String validationGroups) {

        clearInitialState();
        String newValidationGroups = validationGroups;

        // treat empty list as null
        if (newValidationGroups != null && newValidationGroups.matches(EMPTY_VALIDATION_GROUPS_PATTERN)) {
            newValidationGroups = null;
        }

        // only clear cache of validation group classes if value is changing
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

    /**
     * <p class="changed_added_2_0">
     * Return the validation groups passed to the Validation API when checking constraints. If the validationGroupsArray
     * attribute is omitted or empty, the validation groups will be inherited from the branch defaults, or if there are no
     * branch defaults, the {@link jakarta.validation.groups.Default} group will be used.
     * </p>
     *
     * @return the value of the {@code validatinGroups} attribute.
     */
    public String getValidationGroups() {
        return validationGroups;
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_3">Verify</span> that the value is valid according to the Bean Validation
     * constraints.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * Obtain a {@link ValidatorFactory} instance by calling
     * {@link jakarta.validation.Validation#buildDefaultValidatorFactory}.
     * </p>
     *
     * <p>
     * Let <em>validationGroupsArray</em> be a <code>Class []</code> representing validator groups set on the component by
     * the tag handler for this validator. The first search component terminates the search for the validation groups value.
     * If no such value is found use the class name of {@link jakarta.validation.groups.Default} as the value of the
     * validation groups.
     * </p>
     *
     * <p>
     * Let <em>valueExpression</em> be the return from calling {@link UIComponent#getValueExpression} on the argument
     * <em>component</em>, passing the literal string &#8220;value&#8221; (without the quotes) as an argument. If this
     * application is running in an environment with Jakarta Expression Language, obtain the
     * <code>ValueReference</code> from <em>valueExpression</em> and let <em>valueBaseClase</em> be the return from calling
     * <code>ValueReference.getBase()</code> and <em>valueProperty</em> be the return from calling
     * <code>ValueReference.getProperty()</code>. If an earlier version of Jakarta Expression Language is present, use the
     * appropriate methods to inspect <em>valueExpression</em> and derive values for <em>valueBaseClass</em> and
     * <em>valueProperty</em>.
     * </p>
     *
     * <p>
     * If no <code>ValueReference</code> can be obtained, take no action and return.
     * </p>
     *
     * <p>
     * If <code>ValueReference.getBase()</code> return <code>null</code>, take no action and return.
     * </p>
     *
     * <p>
     * Obtain the {@link ValidatorContext} from the {@link ValidatorFactory}.
     * </p>
     *
     * <p>
     * Decorate the {@link MessageInterpolator} returned from {@link ValidatorFactory#getMessageInterpolator} with one that
     * leverages the <code>Locale</code> returned from {@link jakarta.faces.component.UIViewRoot#getLocale}, and store it in
     * the <code>ValidatorContext</code> using {@link ValidatorContext#messageInterpolator}.
     * </p>
     *
     * <p>
     * Obtain the {@link jakarta.validation.Validator} instance from the <code>validatorContext</code>.
     * </p>
     *
     * <p>
     * Obtain a <code>jakarta.validation.BeanDescriptor</code> from the <code>jakarta.validation.Validator</code>. If
     * <code>hasConstraints()</code> on the <code>BeanDescriptor</code> returns false, take no action and return. Otherwise
     * proceed.
     * </p>
     *
     * <p>
     * Call {@link jakarta.validation.Validator#validateValue}, passing <em>valueBaseClass</em>, <em>valueProperty</em>, the
     * <em>value</em> argument, and <em>validatorGroupsArray</em> as arguments.
     * </p>
     *
     * <p>
     * If the returned <code>Set&lt;{@link
     * ConstraintViolation}&gt;</code> is non-empty, for each element in the <code>Set</code>, create a {@link FacesMessage}
     * where the summary and detail are the return from calling {@link ConstraintViolation#getMessage}. Capture all such
     * <code>FacesMessage</code> instances into a <code>Collection</code> and pass them to
     * {@link ValidatorException#ValidatorException(java.util.Collection)}. <span class="changed_added_2_3">If the
     * {@link #ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} application parameter is enabled and this {@code Validator} instance
     * has validation groups other than or in addition to the {@code Default} group, record the fact that this field failed
     * validation so that any <code>&lt;f:validateWholeBean /&gt;</code> component later in the tree is able to skip
     * class-level validation for the bean for which this particular field is a property. Regardless of whether or not
     * {@link #ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} is set, throw the new exception.</span>
     * </p>
     *
     * <p class="changed_added_2_3">
     * If the returned {@code Set} is empty, the {@link #ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} application parameter is
     * enabled and this {@code Validator} instance has validation groups other than or in addition to the {@code Default}
     * group, record the fact that this field passed validation so that any <code>&lt;f:validateWholeBean /&gt;</code>
     * component later in the tree is able to allow class-level validation for the bean for which this particular field is a
     * property.
     * </p>
     *
     * </div>
     *
     * @param context {@inheritDoc}
     * @param component {@inheritDoc}
     * @param value {@inheritDoc}
     *
     * @throws ValidatorException {@inheritDoc}
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (component == null) {
            throw new NullPointerException();
        }

        ValueExpression valueExpression = component.getValueExpression("value");

        if (valueExpression == null) {
            return;
        }

        ValueReference valueReference = getValueReference(context, component, valueExpression);

        if (valueReference == null || valueReference.getBase() == null) {
            return;
        }

        Class<?>[] validationGroupsArray = parseValidationGroups(getValidationGroups());
        
        if (isResolvable(valueReference, valueExpression)) {
            jakarta.validation.Validator beanValidator = getBeanValidator(context);

            @SuppressWarnings("rawtypes")
            Set violationsRaw = null;

            try {
                violationsRaw = beanValidator.validateValue(valueReference.getBase().getClass(), valueReference.getProperty().toString(), value, validationGroupsArray);
            } catch (IllegalArgumentException iae) {
                LOGGER.fine("Unable to validate expression " + valueExpression.getExpressionString()
                        + " using Bean Validation.  Unable to get value of expression. " + " Message from Bean Validation: " + iae.getMessage());
            }

            @SuppressWarnings("unchecked")
            Set<ConstraintViolation<?>> violations = violationsRaw;

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

                // Record the fact that this field failed validation, so that multi-field
                // validation is not attempted.
                if (wholeBeanValidationEnabled(context, validationGroupsArray)) {
                    recordValidationResult(context, component, valueReference.getBase(), valueReference.getProperty().toString(), FAILED_FIELD_LEVEL_VALIDATION);
                }

                throw toThrow;
            }
        }

        // Record the fact that this field passed validation, so that multi-field
        // validation can be performed if desired
        if (wholeBeanValidationEnabled(context, validationGroupsArray)) {
            recordValidationResult(context, component, valueReference.getBase(), valueReference.getProperty().toString(), value);
        }
    }

    private static ValueReference getValueReference(FacesContext context, UIComponent component, ValueExpression valueExpression) {
        try {
            ValueReference reference = valueExpression.getValueReference(context.getELContext());
            if (reference != null) {
                Object base = reference.getBase();
                if (base instanceof CompositeComponentExpressionHolder) {
                    ValueExpression ve = ((CompositeComponentExpressionHolder) base).getExpression(String.valueOf(reference.getProperty()));
                    if (ve != null) {
                        reference = getValueReference(context, component, ve);
                    }
                }
            }
            return reference;
        }
        catch (PropertyNotFoundException e) {
            if (component instanceof UIInput && ((UIInput) component).getSubmittedValue() == null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                               "Property of value expression {0} of component {1} could not be found, but submitted value is null in first place, so not attempting to validate", // See Mojarra issue 4734
                               new Object[]{
                                       valueExpression.getExpressionString(),
                                       component.getId() });
                }
                return null;
            } else {
                throw e;
            }
        }
    }

    private boolean isResolvable(jakarta.el.ValueReference valueReference, ValueExpression valueExpression) {
        Boolean resolvable = null;
        String failureMessage = null;

        if (valueExpression == null) {
            failureMessage = "Unable to validate expression using Bean " + "Validation.  Expression must not be null.";
            resolvable = false;
        } else if (valueReference == null) {
            failureMessage = "Unable to validate expression " + valueExpression.getExpressionString()
                    + " using Bean Validation.  Unable to get value of expression.";
            resolvable = false;
        } else {
            Class<?> baseClass = valueReference.getBase() == null ? null : valueReference.getBase().getClass();

            // case 1, base classes of Map, List, or Array are not resolvable
            if (baseClass != null) {
                if (Map.class.isAssignableFrom(baseClass) || Collection.class.isAssignableFrom(baseClass) || Array.class.isAssignableFrom(baseClass)) {
                    failureMessage = "Unable to validate expression " + valueExpression.getExpressionString()
                            + " using Bean Validation.  Expression evaluates to a Map, List or array.";
                    resolvable = false;
                }
            }
        }

        resolvable = null != resolvable ? resolvable : true;

        if (!resolvable) {
            LOGGER.fine(failureMessage);
        }

        return resolvable;
    }

    private Class<?>[] parseValidationGroups(String validationGroupsStr) {
        if (cachedValidationGroups != null) {
            return cachedValidationGroups;
        }

        if (validationGroupsStr == null) {
            cachedValidationGroups = new Class<?>[] { Default.class };
            return cachedValidationGroups;
        }

        List<Class<?>> validationGroupsList = new ArrayList<>();
        String[] classNames = validationGroupsStr.split(VALIDATION_GROUPS_DELIMITER);
        for (String className : classNames) {
            className = className.trim();
            if (className.length() == 0) {
                continue;
            }

            if (className.equals(Default.class.getName())) {
                validationGroupsList.add(Default.class);
            } else {
                try {
                    validationGroupsList.add(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
                } catch (ClassNotFoundException e1) {
                    try {
                        validationGroupsList.add(Class.forName(className));
                    } catch (ClassNotFoundException e2) {
                        throw new FacesException("Validation group not found: " + className);
                    }
                }
            }
        }

        cachedValidationGroups = validationGroupsList.toArray(new Class[validationGroupsList.size()]);
        return cachedValidationGroups;
    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (!initialStateMarked()) {
            Object values[] = new Object[1];
            values[0] = validationGroups;
            return values;
        }
        return null;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state != null) {
            Object values[] = (Object[]) state;
            validationGroups = (String) values[0];
        }
    }

    private boolean initialState;

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

    private boolean transientValue;

    @Override
    public boolean isTransient() {
        return transientValue;
    }

    @Override
    public void setTransient(boolean transientValue) {
        this.transientValue = transientValue;
    }

    // ----------------------------------------------------- Private helper methods for bean validation

    // MOJARRA IMPLEMENTATION NOTE: identical code exists in Mojarra's com.sun.faces.util.BeanValidation

    private static jakarta.validation.Validator getBeanValidator(FacesContext context) {
        ValidatorFactory validatorFactory = getValidatorFactory(context);

        ValidatorContext validatorContext = validatorFactory.usingContext();
        MessageInterpolator facesMessageInterpolator = new FacesAwareMessageInterpolator(context, validatorFactory.getMessageInterpolator());
        validatorContext.messageInterpolator(facesMessageInterpolator);

        return validatorContext.getValidator();
    }

    private static ValidatorFactory getValidatorFactory(FacesContext context) {
        ValidatorFactory validatorFactory = null;

        Object cachedObject = context.getExternalContext().getApplicationMap().get(VALIDATOR_FACTORY_KEY);

        if (cachedObject instanceof ValidatorFactory) {
            validatorFactory = (ValidatorFactory) cachedObject;
        } else {
            try {
                validatorFactory = Validation.buildDefaultValidatorFactory();
            } catch (ValidationException e) {
                throw new FacesException("Could not build a default Bean Validator factory", e);
            }

            context.getExternalContext().getApplicationMap().put(VALIDATOR_FACTORY_KEY, validatorFactory);
        }

        return validatorFactory;
    }

    private static class FacesAwareMessageInterpolator implements MessageInterpolator {

        private FacesContext context;
        private MessageInterpolator delegate;

        public FacesAwareMessageInterpolator(FacesContext context, MessageInterpolator delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        @Override
        public String interpolate(String message, MessageInterpolator.Context context) {
            Locale locale = this.context.getViewRoot().getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
            return delegate.interpolate(message, context, locale);
        }

        @Override
        public String interpolate(String message, MessageInterpolator.Context context, Locale locale) {
            return delegate.interpolate(message, context, locale);
        }

    }

    // ----------------------------------------------------- Private helper methods for whole bean validation

    private void recordValidationResult(FacesContext context, UIComponent component, Object wholeBean, String propertyName, Object propertyValue) {
        Map<Object, Map<String, Map<String, Object>>> multiFieldCandidates = getMultiFieldValidationCandidates(context, true);
        Map<String, Map<String, Object>> candidate = multiFieldCandidates.getOrDefault(wholeBean, new HashMap<>());

        Map<String, Object> tuple = new HashMap<>(); // new ComponentValueTuple((EditableValueHolder) component, value);
        tuple.put("component", component);
        tuple.put("value", propertyValue);
        candidate.put(propertyName, tuple);

        multiFieldCandidates.putIfAbsent(wholeBean, candidate);
    }

}
