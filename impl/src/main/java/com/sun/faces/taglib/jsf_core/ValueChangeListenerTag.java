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

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.webapp.UIComponentClassicTagBase;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * <p>
 * Tag implementation that creates a {@link ValueChangeListener} instance and registers it on the {@link UIComponent}
 * associated with our most immediate surrounding instance of a tag whose implementation class is a subclass of
 * {@link UIComponentClassicTagBase}. This tag creates no output to the page currently being created.
 * </p>
 * <p/>
 * <p>
 * This class may be used directly to implement a generic event handler registration tag (based on the fully qualified
 * Java class name specified by the <code>type</code> attribute), or as a base class for tag instances that support
 * specific {@link ValueChangeListener} subclasses.
 * </p>
 * <p/>
 * <p>
 * Subclasses of this class must implement the <code>createValueChangeListener()</code> method, which creates and
 * returns a {@link ValueChangeListener} instance. Any configuration properties that are required by this
 * {@link ValueChangeListener} instance must have been set by the <code>createValueChangeListener()</code> method.
 * Generally, this occurs by copying corresponding attribute values on the tag instance.
 * </p>
 * <p/>
 * <p>
 * This tag creates no output to the page currently being created. It is used solely for the side effect of
 * {@link ValueChangeListener} creation.
 * </p>
 */

public class ValueChangeListenerTag extends TagSupport {

    // ------------------------------------------------------------- Attributes

    private static final long serialVersionUID = -212845116876281363L;
    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    /**
     * <p>
     * The fully qualified class name of the {@link ValueChangeListener} instance to be created.
     * </p>
     */
    private ValueExpression type = null;

    /**
     * <p>
     * The value expression used to create a listener instance and it is also used to wire up this listener to an
     * {@link ValueChangeListener} property of a JavaBean class.
     * </p>
     */
    private ValueExpression binding = null;

    /**
     * <p>
     * Set the fully qualified class name of the {@link ValueChangeListener} instance to be created.
     *
     * @param type The new class name
     */
    public void setType(ValueExpression type) {

        this.type = type;

    }

    /*
     * <p>Set the value binding expression for this listener.</p>
     *
     * @param binding The new value binding expression
     */
    public void setBinding(ValueExpression binding) {
        this.binding = binding;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * <p>
     * Create a new instance of the specified {@link ValueChangeListener} class, and register it with the
     * {@link UIComponent} instance associated with our most immediately surrounding {@link UIComponentClassicTagBase}
     * instance, if the {@link UIComponent} instance was created by this execution of the containing JSP page.
     * </p>
     *
     * @throws JspException if a JSP error occurs
     */
    @Override
    public int doStartTag() throws JspException {

        // Locate our parent UIComponentTag
        UIComponentClassicTagBase tag = UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        if (tag == null) {
            // Object[] params = {this.getClass().getName()};
            // PENDING(rogerk): do something with params
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NOT_NESTED_IN_FACES_TAG_ERROR_MESSAGE_ID));
        }

        // Nothing to do unless this tag created a component
        if (!tag.getCreated()) {
            return SKIP_BODY;
        }

        UIComponent component = tag.getComponentInstance();
        if (component == null) {
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_COMPONENT_ERROR_MESSAGE_ID));
        }
        if (!(component instanceof EditableValueHolder)) {
            Object[] params = { "valueChangeListener", "jakarta.faces.component.EditableValueHolder" };
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NOT_NESTED_IN_TYPE_TAG_ERROR_MESSAGE_ID, params));
        }

        // If binding is null, type is set and is a literal value,
        // then don't bother wrapping. Just instantiate and
        // set.
        ValueChangeListener listener;
        if (binding == null && type != null && type.isLiteralText()) {
            try {
                listener = (ValueChangeListener) Util.getListenerInstance(type, null);
            } catch (Exception e) {
                throw new JspException(e.getMessage(), e.getCause());
            }
        } else {
            listener = new BindingValueChangeListener(type, binding);
        }

        ((EditableValueHolder) component).addValueChangeListener(listener);

        return SKIP_BODY;

    }

    /**
     * <p>
     * Release references to any acquired resources.
     */
    @Override
    public void release() {

        type = null;

    }

    // ----------------------------------------------------------- Inner Classes

    private static class BindingValueChangeListener implements ValueChangeListener, Serializable {

        private static final long serialVersionUID = -703503904910636450L;
        private ValueExpression type;
        private ValueExpression binding;

        // -------------------------------------------------------- Constructors

        public BindingValueChangeListener(ValueExpression type, ValueExpression binding) {

            this.type = type;
            this.binding = binding;

        }

        // ------------------------------------ Methods from ValueChangeListener

        /**
         * <p>
         * Invoked when the value change described by the specified {@link jakarta.faces.event.ValueChangeEvent} occurs.
         * </p>
         *
         * @param event The {@link jakarta.faces.event.ValueChangeEvent} that has occurred
         * @throws jakarta.faces.event.AbortProcessingException Signal the JavaServer Faces implementation that no further
         * processing on the current event should be performed
         */
        @Override
        public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {

            ValueChangeListener instance = (ValueChangeListener) Util.getListenerInstance(type, binding);
            if (instance != null) {
                instance.processValueChange(event);
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "jsf.core.taglib.action_or_valuechange_listener.null_type_binding",
                            new Object[] { "ValueChangeListener", event.getComponent().getClientId(FacesContext.getCurrentInstance()) });
                }
            }
        }

    }

}
