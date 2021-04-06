/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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
package jakarta.faces.component.html;

import java.util.ArrayList;
import java.util.List;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIInput;

/**
 * <p>
 * Represents an HTML <code>input</code> element of type <code>hidden</code>.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Hidden</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlInputHidden extends UIInput {

    private static final String OPTIMIZED_PACKAGE = "jakarta.faces.component.";

    public HtmlInputHidden() {
        super();
        setRendererType("jakarta.faces.Hidden");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlInputHidden";

    /**
     * Properties used by this component
     *
     */
    protected enum PropertyKeys {
        ;
        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {
        }

        @Override
        public String toString() {
            return toString != null ? toString : super.toString();
        }
    }

    private void handleAttribute(String name, Object value) {
        List<String> setAttributes = (List<String>) getAttributes().get("jakarta.faces.component.UIComponentBase.attributesThatAreSet");
        if (setAttributes == null) {
            String cname = this.getClass().getName();
            if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {
                setAttributes = new ArrayList<>(6);
                getAttributes().put("jakarta.faces.component.UIComponentBase.attributesThatAreSet", setAttributes);
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
