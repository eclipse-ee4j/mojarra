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

package jakarta.faces;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * A unit test to make sure all classes implementing {@link FacesWrapper} are
 * actually wrapping all public and protected methods of the wrapped class. This
 * should help to keep the wrapper classes in synch with the wrapped classes.
 * </p>
 */
public class FacesWrapperTestCase {

    private static List<Class<?>> wrapperClasses;
    private static List<Class<?>> noWrapperClasses;
    private static List<Method> methodsToIgnore;
    private static final String JAVAX_FACES_PKG = "jakarta.faces.";

    /**
     * Perform class-level initialization for test - lookup for classes
     * implementing FacesWrapper.
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        if (wrapperClasses == null) {
            loadWrapperClasses();
            methodsToIgnore = new ArrayList<Method>();
            methodsToIgnore.add(Object.class.getMethod("toString", new Class[0]));
        }
    }

    /**
     * Unit test to assert wrapperClasses list was loaded (see {@link #setUp()}.
     */
    @Test
    public void testWrapperClassesLoaded() {
        assertNotNull(wrapperClasses);
        assertTrue(!wrapperClasses.isEmpty());
    }

    /**
     * Unit test to assert there are no *Wrapper classes not implementing
     * FacesWrapper.
     */
    @Test
    public void testWrapperClassesImplementFacesWrapper() {
        assertNotNull(noWrapperClasses);
        if (noWrapperClasses.size() > 0) {
            System.out.println("Wrapper classes not implementing jakarta.faces.FacesWrapper:");
            System.out.println(noWrapperClasses.toString());
        }
        assertTrue(noWrapperClasses
                .isEmpty());
    }

    /**
     * The main goal of this TestSuite: unit test to assert all classes
     * implementing FacesWrapper do wrap all public and protected methods of the
     * wrapped class.
     */
    @Test
    public void testWrapperClassWrapsPublicAndProtectedMethods() {
        for (Class<?> wrapper : wrapperClasses) {
            if (wrapper.isInterface()) {
                continue;
            }
            List<Method> wrapperMethods = getPublicAndProtectedMethods(wrapper);
            List<Method> methodsToWrap = getPublicAndProtectedMethods(wrapper.getSuperclass());

            System.out.println("verify " + wrapper.getName() + " is wrapping "
                    + wrapper.getSuperclass().getName() + " well");
            String msg = wrapper.getCanonicalName() + " does not wrap method: ";
            for (Method m : methodsToWrap) {
                if (isMethodContained(m, methodsToIgnore)) {
                    continue;
                }
                assertTrue(isMethodContained(m, wrapperMethods), msg + m.toString());
            }
        }
    }

    // private methods
    /**
     * Returns true it the passed method is contained in the also passed list of
     * methods by also comparing matching parameters.
     *
     * @param m the method (from the wrapped class) to compare against.
     * @param wrapperMethods the list of methods of the wrapper class.
     */
    private boolean isMethodContained(Method m, List<Method> wrapperMethods) {
        String name = m.getName();
        Class<?>[] paramTypes = m.getParameterTypes();
        Class<?> returnType = m.getReturnType();
        for (Method wm : wrapperMethods) {
            if (name.equals(wm.getName()) && Arrays.equals(paramTypes, wm.getParameterTypes())
                    && returnType == wm.getReturnType()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Collect public and protected methods of a class.
     *
     * @param wrapper the class to find its methods.
     * @return list of found methods.
     */
    private List<Method> getPublicAndProtectedMethods(Class<?> wrapper) {
        List<Method> mList = new ArrayList<Method>();
        if (Object.class == wrapper) {
            return mList;
        }

        Method[] methods = wrapper.getDeclaredMethods();
        for (Method m : methods) {
            int mod = m.getModifiers();
            if (!Modifier.isStatic(mod) && (Modifier.isPublic(mod) || Modifier.isProtected(mod))) {
                mList.add(m);
            }
        }
        return mList;
    }

    /**
     * Collect the wrapper classes.
     */
    private void loadWrapperClasses() {
        wrapperClasses = new ArrayList<>();
        noWrapperClasses = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            collectWrapperClasses(
                classLoader,
                JAVAX_FACES_PKG,
                new File(classLoader.getResource("jakarta/faces/Messages.properties").getFile())
                    .getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Walk package tree for collecting wrapper classes.
     *
     * @param classLoader the ClassLoader.
     * @param pkg package name.
     * @param jakartaFacesFolder current File (directory or file)
     * @throws Exception might throw ClassNotFoundException from class loading.
     */
    private void collectWrapperClasses(ClassLoader classLoader, String pkg, File jakartaFacesFolder) throws Exception {
        for (File file : jakartaFacesFolder.listFiles()) {
            if (file.isDirectory()) {
                collectWrapperClasses(classLoader, pkg + file.getName() + ".", file);
            } else {
                addWrapperClassToWrapperClassesList(classLoader, pkg, file);
            }
        }
    }

    /**
     * Add classes that are assignable to FacesWrapper class to the
     * wrapperClasses list - and also add classes with a name ending on
     * "Wrapper" but being not assignable to FacesWrapper to the
     * noWrapperClasses list.
     *
     * @param cl the ClasslOader used to load the class.
     * @param pkg the name of the package working in.
     * @param f the File to analyse.
     * @throws Exception ClassLoader exceptions.
     */
    private void addWrapperClassToWrapperClassesList(ClassLoader cl, String pkg, File f)
            throws Exception {
        String name = f.getName();
        if (!name.endsWith(".class")) {
            return;
        }
        String className = pkg + name.substring(0, name.length() - 6);
        Class<?> c = cl.loadClass(className);
        Class<?> wrappedClass = c.getSuperclass();
        if (wrappedClass != null) {
            // we are not interested in interfaces extending FacesWrapper interface.
            // also skip classes implementing FacesWrapper but extend from Object (e.g. factories).
            if (FacesWrapper.class.isAssignableFrom(wrappedClass) || wrappedClass == Object.class) {
                return;
            }
        }
        if (FacesWrapper.class.isAssignableFrom(c)) {
            wrapperClasses.add(c);
        } else if (c != FacesWrapper.class && c.getName().endsWith("Wrapper")) {
            noWrapperClasses.add(c);
        }
    }
}
