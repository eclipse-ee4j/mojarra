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

import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.webapp.UIComponentELTag;

import com.sun.faces.util.FacesLogger;

import jakarta.el.ValueExpression;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Tag implementation that creates a {@link PhaseListener} instance and registers it on the {@link UIViewRoot}
 * associated with our most immediate surrounding instance of a tag whose component is an instance of
 * {@link UIViewRoot}. This tag creates no output to the page currently being created.
 * </p>
 * <p/>
 */

public class PhaseListenerTag extends TagSupport {

    private static final long serialVersionUID = -387813302573848228L;

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    // ------------------------------------------------------------- Attributes

    /**
     * <p>
     * The fully qualified class name of the {@link PhaseListener} instance to be created.
     * </p>
     */
    private ValueExpression type = null;

    /**
     * <p>
     * The value binding expression used to create a listener instance and it is also used to wire up this listener to an
     * {@link PhaseListener} property of a JavaBean class.
     * </p>
     */
    private ValueExpression binding = null;

    /**
     * <p>
     * Set the fully qualified class name of the {@link PhaseListener} instance to be created.
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
     * Create a new instance of the specified {@link PhaseListener} class, and register it with the {@link UIComponent}
     * instance associated with our most immediately surrounding {@link UIComponentELTag} instance, if the
     * {@link UIComponent} instance was created by this execution of the containing JSP page.
     * </p>
     *
     * @throws JspException if a JSP error occurs
     */
    @Override
    public int doStartTag() throws JspException {

        // find the viewTag
        Tag parent = this;
        UIComponentELTag tag = null;
        while (null != (parent = parent.getParent())) {
            if (parent instanceof UIComponentELTag) {
                tag = (UIComponentELTag) parent;
            }
        }

        if (tag == null) {
            Object params[] = { this.getClass().getName() };
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NOT_NESTED_IN_FACES_TAG_ERROR_MESSAGE_ID, params));
        }

        // Nothing to do unless this tag created a component
        if (!tag.getCreated()) {
            return SKIP_BODY;
        }

        UIViewRoot viewRoot = (UIViewRoot) tag.getComponentInstance();
        if (viewRoot == null) {
            throw new JspException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_COMPONENT_ERROR_MESSAGE_ID));
        }

        // If binding is null, type is set and is a literal value,
        // then don't bother wrapping. Just instantiate and
        // set.
        PhaseListener listener;
        if (binding == null && type != null && type.isLiteralText()) {
            try {
                listener = (PhaseListener) Util.getListenerInstance(type, null);
            } catch (Exception e) {
                throw new JspException(e.getMessage(), e.getCause());
            }
        } else {
            listener = new BindingPhaseListener(type, binding);
        }
        viewRoot.addPhaseListener(listener);

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

    private static class BindingPhaseListener implements PhaseListener, Serializable {

        private static final long serialVersionUID = -3748298130133609119L;
        private ValueExpression type;
        private ValueExpression binding;

        // -------------------------------------------------------- Constructors

        public BindingPhaseListener(ValueExpression type, ValueExpression binding) {

            this.type = type;
            this.binding = binding;

        }

        // ------------------------------------------ Methods from PhaseListener

        /**
         * <p>
         * Handle a notification that the processing for a particular phase has just been completed.
         * </p>
         */
        @Override
        public void afterPhase(PhaseEvent event) {

            PhaseListener listener = getPhaseListener();
            if (listener != null) {
                listener.afterPhase(event);
            }

        }

        /**
         * <p>
         * Handle a notification that the processing for a particular phase of the request processing lifecycle is about to
         * begin.
         * </p>
         */
        @Override
        public void beforePhase(PhaseEvent event) {

            PhaseListener listener = getPhaseListener();
            if (listener != null) {
                listener.beforePhase(event);
            }

        }

        /**
         * <p>
         * Return the identifier of the request processing phase during which this listener is interested in processing
         * {@link jakarta.faces.event.PhaseEvent} events. Legal values are the singleton instances defined by the
         * {@link jakarta.faces.event.PhaseId} class, including <code>PhaseId.ANY_PHASE</code> to indicate an interest in being
         * notified for all standard phases.
         * </p>
         */
        @Override
        public PhaseId getPhaseId() {

            PhaseListener listener = getPhaseListener();
            if (listener != null) {
                return listener.getPhaseId();
            }

            return null;

        }

        /**
         * <p>
         * Invoked when the value change described by the specified {@link jakarta.faces.event.ValueChangeEvent} occurs.
         * </p>
         *
         * @return a <code>PhaseListener</code> instance
         * @throws jakarta.faces.event.AbortProcessingException Signal the JavaServer Faces implementation that no further
         * processing on the current event should be performed
         */
        public PhaseListener getPhaseListener() throws AbortProcessingException {
            PhaseListener instance = (PhaseListener) Util.getListenerInstance(type, binding);
            if (instance != null) {
                return instance;
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    // PENDING i18n
                    LOGGER.warning("PhaseListener will not be processed - " + "both 'binding' and 'type' are null");
                }
                return null;
            }
        }
    }

}
