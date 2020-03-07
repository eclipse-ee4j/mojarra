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
 * <strong>UIParameter</strong> is a {@link UIComponent} that represents an optionally named configuration parameter for
 * a parent component.
 * </p>
 *
 * <p>
 * Parent components should retrieve the value of a parameter by calling <code>getValue()</code>. In this way, the
 * parameter value can be set directly on the component (via <code>setValue()</code>), or retrieved indirectly via the
 * value binding expression.
 * </p>
 *
 * <p>
 * In some scenarios, it is necessary to provide a parameter name, in addition to the parameter value that is accessible
 * via the <code>getValue()</code> method. {@link jakarta.faces.render.Renderer}s that support parameter names on their
 * nested {@link UIParameter} child components should document their use of this property.
 * </p>
 *
 */

public class UIParameter extends UIComponentBase {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Parameter";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Parameter";

    enum PropertyKeys {
        name, value, disable
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIParameter} instance with default property values.
     * </p>
     */
    public UIParameter() {
        super();
        setRendererType(null);
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Return the optional parameter name for this parameter.
     * </p>
     *
     * @return the name.
     */
    public String getName() {
        return (String) getStateHelper().eval(PropertyKeys.name);
    }

    /**
     * <p>
     * Set the optional parameter name for this parameter.
     * </p>
     *
     * @param name The new parameter name, or <code>null</code> for no name
     */
    public void setName(String name) {
        getStateHelper().put(PropertyKeys.name, name);
    }

    /**
     * <p>
     * Returns the <code>value</code> property of the <code>UIParameter</code>.
     * </p>
     *
     * @return the value.
     */
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    /**
     * <p>
     * Sets the <code>value</code> property of the\ <code>UIParameter</code>.
     * </p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    /**
     * <p class="changed_added_2_0">
     * Return the value of the <code>disable</code> directive for this component. This directive determines whether the
     * parameter value should be disabled by assigning it a null value. If true, the <code>value</code> set on this
     * component is ignored.
     * </p>
     *
     * @return <code>true</code> if disabled, <code>false</code> otherwise.
     * @since 2.0
     */
    public boolean isDisable() {
        return (Boolean) getStateHelper().eval(PropertyKeys.disable, false);
    }

    /**
     * <p>
     * Sets the <code>disable</code> property of the <code>UIParameter</code>.
     * </p>
     *
     * @param disable the value for the disable flag.
     * @since 2.0
     */
    public void setDisable(boolean disable) {
        getStateHelper().put(PropertyKeys.disable, disable);
    }

}
