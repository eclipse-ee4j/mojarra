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

package com.sun.faces.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.el.ValueExpression;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

/**
 * <p>
 * Causes all child components of this component to be rendered. This is useful in scenarios where a parent component is
 * expecting a single component to be present, but the application wishes to render more than one.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.passthrough.Element</code>".
 * This value can be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class PassthroughElement extends jakarta.faces.component.UIPanel implements ClientBehaviorHolder {

    private static final String OPTIMIZED_PACKAGE = "jakarta.faces.component.";

    public PassthroughElement() {
        super();
        setRendererType("jakarta.faces.passthrough.Element");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Panel";

    protected enum PropertyKeys {
        onclick, ondblclick, onkeydown, onkeypress, onkeyup, onmousedown, onmousemove, onmouseout, onmouseover, onmouseup, style, styleClass,;

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
     * Return the value of the <code>onclick</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is clicked over this element.
     * @return the value of the <code>onclick</code> property. 
     */
    public java.lang.String getOnclick() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onclick);

    }

    /**
     * <p>
     * Set the value of the <code>onclick</code> property.
     * </p>
     * @param onclick the value of the <code>onclick</code> property.
     */
    public void setOnclick(java.lang.String onclick) {
        getStateHelper().put(PropertyKeys.onclick, onclick);
        handleAttribute("onclick", onclick);
    }

    /**
     * <p>
     * Return the value of the <code>ondblclick</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is double clicked over this element.
     * @return the value of the <code>ondblclick</code> property.
     */
    public java.lang.String getOndblclick() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.ondblclick);

    }

    /**
     * <p>
     * Set the value of the <code>ondblclick</code> property.
     * </p>
     * @param ondblclick the value of the <code>ondblclick</code> property.
     */
    public void setOndblclick(java.lang.String ondblclick) {
        getStateHelper().put(PropertyKeys.ondblclick, ondblclick);
        handleAttribute("ondblclick", ondblclick);
    }

    /**
     * <p>
     * Return the value of the <code>onkeydown</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a key is pressed down over this element.
     * @return the value of the <code>onkeydown</code> property.
     */
    public java.lang.String getOnkeydown() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onkeydown);

    }

    /**
     * <p>
     * Set the value of the <code>onkeydown</code> property.
     * </p>
     * @param onkeydown the value of the <code>onkeydown</code> property.
     */
    public void setOnkeydown(java.lang.String onkeydown) {
        getStateHelper().put(PropertyKeys.onkeydown, onkeydown);
        handleAttribute("onkeydown", onkeydown);
    }

    /**
     * <p>
     * Return the value of the <code>onkeypress</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a key is pressed and released over this element.
     * @return the value of the <code>onkeypress</code> property.
     */
    public java.lang.String getOnkeypress() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onkeypress);

    }

    /**
     * <p>
     * Set the value of the <code>onkeypress</code> property.
     * </p>
     * @param onkeypress the value of the <code>onkeypress</code> property.
     */
    public void setOnkeypress(java.lang.String onkeypress) {
        getStateHelper().put(PropertyKeys.onkeypress, onkeypress);
        handleAttribute("onkeypress", onkeypress);
    }

    /**
     * <p>
     * Return the value of the <code>onkeyup</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a key is released over this element.
     * @return the value of the <code>onkeyup</code> property.
     */
    public java.lang.String getOnkeyup() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onkeyup);

    }

    /**
     * <p>
     * Set the value of the <code>onkeyup</code> property.
     * </p>
     * @param onkeyup the value of the <code>onkeyup</code> property.
     */
    public void setOnkeyup(java.lang.String onkeyup) {
        getStateHelper().put(PropertyKeys.onkeyup, onkeyup);
        handleAttribute("onkeyup", onkeyup);
    }

    /**
     * <p>
     * Return the value of the <code>onmousedown</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is pressed down over this element.
     * @return the value of the <code>onmousedown</code> property.
     */
    public java.lang.String getOnmousedown() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmousedown);

    }

    /**
     * <p>
     * Set the value of the <code>onmousedown</code> property.
     * </p>
     * @param onmousedown the value of the <code>onmousedown</code> property.
     */
    public void setOnmousedown(java.lang.String onmousedown) {
        getStateHelper().put(PropertyKeys.onmousedown, onmousedown);
        handleAttribute("onmousedown", onmousedown);
    }

    /**
     * <p>
     * Return the value of the <code>onmousemove</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is moved within this element.
     * @return the value of the <code>onmousemove</code> property.
     */
    public java.lang.String getOnmousemove() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmousemove);

    }

    /**
     * <p>
     * Set the value of the <code>onmousemove</code> property.
     * </p>
     * @param onmousemove the value of the <code>onmousemove</code> property.
     */
    public void setOnmousemove(java.lang.String onmousemove) {
        getStateHelper().put(PropertyKeys.onmousemove, onmousemove);
        handleAttribute("onmousemove", onmousemove);
    }

    /**
     * <p>
     * Return the value of the <code>onmouseout</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is moved away from this element.
     * @return the value of the <code>onmouseout</code> property.
     */
    public java.lang.String getOnmouseout() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmouseout);

    }

    /**
     * <p>
     * Set the value of the <code>onmouseout</code> property.
     * </p>
     * @param onmouseout the value of the <code>onmouseout</code> property.
     */
    public void setOnmouseout(java.lang.String onmouseout) {
        getStateHelper().put(PropertyKeys.onmouseout, onmouseout);
        handleAttribute("onmouseout", onmouseout);
    }

    /**
     * <p>
     * Return the value of the <code>onmouseover</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is moved onto this element.
     * @return the value of the <code>onmouseover</code> property.
     */
    public java.lang.String getOnmouseover() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmouseover);

    }

    /**
     * <p>
     * Set the value of the <code>onmouseover</code> property.
     * </p>
     * @param onmouseover the value of the <code>onmouseover</code> property.
     */
    public void setOnmouseover(java.lang.String onmouseover) {
        getStateHelper().put(PropertyKeys.onmouseover, onmouseover);
        handleAttribute("onmouseover", onmouseover);
    }

    /**
     * <p>
     * Return the value of the <code>onmouseup</code> property.
     * </p>
     * <p>
     * Contents: Javascript code executed when a pointer button is released over this element.
     * @return the value of the <code>onmouseup</code> property.
     */
    public java.lang.String getOnmouseup() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmouseup);

    }

    /**
     * <p>
     * Set the value of the <code>onmouseup</code> property.
     * </p>
     * @param onmouseup the value of the <code>onmouseup</code> property.
     */
    public void setOnmouseup(java.lang.String onmouseup) {
        getStateHelper().put(PropertyKeys.onmouseup, onmouseup);
        handleAttribute("onmouseup", onmouseup);
    }

    /**
     * <p>
     * Return the value of the <code>style</code> property.
     * </p>
     * <p>
     * Contents: CSS style(s) to be applied when this component is rendered.
     * @return the value of the <code>style</code> property.
     */
    public java.lang.String getStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.style);

    }

    /**
     * <p>
     * Set the value of the <code>style</code> property.
     * </p>
     * @param style the value of the <code>style</code> property.
     */
    public void setStyle(java.lang.String style) {
        getStateHelper().put(PropertyKeys.style, style);
        handleAttribute("style", style);
    }

    /**
     * <p>
     * Return the value of the <code>styleClass</code> property.
     * </p>
     * <p>
     * Contents: Space-separated list of CSS style class(es) to be applied when this element is rendered. This value must be
     * passed through as the "class" property on generated markup.
     * @return the value of the <code>styleClass</code> property.
     */
    public java.lang.String getStyleClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.styleClass);

    }

    /**
     * <p>
     * Set the value of the <code>styleClass</code> property.
     * </p>
     * @param styleClass the value of the <code>styleClass</code> property.
     */
    public void setStyleClass(java.lang.String styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, styleClass);
    }

    private static final List<String> EVENT_NAMES = List.of(
            "click", "dblclick", "keydown", "keypress", "keyup", "mousedown", "mousemove", "mouseout", "mouseover", "mouseup");

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "click";
    }

    // TODO The same as jakarta.faces.component.html.HtmlComponentUtils#handleAttribute
    private void handleAttribute(String name, Object value) {
        @SuppressWarnings("unchecked")
        List<String> setAttributes = (List<String>) getAttributes().get("jakarta.faces.component.UIComponentBase.attributesThatAreSet");
        if (setAttributes == null) {
            String cname = this.getClass().getName();
            if (cname.startsWith(OPTIMIZED_PACKAGE)) {
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
