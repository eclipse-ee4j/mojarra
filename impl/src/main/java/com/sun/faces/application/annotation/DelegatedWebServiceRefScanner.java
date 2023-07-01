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

package com.sun.faces.application.annotation;

import java.lang.annotation.Annotation;

import jakarta.xml.ws.WebServiceRef;

/**
 * <code>Scanner</code> implementation responsible for {@link WebServiceRef} annotations.
 *
 * <p>
 * Note this will delegate down to the WebServiceRefScanner so we can fail gracefully when JavaEE is not available.
 * </p>
 */
class DelegatedWebServiceRefScanner implements Scanner {

    private Scanner delegate;

    public DelegatedWebServiceRefScanner() {
        try {
            delegate = new WebServiceRefScanner();
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
        }
    }

    /**
     * Delegate to the actual WebServiceRef scanner.
     *
     * @return the annotation.
     */
    @Override
    public Class<? extends Annotation> getAnnotation() {
        if (delegate != null) {
            return delegate.getAnnotation();
        }
        return null;
    }

    /**
     * Delegate to the actual EBJ scanner.
     *
     * @param clazz the class.
     * @return the runtime annotation handler.
     */
    @Override
    public RuntimeAnnotationHandler scan(Class<?> clazz) {
        if (delegate != null) {
            return delegate.scan(clazz);
        }
        return null;
    }
}
