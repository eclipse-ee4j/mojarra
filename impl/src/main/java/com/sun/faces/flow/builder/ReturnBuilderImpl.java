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

import com.sun.faces.flow.ReturnNodeImpl;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.flow.builder.ReturnBuilder;

public class ReturnBuilderImpl extends ReturnBuilder {

    private FlowBuilderImpl root;
    String id;

    public ReturnBuilderImpl(FlowBuilderImpl root, String id) {
        this.root = root;
        this.id = id;
    }

    @Override
    public ReturnBuilder markAsStartNode() {
        root._getFlow().setStartNodeId(id);
        return this;
    }

    @Override
    public ReturnBuilder fromOutcome(String outcome) {
        Util.notNull("outcome", outcome);
        ReturnNodeImpl returnNode = new ReturnNodeImpl(id);
        returnNode.setFromOutcome(outcome);
        root._getFlow()._getReturns().put(id, returnNode);
        return this;
    }

    @Override
    public ReturnBuilder fromOutcome(ValueExpression outcome) {
        Util.notNull("outcome", outcome);
        ReturnNodeImpl returnNode = new ReturnNodeImpl(id);
        returnNode.setFromOutcome(outcome);
        root._getFlow()._getReturns().put(id, returnNode);

        return this;
    }

}
