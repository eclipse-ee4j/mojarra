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

package com.sun.faces.facelets.el;

import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 * Composite VariableMapper that attempts to load the ValueExpression from the first
 * VariableMapper, then the second if <code>null</code>.
 * 
 * @see javax.el.VariableMapper
 * @see javax.el.ValueExpression
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CompositeVariableMapper extends VariableMapper {

    private final VariableMapper var0;

    private final VariableMapper var1;

    public CompositeVariableMapper(VariableMapper var0, VariableMapper var1) {
        this.var0 = var0;
        this.var1 = var1;
    }

    /**
     * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String name) {
        ValueExpression ve = this.var0.resolveVariable(name);
        if (ve == null) {
            return this.var1.resolveVariable(name);
        }
        return ve;
    }

    /**
     * @see javax.el.VariableMapper#setVariable(java.lang.String, javax.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String name, ValueExpression expression) {
        return this.var0.setVariable(name, expression);
    }

}
