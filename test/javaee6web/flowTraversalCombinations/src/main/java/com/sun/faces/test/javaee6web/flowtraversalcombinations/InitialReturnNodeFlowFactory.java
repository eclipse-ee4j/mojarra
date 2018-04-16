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

package com.sun.faces.test.javaee6web.flowtraversalcombinations;

import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.faces.flow.Flow;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.FlowBuilderParameter;
import javax.faces.flow.builder.FlowDefinition;
import javax.inject.Named;

@Named
@ApplicationScoped
public class InitialReturnNodeFlowFactory implements Serializable {
    
    @Produces
    @FlowDefinition
    public Flow defineFlow(@FlowBuilderParameter FlowBuilder flowBuilder) {
        String flowId = "initial-return-node-flow";
        flowBuilder.id("", flowId);
        
        flowBuilder.viewNode(flowId, "/" + flowId + "/first_page.xhtml").markAsStartNode();
        
        flowBuilder.returnNode("ReturnToParentFlow")
                   .fromOutcome("#{initialReturnNodeFlowBean.parentNodeToBeReturned}");
         
        return flowBuilder.getFlow();
    }
}
