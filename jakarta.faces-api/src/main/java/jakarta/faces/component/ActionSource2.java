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

/**
 * <p>
 * <strong>ActionSource2</strong> extends {@link ActionSource} and provides a JavaBeans
 * "<code>action</code>" property. The type of this property is a {@link MethodExpression}.
 * This allows the <code>ActionSource</code> concept to leverage the Jakarta Expression Language API.
 * </p>
 *
 * @since 1.2
 * @deprecated Use {@link ActionSource} instead.
 */
@Deprecated(since = "4.1", forRemoval = true)
public interface ActionSource2 extends ActionSource {

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the {@link MethodExpression} pointing at the application action to be invoked, if this {@link UIComponent} is
     * activated by the user, during the <em>Apply Request Values</em> or <em>Invoke Application</em> phase of the request
     * processing lifecycle, depending on the value of the <code>immediate</code> property.
     * </p>
     *
     * @return the action expression.
     */
    MethodExpression getActionExpression();

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
     * @param action The new method expression
     */
    void setActionExpression(MethodExpression action);

}
