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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.util.ReflectionUtils;
import com.sun.faces.util.Util;

public class ReflectionUtil {

    private static final String[] PRIMITIVE_NAMES = new String[] { "boolean", "byte", "char", "double", "float", "int", "long", "short", "void" };

    private static final Class<?>[] PRIMITIVES = new Class<?>[] { boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class,
            short.class, Void.TYPE };

    /**
     *
     */
    private ReflectionUtil() {
    }

    public static Class forName(String name) throws ClassNotFoundException {
        if (null == name || "".equals(name)) {
            return null;
        }
        Class<?> c = forNamePrimitive(name);
        if (c == null) {
            if (name.endsWith("[]")) {
                String nc = name.substring(0, name.length() - 2);
                c = Class.forName(nc, false, Thread.currentThread().getContextClassLoader());
                c = Array.newInstance(c, 0).getClass();
            } else {
                c = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            }
        }
        return c;
    }

    protected static Class forNamePrimitive(String name) {
        if (name.length() <= 8) {
            int p = Arrays.binarySearch(PRIMITIVE_NAMES, name);
            if (p >= 0) {
                return PRIMITIVES[p];
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String name) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        return (T) forName(name).getDeclaredConstructor().newInstance();
    }

    /**
     * Converts an array of Class names to Class types
     *
     * @param s the array of class names.
     * @return the array of classes.
     * @throws ClassNotFoundException
     */
    public static Class[] toTypeArray(String[] s) throws ClassNotFoundException {
        if (s == null) {
            return null;
        }
        Class<?>[] c = new Class[s.length];
        for (int i = 0; i < s.length; i++) {
            c[i] = forName(s[i]);
        }
        return c;
    }

    /**
     * Converts an array of Class types to Class names
     *
     * @param c the array of classes.
     * @return the array of class names.
     */
    public static String[] toTypeNameArray(Class[] c) {
        if (c == null) {
            return null;
        }
        String[] s = new String[c.length];
        for (int i = 0; i < c.length; i++) {
            s[i] = c[i].getName();
        }
        return s;
    }

//    /*
//     * Get a public method form a public class or interface of a given method.
//     * Note that if the base is an instance of a non-public class that
//     * implements a public interface,  calling Class.getMethod() with the base
//     * will not find the method.  To correct this, a version of the
//     * same method must be found in a superclass or interface.
//     **/
//
//    static private Method getMethod(Class cl, String methodName,
//                                    Class[] paramTypes) {
//
//        Method m = null;
//        try {
//            m = cl.getMethod(methodName, paramTypes);
//        } catch (NoSuchMethodException ex) {
//            return null;
//        }
//
//        Class dclass  = m.getDeclaringClass();
//        if (Modifier.isPublic(dclass.getModifiers())) {
//            return m;
//        }
//
//        Class[] intf = dclass.getInterfaces();
//        for (int i = 0; i < intf.length; i++) {
//            m = getMethod(intf[i], methodName, paramTypes);
//            if (m != null) {
//                return m;
//            }
//        }
//        Class c = dclass.getSuperclass();
//        if (c != null) {
//            m = getMethod(c, methodName, paramTypes);
//            if (m != null) {
//                return m;
//            }
//        }
//        return null;
//    }

    protected static String paramString(Class<?>[] types) {
        if (types != null) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> type : types) {
                sb.append(type.getName()).append(", ");
            }
            if (sb.length() > 2) {
                sb.setLength(sb.length() - 2);
            }
            return sb.toString();
        }
        return null;
    }

    public static Object decorateInstance(Class clazz, Class rootType, Object root) {
        Object returnObject = null;
        try {
            if (returnObject == null) {
                // Look for an adapter constructor if we've got
                // an object to adapt
                if (rootType != null && root != null) {
                    Constructor construct = ReflectionUtils.lookupConstructor(clazz, rootType);
                    if (construct != null) {
                        returnObject = construct.newInstance(root);
                    }
                }
            }
            if (clazz != null && returnObject == null) {
                returnObject = clazz.getDeclaredConstructor().newInstance();
            }
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            throw new ConfigurationException(
                    buildMessage(MessageFormat.format("Unable to create a new instance of ''{0}'': {1}", clazz.getName(), e.toString())), e);
        }
        return returnObject;

    }

    public static Object decorateInstance(String className, Class rootType, Object root) {
        Class clazz;
        Object returnObject = null;
        if (className != null) {
            try {
                clazz = loadClass(className, returnObject, null);
                if (clazz != null) {
                    returnObject = decorateInstance(clazz, rootType, root);
                }

            } catch (ClassNotFoundException cnfe) {
                throw new ConfigurationException(buildMessage(MessageFormat.format("Unable to find class ''{0}''", className)));
            } catch (NoClassDefFoundError ncdfe) {
                throw new ConfigurationException(
                        buildMessage(MessageFormat.format("Class ''{0}'' is missing a runtime dependency: {1}", className, ncdfe.toString())));
            } catch (ClassCastException cce) {
                throw new ConfigurationException(buildMessage(MessageFormat.format("Class ''{0}'' is not an instance of ''{1}''", className, rootType)));
            } catch (Exception e) {
                throw new ConfigurationException(buildMessage(MessageFormat.format("Unable to create a new instance of ''{0}'': {1}", className, e.toString())),
                        e);
            }
        }

        return returnObject;

    }

    // --------------------------------------------------------- Private Methods

    private static String buildMessage(String cause) {

        return MessageFormat.format("\n  Source Document: {0}\n  Cause: {1}", "web.xml", cause);

    }

    private static Class<?> loadClass(String className, Object fallback, Class<?> expectedType) throws ClassNotFoundException {

        Class<?> clazz = Util.loadClass(className, fallback);
        if (expectedType != null && !expectedType.isAssignableFrom(clazz)) {
            throw new ClassCastException();
        }
        return clazz;

    }

}
