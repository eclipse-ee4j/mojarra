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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreRenderComponentEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * <p>
 * Base unit tests for all {@link UIComponentBase} subclasses.
 * </p>
 */
public class UIComponentBaseTestCase extends UIComponentTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {

        // Set up the component under test
        super.setUp();
        component = new ComponentTestImpl(expectedId);

    }

    // Tear down instance variables required by ths test case
    @Override
    @AfterEach
    public void tearDown() throws Exception {
        externalContext.setRequestParameterMap(null);

        super.tearDown();

    }

    // ------------------------------------------------- Individual Test Methods
    // Test lifecycle management methods
    @Test
    public void testLifecycleManagement() {

        checkLifecycleParentRendered();
        checkLifecycleParentUnrendered();
        checkLifecycleSelfRendered();
        checkLifecycleSelfUnrendered();

    }

    @Test
    public void testComponentToFromEL2() throws Exception {

        final FacesContext ctx = facesContext;
        ComponentTestImpl c = new ComponentTestImpl();
        ComponentTestImpl c2 = new ComponentTestImpl();
        UIComponent eeo = new UIComponentOverrideEncodeEnd();
        ComponentTestImpl c3 = new ComponentTestImpl();
        UIComponent ebo = new UIComponentOverrideEncodeBegin();

        c.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c);
        c2.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c2);
        c2.encodeEnd(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c);
        c.encodeEnd(ctx);
        assertNull(UIComponent.getCurrentComponent(ctx));

        // sanity check for the case where a component overrides
        // encodeBegin() without calling super or pushComponentToEL
        c.encodeBegin(ctx);
        c2.encodeBegin(ctx);
        ebo.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c2);
        ebo.encodeEnd(ctx); // if the component wasn't pushed
        // it shouldn't be popped.
        assertEquals(UIComponent.getCurrentComponent(ctx), c2);
        c2.encodeEnd(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c);
        c.encodeEnd(ctx);
        assertNull(UIComponent.getCurrentComponent(ctx));

        // sanity check for the case where a component overrides
        // encodeEnd() without calling super or popComponentFromEL
        c.encodeBegin(ctx);
        c2.encodeBegin(ctx);
        eeo.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo);
        eeo.encodeEnd(ctx);
        // this is ugly. Because of a component not doing calling
        // super() or popComponentFromEL, c2 won't be visible
        // as the current component.
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo);
        c2.encodeEnd(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c);
        c.encodeEnd(ctx);
        assertNull(UIComponent.getCurrentComponent(ctx));

        UIComponent eeo2 = new UIComponentOverrideEncodeEnd();
        c.encodeBegin(ctx);
        c2.encodeBegin(ctx);
        eeo.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo);
        c3.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c3);
        eeo2.encodeBegin(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo2);
        eeo2.encodeEnd(ctx);
        // this is ugly.
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo2);
        c3.encodeEnd(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo);
        eeo.encodeEnd(ctx);
        // this is ugly.
        assertEquals(UIComponent.getCurrentComponent(ctx), eeo);
        c2.encodeEnd(ctx);
        assertEquals(UIComponent.getCurrentComponent(ctx), c);
        c.encodeEnd(ctx);
        assertNull(UIComponent.getCurrentComponent(ctx));

    }

    @Test
    public void testEncodeChildren() throws Exception {
        ComponentTestImpl.trace(null);
        UIComponent comp1 = new ComponentTestImpl("one");
        UIComponent comp2 = new ComponentTestImpl("two");
        UIComponent comp3 = new ComponentTestImpl("three");
        UIComponent comp4 = new ComponentTestImpl("four");

        comp1.getChildren().add(comp2);
        comp1.getChildren().add(comp3);
        comp1.getChildren().add(comp4);

        comp1.encodeChildren(facesContext);
        System.out.println("Actual:   " + ComponentTestImpl.trace());
        System.out.println("Expected: " + "/eC-one/eB-two/eE-two/eB-three/eE-three/eB-four/eE-four");
        assertEquals("/eC-one/eB-two/eE-two/eB-three/eE-three/eB-four/eE-four", ComponentTestImpl.trace());

    }

    // Test recursive adding and removing child trees with ids
    @Test
    public void testChildrenRecursive() {

        // Create the components we will need
        UIComponent testComponent = new ComponentTestImpl();
        UIComponent child1 = new ComponentTestImpl("child1");
        UIComponent child2 = new ComponentTestImpl("child2");
        UIComponent child3 = new ComponentTestImpl("child3");

        // Prepare ancestry tree before adding to base component
        child1.getChildren().add(child2);
        child2.getChildren().add(child3);

        // Verify that no child ids are visible yet
        assertNull(testComponent.findComponent("child1"));
        assertNull(testComponent.findComponent("child2"));
        assertNull(testComponent.findComponent("child3"));

        // Add the entire tree
        testComponent.getChildren().add(child1);

        // Verify that all named children get added
        assertEquals(child1, testComponent.findComponent("child1"));
        assertEquals(child2, testComponent.findComponent("child2"));
        assertEquals(child3, testComponent.findComponent("child3"));

        // Remove the entire tree
        testComponent.getChildren().remove(child1);

        // Verify that child ids are no longer visible
        assertNull(testComponent.findComponent("child1"));
        assertNull(testComponent.findComponent("child2"));
        assertNull(testComponent.findComponent("child3"));

    }

    @Test
    public void testChildrenAndFacetsWithNullGetParent() throws Exception {
        ComponentTestImpl child = new ComponentTestImpl() {
            @Override
            public UIComponent getParent() {
                return null;
            }
        };
        component.getChildren().add(child);
        assertNull(component.getChildren().get(0).getParent());
        ComponentTestImpl facet = new ComponentTestImpl() {
            @Override
            public UIComponent getParent() {
                return null;
            }
        };
        component.getFacets().put("nullParent", facet);
        assertNull(component.getFacets().get("nullParent").getParent());
    }

    // Test reconnecting a child or facet to a different component
    @Test
    public void testComponentReconnect() {

        UIComponent parent1 = new ComponentTestImpl();
        UIComponent parent2 = new ComponentTestImpl();

        // Reconnect an existing child as a child
        checkChildCount(parent1, 0);
        checkChildCount(parent2, 0);
        parent1.getChildren().add(component);
        checkChildCount(parent1, 1);
        checkChildCount(parent2, 0);
        checkChildPresent(parent1, component, 0);
        parent2.getChildren().add(component);
        checkChildCount(parent1, 0);
        checkChildCount(parent2, 1);
        checkChildPresent(parent2, component, 0);
        parent2.getChildren().clear();
        checkChildCount(parent1, 0);
        checkChildCount(parent2, 0);

        // Reconnect an existing child as a facet
        checkChildCount(parent1, 0);
        checkFacetCount(parent2, 0);
        parent1.getChildren().add(component);
        checkChildCount(parent1, 1);
        checkFacetCount(parent2, 0);
        checkChildPresent(parent1, component, 0);
        parent2.getFacets().put("facet", component);
        checkChildCount(parent1, 0);
        checkFacetCount(parent2, 1);
        checkFacetPresent(parent2, "facet", component);
        parent2.getFacets().clear();
        checkChildCount(parent1, 0);
        checkFacetCount(parent2, 0);

        // Reconnect an existing facet as a child
        checkFacetCount(parent1, 0);
        checkChildCount(parent2, 0);
        parent1.getFacets().put("facet", component);
        checkFacetCount(parent1, 1);
        checkChildCount(parent2, 0);
        checkFacetPresent(parent1, "facet", component);
        parent2.getChildren().add(component);
        checkFacetCount(parent1, 0);
        checkChildCount(parent2, 1);
        checkChildPresent(parent2, component, 0);
        parent2.getChildren().clear();
        checkFacetCount(parent1, 0);
        checkChildCount(parent2, 0);

    }

    // Test removing components from our naming container.
    @Test
    public void testComponentRemoval() {

        UIComponent testComponent = new ComponentTestImpl();
        UIComponent child1 = new ComponentTestImpl("child1");
        UIComponent child2 = new ComponentTestImpl("child2");
        UIComponent child3 = new ComponentTestImpl("child3");
        UIComponent child = null;

        // adding children to naming container
        testComponent.getChildren().add(child1);
        testComponent.getChildren().add(child2);
        testComponent.getChildren().add(child3);

        // make sure children are stored in naming container properly
        Iterator<UIComponent> kidItr = null;

        kidItr = testComponent.getFacetsAndChildren();

        child = kidItr.next();
        assertTrue(child.equals(child1));

        child = kidItr.next();
        assertTrue(child.equals(child2));

        child = kidItr.next();
        assertTrue(child.equals(child3));

        // make sure child is removed from component and naming container
        // pass in a component to remove method
        testComponent.getChildren().remove(child1);

        kidItr = testComponent.getFacetsAndChildren();

        child = kidItr.next();
        assertTrue(child.equals(child2));

        child = kidItr.next();
        assertTrue(child.equals(child3));

        // make sure child is removed from component and naming container
        // pass an index to remove method
        testComponent.getChildren().remove(0);

        kidItr = testComponent.getFacetsAndChildren();

        child = kidItr.next();
        assertTrue(child.equals(child3));

        // make sure child is removed from component and naming container
        // remove all children
        testComponent.getChildren().clear();

        kidItr = testComponent.getFacetsAndChildren();
        assertTrue(!kidItr.hasNext());
    }

    @Test
    public void testStateHolder2() throws Exception {

        UIComponent c = new UIComponentListener();
        c.subscribeToEvent(PostAddToViewEvent.class, c);
        Object state = c.saveState(facesContext);
        c = new UIComponentListener();
        c.pushComponentToEL(facesContext, c);
        c.restoreState(facesContext, state);
        c.popComponentFromEL(facesContext);
        assertTrue(c.getListenersForEventClass(PostAddToViewEvent.class).size() == 1);

    }

    @Test
    public void testAttributesThatAreSetStateHolder() throws Exception {
        ComponentTestImpl c = new ComponentTestImpl();
        c.getAttributes().put("attr1", "value1");
        c.markInitialState();
        c.getAttributes().put("attr2", "value2");
        assertEquals(Arrays.asList("attr1", "attr2"), c.getAttributes().get("jakarta.faces.component.UIComponentBase.attributesThatAreSet"));

        Object state = c.saveState(facesContext);
        c = new ComponentTestImpl();
        c.pushComponentToEL(facesContext, c);
        c.restoreState(facesContext, state);
        c.popComponentFromEL(facesContext);
        assertEquals(Arrays.asList("attr1", "attr2"), c.getAttributes().get("jakarta.faces.component.UIComponentBase.attributesThatAreSet"));
    }

    @Test
    public void testValueExpressions() throws Exception {

        UIComponentBase test = (UIComponentBase) component;

        // generic attributes
        request.setAttribute("foo", "bar");
        test.getAttributes().clear();
        assertNull(test.getAttributes().get("baz"));
        test.setValueExpression("baz", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{foo}", String.class));
        assertEquals("bar", test.getAttributes().get("baz"));
        test.getAttributes().put("baz", "bop");
        assertEquals("bop", test.getAttributes().get("baz"));
        test.getAttributes().remove("baz");
        assertEquals("bar", test.getAttributes().get("baz"));
        test.setValueExpression("baz", null);
        assertNull(test.getAttributes().get("baz"));

        // "id" property
        try {
            test.setValueExpression("id", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{foo}", String.class));
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected response
        }

        // "parent" property
        try {
            test.setValueExpression("parent",
                    application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{foo}", UIComponent.class));
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected response
        }

        // "rendered" property
        request.setAttribute("foo", Boolean.FALSE);
        test.setValueExpression("rendered", null);
        boolean initial = test.isRendered();
        if (initial) {
            request.setAttribute("foo", Boolean.FALSE);
        } else {
            request.setAttribute("foo", Boolean.TRUE);
        }
        test.setValueExpression("rendered", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{foo}", Boolean.class));
        assertEquals(!initial, test.isRendered());
        test.setRendered(initial);
        assertEquals(initial, test.isRendered());
        assertNotNull(test.getValueExpression("rendered"));

        // "rendererType" property
        request.setAttribute("foo", "bar");
        test.setRendererType(null);
        assertNull(test.getRendererType());
        test.setValueExpression("rendererType", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{foo}", String.class));
        assertNotNull(test.getValueExpression("rendererType"));
        assertEquals("bar", test.getRendererType());
        test.setRendererType("baz");
        assertEquals("baz", test.getRendererType());
        test.setRendererType(null);
        assertEquals("bar", test.getRendererType());
        test.setValueExpression("rendererType", null);
        assertNull(test.getValueExpression("rendererType"));
        assertNull(test.getRendererType());

    }

    // --------------------------------------------------------- support Methods
    // Check that the attributes on the specified components are equal
    protected void checkAttributes(UIComponent comp1, UIComponent comp2) {
        assertEquals(comp1.getAttributes(), comp2.getAttributes());
    }

    // Check that the specified components are equal
    protected void checkComponents(UIComponent comp1, UIComponent comp2) {
        checkAttributes(comp1, comp2);
        // checkFacets(comp1, comp2); // Not saved and restored by component
        checkProperties(comp1, comp2);
    }

    // Check lifecycle processing when parent "rendered" property is "true"
    private void checkLifecycleParentRendered() {

        // Put our component under test in a tree under a UIViewRoot
        component.getAttributes().clear();
        component.getChildren().clear();
        component.getFacets().clear();
        component.setRendered(true);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        UIPanel panel = new UIPanel();
        panel.setRendered(true);
        root.getChildren().add(panel);
        panel.getChildren().add(component);

        // Establish a view with multiple facets and children
        UIComponent facet1 = new ComponentTestImpl("f1");
        UIComponent facet2 = new ComponentTestImpl("f2");
        UIComponent facet3 = new ComponentTestImpl("f3");
        component.getFacets().put("f1", facet1);
        component.getFacets().put("f2", facet2);
        component.getFacets().put("f3", facet3);
        checkFacetCount(component, 3);
        UIComponent child1 = new ComponentTestImpl("c1");
        UIComponent child2 = new ComponentTestImpl("c2");
        UIComponent child3 = new ComponentTestImpl("c3");
        component.getChildren().add(child1);
        component.getChildren().add(child2);
        component.getChildren().add(child3);
        checkChildCount(component, 3);
        UIComponent child2a = new ComponentTestImpl("c2a");
        UIComponent child2b = new ComponentTestImpl("c2b");
        child2.getChildren().add(child2a);
        child2.getChildren().add(child2b);
        checkChildCount(child2, 2);

        // Enqueue a single FacesEvent for each component
        component.queueEvent(new EventTestImpl(component));
        component.queueEvent(new EventTestImpl(facet1));
        component.queueEvent(new EventTestImpl(facet2));
        component.queueEvent(new EventTestImpl(facet3));
        component.queueEvent(new EventTestImpl(child1));
        component.queueEvent(new EventTestImpl(child2));
        component.queueEvent(new EventTestImpl(child3));
        component.queueEvent(new EventTestImpl(child2a));
        component.queueEvent(new EventTestImpl(child2b));

        // Test processDecodes()
        ComponentTestImpl.trace(null);
        component.processDecodes(facesContext);
        assertEquals(lifecycleTrace("pD", "d"), ComponentTestImpl.trace());

        // Test processValidators()
        ComponentTestImpl.trace(null);
        component.processValidators(facesContext);
        assertEquals(lifecycleTrace("pV", null), ComponentTestImpl.trace());

        // Test processUpdates()
        ComponentTestImpl.trace(null);
        component.processUpdates(facesContext);
        assertEquals(lifecycleTrace("pU", null), ComponentTestImpl.trace());

    }

    // Check lifecycle processing when parent "rendered" property is "false"
    private void checkLifecycleParentUnrendered() {

        // Put our component under test in a tree under a UIViewRoot
        component.getAttributes().clear();
        component.getChildren().clear();
        component.getFacets().clear();
        component.setRendered(true);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        UIPanel panel = new UIPanel();
        panel.setRendered(false);
        root.getChildren().add(panel);
        panel.getChildren().add(component);

        // Establish a view with multiple facets and children
        UIComponent facet1 = new ComponentTestImpl("f1");
        UIComponent facet2 = new ComponentTestImpl("f2");
        UIComponent facet3 = new ComponentTestImpl("f3");
        component.getFacets().put("f1", facet1);
        component.getFacets().put("f2", facet2);
        component.getFacets().put("f3", facet3);
        checkFacetCount(component, 3);
        UIComponent child1 = new ComponentTestImpl("c1");
        UIComponent child2 = new ComponentTestImpl("c2");
        UIComponent child3 = new ComponentTestImpl("c3");
        component.getChildren().add(child1);
        component.getChildren().add(child2);
        component.getChildren().add(child3);
        checkChildCount(component, 3);
        UIComponent child2a = new ComponentTestImpl("c2a");
        UIComponent child2b = new ComponentTestImpl("c2b");
        child2.getChildren().add(child2a);
        child2.getChildren().add(child2b);
        checkChildCount(child2, 2);

        // Enqueue a single FacesEvent for each component
        component.queueEvent(new EventTestImpl(component));
        component.queueEvent(new EventTestImpl(facet1));
        component.queueEvent(new EventTestImpl(facet2));
        component.queueEvent(new EventTestImpl(facet3));
        component.queueEvent(new EventTestImpl(child1));
        component.queueEvent(new EventTestImpl(child2));
        component.queueEvent(new EventTestImpl(child3));
        component.queueEvent(new EventTestImpl(child2a));
        component.queueEvent(new EventTestImpl(child2b));

        // Test processDecodes()
        ComponentTestImpl.trace(null);
        component.processDecodes(facesContext);
        assertEquals(lifecycleTrace("pD", "d"), ComponentTestImpl.trace());

        // Test processValidators()
        ComponentTestImpl.trace(null);
        component.processValidators(facesContext);
        assertEquals(lifecycleTrace("pV", null), ComponentTestImpl.trace());

        // Test processUpdates()
        ComponentTestImpl.trace(null);
        component.processUpdates(facesContext);
        assertEquals(lifecycleTrace("pU", null), ComponentTestImpl.trace());

    }

    // Check lifecycle processing when our "rendered" property is "true"
    private void checkLifecycleSelfRendered() {

        // Put our component under test in a tree under a UIViewRoot
        component.getAttributes().clear();
        component.getChildren().clear();
        component.getFacets().clear();
        component.setRendered(true);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Establish a view with multiple facets and children
        UIComponent facet1 = new ComponentTestImpl("f1");
        UIComponent facet2 = new ComponentTestImpl("f2");
        UIComponent facet3 = new ComponentTestImpl("f3");
        component.getFacets().put("f1", facet1);
        component.getFacets().put("f2", facet2);
        component.getFacets().put("f3", facet3);
        checkFacetCount(component, 3);
        UIComponent child1 = new ComponentTestImpl("c1");
        UIComponent child2 = new ComponentTestImpl("c2");
        UIComponent child3 = new ComponentTestImpl("c3");
        component.getChildren().add(child1);
        component.getChildren().add(child2);
        component.getChildren().add(child3);
        checkChildCount(component, 3);
        UIComponent child2a = new ComponentTestImpl("c2a");
        UIComponent child2b = new ComponentTestImpl("c2b");
        child2.getChildren().add(child2a);
        child2.getChildren().add(child2b);
        checkChildCount(child2, 2);

        // Enqueue a single FacesEvent for each component
        component.queueEvent(new EventTestImpl(component));
        component.queueEvent(new EventTestImpl(facet1));
        component.queueEvent(new EventTestImpl(facet2));
        component.queueEvent(new EventTestImpl(facet3));
        component.queueEvent(new EventTestImpl(child1));
        component.queueEvent(new EventTestImpl(child2));
        component.queueEvent(new EventTestImpl(child3));
        component.queueEvent(new EventTestImpl(child2a));
        component.queueEvent(new EventTestImpl(child2b));

        // Test processDecodes()
        ComponentTestImpl.trace(null);
        component.processDecodes(facesContext);
        assertEquals(lifecycleTrace("pD", "d"), ComponentTestImpl.trace());

        // Test processValidators()
        ComponentTestImpl.trace(null);
        component.processValidators(facesContext);
        assertEquals(lifecycleTrace("pV", null), ComponentTestImpl.trace());

        // Test processUpdates()
        ComponentTestImpl.trace(null);
        component.processUpdates(facesContext);
        assertEquals(lifecycleTrace("pU", null), ComponentTestImpl.trace());

    }

    // Check lifecycle processing when our "rendered" property is "false"
    private void checkLifecycleSelfUnrendered() {

        // Put our component under test in a tree under a UIViewRoot
        component.getAttributes().clear();
        component.getChildren().clear();
        component.getFacets().clear();
        component.setRendered(false);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Establish a view with multiple facets and children
        UIComponent facet1 = new ComponentTestImpl("f1");
        UIComponent facet2 = new ComponentTestImpl("f2");
        UIComponent facet3 = new ComponentTestImpl("f3");
        component.getFacets().put("f1", facet1);
        component.getFacets().put("f2", facet2);
        component.getFacets().put("f3", facet3);
        checkFacetCount(component, 3);
        UIComponent child1 = new ComponentTestImpl("c1");
        UIComponent child2 = new ComponentTestImpl("c2");
        UIComponent child3 = new ComponentTestImpl("c3");
        component.getChildren().add(child1);
        component.getChildren().add(child2);
        component.getChildren().add(child3);
        checkChildCount(component, 3);
        UIComponent child2a = new ComponentTestImpl("c2a");
        UIComponent child2b = new ComponentTestImpl("c2b");
        child2.getChildren().add(child2a);
        child2.getChildren().add(child2b);
        checkChildCount(child2, 2);

        // Enqueue a single FacesEvent for each component
        component.queueEvent(new EventTestImpl(component));
        component.queueEvent(new EventTestImpl(facet1));
        component.queueEvent(new EventTestImpl(facet2));
        component.queueEvent(new EventTestImpl(facet3));
        component.queueEvent(new EventTestImpl(child1));
        component.queueEvent(new EventTestImpl(child2));
        component.queueEvent(new EventTestImpl(child3));
        component.queueEvent(new EventTestImpl(child2a));
        component.queueEvent(new EventTestImpl(child2b));

        // Test processDecodes()
        ComponentTestImpl.trace(null);
        component.processDecodes(facesContext);
        assertEquals(lifecycleTrace("pD", "d"), ComponentTestImpl.trace());

        // Test processValidators()
        ComponentTestImpl.trace(null);
        component.processValidators(facesContext);
        assertEquals(lifecycleTrace("pV", null), ComponentTestImpl.trace());

        // Test processUpdates()
        ComponentTestImpl.trace(null);
        component.processUpdates(facesContext);
        assertEquals(lifecycleTrace("pU", null), ComponentTestImpl.trace());

    }

    // Check that the properties on the specified components are equal
    protected void checkProperties(UIComponent comp1, UIComponent comp2) {
        assertEquals(comp1.getClientId(facesContext), comp2.getClientId(facesContext));
        assertEquals(comp1.getId(), comp2.getId());
        assertEquals(comp1.isRendered(), comp2.isRendered());
        assertEquals(comp1.getRendererType(), comp2.getRendererType());
        assertEquals(comp1.getRendersChildren(), comp2.getRendersChildren());
    }

    protected void checkComponentListeners(UIComponent control, UIComponent toValidate) {

        List<SystemEventListener> lc = control.getListenersForEventClass(PostAddToViewEvent.class);
        List<SystemEventListener> tvl = toValidate.getListenersForEventClass(PostAddToViewEvent.class);
        List<SystemEventListener> lc2 = control.getListenersForEventClass(PostConstructViewMapEvent.class);
        List<SystemEventListener> tvl2 = toValidate.getListenersForEventClass(PostConstructViewMapEvent.class);
        assertTrue(lc.size() == tvl.size());
        assertTrue(lc2.size() == tvl2.size());

    }

    // Create a pristine component of the type to be used in state holder tests
    protected UIComponent createComponent() {
        return new ComponentTestImpl();
    }

    /**
     * Construct and return a lifecycle method call trace for the specified method names.
     *
     * @param lmethod Name of the lifecycle method under test
     * @param cmethod Name of the component method that corresponds
     * @return
     */
    protected String lifecycleTrace(String lmethod, String cmethod) {
        StringBuilder sb = new StringBuilder();
        lifecycleTrace(lmethod, cmethod, component, sb);
        return sb.toString();
    }

    protected void lifecycleTrace(String lmethod, String cmethod, UIComponent component, StringBuilder sb) {

        // Append the call for this lifecycle method
        String id = component.getId();
        sb.append('/').append(lmethod).append('-').append(id);
        if (!component.isRendered()) {
            return;
        }

        // Append the calls for each facet
        Iterator<String> names = component.getFacets().keySet().iterator();
        while (names.hasNext()) {
            String name = names.next();
            sb.append('/').append(lmethod).append('-').append(name);
            if (cmethod != null && component.getFacets().get(name).isRendered()) {
                sb.append('/').append(cmethod).append('-').append(name);
            }
        }

        // Append the calls for each child
        Iterator<UIComponent> kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            lifecycleTrace(lmethod, cmethod, kid, sb);
        }

        // Append the call for this component's component method
        if (cmethod != null && component.isRendered()) {
            sb.append('/').append(cmethod).append('-').append(id);
        }

    }

    @Test
    public void testGetFacetsAndChildren() {

        UIComponent testComponent = new ComponentTestImpl();
        UIComponent child1 = new ComponentTestImpl("child1");
        UIComponent child2 = new ComponentTestImpl("child2");
        UIComponent child3 = new ComponentTestImpl("child3");
        UIComponent facet1 = new ComponentTestImpl("facet1");
        UIComponent facet2 = new ComponentTestImpl("facet2");
        UIComponent facet3 = new ComponentTestImpl("facet3");

        testComponent.getChildren().add(child1);
        testComponent.getChildren().add(child2);
        testComponent.getChildren().add(child3);
        testComponent.getFacets().put("facet1", facet1);
        testComponent.getFacets().put("facet2", facet2);
        testComponent.getFacets().put("facet3", facet3);

        Iterator<UIComponent> iter = testComponent.getFacetsAndChildren();
        Object cur = null;
        boolean exceptionThrown = false;
        assertTrue(iter.hasNext());

        try {
            iter.remove();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // facets are returned in an undefined order.
        cur = iter.next();
        assertTrue(cur == facet1 || cur == facet2 || cur == facet3);
        cur = iter.next();
        assertTrue(cur == facet1 || cur == facet2 || cur == facet3);
        cur = iter.next();
        assertTrue(cur == facet1 || cur == facet2 || cur == facet3);

        // followed by components, in the order added
        cur = iter.next();
        assertTrue(cur == child1);
        cur = iter.next();
        assertTrue(cur == child2);
        cur = iter.next();
        assertTrue(cur == child3);

        assertTrue(!iter.hasNext());

    }

    private Object foundComponent = null;

    /**
     * <p>
     * Build a tree with the following layout.
     * </p>
     * <code><pre>
     * root: id: root
     *
     *   form1: id: form1
     *
     *     panel1: id: panel
     *
     *       input1: id: input1
     *
     *       input2: id: input2
     *
     *   form2: id: form2
     *
     *     panel2: id: panel
     *
     *       input3: id: input1
     *
     *       input4: id: input2
     * </pre></code>
     *
     * @return a Map<String, UIComponent>. The key is the string before the first : in the above layout. The value is the
     * component instance. Note that the keys in the map are <b>not</b> the ids.
     */
    private Map<String, UIComponent> setupInvokeOnComponentTree() {
        UIViewRoot root = new UIViewRoot();
        UIForm form1 = new UIForm();
        UIPanel panel1 = new UIPanel();
        UIInput input1 = new UIInput();
        UIInput input2 = new UIInput();
        UIForm form2 = new UIForm();
        UIPanel panel2 = new UIPanel();
        UIInput input3 = new UIInput();
        UIInput input4 = new UIInput();

        root.setId("root");
        form1.setId("form1");
        panel1.setId("panel");
        input1.setId("input1");
        input2.setId("input2");

        form2.setId("form2");
        panel2.setId("panel");
        input3.setId("input1");
        input4.setId("input2");

        root.getChildren().add(form1);
        form1.getChildren().add(panel1);
        panel1.getChildren().add(input1);
        panel1.getChildren().add(input2);

        root.getChildren().add(form2);
        form2.getChildren().add(panel2);
        panel2.getChildren().add(input3);
        panel2.getChildren().add(input4);
        Map<String, UIComponent> result = new HashMap<>();
        result.put("root", root);
        result.put("form1", form1);
        result.put("panel1", panel1);
        result.put("input1", input1);
        result.put("input2", input2);
        result.put("form2", form2);
        result.put("panel2", panel2);
        result.put("input3", input3);
        result.put("input4", input4);

        return result;
    }

    @Test
    public void testInvokeOnComponentPositive() throws Exception {

        Map<String, UIComponent> tree = setupInvokeOnComponentTree();

        UIViewRoot root = (UIViewRoot) tree.get("root");
        UIInput input1 = (UIInput) tree.get("input1");

        foundComponent = null;
        boolean result = false;

        assertNull(UIComponent.getCurrentComponent(facesContext));

        result = root.invokeOnComponent(facesContext, input1.getClientId(facesContext), new ContextCallback() {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent component) {
                assertEquals(UIComponent.getCurrentComponent(context), component);
                foundComponent = component;
            }
        });
        assertEquals(input1, foundComponent);
        assertTrue(result);
        assertNull(UIComponent.getCurrentComponent(facesContext));

    }

    @Test
    public void testInvokeOnComponentNegative() throws Exception {
        Map<String, UIComponent> tree = setupInvokeOnComponentTree();

        UIViewRoot root = (UIViewRoot) tree.get("root");
        UIInput input4 = (UIInput) tree.get("input4");

        foundComponent = null;
        boolean result = false;
        boolean exceptionThrown = false;

        // Negative case 0, null pointers
        exceptionThrown = false;
        FacesContext nullContext = null;
        ContextCallback nullCallback = null;
        try {
            root.invokeOnComponent(nullContext, "form:input7", nullCallback);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            root.invokeOnComponent(facesContext, null, nullCallback);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            root.invokeOnComponent(nullContext, null, nullCallback);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // Negative case 1, not found component.
        result = root.invokeOnComponent(facesContext, "form:input7", new ContextCallback() {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent component) {
                foundComponent = component;
            }
        });
        assertNull(foundComponent);
        assertTrue(!result);

        // Negative case 2A, callback throws exception with found component
        foundComponent = null;
        result = false;
        exceptionThrown = false;
        try {
            result = root.invokeOnComponent(facesContext, "form2:input2", new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent component) {
                    foundComponent = component;
                    // When else am I going to get the chance to throw this exception?
                    throw new IllegalStateException();
                }
            });
        } catch (FacesException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(foundComponent, input4);
        assertTrue(!result);

        // Negative case 2B, callback throws exception with not found component
        foundComponent = null;
        result = false;
        exceptionThrown = false;
        try {
            result = root.invokeOnComponent(facesContext, "form2:input6", new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent component) {
                    foundComponent = component;
                    // When else am I going to get the chance to throw this exception?
                    throw new IllegalStateException();
                }
            });
        } catch (FacesException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            exceptionThrown = true;
        }
        assertTrue(!exceptionThrown);
        assertNull(foundComponent);
        assertTrue(!result);

    }

    @Test
    public void testInvokeOnComponentWithPrependId() throws Exception {
        Map<String, UIComponent> tree = setupInvokeOnComponentTree();

        UIViewRoot root = (UIViewRoot) tree.get("root");
        UIForm truePrependIdForm = (UIForm) tree.get("form1");
        UIForm falsePrependIdForm = (UIForm) tree.get("form2");
        UIInput truePrependIdInput = (UIInput) tree.get("input2");
        UIInput falsePrependIdInput = (UIInput) tree.get("input3");

        truePrependIdForm.setPrependId(true);
        falsePrependIdForm.setPrependId(false);

        foundComponent = null;
        boolean result = false;
        boolean exceptionThrown = false;

        // Case 1, positive find with prependId == true
        result = root.invokeOnComponent(facesContext, "form1:input2", new ContextCallback() {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent component) {
                foundComponent = component;
            }
        });
        assertEquals(truePrependIdInput, foundComponent);
        assertTrue(result);

        // Case 2, negative find with prependId == true
        foundComponent = null;
        result = false;

        result = root.invokeOnComponent(facesContext, "form9:input5", new ContextCallback() {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent component) {
                foundComponent = component;
            }
        });
        assertNull(foundComponent);
        assertTrue(!result);

        // Case 3, exception positive find with prependId == true
        foundComponent = null;
        result = false;
        exceptionThrown = false;
        try {

            result = root.invokeOnComponent(facesContext, "form1:input2", new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent component) {
                    foundComponent = component;
                    throw new IllegalStateException();
                }
            });
        } catch (FacesException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            exceptionThrown = true;
        }
        assertEquals(truePrependIdInput, foundComponent);
        assertTrue(!result);
        assertTrue(exceptionThrown);

        // Case 4, exception negative find with prependId == true
        foundComponent = null;
        result = false;
        exceptionThrown = false;
        try {

            result = root.invokeOnComponent(facesContext, "formFozzy:inputKermit", new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent component) {
                    foundComponent = component;
                    throw new IllegalStateException();
                }
            });
        } catch (FacesException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            exceptionThrown = true;
        }
        assertNull(foundComponent);
        assertTrue(!result);
        assertTrue(!exceptionThrown);

        // Case 5, positive find with prependId == false
        result = root.invokeOnComponent(facesContext, "input1", new ContextCallback() {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent component) {
                foundComponent = component;
            }
        });
        assertEquals(falsePrependIdInput, foundComponent);
        assertTrue(result);

        // Case 6, negative find with prependId == false
        foundComponent = null;
        result = false;

        result = root.invokeOnComponent(facesContext, "input99", new ContextCallback() {
            @Override
            public void invokeContextCallback(FacesContext context, UIComponent component) {
                foundComponent = component;
            }
        });
        assertNull(foundComponent);
        assertTrue(!result);

        // Case 3, exception positive find with prependId == false
        foundComponent = null;
        result = false;
        exceptionThrown = false;
        try {

            result = root.invokeOnComponent(facesContext, "input1", new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent component) {
                    foundComponent = component;
                    throw new IllegalStateException();
                }
            });
        } catch (FacesException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            exceptionThrown = true;
        }
        assertEquals(falsePrependIdInput, foundComponent);
        assertTrue(!result);
        assertTrue(exceptionThrown);

        // Case 4, exception negative find with prependId == false
        foundComponent = null;
        result = false;
        exceptionThrown = false;
        try {

            result = root.invokeOnComponent(facesContext, "inputKermit", new ContextCallback() {
                @Override
                public void invokeContextCallback(FacesContext context, UIComponent component) {
                    foundComponent = component;
                    throw new IllegalStateException();
                }
            });
        } catch (FacesException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            exceptionThrown = true;
        }
        assertNull(foundComponent);
        assertTrue(!result);
        assertTrue(!exceptionThrown);

    }

    @Test
    public void testChildrenListAfterAddPublish() {

        Listener listener = new Listener();
        application.subscribeToEvent(PostAddToViewEvent.class, listener);

        UIComponent c1 = createComponent();
        c1.setInView(true);
        UIComponent c2 = createComponent();
        c2.setInView(true);
        UIComponent c3 = createComponent();
        c3.setInView(true);

        c1.getChildren().add(c2);
        SystemEvent e = listener.getEvent();
        assertNotNull(e);
        assertTrue(e.getSource() == c2);
        assertTrue(((UIComponent) e.getSource()).getParent() == c1);
        listener.reset();
        c2.getChildren().add(c3);
        e = listener.getEvent();
        assertNotNull(e);
        assertTrue(e.getSource() == c3);
        assertTrue(((UIComponent) e.getSource()).getParent() == c2);

        // ensure events are re-published if the event is added
        listener.reset();
        c2.getChildren().remove(c3);
        c1.getChildren().add(c3);
        e = listener.getEvent();
        assertNotNull(e);
        assertTrue(e.getSource() == c3);
        assertTrue(((UIComponent) e.getSource()).getParent() == c1);

        application.unsubscribeFromEvent(PostAddToViewEvent.class, listener);

    }

    @Test
    public void testFacetMapAfterAddViewPublish() {

        QueueingListener listener = new QueueingListener();
        application.subscribeToEvent(PostAddToViewEvent.class, listener);

        UIComponent c1 = createComponent();
        UIComponent c2 = createComponent();
        UIComponent c3 = createComponent();

        List<SystemEvent> e = listener.getEvents();
        Map<String, UIComponent> facets = c1.getFacets();
        facets.put("c2", c2);
        assertEquals(0, e.size());

        UIViewRoot root = new UIViewRoot();
        root.getChildren().add(c1);
        assertEquals(2, e.size());
        assertTrue(e.get(0).getSource() == c1);
        assertTrue(e.get(1).getSource() == c2);

        // remove c1 from the root and add c3 as a facet to c1 - no events should be
        // published
        e.clear();
        root.getChildren().remove(c1);
        facets = c1.getFacets();
        facets.put("c3", c3);
        assertEquals(0, e.size());

        // reorganize the facet structure to ensure nested facets work
        facets.remove("c3");
        c2.getFacets().put("c3", c3);
        root.getChildren().add(c1);
        assertEquals(3, e.size());
        assertTrue(e.get(0).getSource() == c1);
        assertTrue(e.get(1).getSource() == c2);
        assertTrue(e.get(2).getSource() == c3);

        e.clear();
        // ensure clear() method disconnects the facets from the view
        facets.clear();
        c2.getFacets().remove("c3");
        c2.getFacets().put("c3", c3);
        assertEquals(0, e.size());

        application.unsubscribeFromEvent(PostAddToViewEvent.class, listener);

    }

    @Test
    public void testChildrenListAfterAddViewPublish() {

        QueueingListener listener = new QueueingListener();
        application.subscribeToEvent(PostAddToViewEvent.class, listener);

        UIComponent c1 = createComponent();
        UIComponent c2 = createComponent();
        UIComponent c3 = createComponent();
        UIComponent c4 = createComponent();
        c1.getChildren().add(c2);
        List<SystemEvent> e = listener.getEvents();
        assertTrue(e.isEmpty());
        c2.getChildren().add(c3);
        assertTrue(e.isEmpty());
        UIViewRoot root = new UIViewRoot();
        root.getChildren().add(c1);

        // sub-tree has been added to the view. Ensure that subsequent additions
        // to that sub-tree cause the PostAddToViewEvent to fire.
        c2.getChildren().add(c4);
        assertEquals(4, e.size());

        UIComponent[] comps = { c1, c2, c3, c4 };
        for (int i = 0; i < comps.length; i++) {
            assertTrue(e.get(i).getSource() == comps[i], "Index " + i + " invalid");
        }

        // remove c1 and it's children from the subview, then remove and
        // re-add one of the children in the sub-tree. No event should
        // be fired
        e.clear();
        root.getChildren().remove(c1);
        c2.getChildren().remove(c4);
        c2.getChildren().add(c4);
        assertEquals(0, e.size());

        c2.getChildren().remove(c4);
        c1.getChildren().add(c4);
        assertEquals(0, e.size());

        // re-wire c1 as a child of root and ensure all children get re-notified
        root.getChildren().add(c1);
        assertEquals(4, e.size());

        for (int i = 0; i < comps.length; i++) {
            assertTrue(e.get(i).getSource() == comps[i], "Index " + i + " invalid");
        }

        // validate clearing c1's children (effectively removing them from the view
        // will result in no events being fired of components are added to any of
        // the disconnected children.
        // At this point in the test, c2 and c4 are children of c1, and c3
        // is a child of c2.
        c1.getChildren().clear();
        UIComponent temp = createComponent();
        e.clear();
        c2.getChildren().add(temp);
        assertEquals(0, e.size());
        c2.getChildren().remove(temp);
        c3.getChildren().add(temp);
        assertEquals(0, e.size());
        c3.getChildren().remove(temp);
        c4.getChildren().add(temp);
        assertEquals(0, e.size());
        c4.getChildren().remove(temp);

        // now add c2 and c4 as children of c1. This should cause three
        // events to fire
        c1.getChildren().add(c2);
        c1.getChildren().add(c4);
        assertEquals(3, e.size());

        UIComponent[] comps2 = { c2, c3, c4 };
        for (int i = 0; i < comps2.length; i++) {
            assertTrue(e.get(i).getSource() == comps2[i], "Index " + i + " invalid");
        }

        // validate add(int, UIComponent) fires events
        e.clear();
        c1.getChildren().remove(c4);
        c1.getChildren().add(0, c4);

        assertTrue(c1.getChildren().get(0) == c4);
        assertTrue(c1.getChildren().get(1) == c2);
        assertEquals(1, e.size());
        assertTrue(e.get(0).getSource() == c4);

        // validate addAll(Collection<UIComponent>) fires events
        e.clear();
        c1.getChildren().clear();
        List<UIComponent> children = new ArrayList<>(2);
        Collections.addAll(children, c2, c4);
        c1.getChildren().addAll(children);
        assertTrue(c1.getChildren().get(0) == c2);
        assertTrue(c1.getChildren().get(1) == c4);
        assertEquals(3, e.size());
        assertTrue(e.get(0).getSource() == c2);
        assertTrue(e.get(2).getSource() == c4);

        // validate addAll(int, Collection<UIComponent>) fires events
        e.clear();
        children = new ArrayList<>(2);
        UIComponent t1 = createComponent();
        UIComponent t2 = createComponent();
        Collections.addAll(children, t1, t2);
        c1.getChildren().addAll(0, children);
        assertTrue(c1.getChildren().get(0) == t1);
        assertTrue(c1.getChildren().get(1) == t2);
        assertTrue(c1.getChildren().get(2) == c2);
        assertTrue(c1.getChildren().get(3) == c4);
        assertEquals(2, e.size());
        assertTrue(e.get(0).getSource() == t1);
        assertTrue(e.get(1).getSource() == t2);

        // validate retainAll(Collection<UIComponent> properly disconnects
        // the components from the view such that events aren't fired
        // if children are added to them
        e.clear();
        List<UIComponent> retained = new ArrayList<>(2);
        Collections.addAll(retained, c2, c4);
        c1.getChildren().retainAll(retained);
        assertTrue(c1.getChildren().size() == 2);
        assertTrue(c1.getChildren().get(0) == c2);
        assertTrue(c1.getChildren().get(1) == c4);
        t1.getChildren().add(t2);
        assertEquals(0, e.size());

        // test set(int, UIComponent) properly fires an event if the parent
        // the component is being added to is wired to the view
        e.clear();
        c1.getChildren().set(0, t1);
        assertTrue(c1.getChildren().size() == 2);
        assertTrue(c1.getChildren().get(0) == t1);
        assertTrue(c1.getChildren().get(1) == c4);
        assertEquals(2, e.size());
        assertTrue(e.get(0).getSource() == t1);
        assertTrue(e.get(1).getSource() == t2);

        // c2 was removed by the set operation, so ensure it's marked as
        // having been removed from the view by ensuring events aren't fired.
        e.clear();
        UIComponent t3 = createComponent();
        c2.getChildren().add(t3);
        assertEquals(0, e.size());

        application.unsubscribeFromEvent(PostAddToViewEvent.class, listener);

        // validate Iterator.remove() over c1's children correctly disconnects
        // the children from the view
        for (Iterator<UIComponent> i = c1.getChildren().iterator(); i.hasNext();) {
            i.next();
            i.remove();
        }

        // at this point, t1 and c4 should be disconnected meaning adding children
        // to t1, t2, or c4 should result in no events being fired
        e.clear();
        t1.getChildren().add(temp);
        assertEquals(0, e.size());
        t1.getChildren().remove(temp);
        t2.getChildren().add(temp);
        assertEquals(0, e.size());
        t2.getChildren().remove(temp);
        c4.getChildren().add(temp);
        assertEquals(0, e.size());
        c4.getChildren().remove(temp);

    }

    @Test
    public void testEncodeBeginPublish() throws Exception {

        Listener listener = new Listener();
        application.subscribeToEvent(PreRenderComponentEvent.class, listener);

        UIComponent c1 = createComponent();
        c1.encodeBegin(facesContext);
        SystemEvent e = listener.getEvent();
        assertNotNull(e);
        assertTrue(e.getSource() == c1);
        listener.reset();
        c1.encodeChildren(facesContext);
        assertNull(listener.getEvent());
        c1.encodeEnd(facesContext);
        assertNull(listener.getEvent());

        application.unsubscribeFromEvent(PreRenderComponentEvent.class, listener);

    }

    // --------------------------------------------------------- Private Classes
    public static final class Listener implements SystemEventListener {

        private SystemEvent event;

        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            this.event = event;
        }

        @Override
        public boolean isListenerForSource(Object source) {
            return source instanceof UIComponent;
        }

        public SystemEvent getEvent() {
            return event;
        }

        public void reset() {
            event = null;
        }
    }

    public static final class QueueingListener implements SystemEventListener {

        private List<SystemEvent> events = new ArrayList<>();

        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            events.add(event);
        }

        @Override
        public boolean isListenerForSource(Object source) {
            return source instanceof UIComponent;
        }

        public List<SystemEvent> getEvents() {
            return events;
        }

        public void reset() {
            events.clear();
        }
    }

    public static final class ComponentListener implements ComponentSystemEventListener {

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {

        }
    }

    public static final class UIComponentListener extends UIComponentBase implements ComponentSystemEventListener {

        @Override
        public String getFamily() {
            return "family";
        }

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        }

    }

    public static final class UIComponentOverrideEncodeBegin extends UIComponentBase {

        @Override
        public String getFamily() {
            return "UIComponentOverrideEncodeBegin";
        }

        @Override
        public void encodeBegin(FacesContext context) throws IOException {
            // no-op
        }

    }

    public static final class UIComponentOverrideEncodeEnd extends UIComponentBase {

        @Override
        public String getFamily() {
            return "UIComponentOverrideEncodeEnd";
        }

        @Override
        public void encodeEnd(FacesContext context) throws IOException {
            // no-op
        }
    }

    public static final class CustomAbortProcessingException extends AbortProcessingException {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public CustomAbortProcessingException() {
        }

        public CustomAbortProcessingException(String message) {
            super(message);
        }

        public CustomAbortProcessingException(Throwable cause) {
            super(cause);
        }

        public CustomAbortProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
