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

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.validator.Validator;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;



/**
 * <p><strong>ValidatorELTag</strong> is a base class for all JSP custom actions
 * that create and register a <code>Validator</code> instance on the
 * {@link EditableValueHolder} associated with our most immediate surrounding instance
 * of a tag whose implementation class is a subclass of {@link UIComponentTag}.
 * To avoid creating duplicate instances when a page is redisplayed,
 * creation and registration of a {@link Validator} occurs
 * <strong>only</strong> if the corresponding {@link UIComponent} was
 * created (by the owning {@link UIComponentTagBase}) during the execution of the
 * current page.</p>
 *
 * <p>This class must be used as a base class for tag instances that
 * support specific {@link Validator} subclasses.</p>
 *
 * <p>Subclasses of this class must implement the
 * <code>createValidator()</code> method, which creates and returns a
 * {@link Validator} instance.  Any configuration properties that specify
 * the limits to be enforced by this {@link Validator} must have been
 * set by the <code>createValidator()</code> method.  Generally, this occurs
 * by copying corresponding attribute values on the tag instance.</p>
 *
 * <p>This tag creates no output to the page currently being created.  It
 * is used solely for the side effect of {@link Validator} creation.</p>
 *
 */

public abstract class ValidatorELTag extends TagSupport {


    // --------------------------------------------------------- Public Methods


    private static final long serialVersionUID = -4373376368829344328L;

    /**
     * <p>Create a new instance of the specified {@link Validator}
     * class, and register it with the {@link UIComponent} instance associated
     * with our most immediately surrounding {@link UIComponentTagBase} instance, if
     * the {@link UIComponent} instance was created by this execution of the
     * containing JSP page.</p>
     *
     * @throws JspException if a JSP error occurs
     */
    @Override
    public int doStartTag() throws JspException {
        
        // Locate our parent UIComponentTag
        UIComponentClassicTagBase tag =
             UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        if (tag == null) { 
       	    //PENDING i18n
            throw new JspException("Not nested in a UIComponentTag Error for tag with handler class:"+
                    this.getClass().getName());
        }

        // Nothing to do unless this tag created a component
        if (!tag.getCreated()) {
            return (SKIP_BODY);
        }
        
        UIComponent component = tag.getComponentInstance();
        if (component == null) {            
            //PENDING i18n
            throw new JspException("Can't create Component from tag.");
        }
        if (!(component instanceof EditableValueHolder)) {
            // PENDING i18n
            throw new JspException("Not nested in a tag of proper type. Error for tag with handler class:"+
                    this.getClass().getName());
        }

        Validator validator = createValidator();
        
        if (validator == null) {
            // PENDING i18n
            throw new JspException("Can't create class of type:"+
                " javax.faces.validator.Validator.  Validator is null");
        }

        // Register an instance with the appropriate component
        ((EditableValueHolder)component).addValidator(validator);
        
        return (SKIP_BODY);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * <p>Create and return a new {@link Validator} to be registered
     * on our surrounding {@link UIComponent}.</p>
     *
     * @throws JspException if a new instance cannot be created
     *
     * @return the created {@code Validator}
     */
    protected abstract Validator createValidator()
        throws JspException;


}
