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
import static jakarta.faces.component.html.HtmlEvents.getHtmlBodyElementEventNames;

import java.util.Collection;

import jakarta.faces.component.UIPanel;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

/**
 * <p>
 * Causes all child components of this component to be rendered. This is useful in scenarios where a parent component is
 * expecting a single component to be present, but the application wishes to render more than one.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Group</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlPanelGroup extends UIPanel implements ClientBehaviorHolder {

    public HtmlPanelGroup() {
        super();
        setRendererType("jakarta.faces.Group");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlPanelGroup";

    /**
     * Properties used by this component
     *
     */
    protected enum PropertyKeys {
        layout, onclick, ondblclick, onkeydown, onkeypress, onkeyup, onmousedown, onmousemove, onmouseout, onmouseover, onmouseup, style, styleClass,;

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
     * Return the value of the <code>layout</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The type of layout markup to use when rendering this group. If the value is "block" the renderer must
     * produce an HTML "div" element. Otherwise HTML "span" element must be produced.
     */
    public java.lang.String getLayout() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.layout);

    }

    /**
     * <p>
     * Set the value of the <code>layout</code> property.
     * </p>
     *
     * @param layout the new property value
     */
    public void setLayout(java.lang.String layout) {
        getStateHelper().put(PropertyKeys.layout, layout);
    }

    /**
     * <p>
     * Return the value of the <code>onclick</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is clicked over this element.
     */
    public java.lang.String getOnclick() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onclick);

    }

    /**
     * <p>
     * Set the value of the <code>onclick</code> property.
     * </p>
     *
     * @param onclick the new property value
     */
    public void setOnclick(java.lang.String onclick) {
        getStateHelper().put(PropertyKeys.onclick, onclick);
        handleAttribute(this, "onclick", onclick);
    }

    /**
     * <p>
     * Return the value of the <code>ondblclick</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is double clicked over this element.
     */
    public java.lang.String getOndblclick() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.ondblclick);

    }

    /**
     * <p>
     * Set the value of the <code>ondblclick</code> property.
     * </p>
     *
     * @param ondblclick the new property value
     */
    public void setOndblclick(java.lang.String ondblclick) {
        getStateHelper().put(PropertyKeys.ondblclick, ondblclick);
        handleAttribute(this, "ondblclick", ondblclick);
    }

    /**
     * <p>
     * Return the value of the <code>onkeydown</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a key is pressed down over this element.
     */
    public java.lang.String getOnkeydown() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onkeydown);

    }

    /**
     * <p>
     * Set the value of the <code>onkeydown</code> property.
     * </p>
     *
     * @param onkeydown the new property value
     */
    public void setOnkeydown(java.lang.String onkeydown) {
        getStateHelper().put(PropertyKeys.onkeydown, onkeydown);
        handleAttribute(this, "onkeydown", onkeydown);
    }

    /**
     * <p>
     * Return the value of the <code>onkeypress</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a key is pressed and released over this element.
     */
    public java.lang.String getOnkeypress() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onkeypress);

    }

    /**
     * <p>
     * Set the value of the <code>onkeypress</code> property.
     * </p>
     *
     * @param onkeypress the new property value
     */
    public void setOnkeypress(java.lang.String onkeypress) {
        getStateHelper().put(PropertyKeys.onkeypress, onkeypress);
        handleAttribute(this, "onkeypress", onkeypress);
    }

    /**
     * <p>
     * Return the value of the <code>onkeyup</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a key is released over this element.
     */
    public java.lang.String getOnkeyup() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onkeyup);

    }

    /**
     * <p>
     * Set the value of the <code>onkeyup</code> property.
     * </p>
     *
     * @param onkeyup the new property value
     */
    public void setOnkeyup(java.lang.String onkeyup) {
        getStateHelper().put(PropertyKeys.onkeyup, onkeyup);
        handleAttribute(this, "onkeyup", onkeyup);
    }

    /**
     * <p>
     * Return the value of the <code>onmousedown</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is pressed down over this element.
     */
    public java.lang.String getOnmousedown() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmousedown);

    }

    /**
     * <p>
     * Set the value of the <code>onmousedown</code> property.
     * </p>
     *
     * @param onmousedown the new property value
     */
    public void setOnmousedown(java.lang.String onmousedown) {
        getStateHelper().put(PropertyKeys.onmousedown, onmousedown);
        handleAttribute(this, "onmousedown", onmousedown);
    }

    /**
     * <p>
     * Return the value of the <code>onmousemove</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is moved within this element.
     */
    public java.lang.String getOnmousemove() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmousemove);

    }

    /**
     * <p>
     * Set the value of the <code>onmousemove</code> property.
     * </p>
     *
     * @param onmousemove the new property value
     */
    public void setOnmousemove(java.lang.String onmousemove) {
        getStateHelper().put(PropertyKeys.onmousemove, onmousemove);
        handleAttribute(this, "onmousemove", onmousemove);
    }

    /**
     * <p>
     * Return the value of the <code>onmouseout</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is moved away from this element.
     */
    public java.lang.String getOnmouseout() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmouseout);

    }

    /**
     * <p>
     * Set the value of the <code>onmouseout</code> property.
     * </p>
     *
     * @param onmouseout the new property value
     */
    public void setOnmouseout(java.lang.String onmouseout) {
        getStateHelper().put(PropertyKeys.onmouseout, onmouseout);
        handleAttribute(this, "onmouseout", onmouseout);
    }

    /**
     * <p>
     * Return the value of the <code>onmouseover</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is moved onto this element.
     */
    public java.lang.String getOnmouseover() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmouseover);

    }

    /**
     * <p>
     * Set the value of the <code>onmouseover</code> property.
     * </p>
     *
     * @param onmouseover the new property value
     */
    public void setOnmouseover(java.lang.String onmouseover) {
        getStateHelper().put(PropertyKeys.onmouseover, onmouseover);
        handleAttribute(this, "onmouseover", onmouseover);
    }

    /**
     * <p>
     * Return the value of the <code>onmouseup</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when a pointer button is released over this element.
     */
    public java.lang.String getOnmouseup() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onmouseup);

    }

    /**
     * <p>
     * Set the value of the <code>onmouseup</code> property.
     * </p>
     *
     * @param onmouseup the new property value
     */
    public void setOnmouseup(java.lang.String onmouseup) {
        getStateHelper().put(PropertyKeys.onmouseup, onmouseup);
        handleAttribute(this, "onmouseup", onmouseup);
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
     * passed through as the "class" property on generated markup.
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

    @Override
    public Collection<String> getEventNames() {
        return getHtmlBodyElementEventNames(getFacesContext()); 
    }

    @Override
    public String getDefaultEventName() {
        return null;
    }

}
