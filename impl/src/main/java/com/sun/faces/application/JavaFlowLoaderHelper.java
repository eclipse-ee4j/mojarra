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

package com.sun.faces.application;

import static com.sun.faces.util.Util.getCdiBeanManager;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.sun.faces.cdi.CdiUtils;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.flow.FlowDiscoveryCDIExtension;
import com.sun.faces.util.FacesLogger;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.builder.FlowDefinition;

public class JavaFlowLoaderHelper {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    synchronized void loadFlows(FacesContext context, FlowHandler flowHandler) throws IOException {
        BeanManager beanManager = getCdiBeanManager(context);

        FlowDiscoveryCDIExtension flowDiscoveryCDIExtension = CdiUtils.getBeanReference(beanManager, FlowDiscoveryCDIExtension.class);
        if (flowDiscoveryCDIExtension == null) {
            if (LOGGER.isLoggable(SEVERE)) {
                LOGGER.log(SEVERE, "Unable to obtain {0} from CDI implementation.  Flows described with {1} are unavailable.",
                        new String[] { FlowDiscoveryCDIExtension.class.getName(), FlowDefinition.class.getName() });
            }
            return;
        }

        List<Producer<Flow>> flowProducers = flowDiscoveryCDIExtension.getFlowProducers();
        if (!flowProducers.isEmpty()) {
            enableClientWindowModeIfNecessary(context);
        }

        WebConfiguration config = WebConfiguration.getInstance();
        for (Producer<Flow> flowProducer : flowProducers) {
            Flow toAdd = flowProducer.produce(beanManager.<Flow>createCreationalContext(null));
            if (toAdd == null) {
                LOGGER.log(SEVERE, "Flow producer method {0}() returned null.  Ignoring.", flowProducer.toString());
            } else {
                flowHandler.addFlow(context, toAdd);
                config.setHasFlows(true);
            }
        }
    }

    public static void enableClientWindowModeIfNecessary(FacesContext context) {
        String optionValue = ContextParam.CLIENT_WINDOW_MODE.getValue(context);

        boolean clientWindowNeedsEnabling = false;
        if ("none".equals(optionValue)) {
            clientWindowNeedsEnabling = true;

            LOGGER.log(WARNING, "{0} was set to none, but Faces Flows requires {0} is enabled.  Setting to ''url''.",
                    new Object[] { ContextParam.CLIENT_WINDOW_MODE.getName() });

        } else if (optionValue == null) {
            clientWindowNeedsEnabling = true;
        }

        if (clientWindowNeedsEnabling) {
            context.getExternalContext().getApplicationMap().put(JavaFlowLoaderHelper.class.getName(), Boolean.TRUE);
        }
    }
    
    public static boolean isClientWindowModeForciblyEnabled(FacesContext context) {
        return context.getExternalContext().getApplicationMap().get(JavaFlowLoaderHelper.class.getName()) == Boolean.TRUE;
    }
}
