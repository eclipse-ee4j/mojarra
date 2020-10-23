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

import java.lang.reflect.Method;

import jakarta.el.FunctionMapper;

/**
 * Composite FunctionMapper that attempts to load the Method from the first FunctionMapper, then the second if
 * <code>null</code>.
 *
 * @see jakarta.el.FunctionMapper
 * @see java.lang.reflect.Method
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CompositeFunctionMapper extends FunctionMapper {

    private final FunctionMapper fn0;

    private final FunctionMapper fn1;

    public CompositeFunctionMapper(FunctionMapper fn0, FunctionMapper fn1) {
        this.fn0 = fn0;
        this.fn1 = fn1;
    }

    /**
     * @see jakarta.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
     */
    @Override
    public Method resolveFunction(String prefix, String name) {
        Method m = fn0.resolveFunction(prefix, name);
        if (m == null) {
            return fn1.resolveFunction(prefix, name);
        }
        return m;
    }

}
