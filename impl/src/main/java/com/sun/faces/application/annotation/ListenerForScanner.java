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

import com.sun.faces.util.Util;

import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.ListenersFor;

/**
 * <code>Scanner</code> implementation responsible for {@link ListenerFor} annotations.
 */
class ListenerForScanner implements Scanner {

    // ---------------------------------------------------- Methods from Scanner

    @Override
    public Class<? extends Annotation> getAnnotation() {

        return ListenerFor.class;

    }

    @Override
    public RuntimeAnnotationHandler scan(Class<?> clazz) {

        Util.notNull("clazz", clazz);

        ListenerForHandler handler = null;
        ListenerFor listenerFor = clazz.getAnnotation(ListenerFor.class);
        if (listenerFor != null) {
            handler = new ListenerForHandler(new ListenerFor[] { listenerFor });
        } else {
            ListenersFor listenersFor = clazz.getAnnotation(ListenersFor.class);
            if (listenersFor != null) {
                handler = new ListenerForHandler(listenersFor.value());
            }
        }

        return handler;

    }

}
