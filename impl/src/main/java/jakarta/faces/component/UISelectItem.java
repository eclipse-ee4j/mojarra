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

import jakarta.faces.model.SelectItem;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_0_rev_a">UISelectItem</strong> is a component that may be
 * nested inside a {@link UISelectMany} or {@link UISelectOne} component, and causes the addition of a
 * {@link SelectItem} instance to the list of available options for the parent component. The contents of the
 * {@link SelectItem} can be specified in one of the following ways:
 * </p>
 * <ul>
 * <li>The <code>value</code> attribute's value is an instance of {@link SelectItem}.</li>
 * <li>The associated {@link jakarta.el.ValueExpression} points at a model data item of type {@link SelectItem}.</li>
 * <li>A new {@link SelectItem} instance is synthesized from the values of the <code>itemDescription</code>,
 * <code>itemDisabled</code>, <code>itemLabel</code>, <code
 * class="changed_modified_2_0_rev_a">itemEscaped</code>, and <code>itemValue</code> attributes.</li>
 * </ul>
 */

public class UISelectItem extends UIComponentBase {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectItem";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.SelectItem";

    enum PropertyKeys {
        itemDescription, itemDisabled, itemEscaped, itemLabel, itemValue, value, noSelectionOption
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UISelectItem} instance with default property values.
     * </p>
     */
    public UISelectItem() {
        super();
        setRendererType(null);
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Return the description for this selection item.
     * </p>
     *
     * @return the item description.
     */
    public String getItemDescription() {
        return (String) getStateHelper().eval(PropertyKeys.itemDescription);
    }

    /**
     * <p>
     * Set the description for this selection item.
     * </p>
     *
     * @param itemDescription The new description
     */
    public void setItemDescription(String itemDescription) {
        getStateHelper().put(PropertyKeys.itemDescription, itemDescription);
    }

    /**
     * <p>
     * Return the disabled setting for this selection item.
     * </p>
     *
     * @return <code>true</code> is the item disabled, <code>false</code> otherwise.
     */
    public boolean isItemDisabled() {
        return (Boolean) getStateHelper().eval(PropertyKeys.itemDisabled, false);
    }

    /**
     * <p>
     * Set the disabled value for this selection item.
     * </p>
     *
     * @param itemDisabled The new disabled flag
     */
    public void setItemDisabled(boolean itemDisabled) {
        getStateHelper().put(PropertyKeys.itemDisabled, itemDisabled);
    }

    /**
     * <p>
     * Return the escape setting for the label of this selection item.
     * </p>
     *
     * @return <code>true</code> if the item is escaped, <code>false</code> otherwise.
     */
    public boolean isItemEscaped() {
        return (Boolean) getStateHelper().eval(PropertyKeys.itemEscaped, true);
    }

    /**
     * <p>
     * Set the escape value for the label of this selection item.
     * </p>
     *
     * @param itemEscaped The new disabled flag
     */
    public void setItemEscaped(boolean itemEscaped) {
        getStateHelper().put(PropertyKeys.itemEscaped, itemEscaped);
    }

    /**
     * <p>
     * Return the localized label for this selection item.
     * </p>
     *
     * @return the item label.
     */
    public String getItemLabel() {
        return (String) getStateHelper().eval(PropertyKeys.itemLabel);
    }

    /**
     * <p>
     * Set the localized label for this selection item.
     * </p>
     *
     * @param itemLabel The new localized label
     */
    public void setItemLabel(String itemLabel) {
        getStateHelper().put(PropertyKeys.itemLabel, itemLabel);
    }

    /**
     * <p>
     * Return the server value for this selection item.
     * </p>
     *
     * @return the item value.
     */
    public Object getItemValue() {
        return getStateHelper().eval(PropertyKeys.itemValue);
    }

    /**
     * <p>
     * Set the server value for this selection item.
     * </p>
     *
     * @param itemValue The new server value
     */
    public void setItemValue(Object itemValue) {
        getStateHelper().put(PropertyKeys.itemValue, itemValue);
    }

    /**
     * <p>
     * Returns the <code>value</code> property of the <code>UISelectItem</code>.
     * </p>
     *
     * @return the value.
     */
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    /**
     * <p>
     * Sets the <code>value</code> property of the <code>UISelectItem</code>.
     * </p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    /**
     * <p class="changed_added_2_0">
     * Return the value of the <code>noSelectionOption</code> property. If the value of this property is <code>true</code>,
     * the system interprets the option represented by this <code>UISelectItem</code> instance as representing a "no
     * selection" option. See {@link UISelectOne#validateValue} and {@link UISelectMany#validateValue} for usage.
     * </p>
     *
     * @return the no selection option.
     * @since 2.0
     */
    public boolean isNoSelectionOption() {
        return (Boolean) getStateHelper().eval(PropertyKeys.noSelectionOption, false);
    }

    /**
     * <p class="changed_added_2_0">
     * Set the value of the <code>noSelectionOption</code> property.
     * </p>
     *
     * @param noSelectionOption the no selection option.
     * @since 2.0
     */
    public void setNoSelectionOption(boolean noSelectionOption) {
        getStateHelper().put(PropertyKeys.noSelectionOption, noSelectionOption);
    }

}
