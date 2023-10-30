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

import jakarta.faces.component.UIColumn;

/**
 * <p>
 * Represents a column that will be rendered in an HTML <code>table</code> element.
 * </p>
 */
public class HtmlColumn extends UIColumn {

    public HtmlColumn() {
        super();
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlColumn";

    /**
     * The property keys.
     */
    protected enum PropertyKeys {
        footerClass, headerClass, rowHeader, styleClass,;

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
     * Return the value of the <code>footerClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) that will be applied to any footer generated for this column.
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
     * Return the value of the <code>headerClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) that will be applied to any header generated for this column.
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
     * Return the value of the <code>rowHeader</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Flag indicating that this column is a row header column and therefore cells in this column should be
     * rendered with "th" instead of "td" and must have the 'scope="row"' attribute.
     */
    public boolean isRowHeader() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.rowHeader, false);

    }

    /**
     * <p>
     * Set the value of the <code>rowHeader</code> property.
     * </p>
     *
     * @param rowHeader the new property value
     */
    public void setRowHeader(boolean rowHeader) {
        getStateHelper().put(PropertyKeys.rowHeader, rowHeader);
    }

    /**
     * <p>
     * Return the value of the <code>styleClass</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Space-separated list of CSS style class(es) that will be applied to the "td" of this column
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

}
