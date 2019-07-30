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


import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * <p>Tag implementation that adds an attribute with a specified name
 * and String value to the component whose tag it is nested inside,
 * if the component does not already contain an attribute with the
 * same name.  This tag creates no output to the page currently
 * being created.</p>
 *
 * @deprecated The Faces implementation must now provide the
 * implementation for this class.
 */

public class AttributeTag extends TagSupport {


    // ---------------------------------------------------------- Static Members


    private static final long serialVersionUID = -7782950243436672334L;


    // ------------------------------------------------------------- Attributes


    /**
     * <p>The name of the attribute to be created, if not already present.
     */
    private String name = null;


    /**
     * <p>Set the attribute name.</p>
     *
     * @param name The new attribute name
     */
    public void setName(String name) {

        this.name = name;

    }


    /**
     * <p>The value to be associated with this attribute, if it is created.</p>
     */
    private String value = null;



    /**
     * <p>Set the attribute value.</p>
     *
     * @param value The new attribute value
     */
    public void setValue(String value) {

        this.value = value;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Register the specified attribute name and value with the
     * {@link UIComponent} instance associated with our most immediately
     * surrounding {@link UIComponentTag} instance, if this {@link UIComponent}
     * does not already have a value for the specified attribute name.</p>
     *
     * @throws JspException if a Jakarta Server Pages error occurs
     */
    @Override
    public int doStartTag() throws JspException {

        // Locate our parent UIComponentTag
        UIComponentClassicTagBase tag =
             UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        if (tag == null) { // PENDING - i18n
            throw new JspException("Not nested in a UIComponentTag");
        }

        // Add this attribute if it is not already defined
        UIComponent component = tag.getComponentInstance();
        if (component == null) { // PENDING - i18n
            throw new JspException("No component associated with UIComponentTag");
        }

        FacesContext context = FacesContext.getCurrentInstance();
        ExpressionFactory exprFactory =
            context.getApplication().getExpressionFactory();
        ELContext elContext = context.getELContext();

        String nameVal = (String) 
                  exprFactory.createValueExpression(elContext, name, String.class)
                      .getValue(elContext);
        Object valueVal =
                exprFactory.createValueExpression(elContext, value, Object.class)
                    .getValue(elContext);

        if (component.getAttributes().get(nameVal) == null) {
            component.getAttributes().put(nameVal, valueVal);
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
    }

}
