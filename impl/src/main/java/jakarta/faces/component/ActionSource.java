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

import jakarta.el.MethodExpression;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

/**
 * <p>
 * <strong>ActionSource</strong> is an interface that may be implemented by any concrete {@link UIComponent} that wishes
 * to be a source of {@link ActionEvent}s, including the ability to invoke application actions via the default
 * {@link ActionListener} mechanism.
 * </p>
 */
public interface ActionSource {

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return a flag indicating that the default {@link ActionListener} provided by the Jakarta Faces implementation
     * should be executed immediately (that is, during <em>Apply Request Values</em> phase of the request processing
     * lifecycle), rather than waiting until the <em>Invoke Application</em> phase. The default value for this property must
     * be <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if immediate, <code>false</code> otherwise.
     */
    boolean isImmediate();

    /**
     * <p>
     * Set the "immediate execution" flag for this {@link UIComponent}.
     * </p>
     *
     * @param immediate The new immediate execution flag
     */
    void setImmediate(boolean immediate);

    // -------------------------------------------------- Event Listener Methods

    /**
     * <p>
     * Add a new {@link ActionListener} to the set of listeners interested in being notified when {@link ActionEvent}s
     * occur.
     * </p>
     *
     * @param listener The {@link ActionListener} to be added
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    void addActionListener(ActionListener listener);

    /**
     * <p>
     * Return the set of registered {@link ActionListener}s for this {@link ActionSource} instance. If there are no
     * registered listeners, a zero-length array is returned.
     * </p>
     *
     * @return the action listeners, or a zero-length array.
     */
    ActionListener[] getActionListeners();

    /**
     * <p>
     * Remove an existing {@link ActionListener} (if any) from the set of listeners interested in being notified when
     * {@link ActionEvent}s occur.
     * </p>
     *
     * @param listener The {@link ActionListener} to be removed
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    void removeActionListener(ActionListener listener);

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the {@link MethodExpression} pointing at the application action to be invoked, if this {@link UIComponent} is
     * activated by the user, during the <em>Apply Request Values</em> or <em>Invoke Application</em> phase of the request
     * processing lifecycle, depending on the value of the <code>immediate</code> property.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend {@link ActionSource}.
     * Historically this method was declared in {@code ActionSource2} for precisely this reason but default methods in interfaces weren't supported. 
     * </p>
     *
     * @return the action expression.
     * @since 4.1
     */
    default MethodExpression getActionExpression() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Set the {@link MethodExpression} pointing at the appication action to be invoked, if this {@link UIComponent} is
     * activated by the user, during the <em>Apply Request Values</em> or <em>Invoke Application</em> phase of the request
     * processing lifecycle, depending on the value of the <code>immediate</code> property.
     * </p>
     *
     * <p>
     * Any method referenced by such an expression must be public, with a return type of <code>String</code>, and accept no
     * parameters.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend {@link ActionSource}.
     * Historically this method was declared in {@code ActionSource2} for precisely this reason but default methods in interfaces weren't supported. 
     * </p>
     *
     * @param action The new method expression
     * @since 4.1
     */
    default void setActionExpression(MethodExpression action) {
        throw new UnsupportedOperationException();
    }

}
