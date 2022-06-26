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

import static jakarta.faces.component.html.HtmlComponentUtils.handleAttribute;

import jakarta.faces.component.UIOutput;

/**
 * NONE
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Head</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlHead extends UIOutput {

    public HtmlHead() {
        super();
        setRendererType("jakarta.faces.Head");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.OutputHead";

    /**
     * The property keys.
     */
    protected enum PropertyKeys {
        dir, lang, xmlns,;

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

    /**
     * <p>
     * Return the value of the <code>dir</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Direction indication for text that does not inherit directionality. Valid values are "LTR" (left-to-right)
     * and "RTL" (right-to-left). These attributes are case sensitive when rendering to XHTML, so care must be taken to have
     * the correct case.
     */
    public java.lang.String getDir() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.dir);

    }

    /**
     * <p>
     * Set the value of the <code>dir</code> property.
     * </p>
     *
     * @param dir the new property value
     */
    public void setDir(java.lang.String dir) {
        getStateHelper().put(PropertyKeys.dir, dir);
        handleAttribute(this, "dir", dir);
    }

    /**
     * <p>
     * Return the value of the <code>lang</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Code describing the language used in the generated markup for this component.
     */
    public java.lang.String getLang() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.lang);

    }

    /**
     * <p>
     * Set the value of the <code>lang</code> property.
     * </p>
     *
     * @param lang the new property value
     */
    public void setLang(java.lang.String lang) {
        getStateHelper().put(PropertyKeys.lang, lang);
        handleAttribute(this, "lang", lang);
    }

    /**
     * <p>
     * Return the value of the <code>xmlns</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: <div class="changed_added_2_1"> An XML Namespace to be passed through to the rendered element </div>
     */
    public java.lang.String getXmlns() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.xmlns);

    }

    /**
     * <p>
     * Set the value of the <code>xmlns</code> property.
     * </p>
     *
     * @param xmlns the new property value
     */
    public void setXmlns(java.lang.String xmlns) {
        getStateHelper().put(PropertyKeys.xmlns, xmlns);
        handleAttribute(this, "xmlns", xmlns);
    }

}
