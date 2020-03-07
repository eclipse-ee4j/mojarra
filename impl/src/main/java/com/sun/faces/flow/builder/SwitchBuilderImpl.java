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

package com.sun.faces.flow.builder;

import com.sun.faces.flow.SwitchNodeImpl;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.flow.builder.SwitchBuilder;
import jakarta.faces.flow.builder.SwitchCaseBuilder;

public class SwitchBuilderImpl extends SwitchBuilder {

    private FlowBuilderImpl root;
    private String switchId;
    private SwitchNodeImpl switchNode;
    private SwitchCaseBuilderImpl switchCaseBuilder;

    SwitchBuilderImpl(FlowBuilderImpl root, String id) {
        this.root = root;
        switchId = id;
        switchNode = new SwitchNodeImpl(id);
        root._getFlow()._getSwitches().put(id, switchNode);
        switchCaseBuilder = new SwitchCaseBuilderImpl(this);
    }

    @Override
    public SwitchCaseBuilder defaultOutcome(String outcome) {
        Util.notNull("outcome", outcome);
        switchNode.setDefaultOutcome(outcome);
        return switchCaseBuilder;
    }

    @Override
    public SwitchCaseBuilder defaultOutcome(ValueExpression outcome) {
        Util.notNull("outcome", outcome);
        switchNode.setDefaultOutcome(outcome);
        return switchCaseBuilder;
    }

    @Override
    public SwitchBuilderImpl markAsStartNode() {
        root._getFlow().setStartNodeId(switchId);
        return this;
    }

    @Override
    public SwitchCaseBuilder switchCase() {
        return switchCaseBuilder.switchCase();
    }

    FlowBuilderImpl getRoot() {
        return root;
    }

    SwitchNodeImpl getSwitchNode() {
        return switchNode;
    }

}
