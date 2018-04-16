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

package com.sun.faces.test.javaee6web.flowswitchcall;

import java.io.Serializable;
import javax.enterprise.inject.Produces;
import javax.faces.flow.Flow;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.FlowDefinition;
import javax.faces.flow.builder.FlowBuilderParameter;

public class FlowA implements Serializable {

    private static final long serialVersionUID = -7623501087369765218L;

    public FlowA() {
    }

    @Produces
    @FlowDefinition
    public Flow defineFlow(@FlowBuilderParameter FlowBuilder flowBuilder) {
        String flowId = "flow-a";
        flowBuilder.id("", flowId);
        flowBuilder.returnNode("taskFlowReturn1").
                fromOutcome("#{flow_a_Bean.returnValue}");
        flowBuilder.switchNode("switchA").defaultOutcome("defaultPage").
                switchCase().condition("#{flow_a_Bean.switchA_Case01}").fromOutcome("page01").
                switchCase().condition("#{flow_a_Bean.switchA_Case02}").fromOutcome("page02").
                switchCase().condition("#{flow_a_Bean.switchA_Case03}").fromOutcome("switchA_result");
        flowBuilder.switchNode("switchB").defaultOutcome("defaultPage").
                switchCase().condition("#{flow_a_Bean.switchB_Case01}").fromOutcome("page01").
                switchCase().condition("#{flow_a_Bean.switchB_Case02}").fromOutcome("switchB_result").
                switchCase().condition("#{flow_a_Bean.switchB_Case03}").fromOutcome("page03");
        flowBuilder.switchNode("switchC").defaultOutcome("#{flow_a_Bean.defaultOutcome}").
                switchCase().condition("#{flow_a_Bean.switchC_Case01}").fromOutcome("page01").
                switchCase().condition("#{flow_a_Bean.switchC_Case02}").fromOutcome("page02").
                switchCase().condition("#{flow_a_Bean.switchC_Case03}").fromOutcome("page03");

        return flowBuilder.getFlow();
    }
}
