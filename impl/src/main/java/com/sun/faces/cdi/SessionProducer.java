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

package com.sun.faces.cdi;

import static com.sun.faces.util.ReflectionUtils.findClass;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * The SessionProducer is the CDI producer that allows EL resolving of #{session}
 * </p>
 *
 * @since 2.3
 * @see ExternalContext#getSession(boolean)
 */
public class SessionProducer extends CdiProducer<Object> {

    private static final Optional<Class<?>> HTTP_SESSION_CLASS = findClass("jakarta.servlet.http.HttpSession");

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public SessionProducer(BeanManager beanManager) {
        CdiProducer<Object> producer = super.name("session").scope(RequestScoped.class); // #5222

        if (HTTP_SESSION_CLASS.isPresent()) {
            producer = producer.beanClass(beanManager, HTTP_SESSION_CLASS.get()); // #5561
        }

        producer.create(e -> FacesContext.getCurrentInstance().getExternalContext().getSession(false));
    }

}
