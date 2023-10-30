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
 * <p>
 * Renders the component value as text, optionally wrapping in a <code>span</code> element if I18N attributes, CSS
 * styles or style classes are specified.
 * </p>
 * <p class="changed_modified_2_3">
 * If you are using h:outputText (or an equivalent inline EL expression) within a script or style block the value is NOT
 * escaped by default.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Text</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlOutputText extends UIOutput {

    public HtmlOutputText() {
        super();
        setRendererType("jakarta.faces.Text");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlOutputText";

    /**
     * The property keys.
     */
    protected enum PropertyKeys {
        dir, escape, lang, role, style, styleClass, title,;

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
     * Return the value of the <code>escape</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Flag indicating that characters that are sensitive in HTML and XML markup must be escaped. This flag is set
     * to "true" by default.
     */
    public boolean isEscape() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.escape, true);

    }

    /**
     * <p>
     * Set the value of the <code>escape</code> property.
     * </p>
     *
     * @param escape the new property value
     */
    public void setEscape(boolean escape) {
        getStateHelper().put(PropertyKeys.escape, escape);
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
     * Return the value of the <code>role</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents:
     * <p class="changed_added_2_2">
     * Per the WAI-ARIA spec and its relationship to HTML5 (Section title ARIA Role Attriubute), every HTML element may have
     * a "role" attribute whose value must be passed through unmodified on the element on which it is declared in the final
     * rendered markup. The attribute, if specified, must have a value that is a string literal that is, or an EL Expression
     * that evaluates to, a set of space-separated tokens representing the various WAI-ARIA roles that the element belongs
     * to.
     * </p>
     *
     * <p class="changed_added_2_2">
     * It is the page author's responsibility to ensure that the user agent is capable of correctly interpreting the value
     * of this attribute.
     * </p>
     */
    public java.lang.String getRole() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.role);

    }

    /**
     * <p>
     * Set the value of the <code>role</code> property.
     * </p>
     *
     * @param role the new property value
     */
    public void setRole(java.lang.String role) {
        getStateHelper().put(PropertyKeys.role, role);
        handleAttribute(this, "role", role);
    }

    /**
     * <p>
     * Return the value of the <code>style</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: CSS style(s) to be applied when this component is rendered.
     */
    public java.lang.String getStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.style);

    }

    /**
     * <p>
     * Set the value of the <code>style</code> property.
     * </p>
     *
     * @param style the new property value
     */
    public void setStyle(java.lang.String style) {
        getStateHelper().put(PropertyKeys.style, style);
        handleAttribute(this, "style", style);
    }

    /**
     * <p>
     * Return the value of the <code>styleClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) to be applied when this element is rendered. This value must be
     * passed through as the "class" attribute on generated markup.
     */
    public java.lang.String getStyleClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.styleClass);

    }

    /**
     * <p>
     * Set the value of the <code>styleClass</code> property.
     * </p>
     *
     * @param styleClass the new property value
     */
    public void setStyleClass(java.lang.String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }

    /**
     * <p>
     * Return the value of the <code>title</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Advisory title information about markup elements generated for this component.
     */
    public java.lang.String getTitle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.title);

    }

    /**
     * <p>
     * Set the value of the <code>title</code> property.
     * </p>
     *
     * @param title the new property value
     */
    public void setTitle(java.lang.String title) {
        getStateHelper().put(PropertyKeys.title, title);
        handleAttribute(this, "title", title);
    }

}
