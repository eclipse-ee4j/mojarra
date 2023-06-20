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

import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * <p>
 * <strong class="changed_modified_2_0_rev_a changed_modified_2_2">UIOutput</strong> is a {@link UIComponent} that has a
 * value, optionally retrieved from a model tier bean via a value expression, that is displayed to the user. The user
 * cannot directly modify the rendered value; it is for display purposes only.
 * </p>
 *
 * <p>
 * During the <em>Render Response</em> phase of the request processing lifecycle, the current value of this component
 * must be converted to a String (if it is not already), according to the following rules:
 * </p>
 * <ul>
 * <li>If the current value is not <code>null</code>, and is not already a <code>String</code>, locate a
 * {@link Converter} (if any) to use for the conversion, as follows:
 * <ul>
 * <li>If <code>getConverter()</code> returns a non-null {@link Converter}, use that one, otherwise</li>
 * <li>If <code>Application.createConverter(Class)</code>, passing the current value's class, returns a non-null
 * {@link Converter}, use that one.</li>
 * </ul>
 * </li>
 * <li>If the current value is not <code>null</code> and a {@link Converter} was located, call its
 * <code>getAsString()</code> method to perform the conversion.</li>
 * <li>If the current value is not <code>null</code> but no {@link Converter} was located, call <code>toString()</code>
 * on the current value to perform the conversion.</li>
 * </ul>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Text</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class UIOutput extends UIComponentBase implements ValueHolder {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Output";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Output";

    private static final String FORCE_FULL_CONVERTER_STATE = "com.sun.faces.component.UIOutput.forceFullConverterState";

    enum PropertyKeys {
        value, converter
    }

    private Converter<?> converter;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIOutput} instance with default property values.
     * </p>
     */
    public UIOutput() {
        super();
        setRendererType("jakarta.faces.Text");
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    // --------------------------------------- EditableValueHolder Properties

    @Override
    public Converter getConverter() {

        if (converter != null) {
            return converter;
        }

        return (Converter) getStateHelper().eval(PropertyKeys.converter);
    }

    @Override
    public void setConverter(Converter converter) {

        clearInitialState();
        // we don't push the converter to the StateHelper
        // if it's been explicitly set (i.e. it's not a ValueExpression
        // reference
        this.converter = converter;
    }

    @Override
    public Object getLocalValue() {
        return getStateHelper().get(PropertyKeys.value);
    }

    /**
     * <p class="changed_added_2_2">
     * Return the value property.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    @Override
    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    /**
     * <p class="changed_added_2_2">
     * Convenience method to reset this component's value to the un-initialized state.
     * </p>
     *
     * @since 2.2
     */
    public void resetValue() {
        getStateHelper().remove(PropertyKeys.value);
    }

    /**
     * <p>
     * In addition to the actions taken in {@link UIComponentBase} when {@link PartialStateHolder#markInitialState()} is
     * called, check if the installed {@link Converter} is a PartialStateHolder and if it is, call
     * {@link jakarta.faces.component.PartialStateHolder#markInitialState()} on it.
     * </p>
     */
    @Override
    public void markInitialState() {
        super.markInitialState();

        Converter<?> c = getConverter();
        if (c instanceof PartialStateHolder) {
            ((PartialStateHolder) c).markInitialState();
        }
    }

    @Override
    public void clearInitialState() {
        if (initialStateMarked()) {
            super.clearInitialState();

            Converter<?> c = getConverter();
            if (c instanceof PartialStateHolder) {
                ((PartialStateHolder) c).clearInitialState();
            }
        }
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object converterState = null;
        if (converter != null) {
            if (!initialStateMarked() || getAttributes().containsKey(FORCE_FULL_CONVERTER_STATE)) {
                /*
                 * Check if our parent component has its initial state marked and we know we don't. That means we are not using the same
                 * state saving algorithm. So we are going to ALWAYS force to do FSS for the converter.
                 */
                if (getParent() != null && getParent().initialStateMarked()) {
                    getAttributes().put(FORCE_FULL_CONVERTER_STATE, true);
                    if (converter instanceof PartialStateHolder) {
                        PartialStateHolder partialStateHolder = (PartialStateHolder) converter;
                        partialStateHolder.clearInitialState();
                    }
                }

                converterState = saveAttachedState(context, converter);
            } else {
                if (converter instanceof StateHolder) {
                    StateHolder stateHolder = (StateHolder) converter;
                    if (!stateHolder.isTransient()) {
                        converterState = ((StateHolder) converter).saveState(context);
                    }
                }
            }
        }

        Object[] values = (Object[]) super.saveState(context);

        if (converterState != null || values != null) {
            return new Object[] { values, converterState };
        }

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

        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        Object converterState = values[1];
        if (converterState instanceof StateHolderSaver) {
            // this means full state was saved and as such
            // overwrite any existing converter with the saved
            // converter
            converter = (Converter<?>) restoreAttachedState(context, converterState);
        } else {
            // apply any saved state to the existing converter
            if (converterState != null && converter instanceof StateHolder) {
                ((StateHolder) converter).restoreState(context, converterState);
            }
        }
    }
}
