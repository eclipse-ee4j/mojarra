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

package com.sun.faces.context;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.faces.util.Util;

import jakarta.servlet.ServletContext;

/**
 * @see jakarta.faces.context.ExternalContext#getInitParameterMap()
 */
public class InitParameterMap extends BaseContextMap<String> {

    private final ServletContext servletContext;

    // ------------------------------------------------------------ Constructors

    public InitParameterMap(ServletContext newServletContext) {
        servletContext = newServletContext;
    }

    // -------------------------------------------------------- Methods from Map

    @Override
    public String get(Object key) {
        Util.notNull("key", key);
        String keyString = key.toString();
        return servletContext.getInitParameter(keyString);
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    @Override
    public Collection<String> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    @Override
    public boolean containsKey(Object key) {
        return servletContext.getInitParameter(key.toString()) != null;
    }

    @Override
    public boolean equals(Object obj) {
        return !(obj == null || !(obj.getClass() == ExternalContextImpl.theUnmodifiableMapClass)) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = 7 * servletContext.hashCode();
        for (Map.Entry<String, String> stringStringEntry : entrySet()) {
            hashCode += stringStringEntry.hashCode();
        }
        return hashCode;
    }

    // --------------------------------------------- Methods from BaseContextMap

    @Override
    protected Iterator<Map.Entry<String, String>> getEntryIterator() {
        return new EntryIterator(servletContext.getInitParameterNames());
    }

    @Override
    protected Iterator<String> getKeyIterator() {
        return new KeyIterator(servletContext.getInitParameterNames());
    }

    @Override
    protected Iterator<String> getValueIterator() {
        return new ValueIterator(servletContext.getInitParameterNames());
    }

} // END InitParameterMap
