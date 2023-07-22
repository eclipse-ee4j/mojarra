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

package com.sun.faces.util;

import static java.beans.Introspector.getBeanInfo;
import static java.beans.PropertyEditorManager.findEditor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * A set of utility methods to make working with Classes and Reflection a little easier.
 * </p>
 */
public final class ReflectionUtils {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * <p>
     * Cache
     * </p>
     */
    private static final Map<ClassLoader, ConcurrentMap<String, MetaData>> REFLECTION_CACHE = new WeakHashMap<>();

    // ------------------------------------------------------------ Constructors

    private ReflectionUtils() {
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Sets a collection of properties of a given object to the values associated with those properties.
     * <p>
     * In the map that represents these properties, each key represents the name of the property, with the value associated
     * with that key being the value that is set for the property.
     * <p>
     * E.g. map entry key = foo, value = "bar", which "bar" an instance of String, will conceptually result in the following
     * call: <code>object.setFoo("string");</code>
     *
     * <p>
     * NOTE: This particular method assumes that there's a write method for each property in the map with the right type. No
     * specific checking is done whether this is indeed the case.
     *
     * @param object the object on which properties will be set
     * @param propertiesToSet the map containing properties and their values to be set on the object
     */
    public static void setProperties(Object object, Map<String, Object> propertiesToSet) {

        try {
            Map<String, PropertyDescriptor> availableProperties = new HashMap<>();
            for (PropertyDescriptor propertyDescriptor : getBeanInfo(object.getClass()).getPropertyDescriptors()) {
                availableProperties.put(propertyDescriptor.getName(), propertyDescriptor);
            }

            for (Map.Entry<String, Object> propertyToSet : propertiesToSet.entrySet()) {
                availableProperties.get(propertyToSet.getKey()).getWriteMethod().invoke(object, propertyToSet.getValue());
            }

        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sets a collection of properties of a given object to the (optionally coerced) values associated with those
     * properties.
     * <p>
     * In the map that represents these properties, each key represents the name of the property, with the value associated
     * with that key being the value that is set for the property.
     * <p>
     * E.g. map entry key = foo, value = "bar", which "bar" an instance of String, will conceptually result in the following
     * call: <code>object.setFoo("string");</code>
     *
     * <p>
     * NOTE 1: In case the value is a String, and the target type is not String, the standard property editor mechanism will
     * be used to attempt a conversion.
     *
     * <p>
     * Note 2: This method operates somewhat as the reverse of <code>Reflection#setProperties(Object, Map)</code> Here only the
     * available writable properties of the object are matched against the map with properties to set. Properties in the map
     * for which there isn't a corresponding writable property on the object are ignored.
     *
     * <p>
     * Following the above two notes, use this method when attempting to set properties on an object in a lenient best
     * effort basis. Use <code>Reflection#setProperties(Object, Map)</code> when all properties need to be set with the exact
     * type as the value appears in the map.
     *
     *
     * @param object the object on which properties will be set
     * @param propertiesToSet the map containing properties and their values to be set on the object
     */
    public static void setPropertiesWithCoercion(Object object, Map<String, Object> propertiesToSet) {
        try {
            for (PropertyDescriptor property : getBeanInfo(object.getClass()).getPropertyDescriptors()) {
                Method setter = property.getWriteMethod();

                if (setter == null) {
                    continue;
                }

                if (propertiesToSet.containsKey(property.getName())) {

                    Object value = propertiesToSet.get(property.getName());
                    if (value instanceof String && !property.getPropertyType().equals(String.class)) {

                        // Try to convert Strings to the type expected by the converter

                        PropertyEditor editor = findEditor(property.getPropertyType());
                        editor.setAsText((String) value);
                        value = editor.getValue();
                    }

                    property.getWriteMethod().invoke(object, value);
                }

            }
        } catch (Exception e) { // NOPMD
            throw new IllegalStateException(e);
        }
    }

    /**
     * Finds a method based on the method name, amount of parameters and limited typing, if necessary prefixed with "get".
     * <p>
     * Note that this supports overloading, but a limited one. Given an actual parameter of type Long, this will select a
     * method accepting Number when the choice is between Number and a non-compatible type like String. However, it will NOT
     * select the best match if the choice is between Number and Long.
     *
     * @param base the object in which the method is to be found
     * @param methodName name of the method to be found
     * @param params the method parameters
     * @return a method if one is found, null otherwise
     */
    public static Method findMethod(Object base, String methodName, Object[] params) {

        List<Method> methods = new ArrayList<>();
        for (Method method : base.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == params.length) {
                methods.add(method);
            }
        }

        if (methods.size() == 1) {
            return methods.get(0);
        }

        if (methods.size() > 1) {
            // Overloaded methods were found. Try to get a match
            for (Method method : methods) {
                boolean match = true;
                Class<?>[] candidateParams = method.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    if (!candidateParams[i].isInstance(params[i])) {
                        match = false;
                        break;
                    }
                }

                // If all candidate parameters were expected and for none of them the actual
                // parameter was NOT an instance, we have a match
                if (match) {
                    return method;
                }

                // Else, at least one parameter was not an instance
                // Go ahead a test then next methods
            }
        }

        return null;
    }

    /**
     * Returns the Class instance associated with the class of the given string, using the context class loader and if that
     * fails the defining class loader of the current class.
     *
     * @param className fully qualified class name of the class for which a Class instance needs to be created
     * @return the Class object for the class with the given name.
     * @throws IllegalStateException if the class cannot be found.
     */
    public static Class<?> toClass(String className) {
        try {
            return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className);
            } catch (Exception ignore) {
                ignore = null; // Just continue to IllegalStateException on original ClassNotFoundException.
            }

            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates an instance of a class with the given fully qualified class name.
     *
     * @param <T> The generic object type.
     * @param className fully qualified class name of the class for which an instance needs to be created
     * @return an instance of the class denoted by className
     * @throws IllegalStateException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T> T instance(String className) {
        return (T) instance(toClass(className));
    }

    /**
     * Creates a new instance of the class represented by the given Class object
     *
     * @param <T> The generic object type.
     * @param clazz the Class object for which an instance needs to be created
     * @return an instance of the class as given by the clazz parameter
     * @throws IllegalStateException if the class cannot be found, or cannot be instantiated or when a security manager
     * prevents this operation
     */
    public static <T> T instance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * <p>
     * Clears the cache for the specified <code>ClassLoader</code>.
     * </p>
     * <p>
     * This method <em>MUST</em> be called when <code>ConfigureListener
     * .contextDestroyed()</code> is called.
     * </p>
     *
     * @param loader the <code>ClassLoader</code> whose associated cache should be cleared
     */
    public static synchronized void clearCache(ClassLoader loader) {
        REFLECTION_CACHE.remove(loader);
    }

    public static synchronized void initCache(ClassLoader loader) {
        if (REFLECTION_CACHE.get(loader) == null) {
            REFLECTION_CACHE.put(loader, new ConcurrentHashMap<>());
        }
    }

    /**
     * <p>
     * Returns the <code>Constructor</code> appropriate to the specified Class and parameters.
     * </p>
     *
     * @param clazz the Class of interest
     * @param params the parameters for the constructor of the provided Class
     * @return a Constructor that can be invoked with the specified parameters
     */
    public static Constructor<?> lookupConstructor(Class<?> clazz, Class<?>... params) {

        ClassLoader loader = Util.getCurrentLoader(clazz);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, clazz).lookupConstructor(params);
    }

    /**
     * <p>
     * Returns the <code>Method</code> appropriate to the specified object instance, method name, and parameters.
     * </p>
     *
     * @param object the Object instance of interest
     * @param methodName the name of the method
     * @param params the parameters for the specified method
     * @return a Method that can be invoked with the specified parameters
     */
    public static Method lookupMethod(Object object, String methodName, Class<?>... params) {

        Class<?> clazz = object.getClass();

        ClassLoader loader = Util.getCurrentLoader(clazz);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, clazz).lookupMethod(methodName, params);
    }

    /**
     * <p>
     * Returns the <code>Method</code> appropriate to the specified Class, method name, and parameters.
     * </p>
     *
     * @param clazz the Class of interest
     * @param methodName the name of the method
     * @param params the parameters for the specified method
     * @return a Method that can be invoked with the specified parameters
     */
    public static Method lookupMethod(Class<?> clazz, String methodName, Class<?>... params) {

        ClassLoader loader = Util.getCurrentLoader(clazz);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, clazz).lookupMethod(methodName, params);
    }

    /**
     * <p>
     * Constructs a new object instance based off the provided class name.
     * </p>
     *
     * @param className the class of the object to instantiate
     * @return a new instances of said class
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if there is a security violation
     */
    public static Object newInstance(String className)
            throws IllegalArgumentException, ReflectiveOperationException, SecurityException {

        ClassLoader loader = Util.getCurrentLoader(null);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, className).lookupClass().getDeclaredConstructor().newInstance();
    }

    /**
     * <p>
     * Obtain a <code>Class</code> instance based on the provided String name.
     * </p>
     *
     * @param className the class to look up
     * @return the <code>Class</code> corresponding to <code>className</code>
     */
    public static Class<?> lookupClass(String className) {

        ClassLoader loader = Util.getCurrentLoader(null);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, className).lookupClass();

    }

    /**
     * @param className the fully qualified class name
     * @param propertyName a JavaBeans property name
     * @return a method suitable for setting a JavaBeans property, or <code>null</code> if the property doesn't exist or is
     * readonly.
     */
    public static Method lookupWriteMethod(String className, String propertyName) {

        ClassLoader loader = Util.getCurrentLoader(null);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, className).lookupWriteMethod(propertyName);

    }

    /**
     * @param className the fully qualified class name
     * @param propertyName a JavaBeans property name
     * @return a method suitable for obtaining the value of a JavaBeans property, or <code>null</code> if the property
     * doesn't exist or can't be read.
     */
    public static Method lookupReadMethod(String className, String propertyName) {

        ClassLoader loader = Util.getCurrentLoader(null);
        if (loader == null) {
            return null;
        }

        return getMetaData(loader, className).lookupReadMethod(propertyName);

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Return the <code>MetaData</code> for the specified Class.
     * </p>
     *
     * <p>
     * This will check the cache associated with the specified <code>ClassLoader</code>. If there is no cache hit, then a
     * new <code>MetaData</code> instance will be created and stored.
     *
     * @param loader <code>ClassLoader</code>
     * @param clazz the Class of interest
     * @return a <code>MetaData</code> object for the specified Class
     */
    private static MetaData getMetaData(ClassLoader loader, Class<?> clazz) {

        ConcurrentMap<String, MetaData> cache = REFLECTION_CACHE.get(loader);

        if (cache == null) {
            initCache(loader);
            cache = REFLECTION_CACHE.get(loader);
        }

        MetaData meta = cache.get(clazz.getName());
        if (meta == null) {
            meta = new MetaData(clazz);
            cache.put(clazz.getName(), meta);
        }

        return meta;

    }

    /**
     * <p>
     * Return the <code>MetaData</code> for the specified className.
     * </p>
     *
     * <p>
     * This will check the cache associated with the specified <code>ClassLoader</code>. If there is no cache hit, then a
     * new <code>MetaData</code> instance will be created and stored.
     *
     * @param loader <code>ClassLoader</code>
     * @param className the class of interest
     * @return a <code>MetaData</code> object for the specified Class
     */
    private static MetaData getMetaData(ClassLoader loader, String className) {

        ConcurrentMap<String, MetaData> cache = REFLECTION_CACHE.get(loader);

        if (cache == null) {
            initCache(loader);
            cache = REFLECTION_CACHE.get(loader);
        }

        MetaData meta = cache.get(className);
        if (meta == null) {
            try {
                Class<?> clazz = Util.loadClass(className, cache);
                meta = new MetaData(clazz);
                cache.put(className, meta);
            } catch (ClassNotFoundException cnfe) {
                return null;
            }
        }

        return meta;
    }

    /**
     * <p>
     * MetaData contains lookup methods for <code>Constructor</code>s and <code>Method</code>s of a particular Class.
     */
    private static final class MetaData {

        Map<Integer, Constructor> constructors;
        Map<String, HashMap<Integer, Method>> methods;
        Map<String, HashMap<Integer, Method>> declaredMethods;
        Map<String, PropertyDescriptor> propertyDescriptors;
        Class<?> clazz;

        // ------------------------------------------------------------ Constructors

        /**
         * <p>
         * Constructs a new <code>MetaData</code> instance for the specified class.
         * </p>
         *
         * @param clazz class to construct a new MetaData instance from.
         */
        public MetaData(Class<?> clazz) {

            String name;
            this.clazz = clazz;
            Constructor<?>[] ctors = clazz.getConstructors();
            constructors = new HashMap<>(ctors.length, 1.0f);
            for (Constructor<?> ctor : ctors) {
                constructors.put(getKey(ctor.getParameterTypes()), ctor);
            }
            Method[] meths = clazz.getMethods();
            methods = new HashMap<>(meths.length, 1.0f);
            for (Method method : meths) {
                name = method.getName();
                methods.computeIfAbsent(name, k -> new HashMap<>(4, 1.0f))
                       .put(getKey(method.getParameterTypes()), method);
            }

            meths = clazz.getDeclaredMethods();
            declaredMethods = new HashMap<>(meths.length, 1.0f);
            for (Method meth : meths) {
                name = meth.getName();
                declaredMethods.computeIfAbsent(name, k -> new HashMap<>(4, 1.0f))
                               .put(getKey(meth.getParameterTypes()), meth);
            }

            try {
                BeanInfo info = Introspector.getBeanInfo(clazz);
                PropertyDescriptor[] pds = info.getPropertyDescriptors();
                if (pds != null) {
                    if (propertyDescriptors == null) {
                        propertyDescriptors = new HashMap<>(pds.length, 1.0f);
                    }
                    for (PropertyDescriptor pd : pds) {
                        propertyDescriptors.put(pd.getName(), pd);
                    }
                }
            } catch (IntrospectionException ie) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, ie.toString(), ie);
                }
            }

        }

        // ---------------------------------------------------------- Public Methods

        /**
         * <p>
         * Looks up a <code>Constructor</code> based off the specified <code>params</code>.
         * </p>
         *
         * @param params constructor parameters
         * @return the <code>Constructor</code> appropriate to the specified parameters or <code>null</code>
         */
        public Constructor<?> lookupConstructor(Class<?>... params) {
            return constructors.get(getKey(params));
        }

        /**
         * <p>
         * Looks up a <code>Method</code> based off the specified method name and <code>params</code>.
         * </p>
         *
         * @param name the name of the <cod>Method</code>
         * @param params the <code>Method</code> parameters
         * @return the <code>Method</code> appropriate to the specified name and parameters or <code>null</code>
         */
        public Method lookupMethod(String name, Class<?>... params) {

            Map<Integer, Method> map = methods.get(name);
            Integer key = getKey(params);
            Method result = null;
            if (null == map || null == (result = map.get(key))) {
                map = declaredMethods.get(name);
                if (null != map) {
                    result = map.get(key);
                }
            }
            return result;

        }

        /**
         * <p>
         * Looks up the class for this MetaData instance.
         * </p>
         *
         * @return the <code>Class</code> for this MetaData instance
         */
        public Class<?> lookupClass() {

            return clazz;

        }

        /**
         * @param propName a JavaBeans property name
         * @return a method suitable for setting a JavaBeans property, or <code>null</code> if the property doesn't exist or is
         * readonly.
         */
        public Method lookupWriteMethod(String propName) {

            if (propertyDescriptors == null) {
                return null;
            }

            PropertyDescriptor pd = propertyDescriptors.get(propName);
            if (pd != null) {
                return pd.getWriteMethod();
            }
            return null;

        }

        /**
         * @param propName a JavaBeans property name
         * @return a method suitable for obtaining the value of a JavaBeans property, or <code>null</code> if the property
         * doesn't exist or can't be read.
         */
        public Method lookupReadMethod(String propName) {

            if (propertyDescriptors == null) {
                return null;
            }

            PropertyDescriptor pd = propertyDescriptors.get(propName);
            if (pd != null) {
                return pd.getReadMethod();
            }
            return null;

        }

        // --------------------------------------------------------- Private Methods

        /**
         * Return a hashcode of all the class parameters.
         *
         * @param params the parameters to a <code>Constructor</code> or a <code>Method</code> instance
         * @return the result of <code>Arrays.deepHashCode</code>
         */
        private static Integer getKey(Class<?>... params) {

            return Arrays.deepHashCode(params);

        }

    }

} // END ReflectionUtils
