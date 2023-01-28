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

package com.sun.faces.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

final class MockSessionMap implements Map<String, Object> {

    public MockSessionMap(HttpSession session) {
        this.session = session;
    }

    private HttpSession session = null;

    @Override
    public void clear() {
        Iterator<String> keys = keySet().iterator();
        while (keys.hasNext()) {
            session.removeAttribute(keys.next());
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return session.getAttribute(key(key)) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        Enumeration<String> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            Object next = session.getAttribute(keys.nextElement());
            if (next == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set entrySet() {
        Set<Object> set = new HashSet<>();
        Enumeration<String> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            set.add(session.getAttribute(keys.nextElement()));
        }
        return set;
    }

    @Override
    public boolean equals(Object o) {
        return session.equals(o);
    }

    @Override
    public Object get(Object key) {
        return session.getAttribute(key(key));
    }

    @Override
    public int hashCode() {
        return session.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return size() < 1;
    }

    @Override
    public Set<String> keySet() {
        Set<String> set = new HashSet<>();
        Enumeration<String> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            set.add(keys.nextElement());
        }
        return set;
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            return (remove(key));
        }
        String skey = key(key);
        Object previous = session.getAttribute(skey);
        session.setAttribute(skey, value);
        return previous;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        Iterator<? extends String> keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            session.setAttribute(key, map.get(key));
        }
    }

    @Override
    public Object remove(Object key) {
        String skey = key(key);
        Object previous = session.getAttribute(skey);
        session.removeAttribute(skey);
        return previous;
    }

    @Override
    public int size() {
        int n = 0;
        Enumeration<?> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            keys.nextElement();
            n++;
        }
        return n;
    }

    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        Enumeration<String> keys = session.getAttributeNames();
        while (keys.hasMoreElements()) {
            list.add(session.getAttribute(keys.nextElement()));
        }
        return list;
    }

    private String key(Object key) {
        if (key == null) {
            throw new IllegalArgumentException();
        } else if (key instanceof String) {
            return (String) key;
        } else {
            return key.toString();
        }
    }

}
