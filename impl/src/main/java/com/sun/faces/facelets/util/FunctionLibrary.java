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

package com.sun.faces.facelets.util;

import static com.sun.faces.util.Util.unmodifiableSet;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 * <p>
 * This <code>TagLibrary</code> exposes the <code>public static</code> methods defined on the
 * <code>functionsClass</code> provided to the constructor as EL functions.
 * </p>
 */
public class FunctionLibrary implements TagLibrary {

    private final static String SunNamespace = "http://java.sun.com/jsp/jstl/functions";
    private final static String JcpNamespace = "http://xmlns.jcp.org/jsp/jstl/functions";
    private final static String JakartaNamespace = "jakarta.tags.functions";

    public final static Set<String> NAMESPACES = unmodifiableSet(JakartaNamespace, JcpNamespace, SunNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    private String _namespace;
    private Map<String, Method> functions;

    // ------------------------------------------------------------ Constructors

    public FunctionLibrary(Class<?> functionsClass, String namespace) {

        Util.notNull("functionsClass", functionsClass);
        Util.notNull("namespace", namespace);

        _namespace = namespace;

        try {
            Method[] methods = functionsClass.getMethods();
            functions = new HashMap<>(methods.length, 1.0f);
            for (Method method : methods) {
                if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                    functions.put(method.getName(), method);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // ------------------------------------------------- Methods from TagLibrary

    @Override
    public boolean containsNamespace(String ns, Tag t) {
        return _namespace.equals(ns);
    }

    @Override
    public boolean containsTagHandler(String ns, String localName) {
        return false;
    }

    @Override
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException {
        return null;
    }

    @Override
    public boolean containsFunction(String ns, String name) {
        return _namespace.equals(ns) && functions.containsKey(name);
    }

    @Override
    public Method createFunction(String ns, String name) {
        if (_namespace.equals(ns)) {
            return functions.get(name);
        }
        return null;
    }

}
