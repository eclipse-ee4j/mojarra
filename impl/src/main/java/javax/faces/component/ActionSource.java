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

package javax.faces.component;

import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * <p>
 * <strong>ActionSource</strong> is an interface that may be implemented by any concrete
 * {@link UIComponent} that wishes to be a source of {@link ActionEvent}s, including the ability to
 * invoke application actions via the default {@link ActionListener} mechanism.
 * </p>
 */
public interface ActionSource {

    // -------------------------------------------------------------- Properties
   
    /**
     * <p>
     * Return a flag indicating that the default {@link ActionListener} provided by the Jakarta Server
     * Faces implementation should be executed immediately (that is, during <em>Apply Request
     * Values</em> phase of the request processing lifecycle), rather than waiting until the
     * <em>Invoke Application</em> phase. The default value for this property must be
     * <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> if immediate, <code>false</code> otherwise.
     */
    public boolean isImmediate();

    /**
     * <p>
     * Set the "immediate execution" flag for this {@link UIComponent}.
     * </p>
     *
     * @param immediate The new immediate execution flag
     */
    public void setImmediate(boolean immediate);
    

    // -------------------------------------------------- Event Listener Methods

    /**
     * <p>
     * Add a new {@link ActionListener} to the set of listeners interested in being notified when
     * {@link ActionEvent}s occur.
     * </p>
     *
     * @param listener The {@link ActionListener} to be added
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    public void addActionListener(ActionListener listener);

    /**
     * <p>
     * Return the set of registered {@link ActionListener}s for this {@link ActionSource} instance.
     * If there are no registered listeners, a zero-length array is returned.
     * </p>
     * 
     * @return the action listeners, or a zero-length array.
     */
    public abstract ActionListener[] getActionListeners();

    /**
     * <p>
     * Remove an existing {@link ActionListener} (if any) from the set of listeners interested in
     * being notified when {@link ActionEvent}s occur.
     * </p>
     *
     * @param listener The {@link ActionListener} to be removed
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    public void removeActionListener(ActionListener listener);
    
    
    
    
    
    
    
    // -------------------------------------------------------------- Deprecated methods
    
    
    /**
     * <p>
     * If the implementing class also implements {@link ActionSource2}, the implementation of this
     * method must call through to {@link ActionSource2#getActionExpression} and examine the result.
     * If the result came from a previous call to {@link #setAction}, extract the
     * <code>MethodBinding</code> from it and return it. Otherwise, wrap the returned
     * {@link javax.el.MethodExpression} in a <code>MethodBinding</code> implementation, and return
     * it.
     * </p>
     * 
     * <p>
     * If the implementing class does not implement <code>ActionSource2</code>, return the
     * {@link MethodBinding}pointing at the application action to be invoked, if this
     * {@link UIComponent} is activated by the user, during the <em>Apply Request Values</em> or
     * <em>Invoke Application</em> phase of the request processing lifecycle, depending on the value
     * of the <code>immediate</code> property.
     * </p>
     *
     * @deprecated This has been replaced by {@link ActionSource2#getActionExpression}.
     * 
     * @return the action.
     */
    public MethodBinding getAction();

    /**
     * <p>
     * If the implementing class also implements {@link ActionSource2}, the implementation of this
     * method must wrap the argument <code>action</code> in a class that implements
     * {@link javax.el.MethodExpression} and call through to
     * {@link ActionSource2#setActionExpression}, passing the wrapped <code>action</code>.
     * </p>
     *
     * <p>
     * If the implementing class does not implement <code>ActionSource2</code>, set the
     * {@link MethodBinding} pointing at the appication action to be invoked, if this
     * {@link UIComponent} is activated by the user, during the <em>Apply Request Values</em> or
     * <em>Invoke Application</em> phase of the request processing lifecycle, depending on the value
     * of the <code>immediate</code> property.
     * </p>
     *
     * <p>
     * Any method referenced by such an expression must be public, with a return type of
     * <code>String</code>, and accept no parameters.
     * </p>
     *
     * @param action The new MethodBinding expression
     *
     * @deprecated This has been replaced by
     *             {@link ActionSource2#setActionExpression(javax.el.MethodExpression)}.
     */
    public void setAction(MethodBinding action);

    /**
     * <p>
     * If {@link #setActionListener} was not previously called for this instance, this method must
     * return <code>null</code>. If it was called, this method must return the exact
     * <code>MethodBinding</code> instance that was passed to {@link #setActionListener}.
     * </p>
     *
     * <p>
     * The method to be invoked, if this {@link UIComponent} is activated by the user, will be
     * called during the <em>Apply Request Values</em> or <em>Invoke Application</em> phase of the
     * request processing lifecycle, depending upon the value of the <code>immediate</code>
     * property.
     * </p>
     *
     * @return the action listener.
     * @deprecated Use {@link #getActionListeners} instead.
     */
    public MethodBinding getActionListener();

    /**
     * <p>
     * Wrap the argument <code>actionListener</code> in an implementation of {@link ActionListener}
     * and store it in the internal data structure that backs the {@link #getActionListeners}
     * method, taking care to over-write any instance that was stored by a previous call to
     * <code>setActionListener</code>.
     * </p>
     *
     * <p>
     * Any method referenced by such an expression must be public, with a return type of
     * <code>void</code>, and accept a single parameter of type <code>ActionEvent</code>.
     * </p>
     *
     * @param actionListener The new method binding expression
     *
     * @deprecated This has been replaced by
     *             {@link #addActionListener(javax.faces.event.ActionListener)}.
     */
    public void setActionListener(MethodBinding actionListener);


}
