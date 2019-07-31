/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.webapp;


import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;


/**
 * <p>{@link UIComponentTag} is the base class for all Jakarta Server Pages custom
 * actions that correspond to user interface components in a page that is
 * rendered by Jakarta Server Faces.</p>
 *
 * <p>In this version of the specification, <code>UIComponentTag</code>
 * extends {@link UIComponentClassicTagBase} to add properties that use
 * the Faces 1.1 Expression Language.</p>
 *
 * @deprecated Use of this class has been replaced with {@link
 * UIComponentELTag}, which extends
 * <code>UIComponentClassicTagBase</code> to add properties that use the
 * Jakarta Expression Language API introduced as part of Jakarta Server Pages 2.1.
 */

public abstract class UIComponentTag extends UIComponentClassicTagBase implements Tag {

    // ------------------------------------------------------------- Properties

    /**
     * <p>The value binding expression (if any) used to wire up this component
     * to a {@link UIComponent} property of a JavaBean class.</p>
     */
    private String binding = null;


    /**
     * <p>Set the value binding expression for our component.</p>
     *
     * @param binding The new value binding expression
     *
     * @throws IllegalArgumentException if the specified binding is not a
     * valid value binding expression.
     *
     * @throws JspException if the binding cannot be set
     */
    public void setBinding(String binding) throws JspException {
	if (!isValueReference(binding)) {
	    throw new IllegalArgumentException();
        }

	this.binding = binding;
    }

    @Override
    protected boolean hasBinding() {
	return null != binding;
    }



    /**
     * <p>An override for the rendered attribute associated with our
     * {@link UIComponent}.</p>
     */
    private String rendered = null;


    /**
     * <p>Set an override for the rendered attribute.</p>
     *
     * @param rendered The new value for rendered attribute
     */
    public void setRendered(String rendered) {

        this.rendered = rendered;

    }


    /**
     * <p>Flag indicating whether or not rendering should occur.</p>
     */
    private boolean suppressed = false;


    protected boolean isSuppressed() {

        return (suppressed);

    }


    /**
     * <p>Return <code>true</code> if the specified value conforms to the
     * syntax requirements of a value binding expression.  Such expressions
`    * may be used on most component tag attributes to signal a desire for
     * deferred evaluation of the attribute or property value to be set on
     * the underlying {@link UIComponent}.</p>
     *
     * @param value The value to evaluate
     *
     * @throws NullPointerException if <code>value</code> is
     *  <code>null</code>
     *
     * @return whether or not this value has the correct syntax for a
     * value binding expression
     */
    public static boolean isValueReference(String value) {

	if (value == null) {
	    throw new NullPointerException();
	}
	int start = value.indexOf("#{");
	if ((start != -1) && (start < value.indexOf('}', start))) {
            return true;
        }
        return false;

    }

    // ------------------------------------------ Methods from Tag

    /**
     * <p>Release any resources allocated during the execution of this
     * tag handler.</p>
     */
    @Override
    public void release() {
	
	this.suppressed = false;
	this.binding = null;
        this.rendered = null;
	super.release();
    }


    // ----------------  Concrete Implementations of methods from superclass

    /**
     * @param component {@inheritDoc} 
     */
    @Override
    protected void setProperties(UIComponent component) {
        // The "id" property is explicitly set when components are created
        // so it does not need to be set here
        if (rendered != null) {
	    if (isValueReference(rendered)) {
		ValueBinding vb =
		    getFacesContext().getApplication().createValueBinding(rendered);
		component.setValueBinding("rendered", vb);
	    } else {
		component.setRendered(Boolean.valueOf(rendered).booleanValue());
	    }
        }
	if (getRendererType() != null) {
	    component.setRendererType(getRendererType());
	}

    }





    /**
     * <p>Implement <code>createComponent</code> using Faces 1.1 EL
     * API.</p>
     * 
     * @param context {@inheritDoc} 
     * @param newId {@inheritDoc}
     */
    @Override
    protected UIComponent createComponent(FacesContext context, String newId) {
        UIComponent component;
        Application application = context.getApplication();
        if (binding != null) {
            ValueBinding vb = application.createValueBinding(binding);
            component = application.createComponent(vb, context,
                                                    getComponentType());
	    component.setValueBinding("binding", vb);
        } else {
            component = application.createComponent(getComponentType());
        }

        component.setId(newId);
        setProperties(component);

        return component;
    }


    // Tag tree navigation

    /**
     * <p>Locate and return the nearest enclosing {@link UIComponentTag}
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param context <code>PageContext</code> for the current page
     *
     * @return the parent tag
     */
    public static UIComponentTag getParentUIComponentTag(PageContext context) {

        UIComponentClassicTagBase result =
             getParentUIComponentClassicTagBase(context);
        if (!(result instanceof UIComponentTag)) {
            return new UIComponentTagAdapter(result);
        }
        return ((UIComponentTag) result);

    }


    // --------------------------------------------------------- Private Classes


    /**
     * This adatper exposes a UIComponentClassicTagBase as a UIComponentTag
     * for 1.1 component libraries that rely on UIComponent.getParentUIComponentTag().
     *
     * This will work for most use cases, but there are probably some edge
     * cases out there that we're not aware of.
     */
    private static class UIComponentTagAdapter extends UIComponentTag {

        UIComponentClassicTagBase classicDelegate;

        public UIComponentTagAdapter(UIComponentClassicTagBase classicDelegate) {

            this.classicDelegate = classicDelegate;

        }

        @Override
        public String getComponentType() {
            return classicDelegate.getComponentType();
        }

        @Override
        public String getRendererType() {
            return classicDelegate.getRendererType();
        }

        @Override
        public int doStartTag() throws JspException {
            throw new IllegalStateException();
        }

        @Override
        public int doEndTag() throws JspException {
            throw new IllegalStateException();
        }

        @Override
        public UIComponent getComponentInstance() {
            return classicDelegate.getComponentInstance();
        }

        @Override
        public boolean getCreated() { // NOPMD
            return classicDelegate.getCreated();
        }

        @Override
        public Tag getParent() {
            return classicDelegate.getParent();
        }

        @Override
        public void setParent(Tag parent) {
            throw new IllegalStateException();
        }
                
    }


}
