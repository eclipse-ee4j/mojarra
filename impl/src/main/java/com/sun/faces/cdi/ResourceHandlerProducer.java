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

import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.Application;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * The ResourceHandlerProducer is the CDI producer that allows you to inject the ResourceHandler and to do EL resolving
 * of #{resource}
 * </p>
 *
 * @since 2.3
 * @see ResourceHandler
 * @see Application#getResourceHandler()
 */
public class ResourceHandlerProducer extends CdiProducer<ResourceHandler> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public ResourceHandlerProducer(BeanManager beanManager) {
        super.name("resource")
            .scope(RequestScoped.class)
            .beanClass(beanManager, ResourceHandler.class)
            .types(ResourceHandler.class)
            .create(e -> FacesContext.getCurrentInstance().getApplication().getResourceHandler());
    }

}
