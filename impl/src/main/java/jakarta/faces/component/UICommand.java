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

import static jakarta.faces.event.PhaseId.APPLY_REQUEST_VALUES;
import static jakarta.faces.event.PhaseId.INVOKE_APPLICATION;

import jakarta.el.MethodExpression;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.render.Renderer;

/**
 * <p>
 * <strong>UICommand</strong> is a {@link UIComponent} that represents a user interface component which, when activated
 * by the user, triggers an application specific "command" or "action". Such a component is typically rendered as a push
 * button, a menu item, or a hyperlink.
 * </p>
 *
 * <p>
 * When the <code>decode()</code> method of this {@link UICommand}, or its corresponding {@link Renderer}, detects that
 * this control has been activated, it will queue an {@link ActionEvent}. Later on, the <code>broadcast()</code> method
 * will ensure that this event is broadcast to all interested listeners.
 * </p>
 *
 * <p>
 * Listeners will be invoked in the following order:
 * <ol>
 * <li>{@link ActionListener}s, in the order in which they were registered.
 * <li>The "actionListener" {@link MethodExpression} (which will cover the "actionListener" that was set as a
 * <code>MethodBinding</code>).
 * <li>The default {@link ActionListener}, retrieved from the {@link Application} - and therefore, any attached "action"
 * {@link MethodExpression}.
 * </ol>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Button</code>". This value can
 * be changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class UICommand extends UIComponentBase implements ActionSource {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Command";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Command";

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {
        value, immediate, methodBindingActionListener, actionExpression,
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UICommand} instance with default property values.
     * </p>
     */
    public UICommand() {
        super();
        setRendererType("jakarta.faces.Button");
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    // ------------------------------------------------- ActionSource Properties

    /**
     * <p>
     * The immediate flag.
     * </p>
     */
    @Override
    public boolean isImmediate() {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);
    }

    @Override
    public void setImmediate(boolean immediate) {
        getStateHelper().put(PropertyKeys.immediate, immediate);
    }

    /**
     * <p>
     * Returns the <code>value</code> property of the <code>UICommand</code>. This is most often rendered as a label.
     * </p>
     *
     * @return The object representing the value of this component.
     */
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    /**
     * <p>
     * Sets the <code>value</code> property of the <code>UICommand</code>. This is most often rendered as a label.
     * </p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    // ---------------------------------------------------- ActionSource Methods

    @Override
    public MethodExpression getActionExpression() {
        return (MethodExpression) getStateHelper().get(PropertyKeys.actionExpression);
    }

    @Override
    public void setActionExpression(MethodExpression actionExpression) {
        getStateHelper().put(PropertyKeys.actionExpression, actionExpression);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void addActionListener(ActionListener listener) {
        addFacesListener(listener);
    }

    @Override
    public ActionListener[] getActionListeners() {
        return (ActionListener[]) getFacesListeners(ActionListener.class);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void removeActionListener(ActionListener listener) {
        removeFacesListener(listener);
    }

    // ----------------------------------------------------- UIComponent Methods

    /**
     * <p>
     * In addition to to the default {@link UIComponent#broadcast} processing, pass the {@link ActionEvent} being broadcast
     * to the method referenced by <code>actionListener</code> (if any), and to the default {@link ActionListener}
     * registered on the {@link jakarta.faces.application.Application}.
     * </p>
     *
     * @param event {@link FacesEvent} to be broadcast
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     * @throws IllegalArgumentException if the implementation class of this {@link FacesEvent} is not supported by this
     * component
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     */
    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {

        // Perform standard superclass processing (including calling our
        // ActionListeners)
        super.broadcast(event);

        if (event instanceof ActionEvent) {
            FacesContext context = event.getFacesContext();

            // Invoke the default ActionListener
            ActionListener listener = context.getApplication().getActionListener();
            if (listener != null) {
                listener.processAction((ActionEvent) event);
            }
        }
    }

    /**
     *
     * <p>
     * Intercept <code>queueEvent</code> and take the following action. If the event is an <code>{@link ActionEvent}</code>,
     * obtain the <code>UIComponent</code> instance from the event. If the component is an <code>{@link ActionSource}</code>
     * obtain the value of its "immediate" property. If it is true, mark the phaseId for the event to be
     * <code>PhaseId.APPLY_REQUEST_VALUES</code> otherwise, mark the phaseId to be <code>PhaseId.INVOKE_APPLICATION</code>.
     * The event must be passed on to <code>super.queueEvent()</code> before returning from this method.
     * </p>
     *
     */
    @Override
    public void queueEvent(FacesEvent event) {
        UIComponent component = event.getComponent();

        if (event instanceof ActionEvent && component instanceof ActionSource) {
            if (((ActionSource) component).isImmediate()) {
                event.setPhaseId(APPLY_REQUEST_VALUES);
            } else {
                event.setPhaseId(INVOKE_APPLICATION);
            }
        }

        super.queueEvent(event);
    }

}
