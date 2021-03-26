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

import java.io.Serializable;

import com.sun.faces.RIConstants;
import com.sun.faces.flow.builder.FlowBuilderImpl;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.builder.FlowBuilder;
import jakarta.faces.flow.builder.FlowBuilderParameter;
import jakarta.inject.Named;

/*
 * This is an application scoped bean named with a well-defined,
 * but Mojarra private, name.  ApplicationAssociate.loadFlowsFromJars()
 * uses this class to cause any flows defined in this way to be
 * built using the FlowBuilder API.
 *
 * A better way is to @Inject the extension directly but this doesn't
 * seem to work in the version of weld we have.
 */
@Named(RIConstants.FLOW_DISCOVERY_CDI_HELPER_BEAN_NAME)
@Dependent
public class FlowDiscoveryCDIHelper implements Serializable {

    private static final long serialVersionUID = 6217421203074690365L;

    @Produces
    @FlowBuilderParameter
    FlowBuilder createFlowBuilder() {
        return new FlowBuilderImpl(FacesContext.getCurrentInstance());
    }

}
