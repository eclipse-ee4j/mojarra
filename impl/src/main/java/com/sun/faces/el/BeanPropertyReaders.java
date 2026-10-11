/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.el;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.sun.faces.util.PropertyAccessors;

import jakarta.el.ELResolver;

/**
 * The getters of one base class that the {@link jakarta.el.BeanELResolver} of a Faces resolver chain would call, as
 * directly invocable functions. Only plain cases are covered: a public class in an exported package and a public,
 * non-static read method declared in a public class, found through {@link Introspector} exactly like the
 * BeanELResolver finds it. Anything else has no reader and goes through the resolver chain.
 */
final class BeanPropertyReaders {

    private static final Object NONE = new Object();
    private static final int MAX_PROPERTIES = 256;

    /** Tomcat's BeanELResolver then introspects on its own instead of through {@link Introspector}. */
    private static final boolean STANDALONE_BEAN_SUPPORT = Boolean.getBoolean("jakarta.el.BeanSupport.useStandalone");

    private final Class<?> type;
    private final ELResolver beanResolver;
    private final Map<String, Object> readers = new ConcurrentHashMap<>();

    /**
     * @param type the base class
     * @param beanResolver the BeanELResolver that resolves the properties of this class, or <code>null</code> when
     * another resolver may resolve them first
     */
    BeanPropertyReaders(Class<?> type, ELResolver beanResolver) {
        this.type = type;
        this.beanResolver = beanResolver;
    }

    /**
     * @return the BeanELResolver resolving the properties of this class, or <code>null</code> when there are no readers
     */
    ELResolver beanResolver() {
        return beanResolver;
    }

    /**
     * @param name the property name
     * @return a function reading the property from an instance of this class, or <code>null</code> when the property
     * must be read through the resolver chain
     */
    @SuppressWarnings("unchecked")
    Function<Object, Object> reader(String name) {
        if (beanResolver == null) {
            return null;
        }

        Object reader = readers.get(name);
        if (reader == null) {
            reader = introspect(type, name);
            if (reader == null) {
                reader = NONE;
            }
            if (readers.size() < MAX_PROPERTIES) {
                readers.put(name, reader);
            }
        }

        return reader == NONE ? null : (Function<Object, Object>) reader;
    }

    private static Function<Object, Object> introspect(Class<?> type, String name) {
        if (STANDALONE_BEAN_SUPPORT || !isPublic(type)) {
            return null;
        }

        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if (name.equals(descriptor.getName())) {
                    Method getter = descriptor.getReadMethod();
                    if (getter == null || !Modifier.isPublic(getter.getModifiers()) || Modifier.isStatic(getter.getModifiers())
                            || getter.getParameterCount() != 0 || !isPublic(getter.getDeclaringClass())) {
                        return null;
                    }
                    return PropertyAccessors.reader(getter);
                }
            }
        } catch (Exception | LinkageError e) {
            // Not introspectable here: the resolver chain reports it.
        }

        return null;
    }

    private static boolean isPublic(Class<?> type) {
        if (!Modifier.isPublic(type.getModifiers())) {
            return false;
        }
        for (Class<?> enclosing = type.getEnclosingClass(); enclosing != null; enclosing = enclosing.getEnclosingClass()) {
            if (!Modifier.isPublic(enclosing.getModifiers())) {
                return false;
            }
        }
        Module module = type.getModule();
        return !module.isNamed() || module.isExported(type.getPackageName());
    }
}
