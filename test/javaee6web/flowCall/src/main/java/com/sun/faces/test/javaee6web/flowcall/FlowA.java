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

package com.sun.faces.test.javaee6web.flowcall;

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
    public Flow buildMyFlow(@FlowBuilderParameter FlowBuilder flowBuilder) {
        String flowId = "flow-a";
        flowBuilder.id("unique", flowId);
        flowBuilder.returnNode("taskFlowReturn1").
                fromOutcome("#{flow_a_Bean.returnValue}");
        flowBuilder.inboundParameter("param1FromFlowB", "#{flowScope.param1Value}");
        flowBuilder.inboundParameter("param2FromFlowB", "#{flowScope.param2Value}");
        flowBuilder.flowCallNode("callB").flowReference("", "flow-b").
                outboundParameter("param1FromFlowA", "param1Value").
                outboundParameter("param2FromFlowA", "param2Value");

        return flowBuilder.getFlow();
    }
}
