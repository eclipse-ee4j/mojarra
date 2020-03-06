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

package com.sun.faces.mgbean;

import java.util.List;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This doesn't really do anything, aside from being a place holder if the managed bean is configured incorrectly.
 * </p>
 */
public class ErrorBean extends BeanBuilder {

    // ------------------------------------------------------------ Constructors

    public ErrorBean(ManagedBeanInfo beanInfo, String message) {
        super(beanInfo);
        if (message == null || message.length() == 0) {
            throw new IllegalArgumentException();
        }
        queueMessage(message);
    }

    public ErrorBean(ManagedBeanInfo beanInfo, List<String> messages) {
        super(beanInfo);
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException();
        }
        queueMessages(messages);
    }

    // ------------------------------------------------ Methods from BeanBuilder

    @Override
    synchronized void bake() {
        // no-op
    }

    @Override
    protected void buildBean(Object bean, FacesContext context) {
        // no-op
    }
}
