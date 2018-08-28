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

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
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

import com.sun.faces.application.NavigationHandlerImpl;
import com.sun.faces.config.manager.DbfFactory;
import com.sun.faces.util.Util;

@Named
@SessionScoped
public class NavigationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List testResultList;

    public NavigationBean() {
    }

    public String getNavigationHandler() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();
        ViewMapDestroyedListener listener = new ViewMapDestroyedListener();
        app.subscribeToEvent(PreDestroyViewMapEvent.class, UIViewRoot.class, listener);
        try {
            loadTestResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        NavigationHandlerImpl navHandler = (NavigationHandlerImpl) app.getNavigationHandler();
        String newViewId;
        UIViewRoot page;
        boolean gotException = false;

        for (int i = 0; i < testResultList.size(); i++) {
            TestResult testResult = (TestResult) testResultList.get(i);
            Boolean conditionResult = null;
            if (testResult.condition != null) {
                conditionResult = (Boolean) app.getExpressionFactory()
                        .createValueExpression(fc.getELContext(), testResult.condition, Boolean.class).getValue(fc.getELContext());
            }
            System.out.println("Testing from-view-id=" + testResult.fromViewId + " from-action=" + testResult.fromAction + " from-outcome="
                    + testResult.fromOutcome + " if=" + testResult.condition);
            page = Util.getViewHandler(fc).createView(fc, null);
            page.setViewId(testResult.fromViewId);
            page.setLocale(Locale.US);
            page.getViewMap(); // cause the map to be created
            fc.setViewRoot(page);
            listener.reset();
            try {
                navHandler.handleNavigation(fc, testResult.fromAction, testResult.fromOutcome);
            } catch (Exception e) {
                // exception is valid only if context or fromoutcome is null.
                assertTrue(testResult.fromOutcome == null);
                gotException = true;
            }
            if (!gotException) {
                if (!testResult.fromViewId.equals(testResult.toViewId) && (testResult.fromOutcome != null || testResult.condition != null)
                        && (testResult.condition == null || conditionResult != null)) {
                    assertTrue(listener.getPassedEvent() instanceof PreDestroyViewMapEvent);
                } else {
                    assertTrue(!listener.wasProcessEventInvoked());
                    assertTrue(listener.getPassedEvent() == null);
                }
                listener.reset();
                newViewId = fc.getViewRoot().getViewId();
                if (testResult.fromOutcome == null && testResult.condition == null) {
                    listener.reset();
                    System.out.println("assertTrue(" + newViewId + ".equals(" + testResult.fromViewId + "))");
                    assertTrue(newViewId.equals(testResult.fromViewId));
                }
                // test assumption: if condition is false, we advance to some other view
                else if (testResult.condition != null && conditionResult == false) {
                    listener.reset();
                    System.out.println("assertTrue(!" + newViewId + ".equals(" + testResult.toViewId + "))");
                    assertTrue(!newViewId.equals(testResult.toViewId));
                } else {
                    listener.reset();
                    System.out.println("assertTrue(" + newViewId + ".equals(" + testResult.toViewId + "))");
                    assertTrue(newViewId.equals(testResult.toViewId));
                }
            }
        }
        app.unsubscribeFromEvent(PreDestroyViewMapEvent.class, UIViewRoot.class, listener);
        return "SUCCESS";
    }

    private void loadTestResultList() throws Exception {
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
    }

    private void createAndAccrueTestResult(String fromViewId, String fromAction, String fromOutcome, String condition, String toViewId) {
        if (testResultList == null) {
            testResultList = new ArrayList();
        }
        TestResult testResult = new TestResult();
        testResult.fromViewId = fromViewId;
        testResult.fromAction = fromAction;
        testResult.fromOutcome = fromOutcome;
        testResult.condition = condition;
        testResult.toViewId = toViewId;
        testResultList.add(testResult);
    }

    class TestResult extends Object {
        public String fromViewId = null;
        public String fromAction = null;
        public String fromOutcome = null;
        public String condition = null;
        public String toViewId = null;
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
