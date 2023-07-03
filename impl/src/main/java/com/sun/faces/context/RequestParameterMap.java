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

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletRequest;

/**
 * @see jakarta.faces.context.ExternalContext#getRequestParameterMap()
 */
public class RequestParameterMap extends BaseContextMap<String> {

    private String namingContainerPrefix;
    private final ServletRequest request;
    private boolean inspectedParameterNames = false;

    // ------------------------------------------------------------ Constructors

    public RequestParameterMap(ServletRequest request) {
        this.request = request;
    }

    // -------------------------------------------------------- Methods from Map

    @Override
    public String get(Object key) {
        Util.notNull("key", key);
        if (!inspectedParameterNames) {
            inspectedParameterNames = true;
            request.getParameterNames();
        }
        String mapKey = key.toString();
        String mapValue = request.getParameter(mapKey);
        if (mapValue == null && !mapKey.startsWith(getNamingContainerPrefix())) {
            // Support cases where enduser manually obtains a request parameter while in a namespaced view.
            mapValue = request.getParameter(getNamingContainerPrefix() + mapKey);
        }
        return mapValue;
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    /**
     * If view root is instance of naming container, return its container client id, suffixed with separator character.
     *
     * @return The naming container prefix, or an empty string if the view root is not an instance of naming container.
     */
    protected String getNamingContainerPrefix() {
        if (null == namingContainerPrefix) {
            FacesContext context = FacesContext.getCurrentInstance();

            if (context == null) {
                return "";
            }

            namingContainerPrefix = Util.getNamingContainerPrefix(context);
        }

        return namingContainerPrefix;
    }

    @Override
    public Collection<String> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    @Override
    public boolean containsKey(Object key) {
        return key != null && get(key) != null;
    }

    @Override
    public boolean equals(Object obj) {
        return !(obj == null || !(obj.getClass() == ExternalContextImpl.theUnmodifiableMapClass)) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = 7 * request.hashCode();
        for (Map.Entry<String, String> stringStringEntry : entrySet()) {
            hashCode += stringStringEntry.hashCode();
        }
        return hashCode;
    }

    // --------------------------------------------- Methods from BaseContextMap

    @Override
    protected Iterator<Map.Entry<String, String>> getEntryIterator() {
        return new EntryIterator(request.getParameterNames());
    }

    @Override
    protected Iterator<String> getKeyIterator() {
        return new KeyIterator(request.getParameterNames());
    }

    @Override
    protected Iterator<String> getValueIterator() {
        return new ValueIterator(request.getParameterNames());
    }

} // END RequestParameterMap
