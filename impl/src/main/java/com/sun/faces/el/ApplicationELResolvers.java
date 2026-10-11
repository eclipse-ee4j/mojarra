/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.el;

import java.util.Arrays;

import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;

/**
 * The resolvers registered with {@link jakarta.faces.application.Application#addELResolver} (Weld, for one, adds its
 * own). Exposes them, so the Faces chain can apply its shortcut rules to each of them instead of treating the whole
 * composite as an unknown resolver.
 */
public class ApplicationELResolvers extends CompositeELResolver {

    private static final ELResolver[] NONE = {};

    private volatile ELResolver[] resolvers = NONE;
    private volatile boolean converting;

    @Override
    public synchronized void add(ELResolver elResolver) {
        super.add(elResolver);
        ELResolver[] grown = Arrays.copyOf(resolvers, resolvers.length + 1);
        grown[resolvers.length] = elResolver;
        resolvers = grown;
        if (DemuxCompositeELResolver.declaresConvertToType(elResolver)) {
            converting = true;
        }
    }

    public boolean isEmpty() {
        return resolvers.length == 0;
    }

    /**
     * @return the resolvers added so far, in order (the returned array must not be modified)
     */
    ELResolver[] getResolvers() {
        return resolvers;
    }

    /**
     * @return whether any resolver added so far may convert in {@link #convertToType}
     */
    boolean mayConvert() {
        return converting;
    }
}
