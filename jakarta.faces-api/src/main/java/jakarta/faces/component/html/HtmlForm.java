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

import jakarta.faces.component.UIForm;
import jakarta.faces.component.behavior.ClientBehaviorHolder;

/**
 * <p>
 * Represents an HTML <code>form</code> element. Child input components will be submitted unless they have been
 * disabled.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Form</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlForm extends UIForm implements ClientBehaviorHolder {

    public HtmlForm() {
        super();
        setRendererType("jakarta.faces.Form");
        handleAttribute(this, "enctype", "application/x-www-form-urlencoded");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlForm";

    /**
     * Properties used by this component
     *
     */
    protected enum PropertyKeys {
        accept, acceptcharset, dir, enctype, lang, onclick, ondblclick, onkeydown, onkeypress, onkeyup, onmousedown, onmousemove, onmouseout, onmouseover,
        onmouseup, onreset, onsubmit, role, style, styleClass, target, title,;

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
     * Return the value of the <code>accept</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: List of content types that a server processing this form will handle correctly
     */
    public java.lang.String getAccept() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.accept);

    }

    /**
     * <p>
     * Set the value of the <code>accept</code> property.
     * </p>
     *
     * @param accept the new property value
     */
    public void setAccept(java.lang.String accept) {
        getStateHelper().put(PropertyKeys.accept, accept);
        handleAttribute(this, "accept", accept);
    }

    /**
     * <p>
     * Return the value of the <code>acceptcharset</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: List of character encodings for input data that are accepted by the server processing this form.
     */
    public java.lang.String getAcceptcharset() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.acceptcharset);

    }

    /**
     * <p>
     * Set the value of the <code>acceptcharset</code> property.
     * </p>
     *
     * @param acceptcharset the new property value
     */
    public void setAcceptcharset(java.lang.String acceptcharset) {
        getStateHelper().put(PropertyKeys.acceptcharset, acceptcharset);
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
     * Return the value of the <code>enctype</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Content type used to submit the form to the server. If not specified, the default value is
     * "application/x-www-form-urlencoded".
     */
    public java.lang.String getEnctype() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.enctype, "application/x-www-form-urlencoded");

    }

    /**
     * <p>
     * Set the value of the <code>enctype</code> property.
     * </p>
     *
     * @param enctype the new property value
     */
    public void setEnctype(java.lang.String enctype) {
        getStateHelper().put(PropertyKeys.enctype, enctype);
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
     * Return the value of the <code>onreset</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when this form is reset.
     */
    public java.lang.String getOnreset() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onreset);

    }

    /**
     * <p>
     * Set the value of the <code>onreset</code> property.
     * </p>
     *
     * @param onreset the new property value
     */
    public void setOnreset(java.lang.String onreset) {
        getStateHelper().put(PropertyKeys.onreset, onreset);
        handleAttribute(this, "onreset", onreset);
    }

    /**
     * <p>
     * Return the value of the <code>onsubmit</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Javascript code executed when this form is submitted.
     */
    public java.lang.String getOnsubmit() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onsubmit);

    }

    /**
     * <p>
     * Set the value of the <code>onsubmit</code> property.
     * </p>
     *
     * @param onsubmit the new property value
     */
    public void setOnsubmit(java.lang.String onsubmit) {
        getStateHelper().put(PropertyKeys.onsubmit, onsubmit);
        handleAttribute(this, "onsubmit", onsubmit);
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
     * Return the value of the <code>target</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Name of a frame where the response retrieved after this form submit is to be displayed.
     */
    public java.lang.String getTarget() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.target);

    }

    /**
     * <p>
     * Set the value of the <code>target</code> property.
     * </p>
     *
     * @param target the new property value
     */
    public void setTarget(java.lang.String target) {
        getStateHelper().put(PropertyKeys.target, target);
        handleAttribute(this, "target", target);
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

    @Override
    public Collection<String> getEventNames() {
        return getHtmlBodyElementEventNames(getFacesContext()); 
    }

    @Override
    public String getDefaultEventName() {
        return null;
    }

}
