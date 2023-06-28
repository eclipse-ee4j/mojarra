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

import java.io.IOException;

import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.RequiredValidator;
import jakarta.faces.validator.Validator;

/**
 * <p class="changed_added_2_0">
 * <strong class="changed_modified_2_2 changed_modified_2_3">UIViewParameter</strong> represents a binding between a
 * request parameter and a model property or {@link UIViewRoot} property. This is a bi-directional binding.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * The {@link jakarta.faces.view.ViewDeclarationLanguage} implementation must cause an instance of this component to
 * appear in the view for each occurrence of an <code>&lt;f:viewParam /&gt;</code> element placed inside of an
 * <code>&lt;f:metadata /&gt;</code> element. The user must place this facet within the <code>UIViewRoot</code>.
 * </p>
 *
 * <p>
 * Because this class extends <code>UIInput</code> any actions that one would normally take on a <code>UIInput</code>
 * instance are valid for instances of this class. Instances of this class participate in the regular Jakarta Server
 * Faces lifecycle, including on Ajax requests.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */
public class UIViewParameter extends UIInput {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.ViewParameter";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.ViewParameter";

    enum PropertyKeys {
        name, submittedValue
    }

    // ------------------------------------------------------ Instance Variables

    private Renderer inputTextRenderer = null;

    private transient Boolean emptyStringIsNull;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIViewParameter} instance with default property values.
     * </p>
     */
    public UIViewParameter() {

        super();
        setRendererType(null);

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>
     * The raw value is the "implicit" binding for this view parameter. This property maintains the submitted value of the
     * view parameter for the duration of the request. If the view parameter does not explicitly specify a value expression,
     * then when the request ends, this value is stored with the state of this component to use as the submitted value on an
     * ensuing postback.
     * </p>
     */
    private String rawValue;

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {

        return COMPONENT_FAMILY;

    }

    /**
     * <p class="changed_added_2_0">
     * Return the request parameter name from which the value is retrieved.
     * </p>
     *
     * @return the name.
     * @since 2.0
     */
    public String getName() {

        return (String) getStateHelper().eval(PropertyKeys.name);

    }

    /**
     * <p class="changed_added_2_0">
     * Set the request parameter name from which the value is retrieved.
     * </p>
     *
     * @param name The new request parameter name.
     * @since 2.0
     */
    public void setName(String name) {

        getStateHelper().put(PropertyKeys.name, name);

    }

    /**
     * <p class="changed_added_2_0">
     * Return <code>false</code>. The immediate setting is not relevant for view parameters and must be assumed to be
     * <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if immediate, <code>false</code> otherwise.
     * @since 2.0
     */
    @Override
    public boolean isImmediate() {
        return false;
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Assume</span> that the submitted value is always a string,
     * <span class="changed_added_2_2">but the return type from this method is <code>Object</code>.</span>.
     * </p>
     *
     * @return the submitted value.
     * @since 2.0
     */
    @Override
    public Object getSubmittedValue() {
        return getStateHelper().get(PropertyKeys.submittedValue);
    }

    /**
     * PENDING (docs) Interesting that submitted value isn't saved by the parent
     *
     * @param submittedValue The new submitted value
     */
    @Override
    public void setSubmittedValue(Object submittedValue) {
        getStateHelper().put(PropertyKeys.submittedValue, submittedValue);
    }

    // ----------------------------------------------------- UIComponent Methods

    // This is the "Apply Request Phase" step
    // QUESTION should we just override processDecodes() directly?
    // ANSWER: In this case, no. We don't want to take responsibility for
    // traversing any children we may have in the future.

    /**
     * <p class="changed_added_2_0">
     * Override behavior from superclass to pull a value from the incoming request parameter map under the name given by
     * {@link #getName} and store it with a call to {@link UIInput#setSubmittedValue}.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public void decode(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // QUESTION can we move forward and support an array? no different than UISelectMany; perhaps need to know
        // if the value expression is single or multi-valued
        // ANSWER: I'd rather not right now.
        String paramValue = context.getExternalContext().getRequestParameterMap().get(getName());

        // submitted value will stay as previous value (null on initial request) if a parameter is absent
        if (paramValue != null) {
            setSubmittedValue(paramValue);
        }

        rawValue = (String) getSubmittedValue();
        setValid(true);

    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_3">Specialize</span> superclass behavior to treat <code>null</code> differently. In
     * this class, a <code>null</code> value along with the "required" flag being set to <code>true</code> will cause a
     * validation failure. <span class="changed_added_2_3">Otherwise, If the {@link UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME}
     * context parameter is true and the value is {@code null}, call {@link UIInput#setSubmittedValue} passing the empty
     * string as the argument. This will cause the normal validation processing to happen, including bean validation.</span>
     * </p>
     *
     * @param context the Faces context.
     * @since 2.0
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

        Object submittedValue = getSubmittedValue();

        // we have to override since UIInput assumes that a null value means don't check
        if (submittedValue == null && myIsRequired()) {
            String requiredMessageStr = getRequiredMessage();
            FacesMessage message;
            if (null != requiredMessageStr) {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR, requiredMessageStr, requiredMessageStr);
            } else {
                message = MessageFactory.getMessage(context, REQUIRED_MESSAGE_ID, MessageFactory.getLabel(context, this));
            }
            context.addMessage(getClientId(context), message);
            setValid(false);
            context.validationFailed();
            context.renderResponse();
        } else {
            super.processValidators(context);
        }
    }

    private boolean myIsRequired() {
        return super.isRequired() || isRequiredViaNestedRequiredValidator();
    }

    /*
     * JAVASERVERFACES-3058. Handle the nested requiredValidator case explicitly in the case of <f:viewParam>.
     *
     */
    private boolean isRequiredViaNestedRequiredValidator() {
        if ( validators == null ) {
            return false;
        }

        boolean result = false;
        for (Validator<?> validator : validators) {
            if (validator instanceof RequiredValidator) {
                // See JAVASERVERFACES-2526. Note that we can assume
                // that at this point the validator is not disabled,
                // so the mere existence of the validator implies it is
                // enabled.
                result = true;
                Object submittedValue = getSubmittedValue();
                if (submittedValue == null) {
                    // JAVASERVERFACES-3058 asserts that view parameters
                    // should be treated differently than form parameters
                    // if they are not submitted. I'm not sure if that's
                    // correct, but let's put this in and see how
                    // the community responds.
                    setSubmittedValue("");
                }
                break;
            }
        }

        return result;
    }

    /**
     * <p class="changed_added_2_0">
     * Call through to superclass {@link UIInput#updateModel} then take the additional action of pushing the value into
     * request scope if and only if the value is not a value expression, is valid, and the local value was set on this
     * lifecycle execution.
     * </p>
     *
     * @since 2.0
     */

    @Override
    public void updateModel(FacesContext context) {
        super.updateModel(context);
        if (!hasValueExpression() && isValid() && isLocalValueSet()) {
            // QUESTION should this be done even when a value expression is present?
            // ANSWER: I don't see why not.
            context.getExternalContext().getRequestMap().put(getName(), getLocalValue());
        }
    }

    // This is called during the real "Render Response" phase

    /**
     * <p class="changed_added_2_0">
     * Called specially by {@link UIViewRoot#encodeEnd}, this method simply sets the submitted value to be the return from
     * {@link #getStringValue}.
     * </p>
     *
     * @throws IOException when an I/O error occurs.
     * @since 2.0
     */
    @Override
    public void encodeAll(FacesContext context) throws IOException {
        if (context == null) {
            throw new NullPointerException();
        }

        // if there is a value expression, update view parameter w/ latest value after render
        // QUESTION is it okay that a null string value may be suppressing the view parameter value?
        // ANSWER: I'm not sure.
        setSubmittedValue(getStringValue(context));
    }

    /**
     * <p class="changed_added_2_0">
     * If the value of this parameter comes from a <code>ValueExpression</code> return the value of the expression,
     * otherwise, return the local value.
     * </p>
     *
     * @param context the Faces context.
     * @return the string value.
     * @since 2.0
     */

    public String getStringValue(FacesContext context) {
        String result = null;
        if (hasValueExpression()) {
            result = getStringValueFromModel(context);
        } else {
            result = null != rawValue ? rawValue : (String) getValue();
        }
        return result;
    }

    /**
     * <p class="changed_added_2_0">
     * Manually perform standard conversion steps to get a string value from the value expression.
     * </p>
     *
     * @param context the Faces context.
     * @return the string value from the model.
     * @since 2.0
     */

    public String getStringValueFromModel(FacesContext context) throws ConverterException {
        ValueExpression ve = getValueExpression("value");
        if (ve == null) {
            return null;
        }

        Object currentValue = ve.getValue(context.getELContext());

        // If there is a converter attribute, use it to to ask application
        // instance for a converter with this identifier.
        Converter converter = getConverter();

        if (converter == null) {
            // if value is null and no converter attribute is specified, then
            // return null (null has meaning for a view parameters; it means remove it).
            if (currentValue == null) {
                return null;
            }
            // Do not look for "by-type" converters for Strings
            if (currentValue instanceof String) {
                return (String) currentValue;
            }

            // if converter attribute set, try to acquire a converter
            // using its class type.

            Class<?> converterType = currentValue.getClass();
            converter = context.getApplication().createConverter(converterType);

            // if there is no default converter available for this identifier,
            // assume the model type to be String.
            if (converter == null) {
                return currentValue.toString();
            }
        }

        return converter.getAsString(context, this, currentValue);
    }

    /**
     * <p class="changed_added_2_0">
     * Because this class has no {@link Renderer}, leverage the one from the standard HTML_BASIC {@link RenderKit} with
     * <code>component-family: jakarta.faces.Input</code> and <code>renderer-type: jakarta.faces.Text</code> and call its
     * {@link Renderer#getConvertedValue} method.
     * </p>
     *
     * @param submittedValue the submitted value.
     * @return the converted value.
     * @since 2.0
     */
    @Override
    protected Object getConvertedValue(FacesContext context, Object submittedValue) throws ConverterException {

        return getInputTextRenderer(context).getConvertedValue(context, this, submittedValue);

    }

    private Renderer getInputTextRenderer(FacesContext context) {
        if (null == inputTextRenderer) {
            RenderKitFactory rkf = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit standard = rkf.getRenderKit(context, RenderKitFactory.HTML_BASIC_RENDER_KIT);
            inputTextRenderer = standard.getRenderer("jakarta.faces.Input", "jakarta.faces.Text");
        }
        assert null != inputTextRenderer;
        return inputTextRenderer;
    }

    // ----------------------------------------------------- Helper Methods

    private boolean hasValueExpression() {
        return null != getValueExpression("value");
    }

    /**
     * <p class="changed_added_2_0">
     * Inner class to encapsulate a <code>UIViewParameter</code> instance so that it may be safely referenced regardless of
     * whether or not the current view is the same as the view in which this <code>UIViewParameter</code> resides.
     * </p>
     *
     * @since 2.0
     */

    public static class Reference {

        private StateHolderSaver saver;
        private int indexInParent;
        private String viewIdAtTimeOfConstruction;

        /**
         * <p class="changed_added_2_0">
         * Construct a reference to a <code>UIViewParameter</code>. This constructor cause the {@link StateHolder#saveState}
         * method to be called on argument <code>UIViewParameter</code>.
         * </p>
         *
         * @param context the <code>FacesContext</code>for this request
         * @param param the UIViewParameter.
         * @param indexInParent the index of the <code>UIViewParameter</code> in its parent <code>UIPanel</code>.
         * @param viewIdAtTimeOfConstruction the viewId of the view in which the <code>UIViewParameter</code> is included. This
         * may not be the same as the viewId from the <code>context</code> argument.
         *
         * @since 2.0
         */
        public Reference(FacesContext context, UIViewParameter param, int indexInParent, String viewIdAtTimeOfConstruction) {
            saver = new StateHolderSaver(context, param);
            this.indexInParent = indexInParent;
            this.viewIdAtTimeOfConstruction = viewIdAtTimeOfConstruction;
        }

        /**
         * <p class="changed_added_2_0">
         * Return the <code>UIViewParameter</code> to which this instance refers. If the current viewId is the same as the
         * viewId passed to our constructor, use the index passed to the constructor to find the actual
         * <code>UIViewParameter</code> instance and return it. Otherwise, call {@link StateHolder#restoreState} on the saved
         * state and return the result.
         * </p>
         *
         * @param context the <code>FacesContext</code>for this request
         * @return the UIViewParameter.
         * @since 2.0
         */
        public UIViewParameter getUIViewParameter(FacesContext context) {
            UIViewParameter result = null;
            UIViewRoot root = context.getViewRoot();
            // If the view root is the same as when we were constructed...
            if (viewIdAtTimeOfConstruction.equals(root.getViewId())) {
                // get the actual view parameter from the tree...
                UIComponent metadataFacet = root.getFacet(UIViewRoot.METADATA_FACET_NAME);
                result = (UIViewParameter) metadataFacet.getChildren().get(indexInParent);
            } else {
                // otherwise, use the saved one
                result = (UIViewParameter) saver.restore(context);
            }

            return result;
        }

    }

}
