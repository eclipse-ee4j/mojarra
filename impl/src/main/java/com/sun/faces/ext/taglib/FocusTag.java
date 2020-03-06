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

package com.sun.faces.ext.taglib;

import jakarta.el.ValueExpression;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentELTag;


/**
 * Tag to set focus to a given field.  Uses a single attribute, for, which is
 * the id of the field which should receive the focus.
 * 
 * @author driscoll
 */
public class FocusTag extends UIComponentELTag {
    
    private static final String COMPONENT_TYPE = "com.sun.faces.ext.focus";
    private static final String RENDERER_TYPE = "com.sun.faces.ext.render.FocusHTMLRenderer";
    
    public ValueExpression forID = null;


    // Associate the component type.
    @Override
    public String getComponentType() {
        return COMPONENT_TYPE;
    }
    
    
    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        // set forID
        if (forID != null) {
            if (!forID.isLiteralText()) {
                component.setValueExpression("for", forID);
            } else {
                component.getAttributes().put("for", forID.getExpressionString());
            }
        }
    }


    // We'll render our own content
    @Override
    public String getRendererType() {
        return RENDERER_TYPE;
    }

    @Override
    public void release() {
        super.release();
        forID = null;
    }

    public void setFor(ValueExpression forID) {
        this.forID = forID;
    }
    
}
