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

import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.Validator;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_0_rev_a">EditableValueHolder</strong> is an extension of
 * ValueHolder that describes additional features supported by editable components, including {@link ValueChangeEvent}s
 * and {@link Validator}s.
 */

public interface EditableValueHolder extends ValueHolder {

    /**
     * <p>
     * Return the submittedValue value of this component. This method should only be used by the <code>encodeBegin()</code>
     * and/or <code>encodeEnd()</code> methods of this component, or its corresponding {@link Renderer}.
     * <span class="changed_modified_2_0_rev_a">The action taken based on whether the value is <code>null</code>, empty, or
     * non-<code>null</code> is determined based on the value of the
     * <code>jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</code>context-param.</span>
     * </p>
     *
     * @return the submitted value.
     */
    Object getSubmittedValue();

    /**
     * <p class="changed_added_2_0">
     * Convenience method to reset this component's value to the un-initialized state.
     * </p>
     *
     * @since 2.0
     */
    void resetValue();

    /**
     * <p>
     * Set the submittedValue value of this component. This method should only be used by the <code>decode()</code> and
     * <code>validate()</code> method of this component, or its corresponding {@link Renderer}.
     * <span class="changed_modified_2_0_rev_a">The action taken based on whether the value is <code>null</code>, empty, or
     * non-<code>null</code> is determined based on the value of the
     * <code>jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</code>context-param.</span>
     * </p>
     *
     * @param submittedValue The new submitted value
     */
    void setSubmittedValue(Object submittedValue);

    /**
     * Return the "local value set" state for this component. Calls to <code>setValue()</code> automatically reset this
     * property to <code>true</code>.
     *
     * @return <code>true</code> if the local value is set, <code>false</code> otherwise.
     */
    boolean isLocalValueSet();

    /**
     * Sets the "local value set" state for this component.
     *
     * @param localValueSet the "local value set" boolean.
     */
    void setLocalValueSet(boolean localValueSet);

    /**
     * <p>
     * Return a flag indicating whether the local value of this component is valid (no conversion error has occurred).
     * </p>
     *
     * @return <code>true</code> if valid, <code>false</code> otherwise.
     */
    boolean isValid();

    /**
     * <p>
     * Set a flag indicating whether the local value of this component is valid (no conversion error has occurred).
     * </p>
     *
     * @param valid The new valid flag
     */
    void setValid(boolean valid);

    /**
     * <p>
     * Return the "required field" state for this component.
     * </p>
     *
     * @return <code>true</code> if required, <code>false</code> otherwise.
     */
    boolean isRequired();

    /**
     * <p>
     * Set the "required field" state for this component.
     * </p>
     *
     * @param required The new "required field" state
     */
    void setRequired(boolean required);

    /**
     * <p>
     * Return the "immediate" state for this component.
     * </p>
     *
     * @return <code>true</code> if is immediate, <code>false</code> otherwise.
     */
    boolean isImmediate();

    /**
     * <p>
     * Set the "immediate" state for this component. When set to true, the component's value will be converted and validated
     * immediately in the <em>Apply Request Values</em> phase, and {@link ValueChangeEvent}s will be delivered in that phase
     * as well. The default value for this property must be <code>false</code>.
     * </p>
     *
     * @param immediate The new "immediate" state
     */
    void setImmediate(boolean immediate);

    /**
     * <p>
     * Add a {@link Validator} instance to the set associated with this component.
     * </p>
     *
     * @param validator The {@link Validator} to add
     *
     * @throws NullPointerException if <code>validator</code> is null
     */
    void addValidator(Validator validator);

    /**
     * <p>
     * Return the set of registered {@link Validator}s for this component instance. If there are no registered validators, a
     * zero-length array is returned.
     * </p>
     *
     * @return the validators, or a zero-length array.
     */
    Validator[] getValidators();

    /**
     * <p>
     * Remove a {@link Validator} instance from the set associated with this component, if it was previously associated.
     * Otherwise, do nothing.
     * </p>
     *
     * @param validator The {@link Validator} to remove
     */
    void removeValidator(Validator validator);

    /**
     * <p>
     * Add a new {@link ValueChangeListener} to the set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.
     * </p>
     *
     * @param listener The {@link ValueChangeListener} to be added
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    void addValueChangeListener(ValueChangeListener listener);

    /**
     * <p>
     * Return the set of registered {@link ValueChangeListener}s for this component instance. If there are no registered
     * listeners, a zero-length array is returned.
     * </p>
     *
     * @return the value change listeners, or a zero-length array.
     */
    ValueChangeListener[] getValueChangeListeners();

    /**
     * <p>
     * Remove an existing {@link ValueChangeListener} (if any) from the set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.
     * </p>
     *
     * @param listener The {@link ValueChangeListener} to be removed
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    void removeValueChangeListener(ValueChangeListener listener);

}
