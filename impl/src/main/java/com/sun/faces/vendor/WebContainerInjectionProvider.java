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

package com.sun.faces.vendor;

import static java.util.logging.Level.WARNING;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import com.sun.faces.util.FacesLogger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * <p>
 * This <code>InjectionProvider</code> will be used if the <code>PostConstruct</code> and <code>PreDestroy</code>
 * annotations are present, but no specific <code>InjectionProvider</code> has been configured.
 * </p>
 *
 * <p>
 * It's important to note that this will not provide resource injection.
 * </p>
 */
public class WebContainerInjectionProvider implements InjectionProvider {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private static final Map<Class<?>, ConcurrentHashMap<Class<? extends Annotation>, MethodHolder>> methodsPerClazz = new ConcurrentHashMap<>();

    // ------------------------------------------ Methods from InjectionProvider

    @Override
    public void inject(Object managedBean) throws InjectionProviderException {
        // no-op
    }

    @Override
    public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
        if (managedBean != null) {
            invokeAnnotatedMethod(getAnnotatedMethod(managedBean, PreDestroy.class), managedBean);
        }
    }

    @Override
    public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        if (managedBean != null) {
            invokeAnnotatedMethod(getAnnotatedMethod(managedBean, PostConstruct.class), managedBean);
        }
    }

    // --------------------------------------------------------- Private Methods

    private static void invokeAnnotatedMethod(Method method, Object managedBean) throws InjectionProviderException {
        if (method != null) {
            boolean accessible = method.canAccess(managedBean);
            method.setAccessible(true);

            try {
                method.invoke(managedBean);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new InjectionProviderException(e.getMessage(), e);
            } finally {
                method.setAccessible(accessible);
            }
        }

    }

    private static class MethodHolder {

        private final Method method;

        public MethodHolder(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }
    }

    private static Method getAnnotatedMethod(Object managedBean, Class<? extends Annotation> annotation) {

        Class<?> clazz = managedBean.getClass();
        while (!Object.class.equals(clazz)) {

            ConcurrentHashMap<Class<? extends Annotation>, MethodHolder> methodsMap = methodsPerClazz.get(clazz);

            if (methodsMap == null) {

                ConcurrentHashMap<Class<? extends Annotation>, MethodHolder> newMethodsMap = new ConcurrentHashMap<>();

                methodsMap = methodsPerClazz.putIfAbsent(clazz, newMethodsMap);

                if (methodsMap == null) {
                    methodsMap = newMethodsMap;
                }
            }

            MethodHolder methodHolder = methodsMap.get(annotation);

            if (methodHolder == null) {
                Method[] methods = clazz.getDeclaredMethods();
                Method method = getAnnotatedMethodForMethodArr(methods, annotation);

                MethodHolder newMethodHolder = new MethodHolder(method);

                methodHolder = methodsMap.putIfAbsent(annotation, newMethodHolder);

                if (methodHolder == null) {
                    methodHolder = newMethodHolder;
                }
            }

            if (methodHolder.getMethod() != null) {
                return methodHolder.getMethod();
            }

            clazz = clazz.getSuperclass();
        }

        return null;
    }

    private static Method getAnnotatedMethodForMethodArr(Method[] methods, Class<? extends Annotation> annotation) {
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)) {

                // validate method
                if (Modifier.isStatic(method.getModifiers())) {
                    if (LOGGER.isLoggable(WARNING)) {
                        LOGGER.log(WARNING, "faces.core.web.injection.method_not_static", new Object[] { method.toString(), annotation.getName() });
                    }
                    continue;
                }

                if (!Void.TYPE.equals(method.getReturnType())) {
                    if (LOGGER.isLoggable(WARNING)) {
                        LOGGER.log(WARNING, "faces.core.web.injection.method_return_not_void", new Object[] { method.toString(), annotation.getName() });
                    }
                    continue;
                }

                if (method.getParameterTypes().length != 0) {
                    if (LOGGER.isLoggable(WARNING)) {
                        LOGGER.log(WARNING, "faces.core.web.injection.method_no_params", new Object[] { method.toString(), annotation.getName() });
                    }
                    continue;
                }

                Class<?>[] exceptions = method.getExceptionTypes();
                if (method.getExceptionTypes().length != 0) {
                    boolean hasChecked = false;
                    for (Class<?> excClass : exceptions) {
                        if (!RuntimeException.class.isAssignableFrom(excClass)) {
                            hasChecked = true;
                            break;
                        }
                    }
                    if (hasChecked) {
                        if (LOGGER.isLoggable(WARNING)) {
                            LOGGER.log(WARNING, "faces.core.web.injection.method_no_checked_exceptions",
                                    new Object[] { method.toString(), annotation.getName() });
                        }
                        continue;
                    }
                }

                // we found a match.
                return method;
            }
        }

        return null;
    }

}
