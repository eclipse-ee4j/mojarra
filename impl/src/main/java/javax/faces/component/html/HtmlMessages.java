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
import javax.faces.component.UIMessages;

/**
 * 
 * <p>By default, the <code>rendererType</code> property must be set to "<code>javax.faces.Messages</code>".
 * This value can be changed by calling the <code>setRendererType()</code> method.</p>
 */
public class HtmlMessages extends UIMessages {



    private static final String OPTIMIZED_PACKAGE = "javax.faces.component.";

    public HtmlMessages() {
        super();
        setRendererType("javax.faces.Messages");
    }


    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "javax.faces.HtmlMessages";


    protected enum PropertyKeys {
        dir,
        errorClass,
        errorStyle,
        fatalClass,
        fatalStyle,
        infoClass,
        infoStyle,
        lang,
        layout,
        role,
        style,
        styleClass,
        title,
        tooltip,
        warnClass,
        warnStyle,
;
        String toString;
        PropertyKeys(String toString) { this.toString = toString; }
        PropertyKeys() { }
        public String toString() {
            return ((toString != null) ? toString : super.toString());
        }
}

    /**
     * <p>Return the value of the <code>dir</code> property.</p>
     * @return the property value
     * <p>Contents: Direction indication for text that does not inherit directionality.
     * Valid values are "LTR" (left-to-right) and "RTL" (right-to-left).
     * These attributes are case sensitive when rendering to XHTML, so
     * care must be taken to have the correct case.
     */
    public java.lang.String getDir() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.dir);

    }

    /**
     * <p>Set the value of the <code>dir</code> property.</p>
     * @param dir the new property value
     */
    public void setDir(java.lang.String dir) {
        getStateHelper().put(PropertyKeys.dir, dir);
        handleAttribute("dir", dir);
    }


    /**
     * <p>Return the value of the <code>errorClass</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style class to apply to any message
     * with a severity class of "ERROR".
     */
    public java.lang.String getErrorClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.errorClass);

    }

    /**
     * <p>Set the value of the <code>errorClass</code> property.</p>
     * @param errorClass the new property value
     */
    public void setErrorClass(java.lang.String errorClass) {
        getStateHelper().put(PropertyKeys.errorClass, errorClass);
    }


    /**
     * <p>Return the value of the <code>errorStyle</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style(s) to apply to any message
     * with a severity class of "ERROR".
     */
    public java.lang.String getErrorStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.errorStyle);

    }

    /**
     * <p>Set the value of the <code>errorStyle</code> property.</p>
     * @param errorStyle the new property value
     */
    public void setErrorStyle(java.lang.String errorStyle) {
        getStateHelper().put(PropertyKeys.errorStyle, errorStyle);
    }


    /**
     * <p>Return the value of the <code>fatalClass</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style class to apply to any message
     * with a severity class of "FATAL".
     */
    public java.lang.String getFatalClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.fatalClass);

    }

    /**
     * <p>Set the value of the <code>fatalClass</code> property.</p>
     * @param fatalClass the new property value
     */
    public void setFatalClass(java.lang.String fatalClass) {
        getStateHelper().put(PropertyKeys.fatalClass, fatalClass);
    }


    /**
     * <p>Return the value of the <code>fatalStyle</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style(s) to apply to any message
     * with a severity class of "FATAL".
     */
    public java.lang.String getFatalStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.fatalStyle);

    }

    /**
     * <p>Set the value of the <code>fatalStyle</code> property.</p>
     * @param fatalStyle the new property value
     */
    public void setFatalStyle(java.lang.String fatalStyle) {
        getStateHelper().put(PropertyKeys.fatalStyle, fatalStyle);
    }


    /**
     * <p>Return the value of the <code>infoClass</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style class to apply to any message
     * with a severity class of "INFO".
     */
    public java.lang.String getInfoClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.infoClass);

    }

    /**
     * <p>Set the value of the <code>infoClass</code> property.</p>
     * @param infoClass the new property value
     */
    public void setInfoClass(java.lang.String infoClass) {
        getStateHelper().put(PropertyKeys.infoClass, infoClass);
    }


    /**
     * <p>Return the value of the <code>infoStyle</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style(s) to apply to any message
     * with a severity class of "INFO".
     */
    public java.lang.String getInfoStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.infoStyle);

    }

    /**
     * <p>Set the value of the <code>infoStyle</code> property.</p>
     * @param infoStyle the new property value
     */
    public void setInfoStyle(java.lang.String infoStyle) {
        getStateHelper().put(PropertyKeys.infoStyle, infoStyle);
    }


    /**
     * <p>Return the value of the <code>lang</code> property.</p>
     * @return the property value
     * <p>Contents: Code describing the language used in the generated markup
     * for this component.
     */
    public java.lang.String getLang() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.lang);

    }

    /**
     * <p>Set the value of the <code>lang</code> property.</p>
     * @param lang the new property value
     */
    public void setLang(java.lang.String lang) {
        getStateHelper().put(PropertyKeys.lang, lang);
        handleAttribute("lang", lang);
    }


    /**
     * <p>Return the value of the <code>layout</code> property.</p>
     * @return the property value
     * <p>Contents: The type of layout markup to use when rendering
     * error messages.  Valid values are "table" (an HTML
     * table) and "list" (an HTML list).  If not specified,
     * the default value is "list".
     */
    public java.lang.String getLayout() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.layout, "list");

    }

    /**
     * <p>Set the value of the <code>layout</code> property.</p>
     * @param layout the new property value
     */
    public void setLayout(java.lang.String layout) {
        getStateHelper().put(PropertyKeys.layout, layout);
    }


    /**
     * <p>Return the value of the <code>role</code> property.</p>
     * @return the property value
     * <p>Contents: <p class="changed_added_2_2">Per the WAI-ARIA spec and its
     * relationship to HTML5 (Section title ARIA Role Attriubute),
     * every HTML element may have a "role" attribute whose value
     * must be passed through unmodified on the element on which it
     * is declared in the final rendered markup.  The attribute, if
     * specified, must have a value that is a string literal that is,
     * or an EL Expression that evaluates to, a set of
     * space-separated tokens representing the various WAI-ARIA roles
     * that the element belongs to.
     * </p>
     * 
     * <p class="changed_added_2_2">It is the page author's
     * responsibility to ensure that the user agent is capable of
     * correctly interpreting the value of this attribute.</p>
     */
    public java.lang.String getRole() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.role);

    }

    /**
     * <p>Set the value of the <code>role</code> property.</p>
     * @param role the new property value
     */
    public void setRole(java.lang.String role) {
        getStateHelper().put(PropertyKeys.role, role);
        handleAttribute("role", role);
    }


    /**
     * <p>Return the value of the <code>style</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style(s) to be applied when this component is rendered.
     */
    public java.lang.String getStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.style);

    }

    /**
     * <p>Set the value of the <code>style</code> property.</p>
     * @param style the new property value
     */
    public void setStyle(java.lang.String style) {
        getStateHelper().put(PropertyKeys.style, style);
        handleAttribute("style", style);
    }


    /**
     * <p>Return the value of the <code>styleClass</code> property.</p>
     * @return the property value
     * <p>Contents: Space-separated list of CSS style class(es) to be applied when
     * this element is rendered.  This value must be passed through
     * as the "class" attribute on generated markup.
     */
    public java.lang.String getStyleClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.styleClass);

    }

    /**
     * <p>Set the value of the <code>styleClass</code> property.</p>
     * @param styleClass the new property value
     */
    public void setStyleClass(java.lang.String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }


    /**
     * <p>Return the value of the <code>title</code> property.</p>
     * @return the property value
     * <p>Contents: Advisory title information about markup elements generated
     * for this component.
     */
    public java.lang.String getTitle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.title);

    }

    /**
     * <p>Set the value of the <code>title</code> property.</p>
     * @param title the new property value
     */
    public void setTitle(java.lang.String title) {
        getStateHelper().put(PropertyKeys.title, title);
        handleAttribute("title", title);
    }


    /**
     * <p>Return the value of the <code>tooltip</code> property.</p>
     * @return the property value
     * <p>Contents: Flag indicating whether the detail portion of the
     * message should be displayed as a tooltip.
     */
    public boolean isTooltip() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.tooltip, false);

    }

    /**
     * <p>Set the value of the <code>tooltip</code> property.</p>
     * @param tooltip the new property value
     */
    public void setTooltip(boolean tooltip) {
        getStateHelper().put(PropertyKeys.tooltip, tooltip);
    }


    /**
     * <p>Return the value of the <code>warnClass</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style class to apply to any message
     * with a severity class of "WARN".
     */
    public java.lang.String getWarnClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.warnClass);

    }

    /**
     * <p>Set the value of the <code>warnClass</code> property.</p>
     * @param warnClass the new property value
     */
    public void setWarnClass(java.lang.String warnClass) {
        getStateHelper().put(PropertyKeys.warnClass, warnClass);
    }


    /**
     * <p>Return the value of the <code>warnStyle</code> property.</p>
     * @return the property value
     * <p>Contents: CSS style(s) to apply to any message
     * with a severity class of "WARN".
     */
    public java.lang.String getWarnStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.warnStyle);

    }

    /**
     * <p>Set the value of the <code>warnStyle</code> property.</p>
     * @param warnStyle the new property value
     */
    public void setWarnStyle(java.lang.String warnStyle) {
        getStateHelper().put(PropertyKeys.warnStyle, warnStyle);
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
