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

// ParameterTag.java

package com.sun.faces.taglib.jsf_core;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.webapp.UIComponentELTag;

public class ParameterTag extends UIComponentELTag {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//
    private ValueExpression name;
    private ValueExpression value;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers
//

    public ParameterTag() {
        super();
    }

//
// Class methods
//

//
// Accessors
//
    public void setName(ValueExpression name) {
        this.name = name;
    }

    public void setValue(ValueExpression value) {
        this.value = value;
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
        return "jakarta.faces.Parameter";
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        UIParameter parameter = (UIParameter) component;

        if (name != null) {
            if (!name.isLiteralText()) {
                parameter.setValueExpression("name", name);
            } else {
                parameter.setName(name.getExpressionString());
            }
        }
        // if component has non null value, do not call setValue().
        if (value != null) {
            if (!value.isLiteralText()) {
                component.setValueExpression("value", value);
            } else {
                parameter.setValue(value.getExpressionString());
            }
        }
    }
}
