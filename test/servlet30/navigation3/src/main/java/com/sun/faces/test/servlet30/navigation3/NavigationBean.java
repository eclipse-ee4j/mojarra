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

package com.sun.faces.test.servlet30.navigation3;

import static com.sun.faces.util.Util.getViewHandler;
import static java.util.Locale.US;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.manager.DbfFactory;

@Named
@SessionScoped
public class NavigationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<TestResult> testResults;

    public String getNavigationHandler() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        ViewMapDestroyedListener viewMapDestroyedListener = new ViewMapDestroyedListener();
        application.subscribeToEvent(PreDestroyViewMapEvent.class, UIViewRoot.class, viewMapDestroyedListener);

        loadTestResultList();

        NavigationHandler navigationHandler = application.getNavigationHandler();
        String newViewId;
        boolean gotException = false;

        for (TestResult testResult : testResults) {
            Boolean conditionResult = null;
            if (testResult.condition != null) {
                conditionResult = (Boolean)
                        application.getExpressionFactory()
                                   .createValueExpression(
                                           facesContext.getELContext(),
                                           testResult.condition,
                                           Boolean.class)
                                   .getValue(facesContext.getELContext());
            }

            System.out.println(
                    "Testing from-view-id=" + testResult.fromViewId +
                    " from-action=" + testResult.fromAction +
                    " from-outcome=" + testResult.fromOutcome +
                    " if=" + testResult.condition);


            UIViewRoot viewRoot = getViewHandler(facesContext).createView(facesContext, "foo.xhtml");
            viewRoot.setViewId(testResult.fromViewId);
            viewRoot.setLocale(US);
            viewRoot.getViewMap(); // cause the map to be created
            facesContext.setViewRoot(viewRoot);
            viewMapDestroyedListener.reset();

            try {
                navigationHandler.handleNavigation(facesContext, testResult.fromAction, testResult.fromOutcome);
            } catch (Exception e) {
                // exception is valid only if context or fromoutcome is null.
                assertTrue(testResult.fromOutcome == null);
                gotException = true;
            }

            if (!gotException) {
                if (!testResult.fromViewId.equals(testResult.toViewId) && (testResult.fromOutcome != null || testResult.condition != null) && (testResult.condition == null || conditionResult != null)) {
                    assertTrue(viewMapDestroyedListener.getPassedEvent() instanceof PreDestroyViewMapEvent);
                } else {
                    assertTrue(!viewMapDestroyedListener.wasProcessEventInvoked());
                    assertTrue(viewMapDestroyedListener.getPassedEvent() == null);
                }

                viewMapDestroyedListener.reset();

                newViewId = facesContext.getViewRoot().getViewId();

                viewMapDestroyedListener.reset();

                if (testResult.fromOutcome == null && testResult.condition == null) {
                    System.out.println("assertTrue(" + newViewId + ".equals(" + testResult.fromViewId + "))");
                    assertTrue(newViewId.equals(testResult.fromViewId));
                } else if (testResult.condition != null && conditionResult == false) {
                    // Test assumption: if condition is false, we advance to some other view
                    System.out.println("assertTrue(!" + newViewId + ".equals(" + testResult.toViewId + "))");
                    assertTrue(!newViewId.equals(testResult.toViewId));
                } else {
                    System.out.println("assertTrue(" + newViewId + ".equals(" + testResult.toViewId + "))");
                    assertTrue(newViewId.equals(testResult.toViewId));
                }
            }
        }

        application.unsubscribeFromEvent(PreDestroyViewMapEvent.class, UIViewRoot.class, viewMapDestroyedListener);
        return "SUCCESS";
    }

    private void loadTestResultList() {
        try {
            DocumentBuilderFactory f = DbfFactory.getFactory();
            f.setNamespaceAware(false);
            f.setValidating(false);
            DocumentBuilder builder = f.newDocumentBuilder();

            Document d = builder.parse(((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext())
                    .getResourceAsStream("/WEB-INF/navigation-cases-2.xml"));
            NodeList navigationRules = d.getDocumentElement().getElementsByTagName("test");
            for (int i = 0; i < navigationRules.getLength(); i++) {
                Node test = navigationRules.item(i);
                NamedNodeMap attributes = test.getAttributes();
                Node fromViewId = attributes.getNamedItem("fromViewId");
                Node fromAction = attributes.getNamedItem("fromAction");
                Node fromOutput = attributes.getNamedItem("fromOutcome");
                Node condition = attributes.getNamedItem("if");
                Node toViewId = attributes.getNamedItem("toViewId");
                createAndAccrueTestResult(((fromViewId != null) ? fromViewId.getTextContent().trim() : null),
                        ((fromAction != null) ? fromAction.getTextContent().trim() : null),
                        ((fromOutput != null) ? fromOutput.getTextContent().trim() : null),
                        ((condition != null) ? condition.getTextContent().trim() : null),
                        ((toViewId != null) ? toViewId.getTextContent().trim() : null));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createAndAccrueTestResult(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId) {
        if (testResults == null) {
            testResults = new ArrayList<>();
        }

        TestResult testResult = new TestResult();
        testResult.fromViewId = fromViewId;
        testResult.fromAction = fromAction;
        testResult.fromOutcome = fromOutcome;
        testResult.condition = condition;
        testResult.toViewId = toViewId;
        testResults.add(testResult);
    }

    class TestResult extends Object {
        public String fromViewId;
        public String fromAction;
        public String fromOutcome;
        public String condition;
        public String toViewId;
    }

    private static final class ViewMapDestroyedListener implements SystemEventListener {

        private SystemEvent event;
        private boolean processEventInvoked;

        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            this.processEventInvoked = true;
            this.event = event;
        }

        @Override
        public boolean isListenerForSource(Object source) {
            return (source instanceof UIViewRoot);
        }

        public SystemEvent getPassedEvent() {
            return event;
        }

        public boolean wasProcessEventInvoked() {
            return processEventInvoked;
        }

        public void reset() {
            processEventInvoked = false;
            event = null;
        }
    }

    private String title = "Test JSF2 Navigation Handler";

    public String getTitle() {
        return title;
    }

    private String status = "";

    public String getStatus() {
        return status;
    }

}
