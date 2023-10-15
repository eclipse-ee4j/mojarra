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
 * Renders child components in a table, starting a new row after the specified number of columns.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Grid</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlPanelGrid extends UIPanel implements ClientBehaviorHolder {

    public HtmlPanelGrid() {
        super();
        setRendererType("jakarta.faces.Grid");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlPanelGrid";

    /**
     * Properties used by this component
     *
     */
    protected enum PropertyKeys {
        bgcolor, bodyrows, border, captionClass, captionStyle, cellpadding, cellspacing, columnClasses, columns, dir, footerClass, frame, headerClass, lang,
        onclick, ondblclick, onkeydown, onkeypress, onkeyup, onmousedown, onmousemove, onmouseout, onmouseover, onmouseup, role, rowClass, rowClasses, rules,
        style, styleClass, summary, title, width,;

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
     * Return the value of the <code>bgcolor</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Name or code of the background color for this table.
     */
    public java.lang.String getBgcolor() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.bgcolor);

    }

    /**
     * <p>
     * Set the value of the <code>bgcolor</code> property.
     * </p>
     *
     * @param bgcolor the new property value
     */
    public void setBgcolor(java.lang.String bgcolor) {
        getStateHelper().put(PropertyKeys.bgcolor, bgcolor);
        handleAttribute(this, "bgcolor", bgcolor);
    }

    /**
     * <p>
     * Return the value of the <code>bodyrows</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Comma separated list of row indices for which a new "tbody" element should be started (and any previously
     * opened one should be ended).
     */
    public java.lang.String getBodyrows() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.bodyrows);

    }

    /**
     * <p>
     * Set the value of the <code>bodyrows</code> property.
     * </p>
     *
     * @param bodyrows the new property value
     */
    public void setBodyrows(java.lang.String bodyrows) {
        getStateHelper().put(PropertyKeys.bodyrows, bodyrows);
    }

    /**
     * <p>
     * Return the value of the <code>border</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Width (in pixels) of the border to be drawn around this table.
     */
    public int getBorder() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.border, Integer.MIN_VALUE);

    }

    /**
     * <p>
     * Set the value of the <code>border</code> property.
     * </p>
     *
     * @param border the new property value
     */
    public void setBorder(int border) {
        getStateHelper().put(PropertyKeys.border, border);
        handleAttribute(this, "border", border);
    }

    /**
     * <p>
     * Return the value of the <code>captionClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) that will be applied to any caption generated for this table.
     */
    public java.lang.String getCaptionClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.captionClass);

    }

    /**
     * <p>
     * Set the value of the <code>captionClass</code> property.
     * </p>
     *
     * @param captionClass the new property value
     */
    public void setCaptionClass(java.lang.String captionClass) {
        getStateHelper().put(PropertyKeys.captionClass, captionClass);
    }

    /**
     * <p>
     * Return the value of the <code>captionStyle</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: CSS style(s) to be applied when this caption is rendered.
     */
    public java.lang.String getCaptionStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.captionStyle);

    }

    /**
     * <p>
     * Set the value of the <code>captionStyle</code> property.
     * </p>
     *
     * @param captionStyle the new property value
     */
    public void setCaptionStyle(java.lang.String captionStyle) {
        getStateHelper().put(PropertyKeys.captionStyle, captionStyle);
    }

    /**
     * <p>
     * Return the value of the <code>cellpadding</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Definition of how much space the user agent should leave between the border of each cell and its contents.
     */
    public java.lang.String getCellpadding() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.cellpadding);

    }

    /**
     * <p>
     * Set the value of the <code>cellpadding</code> property.
     * </p>
     *
     * @param cellpadding the new property value
     */
    public void setCellpadding(java.lang.String cellpadding) {
        getStateHelper().put(PropertyKeys.cellpadding, cellpadding);
        handleAttribute(this, "cellpadding", cellpadding);
    }

    /**
     * <p>
     * Return the value of the <code>cellspacing</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Definition of how much space the user agent should leave between the left side of the table and the
     * leftmost column, the top of the table and the top of the top side of the topmost row, and so on for the right and
     * bottom of the table. It also specifies the amount of space to leave between cells.
     */
    public java.lang.String getCellspacing() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.cellspacing);

    }

    /**
     * <p>
     * Set the value of the <code>cellspacing</code> property.
     * </p>
     *
     * @param cellspacing the new property value
     */
    public void setCellspacing(java.lang.String cellspacing) {
        getStateHelper().put(PropertyKeys.cellspacing, cellspacing);
        handleAttribute(this, "cellspacing", cellspacing);
    }

    /**
     * <p>
     * Return the value of the <code>columnClasses</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Comma-delimited list of CSS style classes that will be applied to the columns of this table. A space
     * separated list of classes may also be specified for any individual column. If the number of elements in this list is
     * less than the number of actual column children of the UIData, no "class" attribute is output for each column greater
     * than the number of elements in the list. If the number of elements in the list is greater than the number of actual
     * column children of the UIData, the elements at the posisiton in the list after the last column are ignored.
     */
    public java.lang.String getColumnClasses() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.columnClasses);

    }

    /**
     * <p>
     * Set the value of the <code>columnClasses</code> property.
     * </p>
     *
     * @param columnClasses the new property value
     */
    public void setColumnClasses(java.lang.String columnClasses) {
        getStateHelper().put(PropertyKeys.columnClasses, columnClasses);
    }

    /**
     * <p>
     * Return the value of the <code>columns</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The number of columns to render before starting a new row.
     */
    public int getColumns() {
        return (java.lang.Integer) getStateHelper().eval(PropertyKeys.columns, Integer.MIN_VALUE);

    }

    /**
     * <p>
     * Set the value of the <code>columns</code> property.
     * </p>
     *
     * @param columns the new property value
     */
    public void setColumns(int columns) {
        getStateHelper().put(PropertyKeys.columns, columns);
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
     * Return the value of the <code>footerClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) that will be applied to any footer generated for this table.
     */
    public java.lang.String getFooterClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.footerClass);

    }

    /**
     * <p>
     * Set the value of the <code>footerClass</code> property.
     * </p>
     *
     * @param footerClass the new property value
     */
    public void setFooterClass(java.lang.String footerClass) {
        getStateHelper().put(PropertyKeys.footerClass, footerClass);
    }

    /**
     * <p>
     * Return the value of the <code>frame</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Code specifying which sides of the frame surrounding this table will be visible. Valid values are: none (no
     * sides, default value); above (top side only); below (bottom side only); hsides (top and bottom sides only); vsides
     * (right and left sides only); lhs (left hand side only); rhs (right hand side only); box (all four sides); and border
     * (all four sides).
     */
    public java.lang.String getFrame() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.frame);

    }

    /**
     * <p>
     * Set the value of the <code>frame</code> property.
     * </p>
     *
     * @param frame the new property value
     */
    public void setFrame(java.lang.String frame) {
        getStateHelper().put(PropertyKeys.frame, frame);
        handleAttribute(this, "frame", frame);
    }

    /**
     * <p>
     * Return the value of the <code>headerClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) that will be applied to any header generated for this table.
     */
    public java.lang.String getHeaderClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.headerClass);

    }

    /**
     * <p>
     * Set the value of the <code>headerClass</code> property.
     * </p>
     *
     * @param headerClass the new property value
     */
    public void setHeaderClass(java.lang.String headerClass) {
        getStateHelper().put(PropertyKeys.headerClass, headerClass);
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
     * Return the value of the <code>rowClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: <div class="changed_added_2_3">
     *
     * <p>
     * Assigns one or more space-separated CSS class names to each "tr"
     * </p>
     *
     * </div>
     */
    public java.lang.String getRowClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rowClass);

    }

    /**
     * <p>
     * Set the value of the <code>rowClass</code> property.
     * </p>
     *
     * @param rowClass the new property value
     */
    public void setRowClass(java.lang.String rowClass) {
        getStateHelper().put(PropertyKeys.rowClass, rowClass);
    }

    /**
     * <p>
     * Return the value of the <code>rowClasses</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Comma-delimited list of CSS style classes that will be applied to the rows of this table. A space separated
     * list of classes may also be specified for any individual row. Thes styles are applied, in turn, to each row in the
     * table. For example, if the list has two elements, the first style class in the list is applied to the first row, the
     * second to the second row, the first to the third row, the second to the fourth row, etc. In other words, we keep
     * iterating through the list until we reach the end, and then we start at the beginning again.
     */
    public java.lang.String getRowClasses() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rowClasses);

    }

    /**
     * <p>
     * Set the value of the <code>rowClasses</code> property.
     * </p>
     *
     * @param rowClasses the new property value
     */
    public void setRowClasses(java.lang.String rowClasses) {
        getStateHelper().put(PropertyKeys.rowClasses, rowClasses);
    }

    /**
     * <p>
     * Return the value of the <code>rules</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Code specifying which rules will appear between cells within this table. Valid values are: none (no rules,
     * default value); groups (between row groups); rows (between rows only); cols (between columns only); and all (between
     * all rows and columns).
     */
    public java.lang.String getRules() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.rules);

    }

    /**
     * <p>
     * Set the value of the <code>rules</code> property.
     * </p>
     *
     * @param rules the new property value
     */
    public void setRules(java.lang.String rules) {
        getStateHelper().put(PropertyKeys.rules, rules);
        handleAttribute(this, "rules", rules);
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
     * Return the value of the <code>summary</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Summary of this table's purpose and structure, for user agents rendering to non-visual media such as speech
     * and Braille.
     */
    public java.lang.String getSummary() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.summary);

    }

    /**
     * <p>
     * Set the value of the <code>summary</code> property.
     * </p>
     *
     * @param summary the new property value
     */
    public void setSummary(java.lang.String summary) {
        getStateHelper().put(PropertyKeys.summary, summary);
        handleAttribute(this, "summary", summary);
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
     * Return the value of the <code>width</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Width of the entire table, for visual user agents.
     */
    public java.lang.String getWidth() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.width);

    }

    /**
     * <p>
     * Set the value of the <code>width</code> property.
     * </p>
     *
     * @param width the new property value
     */
    public void setWidth(java.lang.String width) {
        getStateHelper().put(PropertyKeys.width, width);
        handleAttribute(this, "width", width);
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
