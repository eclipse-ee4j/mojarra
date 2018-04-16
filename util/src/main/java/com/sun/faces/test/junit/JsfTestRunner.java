/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.junit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * The JSF JUnit 4 Test runner.
 */
public class JsfTestRunner extends BlockJUnit4ClassRunner {

    /**
     * Stores the boolean flag indicating we skip.
     */
    private boolean skip = false;

    /**
     * Constructor.
     *
     * @param clazz the class.
     * @throws InitializationError when initialization fails.
     */
    public JsfTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);

        if (clazz.getAnnotation(JsfTest.class) != null) {
            JsfTest jsfTest = clazz.getAnnotation(JsfTest.class);

            if (System.getProperty("jsf.version") != null) {
                try {
                    JsfVersion serverVersion = JsfVersion.fromString(System.getProperty("jsf.version"));

                    if (serverVersion.ordinal() < jsfTest.value().ordinal()) {
                        this.skip = true;
                    }
                } catch (IllegalArgumentException exception) {
                    /*
                     * We could not match up the version, so we are going to 
                     * assume you still want to run the tests.
                     */
                }
            }
        }
    }

    /**
     * Compute the test methods.
     *
     * @return the test methods.
     */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();
        if (!skip) {
            Iterator<FrameworkMethod> methods = super.computeTestMethods().iterator();

            while (methods.hasNext()) {
                FrameworkMethod method = methods.next();

                if (method.getAnnotation(JsfTest.class) != null) {
                    JsfTest jsfTest = method.getAnnotation(JsfTest.class);
                    boolean excludeFlag = false;

                    if (jsfTest.excludes().length > 0) {
                        JsfServerExclude exclude = JsfServerExclude.fromString(System.getProperty("jsf.serverString"));

                        if (exclude != null) {
                            for (JsfServerExclude current : jsfTest.excludes()) {
                                if (current.equals(exclude)) {
                                    excludeFlag = true;
                                }
                            }
                        }
                    }

                    if (!excludeFlag && System.getProperty("jsf.version") != null) {
                        try {
                            JsfVersion serverVersion = JsfVersion.fromString(System.getProperty("jsf.version"));

                            if (serverVersion.ordinal() < jsfTest.value().ordinal()) {
                            } else {
                                result.add(method);
                            }
                        } catch (IllegalArgumentException exception) {
                            /*
                             * We could not match up the version, so we are going to
                             * assume you still want to run the tests.
                             */
                            result.add(method);
                        }
                    }
                } else {
                    result.add(method);
                }
            }
        }
        return result;
    }

    /*
     * Allow for no (active) test methods on the test class.
     */
    @Override
    @SuppressWarnings({"deprecation"})
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);
    }
}
