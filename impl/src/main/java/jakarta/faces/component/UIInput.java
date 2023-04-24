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
import java.util.List;
import java.util.Map;

import com.sun.faces.api.component.UIInputImpl;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.render.Renderer;
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


    // ------------------------------------------------------ Manifest Constants

    /**
     * The standard component type for this component.
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Input";

    /**
     * The standard component family for this component.
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Input";

    /**
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a conversion error
     * occurs, and neither the page author nor the {@link ConverterException} provides a message.
     */
    public static final String CONVERSION_MESSAGE_ID = "jakarta.faces.component.UIInput.CONVERSION";

    /**
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a required check fails.
     */
    public static final String REQUIRED_MESSAGE_ID = "jakarta.faces.component.UIInput.REQUIRED";

    /**
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a model update error
     * occurs, and the thrown exception has no message.
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


    UIInputImpl uiInputImpl;

    // ------------------------------------------------------------ Constructors


    /**
     * <p>
     * Create a new {@link UIInput} instance with default property values.
     * </p>
     */
    public UIInput() {
        super(new UIInputImpl());
        setRendererType("jakarta.faces.Text");
        this.uiInputImpl = (UIInputImpl) getUiComponentBaseImpl();
        uiInputImpl.setPeer(this);
    }


    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * Return the submittedValue value of this {@link UIInput} component. This method should only be used by the
     * <code>decode()</code> and <code>validate()</code> method of this component, or its corresponding {@link Renderer}.
     */
    @Override
    public Object getSubmittedValue() {
        return uiInputImpl.getSubmittedValue();
    }

    /**
     * Set the submittedValue value of this {@link UIInput} component. This method should only be used by the
     * <code>decode()</code> and <code>validate()</code> method of this component, or its corresponding {@link Renderer}.
     *
     * @param submittedValue The new submitted value
     */
    @Override
    public void setSubmittedValue(Object submittedValue) {
        uiInputImpl.setSubmittedValue(submittedValue);
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
        return uiInputImpl.getValue();
    }

    @Override
    public void setValue(Object value) {
        uiInputImpl.setValue(value);
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
        uiInputImpl.resetValue();
    }

    /**
     * Return the "local value set" state for this component. Calls to <code>setValue()</code> automatically reset this
     * property to <code>true</code>.
     */
    @Override
    public boolean isLocalValueSet() {
        return uiInputImpl.isLocalValueSet();
    }

    /**
     * Sets the "local value set" state for this component.
     */
    @Override
    public void setLocalValueSet(boolean localValueSet) {
        uiInputImpl.setLocalValueSet(localValueSet);
    }

    /**
     * Return the "required field" state for this component.
     */
    @Override
    public boolean isRequired() {
        return uiInputImpl.isRequired();
    }

    /**
     * If there has been a call to {@link #setRequiredMessage} on this instance, return the message. Otherwise, call
     * {@link #getValueExpression} passing the key "requiredMessage", get the result of the expression, and return it. Any
     * {@link ELException}s thrown during the call to <code>getValue()</code> must be wrapped in a {@link FacesException}
     * and rethrown.
     *
     * @return the required message.
     */
    public String getRequiredMessage() {
        return uiInputImpl.getRequiredMessage();
    }

    /**
     * Override any {@link ValueExpression} set for the "requiredMessage" with the literal argument provided to this method.
     * Subsequent calls to {@link #getRequiredMessage} will return this value;
     *
     * @param message the literal message value to be displayed in the event the user hasn't supplied a value and one is
     * required.
     */
    public void setRequiredMessage(String message) {
        uiInputImpl.setRequiredMessage(message);
    }

    /**
     * If there has been a call to {@link #setConverterMessage} on this instance, return the message. Otherwise, call
     * {@link #getValueExpression} passing the key "converterMessage", get the result of the expression, and return it. Any
     * {@link ELException}s thrown during the call to <code>getValue()</code> must be wrapped in a {@link FacesException}
     * and rethrown.
     *
     * @return the converter message.
     */
    public String getConverterMessage() {
        return uiInputImpl.getConverterMessage();
    }

    /**
     * Override any {@link ValueExpression} set for the "converterMessage" with the literal argument provided to this
     * method. Subsequent calls to {@link #getConverterMessage} will return this value;
     *
     * @param message the literal message value to be displayed in the event conversion fails.
     */
    public void setConverterMessage(String message) {
        uiInputImpl.setConverterMessage(message);
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
        return uiInputImpl.getValidatorMessage();
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
        uiInputImpl.setValidatorMessage(message);
    }

    @Override
    public boolean isValid() {
        return uiInputImpl.isValid();
    }

    @Override
    public void setValid(boolean valid) {
        uiInputImpl.setValid(valid);
    }

    /**
     * Set the "required field" state for this component.
     *
     * @param required The new "required field" state
     */
    @Override
    public void setRequired(boolean required) {
        uiInputImpl.setRequired(required);
    }

    @Override
    public boolean isImmediate() {
        return uiInputImpl.isImmediate();
    }

    @Override
    public void setImmediate(boolean immediate) {
        uiInputImpl.setImmediate(immediate);
    }


    // ----------------------------------------------------- UIComponent Methods

    /**
     * In addition to the actions taken in {@link UIOutput} when {@link PartialStateHolder#markInitialState()} is called,
     * check if any of the installed {@link Validator}s are PartialStateHolders and if so, call
     * {@link jakarta.faces.component.PartialStateHolder#markInitialState()} as appropriate.
     */
    @Override
    public void markInitialState() {
        uiInputImpl.markInitialState();

    }

    @Override
    public void clearInitialState() {
        uiInputImpl.clearInitialState();
    }

    /**
     * Specialized decode behavior on top of that provided by the superclass. In addition to the standard
     * <code>processDecodes</code> behavior inherited from {@link UIComponentBase}, calls <code>validate()</code> if the the
     * <code>immediate</code> property is true; if the component is invalid afterwards or a <code>RuntimeException</code> is
     * thrown, calls {@link FacesContext#renderResponse}.
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processDecodes(FacesContext context) {
        uiInputImpl.processDecodes(context);
    }

    /**
     * <span class="changed_modified_2_3">In</span> addition to the standard <code>processValidators</code> behavior
     * inherited from {@link UIComponentBase}, calls <code>validate()</code> if the <code>immediate</code> property is false
     * (which is the default); if the component is invalid afterwards, calls {@link FacesContext#renderResponse}.
     * <span class="changed_added_2_3">To ensure the {@code PostValidateEvent} is published at the proper time, this
     * component must be validated first, followed by the component's children and facets.</span> If a
     * <code>RuntimeException</code> is thrown during validation processing, calls {@link FacesContext#renderResponse} and
     * re-throw the exception.
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processValidators(FacesContext context) {
        uiInputImpl.processValidators(context);
    }

    /**
     * In addition to the standard <code>processUpdates</code> behavior inherited from {@link UIComponentBase}, calls
     * <code>updateModel()</code>. If the component is invalid afterwards, calls {@link FacesContext#renderResponse}. If a
     * <code>RuntimeException</code> is thrown during update processing, calls {@link FacesContext#renderResponse} and
     * re-throw the exception.
     *
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void processUpdates(FacesContext context) {
        uiInputImpl.processUpdates(context);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void decode(FacesContext context) {
        uiInputImpl.decode(context);
    }

    /**
     * <span class="changed_modified_2_0">Perform</span> the following algorithm to update the model data associated with
     * this {@link UIInput}, if any, as appropriate.
     *
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
        uiInputImpl.updateModel(context);
    }


    // ------------------------------------------------------ Validation Methods

    /**
     * <span class="changed_modified_2_0 changed_modified_2_2 changed_modified_2_3">Perform</span> the following algorithm
     * to validate the local value of this {@link UIInput}.
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
        uiInputImpl.validate(context);
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
    public Object getConvertedValue(FacesContext context, Object newSubmittedValue) throws ConverterException {
        return uiInputImpl.getConvertedValue(context, newSubmittedValue);
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
    public void validateValue(FacesContext context, Object newValue) {
        uiInputImpl.validateValue(context, newValue);
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
    public boolean compareValues(Object previous, Object value) {
        return uiInputImpl.compareValues(previous, value);
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
     * Add a {@link Validator} instance to the set associated with this {@link UIInput}.
     *
     * @param validator The {@link Validator} to add
     * @throws NullPointerException if <code>validator</code> is null
     */
    @Override
    public void addValidator(Validator validator) {
        uiInputImpl.addValidator(validator);

    }

    /**
     * Return the set of registered {@link Validator}s for this {@link UIInput} instance. If there are no registered
     * validators, a zero-length array is returned.
     */
    @Override
    public Validator[] getValidators() {
        return uiInputImpl.getValidators();
    }

    /**
     * Remove a {@link Validator} instance from the set associated with this {@link UIInput}, if it was previously
     * associated. Otherwise, do nothing.
     *
     * @param validator The {@link Validator} to remove
     */
    @Override
    public void removeValidator(Validator validator) {
        uiInputImpl.removeValidator(validator);
    }


    // ------------------------------------------------ Event Processing Methods

    /**
     * Add a new {@link ValueChangeListener} to the set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.
     *
     * @param listener The {@link ValueChangeListener} to be added
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    @Override
    public void addValueChangeListener(ValueChangeListener listener) {
        uiInputImpl.addValueChangeListener(listener);
    }

    /**
     * Return the set of registered {@link ValueChangeListener}s for this {@link UIInput} instance. If there are no
     * registered listeners, a zero-length array is returned.
     */
    @Override
    public ValueChangeListener[] getValueChangeListeners() {
        return uiInputImpl.getValueChangeListeners();
    }

    /**
     * Remove an existing {@link ValueChangeListener} (if any) from the set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.
     *
     * @param listener The {@link ValueChangeListener} to be removed
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {
        uiInputImpl.removeValueChangeListener(listener);
    }


    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {
        return uiInputImpl.saveState(context);
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        uiInputImpl.restoreState(context, state);
    }


    boolean considerEmptyStringNull(FacesContext context) {
        return uiInputImpl.considerEmptyStringNull(context);
    }

}
