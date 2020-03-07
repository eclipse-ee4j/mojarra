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

package jakarta.faces.component;

/**
 * <p>
 * <strong>UIColumn</strong> is a {@link UIComponent} that represents a single column of data within a parent
 * {@link UIData} component.
 * </p>
 */
public class UIColumn extends UIComponentBase {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Column";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Column";

    // ----------------------------------------------------------- Constructors

    /**
     * <p>
     * Create a new {@link UIColumn} instance with default property values.
     * </p>
     */
    public UIColumn() {
        super();
        setRendererType(null);
    }

    // -------------------------------------------------------------- Properties

    /**
     * Get the component family.
     *
     * @return the component family.
     */
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Return the footer facet of the column (if any). A convenience method for <code>getFacet("footer")</code>.
     * </p>
     *
     * @return the footer component.
     */
    public UIComponent getFooter() {
        return getFacet("footer");
    }

    /**
     * <p>
     * Set the footer facet of the column. A convenience method for <code>getFacets().put("footer", footer)</code>.
     * </p>
     *
     * @param footer the new footer facet
     *
     * @throws NullPointerException if <code>footer</code> is <code>null</code>
     */
    public void setFooter(UIComponent footer) {
        getFacets().put("footer", footer);
    }

    /**
     * <p>
     * Return the header facet of the column (if any). A convenience method for <code>getFacet("header")</code>.
     * </p>
     *
     * @return the header component.
     */
    public UIComponent getHeader() {
        return getFacet("header");
    }

    /**
     * <p>
     * Set the header facet of the column. A convenience method for <code>getFacets().put("header", header)</code>.
     * </p>
     *
     * @param header the new header facet
     *
     * @throws NullPointerException if <code>header</code> is <code>null</code>
     */
    public void setHeader(UIComponent header) {
        getFacets().put("header", header);
    }

}
