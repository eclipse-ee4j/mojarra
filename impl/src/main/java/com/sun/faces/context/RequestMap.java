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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import com.sun.faces.util.Util;

import jakarta.servlet.ServletRequest;

/**
 * @see jakarta.faces.context.ExternalContext#getRequestMap()
 */
public class RequestMap extends BaseContextMap<Object> {

    private final ServletRequest request;

    // ------------------------------------------------------------ Constructors

    public RequestMap(ServletRequest request) {
        this.request = request;
    }

    // -------------------------------------------------------- Methods from Map

    @Override
    public void clear() {
        for (Enumeration<String> e = request.getAttributeNames(); e.hasMoreElements();) {
            request.removeAttribute(e.nextElement());
        }
    }

    // Supported by maps if overridden
    @Override
    public void putAll(Map t) {
        for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            request.setAttribute((String) entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object get(Object key) {
        Util.notNull("key", key);
        return request.getAttribute(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        Util.notNull("key", key);
        Object result = request.getAttribute(key);
        request.setAttribute(key, value);
        return result;
    }

    @Override
    public Object remove(Object key) {
        if (key == null) {
            return null;
        }
        String keyString = key.toString();
        Object result = request.getAttribute(keyString);
        request.removeAttribute(keyString);
        return result;
    }

    @Override
    public boolean containsKey(Object key) {
        return request.getAttribute(key.toString()) != null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RequestMap && super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = 7 * request.hashCode();
        for (Map.Entry<String, Object> stringObjectEntry : entrySet()) {
            hashCode += stringObjectEntry.hashCode();
        }
        return hashCode;
    }

    // --------------------------------------------- Methods from BaseContextMap

    @Override
    protected Iterator<Map.Entry<String, Object>> getEntryIterator() {
        return new EntryIterator(request.getAttributeNames());
    }

    @Override
    protected Iterator<String> getKeyIterator() {
        return new KeyIterator(request.getAttributeNames());
    }

    @Override
    protected Iterator<Object> getValueIterator() {
        return new ValueIterator(request.getAttributeNames());
    }

} // END RequestMap
