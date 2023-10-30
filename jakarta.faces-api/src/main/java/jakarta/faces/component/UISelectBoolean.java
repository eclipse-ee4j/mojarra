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

/**
 * <p>
 * <strong>UISelectBoolean</strong> is a {@link UIComponent} that represents a single boolean (<code>true</code> or
 * <code>false</code>) value. It is most commonly rendered as a checkbox.
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Checkbox</code>". This value
 * can be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */

public class UISelectBoolean extends UIInput {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectBoolean";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.SelectBoolean";

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UISelectBoolean} instance with default property values.
     * </p>
     */
    public UISelectBoolean() {
        super();
        setRendererType("jakarta.faces.Checkbox");
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Return the local value of the selected state of this component. This method is a typesafe alias for
     * <code>getValue()</code>.
     * </p>
     *
     * @return true if selected, false otherwise.
     */
    public boolean isSelected() {

        return Boolean.TRUE.equals(getValue());
    }

    /**
     * <p>
     * Set the local value of the selected state of this component. This method is a typesafe alias for
     * <code>setValue()</code>.
     * </p>
     *
     * @param selected The new selected state
     */
    public void setSelected(boolean selected) {
        setValue(selected);
    }

    // ---------------------------------------------------------------- Bindings

    /**
     * <p>
     * Return any {@link ValueExpression} set for <code>value</code> if a {@link ValueExpression} for <code>selected</code>
     * is requested; otherwise, perform the default superclass processing for this method.
     * </p>
     *
     * @param name Name of the attribute or property for which to retrieve a {@link ValueExpression}
     *
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @since 1.2
     */
    @Override
    public ValueExpression getValueExpression(String name) {
        if ("selected".equals(name)) {
            return super.getValueExpression("value");
        }

        return super.getValueExpression(name);
    }

    /**
     * <p>
     * Store any {@link ValueExpression} specified for <code>selected</code> under <code>value</code> instead; otherwise,
     * perform the default superclass processing for this method.
     * </p>
     *
     * @param name Name of the attribute or property for which to set a {@link ValueExpression}
     * @param binding The {@link ValueExpression} to set, or <code>null</code> to remove any currently set
     * {@link ValueExpression}
     *
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @since 1.2
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {
        if ("selected".equals(name)) {
            super.setValueExpression("value", binding);
        } else {
            super.setValueExpression(name, binding);
        }
    }

}
