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
import static java.util.logging.Level.FINE;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.enterprise.event.Observes;
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
 *  Use the CDI annotation scanning feature to find all beans annotated
 *  with @FlowDefinition. The scanning work done here is leveraged
 *  in FlowDiscoveryCDIHelper.discoverFlows().
 *
 *  Use BeforeBeanDiscovery to manually register the  bean FlowDiscoveryCDIHelper.
 *  This is needed since CDI doesn't scan archives that contain extensions,
 *  such as the Mojarra jar
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

    private static final Logger LOGGER = FacesLogger.FLOW.getLogger();
    private List<Producer<Flow>> flowProducers = new CopyOnWriteArrayList<>();


    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        addAnnotatedTypes(beforeBeanDiscovery, beanManager, FlowDiscoveryCDIHelper.class);
    }

    void findFlowDefiners(@Observes ProcessProducer<?, Flow> processProducer) {
        if (processProducer.getAnnotatedMember().isAnnotationPresent(FlowDefinition.class)) {
            flowProducers.add(processProducer.getProducer());
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, "Discovered Flow Producer {0}", processProducer.getProducer().toString());
            }
        }
    }

    public List<Producer<Flow>> getFlowProducers() {
        return flowProducers;
    }

}
