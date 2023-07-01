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

package jakarta.faces.component.behavior;

import static jakarta.faces.component.behavior.ClientBehaviorHint.SUBMITTING;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorListener;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_0_rev_a changed_modified_2_2">An</span> instance of this class is added as a
 * {@link ClientBehavior} to a component using the
 * {@link jakarta.faces.component.behavior.ClientBehaviorHolder#addClientBehavior} contract that components implement.
 * The presence of this {@link ClientBehavior} will cause the rendering of JavaScript that produces an <code>Ajax</code>
 * request using the specification public JavaScript API when the component is rendered.
 * </p>
 *
 * <p class="changed_modified_2_0_rev_a">
 * If the component is an instance of {@link jakarta.faces.component.EditableValueHolder}, Where at all possible, the
 * component must have the UI register the ajax event when the initial value is changed, not when focus is lost on the
 * component.
 * </p>
 *
 * <!-- https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=1219 -->
 *
 * @since 2.0
 */
public class AjaxBehavior extends ClientBehaviorBase {

    /**
     * <p class="changed_added_2_0">
     * The standard id for this behavior.
     * </p>
     */
    public static final String BEHAVIOR_ID = "jakarta.faces.behavior.Ajax";

    private static final Set<ClientBehaviorHint> HINTS = unmodifiableSet(EnumSet.of(SUBMITTING));

    private String onerror;
    private String onevent;
    private String delay;
    private List<String> execute;
    private List<String> render;
    private Boolean disabled;
    private Boolean immediate;
    private Boolean resetValues;

    private Map<String, ValueExpression> bindings;

    /**
     * Default constructor that just creates this instance.
     */
    public AjaxBehavior() {

    }

    // ---------------------------------------------------------- Public Methods
    @Override
    public String getRendererType() {
        // We use the same sring for both the behavior id and the renderer
        // type.
        return BEHAVIOR_ID;
    }

    /**
     * <p class="changed_added_2_0">
     * This method returns an unmodifiable <code>Set</code> containing the {@link ClientBehaviorHint}
     * <code>SUBMITTING</code>.
     * </p>
     *
     * @return unmodifiable set containing the hint {@link ClientBehaviorHint} <code>SUBMITTING</code>.
     *
     * @since 2.0
     */
    @Override
    public Set<ClientBehaviorHint> getHints() {
        return HINTS;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>String</code> of JavaScript function name that will be used to identify the client callback function
     * that should be run in the event of an error.
     *
     * @return the JavaScript function name of <code>ONERROR</code>.
     *
     * @since 2.0
     */
    public String getOnerror() {
        return (String) eval(ONERROR, onerror);
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the JavaScript function name that will be used to identify the client callback function that should be run in
     * the event of an error.
     *
     * @param onerror the error handling function name
     *
     * @since 2.0
     */
    public void setOnerror(String onerror) {
        this.onerror = onerror;

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>String</code> of JavaScript function name that will be used to identify the client callback function
     * that should be run on the occurance of a client-side event.
     *
     * @return the JavaScript function name of <code>ONEVENT</code>.
     *
     * @since 2.0
     */
    public String getOnevent() {
        return (String) eval(ONEVENT, onevent);
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the JavaScript function name that will be used to identify the client callback function that should be run in
     * response to event activity.
     *
     * @param onevent the event handling function name
     *
     * @since 2.0
     */
    public void setOnevent(String onevent) {
        this.onevent = onevent;

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Return a non-empty <code>Collection&lt;String&gt;</code> of component identifiers that will be used to identify
     * components that should be processed during the <code>execute</code> phase of the request processing lifecycle.
     * </p>
     * <p>
     * Note that the returned collection may be unmodifiable. Modifications should be performed by calling
     * {@link #setExecute}.
     * </p>
     *
     * @return the JavaScript function name of <code>EXECUTE</code>.
     *
     * @since 2.0
     */
    public Collection<String> getExecute() {
        return getCollectionValue(EXECUTE, execute);
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the component identifiers that will be used to identify components that should be processed during the
     * <code>execute</code> phase of the request processing lifecycle.
     * </p>
     *
     * @param execute the ids of components to execute
     *
     * @since 2.0
     */
    public void setExecute(Collection<String> execute) {
        this.execute = copyToList(execute);

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_2">
     * Returns the delay value, or <code>null</code> if no value was set.
     * </p>
     *
     * @return the delay value.
     *
     * @since 2.2
     */
    public String getDelay() {
        return (String) eval(DELAY, delay);
    }

    /**
     * <p class="changed_added_2_2">
     * If less than <em>delay</em> milliseconds elapses between calls to <em>request()</em> only the most recent one is sent
     * and all other requests are discarded. The default value of this option is 300. If the value of <em>delay</em> is the
     * literal string <code>'none'</code> without the quotes, no delay is used.
     * </p>
     *
     * @param delay the ajax delay value
     *
     * @since 2.2
     */
    public void setDelay(String delay) {
        this.delay = delay;

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Return a non-empty <code>Collection&lt;String&gt;</code> of component identifiers that will be used to identify
     * components that should be processed during the <code>render</code> phase of the request processing lifecycle.
     * </p>
     * <p>
     * Note that the returned collection may be unmodifiable. Modifications should be performed by calling
     * {@link #setRender}.
     * </p>
     *
     * @return the ids of components to render.
     *
     * @since 2.0
     */
    public Collection<String> getRender() {
        return getCollectionValue(RENDER, render);
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the component identifiers that will be used to identify components that should be processed during the
     * <code>render</code> phase of the request processing lifecycle.
     * </p>
     *
     * @param render the ids of components to render
     *
     * @since 2.0
     */
    public void setRender(Collection<String> render) {
        this.render = copyToList(render);

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_2">
     * Return the resetValues status of this behavior.
     * </p>
     *
     * @return the resetValues status.
     *
     * @since 2.2
     */

    public boolean isResetValues() {
        Boolean result = (Boolean) eval(RESET_VALUES, resetValues);
        return result != null ? result : false;
    }

    /**
     * <p class="changed_added_2_2">
     * Set the resetValues status of this behavior.
     * </p>
     *
     * @param resetValues the resetValues status.
     *
     * @since 2.2
     */

    public void setResetValues(boolean resetValues) {
        this.resetValues = resetValues;

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Return the disabled status of this behavior.
     * </p>
     *
     * @return the disabled status of this behavior.
     *
     * @since 2.0
     */
    public boolean isDisabled() {
        Boolean result = (Boolean) eval(DISABLED, disabled);
        return result != null ? result : false;
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the disabled status of this behavior.
     * </p>
     *
     * @param disabled the flag to be set.
     *
     * @since 2.0
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Return the immediate status of this behavior.
     * </p>
     *
     * @return the immediate status.
     *
     * @since 2.0
     */
    public boolean isImmediate() {
        Boolean result = (Boolean) eval(IMMEDIATE, immediate);
        return result != null ? result : false;
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the immediate status of this behavior.
     * </p>
     *
     * @param immediate the flag to be set.
     *
     * @since 2.0
     */
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Tests whether the immediate attribute is specified. Returns true if the immediate attribute is specified, either as a
     * locally set property or as a value expression. This information allows an associated client behavior renderer to fall
     * back on the parent component's immediate status when immediate is not explicitly specified on the
     * <code>AjaxBehavior</code>.
     * </p>
     *
     * @return the flag whether the immediate attribute is specified.
     *
     * @since 2.0
     */
    public boolean isImmediateSet() {
        return immediate != null || getValueExpression(IMMEDIATE) != null;
    }

    /**
     * <p class="changed_added_2_2">
     * Tests whether the resetValues attribute is specified. Returns true if the resetValues attribute is specified, either
     * as a locally set property or as a value expression.
     * </p>
     *
     * @return the flag whether the resetValues attribute is specified.
     *
     * @since 2.2
     */
    public boolean isResetValuesSet() {
        return resetValues != null || getValueExpression(RESET_VALUES) != null;
    }

    /**
     * <p class="changed_added_2_0">
     * Returns the {@link ValueExpression} used to calculate the value for the specified property name, if any.
     * </p>
     *
     * @param name Name of the property for which to retrieve a {@link ValueExpression}
     *
     * @return the {@link ValueExpression}.
     *
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     */
    public ValueExpression getValueExpression(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        return bindings == null ? null : bindings.get(name);
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the {@link ValueExpression} used to calculate the value for the specified property name.
     * </p>
     *
     * @param name Name of the property for which to set a {@link ValueExpression}
     * @param binding The {@link ValueExpression} to set, or <code>null</code> to remove any currently set
     * {@link ValueExpression}
     *
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     */
    public void setValueExpression(String name, ValueExpression binding) {
        if (name == null) {
            throw new NullPointerException();
        }

        if (binding != null) {

            if (binding.isLiteralText()) {
                setLiteralValue(name, binding);
            } else {
                if (bindings == null) {

                    // We use a very small initial capacity on this HashMap.
                    // The goal is not to reduce collisions, but to keep the
                    // memory footprint small. It is very unlikely that an
                    // an AjaxBehavior would have more than 1 or 2 bound
                    // properties - and even if more are present, it's okay
                    // if we have some collisions - will still be fast.
                    bindings = new HashMap<>(6, 1.0f);
                }

                bindings.put(name, binding);
            }
        } else {
            if (bindings != null) {
                bindings.remove(name);
                if (bindings.isEmpty()) {
                    bindings = null;
                }
            }
        }

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Add the specified {@link AjaxBehaviorListener} to the set of listeners registered to receive event notifications from
     * this {@link AjaxBehavior}.
     * </p>
     *
     * @param listener The {@link AjaxBehaviorListener} to be registered
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     *
     * @since 2.0
     */
    public void addAjaxBehaviorListener(AjaxBehaviorListener listener) {
        addBehaviorListener(listener);
    }

    /**
     * <p class="changed_added_2_0">
     * Remove the specified {@link AjaxBehaviorListener} from the set of listeners registered to receive event notifications
     * from this {@link AjaxBehavior}.
     * </p>
     *
     * @param listener The {@link AjaxBehaviorListener} to be removed
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     *
     * @since 2.0
     */
    public void removeAjaxBehaviorListener(AjaxBehaviorListener listener) {
        removeBehaviorListener(listener);
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        
        Object[] values;

        Object superState = super.saveState(context);

        if (initialStateMarked()) {
            if (superState == null) {
                values = null;
            } else {
                values = new Object[] { superState };
            }
        } else {
            values = new Object[10];

            values[0] = superState;
            values[1] = onerror;
            values[2] = onevent;
            values[3] = disabled;
            values[4] = immediate;
            values[5] = resetValues;
            values[6] = delay;
            values[7] = saveList(execute);
            values[8] = saveList(render);
            values[9] = saveBindings(context, bindings);
        }

        return values;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        
        if (state != null) {

            Object[] values = (Object[]) state;
            super.restoreState(context, values[0]);

            if (values.length != 1) {
                onerror = (String) values[1];
                onevent = (String) values[2];
                disabled = (Boolean) values[3];
                immediate = (Boolean) values[4];
                resetValues = (Boolean) values[5];
                delay = (String) values[6];
                execute = restoreList(EXECUTE, values[7]);
                render = restoreList(RENDER, values[8]);
                bindings = restoreBindings(context, values[9]);

                // If we saved state last time, save state again next time.
                clearInitialState();
            }
        }
    }

    // --------------------------------------------------------- Private Methods

    // Utility for saving bindings state
    private static Object saveBindings(FacesContext context, Map<String, ValueExpression> bindings) {

        // Note: This code is copied from UIComponentBase. In a future
        // version of the Jakarta Faces Specification, it would be useful to define a
        // attribute/property/bindings/state helper object that can be
        // shared across components/behaviors/validaters/converters.

        if (bindings == null) {
            return null;
        }

        Object[] values = new Object[2];
        values[0] = bindings.keySet().toArray(new String[bindings.size()]);

        Object[] bindingValues = bindings.values().toArray();
        for (int i = 0; i < bindingValues.length; i++) {
            bindingValues[i] = UIComponentBase.saveAttachedState(context, bindingValues[i]);
        }

        values[1] = bindingValues;

        return values;
    }

    // Utility for restoring bindings from state
    private static Map<String, ValueExpression> restoreBindings(FacesContext context, Object state) {

        // Note: This code is copied from UIComponentBase. See note above
        // in saveBindings().

        if (state == null) {
            return null;
        }
        Object[] values = (Object[]) state;
        String[] names = (String[]) values[0];
        Object[] states = (Object[]) values[1];
        Map<String, ValueExpression> bindings = new HashMap<>(names.length);
        for (int i = 0; i < names.length; i++) {
            bindings.put(names[i], (ValueExpression) UIComponentBase.restoreAttachedState(context, states[i]));
        }
        return bindings;
    }

    // Save the List<String>, either as a String (single element) or as
    // a String[] (multiple elements.
    private static Object saveList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        int size = list.size();

        if (size == 1) {
            return list.get(0);
        }

        return list.toArray(new String[size]);
    }

    // Restore the list from a String (single element) or a String[]
    // (multiple elements)
    private static List<String> restoreList(String propertyName, Object state) {
        if (state == null) {
            return null;
        }

        List<String> list = null;

        if (state instanceof String) {
            list = toSingletonList(propertyName, (String) state);
        } else if (state instanceof String[]) {
            list = List.of((String[]) state);
        }

        return list;
    }

    private Object eval(String propertyName, Object value) {
        if (value != null) {
            return value;
        }

        ValueExpression expression = getValueExpression(propertyName);

        if (expression != null) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            return expression.getValue(ctx.getELContext());
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getCollectionValue(String propertyName, Collection<String> collection) {
        if (collection != null) {
            return collection;
        }

        Collection<String> result = null;
        ValueExpression expression = getValueExpression(propertyName);

        if (expression != null) {

            FacesContext ctx = FacesContext.getCurrentInstance();
            Object value = expression.getValue(ctx.getELContext());

            if (value != null) {

                if (value instanceof Collection) {
                    // Unchecked cast to Collection<String>
                    return (Collection<String>) value;
                }

                result = toList(propertyName, expression, value);
            }
        }

        return result == null ? Collections.<String>emptyList() : result;
    }

    // Sets a property, converting it from a literal
    private void setLiteralValue(String propertyName, ValueExpression expression) {
        assert expression.isLiteralText();

        Object value;
        ELContext context = FacesContext.getCurrentInstance().getELContext();

        try {
            value = expression.getValue(context);
        } catch (ELException ele) {
            throw new FacesException(ele);
        }

        if (null != propertyName) {
            switch (propertyName) {
            case ONEVENT:
                onevent = (String) value;
                break;
            case DELAY:
                delay = (String) value;
                break;
            case ONERROR:
                onerror = (String) value;
                break;
            case IMMEDIATE:
                immediate = (Boolean) value;
                break;
            case RESET_VALUES:
                resetValues = (Boolean) value;
                break;
            case DISABLED:
                disabled = (Boolean) value;
                break;
            case EXECUTE:
                execute = toList(propertyName, expression, value);
                break;
            case RENDER:
                render = toList(propertyName, expression, value);
                break;
            }
        }
    }

    // Converts the specified object to a List<String>
    private static List<String> toList(String propertyName, ValueExpression expression, Object value) {
        if (value instanceof String) {

            String strValue = (String) value;

            // If the value contains no spaces, we can optimize.
            // This is worthwhile, since the execute/render lists
            // will often only contain a single value.
            if (strValue.indexOf(' ') == -1) {
                return toSingletonList(propertyName, strValue);
            }

            // We're stuck splitting up the string.
            String[] values = SPLIT_PATTERN.split(strValue);
            if (values == null || values.length == 0) {
                return null;
            }

            // Note that we could create a Set out of the values if
            // we care about removing duplicates. However, the
            // presence of duplicates does not real harm. They will
            // be consolidated during the partial view traversal. So,
            // just create a list - garbage in, garbage out.
            return List.of(values);
        }

        // RELEASE_PENDING i18n ;
        throw new FacesException(expression.toString() + " : '" + propertyName + "' attribute value must be either a String or a Collection");
    }

    // Converts a String with no spaces to a singleton list
    private static List<String> toSingletonList(String propertyName, String value) {
        if (null == value || value.length() == 0) {
            return null;
        }

        if (value.charAt(0) == '@') {
            // These are very common, so we use shared copies
            // of these collections instead of re-creating.
            switch (value) {
                case ALL:  return ALL_LIST;
                case FORM: return FORM_LIST;
                case THIS: return THIS_LIST;
                case NONE: return NONE_LIST;
            }
        }

        return List.of(value);
    }

    // Makes a defensive copy of the collection, converting to a List
    // (to make state saving a bit easier).
    private static List<String> copyToList(Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }

        return List.copyOf(collection);
    }

    // Property name constants
    private static final String ONEVENT = "onevent";
    private static final String ONERROR = "onerror";
    private static final String IMMEDIATE = "immediate";
    private static final String RESET_VALUES = "resetValues";
    private static final String DISABLED = "disabled";
    private static final String EXECUTE = "execute";
    private static final String RENDER = "render";
    private static final String DELAY = "delay";

    // Id keyword constants
    private static final String ALL = "@all";
    private static final String FORM = "@form";
    private static final String THIS = "@this";
    private static final String NONE = "@none";

    // Shared execute/render collections
    private static final List<String> ALL_LIST = List.of(ALL);
    private static final List<String> FORM_LIST = List.of(FORM);
    private static final List<String> THIS_LIST = List.of(THIS);
    private static final List<String> NONE_LIST = List.of(NONE);

    // Pattern used for execute/render string splitting
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" ");

}
