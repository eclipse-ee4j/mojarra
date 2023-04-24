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

import com.sun.faces.api.component.UIOutputImpl;

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
     * The standard component type for this component.
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Output";

    /**
     * The standard component family for this component.
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Output";


    UIOutputImpl uiOutputImpl;

    // ------------------------------------------------------------ Constructors



    /**
     * <p>
     * Create a new {@link UIOutput} instance with default property values.
     * </p>
     */
    public UIOutput() {
        this(new UIOutputImpl());
        this.uiOutputImpl.setPeer(this);
    }

    /**
     * <p>
     * Create a new {@link UIOutput} instance with given property values.
     * </p>
     *
     * @param uiOutputImpl the UIOutputImpl to delegate to
     */
    public UIOutput(UIOutputImpl uiOutputImpl) {
        super(uiOutputImpl);
        setRendererType("jakarta.faces.Text");
        this.uiOutputImpl = (UIOutputImpl) getUiComponentBaseImpl();
    }


    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    // --------------------------------------- EditableValueHolder Properties

    @Override
    public Converter getConverter() {
        return uiOutputImpl.getConverter();
    }

    @Override
    public void setConverter(Converter converter) {
        uiOutputImpl.setConverter(converter);
    }

    @Override
    public Object getLocalValue() {
        return uiOutputImpl.getLocalValue();
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
        return uiOutputImpl.getValue();
    }

    @Override
    public void setValue(Object value) {
        uiOutputImpl.setValue(value);
    }

    /**
     * <p class="changed_added_2_2">
     * Convenience method to reset this component's value to the un-initialized state.
     * </p>
     *
     * @since 2.2
     */
    public void resetValue() {
        uiOutputImpl.resetValue();
    }

    /**
     * In addition to the actions taken in {@link UIComponentBase} when {@link PartialStateHolder#markInitialState()} is
     * called, check if the installed {@link Converter} is a PartialStateHolder and if it is, call
     * {@link jakarta.faces.component.PartialStateHolder#markInitialState()} on it.
     */
    @Override
    public void markInitialState() {
        uiOutputImpl.markInitialState();
    }

    @Override
    public void clearInitialState() {
        uiOutputImpl.clearInitialState();
    }

    @Override
    public Object saveState(FacesContext context) {
        return uiOutputImpl.saveState(context);
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        uiOutputImpl.restoreState(context, state);
    }
}
