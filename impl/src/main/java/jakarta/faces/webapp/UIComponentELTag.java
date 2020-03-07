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

package jakarta.faces.webapp;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

/**
 * <p>
 * {@link UIComponentELTag} specializes its superclass to allow for properties that take their values from Jakarta
 * Expression Language expressions.
 * </p>
 *
 * <p>
 * This tag is designed for use with Faces version 1.2 and Jakarta Server Pages version 2.1 containers.
 * </p>
 *
 */

public abstract class UIComponentELTag extends UIComponentClassicTagBase implements Tag {

    // ------------------------------------------------------------- Attributes

    /**
     * <p>
     * The value binding expression (if any) used to wire up this component to a {@link UIComponent} property of a JavaBean
     * class.
     * </p>
     */
    private ValueExpression binding = null;

    /**
     * <p>
     * Set the value expression for our component.
     * </p>
     *
     * @param binding The new value expression
     *
     * @throws JspException if an error occurs
     */
    public void setBinding(ValueExpression binding) throws JspException {
        this.binding = binding;
    }

    @Override
    protected boolean hasBinding() {
        return null != binding;
    }

    /**
     * <p>
     * An override for the rendered attribute associated with our {@link UIComponent}.
     * </p>
     */
    private ValueExpression rendered = null;

    /**
     * <p>
     * Set an override for the rendered attribute.
     * </p>
     *
     * @param rendered The new value for rendered attribute
     */
    public void setRendered(ValueExpression rendered) {
        this.rendered = rendered;
    }

    /**
     * <p>
     * Return the {@link ELContext} for the {@link FacesContext} for this request.
     * </p>
     *
     * <p>
     * This is a convenience for <code>getFacesContext().getELContext()</code>.
     * </p>
     */

    @Override
    protected ELContext getELContext() {
        FacesContext fc = getFacesContext();
        ELContext result = null;
        if (null != fc) {
            result = fc.getELContext();
        }
        return result;
    }

    // ------------------------------------------------------------ Tag Methods

    /**
     * <p>
     * Release any resources allocated during the execution of this tag handler.
     * </p>
     */
    @Override
    public void release() {

        binding = null;
        rendered = null;
        super.release();
    }

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>
     * Override properties and attributes of the specified component, if the corresponding properties of this tag handler
     * instance were explicitly set. This method must be called <strong>ONLY</strong> if the specified {@link UIComponent}
     * was in fact created during the execution of this tag handler instance, and this call will occur
     * <strong>BEFORE</strong> the {@link UIComponent} is added to the view.
     * </p>
     *
     * <p>
     * Tag subclasses that want to support additional set properties must ensure that the base class
     * <code>setProperties()</code> method is still called. A typical implementation that supports extra properties
     * <code>foo</code> and <code>bar</code> would look something like this:
     * </p>
     *
     * <pre>
     * protected void setProperties(UIComponent component) {
     *     super.setProperties(component);
     *     if (foo != null) {
     *         component.setAttribute("foo", foo);
     *     }
     *     if (bar != null) {
     *         component.setAttribute("bar", bar);
     *     }
     * }
     * </pre>
     *
     * <p>
     * The default implementation overrides the following properties:
     * </p>
     * <ul>
     * <li><code>rendered</code> - Set if a value for the <code>rendered</code> property is specified for this tag handler
     * instance.</li>
     * <li><code>rendererType</code> - Set if the <code>getRendererType()</code> method returns a non-null value.</li>
     * </ul>
     *
     * @param component {@link UIComponent} whose properties are to be overridden
     */
    @Override
    protected void setProperties(UIComponent component) {
        // The "id" property is explicitly set when components are created
        // so it does not need to be set here
        if (rendered != null) {
            if (rendered.isLiteralText()) {
                try {
                    component.setRendered(Boolean.valueOf(rendered.getExpressionString()).booleanValue());
                } catch (ELException e) {
                    throw new FacesException(e);
                }
            } else {
                component.setValueExpression("rendered", rendered);
            }
        }
        if (getRendererType() != null) {
            component.setRendererType(getRendererType());
        }

    }

    /**
     * <p>
     * Create and return a new child component of the type returned by calling <code>getComponentType()</code>. If this
     * {@link UIComponentELTag} has a non-null <code>binding</code> attribute, this is done by call
     * {@link Application#createComponent} with the {@link ValueExpression} created for the <code>binding</code> attribute,
     * and the {@link ValueExpression} will be stored on the component. Otherwise, {@link Application#createComponent} is
     * called with only the component type. Finally, initialize the components id and other properties.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param newId id of the component
     */
    @Override
    protected UIComponent createComponent(FacesContext context, String newId) throws JspException {
        UIComponent component;
        Application application = context.getApplication();
        if (binding != null) {
            component = application.createComponent(binding, context, getComponentType());
            component.setValueExpression("binding", binding);
        } else {
            component = application.createComponent(getComponentType());
        }

        component.setId(newId);
        setProperties(component);

        return component;
    }

}
