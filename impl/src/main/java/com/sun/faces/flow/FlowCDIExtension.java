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

package com.sun.faces.flow;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;
import com.sun.faces.util.cdi11.CDIUtil;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.faces.flow.FlowScoped;

public class FlowCDIExtension implements Extension {

    private Map<Contextual<?>, FlowCDIContext.FlowBeanInfo> flowScopedBeanFlowIds;
    private boolean isCdiOneOneOrGreater = false;
    private CDIUtil cdiUtil = null;

    private static final Logger LOGGER = FacesLogger.FLOW.getLogger();

    public FlowCDIExtension() {
        flowScopedBeanFlowIds = new ConcurrentHashMap<>();
        isCdiOneOneOrGreater = Util.isCdiOneOneOrLater(null);
    }

    public void processBean(@Observes ProcessBean<?> event) {
        FlowScoped flowScoped = event.getAnnotated().getAnnotation(FlowScoped.class);
        if (null != flowScoped) {
            FlowCDIContext.FlowBeanInfo fbi = new FlowCDIContext.FlowBeanInfo();
            fbi.definingDocumentId = flowScoped.definingDocumentId();
            fbi.id = flowScoped.value();
            flowScopedBeanFlowIds.put(event.getBean(), fbi);
        }
    }

    public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addScope(FlowScoped.class, true, true);
    }

    void afterBeanDiscovery(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {
        event.addContext(new FlowCDIContext(flowScopedBeanFlowIds));
        flowScopedBeanFlowIds.clear();

        if (isCdiOneOneOrGreater) {
            Class clazz = null;
            try {
                clazz = Class.forName("com.sun.faces.flow.FlowCDIEventFireHelperImpl");
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
                return;
            }
            if (null == cdiUtil) {
                ServiceLoader<CDIUtil> oneCdiUtil = ServiceLoader.load(CDIUtil.class);
                for (CDIUtil oneAndOnly : oneCdiUtil) {
                    if (null != cdiUtil) {
                        String message = "Must only have one implementation of CDIUtil available";
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, message);
                        }
                        throw new IllegalStateException(message);
                    }
                    cdiUtil = oneAndOnly;
                }

            }
            if (null != cdiUtil) {
                Bean bean = cdiUtil.createHelperBean(beanManager, clazz);
                event.addBean(bean);
            } else if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Unable to obtain CDI 1.1 utilities for Mojarra");
            }

        }

    }

}
