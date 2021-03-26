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

package com.sun.faces.flow;

import static com.sun.faces.cdi.CdiUtils.addAnnotatedTypes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.faces.flow.FlowCDIContext.FlowBeanInfo;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.faces.flow.FlowScoped;

public class FlowCDIExtension implements Extension {

    private Map<Contextual<?>, FlowCDIContext.FlowBeanInfo> flowScopedBeanFlowIds = new ConcurrentHashMap<>();

    /**
     * Before bean discovery.
     *
     * @param beforeBeanDiscovery the before bean discovery.
     * @param beanManager the bean manager.
     */
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        addAnnotatedTypes(beforeBeanDiscovery, beanManager, FlowCDIEventFireHelperImpl.class);
        beforeBeanDiscovery.addScope(FlowScoped.class, true, true);
    }

    public void processBean(@Observes ProcessBean<?> event) {
        FlowScoped flowScoped = event.getAnnotated().getAnnotation(FlowScoped.class);

        if (flowScoped != null) {
            flowScopedBeanFlowIds.put(
                event.getBean(),
                new FlowBeanInfo(flowScoped.definingDocumentId(), flowScoped.value()));
        }
    }

    void afterBeanDiscovery(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {
        event.addContext(new FlowCDIContext(flowScopedBeanFlowIds));
        flowScopedBeanFlowIds.clear();
    }

}
