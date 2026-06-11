/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package com.sun.faces.application.applicationimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

class ComponentBindingScopeCheckerTest {

    @Test
    void warnsOnceWhenSameComponentSurvivesAcrossRequests() {
        UIComponent leaked = new UIOutput();
        ValueExpression binding = binding("#{sessionBean.component}");
        List<FacesMessage> messages = new ArrayList<>();

        try (MockedStatic<FacesContext> staticContext = mockStatic(FacesContext.class)) {
            check(staticContext, request(true, messages), binding, leaked);
            assertTrue(messages.isEmpty(), "First sighting must not warn");

            check(staticContext, request(true, messages), binding, leaked);
            assertEquals(1, messages.size(), "Cross-request reuse of the same instance must warn");
            assertEquals(FacesMessage.SEVERITY_WARN, messages.get(0).getSeverity());
            // Summary and detail must both resolve from the LogStrings bundle, not fall back to the raw message id.
            assertTrue(messages.get(0).getSummary().contains("survived across requests"), messages.get(0).getSummary());
            assertTrue(messages.get(0).getDetail().contains("findComponent"), messages.get(0).getDetail());

            check(staticContext, request(true, messages), binding, leaked);
            assertEquals(1, messages.size(), "A further request must not enqueue a duplicate page message");
        }
    }

    @Test
    void doesNotWarnOnReEvaluationWithinTheSameRequest() {
        UIComponent component = new UIOutput();
        ValueExpression binding = binding("#{requestBean.component}");
        List<FacesMessage> messages = new ArrayList<>();

        try (MockedStatic<FacesContext> staticContext = mockStatic(FacesContext.class)) {
            FacesContext sameRequest = request(true, messages);
            check(staticContext, sameRequest, binding, component);
            check(staticContext, sameRequest, binding, component);
            assertTrue(messages.isEmpty(), "Same instance within one request is normal, must not warn");
        }
    }

    @Test
    void doesNotWarnForDistinctComponentInstancesAcrossRequests() {
        ValueExpression binding = binding("#{requestBean.component}");
        List<FacesMessage> messages = new ArrayList<>();

        try (MockedStatic<FacesContext> staticContext = mockStatic(FacesContext.class)) {
            check(staticContext, request(true, messages), binding, new UIOutput());
            check(staticContext, request(true, messages), binding, new UIOutput());
            assertTrue(messages.isEmpty(), "A fresh instance each request is the request-scoped case, must not warn");
        }
    }

    @Test
    void doesNotWarnOutsideDevelopmentStage() {
        UIComponent leaked = new UIOutput();
        ValueExpression binding = binding("#{sessionBean.component}");
        List<FacesMessage> messages = new ArrayList<>();

        try (MockedStatic<FacesContext> staticContext = mockStatic(FacesContext.class)) {
            check(staticContext, request(false, messages), binding, leaked);
            check(staticContext, request(false, messages), binding, leaked);
            assertTrue(messages.isEmpty(), "Must be silent outside Development stage");
        }
    }

    // ---- helpers ----

    private static void check(MockedStatic<FacesContext> staticContext, FacesContext context, ValueExpression binding, UIComponent component) {
        staticContext.when(FacesContext::getCurrentInstance).thenReturn(context);
        ComponentBindingScopeChecker.check(context, binding, component);
    }

    /**
     * A mock {@link FacesContext} standing in for one request: its own request map (so each gets a distinct request
     * sequence) and a shared message list that {@code addMessage} appends to and {@code getMessageList} returns.
     */
    private static FacesContext request(boolean development, List<FacesMessage> messages) {
        Map<String, Object> requestMap = new HashMap<>();
        ExternalContext externalContext = mock(ExternalContext.class);
        when(externalContext.getRequestMap()).thenReturn(requestMap);

        Application application = mock(Application.class);
        when(application.getMessageBundle()).thenReturn(null);

        FacesContext context = mock(FacesContext.class);
        when(context.isProjectStage(ProjectStage.Development)).thenReturn(development);
        when(context.getExternalContext()).thenReturn(externalContext);
        when(context.getApplication()).thenReturn(application);
        when(context.getMessageList()).thenReturn(messages);
        doAnswer(invocation -> messages.add(invocation.getArgument(1))).when(context).addMessage(isNull(), any(FacesMessage.class));
        return context;
    }

    private static ValueExpression binding(String expressionString) {
        ValueExpression binding = mock(ValueExpression.class);
        when(binding.getExpressionString()).thenReturn(expressionString);
        return binding;
    }
}
