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

package com.sun.faces.test.servlet30.navigation2;

import static com.sun.faces.util.Util.getViewHandler;
import static java.util.Locale.US;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
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

import com.sun.faces.application.NavigationHandlerImpl;
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

        try {
            loadTestResultList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        NavigationHandler navigationHandler = application.getNavigationHandler();
        String newViewId;
        boolean gotException = false;

        for (TestResult testResult : testResults) {

            System.out.println(
                    "Testing from-view-id=" + testResult.fromViewId +
                    " from-action=" + testResult.fromAction +
                    " from-outcome=" + testResult.fromOutcome);

            UIViewRoot viewRoot = getViewHandler(facesContext).createView(facesContext, testResult.fromViewId);

            System.out.println("\n\n****\n\n VIEW ID" + testResult.fromViewId);

            viewRoot.setViewId(testResult.fromViewId);
            viewRoot.setLocale(US);
            viewRoot.getViewMap(); // cause the map to be created
            facesContext.setViewRoot(viewRoot);
            viewMapDestroyedListener.reset();

            try {
                navigationHandler.handleNavigation(facesContext, testResult.fromAction, testResult.fromOutcome);
            } catch (Exception e) {
                // Exception is valid only if context or fromoutcome is null.
                assertTrue(testResult.fromOutcome == null);
                gotException = true;
            }

            if (!gotException) {
                if (!testResult.fromViewId.equals(testResult.toViewId) && testResult.fromOutcome != null) {
                    assertTrue(viewMapDestroyedListener.getPassedEvent() instanceof PreDestroyViewMapEvent);
                } else {
                    assertTrue(!viewMapDestroyedListener.wasProcessEventInvoked());
                    assertTrue(viewMapDestroyedListener.getPassedEvent() == null);
                }

                viewMapDestroyedListener.reset();

                newViewId = facesContext.getViewRoot().getViewId();

                viewMapDestroyedListener.reset();

                if (testResult.fromOutcome == null) {
                    System.out.println("assertTrue(" + newViewId + ".equals(" + testResult.fromViewId + "))");
                    assertTrue(newViewId.equals(testResult.fromViewId));
                } else {
                    System.out.println("assertTrue(" + newViewId + ".equals(" + testResult.toViewId + "))");
                    assertTrue(newViewId.equals(testResult.toViewId));
                }
            }
        }

        application.unsubscribeFromEvent(PreDestroyViewMapEvent.class, UIViewRoot.class, viewMapDestroyedListener);

        return "SUCCESS";
    }

    public String getSimilarViewIds() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        NavigationHandler navigationHandler = application.getNavigationHandler();
        UIViewRoot viewRoot = application.getViewHandler().createView(facesContext, "/dir1/dir2/dir3/test.xhtml");

        viewRoot.setLocale(US);
        facesContext.setViewRoot(viewRoot);

        try {
            navigationHandler.handleNavigation(facesContext, null, "home");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newViewId = facesContext.getViewRoot().getViewId();
        assertTrue("newViewId is: " + newViewId, "/dir1/dir2/dir3/home.xhtml".equals(newViewId));

        return "SUCCESS";
    }

    public String getSeparateRule() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();
        int cnt = 0;
        ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) app.getNavigationHandler();
        Map caseListMap = handler.getNavigationCases();
        Iterator iter = caseListMap.keySet().iterator();
        while (iter.hasNext()) {
            String fromViewId = (String) iter.next();
            if (fromViewId.equals("/login.xhtml")) {
                Set<NavigationCase> caseSet = (Set<NavigationCase>) caseListMap.get(fromViewId);
                for (NavigationCase navCase : caseSet) {
                    if (navCase.getFromViewId().equals("/login.xhtml")) {
                        cnt++;
                    }
                }
            }
        }
        assertTrue(cnt == 6);
        return "SUCCESS";
    }

    public String getWrappedNavigationHandler() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();
        ConfigurableNavigationHandler impl = new NavigationHandlerImpl();
        NavigationHandler parent = new WrapperNavigationHandler(impl);
        parent.handleNavigation(fc, "", "");

        int cnt = 0;
        Map caseListMap = impl.getNavigationCases();
        Iterator iter = caseListMap.keySet().iterator();
        while (iter.hasNext()) {
            String fromViewId = (String) iter.next();
            if (fromViewId.equals("/login.xhtml")) {
                Set<NavigationCase> caseSet = (Set<NavigationCase>) caseListMap.get(fromViewId);
                for (NavigationCase navCase : caseSet) {
                    if (navCase.getFromViewId().equals("/login.xhtml")) {
                        cnt++;
                    }
                }
            }
        }
        assertTrue(cnt == 6);
        return "SUCCESS";
    }

    public String getRedirectParameters() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        UIViewRoot viewRoot = getViewHandler(facesContext).createView(facesContext, "/page1.xhtml");
        viewRoot.setViewId("/page1.xhtml");
        facesContext.setViewRoot(viewRoot);

        ConfigurableNavigationHandler configurableNavigationHandler = (ConfigurableNavigationHandler) application.getNavigationHandler();


        // 1. Case 1

        NavigationCase navigationCase1 = configurableNavigationHandler.getNavigationCase(facesContext, null, "redirectOutcome1");
        Map<String, List<String>> parameters = navigationCase1.getParameters();
        assertNotNull(parameters);
        assertEquals(2, parameters.size());

        List<String> fooParams = parameters.get("foo");
        assertNotNull(fooParams);
        assertEquals(2, fooParams.size());
        assertEquals("bar", fooParams.get(0));
        assertEquals("bar2", fooParams.get(1));

        List<String> foo2Params = parameters.get("foo2");
        assertEquals(1, foo2Params.size());
        assertEquals("bar3", foo2Params.get(0));
        assertTrue(navigationCase1.isIncludeViewParams());


        // 2. Case 2

        NavigationCase navigationCase2 = configurableNavigationHandler.getNavigationCase(facesContext, null, "redirectOutcome2");
        parameters = navigationCase2.getParameters();
        assertNull(parameters);
        assertFalse(navigationCase2.isIncludeViewParams());


        // 3. Ensure implicit navigation outcomes that include query strings are properly parsed.

        NavigationCase navigationCase3 = configurableNavigationHandler.getNavigationCase(
                facesContext, null,
                "test?foo=rab&amp;foo=rab2&foo2=rab3&amp;faces-redirect=true&includeViewParams=true&");
        assertNotNull(navigationCase3);

        parameters = navigationCase3.getParameters();
        assertNotNull(parameters);
        assertTrue(navigationCase3.isRedirect());
        assertTrue(navigationCase3.isIncludeViewParams());
        assertEquals(2, parameters.size());

        fooParams = parameters.get("foo");
        assertNotNull(fooParams);
        assertEquals(2, fooParams.size());
        assertEquals("rab", fooParams.get(0));
        assertEquals("rab2", fooParams.get(1));

        foo2Params = parameters.get("foo2");
        assertEquals(1, foo2Params.size());
        assertEquals("rab3", foo2Params.get(0));


        // 4. Ensure implicit navigation outcomes that include query strings separated with &amp; are properly parsed.

        NavigationCase c4 = configurableNavigationHandler.getNavigationCase(facesContext, null,
                "test?foo=rab&amp;foo=rab2&foo2=rab3&amp;faces-redirect=true&amp;includeViewParams=true&");
        assertNotNull(c4);
        parameters = c4.getParameters();
        assertNotNull(parameters);
        assertTrue(c4.isRedirect());
        assertTrue(c4.isIncludeViewParams());
        assertEquals(2, parameters.size());
        fooParams = parameters.get("foo");
        assertNotNull(fooParams);
        assertEquals(2, fooParams.size());
        assertEquals("rab", fooParams.get(0));
        assertEquals("rab2", fooParams.get(1));
        foo2Params = parameters.get("foo2");
        assertEquals(1, foo2Params.size());
        assertEquals("rab3", foo2Params.get(0));


        // 5. Ensure invalid query string correctly handled

        NavigationCase c5 = configurableNavigationHandler.getNavigationCase(facesContext, null, "test?");

        assertNotNull(c5);
        assertNull(c5.getParameters());
        assertFalse(c5.isRedirect());
        assertFalse(c5.isIncludeViewParams());


        // 6. Ensure redirect parameter el evaluation is performed more than once

        NavigationCase ncase = configurableNavigationHandler.getNavigationCase(facesContext, null, "redirectOutcome3");
        String url = facesContext.getExternalContext().encodeRedirectURL("/path.xhtml", evaluateExpressions(facesContext, ncase.getParameters()));
        System.out.println("URL: " + url);
        assertTrue(url.contains("param=1"));
        url = facesContext.getExternalContext().encodeRedirectURL("/path.xhtml", evaluateExpressions(facesContext, ncase.getParameters()));
        assertTrue(url.contains("param=2"));

        return "SUCCESS";
    }

    private void loadTestResultList() throws Exception {

        DocumentBuilderFactory documentBuilderFactory = DbfFactory.getFactory();
        documentBuilderFactory.setNamespaceAware(false);
        documentBuilderFactory.setValidating(false);
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

        Document navigationCasesDocument =
                builder.parse(((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext())
                       .getResourceAsStream("/WEB-INF/navigation-cases.xml"));

        NodeList navigationRules = navigationCasesDocument.getDocumentElement().getElementsByTagName("test");
        for (int i = 0; i < navigationRules.getLength(); i++) {
            Node test = navigationRules.item(i);
            NamedNodeMap attributes = test.getAttributes();

            Node fromViewId = attributes.getNamedItem("fromViewId");
            Node fromAction = attributes.getNamedItem("fromAction");
            Node fromOutput = attributes.getNamedItem("fromOutcome");
            Node toViewId = attributes.getNamedItem("toViewId");

            createAndAccrueTestResult(((fromViewId != null) ? fromViewId.getTextContent().trim() : null),
                    ((fromAction != null) ? fromAction.getTextContent().trim() : null),
                    ((fromOutput != null) ? fromOutput.getTextContent().trim() : null),
                    ((toViewId != null) ? toViewId.getTextContent().trim() : null));
        }
    }

    private void createAndAccrueTestResult(String fromViewId, String fromAction, String fromOutcome, String toViewId) {
        if (testResults == null) {
            testResults = new ArrayList<>();
        }

        TestResult testResult = new TestResult();
        testResult.fromViewId = fromViewId;
        testResult.fromAction = fromAction;
        testResult.fromOutcome = fromOutcome;
        testResult.toViewId = toViewId;
        testResults.add(testResult);
    }

    class TestResult extends Object {
        public String fromViewId;
        public String fromAction;
        public String fromOutcome;
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

    private static final class WrapperNavigationHandler extends NavigationHandler {

        private NavigationHandler delegate;

        public WrapperNavigationHandler(NavigationHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handleNavigation(FacesContext context, String fromAction, String outcome) {
            delegate.handleNavigation(context, fromAction, outcome);
        }
    }

    private Map<String, List<String>> evaluateExpressions(FacesContext context, Map<String, List<String>> map) {

        if (map != null && !map.isEmpty()) {
            Map<String, List<String>> ret = new HashMap<String, List<String>>(map.size());
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                ret.put(entry.getKey(), evaluateExpressions(context, entry.getValue()));
            }

            return ret;
        }

        return map;

    }

    private List<String> evaluateExpressions(FacesContext context, List<String> values) {
        if (!values.isEmpty()) {
            List<String> ret = new ArrayList<String>(values.size());
            Application app = context.getApplication();
            for (String val : values) {
                if (val != null) {
                    String value = val.trim();
                    if (isExpression(value)) {
                        value = app.evaluateExpressionGet(context, value, String.class);
                    }
                    ret.add(value);
                }
            }

            return ret;
        }
        return values;
    }

    private static boolean isExpression(String expression) {

        if (null == expression) {
            return false;
        }

        // check to see if attribute has an expression
        int start = expression.indexOf("#{");
        return start != -1 && expression.indexOf('}', start + 2) != -1;
    }

    private String title = "Test Navigation Handler";

    public String getTitle() {
        return title;
    }

    private String status = "";

    public String getStatus() {
        return status;
    }

}
