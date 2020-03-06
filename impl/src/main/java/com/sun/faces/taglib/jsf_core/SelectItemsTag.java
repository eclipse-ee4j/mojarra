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
import jakarta.faces.component.UISelectItems;
import jakarta.faces.webapp.UIComponentELTag;

/**
 * This class is the tag handler that evaluates the <code>selectitems</code> custom tag.
 */

public class SelectItemsTag extends UIComponentELTag {

    private ValueExpression value;
    private String var;
    private ValueExpression itemValue;
    private ValueExpression itemLabel;
    private ValueExpression itemDescription;
    private ValueExpression itemDisabled;
    private ValueExpression itemLabelEscaped;
    private ValueExpression noSelectionOption;

    // ---------------------------------------------------------- Tag Attributes

    public void setValue(ValueExpression value) {
        this.value = value;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setItemValue(ValueExpression itemValue) {
        this.itemValue = itemValue;
    }

    public void setItemLabel(ValueExpression itemLabel) {
        this.itemLabel = itemLabel;
    }

    public void setItemDescription(ValueExpression itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setItemDisabled(ValueExpression itemDisabled) {
        this.itemDisabled = itemDisabled;
    }

    public void setItemLabelEscaped(ValueExpression itemLabelEscaped) {
        this.itemLabelEscaped = itemLabelEscaped;
    }

    public void setNoSelectionOption(ValueExpression noSelectionOption) {
        this.noSelectionOption = noSelectionOption;
    }

    // ----------------------------------------- Methods from UIComponentTagBase

    /**
     * @see jakarta.faces.webapp.UIComponentELTag#getRendererType()
     */
    @Override
    public String getRendererType() {
        return null;
    }

    /**
     * @see jakarta.faces.webapp.UIComponentELTag#getComponentType()
     * @see jakarta.faces.component.UISelectItems#COMPONENT_TYPE
     */
    @Override
    public String getComponentType() {
        return UISelectItems.COMPONENT_TYPE;
    }

    // ------------------------------------------- Methods from UIComponentELTag

    /**
     * @see jakarta.faces.webapp.UIComponentELTag#setProperties(jakarta.faces.component.UIComponent)
     * @param component
     */
    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (value != null) {
            component.setValueExpression("value", value);
        }
        if (var != null) {
            component.getAttributes().put("var", var);
        }
        if (itemValue != null) {
            component.setValueExpression("itemValue", itemValue);
        }
        if (itemLabel != null) {
            component.setValueExpression("itemLabel", itemLabel);
        }
        if (itemDescription != null) {
            component.setValueExpression("itemDescription", itemDescription);
        }
        if (itemDisabled != null) {
            component.setValueExpression("itemDisabled", itemDisabled);
        }
        if (itemLabelEscaped != null) {
            component.setValueExpression("itemLabelEscaped", itemLabelEscaped);
        }
        if (noSelectionOption != null) {
            component.setValueExpression("noSelectionOption", noSelectionOption);
        }

    }

} // end of class SelectItemsTag
