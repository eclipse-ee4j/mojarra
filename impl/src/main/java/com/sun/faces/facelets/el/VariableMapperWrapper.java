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

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for wrapping another VariableMapper with a new context,
 * represented by a {@link java.util.Map Map}. Modifications occur to the Map
 * instance, but resolve against the wrapped VariableMapper if the Map doesn't
 * contain the ValueExpression requested.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class VariableMapperWrapper extends VariableMapper {

    private final VariableMapper target;

    private Map vars;

    /**
     *
     */
    public VariableMapperWrapper(VariableMapper orig) {
        super();
        this.target = orig;
    }

    /**
     * First tries to resolve agains the inner Map, then the wrapped
     * ValueExpression.
     *
     * @see jakarta.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String variable) {
        ValueExpression ve = null;
        try {
            if (this.vars != null) {
                ve = (ValueExpression) this.vars.get(variable);
            }
            if (ve == null) {
                return this.target.resolveVariable(variable);
            }
            return ve;
        } catch (StackOverflowError e) {
            throw new ELException("Could not Resolve Variable [Overflow]: "
                                  + variable, e);
        }
    }

    /**
     * Set the ValueExpression on the inner Map instance.
     *
     * @see jakarta.el.VariableMapper#setVariable(java.lang.String,
     *      jakarta.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String variable,
                                       ValueExpression expression) {
        if (this.vars == null) {
            this.vars = new HashMap();
        }
        return (ValueExpression) this.vars.put(variable, expression);
	}
}
