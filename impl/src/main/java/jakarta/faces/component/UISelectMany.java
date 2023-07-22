/*
 * Copyright (c) 2022, 2022 Contributors to Eclipse Foundation.
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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.model.SelectItem;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_3">UISelectMany</strong> is a {@link UIComponent} that
 * represents the user's choice of a zero or more items from among a discrete set of available options. The user can
 * modify the selected values. Optionally, the component can be preconfigured with zero or more currently selected
 * items, by storing them as an array <span class="changed_added_2_0">or <code>Collection</code></span> in the
 * <code>value</code> property of the component.
 * </p>
 *
 * <p>
 * This component is generally rendered as a select box or a group of checkboxes.
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Listbox</code>". This value
 * can be changed by calling the <code>setRendererType()</code> method.
 * </p>
 *
 * <p>
 * The {@link jakarta.faces.render.Renderer} for this component must perform the following logic on
 * <a id="getConvertedValue"><code>getConvertedValue()</code></a>:
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * Obtain the {@link jakarta.faces.convert.Converter} using the following algorithm:
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * If the component has an attached {@link jakarta.faces.convert.Converter}, use it.
 * </p>
 *
 * <p>
 * If not, look for a {@link ValueExpression} for <code>value</code> (if any). The {@link ValueExpression} must point to
 * something that is:
 * </p>
 *
 * <ul>
 * <li>
 * <p>
 * An array of primitives (such as <code>int[]</code>). Look up the registered by-class
 * {@link jakarta.faces.convert.Converter} for this primitive type.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * An array of objects (such as <code>Integer[]</code> or <code>String[]</code>). Look up the registered by-class
 * {@link jakarta.faces.convert.Converter} for the underlying element type.
 * </p>
 * </li>
 *
 * <li class="changed_added_2_0">
 * <p>
 * A <code>java.util.Collection</code>. Do not convert the values. <span class="changed_modified_2_3">Instead, convert
 * the provided set of available options to string, exactly as done during render response, and for any match with the
 * submitted values, add the available option as object to the collection.</span>
 * </p>
 * </li>
 * </ul>
 * </blockquote>
 *
 * <p>
 * If for any reason a <code>Converter</code> cannot be found, assume the type to be a String array.
 * </p>
 *
 * </blockquote>
 *
 * <p>
 * Use the selected {@link jakarta.faces.convert.Converter} (if any) to convert each element in the values array from
 * the request to the proper type, <span class="changed_added_2_0">and store the result of each conversion in a data
 * structure, called <em>targetForConvertedValues</em> for discussion. Create <em>targetForConvertedValues</em> using
 * the following algorithm.</span>
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <ul>
 *
 * <li>
 * <p>
 * If the component has a <code>ValueExpression</code> for <code>value</code> and the type of the expression is an
 * array, let <em>targetForConvertedValues</em> be a new array of the expected type.
 * </p>
 * </li>
 *
 *
 * <li>
 * <p>
 * If the component has a <code>ValueExpression</code> for <code>value</code>, let <em>modelType</em> be the type of the
 * value expression. If <em>modelType</em> is a <code>Collection</code>, do the following to arrive at
 * <em>targetForConvertedValues</em>:
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * Ask the component for its attribute under the key "<code>collectionType</code>", without the quotes. If there is a
 * value for that key, the value must be a String that is a fully qualified Java class name, or a <code>Class</code>
 * object, or a <code>ValueExpression</code> that evaluates to a String or a <code>Class</code>. In all cases, the value
 * serves to identify the concrete type of the class that implements <code>Collection</code>. For discussion, this is
 * called <em>collectionType</em>. Let <em>targetForConvertedValues</em> be a new instance of <code>Collection</code>
 * implemented by the concrete class specified in <em>collectionType</em>. If, <em>collectionType</em> can not be
 * discovered, or an instance of <code>Collection</code> implemented by the concrete class specified in
 * <em>collectionType</em> cannot be created, throw a {@link jakarta.faces.FacesException} with a correctly localized
 * error message. Note that <code>FacesException</code> is thrown instead of <code>ConverterException</code> because
 * this case would only arise from developer error, rather than end-user error.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If there is no "<code>collectionType</code>" attribute, call <code>getValue()</code> on the component. The result
 * will implement <code>Collection</code>. If the result also implements <code>Cloneable</code>, let
 * <em>targetForConvertedValues</em> be the result of calling its <code>clone()</code> method, then calling
 * <code>clear()</code> on the cloned <code>Collection</code>. If unable to clone the value for any reason, log a
 * message and proceed to the next step.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If <em>modelType</em> is a concrete class, let <em>targetForConvertedValues</em> be a new instance of that class.
 * Otherwise, the concrete type for <em>targetForConvertedValues</em> is taken from the following table. All classes are
 * in the <code>java.util</code> package. All collections must be created with an initial capacity equal to the length
 * of the values array from the request.
 * </p>
 *
 * <table border="1">
 * <caption>modelType to targetForConvertedValues mapping</caption>
 *
 * <tr>
 *
 * <th>If <em>modelType</em> is an instance of</th>
 *
 * <th>then <em>targetForConvertedValues</em> must be an instance of</th>
 *
 * </tr>
 *
 * <tr>
 *
 * <td><code>SortedSet</code></td>
 *
 * <td><code>TreeSet</code></td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td><code>Queue</code></td>
 *
 * <td><code>LinkedList</code></td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td><code>Set</code></td>
 *
 * <td><code>HashSet</code></td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>anything else</td>
 *
 * <td><code>ArrayList</code></td>
 *
 * </tr>
 *
 * </table>
 *
 * </li>
 *
 * </ul>
 *
 * <li>
 * <p>
 * If the component does not have a <code>ValueExpression</code> for <code>value</code>, let
 * <em>targetForConvertedValues</em> be an array of type <code>Object</code>.
 * </p>
 *
 * </ul>
 *
 * </div>
 *
 * <p>
 * Return <em>targetForConvertedValues</em> after populating it with the converted values.
 * </p>
 *
 *
 */
public class UISelectMany extends UIInput {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectMany";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.SelectMany";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a value not matching
     * the available options is specified.
     */
    public static final String INVALID_MESSAGE_ID = "jakarta.faces.component.UISelectMany.INVALID";

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UISelectMany} instance with default property values.
     * </p>
     */
    public UISelectMany() {
        super();
        setRendererType("jakarta.faces.Listbox");
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    private transient Object submittedValue = null;

    @Override
    public Object getSubmittedValue() {
        if (submittedValue == null && !isValid() && considerEmptyStringNull(FacesContext.getCurrentInstance())) { // JAVASERVERFACES_SPEC_PUBLIC-671
            return new String[0]; // Mojarra#5081
        }

        return submittedValue;
    }

    @Override
    public void setSubmittedValue(Object submittedValue) {
        this.submittedValue = submittedValue;
    }

    /**
     * <p>
     * Return the currently selected values, or <code>null</code> if there are no currently selected values. This is a
     * typesafe alias for <code>getValue()</code>.
     * </p>
     *
     * @return the selected values, or <code>null</code>.
     */
    public Object[] getSelectedValues() {
        return (Object[]) getValue();
    }

    /**
     * <p>
     * Set the currently selected values, or <code>null</code> to indicate that there are no currently selected values. This
     * is a typesafe alias for <code>setValue()</code>.
     * </p>
     *
     * @param selectedValues The new selected values (if any)
     */
    public void setSelectedValues(Object[] selectedValues) {
        setValue(selectedValues);
    }

    // ---------------------------------------------------------------- Bindings


    /**
     * <p>
     * Return any {@link ValueExpression} set for <code>value</code> if a {@link ValueExpression} for
     * <code>selectedValues</code> is requested; otherwise, perform the default superclass processing for this method.
     * </p>
     *
     * @param name Name of the attribute or property for which to retrieve a {@link ValueExpression}
     * @return the value expression, or <code>null</code>.
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @since 1.2
     */
    @Override
    public ValueExpression getValueExpression(String name) {
        if ("selectedValues".equals(name)) {
            return super.getValueExpression("value");
        } else {
            return super.getValueExpression(name);
        }

    }

    /**
     * <p>
     * Store any {@link ValueExpression} specified for <code>selectedValues</code> under <code>value</code> instead;
     * otherwise, perform the default superclass processing for this method.
     * </p>
     *
     * @param name Name of the attribute or property for which to set a {@link ValueExpression}
     * @param binding The {@link ValueExpression} to set, or <code>null</code> to remove any currently set
     * {@link ValueExpression}
     *
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @since 1.2
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {
        if ("selectedValues".equals(name)) {
            super.setValueExpression("value", binding);
        } else {
            super.setValueExpression(name, binding);
        }

    }

    // --------------------------------------------------------- UIInput Methods

    /**
     * <p>
     * Return <code>true</code> if the new value is different from the previous value. Value comparison must not be
     * sensitive to element order.
     * </p>
     *
     * @param previous old value of this component
     * @param value new value of this component
     * @return <code>true</code> if the new value is different from the previous value, <code>false</code> otherwise.
     */
    @Override
    protected boolean compareValues(Object previous, Object value) {
        if (previous == null && value != null) {
            return true;
        } else if (previous != null && value == null) {
            return true;
        } else if (previous == null) {
            return false;
        }

        boolean valueChanged = false;
        Object[] oldarray;
        Object[] newarray;

        // The arrays may be arrays of primitives; for simplicity,
        // perform the boxing here.
        if (!(previous instanceof Object[])) {
            previous = toObjectArray(previous);
        }

        if (!(value instanceof Object[])) {
            value = toObjectArray(value);
        }

        // If values are still not of the type Object[], it is perhaps a
        // mistake by the renderers, so return false, so that
        // ValueChangedEvent is not queued in this case.
        if (!(previous instanceof Object[]) || !(value instanceof Object[])) {
            return false;
        }
        oldarray = (Object[]) previous;
        newarray = (Object[]) value;

        // If we got here then both the arrays cannot be null
        // if their lengths vary, return false.
        if (oldarray.length != newarray.length) {
            return true;
        }

        // make sure every element in the previous array occurs the same
        // number of times in the current array. This should help us
        // to find out the values changed are not. Since we cannot assume
        // the browser will send the elements in the same order everytime,
        // it will not suffice to just compare the element position and position.
        int count1;
        int count2;
        for (Object element : oldarray) {
            count1 = countElementOccurrence(element, oldarray);
            count2 = countElementOccurrence(element, newarray);
            if (count1 != count2) {
                valueChanged = true;
                break;
            }
        }
        return valueChanged;

    }

    /**
     * <p>
     * Return the number of occurrences of a particular element in the array.
     * </p>
     *
     * @param element object whose occurrance is to be counted in the array.
     * @param array object representing the old value of this component.
     */
    private static int countElementOccurrence(Object element, Object[] array) {
        int count = 0;
        for (Object arrayElement : array) {
            if (arrayElement != null && element != null) {
                if (arrayElement.equals(element)) {
                    count++;
                }
            }
        }
        return count;

    }

    /**
     * Convert an array of primitives to an array of boxed objects.
     *
     * @param primitiveArray object containing the primitive values
     * @return an Object array, or null if the incoming value is not in fact an array at all.
     */
    private static Object[] toObjectArray(Object primitiveArray) {
        if (primitiveArray == null) {
            throw new NullPointerException();
        }

        if (primitiveArray instanceof Object[]) {
            return (Object[]) primitiveArray;
        }

        if (primitiveArray instanceof Collection) {
            return ((Collection) primitiveArray).toArray();
        }

        Class<?> clazz = primitiveArray.getClass();
        if (!clazz.isArray()) {
            return null;
        }

        int length = Array.getLength(primitiveArray);
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = Array.get(primitiveArray, i);
        }

        return array;
    }

    // ------------------------------------------------------ Validation Methods

    /**
     * <p>
     * <span class="changed_modified_2_0">In</span> addition to the standard validation behavior inherited from
     * {@link UIInput}, ensure that any specified values are equal to one of the available options. Before comparing each
     * option, coerce the option value type to the type of this component's value following the Expression Language coercion
     * rules. If the specified value is not equal to any of the options, enqueue an error message and set the
     * <code>valid</code> property to <code>false</code>.
     * </p>
     *
     * <p class="changed_modified_2_0">
     * This method must explicitly support a value argument that is a single value or a value argument that is a
     * <code>Collection</code> or Array of values.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If {@link #isRequired} returns <code>true</code>, and the current value is equal to the value of an inner
     * {@link UISelectItem} whose {@link UISelectItem#isNoSelectionOption} method returns <code>true</code>, enqueue an
     * error message and set the <code>valid</code> property to <code>false</code>.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     *
     * @param value The converted value to test for membership.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */

    @Override
    protected void validateValue(FacesContext context, Object value) {
        super.validateValue(context, value);

        // Skip validation if it is not necessary
        if (!isValid() || value == null) {
            return;
        }

        boolean doAddMessage = false;

        // Ensure that the values match one of the available options
        // Don't arrays cast to "Object[]", as we may now be using an array
        // of primitives
        Converter converter = getConverter();
        for (Iterator i = getValuesIterator(value); i.hasNext();) {
            Iterator<SelectItem> items = new SelectItemsIterator(context, this);
            Object currentValue = i.next();
            if (!SelectUtils.matchValue(context, this, currentValue, items, converter)) {
                doAddMessage = true;
                break;
            }
        }

        // Ensure that if the value is noSelection and a
        // value is required, a message is queued
        if (isRequired()) {
            for (Iterator i = getValuesIterator(value); i.hasNext();) {
                Iterator<SelectItem> items = new SelectItemsIterator(context, this);
                Object currentValue = i.next();
                if (SelectUtils.valueIsNoSelectionOption(context, this, currentValue, items, converter)) {
                    doAddMessage = true;
                    break;
                }
            }
        }

        if (doAddMessage) {
            // Enqueue an error message if an invalid value was specified
            FacesMessage message = MessageFactory.getMessage(context, INVALID_MESSAGE_ID, MessageFactory.getLabel(context, this));
            context.addMessage(getClientId(context), message);
            setValid(false);
        }

    }

    // --------------------------------------------------------- Private Methods

    private Iterator getValuesIterator(Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).iterator();
        }

        return new ArrayIterator(value);
    }

    // ---------------------------------------------------------- Nested Classes

    /**
     * Exposes an Array as an Iterator.
     */
    private static final class ArrayIterator implements Iterator {

        private final int length;
        private final Object value;
        private int idx = 0;

        // -------------------------------------------------------- Constructors

        ArrayIterator(Object value) {

            this.value = value;
            length = Array.getLength(value);

        }

        // ------------------------------------------------------------ Iterator

        @Override
        public boolean hasNext() {
            return idx < length;
        }

        @Override
        public Object next() {

            if (idx >= length) {
                throw new NoSuchElementException();
            } else {
                return Array.get(value, idx++);
            }

        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();

        }

    }
}
