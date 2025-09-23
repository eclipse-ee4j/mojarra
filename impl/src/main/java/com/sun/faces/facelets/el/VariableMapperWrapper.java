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

package com.sun.faces.facelets.el;

import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

/**
 * Utility class for wrapping another VariableMapper with a new context, represented by a {@link java.util.Map Map}.
 * Modifications occur to the Map instance, but resolve against the wrapped VariableMapper if the Map doesn't contain
 * the ValueExpression requested.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class VariableMapperWrapper extends VariableMapper {

    private final VariableMapper target;

    private Map<String,ValueExpression> vars;

    /**
     * @param orig the original variable mapper to be wrapped
     */
    public VariableMapperWrapper(VariableMapper orig) {
        super();
        target = orig;
    }

    /**
     * First tries to resolve against the inner Map, then the wrapped ValueExpression.
     *
     * @see jakarta.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String variable) {
        try {
            // note that vars may contain null values so we can't use vars.getOrDefault
            final ValueExpression ve = (vars != null ? vars.get(variable) : null);
            return ve != null ? ve : target.resolveVariable(variable);
        }
        catch (StackOverflowError e) {
            throw new ELException("Could not Resolve Variable [Overflow]: " + variable, e);
        }
    }

    /**
     * Set the ValueExpression on the inner Map instance.
     *
     * @see jakarta.el.VariableMapper#setVariable(java.lang.String, jakarta.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String variable, ValueExpression expression) {
        if (vars == null) {
            vars = new HashMap<>();
        }
        return vars.put(variable, expression);
    }
}
