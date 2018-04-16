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

package javax.faces.component;

/**
 * <p>
 * Base class for classes that wrap a <code>MethodBinding</code> and implement a faces listener-like
 * interface.
 * </p>
 *
 */

abstract class MethodBindingAdapterBase extends Object {

    /**
     * <p>
     * Recursively interrogate the <code>cause</code> property of the argument
     * <code>exception</code> and stop recursing either when it is an instance of
     * <code>expectedExceptionClass</code> or <code>null</code>. Return the result.
     * </p>
     */

    Throwable getExpectedCause(Class expectedExceptionClass, Throwable exception) {
        Throwable result = exception.getCause();
        if (null != result) {
            if (!expectedExceptionClass.isAssignableFrom(result.getClass())) {
                result = getExpectedCause(expectedExceptionClass, result);
            }
        }
        return result;
    }

}
