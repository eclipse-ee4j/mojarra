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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.builder.FlowDefinition;

/*
 *  This is the hook into the bootstrapping of the entire feature.
 *
 *  Use the CDI anntation scanning feature to find all beans annotated
 *  with @FlowDefinition.  The scanning work done here is leveraged
 *  in FlowDiscoveryCDIHelper.discoverFlows().
 *
 *  Use BeforeBeanDiscovery to manually register an application scoped
 *  bean FlowDiscoveryCDIHelper.  I would rather not do this, but I
 *  couldn't get the system to find this bean in any other way.  I think
 *  this may have something to do with CDI being told not to scan within
 *  jakarta.faces.jar, or something like that.
 *
 *  Use AfterBeanDiscovery to add a custom Context.  This is necessary
 *  because it was the only way I found that actually worked that let me
 *  pass in the results of the scanning into something I could invoke in
 *  when processing the Faces PostConstructApplicationEvent.
 *
 *  Use ProcessBean to build up a tuple for each bean annotated with
 *  @FlowDefinition { definingClass, flow-id, defining-document-id }.  A
 *  data structure containing all the tuples is passed to
 *  FlowDiscoveryCDIContext.
 *
 */
public class FlowDiscoveryCDIExtension implements Extension {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.FLOW.getLogger();
    private List<Producer<Flow>> flowProducers;

    public FlowDiscoveryCDIExtension() {
        flowProducers = new CopyOnWriteArrayList<>();

    }

    public List<Producer<Flow>> getFlowProducers() {
        return flowProducers;
    }

    void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
        AnnotatedType flowDiscoveryHelper = beanManager.createAnnotatedType(FlowDiscoveryCDIHelper.class);
        event.addAnnotatedType(flowDiscoveryHelper);

    }

    <T> void findFlowDefiners(@Observes ProcessProducer<T, Flow> pp) {
        if (pp.getAnnotatedMember().isAnnotationPresent(FlowDefinition.class)) {
            flowProducers.add(pp.getProducer());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Discovered Flow Producer {0}", pp.getProducer().toString());
            }

        }
    }

}
