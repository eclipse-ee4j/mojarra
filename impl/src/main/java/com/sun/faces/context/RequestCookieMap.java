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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.sun.faces.util.Util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @see jakarta.faces.context.ExternalContext#getRequestCookieMap()
 */
public class RequestCookieMap extends BaseContextMap<Object> {

    private final HttpServletRequest request;

    // ------------------------------------------------------------ Constructors

    public RequestCookieMap(HttpServletRequest newRequest) {
        request = newRequest;
    }

    // -------------------------------------------------------- Methods from Map

    @Override
    public Object get(Object key) {
        Util.notNull("key", key);

        Cookie[] cookies = request.getCookies();
        if (null == cookies) {
            return null;
        }

        String keyString = key.toString();
        Object result = null;

        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(keyString)) {
                result = cookies[i];
                break;
            }
        }
        return result;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    @Override
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    @Override
    public boolean equals(Object obj) {
        return !(obj == null || !(obj.getClass() == ExternalContextImpl.theUnmodifiableMapClass)) && super.equals(obj);
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
        return new EntryIterator(new CookieArrayEnumerator(request.getCookies()));
    }

    @Override
    protected Iterator<String> getKeyIterator() {
        return new KeyIterator(new CookieArrayEnumerator(request.getCookies()));
    }

    @Override
    protected Iterator<Object> getValueIterator() {
        return new ValueIterator(new CookieArrayEnumerator(request.getCookies()));
    }

    // ----------------------------------------------------------- Inner Classes

    private static class CookieArrayEnumerator implements Enumeration {

        Cookie[] cookies;
        int curIndex = -1;
        int upperBound;

        public CookieArrayEnumerator(Cookie[] cookies) {
            this.cookies = cookies;
            upperBound = this.cookies != null ? this.cookies.length : -1;
        }

        @Override
        public boolean hasMoreElements() {
            return curIndex + 2 <= upperBound;
        }

        @Override
        public Object nextElement() {
            curIndex++;
            if (curIndex < upperBound) {
                return cookies[curIndex].getName();
            } else {
                throw new NoSuchElementException();
            }
        }

    }

} // END RequestCookiesMap
