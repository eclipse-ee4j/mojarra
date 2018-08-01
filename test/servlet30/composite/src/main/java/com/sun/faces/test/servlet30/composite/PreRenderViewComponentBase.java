/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.composite;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UINamingContainer;

public abstract class PreRenderViewComponentBase extends UINamingContainer {

    private Map<String, Object> localAttrs;

    @Override
    public Map<String, Object> getAttributes() {
        if (localAttrs == null) {
            localAttrs = new MapWrapper(super.getAttributes());
        }

        return localAttrs;
    }

    public class MapWrapper implements Map<String, Object> {

        private Map<String, Object> parent;

        public MapWrapper(Map<String, Object> parent) {
            this.parent = parent;
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean containsKey(Object key) {
            return parent.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return parent.containsValue(value);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return parent.entrySet();
        }

        @Override
        public Object get(Object key) {
            return parent.get(key);
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return parent.keySet();
        }

        @Override
        public Object put(String key, Object value) {
            return parent.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            parent.putAll(m);
        }

        @Override
        public Object remove(Object key) {
            return parent.remove(key);
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public Collection<Object> values() {
            return parent.values();
        }
    }
}
