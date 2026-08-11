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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.Util;

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
 * 
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/loadBundle.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class LoadBundleHandler extends TagHandlerImpl {

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
        if (!basename.isLiteral()) {
            // A non-literal basename is re-evaluated per request, so the view must be re-applied on every (re)build
            // instead of skipped (see refreshTransientBuildOnPSS) to re-resolve the bundle under var.
            markDynamicTransientBuild(ctx);
        }

        final ResourceBundle bundle;
        try {
            final String name = basename.getValue(ctx);
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            final UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
            final Locale locale = root != null && root.getLocale() != null ? root.getLocale() : Locale.getDefault();

            bundle = ResourceBundle.getBundle(name, locale, cl);
        }
        catch (Exception e) {
            throw new TagAttributeException(tag, basename, e);
        }

        final ResourceBundleMap map = new ResourceBundleMap(bundle);
        final FacesContext faces = ctx.getFacesContext();

        faces.getExternalContext().getRequestMap().put(var.getValue(ctx), map);
    }

    // ResourceBundleMap ---------------------------------------------------------------------

    private final static class ResourceBundleMap implements Map<String,Object> {

        private static final String MISSING_RESOURCE_PLACEHOLDER = "???";

        private final ResourceBundle bundle;

        public ResourceBundleMap(ResourceBundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            return key != null && bundle.containsKey((String)key);
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Map.Entry<String,Object>> entrySet() {
            Set<String> keys = keySet();
            Set<Map.Entry<String,Object>> set = new HashSet<>(Util.calculateMapCapacity(keys.size()));
            for (String key : keys) {
                set.add(Map.entry(key, bundle.getObject(key)));
            }
            return set;
        }

        @Override
        public Object get(Object key) {
            try {
                return bundle.getObject((String) key);
            } catch (MissingResourceException mre) {
                return MISSING_RESOURCE_PLACEHOLDER + key + MISSING_RESOURCE_PLACEHOLDER;
            }
        }

        @Override
        public boolean isEmpty() {
            return keySet().isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return bundle.keySet();
        }

        @Override
        public Object put(String key, Object value) {
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
        public Collection<Object> values() {
            Set<String> keys = bundle.keySet();
            List<Object> list = new ArrayList<>(keys.size());
            for (String key : keys) {
                list.add(bundle.getObject(key));
            }
            return list;
        }

    }

}
