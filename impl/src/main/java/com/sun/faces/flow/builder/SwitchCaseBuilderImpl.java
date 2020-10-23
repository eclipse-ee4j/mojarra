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

import com.sun.faces.flow.SwitchCaseImpl;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.flow.builder.SwitchCaseBuilder;

public class SwitchCaseBuilderImpl extends SwitchCaseBuilder {

    private SwitchBuilderImpl root;
    private SwitchCaseImpl myCase;

    public SwitchCaseBuilderImpl(SwitchBuilderImpl root) {
        this.root = root;
        myCase = null;
    }

    public SwitchCaseImpl getNavigationCase() {
        return myCase;
    }

    @Override
    public SwitchCaseBuilder switchCase() {
        SwitchCaseBuilderImpl result = new SwitchCaseBuilderImpl(root);
        result.myCase = new SwitchCaseImpl();
        root.getSwitchNode()._getCases().add(result.myCase);
        return result;
    }

    @Override
    public SwitchCaseBuilder condition(ValueExpression expression) {
        Util.notNull("expression", expression);
        myCase.setConditionExpression(expression);
        return this;
    }

    @Override
    public SwitchCaseBuilder condition(String expression) {
        Util.notNull("expression", expression);
        myCase.setCondition(expression);
        return this;
    }

    @Override
    public SwitchCaseBuilder fromOutcome(String outcome) {
        Util.notNull("outcome", outcome);
        myCase.setFromOutcome(outcome);
        return this;
    }

}
