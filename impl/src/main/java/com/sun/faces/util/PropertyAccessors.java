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

package com.sun.faces.util;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Turns JavaBean getter {@link Method}s into directly invocable functions, for hot paths that would otherwise go
 * through {@link Method#invoke} (such as {@code UIComponent.getAttributes().get/put} on property-backed attributes).
 *
 * <p>
 * The preferred form is a {@link LambdaMetafactory} generated class, which the JIT can inline like a regular call. It
 * is defined in the accessor's declaring class (via {@link MethodHandles#privateLookupIn}), so it resolves against that
 * class's own class loader -- which matters for application component classes that Mojarra's class loader cannot
 * see. When that is not permitted (e.g. a package not opened to Mojarra in a named module), a {@link MethodHandle} is
 * used, and when even that fails, {@link Method#invoke}. Each step is behaviour-equivalent: the accessor's own
 * exceptions propagate unwrapped (callers wrap them, exactly as they unwrap {@link InvocationTargetException}).
 *
 * <p>
 * Creating a lambda costs a class definition, so callers should create accessors lazily, for the properties actually
 * used, and cache them per class.
 *
 * <p>
 * Only getters are covered: they take no argument, so no argument adaptation can change which exception a bad call
 * raises (for a setter, {@link Method#invoke} reports a {@code null} primitive or a mistyped value as
 * {@link IllegalArgumentException}, a lambda would not).
 */
public final class PropertyAccessors {

    private static final MethodType FUNCTION_SIGNATURE = methodType(Object.class, Object.class);

    private PropertyAccessors() {
    }

    /**
     * Returns a function invoking the given getter on its argument.
     */
    @SuppressWarnings("unchecked")
    public static Function<Object, Object> reader(Method getter) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(getter.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle handle = lookup.unreflect(getter);
            try {
                CallSite site = LambdaMetafactory.metafactory(lookup, "apply", methodType(Function.class), FUNCTION_SIGNATURE, handle,
                        handle.type().wrap().changeReturnType(Object.class).changeParameterType(0, getter.getDeclaringClass()));
                return (Function<Object, Object>) site.getTarget().invokeExact();
            } catch (Throwable lambdaNotPossible) {
                MethodHandle generic = handle.asType(FUNCTION_SIGNATURE);
                return bean -> invokeReader(generic, bean);
            }
        } catch (Throwable lookupNotPossible) {
            return bean -> invokeReflectively(getter, bean);
        }
    }

    private static Object invokeReader(MethodHandle handle, Object bean) {
        try {
            return (Object) handle.invokeExact(bean);
        } catch (Throwable t) {
            throw sneakyThrow(t);
        }
    }

    private static Object invokeReflectively(Method method, Object bean, Object... args) {
        try {
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
