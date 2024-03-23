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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * <p>
 * Base unit tests for all {@link UIComponent} implementation classes.
 * </p>
 */
public class UIComponentTestCase extends JUnitFacesTestCaseBase {

    // ------------------------------------------------------ Instance Variables
    // The component to be tested
    protected UIComponent component = null;

    // The set of attribute names expected on a pristine component instance
    protected String expectedAttributes[] = null;

    // The expected component family on a pristine component instance
    protected String expectedFamily = null;

    // The expected component identifier on a pristine component instance
    protected String expectedId = null;

    // The expected rendered on a pristine component instance
    protected boolean expectedRendered = true;

    // The expected rendererType on a pristine component instance
    protected String expectedRendererType = null;

    // The expected rendersChildren on a pristine component instance
    protected boolean expectedRendersChildren = false;

    private Map.Entry<String, UIComponent> bogusEntry = new Map.Entry<>() {
        @Override
        public boolean equals(Object r) {
            return false;
        }

        @Override
        public String getKey() {
            return "key";
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public UIComponent getValue() {
            return null;
        }

        @Override
        public UIComponent setValue(UIComponent value) {
            return null;
        }

    };

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        expectedAttributes = new String[0];
        expectedFamily = "Test";
        expectedId = "test";
        expectedRendered = true;
        expectedRendererType = null;
        expectedRendersChildren = false;

        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setViewId("/viewId");
        facesContext.setViewRoot(root);
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, renderKit);
        } catch (IllegalArgumentException e) {
        }

        component = new ComponentTestImpl(expectedId);
    }

    // Tear down instance variables required by this test case.
    @Override
    @AfterEach
    public void tearDown() throws Exception {

        component = null;
        expectedAttributes = null;
        expectedFamily = null;
        expectedId = null;
        expectedRendered = true;
        expectedRendererType = null;
        expectedRendersChildren = false;
        super.tearDown();

    }

    // ------------------------------------------------- Individual Test Methods
    @Test
    public void testValidationEvents() {
        Listener prelistener = new Listener();
        Listener postlistener = new Listener();
        List<String> ldata = new ArrayList<>();
        ldata.add("one");
        UIViewRoot root = new UIViewRoot();
        root.setId("root");
        root.subscribeToEvent(PreValidateEvent.class, prelistener);
        root.subscribeToEvent(PostValidateEvent.class, postlistener);
        UIOutput out = new UIOutput();
        out.setId("out");
        out.subscribeToEvent(PreValidateEvent.class, prelistener);
        out.subscribeToEvent(PostValidateEvent.class, postlistener);
        root.getChildren().add(out);
        UIForm f = new UIForm();
        f.setSubmitted(true);
        f.setId("form");
        f.subscribeToEvent(PreValidateEvent.class, prelistener);
        f.subscribeToEvent(PostValidateEvent.class, postlistener);
        root.getChildren().add(f);
        UIData data = new UIData();
        data.setId("data");
        data.subscribeToEvent(PreValidateEvent.class, prelistener);
        data.subscribeToEvent(PostValidateEvent.class, postlistener);
        data.setValue(ldata);
        UIColumn c = new UIColumn();
        c.setId("column");
        c.subscribeToEvent(PreValidateEvent.class, prelistener);
        c.subscribeToEvent(PostValidateEvent.class, postlistener);
        UIInput in = new UIInput();
        in.setId("in");
        in.subscribeToEvent(PreValidateEvent.class, prelistener);
        in.subscribeToEvent(PostValidateEvent.class, postlistener);
        in.addValidator(new ValidationSignal());
        c.getChildren().add(in);
        data.getChildren().add(c);
        f.getChildren().add(data);
        data.setRowIndex(0);
        UIComponent col = data.getChildren().get(0);
        ((UIInput) col.getChildren().get(0)).setSubmittedValue("hello");
        data.setRowIndex(-1);
        root.processValidators(facesContext);
        assertEquals("root/out/form/data/in/", prelistener.getResults());
        assertEquals("out/*/in/data/form/root/", postlistener.getResults());

    }

    // Test behavior of Map returned by getAttributes()
    @Test
    public void testAttributesMap() {

        // Initialize some attributes
        Map<String, Object> attributes = component.getAttributes();
        attributes.put("foo", "bar");
        attributes.put("baz", "bop");

        // Test containsKey()
        assertTrue(attributes.containsKey("foo"));
        assertTrue(attributes.containsKey("baz"));
        assertTrue(!attributes.containsKey("bar"));
        assertTrue(!attributes.containsKey("bop"));
        assertTrue(!attributes.containsKey("id")); // Property name
        assertTrue(!attributes.containsKey("parent")); // Property name

        // Test get()
        assertEquals("bar", attributes.get("foo"));
        assertEquals("bop", attributes.get("baz"));
        assertNull(attributes.get("bar"));
        assertNull(attributes.get("bop"));
        component.setId("oldvalue");
        assertEquals("oldvalue", attributes.get("id")); // Property
        component.setRendered(false);
        assertTrue(!((Boolean) attributes.get("rendered")).booleanValue());
        component.setRendered(true);
        assertTrue(((Boolean) attributes.get("rendered")).booleanValue());

        // Test put()
        try {
            attributes.put(null, "dummy");
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
        try {
            attributes.put("rendersChildren", null); // Primitive property
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            attributes.put("rendersChildren", Boolean.TRUE); // Write-only
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        attributes.put("id", "newvalue");
        assertEquals("newvalue", attributes.get("id"));
        assertEquals("newvalue", component.getId());
        attributes.put("rendered", Boolean.TRUE);
        assertTrue(component.isRendered());
        attributes.put("rendered", Boolean.FALSE);
        assertTrue(!component.isRendered());

        // Test remove()
        attributes.remove("baz");
        assertTrue(!attributes.containsKey("baz"));
        assertNull(attributes.get("baz"));
        try {
            attributes.remove("id");
            fail("Should have thrown IllegalArgumentException()");
        } catch (IllegalArgumentException e) {
            // Expected result
        }

    }

    // Negative tests on attribute methods
    @Test
    public void testAttributesNegative() {

        // getAttributes().get() - null
        try {
            component.getAttributes().get(null);
            fail("should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }

        // getAttributes().put() - null
        try {
            component.getAttributes().put(null, "bar");
            fail("should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }

    }

    // Positive tests on attribute methods
    @Test
    public void testAttributesPositive() {

        checkAttributeCount(component, expectedAttributes.length);
        checkAttributeMissing(component, "foo");
        checkAttributeMissing(component, "baz");

        component.getAttributes().put("foo", "bar");
        checkAttributeCount(component, expectedAttributes.length + 1);
        checkAttributePresent(component, "foo", "bar");
        checkAttributeMissing(component, "baz");

        component.getAttributes().put("baz", "bop");
        checkAttributeCount(component, expectedAttributes.length + 2);
        checkAttributePresent(component, "foo", "bar");
        checkAttributePresent(component, "baz", "bop");

        component.getAttributes().put("baz", "boo");
        checkAttributeCount(component, expectedAttributes.length + 2);
        checkAttributePresent(component, "foo", "bar");
        checkAttributePresent(component, "baz", "boo");

        component.getAttributes().remove("foo");
        checkAttributeCount(component, expectedAttributes.length + 1);
        checkAttributeMissing(component, "foo");
        checkAttributePresent(component, "baz", "boo");

    }

    // Test attribute-property transparency
    @Test
    public void testAttributesTransparency() {

        assertEquals(component.getChildren(), component.getAttributes().get("children"));

        assertEquals(component.getFacets(), component.getAttributes().get("facets"));

        assertEquals(component.getId(), component.getAttributes().get("id"));

        assertEquals(component.getParent(), component.getAttributes().get("parent"));

        assertEquals(component.isRendered(), ((Boolean) component.getAttributes().get("rendered")).booleanValue());
        component.setRendered(false);
        assertEquals(Boolean.FALSE, component.getAttributes().get("rendered"));
        component.setRendered(true);
        assertEquals(Boolean.TRUE, component.getAttributes().get("rendered"));
        component.getAttributes().put("rendered", Boolean.FALSE);
        assertTrue(!component.isRendered());
        component.getAttributes().put("rendered", Boolean.TRUE);
        assertTrue(component.isRendered());

        component.setRendererType("foo");
        assertEquals("foo", component.getAttributes().get("rendererType"));
        component.setRendererType(null);
        assertNull(component.getAttributes().get("rendererType"));
        component.getAttributes().put("rendererType", "bar");
        assertEquals("bar", component.getRendererType());
        component.getAttributes().put("rendererType", null);
        assertNull(component.getRendererType());

        assertEquals(component.getRendersChildren(), ((Boolean) component.getAttributes().get("rendersChildren")).booleanValue());

    }

    // Test getChildren().iterator()
    @Test
    public void testChildrenIterator() {

        Iterator<UIComponent> kids;

        // Construct components we will need
        UIComponent comp0 = new ComponentTestImpl(null);
        UIComponent comp1 = new ComponentTestImpl("comp1");
        UIComponent comp2 = new ComponentTestImpl("comp2");
        UIComponent comp3 = new ComponentTestImpl("comp3");
        UIComponent comp4 = new ComponentTestImpl("comp4");
        UIComponent comp5 = new ComponentTestImpl("comp5");
        List<UIComponent> comps = new ArrayList<>();
        comps.add(comp0);
        comps.add(comp1);
        comps.add(comp2);
        comps.add(comp3);
        comps.add(comp4);
        comps.add(comp5);

        // Test hasNext() and next()
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().iterator();
        assertTrue(kids.hasNext());
        assertEquals(comp0, kids.next());
        assertEquals(comp1, kids.next());
        assertEquals(comp2, kids.next());
        assertEquals(comp3, kids.next());
        assertEquals(comp4, kids.next());
        assertEquals(comp5, kids.next());
        assertTrue(!kids.hasNext());

        // Test remove()
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (kid == comp2 || kid == comp4) {
                kids.remove();
            }
        }
        kids = component.getChildren().iterator();
        assertTrue(kids.hasNext());
        assertEquals(comp0, kids.next());
        assertEquals(comp1, kids.next());
        assertEquals(comp3, kids.next());
        assertEquals(comp5, kids.next());
        assertTrue(!kids.hasNext());

    }

    // Test getChildren().listIterator()
    @Test
    public void testChildrenListIterator() {

        ListIterator<UIComponent> kids;

        // Construct components we will need
        UIComponent comp0 = new ComponentTestImpl(null);
        UIComponent comp1 = new ComponentTestImpl("comp1");
        UIComponent comp2 = new ComponentTestImpl("comp2");
        UIComponent comp3 = new ComponentTestImpl("comp3");
        UIComponent comp4 = new ComponentTestImpl("comp4");
        UIComponent comp5 = new ComponentTestImpl("comp5");
        UIComponent comp6 = new ComponentTestImpl("comp6");
        List<UIComponent> comps = new ArrayList<>();
        comps.add(comp0);
        comps.add(comp1);
        comps.add(comp2);
        comps.add(comp3);
        comps.add(comp4);
        comps.add(comp5);

        // Test hasNext(), next(), and nextIndex()
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().listIterator();
        assertTrue(kids.hasNext());
        assertEquals(0, kids.nextIndex());
        assertEquals(comp0, kids.next());
        assertEquals(1, kids.nextIndex());
        assertEquals(comp1, kids.next());
        assertEquals(2, kids.nextIndex());
        assertEquals(comp2, kids.next());
        assertEquals(3, kids.nextIndex());
        assertEquals(comp3, kids.next());
        assertEquals(4, kids.nextIndex());
        assertEquals(comp4, kids.next());
        assertEquals(5, kids.nextIndex());
        assertEquals(comp5, kids.next());
        assertEquals(6, kids.nextIndex());
        assertTrue(!kids.hasNext());

        // Test hasPrevious(), previous(), and previousIndex()
        assertTrue(kids.hasPrevious());
        assertEquals(5, kids.previousIndex());
        assertEquals(comp5, kids.previous());
        assertEquals(4, kids.previousIndex());
        assertEquals(comp4, kids.previous());
        assertEquals(3, kids.previousIndex());
        assertEquals(comp3, kids.previous());
        assertEquals(2, kids.previousIndex());
        assertEquals(comp2, kids.previous());
        assertEquals(1, kids.previousIndex());
        assertEquals(comp1, kids.previous());
        assertEquals(0, kids.previousIndex());
        assertEquals(comp0, kids.previous());
        assertEquals(-1, kids.previousIndex());
        assertTrue(!kids.hasPrevious());

        // Test remove()
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().listIterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (kid == comp2 || kid == comp4) {
                kids.remove();
            }
        }
        kids = component.getChildren().listIterator();
        assertTrue(kids.hasNext());
        assertEquals(comp0, kids.next());
        assertEquals(comp1, kids.next());
        assertEquals(comp3, kids.next());
        assertEquals(comp5, kids.next());
        assertTrue(!kids.hasNext());

        // Test set()
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().listIterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (kid == comp2) {
                kids.set(comp6);
            }
        }
        kids = component.getChildren().listIterator();
        assertTrue(kids.hasNext());
        assertEquals(0, kids.nextIndex());
        assertEquals(comp0, kids.next());
        assertEquals(1, kids.nextIndex());
        assertEquals(comp1, kids.next());
        assertEquals(2, kids.nextIndex());
        assertEquals(comp6, kids.next());
        assertEquals(3, kids.nextIndex());
        assertEquals(comp3, kids.next());
        assertEquals(4, kids.nextIndex());
        assertEquals(comp4, kids.next());
        assertEquals(5, kids.nextIndex());
        assertEquals(comp5, kids.next());
        assertEquals(6, kids.nextIndex());
        assertTrue(!kids.hasNext());

        // Test add()
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().listIterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (kid == comp2) {
                kids.add(comp6);
            }
        }
        kids = component.getChildren().listIterator();
        assertTrue(kids.hasNext());
        assertEquals(0, kids.nextIndex());
        assertEquals(comp0, kids.next());
        assertEquals(1, kids.nextIndex());
        assertEquals(comp1, kids.next());
        assertEquals(2, kids.nextIndex());
        assertEquals(comp2, kids.next());
        assertEquals(3, kids.nextIndex());
        assertEquals(comp6, kids.next());
        assertEquals(4, kids.nextIndex());
        assertEquals(comp3, kids.next());
        assertEquals(5, kids.nextIndex());
        assertEquals(comp4, kids.next());
        assertEquals(6, kids.nextIndex());
        assertEquals(comp5, kids.next());
        assertEquals(7, kids.nextIndex());
        assertTrue(!kids.hasNext());

        // Test listIterator(int)
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        kids = component.getChildren().listIterator(2);
        assertTrue(kids.hasNext());
        assertTrue(kids.hasPrevious());
        assertEquals(2, kids.nextIndex());
        assertEquals(1, kids.previousIndex());
        assertEquals(comp2, kids.next());
        assertEquals(comp3, kids.next());
        assertEquals(comp4, kids.next());
        assertEquals(comp4, kids.previous());
        assertEquals(comp3, kids.previous());
        assertEquals(comp2, kids.previous());
        assertEquals(comp1, kids.previous());

        // Test IOB exception for list iterator
        component.getChildren().clear();
        component.getChildren().addAll(comps);
        try {
            component.getChildren().listIterator(-1);
            fail("Should throw IndexOutOfBoundsException on index -1");
        } catch (IndexOutOfBoundsException e) {
            // Expected result
        }

        try {
            component.getChildren().listIterator(component.getChildren().size() + 1);
            fail("Should throw IndexOutOfBoundsException on index = size() + 1");
        } catch (IndexOutOfBoundsException e) {
            // Expected result
        }

        // Iterate with list iterator in reverse order
        int i = component.getChildren().size() - 1;
        for (ListIterator<UIComponent> li = component.getChildren().listIterator(component.getChildren().size()); li.hasPrevious();) {

            assertEquals(comps.get(i--), li.previous());
        }

    }

    // Negative tests on children methods
    @Test
    public void testChidrenNegative() {

        // Construct components we will need
        UIComponent comp0 = new ComponentTestImpl(null);
        UIComponent comp1 = new ComponentTestImpl("comp1");
        UIComponent comp2 = new ComponentTestImpl("comp2");
        UIComponent comp3 = new ComponentTestImpl("comp3");

        // Set up and verify initial state
        List<UIComponent> children = component.getChildren();
        children.add(comp0);
        children.add(comp1);
        children.add(comp2);
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // add(Object) - NullPointerException
        try {
            children.add(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // add(int,Object) - IndexOutOfBoundsException low
        try {
            children.add(-1, comp3);
            fail("Should have thrown IndexOutOfBoundsException low");
        } catch (IndexOutOfBoundsException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // add(int,Object) - IndexOutOfBoundsException high
        try {
            children.add(4, comp3);
            fail("Should have thrown IndexOutOfBoundsException high");
        } catch (IndexOutOfBoundsException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // add(int,Object) - NullPointerException
        try {
            children.add(1, null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // set(int,Object) - IndexOutOfBoundsException low
        try {
            children.set(-1, comp3);
            fail("Should have thrown IndexOutOfBoundsException low");
        } catch (IndexOutOfBoundsException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // set(int,Object) - IndexOutOfBoundsException high
        try {
            children.set(4, comp3);
            fail("Should have thrown IndexOutOfBoundsException high");
        } catch (IndexOutOfBoundsException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

        // set(int,Object) - NullPointerException
        try {
            children.set(1, null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildMissing(component, comp3);

    }

    // Positive tests on children methods
    @Test
    public void testChildrenPositive() {

        // Construct components we will need
        UIComponent comp0 = new ComponentTestImpl(null);
        UIComponent comp1 = new ComponentTestImpl("comp1");
        UIComponent comp2 = new ComponentTestImpl("comp2");
        UIComponent comp3 = new ComponentTestImpl("comp3");
        UIComponent comp4 = new ComponentTestImpl("comp4");
        UIComponent comp5 = new ComponentTestImpl("comp5");
        UIComponent comp6 = new ComponentTestImpl("comp6");

        // Verify initial state
        List<UIComponent> children = component.getChildren();
        checkChildMissing(component, comp0);
        checkChildCount(component, 0);
        checkChildMissing(component, comp1);
        checkChildMissing(component, comp2);
        checkChildMissing(component, comp3);
        checkChildMissing(component, comp4);
        checkChildMissing(component, comp5);
        checkChildMissing(component, comp6);

        // add(Object)
        children.add(comp1);
        checkChildCount(component, 1);
        checkChildMissing(component, comp0);
        checkChildPresent(component, comp1, 0);
        checkChildMissing(component, comp2);
        checkChildMissing(component, comp3);
        checkChildMissing(component, comp4);
        checkChildMissing(component, comp5);
        checkChildMissing(component, comp6);

        // add(int, Object)
        children.add(0, comp0);
        checkChildCount(component, 2);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildMissing(component, comp2);
        checkChildMissing(component, comp3);
        checkChildMissing(component, comp4);
        checkChildMissing(component, comp5);
        checkChildMissing(component, comp6);

        // addAll(Collection)
        ArrayList<UIComponent> list1 = new ArrayList<>();
        list1.add(comp4);
        list1.add(comp5);
        children.addAll(list1);
        checkChildCount(component, 4);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildMissing(component, comp2);
        checkChildMissing(component, comp3);
        checkChildPresent(component, comp4, 2);
        checkChildPresent(component, comp5, 3);
        checkChildMissing(component, comp6);

        // addAll(int, Collection)
        ArrayList<UIComponent> list2 = new ArrayList<>();
        list2.add(comp2);
        list2.add(comp3);
        children.addAll(2, list2);
        checkChildCount(component, 6);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildPresent(component, comp3, 3);
        checkChildPresent(component, comp4, 4);
        checkChildPresent(component, comp5, 5);
        checkChildMissing(component, comp6);

        // contains(Object) is tested in checkChildPresent / checkChildMissing
        // containsAll(Collection)
        assertTrue(children.containsAll(list1));
        assertTrue(children.containsAll(list2));

        // get(int) is tested in checkChildPresent / checkChildMissing
        // indexOf(Object) is tested in checkChildPresent / checkChildMissing
        // isEmpty() is tested in checkChildCount
        // iterator() is tested in testChildrenIterator
        // listIterator() is tested in testChildrenListIterator
        // toArray(Object[])
        UIComponent kids[] = children.toArray(new UIComponent[0]);
        assertEquals(comp0, kids[0]);
        assertEquals(comp1, kids[1]);
        assertEquals(comp2, kids[2]);
        assertEquals(comp3, kids[3]);
        assertEquals(comp4, kids[4]);
        assertEquals(comp5, kids[5]);

        // subList(int,int)
        List<UIComponent> subList = children.subList(3, 5);
        assertEquals(2, subList.size());
        assertEquals(comp3, subList.get(0));
        assertEquals(comp4, subList.get(1));

        // set(int,Object)
        children.set(4, comp6);
        checkChildCount(component, 6);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildPresent(component, comp3, 3);
        checkChildMissing(component, comp4);
        checkChildPresent(component, comp5, 5);
        checkChildPresent(component, comp6, 4);
        assertTrue(!children.containsAll(list1));
        assertTrue(children.containsAll(list2));

        // remove(int)
        children.remove(4);
        checkChildCount(component, 5);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildPresent(component, comp2, 2);
        checkChildPresent(component, comp3, 3);
        checkChildMissing(component, comp4);
        checkChildPresent(component, comp5, 4);
        checkChildMissing(component, comp6);
        assertTrue(!children.containsAll(list1));
        assertTrue(children.containsAll(list2));

        // removeAll(Collection)
        children.removeAll(list2);
        checkChildCount(component, 3);
        checkChildPresent(component, comp0, 0);
        checkChildPresent(component, comp1, 1);
        checkChildMissing(component, comp2);
        checkChildMissing(component, comp3);
        checkChildMissing(component, comp4);
        checkChildPresent(component, comp5, 2);
        checkChildMissing(component, comp6);
        assertTrue(!children.containsAll(list1));
        assertTrue(!children.containsAll(list2));

        // retainAll()
        ArrayList<UIComponent> list3 = new ArrayList<>();
        list3.add(comp1);
        list3.add(comp3);
        list3.add(comp5);
        children.retainAll(list3);
        checkChildCount(component, 2);
        checkChildMissing(component, comp0);
        checkChildPresent(component, comp1, 0);
        checkChildMissing(component, comp2);
        checkChildMissing(component, comp3);
        checkChildMissing(component, comp4);
        checkChildPresent(component, comp5, 1);
        checkChildMissing(component, comp6);
        assertTrue(!children.containsAll(list3));

        // size() is tested in checkChildCount
        // clear()
        children.clear();
        checkChildCount(component, 0);
        assertNull(comp0.getParent());
        assertNull(comp1.getParent());
        assertNull(comp2.getParent());
        assertNull(comp3.getParent());
        assertNull(comp4.getParent());
        assertNull(comp5.getParent());
        assertNull(comp6.getParent());

    }

    // Test replacing a child with a new one that has the same id
    @Test
    public void testChidrenReplace() {

        ComponentTestImpl child1 = new ComponentTestImpl("child");
        ComponentTestImpl child2 = new ComponentTestImpl("child");

        checkChildCount(component, 0);
        component.getChildren().add(child1);
        checkChildCount(component, 1);
        checkChildPresent(component, child1, 0);
        checkChildMissing(component, child2);
        component.getChildren().set(0, child2);
        checkChildCount(component, 1);
        checkChildMissing(component, child1);
        checkChildPresent(component, child2, 0);
        component.getChildren().clear();
        checkChildCount(component, 0);

    }

    // Test Set returned by getFacets().entrySet()
    @Test
    public void testFacetsMapEntrySet() {

        Map<String, UIComponent> facets;
        Set<Map.Entry<String, UIComponent>> matches;
        Set<Map.Entry<String, UIComponent>> entrySet;
        Iterator<Map.Entry<String, UIComponent>> entries;

        // Construct the pre-load set of facets we will need
        UIComponent facet1 = new ComponentTestImpl("facet1");
        UIComponent facet2 = new ComponentTestImpl("facet2");
        UIComponent facet3 = new ComponentTestImpl("facet3");
        UIComponent facet4 = new ComponentTestImpl("facet4");
        UIComponent facet5 = new ComponentTestImpl("facet5");
        UIComponent facet6 = new ComponentTestImpl("facet6"); // Not normally added
        Map<String, UIComponent> preload = new HashMap<>();
        preload.put("a", facet1);
        preload.put("b", facet2);
        preload.put("c", facet3);
        preload.put("d", facet4);
        preload.put("e", facet5);

        // Test add()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        try {
            entrySet.add(bogusEntry);
            fail("Should have thrown UnsupportedOperationExcepton");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // Test clear()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        assertEquals(5, facets.size());
        assertEquals(5, entrySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);
        entrySet.clear();
        assertEquals(0, facets.size());
        assertEquals(0, entrySet.size());
        checkFacetMissing(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetMissing(component, "e", facet5);

        // Test contains()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        assertTrue(entrySet.contains(new TestMapEntry("a", facet1)));
        assertTrue(entrySet.contains(new TestMapEntry("b", facet2)));
        assertTrue(entrySet.contains(new TestMapEntry("c", facet3)));
        assertTrue(entrySet.contains(new TestMapEntry("d", facet4)));
        assertTrue(entrySet.contains(new TestMapEntry("e", facet5)));
        assertTrue(!entrySet.contains(new TestMapEntry("f", facet6)));

        // Test containsAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        matches = new HashSet<>();
        matches.add(new TestMapEntry("a", facet1));
        matches.add(new TestMapEntry("c", facet3));
        matches.add(new TestMapEntry("d", facet4));
        assertTrue(entrySet.containsAll(matches));
        matches = new HashSet<>();
        matches.add(new TestMapEntry("a", facet1));
        matches.add(new TestMapEntry("c", facet3));
        matches.add(new TestMapEntry("f", facet6));
        assertTrue(!entrySet.containsAll(matches));

        // Test iterator().hasNext() and iterator().next()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        matches = new HashSet<>();
        entries = entrySet.iterator();
        while (entries.hasNext()) {
            matches.add(entries.next());
        }
        assertTrue(entrySet.equals(matches));

        // Test iterator().remove()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        entries = entrySet.iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            if ("b".equals(entry.getKey()) || "d".equals(entry.getKey())) {
                entries.remove();
            }
        }
        assertEquals(3, facets.size());
        assertEquals(3, entrySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test iterator() based modify-value
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        entries = entrySet.iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            if ("c".equals(entry.getKey())) {
                entry.setValue(facet6);
            }
        }
        assertEquals(5, facets.size());
        assertEquals(5, entrySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetPresent(component, "c", facet6);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test remove()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        entrySet.remove(new TestMapEntry("c", facet3));
        assertEquals(4, facets.size());
        assertEquals(4, entrySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test removeAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        matches = new HashSet<>();
        matches.add(new TestMapEntry("b", facet2));
        matches.add(new TestMapEntry("d", facet4));
        entrySet.removeAll(matches);
        assertEquals(3, facets.size());
        assertEquals(3, entrySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test retainAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        entrySet = facets.entrySet();
        matches = new HashSet<>();
        matches.add(new TestMapEntry("b", facet2));
        matches.add(new TestMapEntry("d", facet4));
        matches.add(new TestMapEntry("f", facet6));
        entrySet.retainAll(matches);
        assertEquals(2, facets.size());
        assertEquals(2, entrySet.size());
        checkFacetMissing(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetMissing(component, "e", facet5);

    }

    // Test Set returned by getFacets().keySet()
    @Test
    public void testFacetsMapKeySet() {

        Map<String, UIComponent> facets;
        Set<Object> matches;
        Set<String> keySet;
        Iterator<String> keys;

        // Construct the pre-load set of facets we will need
        UIComponent facet1 = new ComponentTestImpl("facet1");
        UIComponent facet2 = new ComponentTestImpl("facet2");
        UIComponent facet3 = new ComponentTestImpl("facet3");
        UIComponent facet4 = new ComponentTestImpl("facet4");
        UIComponent facet5 = new ComponentTestImpl("facet5");
        Map<String, UIComponent> preload = new HashMap<>();
        preload.put("a", facet1);
        preload.put("b", facet2);
        preload.put("c", facet3);
        preload.put("d", facet4);
        preload.put("e", facet5);

        // Test clear()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        assertEquals(5, facets.size());
        assertEquals(5, keySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);
        keySet.clear();
        assertEquals(0, facets.size());
        assertEquals(0, keySet.size());
        checkFacetMissing(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetMissing(component, "e", facet5);

        // Test contains()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        assertTrue(keySet.contains("a"));
        assertTrue(keySet.contains("b"));
        assertTrue(keySet.contains("c"));
        assertTrue(keySet.contains("d"));
        assertTrue(keySet.contains("e"));
        assertTrue(!keySet.contains("f"));

        // Test containsAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        matches = new HashSet<>();
        matches.add("a");
        matches.add("c");
        matches.add("d");
        assertTrue(keySet.containsAll(matches));
        matches = new HashSet<>();
        matches.add("a");
        matches.add("c");
        matches.add("f");
        assertTrue(!keySet.containsAll(matches));

        // Test iterator().hasNext() and iterator().next()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        matches = new HashSet<>();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            matches.add(keys.next());
        }
        assertTrue(keySet.equals(matches));

        // Test iterator().remove()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("b".equals(key) || "d".equals(key)) {
                keys.remove();
            }
        }
        assertEquals(3, facets.size());
        assertEquals(3, keySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test remove()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        keySet.remove("c");
        assertEquals(4, facets.size());
        assertEquals(4, keySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test removeAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        matches = new HashSet<>();
        matches.add("b");
        matches.add("d");
        keySet.removeAll(matches);
        assertEquals(3, facets.size());
        assertEquals(3, keySet.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test retainAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        keySet = facets.keySet();
        matches = new HashSet<>();
        matches.add("b");
        matches.add("d");
        matches.add("f");
        keySet.retainAll(matches);
        assertEquals(2, facets.size());
        assertEquals(2, keySet.size());
        checkFacetMissing(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetMissing(component, "e", facet5);

    }

    // Test Collection returned by getFacets().values()
    @Test
    public void testFacetsMapValues() {

        Map<String, UIComponent> facets;
        Collection<UIComponent> matches;
        Collection<UIComponent> values;
        Iterator<UIComponent> vals;

        // Construct the pre-load set of facets we will need
        UIComponent facet1 = new ComponentTestImpl("facet1");
        UIComponent facet2 = new ComponentTestImpl("facet2");
        UIComponent facet3 = new ComponentTestImpl("facet3");
        UIComponent facet4 = new ComponentTestImpl("facet4");
        UIComponent facet5 = new ComponentTestImpl("facet5");
        UIComponent facet6 = new ComponentTestImpl("facet6"); // Not normally added
        Map<String, UIComponent> preload = new HashMap<>();
        preload.put("a", facet1);
        preload.put("b", facet2);
        preload.put("c", facet3);
        preload.put("d", facet4);
        preload.put("e", facet5);

        // Test add()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        try {
            values.add(new ComponentTestImpl("facet0"));
            fail("Should have thrown UnsupportedOperationExcepton");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // Test addAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        try {
            values.addAll(preload.values());
            fail("Should have thrown UnsupportedOperationExcepton");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // Test clear()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        assertEquals(5, facets.size());
        assertEquals(5, values.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);
        values.clear();
        assertEquals(0, facets.size());
        assertEquals(0, values.size());
        checkFacetMissing(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetMissing(component, "e", facet5);

        // Test contains()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        assertTrue(values.contains(facet1));
        assertTrue(values.contains(facet2));
        assertTrue(values.contains(facet3));
        assertTrue(values.contains(facet4));
        assertTrue(values.contains(facet5));
        assertTrue(!values.contains(facet6));

        // Test containsAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        matches = new ArrayList<>();
        matches.add(facet1);
        matches.add(facet3);
        matches.add(facet4);
        assertTrue(values.containsAll(matches));
        matches = new ArrayList<>();
        matches.add(facet1);
        matches.add(facet3);
        matches.add(facet6);
        assertTrue(!values.containsAll(matches));

        // Test iterator().hasNext() and iterator().next()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        matches = new ArrayList<>();
        vals = values.iterator();
        while (vals.hasNext()) {
            matches.add(vals.next());
        }
        assertTrue(matches.containsAll(values));

        // Test iterator().remove()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        vals = values.iterator();
        while (vals.hasNext()) {
            UIComponent val = vals.next();
            if (facet2.equals(val) || facet4.equals(val)) {
                vals.remove();
            }
        }
        assertEquals(3, facets.size());
        assertEquals(3, values.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test remove()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        values.remove(facet3);
        assertEquals(4, facets.size());
        assertEquals(4, values.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test removeAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        matches = new ArrayList<>();
        matches.add(facet2);
        matches.add(facet4);
        values.removeAll(matches);
        assertEquals(3, facets.size());
        assertEquals(3, values.size());
        checkFacetPresent(component, "a", facet1);
        checkFacetMissing(component, "b", facet2);
        checkFacetPresent(component, "c", facet3);
        checkFacetMissing(component, "d", facet4);
        checkFacetPresent(component, "e", facet5);

        // Test retainAll()
        facets = component.getFacets();
        facets.clear();
        facets.putAll(preload);
        values = facets.values();
        matches = new ArrayList<>();
        matches.add(facet2);
        matches.add(facet4);
        matches.add(facet6);
        values.retainAll(matches);
        assertEquals(2, facets.size());
        assertEquals(2, values.size());
        checkFacetMissing(component, "a", facet1);
        checkFacetPresent(component, "b", facet2);
        checkFacetMissing(component, "c", facet3);
        checkFacetPresent(component, "d", facet4);
        checkFacetMissing(component, "e", facet5);

    }

    // Negative tests on facet methods
    @Test
    public void testFacetsNegative() {

        // Construct components we will need
        UIComponent facet1 = new ComponentTestImpl("facet1");
        UIComponent facet2 = new ComponentTestImpl("facet2");
        UIComponent facet3 = new ComponentTestImpl("facet3");

        // Set up and verify initial conditions
        Map<String, UIComponent> facets = component.getFacets();
        facets.put("facet1", facet1);
        facets.put("facet2", facet2);
        checkFacetCount(component, 2);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetPresent(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);

        // put(Object,Object) - null first argument
        try {
            facets.put(null, facet3);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
        checkFacetCount(component, 2);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetPresent(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);

        // put(Object,Object) - null second argument
        try {
            facets.put("facet3", null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
        checkFacetCount(component, 2);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetPresent(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);

    }

    // Positive tests on facet methods
    @Test
    public void testFacetsPositive() {

        // Construct components we will need
        UIComponent facet1 = new ComponentTestImpl("facet1");
        UIComponent facet2 = new ComponentTestImpl("facet2");
        UIComponent facet3 = new ComponentTestImpl("facet3");
        UIComponent facet4 = new ComponentTestImpl("facet4");
        UIComponent facet5 = new ComponentTestImpl("facet5");

        // Verify initial conditions
        Map<String, UIComponent> facets = component.getFacets();
        checkFacetCount(component, 0);
        checkFacetMissing(component, "facet1", facet1);
        checkFacetMissing(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);
        checkFacetMissing(component, "facet4", facet4);
        checkFacetMissing(component, "facet5", facet5);

        // containsKey(Object) is tested in checkFacetMissing / checkFacetPresent
        // containsValue(Object) is tested in checkFacetMissing / checkFacetPresent
        // entrySet() is tested in testFacetsMapEntrySet()
        // get(Object) is tested in checkFacetMissing / checkFacetPresent
        // isEmpty() is tested in checkFacetCount
        // keySet() is tested in testFacetsMapKeySet()
        // put(Object,Object)
        facets.put("facet1", facet1);
        checkFacetCount(component, 1);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetMissing(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);
        checkFacetMissing(component, "facet4", facet4);
        checkFacetMissing(component, "facet5", facet5);

        // put(Object,Object)
        facets.put("facet4", facet4);
        checkFacetCount(component, 2);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetMissing(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);
        checkFacetPresent(component, "facet4", facet4);
        checkFacetMissing(component, "facet5", facet5);

        // putAll(Map)
        Map<String, UIComponent> map = new HashMap<>();
        map.put("facet2", facet2);
        map.put("facet3", facet3);
        facets.putAll(map);
        checkFacetCount(component, 4);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetPresent(component, "facet2", facet2);
        checkFacetPresent(component, "facet3", facet3);
        checkFacetPresent(component, "facet4", facet4);
        checkFacetMissing(component, "facet5", facet5);

        // remove(Object)
        facets.remove("facet3");
        checkFacetCount(component, 3);
        checkFacetPresent(component, "facet1", facet1);
        checkFacetPresent(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);
        checkFacetPresent(component, "facet4", facet4);
        checkFacetMissing(component, "facet5", facet5);

        // values() is tested in testFacetsMapValues()
        // clear()
        facets.clear();
        checkFacetCount(component, 0);
        checkFacetMissing(component, "facet1", facet1);
        checkFacetMissing(component, "facet2", facet2);
        checkFacetMissing(component, "facet3", facet3);
        checkFacetMissing(component, "facet4", facet4);
        checkFacetMissing(component, "facet5", facet5);

    }

    // Test a pristine UIComponent instance
    @Test
    public void testPristine() {

        // Validate attributes
        checkAttributeCount(component, expectedAttributes.length);
        for (int i = 0; i < expectedAttributes.length; i++) {
            checkAttributePresent(component, expectedAttributes[i], null);
        }

        // Validate properties
        assertEquals(expectedFamily, component.getFamily());
        assertEquals(expectedId, component.getId());
        assertNull(component.getParent());
        assertEquals(expectedRendered, component.isRendered());
        assertEquals(expectedRendererType, component.getRendererType());
        assertEquals(expectedRendersChildren, component.getRendersChildren());

        // Validate children and facets
        checkChildCount(component, 0);
        checkFacetCount(component, 0);
        int n = 0;
        Iterator<?> items = component.getFacetsAndChildren();
        assertNotNull(items);
        while (items.hasNext()) {
            items.next();
            n++;
        }
        assertEquals(0, n);

    }

    // Test setting properties to invalid values
    @Test
    public void testPropertiesInvalid() throws Exception {

        // id - zero length
        try {
            component.setId("");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }

        // id - leading digits
        try {
            component.setId("1abc");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }

        // id - invalid characters 1
        try {
            component.setId("a*c");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }

        // id - invalid characters 2
        try {
            component.setId(" abc");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }

        // id - invalid characters 3
        try {
            component.setId("-abc");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }

    }

    // Test setting properties to valid values
    @Test
    public void testPropertiesValid() throws Exception {

        // id - simple name
        component.setId("foo");
        assertEquals("foo", component.getId());

        // id - complex name
        component.setId("a123-bcd_e");
        assertEquals("a123-bcd_e", component.getId());

        // parent
        UIComponent parent = new ComponentTestImpl("parent");
        component.setParent(parent);
        assertEquals(parent, component.getParent());

        // rendered
        component.setRendered(!expectedRendered);
        assertEquals(!expectedRendered, component.isRendered());

        // rendererType
        component.setRendererType("foo");
        assertEquals("foo", component.getRendererType());

    }

    // --------------------------------------------------------- Support Methods
    // Validate that the specified number of attributes are present.
    protected void checkAttributeCount(UIComponent component, int count) {
        int result = 0;
        Iterator<?> names = component.getAttributes().keySet().iterator();
        while (names.hasNext()) {
            names.next();
            result++;
        }
        assertEquals(count, result);
    }

    // Validate that the specified attribute name is not present
    protected void checkAttributeMissing(UIComponent component, String name) {
        assertNull(component.getAttributes().get(name), "Attribute " + name + " should be missing");
        Iterator<?> keys = component.getAttributes().keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (name.equals(key)) {
                fail("Attribute " + name + " should not be in names list");
            }
        }
    }

    // Validate that the specified attribute name is present with the
    // specified value (if value is not null)
    protected void checkAttributePresent(UIComponent component, String name, Object value) {
        assertNotNull(component.getAttributes().get(name), "attribute " + name + " should be present");
        if (value != null) {
            assertEquals(value, component.getAttributes().get(name));
        }
        Iterator<?> keys = component.getAttributes().keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (name.equals(key)) {
                if (value != null) {
                    assertEquals(value, component.getAttributes().get(name));
                }
                return;
            }
        }
        fail("attribute " + name + " should be in name list");

    }

    // Validate that the specified number of children are present
    protected void checkChildCount(UIComponent component, int count) {
        assertEquals(count, component.getChildCount());
        assertEquals(count, component.getChildren().size());
        assertEquals(count, component.getChildCount());
        if (count == 0) {
            assertTrue(component.getChildren().isEmpty());
        } else {
            assertTrue(!component.getChildren().isEmpty());
        }
    }

    // Validate that the specified child is not present
    protected void checkChildMissing(UIComponent component, UIComponent child) {
        assertNull(child.getParent(), "child " + child + " has no parent");
        List<?> children = component.getChildren();
        assertTrue(!children.contains(child), "child " + child + " should not be contained");
        assertEquals(-1, children.indexOf(child));
        for (int i = 0; i < children.size(); i++) {
            if (child.equals(children.get(i))) {
                fail("child " + child + " should be missing");
            }
        }
    }

    // Validate that the specified child is present at the specified index
    protected void checkChildPresent(UIComponent component, UIComponent child, int index) {
        List<UIComponent> children = component.getChildren();
        assertTrue(children.contains(child), "child " + child + " should be contained");
        assertEquals(index, children.indexOf(child));
        UIComponent kid = children.get(index);
        assertEquals(child, kid);
        assertEquals(component, kid.getParent());
    }

    // Validate that the specified number of facets is present
    protected void checkFacetCount(UIComponent component, int count) {
        assertEquals(count, component.getFacets().size());
        if (count == 0) {
            assertTrue(component.getFacets().isEmpty());
        } else {
            assertTrue(!component.getFacets().isEmpty());
        }
    }

    // Validate that the specified facet is not present
    protected void checkFacetMissing(UIComponent component, String name, UIComponent facet) {
        assertNull(facet.getParent(), "facet " + name + " has no parent");
        Map<String, UIComponent> facets = component.getFacets();
        assertTrue(!facets.containsKey(name), "facet " + name + " key not present");
        assertTrue(!facets.containsValue(facet), "facet " + name + " value not present");
        assertNull(facets.get(name), "facet " + name + " key not found by get");
        assertNull(component.getFacet(name), "facet " + name + " not returned by getFacet(String)");
        Iterator<String> keys = facets.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (name.equals(key)) {
                fail("facet " + name + " found in keys");
            }
        }
        Iterator<UIComponent> values = facets.values().iterator();
        while (values.hasNext()) {
            UIComponent value = values.next();
            if (facet.equals(value)) {
                fail("facet " + name + " found in values");
            }
        }
    }

    // Validate that the specified facet is present
    protected void checkFacetPresent(UIComponent component, String name, UIComponent facet) {

        assertEquals(component, facet.getParent());
        Map<String, UIComponent> facets = component.getFacets();
        assertTrue(facets.containsKey(name), "facet " + name + " key is present");
        assertTrue(facets.containsValue(facet), "facet " + name + " value is present");
        assertEquals(facet, facets.get(name));
        assertTrue(facet == component.getFacet(name), "facet " + name + " returned by getFacet(String)");
        boolean found = false;
        Iterator<String> keys = facets.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (name.equals(key)) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("facet " + name + " not found in keys");
        }
        found = false;
        Iterator<UIComponent> values = facets.values().iterator();
        while (values.hasNext()) {
            UIComponent value = values.next();
            if (facet.equals(value)) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("facet " + name + " not found in values");
        }
    }

    // --------------------------------------------------------- Private Classes
    // Test Implementation of Map.Entry
    private class TestMapEntry implements Map.Entry<String, UIComponent> {

        private TestMapEntry(String key, UIComponent value) {
            this.key = key;
            this.value = value;
        }

        private String key;
        private UIComponent value;

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            if (key == null) {
                if (e.getKey() != null) {
                    return false;
                }
            } else {
                if (!key.equals(e.getKey())) {
                    return false;
                }
            }
            if (value == null) {
                if (e.getValue() != null) {
                    return false;
                }
            } else {
                if (!value.equals(e.getValue())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public UIComponent getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public UIComponent setValue(UIComponent value) {
            UIComponent previous = this.value;
            this.value = value;
            return previous;
        }

    }

    public static class Listener implements ComponentSystemEventListener, Serializable {

        private static final long serialVersionUID = 1L;
        private StringBuilder sb = new StringBuilder();

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
            UIComponent source = (UIComponent) event.getSource();
            Boolean validatorCalled = (Boolean) source.getAttributes().remove("vCalled");
            if (validatorCalled != null) {
                sb.append("*/");
            }
            sb.append(source.getId()).append('/');
        }

        public String getResults() {
            return sb.toString();
        }
    }

    public static class ValidationSignal implements Validator<Object> {

        @Override
        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

            component.getAttributes().put("vCalled", Boolean.TRUE);

        }
    }

}
