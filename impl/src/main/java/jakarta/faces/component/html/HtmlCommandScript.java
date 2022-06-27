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

import jakarta.faces.component.UICommand;

/**
 * <p>
 * Represents an HTML <code>script</code> element for a function that acts like an ajax form submit. This component must
 * be placed inside a form, and requires JavaScript to be enabled in the client.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Script</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class HtmlCommandScript extends UICommand {

    public HtmlCommandScript() {
        super();
        setRendererType("jakarta.faces.Script");
    }

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.HtmlCommandScript";

    /**
     * The property keys.
     */
    protected enum PropertyKeys {
        autorun, execute, name, onerror, onevent, render, resetValues,;

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
     * Return the value of the <code>autorun</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Whether to execute declared JavaScript function during <code>load</code> event of the <code>window</code>.
     * Defaults to <code>false</code>.
     */
    public boolean isAutorun() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.autorun, false);

    }

    /**
     * <p>
     * Set the value of the <code>autorun</code> property.
     * </p>
     *
     * @param autorun the new property value
     */
    public void setAutorun(boolean autorun) {
        getStateHelper().put(PropertyKeys.autorun, autorun);
    }

    /**
     * <p>
     * Return the value of the <code>execute</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: This is a space separated list of client identifiers of components that will participate in the "execute"
     * portion of the Request Processing Lifecycle. If a literal is specified the identifiers must be space delimited. Any
     * of the keywords "@this", "@form", "@all", "@none" may be specified in the identifier list. If not specified, the
     * default value of "@this" is assumed. For example, <code>@this clientIdOne clientIdTwo</code>.
     */
    public java.lang.String getExecute() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.execute);

    }

    /**
     * <p>
     * Set the value of the <code>execute</code> property.
     * </p>
     *
     * @param execute the new property value
     */
    public void setExecute(java.lang.String execute) {
        getStateHelper().put(PropertyKeys.execute, execute);
    }

    /**
     * <p>
     * Return the value of the <code>name</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Name of JavaScript function to be declared, e.g. <code>name="functionName"</code>. This can be a namespaced
     * function name, e.g. <code>name="ez.functionName"</code>.
     */
    public java.lang.String getName() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.name);

    }

    /**
     * <p>
     * Set the value of the <code>name</code> property.
     * </p>
     *
     * @param name the new property value
     */
    public void setName(java.lang.String name) {
        getStateHelper().put(PropertyKeys.name, name);
    }

    /**
     * <p>
     * Return the value of the <code>onerror</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The name of the JavaScript function that will handle errors.
     */
    public java.lang.String getOnerror() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onerror);

    }

    /**
     * <p>
     * Set the value of the <code>onerror</code> property.
     * </p>
     *
     * @param onerror the new property value
     */
    public void setOnerror(java.lang.String onerror) {
        getStateHelper().put(PropertyKeys.onerror, onerror);
    }

    /**
     * <p>
     * Return the value of the <code>onevent</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: The name of the JavaScript function that will handle UI events.
     */
    public java.lang.String getOnevent() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.onevent);

    }

    /**
     * <p>
     * Set the value of the <code>onevent</code> property.
     * </p>
     *
     * @param onevent the new property value
     */
    public void setOnevent(java.lang.String onevent) {
        getStateHelper().put(PropertyKeys.onevent, onevent);
    }

    /**
     * <p>
     * Return the value of the <code>render</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: This is a space separated list of client identifiers of components that will participate in the "render"
     * portion of the Request Processing Lifecycle. If a literal is specified the identifiers must be space delimited. Any
     * of the keywords "@this", "@form", "@all", "@none" may be specified in the identifier list. If not specified, the
     * default value of "@none" is assumed. For example, <code>@this clientIdOne clientIdTwo</code>.
     */
    public java.lang.String getRender() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.render);

    }

    /**
     * <p>
     * Set the value of the <code>render</code> property.
     * </p>
     *
     * @param render the new property value
     */
    public void setRender(java.lang.String render) {
        getStateHelper().put(PropertyKeys.render, render);
    }

    /**
     * <p>
     * Return the value of the <code>resetValues</code> property.
     * </p>
     *
     * @return the property value
     * <p>
     * Contents: Reset specific input values. Interpret the value of the <code>render</code> attribute as a space separated
     * list of client identifiers suitable for passing directly to <code>UIViewRoot.resetValues()</code>. The implementation
     * must cause an <code>ActionListener</code> to be attached to the <code>ActionSource</code> component in which this tag
     * is nested that calls <code>UIViewRoot.resetValues()</code> passing the value of the <code>render</code> attribute as
     * the argument.
     */
    public java.lang.Boolean getResetValues() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.resetValues);

    }

    /**
     * <p>
     * Set the value of the <code>resetValues</code> property.
     * </p>
     *
     * @param resetValues the new property value
     */
    public void setResetValues(java.lang.Boolean resetValues) {
        getStateHelper().put(PropertyKeys.resetValues, resetValues);
    }

}
