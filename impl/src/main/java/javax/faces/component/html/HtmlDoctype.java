/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
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
package javax.faces.component.html;

import java.util.ArrayList;
import java.util.List;
import javax.el.ValueExpression;
import javax.faces.component.UIOutput;

/**
 * NONE
 * <p>By default, the <code>rendererType</code> property must be set to "<code>javax.faces.Doctype</code>".
 * This value can be changed by calling the <code>setRendererType()</code> method.</p>
 */
public class HtmlDoctype extends UIOutput {



    private static final String OPTIMIZED_PACKAGE = "javax.faces.component.";

    public HtmlDoctype() {
        super();
        setRendererType("javax.faces.Doctype");
    }


    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "javax.faces.OutputDoctype";


    protected enum PropertyKeys {
        publicVal("public"),
        rootElement,
        system,
;
        String toString;
        PropertyKeys(String toString) { this.toString = toString; }
        PropertyKeys() { }
        public String toString() {
            return ((toString != null) ? toString : super.toString());
        }
}

    /**
     * <p>Return the value of the <code>public</code> property.</p>
     * @return the property value
     * <p>Contents: <div class="changed_added_2_1">
     * Will be output as the public part of the DOCTYPE
     * </div>
     */
    public java.lang.String getPublic() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.publicVal);

    }

    /**
     * <p>Set the value of the <code>public</code> property.</p>
     * @param _public the new property value
     */
    public void setPublic(java.lang.String _public) {
        getStateHelper().put(PropertyKeys.publicVal, _public);
        handleAttribute("public", _public);
    }


    /**
     * <p>Return the value of the <code>rootElement</code> property.</p>
     * @return the property value
     * <p>Contents: <div class="changed_added_2_1">
     * The root XML element
     * </div>
     */
    public java.lang.String getRootElement() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rootElement);

    }

    /**
     * <p>Set the value of the <code>rootElement</code> property.</p>
     * @param rootElement the new property value
     */
    public void setRootElement(java.lang.String rootElement) {
        getStateHelper().put(PropertyKeys.rootElement, rootElement);
        handleAttribute("rootElement", rootElement);
    }


    /**
     * <p>Return the value of the <code>system</code> property.</p>
     * @return the property value
     * <p>Contents: <div class="changed_added_2_1">
     * Will be output as the system part of the DOCTYPE
     * </div>
     */
    public java.lang.String getSystem() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.system);

    }

    /**
     * <p>Set the value of the <code>system</code> property.</p>
     * @param system the new property value
     */
    public void setSystem(java.lang.String system) {
        getStateHelper().put(PropertyKeys.system, system);
        handleAttribute("system", system);
    }


    private void handleAttribute(String name, Object value) {
        List<String> setAttributes = (List<String>) this.getAttributes().get("javax.faces.component.UIComponentBase.attributesThatAreSet");
        if (setAttributes == null) {
            String cname = this.getClass().getName();
            if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
                setAttributes = new ArrayList<String>(6);
                this.getAttributes().put("javax.faces.component.UIComponentBase.attributesThatAreSet", setAttributes);
            }
        }
        if (setAttributes != null) {
            if (value == null) {
                ValueExpression ve = getValueExpression(name);
                if (ve == null) {
                    setAttributes.remove(name);
                }
            } else if (!setAttributes.contains(name)) {
                setAttributes.add(name);
            }
        }
    }

}
