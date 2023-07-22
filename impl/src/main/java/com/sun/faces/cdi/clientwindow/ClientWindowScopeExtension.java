/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package com.sun.faces.cdi.clientwindow;

import static com.sun.faces.cdi.CdiUtils.addAnnotatedTypes;

import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.faces.lifecycle.ClientWindowScoped;

public class ClientWindowScopeExtension implements Extension
{

    private static final Logger LOGGER = FacesLogger.CLIENTWINDOW.getLogger();

    public ClientWindowScopeExtension() {
        LOGGER.finest("Constructor @ClientWindowScope CDI Extension called");
    }

    public void beforeBean(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        addAnnotatedTypes(beforeBeanDiscovery, beanManager);
    }

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager)
    {
        beforeBeanDiscovery.addScope(ClientWindowScoped.class, true, true);
    }

    public void processBean(@Observes ProcessBean<?> event) {
        ClientWindowScoped clientWindowScoped = event.getAnnotated().getAnnotation(ClientWindowScoped.class);
        if (clientWindowScoped != null) {
            LOGGER.finest("Processing occurrence of @ClientWindowScoped");
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        LOGGER.finest("Adding @ClientWindowScope context to CDI runtime");
        afterBeanDiscovery.addContext(new ClientWindowScopeContext());
    }
}
