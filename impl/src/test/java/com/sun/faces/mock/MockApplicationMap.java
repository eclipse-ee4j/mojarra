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
import jakarta.servlet.ServletContext;

final class MockApplicationMap implements Map {

    public MockApplicationMap(ServletContext context) {
        this.context = context;
    }

    private ServletContext context = null;

    public void clear() {
        Iterator keys = keySet().iterator();
        while (keys.hasNext()) {
            context.removeAttribute((String) keys.next());
        }
    }

    public boolean containsKey(Object key) {
        return (context.getAttribute(key(key)) != null);
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return (false);
        }
        Enumeration keys = context.getAttributeNames();
        while (keys.hasMoreElements()) {
            Object next = context.getAttribute((String) keys.nextElement());
            if (next == value) {
                return (true);
            }
        }
        return (false);
    }

    public Set entrySet() {
        Set set = new HashSet();
        Enumeration keys = context.getAttributeNames();
        while (keys.hasMoreElements()) {
            set.add(context.getAttribute((String) keys.nextElement()));
        }
        return (set);
    }

    public boolean equals(Object o) {
        return (context.equals(o));
    }

    public Object get(Object key) {
        return (context.getAttribute(key(key)));
    }

    public int hashCode() {
        return (context.hashCode());
    }

    public boolean isEmpty() {
        return (size() < 1);
    }

    public Set keySet() {
        Set set = new HashSet();
        Enumeration keys = context.getAttributeNames();
        while (keys.hasMoreElements()) {
            set.add(keys.nextElement());
        }
        return (set);
    }

    public Object put(Object key, Object value) {
        if (value == null) {
            return (remove(key));
        }
        String skey = key(key);
        Object previous = context.getAttribute(skey);
        context.setAttribute(skey, value);
        return (previous);
    }

    public void putAll(Map map) {
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            context.setAttribute(key, map.get(key));
        }
    }

    public Object remove(Object key) {
        String skey = key(key);
        Object previous = context.getAttribute(skey);
        context.removeAttribute(skey);
        return (previous);
    }

    public int size() {
        int n = 0;
        Enumeration keys = context.getAttributeNames();
        while (keys.hasMoreElements()) {
            keys.nextElement();
            n++;
        }
        return (n);
    }

    public Collection values() {
        List list = new ArrayList();
        Enumeration keys = context.getAttributeNames();
        while (keys.hasMoreElements()) {
            list.add(context.getAttribute((String) keys.nextElement()));
        }
        return (list);
    }

    private String key(Object key) {
        if (key == null) {
            throw new IllegalArgumentException();
        } else if (key instanceof String) {
            return ((String) key);
        } else {
            return (key.toString());
        }
    }

}
