/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.taglib.jsf_core;

import jakarta.el.ValueExpression;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.webapp.UIComponentELTag;

/**
 * This class is the tag handler that evaluates the <code>selectitem</code> custom tag.
 */

public class SelectItemTag extends UIComponentELTag {

    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //

    // Attribute Instance Variables

    protected ValueExpression itemValue;
    protected ValueExpression itemLabel;
    protected ValueExpression itemDescription;
    protected ValueExpression itemDisabled;
    protected ValueExpression noSelectionOption = null;

    protected ValueExpression value;

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //

    public SelectItemTag() {
        super();
    }

    //
    // Class methods
    //

    //
    // Accessors
    //

    public void setItemValue(ValueExpression value) {
        this.itemValue = value;
    }

    public void setItemLabel(ValueExpression label) {
        this.itemLabel = label;
    }

    public void setItemDescription(ValueExpression itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setItemDisabled(ValueExpression itemDisabled) {
        this.itemDisabled = itemDisabled;
    }

    public void setValue(ValueExpression value) {
        this.value = value;
    }

    public void setNoSelectionOption(ValueExpression noSelectionOption) {
        this.noSelectionOption = noSelectionOption;
    }

    //
    // General Methods
    //
    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    public String getComponentType() {
        return "jakarta.faces.SelectItem";
    }

    //
    // Methods from BaseComponentTag
    //

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        UISelectItem selectItem = (UISelectItem) component;

        if (null != value) {
            if (!value.isLiteralText()) {
                selectItem.setValueExpression("value", value);
            } else {
                selectItem.setValue(value.getExpressionString());
            }
        }

        if (null != itemValue) {
            if (!itemValue.isLiteralText()) {
                selectItem.setValueExpression("itemValue", itemValue);
            } else {
                selectItem.setItemValue(itemValue.getExpressionString());
            }
        }
        if (null != itemLabel) {
            if (!itemLabel.isLiteralText()) {
                selectItem.setValueExpression("itemLabel", itemLabel);
            } else {
                selectItem.setItemLabel(itemLabel.getExpressionString());
            }
        }
        if (null != itemDescription) {
            if (!itemDescription.isLiteralText()) {
                selectItem.setValueExpression("itemDescription", itemDescription);
            } else {
                selectItem.setItemDescription(itemDescription.getExpressionString());
            }
        }
        if (null != itemDisabled) {
            if (!itemDisabled.isLiteralText()) {
                selectItem.setValueExpression("itemDisabled", itemDisabled);
            } else {
                selectItem.setItemDisabled(Boolean.valueOf(itemDisabled.getExpressionString()).booleanValue());
            }
        }
        if (null != noSelectionOption) {
            if (!noSelectionOption.isLiteralText()) {
                selectItem.setValueExpression("noSelectionOption", noSelectionOption);
            } else {
                selectItem.setNoSelectionOption(Boolean.valueOf(noSelectionOption.getExpressionString()).booleanValue());
            }
        }
        if (null != escape) {
            if (!escape.isLiteralText()) {
                selectItem.setValueExpression("escape", escape);
            } else {
                selectItem.setItemEscaped(Boolean.valueOf(escape.getExpressionString()).booleanValue());
            }
        }

    }

    /**
     * Holds value of property escape.
     */
    private ValueExpression escape;

    /**
     * Getter for property escape.
     * 
     * @return Value of property escape.
     */
    public ValueExpression getEscape() {
        return this.escape;
    }

    /**
     * Setter for property escape.
     * 
     * @param escape New value of property escape.
     */
    public void setEscape(ValueExpression escape) {
        this.escape = escape;
    }

} // end of class SelectItemTag
