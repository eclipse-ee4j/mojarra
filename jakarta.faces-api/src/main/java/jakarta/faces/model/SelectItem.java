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

package jakarta.faces.model;

import java.io.Serializable;

import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_0_rev_a">SelectItem</strong> represents a single <em>item</em>
 * in the list of supported <em>items</em> associated with a {@link UISelectMany} or {@link UISelectOne} component.
 * </p>
 */

public class SelectItem implements Serializable {

    private static final long serialVersionUID = 876782311414654999L;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a <code>SelectItem</code> with no initialized property values.
     * </p>
     */
    public SelectItem() {

        super();

    }

    /**
     * <p>
     * Construct a <code>SelectItem</code> with the specified value. The <code>label</code> property will be set to the
     * value (converted to a String, if necessary), the <code>description</code> property will be set to <code>null</code>,
     * the <code>disabled</code> property will be set to <code>false</code>, and the <code>escape</code> property will be
     * set to ( <code>true</code>.
     * </p>
     *
     * @param value Value to be delivered to the model if this item is selected by the user
     */
    public SelectItem(Object value) {

        this(value, value == null ? null : value.toString(), null, false, true, false);

    }

    /**
     * <p>
     * Construct a <code>SelectItem</code> with the specified value and label. The <code>description</code> property will be
     * set to <code>null</code>, the <code>disabled</code> property will be set to <code>false</code>, and the
     * <code>escape</code> property will be set to <code>true</code>.
     * </p>
     *
     * @param value Value to be delivered to the model if this item is selected by the user
     * @param label Label to be rendered for this item in the response
     */
    public SelectItem(Object value, String label) {

        this(value, label, null, false, true, false);

    }

    /**
     * <p>
     * Construct a <code>SelectItem</code> instance with the specified value, label and description. This
     * <code>disabled</code> property will be set to <code>false</code>, and the <code>escape</code> property will be set to
     * <code>true</code>.
     * </p>
     *
     * @param value Value to be delivered to the model if this item is selected by the user
     * @param label Label to be rendered for this item in the response
     * @param description Description of this item, for use in tools
     */
    public SelectItem(Object value, String label, String description) {

        this(value, label, description, false, true, false);

    }

    /**
     * <p>
     * Construct a <code>SelectItem</code> instance with the specified property values. The <code>escape</code> property
     * will be set to <code>true</code>.
     * </p>
     *
     * @param value Value to be delivered to the model if this item is selected by the user
     * @param label Label to be rendered for this item in the response
     * @param description Description of this item, for use in tools
     * @param disabled Flag indicating that this option is disabled
     */
    public SelectItem(Object value, String label, String description, boolean disabled) {

        this(value, label, description, disabled, true, false);

    }

    /**
     * <p>
     * Construct a <code>SelectItem</code> instance with the specified property values.
     * </p>
     *
     * @param value Value to be delivered to the model if this item is selected by the user
     * @param label Label to be rendered for this item in the response
     * @param description Description of this item, for use in tools
     * @param disabled Flag indicating that this option is disabled
     * @param escape Flag indicating that the text of this option should be escaped when rendered.
     * @since 1.2
     */
    public SelectItem(Object value, String label, String description, boolean disabled, boolean escape) {

        this(value, label, description, disabled, escape, false);

    }

    /**
     * <p>
     * Construct a <code>SelectItem</code> instance with the specified property values.
     * </p>
     *
     * @param value Value to be delivered to the model if this item is selected by the user
     * @param label Label to be rendered for this item in the response
     * @param description Description of this item, for use in tools
     * @param disabled Flag indicating that this option is disabled
     * @param escape Flag indicating that the text of this option should be escaped when rendered.
     * @param noSelectionOption Flag indicating that the current option is a "no selection" option
     * @since 1.2
     */
    public SelectItem(Object value, String label, String description, boolean disabled, boolean escape, boolean noSelectionOption) {

        super();
        setValue(value);
        setLabel(label);
        setDescription(description);
        setDisabled(disabled);
        setEscape(escape);
        setNoSelectionOption(noSelectionOption);

    }

    // ------------------------------------------------------ Instance Variables

    private String description = null;
    private boolean disabled = false;
    private String label = null;
    @SuppressWarnings({ "NonSerializableFieldInSerializableClass" })
    private Object value = null;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return a description of this item, for use in development tools.
     *
     * @return a description of this item, for use in development tools
     */
    public String getDescription() {

        return description;

    }

    /**
     * <p>
     * Set the description of this item, for use in development tools.
     * </p>
     *
     * @param description The new description
     */
    public void setDescription(String description) {

        this.description = description;

    }

    /**
     * <p>
     * Return the disabled flag for this item, which should modify the rendered output to make this item unavailable for
     * selection by the user if set to <code>true</code>.
     * </p>
     *
     * @return the disabled flag for this item
     */
    public boolean isDisabled() {

        return disabled;

    }

    /**
     * <p>
     * Set the disabled flag for this item, which should modify the rendered output to make this item unavailable for
     * selection by the user if set to <code>true</code>.
     * </p>
     *
     * @param disabled The new disabled flag
     */
    public void setDisabled(boolean disabled) {

        this.disabled = disabled;

    }

    /**
     * <p>
     * Return the label of this item, to be rendered visibly for the user.
     *
     * @return the label of this item
     */
    public String getLabel() {

        return label;

    }

    /**
     * <p>
     * Set the label of this item, to be rendered visibly for the user.
     *
     * @param label The new label
     */
    public void setLabel(String label) {

        this.label = label;

    }

    /**
     * <p>
     * Return the value of this item, to be delivered to the model if this item is selected by the user.
     *
     * @return the value of this item
     */
    public Object getValue() {

        return value;

    }

    /**
     * <p>
     * Set the value of this item, to be delivered to the model if this item is selected by this user.
     *
     * @param value The new value
     *
     */
    public void setValue(Object value) {

        this.value = value;

    }

    private boolean escape;

    /**
     * <p class="changed_added_2_0_rev_a">
     * If and only if this returns <code>true</code>, the code that renders this select item must escape the label using
     * escaping syntax appropriate to the content type being rendered.
     * </p>
     *
     * @return the escape value.
     * @since 2.0
     */
    public boolean isEscape() {
        return escape;
    }

    /**
     * <p class="changed_added_2_0_rev_a">
     * Set the value of the escape property. See {@link #isEscape}.
     * </p>
     *
     * @param escape the new value of the escape property
     *
     * @since 2.0
     */
    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    private boolean noSelectionOption = false;

    /**
     * <p class="changed_added_2_0">
     * Return the value of the <code>noSelectionOption</code> property. If the value of this property is <code>true</code>,
     * the system interprets the option represented by this <code>SelectItem</code> instance as representing a "no
     * selection" option. See {@link UISelectOne#validateValue} and {@link UISelectMany#validateValue} for usage.
     * </p>
     *
     * @return the value of the <code>noSelectionOption</code> property
     *
     * @since 2.0
     */

    public boolean isNoSelectionOption() {
        return noSelectionOption;
    }

    /**
     * <p class="changed_added_2_0">
     * Set the value of the <code>noSelectionOption</code> property.
     * </p>
     *
     * @param noSelectionOption the new value of the {@code noSelectionOption} property
     *
     * @since 2.0
     */

    public void setNoSelectionOption(boolean noSelectionOption) {
        this.noSelectionOption = noSelectionOption;
    }

}
