/*
 * Copyright (c) 2025 Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.facelets.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;

import com.sun.faces.facelets.tag.composite.CompositeLibrary;
import com.sun.faces.facelets.tag.faces.PassThroughAttributeLibrary;
import com.sun.faces.facelets.tag.faces.PassThroughElementLibrary;
import com.sun.faces.facelets.tag.faces.core.CoreLibrary;
import com.sun.faces.facelets.tag.faces.html.HtmlLibrary;
import com.sun.faces.facelets.tag.jstl.core.JstlCoreLibrary;
import com.sun.faces.facelets.tag.ui.UILibrary;
import com.sun.faces.facelets.util.FunctionLibrary;

/**
 * Utility class for checking and reporting deprecated namespace usage.
 * 
 * @since 5.0
 */
final class DeprecatedNamespacesChecker {

    private static final Map<String, String> DEPRECATED_NAMESPACE_REPLACEMENTS;

    private static final Set<String> foundNamespaces = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        var map = new HashMap<String, String>();
        mapDeprecatedNamespaces(map, CoreLibrary.NAMESPACES, CoreLibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, HtmlLibrary.NAMESPACES, HtmlLibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, UILibrary.NAMESPACES, UILibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, CompositeLibrary.NAMESPACES, CompositeLibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, PassThroughElementLibrary.NAMESPACES, PassThroughElementLibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, PassThroughAttributeLibrary.NAMESPACES, PassThroughAttributeLibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, JstlCoreLibrary.NAMESPACES, JstlCoreLibrary.DEFAULT_NAMESPACE);
        mapDeprecatedNamespaces(map, FunctionLibrary.NAMESPACES, FunctionLibrary.DEFAULT_NAMESPACE);
        DEPRECATED_NAMESPACE_REPLACEMENTS = Collections.unmodifiableMap(map);
    }

    private static void mapDeprecatedNamespaces(Map<String, String> map, Set<String> namespaces, String defaultNamespace) {
        for (String namespace : namespaces) {
            if (!namespace.equals(defaultNamespace)) {
                map.put(namespace, defaultNamespace);
            }
        }
    }

    /**
     * If given namespace is null, return null.
     * If FacesContext is null, return null.
     * If ProjectStage is not Development, return null.
     * If given namespace was already previously checked, return null.
     * If given namespace is not deprecated, return null.
     * If given namespace is deprecated, return replacement namespace.
     */
    static String shouldWarnAboutForDeprecatedNamespace(String namespace) {
        if (namespace == null || foundNamespaces.contains(namespace)) {
            return null;
        }

        var context = FacesContext.getCurrentInstance();
        
        if (context == null || context.getApplication().getProjectStage() != ProjectStage.Development) {
            return null;
        }

        foundNamespaces.add(namespace);
        return DEPRECATED_NAMESPACE_REPLACEMENTS.get(namespace);
    }
}
