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

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

/**
 * Default instance of a VariableMapper backed by a Map
 *
 * @see jakarta.el.VariableMapper
 * @see jakarta.el.ValueExpression
 * @see java.util.Map
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class DefaultVariableMapper extends VariableMapper {

    private Map<String, ValueExpression> vars;

    public DefaultVariableMapper() {
        super();
    }

    /**
     * @see jakarta.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String name) {
        if (vars != null) {
            return vars.get(name);
        }
        return null;
    }

    /**
     * @see jakarta.el.VariableMapper#setVariable(java.lang.String, jakarta.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String name, ValueExpression expression) {
        if (vars == null) {
            vars = new HashMap<>();
        }
        return vars.put(name, expression);
    }

}
