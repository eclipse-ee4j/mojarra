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

package com.sun.faces.facelets.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.sun.faces.util.Cache;
import com.sun.faces.util.Util;

import jakarta.faces.context.FacesContext;

/**
 * Used to provide aliases to Facelets generated unique IDs with tend to be womewhat long.
 */
public class IdMapper {

    private static final String KEY = IdMapper.class.getName();

    private Cache<String, String> idCache = new Cache<>(new IdGen());

    // ------------------------------------------------------------ Constructors

    IdMapper() {
    }

    // ---------------------------------------------------------- Public Methods

    public String getAliasedId(String id) {

        return idCache.get(id);

    }

    public static void setMapper(FacesContext ctx, IdMapper mapper) {

        Util.notNull("ctx", ctx);
        if (mapper == null) {
            ctx.getAttributes().remove(KEY);
        } else {
            ctx.getAttributes().put(KEY, mapper);
        }

    }

    public static IdMapper getMapper(FacesContext ctx) {

        Util.notNull("ctx", ctx);
        return (IdMapper) ctx.getAttributes().get(KEY);

    }

    // ---------------------------------------------------------- Nested Classes

    private static final class IdGen implements Cache.Factory<String, String> {

        private AtomicInteger counter = new AtomicInteger(0);

        // ------------------------------------------ Methods from Cache.Factory

        @Override
        public String newInstance(String arg) throws InterruptedException {

            return 't' + Integer.toString(counter.incrementAndGet());

        }

    }
}
