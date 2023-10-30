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
import jakarta.faces.view.ViewMetadata;

/**
 * <div class="changed_added_2_3">
 *
 * <p>
 * <strong>UIImportConstants</strong> imports a mapping of all constant field values of the given type in the current
 * view.
 * </p>
 *
 * <p>
 * The {@link jakarta.faces.view.ViewDeclarationLanguage} implementation must cause an instance of this component to be
 * placed in the view for each occurrence of an <code>&lt;f:importConstants /&gt;</code> element placed inside of an
 * <code>&lt;f:metadata /&gt;</code> element. The user must place <code>&lt;f:metadata /&gt;</code> as a direct child of
 * the <code>UIViewRoot</code>. The {@link ViewMetadata#createMetadataView(jakarta.faces.context.FacesContext)} must
 * take care of actual task of importing the constants.
 * </p>
 *
 * <p>
 * Instances of this class participate in the regular Jakarta Faces lifecycle, including on Ajax requests.
 * </p>
 *
 * <p>
 * The purpose of this component is to provide a mapping of all constant field values of the given type in the current
 * view. Constant field values are all <code>public static final</code> fields of the given type. The map key represents
 * the constant field name as <code>String</code>. The map value represents the actual constant field value. This works
 * for classes, interfaces and enums.
 * </p>
 *
 * <h2 id="usage">Usage</h2>
 *
 * <p>
 * The below constant fields:
 * </p>
 *
 * <pre>
 * package com.example;
 *
 * public class Foo {
 *     public static final String FOO1 = "foo1";
 *     public static final String FOO2 = "foo2";
 * }
 * </pre>
 *
 * <pre>
 * package com.example;
 *
 * public interface Bar {
 *     public static final String BAR1 = "bar1";
 *     public static final String BAR2 = "bar2";
 * }
 * </pre>
 *
 * <pre>
 * package com.example;
 *
 * public enum Baz {
 *     BAZ1, BAZ2;
 * }
 * </pre>
 *
 * <p>
 * Can be imported as below:
 * </p>
 *
 * <pre>
 * &lt;f:metadata&gt;
 *     &lt;f:importConstants type="com.example.Foo" /&gt;
 *     &lt;f:importConstants type="com.example.Bar" var="Barrr" /&gt;
 *     &lt;f:importConstants type="com.example.Baz" /&gt;
 * &lt;/f:metadata&gt;
 * </pre>
 *
 * <p>
 * And can be referenced as below:
 * </p>
 *
 * <pre>
 * #{Foo.FOO1}, #{Foo.FOO2}, #{Barrr.BAR1}, #{Barrr.BAR2}, #{Baz.BAZ1}, #{Baz.BAZ2}
 * </pre>
 *
 * <pre>
 * &lt;h:selectOneMenu value="#{bean.baz}" &gt;
 *     &lt;f:selectItems value="#{Baz}" /&gt;
 * &lt;/h:selectOneMenu&gt;
 * </pre>
 *
 * </div>
 *
 * @since 2.3
 */
public class UIImportConstants extends UIComponentBase {

    // ---------------------------------------------------------------------------------------------- Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.ImportConstants";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.ImportConstants";

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {
        type, var;
    }

    // ---------------------------------------------------------------------------------------------------- Constructors

    /**
     * <p>
     * Create a new {@link UIImportConstants} instance with renderer type set to <code>null</code>.
     * </p>
     */
    public UIImportConstants() {
        setRendererType(null);
    }

    // ------------------------------------------------------------------------------------------------------ Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Returns the fully qualified name of the type to import the constant field values for.
     * </p>
     *
     * @return The fully qualified name of the type to import the constant field values for.
     */
    public String getType() {
        return (String) getStateHelper().eval(PropertyKeys.type);
    }

    /**
     * <p>
     * Sets the fully qualified name of the type to import the constant field values for.
     * </p>
     *
     * @param type The fully qualified name of the type to import the constant field values for.
     */
    public void setType(final String type) {
        getStateHelper().put(PropertyKeys.type, type);
    }

    /**
     * <p>
     * Returns name of request scope attribute under which constants will be exposed as a Map.
     * </p>
     *
     * @return Name of request scope attribute under which constants will be exposed as a Map.
     */
    public String getVar() {
        return (String) getStateHelper().eval(PropertyKeys.var);
    }

    /**
     * <p>
     * Sets name of request scope attribute under which constants will be exposed as a Map.
     * </p>
     *
     * @param var Name of request scope attribute under which constants will be exposed as a Map.
     */
    public void setVar(final String var) {
        getStateHelper().put(PropertyKeys.var, var);
    }

    // --------------------------------------------------------------------------------------------- UIComponent Methods

    /**
     * <p>
     * Set the {@link ValueExpression} used to calculate the value for the specified attribute or property name, if any. If
     * a {@link ValueExpression} is set for the <code>var</code> property, throw an illegal argument exception.
     * </p>
     *
     * @throws IllegalArgumentException If <code>name</code> is one of <code>id</code>, <code>parent</code>, or
     * <code>var</code>.
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {
        if (PropertyKeys.var.toString().equals(name)) {
            throw new IllegalArgumentException(name);
        }

        super.setValueExpression(name, binding);
    }

}
