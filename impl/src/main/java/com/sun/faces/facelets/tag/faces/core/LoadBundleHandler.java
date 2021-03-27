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

package com.sun.faces.facelets.tag.faces.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

/**
 * Load a resource bundle localized for the Locale of the current view, and expose it (as a Map) in the request
 * attributes of the current request.
 * <p/>
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/loadBundle.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class LoadBundleHandler extends TagHandlerImpl {

    private final static class ResourceBundleMap implements Map {
        private final static class ResourceEntry implements Map.Entry {

            protected final String key;

            protected final String value;

            public ResourceEntry(String key, String value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public Object getKey() {
                return key;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public Object setValue(Object value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                return key.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ResourceEntry && hashCode() == obj.hashCode();
            }
        }

        protected final ResourceBundle bundle;

        public ResourceBundleMap(ResourceBundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            try {
                bundle.getString(key.toString());
                return true;
            } catch (MissingResourceException e) {
                return false;
            }
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set entrySet() {
            Enumeration e = bundle.getKeys();
            Set s = new HashSet();
            String k;
            while (e.hasMoreElements()) {
                k = (String) e.nextElement();
                s.add(new ResourceEntry(k, bundle.getString(k)));
            }
            return s;
        }

        @Override
        public Object get(Object key) {
            try {
                return bundle.getObject((String) key);
            } catch (java.util.MissingResourceException mre) {
                return "???" + key + "???";
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Set keySet() {
            Enumeration e = bundle.getKeys();
            Set s = new HashSet();
            while (e.hasMoreElements()) {
                s.add(e.nextElement());
            }
            return s;
        }

        @Override
        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return keySet().size();
        }

        @Override
        public Collection values() {
            Enumeration e = bundle.getKeys();
            Set s = new HashSet();
            while (e.hasMoreElements()) {
                s.add(bundle.getObject((String) e.nextElement()));
            }
            return s;
        }
    }

    private final TagAttribute basename;

    private final TagAttribute var;

    /**
     * @param config
     */
    public LoadBundleHandler(TagConfig config) {
        super(config);
        basename = getRequiredAttribute("basename");
        var = getRequiredAttribute("var");
    }

    /**
     * See taglib documentation.
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        ResourceBundle bundle = null;
        try {
            String name = basename.getValue(ctx);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (root != null && root.getLocale() != null) {
                bundle = ResourceBundle.getBundle(name, root.getLocale(), cl);
            } else {
                bundle = ResourceBundle.getBundle(name, Locale.getDefault(), cl);
            }
        } catch (Exception e) {
            throw new TagAttributeException(tag, basename, e);
        }
        ResourceBundleMap map = new ResourceBundleMap(bundle);
        FacesContext faces = ctx.getFacesContext();
        faces.getExternalContext().getRequestMap().put(var.getValue(ctx), map);
    }
}
