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

import jakarta.el.ValueExpression;
import jakarta.faces.model.SelectItem;

/**
 * <p>
 * <strong>UISelectItems</strong> is a component that may be nested inside a {@link UISelectMany} or {@link UISelectOne}
 * component, and causes the addition of one or more {@link SelectItem} instances to the list of available options in
 * the parent component. The <code>value</code> of this component (set either directly, or acquired indirectly a
 * {@link jakarta.el.ValueExpression}, can be of any of the following types:
 * </p>
 * <ul>
 * <li><em>Single instance of {@link SelectItem}</em> - This instance is added to the set of available options for the
 * parent tag.</li>
 * <li><em>Array of {@link SelectItem}</em> - This set of instances is added to the set of available options for the
 * parent component, in ascending subscript order.</li>
 * <li><em>Collection of {@link SelectItem}</em> - This set of instances is added to the set of available options for
 * the parent component, in the order provided by an iterator over them.</li>
 * <li><em>Map</em> - The keys of this object (once converted to Strings) are assumed to be labels, and the values of
 * this object (once converted to Strings) are assumed to be values, of {@link SelectItem} instances that will be
 * constructed dynamically and added to the set of available options for the parent component, in the order provided by
 * an iterator over the keys.</li>
 * </ul>
 */

public class UISelectItems extends UIComponentBase {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectItems";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.SelectItems";

    enum PropertyKeys {
        value
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UISelectItems} instance with default property values.
     * </p>
     */
    public UISelectItems() {

        super();
        setRendererType(null);

    }

    // ---------------------------------------------------------------- Bindings

    /**
     * <p class="changed_modified_4_0">
     * Set the {@link ValueExpression} used to calculate the value for the specified attribute or property name, if any.
     * In addition, if a {@link ValueExpression} is set for the <code>var</code> property, regardless of the value, throw an illegal argument exception.
     * </p>
     *
     * @throws IllegalArgumentException If <code>name</code> is one of <code>id</code>, <code>parent</code>, or <code>var</code>.
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {

        if ("var".equals(name)) {
            throw new IllegalArgumentException();
        }

        super.setValueExpression(name, binding);
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {

        return COMPONENT_FAMILY;

    }

    // -------------------------------------------------- ValueHolder Properties

    /**
     * <p>
     * Returns the <code>value</code> property of the <code>UISelectItems</code>.
     * </p>
     *
     * @return the value.
     */
    public Object getValue() {

        return getStateHelper().eval(PropertyKeys.value);

    }

    /**
     * <p>
     * Sets the <code>value</code> property of the <code>UISelectItems</code>.
     * </p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {

        getStateHelper().put(PropertyKeys.value, value);

    }

}
