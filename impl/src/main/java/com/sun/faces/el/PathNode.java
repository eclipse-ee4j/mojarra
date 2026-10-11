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

import java.util.function.Function;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ValueExpression;

/**
 * Evaluates <code>name</code> or <code>name.property.property...</code> the way the EL interpreter does:
 * <ol>
 * <li>the name is a variable captured from the variable mapper when the expression was created, whose expression is
 * evaluated, or else it is resolved by the resolver chain with no base;
 * <li>each property is resolved by the resolver chain on the previous value, until a value is <code>null</code>.
 * </ol>
 * In addition, when the Faces chain would resolve a property with the BeanELResolver, the getter is called directly,
 * with an inline cache of the reader for the last base class seen. A getter that throws is called once more through
 * the BeanELResolver, so that its exception is wrapped exactly as the resolver wraps it.
 *
 * <p>
 * A node is shared by every expression created from the same expression string -- Facelets creates the expressions of
 * template text again on every build of the view -- so the inline cache serves them all, from any thread: its entries
 * are immutable, and a concurrent replacement only costs another lookup. The variable is the expression's own.
 */
final class PathNode {

    /** Returned when the name or a property is not resolved: the EL implementation must then evaluate. */
    static final Object UNRESOLVED = new Object();

    /** A reader cached for one base class, valid as long as the chain's readers are the same instance. */
    private static final class ReaderCache {
        final Class<?> type;
        final ClassValue<BeanPropertyReaders> owner;
        final Function<Object, Object> reader;
        final ELResolver beanResolver;

        ReaderCache(Class<?> type, ClassValue<BeanPropertyReaders> owner, Function<Object, Object> reader, ELResolver beanResolver) {
            this.type = type;
            this.owner = owner;
            this.reader = reader;
            this.beanResolver = beanResolver;
        }
    }

    private final String name;
    private final String[] path;
    private final ReaderCache[] readerCaches;

    /**
     * @param path the name followed by the properties, possibly none (not copied, must not be modified)
     */
    PathNode(String[] path) {
        name = path[0];
        this.path = path;
        readerCaches = new ReaderCache[path.length];
    }

    String name() {
        return name;
    }

    String[] path() {
        return path;
    }

    /**
     * @param variable the variable the variable mapper had for the name when the expression was created, or
     * <code>null</code>
     * @return the value, or {@link #UNRESOLVED}
     */
    Object getValue(ELContext context, ELResolver resolver, ValueExpression variable) {
        Object base;
        if (variable != null) {
            base = variable.getValue(context);
        } else {
            context.setPropertyResolved(false);
            base = resolver.getValue(context, null, name);
            if (!context.isPropertyResolved()) {
                return UNRESOLVED;
            }
        }

        if (path.length > 1) {
            for (int i = 1; base != null && i < path.length; i++) {
                context.setPropertyResolved(false);
                base = getProperty(context, resolver, base, i);
            }
            if (!context.isPropertyResolved()) {
                return UNRESOLVED;
            }
        }

        return base;
    }

    private Object getProperty(ELContext context, ELResolver resolver, Object base, int index) {
        String property = path[index];

        if (resolver instanceof DemuxCompositeELResolver) {
            ClassValue<BeanPropertyReaders> owner = ((DemuxCompositeELResolver) resolver).beanReaders();
            Class<?> type = base.getClass();
            ReaderCache cache = readerCaches[index];
            if (cache == null || cache.type != type || cache.owner != owner) {
                BeanPropertyReaders readers = owner.get(type);
                cache = new ReaderCache(type, owner, readers.reader(property), readers.beanResolver());
                readerCaches[index] = cache;
            }

            if (cache.reader != null) {
                Object value;
                try {
                    value = cache.reader.apply(base);
                } catch (VirtualMachineError e) {
                    throw e;
                } catch (Throwable e) {
                    // Let the BeanELResolver call it and wrap its exception.
                    return cache.beanResolver.getValue(context, base, property);
                }
                // Marked resolved after the call: Expressly's BeanELResolver does so too (Tomcat's before, which only
                // differs for a throwing getter, handled above, and for evaluation listeners, which are delegated).
                context.setPropertyResolved(base, property);
                return value;
            }
        }

        return resolver.getValue(context, base, property);
    }
}
