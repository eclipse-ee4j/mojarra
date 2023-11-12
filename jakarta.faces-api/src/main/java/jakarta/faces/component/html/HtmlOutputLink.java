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

import jakarta.faces.component.UIOutput;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlEvents.HtmlDocumentElementEvent;

/**
 * <p>
 * Represents an HTML <code>a</code> (hyperlink) element that may be used to link to an arbitrary URL defined by the
 * <code>value</code> property.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Link</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlOutputLink extends UIOutput implements ClientBehaviorHolder {

    public HtmlOutputLink() {
        super();
        setRendererType("jakarta.faces.Link");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlOutputLink";

    /**
     * The property keys.
     */
    protected enum PropertyKeys {
        accesskey, charset, coords, dir, disabled, fragment, hreflang, lang, onblur, onclick, ondblclick, onfocus, onkeydown, onkeypress, onkeyup, onmousedown,
        onmousemove, onmouseout, onmouseover, onmouseup, rel, rev, role, shape, style, styleClass, tabindex, target, title, type,;

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
     * Return the value of the <code>charset</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The character encoding of the resource designated by this hyperlink.
     */
    public java.lang.String getCharset() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.charset);

    }

    /**
     * <p>
     * Set the value of the <code>charset</code> property.
     * </p>
     *
     * @param charset the new property value
     */
    public void setCharset(java.lang.String charset) {
        getStateHelper().put(PropertyKeys.charset, charset);
        handleAttribute(this, "charset", charset);
    }

    /**
     * <p>
     * Return the value of the <code>coords</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The position and shape of the hot spot on the screen (for use in client-side image maps).
     */
    public java.lang.String getCoords() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.coords);

    }

    /**
     * <p>
     * Set the value of the <code>coords</code> property.
     * </p>
     *
     * @param coords the new property value
     */
    public void setCoords(java.lang.String coords) {
        getStateHelper().put(PropertyKeys.coords, coords);
        handleAttribute(this, "coords", coords);
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
     * Contents: Flag indicating that this element must never receive focus or be included in a subsequent submit.
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
     * Return the value of the <code>fragment</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The identifier of the page fragment which should be brought into focus when the target page is rendered.
     * The value of this attribute is appended to the end of target URL following a hash (#) mark. This notation is part of
     * the standard URL syntax.
     */
    public java.lang.String getFragment() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.fragment);

    }

    /**
     * <p>
     * Set the value of the <code>fragment</code> property.
     * </p>
     *
     * @param fragment the new property value
     */
    public void setFragment(java.lang.String fragment) {
        getStateHelper().put(PropertyKeys.fragment, fragment);
    }

    /**
     * <p>
     * Return the value of the <code>hreflang</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The language code of the resource designated by this hyperlink.
     */
    public java.lang.String getHreflang() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.hreflang);

    }

    /**
     * <p>
     * Set the value of the <code>hreflang</code> property.
     * </p>
     *
     * @param hreflang the new property value
     */
    public void setHreflang(java.lang.String hreflang) {
        getStateHelper().put(PropertyKeys.hreflang, hreflang);
        handleAttribute(this, "hreflang", hreflang);
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
     * Return the value of the <code>rel</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The relationship from the current document to the anchor specified by this hyperlink. The value of this
     * attribute is a space-separated list of link types.
     */
    public java.lang.String getRel() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rel);

    }

    /**
     * <p>
     * Set the value of the <code>rel</code> property.
     * </p>
     *
     * @param rel the new property value
     */
    public void setRel(java.lang.String rel) {
        getStateHelper().put(PropertyKeys.rel, rel);
        handleAttribute(this, "rel", rel);
    }

    /**
     * <p>
     * Return the value of the <code>rev</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: A reverse link from the anchor specified by this hyperlink to the current document. The value of this
     * attribute is a space-separated list of link types.
     */
    public java.lang.String getRev() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rev);

    }

    /**
     * <p>
     * Set the value of the <code>rev</code> property.
     * </p>
     *
     * @param rev the new property value
     */
    public void setRev(java.lang.String rev) {
        getStateHelper().put(PropertyKeys.rev, rev);
        handleAttribute(this, "rev", rev);
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
     * Return the value of the <code>shape</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The shape of the hot spot on the screen (for use in client-side image maps). Valid values are: default
     * (entire region); rect (rectangular region); circle (circular region); and poly (polygonal region).
     */
    public java.lang.String getShape() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.shape);

    }

    /**
     * <p>
     * Set the value of the <code>shape</code> property.
     * </p>
     *
     * @param shape the new property value
     */
    public void setShape(java.lang.String shape) {
        getStateHelper().put(PropertyKeys.shape, shape);
        handleAttribute(this, "shape", shape);
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
     * Return the value of the <code>target</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Name of a frame where the resource retrieved via this hyperlink is to be displayed.
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

    /**
     * <p>
     * Return the value of the <code>type</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The content type of the resource designated by this hyperlink.
     */
    public java.lang.String getType() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.type);

    }

    /**
     * <p>
     * Set the value of the <code>type</code> property.
     * </p>
     *
     * @param type the new property value
     */
    public void setType(java.lang.String type) {
        getStateHelper().put(PropertyKeys.type, type);
        handleAttribute(this, "type", type);
    }

    @Override
    public Collection<String> getEventNames() {
        return getHtmlBodyElementEventNames(getFacesContext()); 
    }

    @Override
    public String getDefaultEventName() {
        return HtmlDocumentElementEvent.click.name();
    }

}
