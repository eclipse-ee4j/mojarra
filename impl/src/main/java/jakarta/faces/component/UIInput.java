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

package jakarta.faces.component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * <p>
 * <span class="changed_modified_2_0 changed_modified_2_0_rev_a changed_modified_2_2
 * changed_modified_2_3"><strong>UIInput</strong></span> is a {@link UIComponent} that represents a component that both
 * displays output to the user (like {@link UIOutput} components do) and processes request parameters on the subsequent
 * request that need to be decoded. There are no restrictions on the data type of the local value, or the object
 * referenced by the value binding expression (if any); however, individual {@link jakarta.faces.render.Renderer}s will
 * generally impose restrictions on the type of data they know how to display.
 * </p>
 *
 * <p>
 * During the <em>Apply Request Values</em> phase of the request processing lifecycle, the decoded value of this
 * component, usually but not necessarily a String, must be stored - but not yet converted - using
 * <code>setSubmittedValue()</code>. If the component wishes to indicate that no particular value was submitted, it can
 * either do nothing, or set the submitted value to <code>null</code>.
 * </p>
 *
 * <p>
 * By default, during the <em>Process Validators</em> phase of the request processing lifecycle, the submitted value
 * will be converted to a typesafe object, and, if validation succeeds, stored as a local value using
 * <code>setValue()</code>. However, if the <code>immediate</code> property is set to <code>true</code>, this processing
 * will occur instead at the end of the <em>Apply Request Values</em> phase.
 * </p>
 *
 * <p>
 * During the <em>Render Response</em> phase of the request processing lifecycle, conversion for output occurs as for
 * {@link UIOutput}.
 * </p>
 *
 * <p>
 * When the <code>validate()</code> method of this {@link UIInput} detects that a value change has actually occurred,
 * and that all validations have been successfully passed, it will queue a {@link ValueChangeEvent}. Later on, the
 * <code>broadcast()</code> method will ensure that this event is broadcast to all interested listeners. This event will
 * be delivered by default in the <em>Process Validators</em> phase, but can be delivered instead during <em>Apply
 * Request Values</em> if the <code>immediate</code> property is set to <code>true</code>.
 * <span class="changed_added_2_0">If the validation fails, the implementation must call
 * {@link FacesContext#validationFailed}.</span>
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>Text</code>". This value can be changed by
 * calling the <code>setRendererType()</code> method.
 * </p>
 */
public class UIInput extends UIOutput implements EditableValueHolder {

    private static final String BEANS_VALIDATION_AVAILABLE = "jakarta.faces.private.BEANS_VALIDATION_AVAILABLE";

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Input";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Input";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a conversion error
     * occurs, and neither the page author nor the {@link ConverterException} provides a message.
     * </p>
     */
    public static final String CONVERSION_MESSAGE_ID = "jakarta.faces.component.UIInput.CONVERSION";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a required check fails.
     * </p>
     */
    public static final String REQUIRED_MESSAGE_ID = "jakarta.faces.component.UIInput.REQUIRED";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a model update error
     * occurs, and the thrown exception has no message.
     * </p>
     */
    public static final String UPDATE_MESSAGE_ID = "jakarta.faces.component.UIInput.UPDATE";

    /**
     * <p class="changed_added_2_0">
     * The name of a context parameter that indicates how empty values should be handled with respect to validation. See
     * {@link #validateValue} for the allowable values and specification of how they should be interpreted.
     * </p>
     */

    public static final String VALIDATE_EMPTY_FIELDS_PARAM_NAME = "jakarta.faces.VALIDATE_EMPTY_FIELDS";

    /**
     * <p class="changed_modified_2_3">
     * The name of a context parameter that indicates how empty strings need to be interpreted.
     * </p>
     */
    public static final String EMPTY_STRING_AS_NULL_PARAM_NAME = "jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL";

    /**
     * <p class="changed_modified_2_3">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of its value returns true,
     * validation must be performed, even when there is no corresponding value for this component in the incoming request.
     * See {@link #validate}.
     * </p>
     */
    public static final String ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE = "jakarta.faces.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE";

    private static final Validator[] EMPTY_VALIDATOR = new Validator[0];

    private transient Boolean emptyStringIsNull;

    private transient Boolean validateEmptyFields;

    private transient Boolean isSetAlwaysValidateRequired;

    enum PropertyKeys {
        /**
         * <p>
         * The "localValueSet" state for this component.
         */
        localValueSet,

        /**
         * <p>
         * If the input is required or not.
         * </p>
         */
        required,

        /**
         * <p>
         * Custom message to be displayed if input is required but non was submitted.
         * </p>
         */
        requiredMessage,

        /**
         * <p>
         * Custom message to be displayed when conversion fails.
         * </p>
         */
        converterMessage,

        /**
         * <p>
         * Custom message to be displayed when validation fails.
         * </p>
         */
        validatorMessage,

        /**
         * <p>
         * Flag indicating whether or not this component is valid.
         * </p>
         */
        valid,

        /**
         * <p>
         * Flag indicating when conversion/validation should occur.
         * </p>
         */
        immediate,

    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIInput} instance with default property values.
     * </p>
     */
    public UIInput() {

        super();
        setRendererType("jakarta.faces.Text");

    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {

        return COMPONENT_FAMILY;

    }

    /**
     * <p>
     * The submittedValue value of this {@link UIInput} component.
     * </p>
     */
    private transient Object submittedValue = null;

    /**
     * <p>
     * Return the submittedValue value of this {@link UIInput} component. This method should only be used by the
     * <code>decode()</code> and <code>validate()</code> method of this component, or its corresponding {@link Renderer}.
     * </p>
     */
    @Override
    public Object getSubmittedValue() {
        if (submittedValue == null && !isValid() && considerEmptyStringNull(FacesContext.getCurrentInstance())) { // JAVASERVERFACES_SPEC_PUBLIC-671
            return "";
        } else {
            return submittedValue;
        }
    }

    /**
     * <p>
     * Set the submittedValue value of this {@link UIInput} component. This method should only be used by the
     * <code>decode()</code> and <code>validate()</code> method of this component, or its corresponding {@link Renderer}.
     * </p>
     *
     * @param submittedValue The new submitted value
     */
    @Override
    public void setSubmittedValue(Object submittedValue) {

        this.submittedValue = submittedValue;

    }

    /**
     * <p class="changed_added_2_2">
     * If there is a local value, return it, otherwise return the result of calling {@code super.getVaue()}.
     * </p>
     *
     * @since 2.2
     */

    @Override
    public Object getValue() {
        return isLocalValueSet() ? getLocalValue() : super.getValue();
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        // Mark the local value as set.
        setLocalValueSet(true);
    }

    /**
     * <p>
     * <span class="changed_modified_2_2">Convenience</span> method to reset this component's value to the un-initialized
     * state. This method does the following:
     * </p>
     *
     * <p class="changed_modified_2_2">
     * Call {@link UIOutput#setValue}.
     * </p>
     *
     * <p>
     * Call {@link #setSubmittedValue} passing <code>null</code>.
     * </p>
     *
     * <p>
     * Clear state for property <code>localValueSet</code>.
     * </p>
     *
     * <p>
     * Clear state for property <code>valid</code>.
     * </p>
     *
     * <p>
     * Upon return from this call if the instance had a <code>ValueBinding</code> associated with it for the "value"
     * property, this binding is evaluated when {@link UIOutput#getValue} is called. Otherwise, <code>null</code> is
     * returned from <code>getValue()</code>.
     * </p>
     */

    @Override
    public void resetValue() {
        super.resetValue();
        setSubmittedValue(null);
        getStateHelper().remove(PropertyKeys.localValueSet);
        getStateHelper().remove(PropertyKeys.valid);
    }

    /**
     * Return the "local value set" state for this component. Calls to <code>setValue()</code> automatically reset this
     * property to <code>true</code>.
     */
    @Override
    public boolean isLocalValueSet() {
        return (Boolean) getStateHelper().eval(PropertyKeys.localValueSet, false);
    }

    /**
     * Sets the "local value set" state for this component.
     */
    @Override
    public void setLocalValueSet(boolean localValueSet) {
        getStateHelper().put(PropertyKeys.localValueSet, localValueSet);
    }

    /**
     * <p>
     * Return the "required field" state for this component.
     * </p>
     */
    @Override
    public boolean isRequired() {

        return (Boolean) getStateHelper().eval(PropertyKeys.required, false);

    }

    /**
     * <p>
     * If there has been a call to {@link #setRequiredMessage} on this instance, return the message. Otherwise, call
     * {@link #getValueExpression} passing the key "requiredMessage", get the result of the expression, and return it. Any
     * {@link ELException}s thrown during the call to <code>getValue()</code> must be wrapped in a {@link FacesException}
     * and rethrown.
     *
     * @return the required message.
     */

    public String getRequiredMessage() {

        return (String) getStateHelper().eval(PropertyKeys.requiredMessage);

    }

    /**
     * <p>
     * Override any {@link ValueExpression} set for the "requiredMessage" with the literal argument provided to this method.
     * Subsequent calls to {@link #getRequiredMessage} will return this value;
     * </p>
     *
     * @param message the literal message value to be displayed in the event the user hasn't supplied a value and one is
     * required.
     */

    public void setRequiredMessage(String message) {

        getStateHelper().put(PropertyKeys.requiredMessage, message);

    }

    /**
     * <p>
     * If there has been a call to {@link #setConverterMessage} on this instance, return the message. Otherwise, call
     * {@link #getValueExpression} passing the key "converterMessage", get the result of the expression, and return it. Any
     * {@link ELException}s thrown during the call to <code>getValue()</code> must be wrapped in a {@link FacesException}
     * and rethrown.
     *
     * @return the converter message.
     */

    public String getConverterMessage() {

        return (String) getStateHelper().eval(PropertyKeys.converterMessage);

    }

    /**
     * <p>
     * Override any {@link ValueExpression} set for the "converterMessage" with the literal argument provided to this
     * method. Subsequent calls to {@link #getConverterMessage} will return this value;
     * </p>
     *
     * @param message the literal message value to be displayed in the event conversion fails.
     */

    public void setConverterMessage(String message) {

        getStateHelper().put(PropertyKeys.converterMessage, message);

    }

    /**
     * <p>
     * If there has been a call to {@link #setValidatorMessage} on this instance, return the message. Otherwise, call
     * {@link #getValueExpression} passing the key "validatorMessage", get the result of the expression, and return it. Any
     * {@link ELException}s thrown during the call to <code>getValue()</code> must be wrapped in a {@link FacesException}
     * and rethrown.
     *
     * @return the validator message.
     */

    public String getValidatorMessage() {

        return (String) getStateHelper().eval(PropertyKeys.validatorMessage);

    }

    /**
     * <p>
     * Override any {@link ValueExpression} set for the "validatorMessage" with the literal argument provided to this
     * method. Subsequent calls to {@link #getValidatorMessage} will return this value;
     * </p>
     *
     * @param message the literal message value to be displayed in the event validation fails.
     */

    public void setValidatorMessage(String message) {

        getStateHelper().put(PropertyKeys.validatorMessage, message);

    }

    @Override
    public boolean isValid() {

        return (Boolean) getStateHelper().eval(PropertyKeys.valid, true);

    }

    @Override
    public void setValid(boolean valid) {

        getStateHelper().put(PropertyKeys.valid, valid);

    }

    /**
     * <p>
     * Set the "required field" state for this component.
     * </p>
     *
     * @param required The new "required field" state
     */
    @Override
    public void setRequired(boolean required) {

        getStateHelper().put(PropertyKeys.required, required);

    }

    @Override
    public boolean isImmediate() {

        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);

    }

    @Override
    public void setImmediate(boolean immediate) {

        getStateHelper().put(PropertyKeys.immediate, immediate);

    }


    // ----------------------------------------------------- UIComponent Methods

    /**
     * <p>
     * In addition to the actions taken in {@link UIOutput} when {@link PartialStateHolder#markInitialState()} is called,
     * check if any of the installed {@link Validator}s are PartialStateHolders and if so, call
     * {@link jakarta.faces.component.PartialStateHolder#markInitialState()} as appropriate.
     * </p>
     */
    @Override
    public void markInitialState() {
        super.markInitialState();
        if (validators != null) {
            validators.markInitialState();
        }

    }

    @Override
    public void clearInitialState() {
        if (initialStateMarked()) {
            super.clearInitialState();
            if (validators != null) {
                validators.clearInitialState();
            }
        }

    }

    /**
     * <p>
     * Specialized decode behavior on top of that provided by the superclass. In addition to the standard
     * <code>processDecodes</code> behavior inherited from {@link UIComponentBase}, calls <code>validate()</code> if the the
     * <code>immediate</code> property is true; if the component is invalid afterwards or a <code>RuntimeException</code> is
     * thrown, calls {@link FacesContext#renderResponse}.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processDecodes(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        super.processDecodes(context);

        if (isImmediate()) {
            executeValidate(context);
        }
    }

    /**
     * <p>
     * <span class="changed_modified_2_3">In</span> addition to the standard <code>processValidators</code> behavior
     * inherited from {@link UIComponentBase}, calls <code>validate()</code> if the <code>immediate</code> property is false
     * (which is the default); if the component is invalid afterwards, calls {@link FacesContext#renderResponse}.
     * <span class="changed_added_2_3">To ensure the {@code PostValidateEvent} is published at the proper time, this
     * component must be validated first, followed by the component's children and facets.</span> If a
     * <code>RuntimeException</code> is thrown during validation processing, calls {@link FacesContext#renderResponse} and
     * re-throw the exception.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processValidators(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, this);

        if (!isImmediate()) {
            Application application = context.getApplication();
            application.publishEvent(context, PreValidateEvent.class, this);
            executeValidate(context);
            application.publishEvent(context, PostValidateEvent.class, this);
        }
        for (Iterator<UIComponent> i = getFacetsAndChildren(); i.hasNext();) {
            i.next().processValidators(context);
        }

        popComponentFromEL(context);
    }

    /**
     * <p>
     * In addition to the standard <code>processUpdates</code> behavior inherited from {@link UIComponentBase}, calls
     * <code>updateModel()</code>. If the component is invalid afterwards, calls {@link FacesContext#renderResponse}. If a
     * <code>RuntimeException</code> is thrown during update processing, calls {@link FacesContext#renderResponse} and
     * re-throw the exception.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processUpdates(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        super.processUpdates(context);

        pushComponentToEL(context, this);

        try {
            updateModel(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        } finally {
            popComponentFromEL(context);
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void decode(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Force validity back to "true"
        setValid(true);
        super.decode(context);
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> the following algorithm to update the model data associated with
     * this {@link UIInput}, if any, as appropriate.
     * </p>
     * <ul>
     * <li>If the <code>valid</code> property of this component is <code>false</code>, take no further action.</li>
     * <li>If the <code>localValueSet</code> property of this component is <code>false</code>, take no further action.</li>
     * <li>If no {@link ValueExpression} for <code>value</code> exists, take no further action.</li>
     * <li>Call <code>setValue()</code> method of the {@link ValueExpression} to update the value that the
     * {@link ValueExpression} points at.</li>
     * <li>If the <code>setValue()</code> method returns successfully:
     * <ul>
     * <li>Clear the local value of this {@link UIInput}.</li>
     * <li>Set the <code>localValueSet</code> property of this {@link UIInput} to false.</li>
     * </ul>
     * </li>
     * <li>If the <code>setValue()</code> method throws an Exception:
     * <ul>
     * <li class="changed_modified_2_0">Enqueue an error message. Create a {@link FacesMessage} with the id
     * {@link #UPDATE_MESSAGE_ID}. Create a {@link UpdateModelException}, passing the <code>FacesMessage</code> and the
     * caught exception to the constructor. Create an {@link ExceptionQueuedEventContext}, passing the
     * <code>FacesContext</code>, the <code>UpdateModelException</code>, this component instance, and
     * {@link PhaseId#UPDATE_MODEL_VALUES} to its constructor. Call {@link FacesContext#getExceptionHandler} and then call
     * {@link ExceptionHandler#processEvent}, passing the <code>ExceptionQueuedEventContext</code>.</li>
     * <li>Set the <code>valid</code> property of this {@link UIInput} to <code>false</code>.</li>
     * </ul>
     * The exception must not be re-thrown. This enables tree traversal to continue for this lifecycle phase, as in all the
     * other lifecycle phases.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public void updateModel(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (!isValid() || !isLocalValueSet()) {
            return;
        }
        ValueExpression ve = getValueExpression("value");
        if (ve != null) {
            Throwable caught = null;
            FacesMessage message = null;
            try {
                ve.setValue(context.getELContext(), getLocalValue());
                resetValue();
            } catch (ELException e) {
                caught = e;
                String messageStr = e.getMessage();
                Throwable result = e.getCause();
                while (null != result && result.getClass().isAssignableFrom(ELException.class)) {
                    messageStr = result.getMessage();
                    result = result.getCause();
                }
                if (null == messageStr) {
                    message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
                } else {
                    message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageStr, messageStr);
                }
                setValid(false);
            } catch (Exception e) {
                caught = e;
                message = MessageFactory.getMessage(context, UPDATE_MESSAGE_ID, MessageFactory.getLabel(context, this));
                setValid(false);
            }
            if (caught != null) {
                assert message != null;
                // PENDING(edburns): verify this is in the spec.
                @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
                UpdateModelException toQueue = new UpdateModelException(message, caught);
                ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext(context, toQueue, this, PhaseId.UPDATE_MODEL_VALUES);
                context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, eventContext);

            }

        }
    }

    // ------------------------------------------------------ Validation Methods

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_2 changed_modified_2_3">Perform</span> the following algorithm
     * to validate the local value of this {@link UIInput}.
     * </p>
     *
     * <ul>
     *
     * <li>Retrieve the submitted value with {@link #getSubmittedValue}. If this returns <code>null</code>,
     * <span class="changed_modified_2_3">and the value of the {@link #ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE}
     * context-param is true (ignoring case), examine the value of the "required" property. If the value of "required" is
     * true, continue as below. If the value of "required" is false or the required attribute is not set, exit without
     * further processing. If the context-param is not set, or is set to false (ignoring case),</span> exit without further
     * processing. (This indicates that no value was submitted for this component.)</li>
     *
     * <li><span class="changed_modified_2_0">If the
     * <code>jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</code> context parameter value is
     * <code>true</code> (ignoring case), and <code>getSubmittedValue()</code> returns a zero-length <code>String</code>
     * call <code>{@link #setSubmittedValue}</code>, passing <code>null</code> as the argument and continue processing using
     * <code>null</code> as the current submitted value.</span></li>
     *
     * <li>Convert the submitted value into a "local value" of the appropriate data type by calling
     * {@link #getConvertedValue}.</li>
     * <li><span class="changed_added_2_0_rev_a">If conversion fails</span>:
     * <ul>
     * <li>Enqueue an appropriate error message by calling the <code>addMessage()</code> method on the
     * <code>FacesContext</code>.</li>
     * <li>Set the <code>valid</code> property on this component to <code>false</code></li>
     * </ul>
     * </li>
     *
     * <li>Validate the property by calling {@link #validateValue}.</li>
     *
     * <li>If the <code>valid</code> property of this component is still <code>true</code>, retrieve the previous value of
     * the component (with <code>getValue()</code>), store the new local value using <code>setValue()</code>, and reset the
     * submitted value to null <span class="changed_added_2_2">with a call to {@link #setSubmittedValue} passing
     * {@code null} as the argument</span>. If the local value is different from the previous value of this component,
     * <span class="changed_modified_2_1">as determined by a call to {@link #compareValues}</span>, fire a
     * {@link ValueChangeEvent} to be broadcast to all interested listeners.</li>
     *
     * </ul>
     * <p>
     * Application components implementing {@link UIInput} that wish to perform validation with logic embedded in the
     * component should perform their own correctness checks, and then call the <code>super.validate()</code> method to
     * perform the standard processing described above.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     * @throws NullPointerException if <code>context</code> is null
     */
    public void validate(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Submitted value == null means "the component was not submitted
        // at all".
        Object submittedValue = getSubmittedValue();
        if (submittedValue == null) {
            if (isRequired() && isSetAlwaysValidateRequired(context)) {
                // continue as below
            } else {
                if (this instanceof UIViewParameter && considerEmptyStringNull(context)) {
                    // https://github.com/eclipse-ee4j/mojarra/issues/4550
                    // https://github.com/eclipse-ee4j/mojarra/issues/4716
                    validateValue(context,  getConvertedValue(context, submittedValue));
                }
                return;
            }
        }

        // If non-null, an instanceof String, and we're configured to treat
        // zero-length Strings as null:
        // call setSubmittedValue(null)
        if ((considerEmptyStringNull(context)
             && submittedValue instanceof String
             && ((String) submittedValue).length() == 0)) {
            setSubmittedValue(null);
            submittedValue = null;
        }

        Object newValue = null;

        try {
            newValue = getConvertedValue(context, submittedValue);
        } catch (ConverterException ce) {
            addConversionErrorMessage(context, ce);
            setValid(false);
            if (submittedValue == null) {
                setSubmittedValue("");
            }
        }

        validateValue(context, newValue);

        // If our value is valid, store the new value, erase the
        // "submitted" value, and emit a ValueChangeEvent if appropriate
        if (isValid()) {
            Object previous = getValue();
            setValue(newValue);
            setSubmittedValue(null);
            if (compareValues(previous, newValue)) {
                queueEvent(new ValueChangeEvent(context, this, previous, newValue));
            }
        }

    }

    /*
     * Respecting the fact that someone may have decorated FacesContextFactory and thus skipped our saving of this init
     * param, look for the init param and return its value. The return is saved in a transient ivar to provide performance
     * while not perturbing state saving.
     */

    private boolean isSetAlwaysValidateRequired(FacesContext context) {
        if (null != isSetAlwaysValidateRequired) {
            return isSetAlwaysValidateRequired;
        }

        Boolean bool = (Boolean) context.getAttributes().get(ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE);
        if (null != bool) {
            isSetAlwaysValidateRequired = bool;
        } else {
            String val = context.getExternalContext().getInitParameter(ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE);
            isSetAlwaysValidateRequired = Boolean.valueOf(val);
        }

        return isSetAlwaysValidateRequired;
    }

    /**
     * <p>
     * Convert the submitted value into a "local value" of the appropriate data type, if necessary. Employ the following
     * algorithm to do so:
     * </p>
     * <ul>
     * <li>If a <code>Renderer</code> is present, call <code>getConvertedValue()</code> to convert the submitted value.</li>
     * <li>If no <code>Renderer</code> is present, and the submitted value is a String, locate a {@link Converter} as
     * follows:
     * <ul>
     * <li>If <code>getConverter()</code> returns a non-null {@link Converter}, use that instance.</li>
     * <li>Otherwise, if a value binding for <code>value</code> exists, call <code>getType()</code> on it.
     * <ul>
     * <li>If this call returns <code>null</code>, assume the output type is <code>String</code> and perform no
     * conversion.</li>
     * <li>Otherwise, call <code>Application.createConverter(Class)</code> to locate any registered {@link Converter}
     * capable of converting data values of the specified type.</li>
     * </ul>
     * </li>
     * </ul>
     * <li>If a {@link Converter} instance was located, call its <code>getAsObject()</code> method to perform the
     * conversion. <span class="changed_modified_2_0_rev_a">If conversion fails, the <code>Converter</code> will have thrown
     * a <code>ConverterException</code> which is declared as a checked exception on this method, and thus must be handled
     * by the caller.</span></li>
     * <li>Otherwise, use the submitted value without any conversion</li>
     * </ul>
     * <p>
     * This method can be overridden by subclasses for more specific behavior.
     * </p>
     *
     * @param context the Faces context.
     * @param newSubmittedValue the new submitted value.
     * @return the converted value.
     */
    protected Object getConvertedValue(FacesContext context, Object newSubmittedValue) throws ConverterException {
        Renderer renderer = getRenderer(context);
        Object newValue;

        if (renderer != null) {
            newValue = renderer.getConvertedValue(context, this, newSubmittedValue);
        } else if (newSubmittedValue instanceof String) {
            // If there's no Renderer, and we've got a String,
            // run it through the Converter (if any)
            Converter converter = getConverterWithType(context);
            if (converter != null) {
                newValue = converter.getAsObject(context, this, (String) newSubmittedValue);
            } else {
                newValue = newSubmittedValue;
            }
        } else {
            newValue = newSubmittedValue;
        }
        return newValue;
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Set</span> the "valid" property according to the below algorithm.
     * </p>
     *
     * <ul>
     *
     * <li>
     *
     * <p>
     * If the <code>valid</code> property on this component is still <code>true</code>, and the <code>required</code>
     * property is also <code>true</code>, ensure that the local value is not empty (where "empty" is defined as
     * <code>null</code> or a zero-length String). If the local value is empty:
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Enqueue an appropriate error message by calling the <code>addMessage()</code> method on the <code>FacesContext</code>
     * instance for the current request. If the {@link #getRequiredMessage} returns non-<code>null</code>, use the value as
     * the <code>summary</code> and <code>detail</code> in the {@link FacesMessage} that is enqueued on the
     * <code>FacesContext</code>, otherwise use the message for the {@link #REQUIRED_MESSAGE_ID}.</li>
     *
     * <li>Set the <code>valid</code> property on this component to <code>false</code>.</li>
     *
     * <li>
     * <p class="changed_modified_2_0">
     * If calling {@link ValidatorException#getFacesMessages} returns non-<code>null</code>, each message should be added to
     * the <code>FacesContext</code>. Otherwise the single message returned from {@link ValidatorException#getFacesMessage}
     * should be added.
     * </p>
     * </li>
     *
     * </ul>
     *
     * </li>
     *
     * <li class="changed_added_2_0">
     *
     * <p>
     * Otherwise, if the <code>valid</code> property on this component is still <code>true</code>, take the following action
     * to determine if validation of this component should proceed.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * If the value is not empty, validation should proceed.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If the value is empty, but the system has been directed to validate empty fields, validation should proceed. The
     * implementation must obtain the init parameter <code>Map</code> from the <code>ExternalContext</code> and inspect the
     * value for the key given by the value of the symbolic constant {@link #VALIDATE_EMPTY_FIELDS_PARAM_NAME}. If there is
     * no value under that key, use the same key and look in the application map from the <code>ExternalContext</code>. If
     * the value is <code>null</code> or equal to the string &#8220;<code>auto</code>&#8221; (without the quotes) take
     * appropriate action to determine if Bean Validation is present in the runtime environment. If not, validation should
     * not proceed. If so, validation should proceed. If the value is equal (ignoring case) to
     * &#8220;<code>true</code>&#8221; (without the quotes) validation should proceed. Otherwise, validation should not
     * proceed.
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p>
     * If the above determination indicates that validation should proceed, call the <code>validate()</code> method of each
     * {@link Validator} registered for this {@link UIInput}, followed by the method pointed at by the
     * <code>validatorBinding</code> property (if any). If any of these validators or the method throws a
     * {@link ValidatorException}, catch the exception, add its message (if any) to the {@link FacesContext}, and set the
     * <code>valid</code> property of this component to false.</li>
     *
     * </ul>
     *
     * @param context the Faces context.
     * @param newValue the new value.
     */

    protected void validateValue(FacesContext context, Object newValue) {
        // If our value is valid, enforce the required property if present
        if (isValid() && isRequired() && isEmpty(newValue)) {
            String requiredMessageStr = getRequiredMessage();
            FacesMessage message;
            if (null != requiredMessageStr) {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR, requiredMessageStr, requiredMessageStr);
            } else {
                message = MessageFactory.getMessage(context, REQUIRED_MESSAGE_ID, MessageFactory.getLabel(context, this));
            }
            context.addMessage(getClientId(context), message);
            setValid(false);
        }

        // If our value is valid and not empty or empty w/ validate empty fields enabled, call all validators
        if (isValid() && (!isEmpty(newValue) || validateEmptyFields(context))) {
            if (validators != null) {
                Validator[] validators = this.validators.asArray(Validator.class);
                for (Validator validator : validators) {
                    try {
                        validator.validate(context, this, newValue);
                    } catch (ValidatorException ve) {
                        // If the validator throws an exception, we're
                        // invalid, and we need to add a message
                        setValid(false);
                        FacesMessage message;
                        String validatorMessageString = getValidatorMessage();

                        if (null != validatorMessageString) {
                            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, validatorMessageString, validatorMessageString);
                            message.setSeverity(FacesMessage.SEVERITY_ERROR);
                        } else {
                            Collection<FacesMessage> messages = ve.getFacesMessages();
                            if (null != messages) {
                                message = null;
                                String cid = getClientId(context);
                                for (FacesMessage m : messages) {
                                    context.addMessage(cid, m);
                                }
                            } else {
                                message = ve.getFacesMessage();
                            }
                        }
                        if (message != null) {
                            context.addMessage(getClientId(context), message);
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Return <code>true</code> if the new value is different from the previous value. First compare the two values by
     * passing <em>value</em> to the <code>equals</code> method on argument <em>previous</em>. If that method returns
     * <code>true</code>, return <code>true</code>. If that method returns <code>false</code>, and both arguments implement
     * <code>java.lang.Comparable</code>, compare the two values by passing <em>value</em> to the <code>compareTo</code>
     * method on argument <em>previous</em>. Return <code>true</code> if this method returns <code>0</code>,
     * <code>false</code> otherwise.
     * </p>
     *
     * @param previous old value of this component (if any)
     * @param value new value of this component (if any)
     * @return <code>true</code> if the new value is different from the previous value, <code>false</code> otherwise.
     */
    protected boolean compareValues(Object previous, Object value) {
        boolean result = true;

        if (previous == null) {
            result = value != null;
        } else if (value == null) {
            result = true;
        } else {
            boolean previousEqualsValue = previous.equals(value);
            if (!previousEqualsValue && previous instanceof Comparable && value instanceof Comparable) {
                try {
                    result = !(0 == ((Comparable) previous).compareTo(value));
                } catch (ClassCastException cce) {
                    // Comparable throws CCE if the types prevent a comparison
                    result = true;
                }
            } else {
                result = !previousEqualsValue;
            }
        }
        return result;
    }

    /**
     * Executes validation logic.
     */
    private void executeValidate(FacesContext context) {
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.validationFailed();
            context.renderResponse();
        }
    }

    /**
     * <p class="changed_modified_2_3">
     * Is the value denoting an empty value.
     * </p>
     *
     * <p class="changed_modified_2_3">
     * If the value is null, return true. If the value is a String and it is the empty string, return true. If the value is
     * an array and the array length is 0, return true. If the value is a List and the List is empty, return true. If the
     * value is a Collection and the Collection is empty, return true. If the value is a Map and the Map is empty, return
     * true. In all other cases, return false.
     * </p>
     *
     * @param value the value to check.
     * @return true if it is, false otherwise.
     */
    public static boolean isEmpty(Object value) {

        if (value == null) {
            return true;
        } else if (value instanceof String && ((String) value).length() < 1) {
            return true;
        } else if (value.getClass().isArray()) {
            if (0 == java.lang.reflect.Array.getLength(value)) {
                return true;
            }
        } else if (value instanceof List) {
            if (((List) value).isEmpty()) {
                return true;
            }
        } else if (value instanceof Collection) {
            if (((Collection) value).isEmpty()) {
                return true;
            }
        } else if (value instanceof Map && ((Map) value).isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * The set of {@link Validator}s associated with this <code>UIComponent</code>.
     * </p>
     */
    AttachedObjectListHolder<Validator> validators;

    /**
     * <p>
     * Add a {@link Validator} instance to the set associated with this {@link UIInput}.
     * </p>
     *
     * @param validator The {@link Validator} to add
     * @throws NullPointerException if <code>validator</code> is null
     */
    @Override
    public void addValidator(Validator validator) {

        if (validator == null) {
            throw new NullPointerException();
        }

        if (validators == null) {
            validators = new AttachedObjectListHolder<>();
        }
        validators.add(validator);

    }

    /**
     * <p>
     * Return the set of registered {@link Validator}s for this {@link UIInput} instance. If there are no registered
     * validators, a zero-length array is returned.
     * </p>
     */
    @Override
    public Validator[] getValidators() {

        return validators != null ? validators.asArray(Validator.class) : EMPTY_VALIDATOR;

    }

    /**
     * <p>
     * Remove a {@link Validator} instance from the set associated with this {@link UIInput}, if it was previously
     * associated. Otherwise, do nothing.
     * </p>
     *
     * @param validator The {@link Validator} to remove
     */
    @Override
    public void removeValidator(Validator validator) {

        if (validator == null) {
            return;
        }

        if (validators != null) {
            validators.remove(validator);
        }

    }

    // ------------------------------------------------ Event Processing Methods

    /**
     * <p>
     * Add a new {@link ValueChangeListener} to the set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.
     * </p>
     *
     * @param listener The {@link ValueChangeListener} to be added
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    @Override
    public void addValueChangeListener(ValueChangeListener listener) {

        addFacesListener(listener);

    }

    /**
     * <p>
     * Return the set of registered {@link ValueChangeListener}s for this {@link UIInput} instance. If there are no
     * registered listeners, a zero-length array is returned.
     * </p>
     */
    @Override
    public ValueChangeListener[] getValueChangeListeners() {

        return (ValueChangeListener[]) getFacesListeners(ValueChangeListener.class);
    }

    /**
     * <p>
     * Remove an existing {@link ValueChangeListener} (if any) from the set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.
     * </p>
     *
     * @param listener The {@link ValueChangeListener} to be removed
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {

        removeFacesListener(listener);

    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object[] result = null;

        Object superState = super.saveState(context);
        Object validatorsState = validators != null ? validators.saveState(context) : null;

        if (superState != null || validatorsState != null) {
            result = new Object[] { superState, validatorsState };
        }

        return result;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        if (values[1] != null) {
            if (validators == null) {
                validators = new AttachedObjectListHolder<>();
            }
            validators.restoreState(context, values[1]);
        }

    }

    private Converter getConverterWithType(FacesContext context) {
        Converter converter = getConverter();
        if (converter != null) {
            return converter;
        }

        ValueExpression valueExpression = getValueExpression("value");
        if (valueExpression == null) {
            return null;
        }

        Class<?> converterType;
        try {
            converterType = valueExpression.getType(context.getELContext());
        } catch (ELException e) {
            throw new FacesException(e);
        }

        // if converterType is null, String, or Object, assume
        // no conversion is needed
        if (converterType == null || converterType == String.class || converterType == Object.class) {
            return null;
        }

        // if getType returns a type for which we support a default
        // conversion, acquire an appropriate converter instance.
        try {
            Application application = context.getApplication();
            return application.createConverter(converterType);
        } catch (Exception e) {
            return null;
        }
    }

    private void addConversionErrorMessage(FacesContext context, ConverterException ce) {
        FacesMessage message;
        String converterMessageString = getConverterMessage();
        if (null != converterMessageString) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, converterMessageString, converterMessageString);
        } else {
            message = ce.getFacesMessage();
            if (message == null) {
                message = MessageFactory.getMessage(context, CONVERSION_MESSAGE_ID);
                if (message.getDetail() == null) {
                    message.setDetail(ce.getMessage());
                }
            }
        }

        context.addMessage(getClientId(context), message);
    }

    boolean considerEmptyStringNull(FacesContext ctx) {

        if (emptyStringIsNull == null) {
            String val = ctx.getExternalContext().getInitParameter(EMPTY_STRING_AS_NULL_PARAM_NAME);
            emptyStringIsNull = Boolean.valueOf(val);
        }

        return emptyStringIsNull;

    }

    private boolean validateEmptyFields(FacesContext ctx) {

        if (validateEmptyFields == null) {
            ExternalContext extCtx = ctx.getExternalContext();
            String val = extCtx.getInitParameter(VALIDATE_EMPTY_FIELDS_PARAM_NAME);

            if (null == val) {
                val = (String) extCtx.getApplicationMap().get(VALIDATE_EMPTY_FIELDS_PARAM_NAME);
            }
            if (val == null || "auto".equals(val)) {
                validateEmptyFields = isBeansValidationAvailable(ctx);
            } else {
                validateEmptyFields = Boolean.valueOf(val);
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
                appMap.put(BEANS_VALIDATION_AVAILABLE, result = true);
            } catch (Throwable t) {
                appMap.put(BEANS_VALIDATION_AVAILABLE, Boolean.FALSE);
            }
        }

        return result;
    }

}
