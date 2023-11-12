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

import jakarta.faces.context.FacesContext;

class PackageUtils {

    private PackageUtils() {
    }

    public static String generateCreatedBy(FacesContext facesContext) {
        String applicationContextPath = "unitTest";
        try {
            applicationContextPath = facesContext.getExternalContext().getApplicationContextPath();
        } catch (Throwable e) {
            // ignore
        }
        return applicationContextPath + " " + Thread.currentThread().toString() + " " + System.currentTimeMillis();
    }

    /**
     * <p>
     * Identify and return the class loader that is associated with the calling
     * web application.
     * </p>
     *
     * @throws FacesException if the web application class loader cannot be
     * identified
     */
    public static ClassLoader getContextClassLoader2() throws FacesException {
        // J2EE 1.3 (and later) containers are required to make the
        // web application class loader visible through the context
        // class loader of the current thread.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new FacesException("getContextClassLoader");
        }

        return classLoader;
    }

    public static boolean isAnyNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given object equals one of the given
     * objects.
     *
     * @param <T> The generic object type.
     * @param object The object to be checked if it equals one of the given
     * objects.
     * @param objects The argument list of objects to be tested for equality.
     * @return <code>true</code> if the given object equals one of the given
     * objects.
     */
    @SafeVarargs
    public static <T> boolean isOneOf(T object, T... objects) {
        for (Object other : objects) {
            if (object == null ? other == null : object.equals(other)) {
                return true;
            }
        }
        return false;
    }
}
