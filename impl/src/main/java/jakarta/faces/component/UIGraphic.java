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
 * <strong>UIGraphic</strong> is a {@link UIComponent} that displays a graphical image to the user. The user cannot
 * manipulate this component; it is for display purposes only.
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Image</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class UIGraphic extends UIComponentBase {

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {

        /**
         * <p>
         * The local value of this {@link UIComponent}.
         * </p>
         */
        value
    }

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Graphic";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Graphic";

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIGraphic} instance with default property values.
     * </p>
     */
    public UIGraphic() {
        super();
        setRendererType("jakarta.faces.Image");
    }

    // ------------------------------------------------------ Instance Variables

    // private Object value = null;

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Return the image URL for this {@link UIGraphic}. This method is a typesafe alias for <code>getValue()</code>.
     * </p>
     *
     * @return the url.
     */
    public String getUrl() {
        return (String) getValue();
    }

    /**
     * <p>
     * Set the image URL for this {@link UIGraphic}. This method is a typesafe alias for <code>setValue()</code>.
     * </p>
     *
     * @param url The new image URL
     */
    public void setUrl(String url) {
        setValue(url);
    }

    /**
     * <p>
     * Returns the <code>value</code> property of the <code>UIGraphic</code>. This will typically be rendered as an URL.
     * </p>
     *
     * @return the value.
     */
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    /**
     * <p>
     * Sets the <code>value</code> property of the <code>UIGraphic</code>. This will typically be rendered as an URL.
     * </p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    // ---------------------------------------------------------------- Bindings


    /**
     * <p>
     * Return any {@link ValueExpression} set for <code>value</code> if a {@link ValueExpression} for <code>url</code> is
     * requested; otherwise, perform the default superclass processing for this method.
     * </p>
     *
     * @param name Name of the attribute or property for which to retrieve a {@link ValueExpression}
     * @return the value expression, or <code>null</code>.
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @since 1.2
     */
    @Override
    public ValueExpression getValueExpression(String name) {
        if ("url".equals(name)) {
            return super.getValueExpression("value");
        } else {
            return super.getValueExpression(name);
        }
    }

    /**
     * <p>
     * Store any {@link ValueExpression} specified for <code>url</code> under <code>value</code> instead; otherwise, perform
     * the default superclass processing for this method.
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
        if ("url".equals(name)) {
            super.setValueExpression("value", binding);
        } else {
            super.setValueExpression(name, binding);
        }
    }

    // ----------------------------------------------------- StateHolder Methods

}
