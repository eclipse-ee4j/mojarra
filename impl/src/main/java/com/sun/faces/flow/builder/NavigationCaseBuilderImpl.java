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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.flow.builder.NavigationCaseBuilder;

public class NavigationCaseBuilderImpl extends NavigationCaseBuilder {

    private FlowBuilderImpl root;
    private MutableNavigationCase navCase;

    public NavigationCaseBuilderImpl(FlowBuilderImpl root) {
        navCase = new MutableNavigationCase();
        this.root = root;
    }

    @Override
    public NavigationCaseBuilder toFlowDocumentId(String toFlowDocumentId) {
        Util.notNull("toFlowDocumentId", toFlowDocumentId);
        navCase.setToFlowDocumentId(toFlowDocumentId);
        return this;
    }

    @Override
    public NavigationCaseBuilder fromAction(String fromAction) {
        Util.notNull("fromAction", fromAction);
        navCase.setFromAction(fromAction);
        return this;
    }

    @Override
    public NavigationCaseBuilder fromOutcome(String fromOutcome) {
        Util.notNull("fromOutcome", fromOutcome);
        navCase.setFromOutcome(fromOutcome);
        return this;
    }

    @Override
    public NavigationCaseBuilder fromViewId(String fromViewId) {
        Util.notNull("fromViewId", fromViewId);
        navCase.setFromViewId(fromViewId);
        Map<String, Set<NavigationCase>> rules = root._getFlow()._getNavigationCases();
        Set<NavigationCase> cases = rules.get(fromViewId);
        if (null == cases) {
            cases = new CopyOnWriteArraySet<>();
            rules.put(fromViewId, cases);
        }
        cases.add(navCase);
        return this;
    }

    @Override
    public NavigationCaseBuilder toViewId(String toViewId) {
        Util.notNull("toViewId", toViewId);
        navCase.setToViewId(toViewId);
        return this;
    }

    @Override
    public NavigationCaseBuilder condition(String condition) {
        Util.notNull("condition", condition);
        navCase.setCondition(condition);
        return this;
    }

    @Override
    public NavigationCaseBuilder condition(ValueExpression condition) {
        Util.notNull("condition", condition);
        navCase.setConditionExpression(condition);
        return this;
    }

    @Override
    public RedirectBuilder redirect() {
        navCase.setRedirect(true);
        return new RedirectBuilderImpl();
    }

    private class RedirectBuilderImpl extends NavigationCaseBuilder.RedirectBuilder {

        public RedirectBuilderImpl() {
        }

        @Override
        public RedirectBuilder parameter(String name, String value) {
            Util.notNull("name", name);
            Util.notNull("value", value);
            Map<String, List<String>> redirectParams = navCase.getParameters();
            List<String> values = redirectParams.get(name);
            if (null == values) {
                values = new CopyOnWriteArrayList<>();
                redirectParams.put(name, values);
            }
            values.add(value);
            return this;
        }

        @Override
        public RedirectBuilder includeViewParams() {
            navCase.isIncludeViewParams();
            return this;
        }

    }

}
