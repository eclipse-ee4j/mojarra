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

package org.glassfish.mojarra.facelets.impl;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.util.Cache;
import org.glassfish.mojarra.util.Util;

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
        }
        else {
            ctx.getAttributes().put(KEY, mapper);
        }

    }

    public static IdMapper getMapper(FacesContext ctx) {

        Util.notNull("ctx", ctx);
        return (IdMapper) ctx.getAttributes().get(KEY);

    }

    /**
     * Returns the alias the mapper in effect for the current build gives to the given id, or the id itself when no mapper is in effect.
     *
     * @param ctx the current FacesContext
     * @param id the id to alias
     * @return the aliased id
     */
    public static String getAliasedId(FacesContext ctx, String id) {

        IdMapper mapper = getMapper(ctx);
        return mapper != null ? mapper.getAliasedId(id) : id;

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
