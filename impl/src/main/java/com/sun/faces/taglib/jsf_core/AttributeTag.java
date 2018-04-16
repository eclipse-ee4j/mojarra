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

package com.sun.faces.taglib.jsf_core;


import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentELTag;
import javax.faces.webapp.UIComponentClassicTagBase;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.sun.faces.util.MessageUtils;


/**
 * <p>Tag implementation that adds an attribute with a specified name
 * and String value to the component whose tag it is nested inside,
 * if the component does not already contain an attribute with the
 * same name.  This tag creates no output to the page currently
 * being created.</p>
 *
 */

public class AttributeTag extends TagSupport {


    // ------------------------------------------------------------- Attributes


    private static final long serialVersionUID = -4058910110356397536L;

    /**
     * <p>The name of the attribute to be created, if not already present.
     */
    private ValueExpression name = null;


    /**
     * <p>Set the attribute name.</p>
     *
     * @param name The new attribute name
     */
    public void setName(ValueExpression name) {

        this.name = name;

    }


    /**
     * <p>The value to be associated with this attribute, if it is created.</p>
     */
    private ValueExpression value = null;



    /**
     * <p>Set the attribute value.</p>
     *
     * @param value The new attribute value
     */
    public void setValue(ValueExpression value) {

        this.value = value;

    }


    // -------------------------------------------------------- Methods from Tag


    /**
     * <p>Register the specified attribute name and value with the
     * {@link UIComponent} instance associated with our most immediately
     * surrounding {@link UIComponentClassicTagBase} instance, if this
     * {@link UIComponent} does not already have a value for the
     * specified attribute name.</p>
     *
     * @exception JspException if a JSP error occurs
     */
    @Override
    public int doStartTag() throws JspException {

        // Locate our parent UIComponentTagBase
        UIComponentClassicTagBase tag =
            UIComponentELTag.getParentUIComponentClassicTagBase(pageContext);
        if (tag == null) {
        	String message = MessageUtils.getExceptionMessageString
        	(MessageUtils.NOT_NESTED_IN_UICOMPONENT_TAG_ERROR_MESSAGE_ID);
        	throw new JspException(message);
        }
        
        // Add this attribute if it is not already defined
        UIComponent component = tag.getComponentInstance();
        if (component == null) {
        	String message = MessageUtils.getExceptionMessageString
        	(MessageUtils.NO_COMPONENT_ASSOCIATED_WITH_UICOMPONENT_TAG_MESSAGE_ID);
        	throw new JspException(message);
        }

        FacesContext context = FacesContext.getCurrentInstance();
        ELContext elContext = context.getELContext();

        String nameVal = null;
        Object valueVal = null;
	boolean isLiteral = false;

        if (name != null) {
            nameVal = (String) name.getValue(elContext);
        }

        if (value != null) {
	    if (isLiteral = value.isLiteralText()) {
		valueVal = value.getValue(elContext);
	    }
        }
	
        if (component.getAttributes().get(nameVal) == null) {
	    if (isLiteral) {
		component.getAttributes().put(nameVal, valueVal);
	    }
	    else {
		component.setValueExpression(nameVal, value);
	    }
        }
        return (SKIP_BODY);

    }

    @Override
    public int doEndTag() throws JspException {
	this.release();
	return (EVAL_PAGE);
    }


    /**
     * <p>Release references to any acquired resources.
     */
    @Override
    public void release() {

        this.name = null;
        this.value = null;

    } // END release

}
