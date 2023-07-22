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

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.SystemEventListener;

/**
 * {@link RuntimeAnnotationHandler} responsible for processing {@link ListenerFor} annotations.
 */
class ListenerForHandler implements RuntimeAnnotationHandler {

    private final ListenerFor[] listenersFor;

    // ------------------------------------------------------------ Constructors

    public ListenerForHandler(ListenerFor[] listenersFor) {

        this.listenersFor = listenersFor;

    }

    // ----------------------------------- Methods from RuntimeAnnotationHandler

    @SuppressWarnings({ "UnusedDeclaration" })
    @Override
    public void apply(FacesContext ctx, Object... params) {

        Object listener;
        UIComponent target;
        if (params.length == 2) {
            // handling @ListenerFor on a Renderer
            listener = params[0];
            target = (UIComponent) params[1];
        } else {
            // handling @ListenerFor on a UIComponent
            listener = params[0];
            target = (UIComponent) params[0];
        }

        if (listener instanceof ComponentSystemEventListener) {
            for (ListenerFor listenerFor : listenersFor) {
                target.subscribeToEvent(listenerFor.systemEventClass(), (ComponentSystemEventListener) listener);
            }
        } else if (listener instanceof SystemEventListener) {
            Class<?> sourceClassValue = null;
            Application app = ctx.getApplication();
            for (ListenerFor listenerFor : listenersFor) {
                sourceClassValue = listenerFor.sourceClass();
                if (sourceClassValue == Void.class) {
                    app.subscribeToEvent(listenerFor.systemEventClass(), (SystemEventListener) listener);
                } else {
                    app.subscribeToEvent(listenerFor.systemEventClass(), listenerFor.sourceClass(), (SystemEventListener) listener);

                }
            }
        }

    }

}
