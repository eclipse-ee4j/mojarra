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

package javax.faces.component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import com.sun.faces.mock.MockRenderKit;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Test case for the <strong>javax.faces.UIViewRoot</strong>
 * concrete class.</p>
 */
public class UIViewRootTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UIViewRootTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    /**
     * Return the tests included in this test suite.
     *
     * @return
     */
    public static Test suite() {
        return (new TestSuite(UIViewRootTestCase.class));
    }

    public static String FACTORIES[][] = {
        {FactoryFinder.APPLICATION_FACTORY,
            "com.sun.faces.mock.MockApplicationFactory"
        },
        {FactoryFinder.FACES_CONTEXT_FACTORY,
            "com.sun.faces.mock.MockFacesContextFactory"
        },
        {FactoryFinder.LIFECYCLE_FACTORY,
            "com.sun.faces.mock.MockLifecycleFactory"
        },
        {FactoryFinder.RENDER_KIT_FACTORY,
            "com.sun.faces.mock.MockRenderKitFactory"
        }
    };

    @Override
    public void setUp() throws Exception {
        FactoryFinder.releaseFactories();
        super.setUp();
        for (int i = 0, len = FACTORIES.length; i < len; i++) {
            System.getProperties().remove(FACTORIES[i][0]);
        }

        FactoryFinder.releaseFactories();
        int len, i = 0;

        // simulate the "faces implementation specific" part
        for (i = 0, len = FACTORIES.length; i < len; i++) {
            FactoryFinder.setFactory(FACTORIES[i][0],
                    FACTORIES[i][1]);
        }

        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setViewId("/viewId");
        facesContext.setViewRoot(root);
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                    renderKit);
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tear down instance variables required by this test case.
     *
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
        component = null;
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods
    public void testAddGetComponentResources() {
        application.addComponent("javax.faces.ComponentResourceContainer", Container.class.getName());
        UIViewRoot root = new UIViewRoot();
        UIOutput resource = new UIOutput();

        // no target argument should result in target being head
        root.addComponentResource(facesContext, resource);
        List<UIComponent> components = root.getComponentResources(facesContext, "head");
        assertNotNull(components);
        assertTrue(components.size() == 1);
        assertTrue(components.get(0) == resource);
        UIOutput resource2 = new UIOutput();
        root.addComponentResource(facesContext, resource2);
        assertTrue(components.size() == 2);
        assertTrue(components.get(1) == resource2);
        root.addComponentResource(facesContext, resource2, "form");
        components = root.getComponentResources(facesContext, "form");
        assertTrue(components.size() == 1);
        root.addComponentResource(facesContext, resource2, "body");
        components = root.getComponentResources(facesContext, "body");
        assertTrue(components.size() == 1);

        // the default implementation masks the facet name values
        // of head and form to ensure there are no collisions with valid
        // facets by the name.  Calling UIViewRoot.getFacet("head") or
        // get("form") will return null.
        assertNull(root.getFacet("head"));
        assertNull(root.getFacet("form"));
        assertNull(root.getFacet("body"));
        assertNotNull(root.getFacet("javax_faces_location_HEAD"));
        assertNotNull(root.getFacet("javax_faces_location_FORM"));
        assertNotNull(root.getFacet("javax_faces_location_BODY"));

        // custom locations will also be masked
        root.addComponentResource(facesContext, resource2, "gt");
        assertNotNull(root.getFacet("javax_faces_location_gt"));
        components = root.getComponentResources(facesContext, "gt");
        assertTrue(components.size() == 1);
    }

    // Test AbortProcessingException support
    public void testAbortProcessingException() {
        // Register three listeners, with the second one set to abort
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        root.addFacesListener(new ListenerTestImpl("a", false));
        root.addFacesListener(new ListenerTestImpl("b", true));
        root.addFacesListener(new ListenerTestImpl("c", false));

        // Queue two event and check the results
        ListenerTestImpl.trace(null);
        EventTestImpl event1 = new EventTestImpl(root, "1");
        event1.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        root.queueEvent(event1);
        EventTestImpl event2 = new EventTestImpl(root, "2");
        event2.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        root.queueEvent(event2);
        root.processDecodes(facesContext);
        assertEquals("/a/1/b/1/a/2/b/2", ListenerTestImpl.trace());
    }

    // Test event queuing and dequeuing during broadcasting
    public void testEventBroadcasting() {
        // Register a listener that will conditionally queue a new event
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);

        root.addFacesListener(new ListenerTestImpl("t", "2", "4"));
        ListenerTestImpl.trace(null);

        // Queue some events, including the trigger one
        EventTestImpl event = new EventTestImpl(root, "1");
        event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        root.queueEvent(event);
        event = new EventTestImpl(root, "2");
        event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        root.queueEvent(event);
        event = new EventTestImpl(root, "3");
        event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        root.queueEvent(event);

        // Simulate the Apply Request Values phase
        root.processDecodes(facesContext);

        // Validate the results (expect 4th event to also be queued)
        String expected = "/t/1/t/2/t/3/t/4";
        assertEquals(expected, ListenerTestImpl.trace());
    }

    // Test event queuing and broadcasting
    public void testEventQueuing() {
        // Check for correct ifecycle management processing of event broadcast
        checkEventQueueing(PhaseId.APPLY_REQUEST_VALUES);
        checkEventQueueing(PhaseId.PROCESS_VALIDATIONS);
        checkEventQueueing(PhaseId.UPDATE_MODEL_VALUES);
        checkEventQueueing(PhaseId.INVOKE_APPLICATION);
        checkEventQueueing(PhaseId.ANY_PHASE);
    }

    public void testLocaleFromVB() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        ValueExpression expression = application.getExpressionFactory().createValueExpression(facesContext.getELContext(),
                "#{locale}", Object.class);
        request.setAttribute("locale", Locale.CHINESE);
        assertEquals(Locale.getDefault(), root.getLocale());
        root.setValueExpression("locale", expression);
        assertEquals(Locale.CHINESE, root.getLocale());

        // test locale from String
        request.setAttribute("locale", "en");
        assertEquals(new Locale("en"), root.getLocale());
        request.setAttribute("locale", "en_IE");
        assertEquals(new Locale("en", "IE"), root.getLocale());
        request.setAttribute("locale", "en_IE_EURO");
        assertEquals(new Locale("en", "IE", "EURO"), root.getLocale());

        root.setLocale(Locale.CANADA_FRENCH);
        assertEquals(Locale.CANADA_FRENCH, root.getLocale());
    }

    public void testUninitializedInstance() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        assertEquals(javax.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT,
                root.getRenderKitId());
        assertEquals(Locale.getDefault(), root.getLocale());
    }

    public void testPhaseMethExpression() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        doTestPhaseMethodExpression(root, false);
    }

    public void testPhaseMethExpressionSkipping() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        doTestPhaseMethodExpression(root, true);
    }

    public void testPhaseListener() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        doTestPhaseListener(root, false);
    }

    public void testPhaseListenerSkipping() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        doTestPhaseListener(root, true);
    }

    public void testPhaseMethodExpressionAndListener() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        doTestPhaseMethodExpressionAndListener(root, false);
    }

    public void testPhaseMethodExpressionAndListenerSkipping() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        doTestPhaseMethodExpressionAndListener(root, true);
    }

    public void testPhaseMethExpressionState() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        Object state = root.saveState(facesContext);
        root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        root.restoreState(facesContext, state);

        doTestPhaseMethodExpression(root, false);
    }

    public void testPhaseListenerState() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        Object state = root.saveState(facesContext);
        root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        root.restoreState(facesContext, state);

        doTestPhaseListener(root, false);
    }

    public void testPhaseMethodExpressionAndListenerState() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        Object state = root.saveState(facesContext);
        root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        root.restoreState(facesContext, state);

        doTestPhaseMethodExpressionAndListener(root, false);
    }

    public void testPhaseListenerExceptions() throws Exception {
        PhaseId[] ids = {
            PhaseId.APPLY_REQUEST_VALUES,
            PhaseId.PROCESS_VALIDATIONS,
            PhaseId.UPDATE_MODEL_VALUES,
            PhaseId.INVOKE_APPLICATION,
            PhaseId.RENDER_RESPONSE};
        Class[] args = new Class[]{PhaseEvent.class};
        MethodExpression beforeExpression = facesContext.getApplication()
                .getExpressionFactory()
                .createMethodExpression(facesContext.getELContext(),
                        "#{bean.beforePhase}", null,
                        args);
        MethodExpression afterExpression = facesContext.getApplication()
                .getExpressionFactory()
                .createMethodExpression(facesContext.getELContext(),
                        "#{bean.afterPhase}", null,
                        args);
        for (PhaseId id : ids) {
            UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
            PhaseListenerBean bean = new PhaseListenerBean(id, true, false);
            PhaseListenerBean pl1 = new PhaseListenerBean(id);
            PhaseListenerBean pl2 = new PhaseListenerBean(id);
            facesContext.getExternalContext().getRequestMap().put("bean", bean);
            root.setBeforePhaseListener(beforeExpression);
            root.setAfterPhaseListener(afterExpression);
            root.addPhaseListener(pl1);
            root.addPhaseListener(pl2);

            // validate behavior
            callRightLifecycleMethodGivenPhaseId(root, id);
            assertTrue(bean.isBeforePhaseCalled());
            assertTrue(!bean.isAfterPhaseCalled());
            assertTrue(!pl1.isBeforePhaseCalled());
            assertTrue(!pl1.isAfterPhaseCalled());
            assertTrue(!pl2.isBeforePhaseCalled());
            assertTrue(!pl2.isAfterPhaseCalled());

            // ensure PLs are invoked properly in the case of exceptions
            root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
            bean = new PhaseListenerBean(id);
            pl1 = new PhaseListenerBean(id, true, false);
            pl2 = new PhaseListenerBean(id);
            facesContext.getExternalContext().getRequestMap().put("bean", bean);
            root.setBeforePhaseListener(beforeExpression);
            root.setAfterPhaseListener(afterExpression);
            root.addPhaseListener(pl1);
            root.addPhaseListener(pl2);

            // validate behavior
            callRightLifecycleMethodGivenPhaseId(root, id);
            assertTrue(bean.isBeforePhaseCalled());
            assertTrue(bean.isAfterPhaseCalled());
            assertTrue(pl1.isBeforePhaseCalled());
            assertTrue(!pl1.isAfterPhaseCalled());
            assertTrue(!pl2.isBeforePhaseCalled());
            assertTrue(!pl2.isAfterPhaseCalled());
        }
    }

    public void doTestPhaseMethodExpression(UIViewRoot root,
            boolean skipping) throws Exception {
        PhaseSkipTestComponent comp = null;
        if (skipping) {
            comp = new PhaseSkipTestComponent();
            root.getChildren().add(comp);
            facesContext.responseComplete();
        }
        doTestPhaseMethodExpressionWithPhaseId(root,
                PhaseId.APPLY_REQUEST_VALUES);
        if (skipping) {
            assertTrue(!comp.isDecodeCalled());
        }
        doTestPhaseMethodExpressionWithPhaseId(root, PhaseId.PROCESS_VALIDATIONS);
        if (skipping) {
            assertTrue(!comp.isProcessValidatorsCalled());
        }
        doTestPhaseMethodExpressionWithPhaseId(root, PhaseId.UPDATE_MODEL_VALUES);
        if (skipping) {
            assertTrue(!comp.isProcessUpdatesCalled());
        }
        doTestPhaseMethodExpressionWithPhaseId(root, PhaseId.INVOKE_APPLICATION);
        doTestPhaseMethodExpressionWithPhaseId(root, PhaseId.RENDER_RESPONSE);
        if (skipping) {
            assertTrue(!comp.isEncodeBeginCalled());
        }
    }

    public void doTestPhaseMethodExpressionWithPhaseId(UIViewRoot root,
            PhaseId phaseId) throws Exception {
        PhaseListenerBean phaseListenerBean = new PhaseListenerBean(phaseId);
        facesContext.getExternalContext().getRequestMap().put("bean",
                phaseListenerBean);
        Class[] args = new Class[]{PhaseEvent.class};
        MethodExpression beforeExpression = facesContext.getApplication().getExpressionFactory().createMethodExpression(facesContext.getELContext(),
                "#{bean.beforePhase}", null,
                args),
                afterExpression = facesContext.getApplication().getExpressionFactory().createMethodExpression(facesContext.getELContext(),
                        "#{bean.afterPhase}", null,
                        args);
        root.setBeforePhaseListener(beforeExpression);
        root.setAfterPhaseListener(afterExpression);

        callRightLifecycleMethodGivenPhaseId(root, phaseId);

        assertTrue(phaseListenerBean.isBeforePhaseCalled());
        assertTrue(phaseListenerBean.isAfterPhaseCalled());
    }

    public void doTestPhaseListener(UIViewRoot root,
            boolean skipping) throws Exception {
        PhaseSkipTestComponent comp = null;
        if (skipping) {
            comp = new PhaseSkipTestComponent();
            root.getChildren().add(comp);
            facesContext.responseComplete();
        }
        doTestPhaseListenerWithPhaseId(root,
                PhaseId.APPLY_REQUEST_VALUES);
        if (skipping) {
            assertTrue(!comp.isDecodeCalled());
        }
        doTestPhaseListenerWithPhaseId(root, PhaseId.PROCESS_VALIDATIONS);
        if (skipping) {
            assertTrue(!comp.isProcessValidatorsCalled());
        }
        doTestPhaseListenerWithPhaseId(root, PhaseId.UPDATE_MODEL_VALUES);
        if (skipping) {
            assertTrue(!comp.isProcessUpdatesCalled());
        }
        doTestPhaseListenerWithPhaseId(root, PhaseId.INVOKE_APPLICATION);
        doTestPhaseListenerWithPhaseId(root, PhaseId.RENDER_RESPONSE);
        if (skipping) {
            assertTrue(!comp.isEncodeBeginCalled());
        }
    }

    public void doTestPhaseListenerWithPhaseId(UIViewRoot root,
            PhaseId phaseId) throws Exception {
        PhaseListenerBean phaseListener = new PhaseListenerBean(phaseId);
        root.addPhaseListener(phaseListener);

        callRightLifecycleMethodGivenPhaseId(root, phaseId);

        assertTrue(phaseListener.isBeforePhaseCalled());
        assertTrue(phaseListener.isAfterPhaseCalled());
    }

    public void doTestPhaseMethodExpressionAndListener(UIViewRoot root,
            boolean skipping) throws Exception {
        PhaseSkipTestComponent comp = null;
        if (skipping) {
            comp = new PhaseSkipTestComponent();
            root.getChildren().add(comp);
            facesContext.responseComplete();
        }
        doTestPhaseMethodExpressionAndListenerWithPhaseId(root,
                PhaseId.APPLY_REQUEST_VALUES);
        if (skipping) {
            assertTrue(!comp.isDecodeCalled());
        }
        doTestPhaseMethodExpressionAndListenerWithPhaseId(root,
                PhaseId.PROCESS_VALIDATIONS);
        if (skipping) {
            assertTrue(!comp.isProcessValidatorsCalled());
        }
        doTestPhaseMethodExpressionAndListenerWithPhaseId(root,
                PhaseId.UPDATE_MODEL_VALUES);
        if (skipping) {
            assertTrue(!comp.isProcessUpdatesCalled());
        }
        doTestPhaseMethodExpressionAndListenerWithPhaseId(root,
                PhaseId.INVOKE_APPLICATION);
        doTestPhaseMethodExpressionAndListenerWithPhaseId(root,
                PhaseId.RENDER_RESPONSE);
        if (skipping) {
            assertTrue(!comp.isEncodeBeginCalled());
        }
    }

    public void doTestPhaseMethodExpressionAndListenerWithPhaseId(UIViewRoot root,
            PhaseId phaseId) throws Exception {
        PhaseListenerBean phaseListener = new PhaseListenerBean(phaseId);
        PhaseListenerBean phaseListenerBean = new PhaseListenerBean(phaseId);
        facesContext.getExternalContext().getRequestMap().put("bean",
                phaseListenerBean);
        Class[] args = new Class[]{PhaseEvent.class};
        MethodExpression beforeExpression = facesContext.getApplication().getExpressionFactory().createMethodExpression(facesContext.getELContext(),
                "#{bean.beforePhase}", null,
                args),
                afterExpression = facesContext.getApplication().getExpressionFactory().createMethodExpression(facesContext.getELContext(),
                        "#{bean.afterPhase}", null,
                        args);
        root.setBeforePhaseListener(beforeExpression);
        root.setAfterPhaseListener(afterExpression);
        root.addPhaseListener(phaseListener);

        callRightLifecycleMethodGivenPhaseId(root, phaseId);

        assertTrue(phaseListenerBean.isBeforePhaseCalled());
        assertTrue(phaseListenerBean.isAfterPhaseCalled());
        assertTrue(phaseListener.isBeforePhaseCalled());
        assertTrue(phaseListener.isAfterPhaseCalled());
    }

    private void checkEventQueuesSizes(List<List> events,
            int applyEventsSize, int valEventsSize, int updateEventsSize, int appEventsSize) {
        List applyEvents = events.get(PhaseId.APPLY_REQUEST_VALUES.getOrdinal());
        assertEquals("Apply-Request-Values Event Count", applyEventsSize, applyEvents.size());
        List valEvents = events.get(PhaseId.PROCESS_VALIDATIONS.getOrdinal());
        assertEquals("Process-Validations Event Count", valEventsSize, valEvents.size());
        List updateEvents = events.get(PhaseId.UPDATE_MODEL_VALUES.getOrdinal());
        assertEquals("Update-Model Event Count", updateEventsSize, updateEvents.size());
        List appEvents = events.get(PhaseId.INVOKE_APPLICATION.getOrdinal());
        assertEquals("Invoke-Application Event Count", appEventsSize, appEvents.size());
    }

    // Test Events List Clearing
    public void testEventsListClear() {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        EventTestImpl event1, event2, event3, event4 = null;
        event1 = new EventTestImpl(root, "1");
        event1.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        root.queueEvent(event1);
        event2 = new EventTestImpl(root, "2");
        event2.setPhaseId(PhaseId.PROCESS_VALIDATIONS);
        root.queueEvent(event2);
        event3 = new EventTestImpl(root, "3");
        event3.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        root.queueEvent(event3);
        event4 = new EventTestImpl(root, "4");
        event4.setPhaseId(PhaseId.INVOKE_APPLICATION);
        root.queueEvent(event4);
        final Field fields[] = UIViewRoot.class.getDeclaredFields();
        Field field = null;
        List<List> events = null;
        for (int i = 0; i < fields.length; ++i) {
            if ("events".equals(fields[i].getName())) {
                field = fields[i];
                field.setAccessible(true);
                try {
                    events = TypedCollections.dynamicallyCastList((List) field.get(root), List.class);
                } catch (Exception e) {
                    assertTrue(false);
                }
                break;
            }
        }
        // CASE: renderReponse not set; responseComplete not set;
        // check for existence of events before processDecodes
        checkEventQueuesSizes(events, 1, 1, 1, 1);
        root.processDecodes(facesContext);
        // there should be no events
        checkEventQueuesSizes(events, 0, 1, 1, 1);

        // requeue apply request event
        root.queueEvent(event1);
        // CASE: renderReponse set;
        // check for existence of events before processValidators
        checkEventQueuesSizes(events, 1, 1, 1, 1);
        facesContext.renderResponse();
        root.processValidators(facesContext);
        // there should be no events
        checkEventQueuesSizes(events, 0, 0, 0, 0);

        // reset FacesContext
        facesContext.setRenderResponse(false);
        facesContext.setResponseComplete(false);
        // requeue all events
        root.queueEvent(event1);
        root.queueEvent(event2);
        root.queueEvent(event3);
        root.queueEvent(event4);
        try {
            events = TypedCollections.dynamicallyCastList((List) field.get(root), List.class);
        } catch (Exception e) {
            assertTrue(false);
        }
        // CASE: response set;
        // check for existence of events before processValidators
        checkEventQueuesSizes(events, 1, 1, 1, 1);
        facesContext.renderResponse();
        root.processValidators(facesContext);
        // there should be no events
        checkEventQueuesSizes(events, 0, 0, 0, 0);

        // reset FacesContext
        facesContext.setRenderResponse(false);
        facesContext.setResponseComplete(false);
        // requeue all events
        root.queueEvent(event1);
        root.queueEvent(event2);
        root.queueEvent(event3);
        root.queueEvent(event4);
        try {
            events = TypedCollections.dynamicallyCastList((List) field.get(root), List.class);
        } catch (Exception e) {
            assertTrue(false);
        }
        // CASE: response complete;
        // check for existence of events before processUpdates
        checkEventQueuesSizes(events, 1, 1, 1, 1);
        facesContext.responseComplete();
        root.processUpdates(facesContext);
        // there should be no events
        checkEventQueuesSizes(events, 0, 0, 0, 0);

        // reset FacesContext
        facesContext.setRenderResponse(false);
        facesContext.setResponseComplete(false);
        // requeue all events
        root.queueEvent(event1);
        root.queueEvent(event2);
        root.queueEvent(event3);
        root.queueEvent(event4);
        try {
            events = TypedCollections.dynamicallyCastList((List) field.get(root), List.class);
        } catch (Exception e) {
            assertTrue(false);
        }
        // CASE: response complete;
        // check for existence of events before processApplication
        checkEventQueuesSizes(events, 1, 1, 1, 1);
        facesContext.responseComplete();
        root.processApplication(facesContext);
        // there should be no events
        checkEventQueuesSizes(events, 0, 0, 0, 0);

        //finally, get the internal events list one more time
        //to make sure it is null
        try {
            events = TypedCollections.dynamicallyCastList((List) field.get(root), List.class);
        } catch (Exception e) {
            assertTrue(false);
        }
        assertNull("events", events);
    }

    private void callRightLifecycleMethodGivenPhaseId(UIViewRoot root,
            PhaseId phaseId) throws Exception {
        if (phaseId.getOrdinal() == PhaseId.APPLY_REQUEST_VALUES.getOrdinal()) {
            root.processDecodes(facesContext);
        } else if (phaseId.getOrdinal() == PhaseId.PROCESS_VALIDATIONS.getOrdinal()) {
            root.processValidators(facesContext);
        } else if (phaseId.getOrdinal() == PhaseId.UPDATE_MODEL_VALUES.getOrdinal()) {
            root.processUpdates(facesContext);
        } else if (phaseId.getOrdinal() == PhaseId.INVOKE_APPLICATION.getOrdinal()) {
            root.processApplication(facesContext);
        } else if (phaseId.getOrdinal() == PhaseId.RENDER_RESPONSE.getOrdinal()) {
            root.encodeBegin(facesContext);
            root.encodeEnd(facesContext);
        }
    }

    // --------------------------------------------------------- Support Methods
    private void checkEventQueueing(PhaseId phaseId) {

        // NOTE:  Current semantics for ANY_PHASE listeners is that
        // the event should be delivered exactly once, so the existence
        // of such a listener does not cause the event to remain queued.
        // Therefore, the expected string is the same as for any
        // phase-specific listener, and it should get matched after
        // Apply Request Values processing since that is first phase
        // for which events are fired
        // Register an event listener for the specified phase id
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        facesContext.setViewRoot(root);
        EventTestImpl event = null;
        ListenerTestImpl listener = new ListenerTestImpl("t");
        root.addFacesListener(listener);

        // Queue some events to be processed
        event = new EventTestImpl(root, "1");
        event.setPhaseId(phaseId);
        root.queueEvent(event);
        event = new EventTestImpl(root, "2");
        event.setPhaseId(phaseId);
        root.queueEvent(event);
        String expected = "/t/1/t/2";

        // Fire off the relevant lifecycle methods and check expected results
        ListenerTestImpl.trace(null);
        assertEquals("", ListenerTestImpl.trace());
        root.processDecodes(facesContext);
        if (PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)
                || PhaseId.ANY_PHASE.equals(phaseId)) {
            assertEquals(expected, ListenerTestImpl.trace());
        } else {
            assertEquals("", ListenerTestImpl.trace());
        }
        root.processValidators(facesContext);
        if (PhaseId.PROCESS_VALIDATIONS.equals(phaseId)
                || PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)
                || PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)
                || PhaseId.ANY_PHASE.equals(phaseId)) {
            assertEquals(expected, ListenerTestImpl.trace());
        } else {
            assertEquals("", ListenerTestImpl.trace());
        }
        root.processUpdates(facesContext);
        if (PhaseId.UPDATE_MODEL_VALUES.equals(phaseId)
                || PhaseId.PROCESS_VALIDATIONS.equals(phaseId)
                || PhaseId.APPLY_REQUEST_VALUES.equals(phaseId)
                || PhaseId.ANY_PHASE.equals(phaseId)) {
            assertEquals(expected, ListenerTestImpl.trace());
        } else {
            assertEquals("", ListenerTestImpl.trace());
        }
        root.processApplication(facesContext);
        assertEquals(expected, ListenerTestImpl.trace());
    }

    // These overrides are necessary because our normal setup
    // calls releaseFactories, which makes it impossible to get clientIds.
    @Override
    public void testInvokeOnComponentPositive() throws Exception {
        super.setUp();
        super.testInvokeOnComponentPositive();
    }

    @Override
    public void testInvokeOnComponentNegative() throws Exception {
        super.setUp();
        super.testInvokeOnComponentNegative();
    }

    @Override
    public void testInvokeOnComponentWithPrependId() throws Exception {
        super.setUp();
        super.testInvokeOnComponentWithPrependId();
    }

    @Override
    public void testChildrenListAfterAddViewPublish() {
        // overridding to do nothing.  UIViewRoot is a special cases
        // and there should always only be on UIViewRoot in a tree
    }

    @Override
    public void testFacetMapAfterAddViewPublish() {
        // overridding to do nothing.  UIViewRoot is a special cases
        // and there should always only be on UIViewRoot in a tree
    }

    // Check that the properties on the specified components are equal
    @Override
    protected void checkProperties(UIComponent comp1, UIComponent comp2) {
        super.checkProperties(comp1, comp2);
        UIViewRoot vr1 = (UIViewRoot) comp1;
        UIViewRoot vr2 = (UIViewRoot) comp2;
        assertEquals(vr2.getRenderKitId(), vr2.getRenderKitId());
        assertEquals(vr1.getViewId(), vr2.getViewId());
        assertEquals(vr1.getLocale(), vr2.getLocale());
    }

    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIViewRoot();
        component.setRendererType(null);
        return (component);
    }


    public static class PhaseListenerBean extends Object
            implements PhaseListener {

        private boolean beforePhaseCalled = false;
        private boolean afterPhaseCalled = false;
        private PhaseId phaseId = null;
        private boolean exceptionBefore;
        private boolean exceptionAfter;

        public PhaseListenerBean(PhaseId phaseId) {
            this.phaseId = phaseId;
        }

        public PhaseListenerBean(PhaseId phaseId,
                boolean exceptionBefore,
                boolean exceptionAfter) {
            this(phaseId);
            this.exceptionBefore = exceptionBefore;
            this.exceptionAfter = exceptionAfter;
        }

        public boolean isBeforePhaseCalled() {
            return beforePhaseCalled;
        }

        public boolean isAfterPhaseCalled() {
            return afterPhaseCalled;
        }

        @Override
        public void beforePhase(PhaseEvent e) {
            beforePhaseCalled = true;
            if (exceptionBefore) {
                throw new RuntimeException();
            }
        }

        @Override
        public void afterPhase(PhaseEvent e) {
            afterPhaseCalled = true;
            if (exceptionAfter) {
                throw new RuntimeException();
            }
        }

        @Override
        public PhaseId getPhaseId() {
            return phaseId;
        }
    }

    public static class PhaseSkipTestComponent extends UIInput {

        private boolean decodeCalled = false;

        @Override
        public void decode(FacesContext context) {
            decodeCalled = true;
        }

        public boolean isDecodeCalled() {
            return decodeCalled;
        }

        private boolean encodeBeginCalled = false;

        @Override
        public void encodeBegin(FacesContext context) throws IOException {
            encodeBeginCalled = true;
        }

        public boolean isEncodeBeginCalled() {
            return encodeBeginCalled;
        }

        private boolean processValidatorsCalled = false;

        @Override
        public void processValidators(FacesContext context) {
            processValidatorsCalled = true;
        }

        public boolean isProcessValidatorsCalled() {
            return processValidatorsCalled;
        }

        private boolean processUpdatesCalled = false;

        @Override
        public void processUpdates(FacesContext context) {
            processUpdatesCalled = true;
        }

        public boolean isProcessUpdatesCalled() {
            return processUpdatesCalled;
        }
    }

    public static class Container extends UIPanel {

        @Override
        public void encodeAll(FacesContext context) throws IOException {
        }
    }
}
