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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

/**
 * <p>
 * Represents an HTML <code>textarea</code> element.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Textarea</code>". This value
 * can be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlInputTextarea extends UIInput implements ClientBehaviorHolder {

    public HtmlInputTextarea() {
        super();
        setRendererType("jakarta.faces.Textarea");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlInputTextarea";

    /**
     * Properties used by this component
     *
     */
    protected enum PropertyKeys {
        accesskey, cols, dir, disabled, label, lang, onblur, onchange, onclick, ondblclick, onfocus, onkeydown, onkeypress, onkeyup, onmousedown, onmousemove,
        onmouseout, onmouseover, onmouseup, onselect, readonly, role, rows, style, styleClass, tabindex, title,;

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
     * Return the value of the <code>accesskey</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Access key that, when pressed, transfers focus to this element.
     */
    public java.lang.String getAccesskey() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.accesskey);

    }

    /**
     * <p>
     * Set the value of the <code>accesskey</code> property.
     * </p>
     *
     * @param accesskey the new property value
     */
    public void setAccesskey(java.lang.String accesskey) {
        getStateHelper().put(PropertyKeys.accesskey, accesskey);
        handleAttribute(this, "accesskey", accesskey);
    }

    /**
     * <p>
     * Return the value of the <code>cols</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The number of columns to be displayed.
     */
    public int getCols() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.cols, Integer.MIN_VALUE);

    }

    /**
     * <p>
     * Set the value of the <code>cols</code> property.
     * </p>
     *
     * @param cols the new property value
     */
    public void setCols(int cols) {
        getStateHelper().put(PropertyKeys.cols, cols);
        handleAttribute(this, "cols", cols);
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
     * Return the value of the <code>disabled</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Flag indicating that this element must never receive focus or be included in a subsequent submit. A value
     * of false causes no attribute to be rendered, while a value of true causes the attribute to be rendered as
     * disabled="disabled".
     */
    public boolean isDisabled() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.disabled, false);

    }

    /**
     * <p>
     * Set the value of the <code>disabled</code> property.
     * </p>
     *
     * @param disabled the new property value
     */
    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    /**
     * <p>
     * Return the value of the <code>label</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: A localized user presentable name for this component.
     */
    public java.lang.String getLabel() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.label);

    }

    /**
     * <p>
     * Set the value of the <code>label</code> property.
     * </p>
     *
     * @param label the new property value
     */
    public void setLabel(java.lang.String label) {
        getStateHelper().put(PropertyKeys.label, label);
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
     * Return the value of the <code>onblur</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when this element loses focus.
     */
    public java.lang.String getOnblur() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onblur);

    }

    /**
     * <p>
     * Set the value of the <code>onblur</code> property.
     * </p>
     *
     * @param onblur the new property value
     */
    public void setOnblur(java.lang.String onblur) {
        getStateHelper().put(PropertyKeys.onblur, onblur);
        handleAttribute(this, "onblur", onblur);
    }

    /**
     * <p>
     * Return the value of the <code>onchange</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when this element loses focus and its value has been modified since gaining focus.
     */
    public java.lang.String getOnchange() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onchange);

    }

    /**
     * <p>
     * Set the value of the <code>onchange</code> property.
     * </p>
     *
     * @param onchange the new property value
     */
    public void setOnchange(java.lang.String onchange) {
        getStateHelper().put(PropertyKeys.onchange, onchange);
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
     * Return the value of the <code>onfocus</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when this element receives focus.
     */
    public java.lang.String getOnfocus() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onfocus);

    }

    /**
     * <p>
     * Set the value of the <code>onfocus</code> property.
     * </p>
     *
     * @param onfocus the new property value
     */
    public void setOnfocus(java.lang.String onfocus) {
        getStateHelper().put(PropertyKeys.onfocus, onfocus);
        handleAttribute(this, "onfocus", onfocus);
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
     * Return the value of the <code>onselect</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when text within this element is selected by the user.
     */
    public java.lang.String getOnselect() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onselect);

    }

    /**
     * <p>
     * Set the value of the <code>onselect</code> property.
     * </p>
     *
     * @param onselect the new property value
     */
    public void setOnselect(java.lang.String onselect) {
        getStateHelper().put(PropertyKeys.onselect, onselect);
        handleAttribute(this, "onselect", onselect);
    }

    /**
     * <p>
     * Return the value of the <code>readonly</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Flag indicating that this component will prohibit changes by the user. The element may receive focus unless
     * it has also been disabled. A value of false causes no attribute to be rendered, while a value of true causes the
     * attribute to be rendered as readonly="readonly".
     */
    public boolean isReadonly() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.readonly, false);

    }

    /**
     * <p>
     * Set the value of the <code>readonly</code> property.
     * </p>
     *
     * @param readonly the new property value
     */
    public void setReadonly(boolean readonly) {
        getStateHelper().put(PropertyKeys.readonly, readonly);
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
     * Return the value of the <code>rows</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The number of rows to be displayed.
     */
    public int getRows() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.rows, Integer.MIN_VALUE);

    }

    /**
     * <p>
     * Set the value of the <code>rows</code> property.
     * </p>
     *
     * @param rows the new property value
     */
    public void setRows(int rows) {
        getStateHelper().put(PropertyKeys.rows, rows);
        handleAttribute(this, "rows", rows);
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
     * Return the value of the <code>tabindex</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Position of this element in the tabbing order for the current document. This value must be an integer
     * between 0 and 32767.
     */
    public java.lang.String getTabindex() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.tabindex);

    }

    /**
     * <p>
     * Set the value of the <code>tabindex</code> property.
     * </p>
     *
     * @param tabindex the new property value
     */
    public void setTabindex(java.lang.String tabindex) {
        getStateHelper().put(PropertyKeys.tabindex, tabindex);
        handleAttribute(this, "tabindex", tabindex);
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

    private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList("blur", "change", "valueChange", "click", "dblclick",
            "focus", "keydown", "keypress", "keyup", "mousedown", "mousemove", "mouseout", "mouseover", "mouseup", "select"));

    @Override
    public Collection<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public String getDefaultEventName() {
        return "valueChange";
    }

}
