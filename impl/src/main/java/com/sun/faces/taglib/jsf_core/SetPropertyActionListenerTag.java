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

import jakarta.el.ValueExpression;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.sun.faces.util.MessageUtils;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.webapp.UIComponentClassicTagBase;
import jakarta.faces.webapp.UIComponentELTag;

/**
 * <p>
 * Tag implementation that creates a special {@link ActionListener} instance and registers it on the
 * {@link ActionSource} associated with our most immediate surrounding instance of a tag whose implementation class is a
 * subclass of {@link UIComponentClassicTagBase}. This tag creates no output to the page currently being created.
 * </p>
 * <p/>
 * <p>
 * The ActionListener instance created and installed by this tag has the following behavior and contract.
 * </p>
 *
 * <ul>
 *
 * <li>Only create and register the <code>ActionListener</code> instance the first time the component for this tag is
 * created</li>
 *
 * <li>The "target" and "value" tag attributes are ValueExpression instances and are stored unevaluated as instance
 * variables of the listener.</li>
 *
 * <li>When the listener executes, call getValue() on the "value" ValueExpression. Pass the result to a call to
 * setValue() on the "target" ValueExpression</li>
 *
 * </ul>
 *
 * <p>
 * This tag creates no output to the page currently being created. It is used solely for the side effect of
 * {@link ActionListener} creation.
 * </p>
 */

public class SetPropertyActionListenerTag extends TagSupport {

    // ------------------------------------------------------------- Attributes

    static final long serialVersionUID = 7966883942522780374L;

    /**
     * <p>
     * The target of the value attribute.
     * </p>
     */
    private ValueExpression target = null;

    /**
     * <p>
     * The value that is set into the target attribute.
     * </p>
     */
    private ValueExpression value = null;

    /**
     * <p>
     * Setter for the target attribute
     * </p>
     *
     * @param target The new class name
     */
    public void setTarget(ValueExpression target) {

        this.target = target;

    }

    /*
     * <p>Setter for the value attribute</p>
     *
     * @param value The new value value expression
     *
     * @throws JspException if a JSP error occurs
     */
    public void setValue(ValueExpression value) {
        this.value = value;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * <p>
     * Create a new instance of the {@link ActionListener} class, and register it with the {@link UIComponent} instance
     * associated with our most immediately surrounding {@link UIComponentClassicTagBase} instance. The behavior of the
     * {@link ActionListener} must conform to the class description.
     * </p>
     *
     * @throws JspException if a JSP error occurs
     */
    @Override
    public int doStartTag() throws JspException {

        // Locate our parent UIComponentTag
        UIComponentClassicTagBase tag = UIComponentELTag.getParentUIComponentClassicTagBase(pageContext);
        if (tag == null) {
            Object params[] = { this.getClass().getName() };
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NOT_NESTED_IN_FACES_TAG_ERROR_MESSAGE_ID, params));
        }

        // Nothing to do unless this tag created a component
        if (!tag.getCreated()) {
            return (SKIP_BODY);
        }

        UIComponent component = tag.getComponentInstance();
        if (component == null) {
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_COMPONENT_ERROR_MESSAGE_ID));
        }
        if (!(component instanceof ActionSource)) {
            Object params[] = { "setPropertyActionListener", "jakarta.faces.component.ActionSource" };
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NOT_NESTED_IN_TYPE_TAG_ERROR_MESSAGE_ID, params));
        }

        ActionListener handler = new SetPropertyActionListenerImpl(target, value);
        ((ActionSource) component).addActionListener(handler);

        return (SKIP_BODY);

    }

    /**
     * <p>
     * Release references to any acquired resources.
     */
    @Override
    public void release() {

        this.value = null;
        this.target = null;

    }

}
