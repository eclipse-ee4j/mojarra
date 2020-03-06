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

// TestValueBindingImpl.java
package com.sun.faces.el;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.TestBean;
import com.sun.faces.cactus.TestBean.InnerBean;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.FacesException;
import javax.faces.component.StateHolder;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import javax.faces.application.Application;

/**
 * <B>TestValueBindingImpl </B> is a class ... <p/><B>Lifetime And Scope </B>
 * <P>
 */

public class TestValueBindingImpl extends ServletFacesTestCase
{

    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //
    protected ValueBinding valueBinding;

 //   protected ValueBindingFactory factory = new ValueBindingFactory();

    // Attribute Instance Variables

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //

    public TestValueBindingImpl()
    {
        super("TestValueBindingImpl");
    }

    public TestValueBindingImpl(String name)
    {
        super(name);
    }

    //
    // Class methods
    //

    //
    // Methods from TestCase
    //

    //
    // General Methods
    //

    protected ValueBinding create(String ref) throws Exception
    {
        return (getFacesContext().getApplication().createValueBinding("#{" + ref + "}"));
    }

    public void populateRequest(WebRequest theRequest)
    {
        theRequest.addHeader("ELHeader", "ELHeader");
        theRequest.addHeader("multiheader", "1");
        theRequest.addHeader("multiheader", "2");
        theRequest.addParameter("ELParam", "ELParam");
        theRequest.addParameter("multiparam", "one");
        theRequest.addParameter("multiparam", "two");
        theRequest.addCookie("cookie", "monster");
    }

    public void beginELGet(WebRequest theRequest)
    {
        populateRequest(theRequest);
    }

    public void testELGet() throws Exception
    {
        TestBean testBean = new TestBean();
        InnerBean newInner, oldInner = new InnerBean();
        testBean.setInner(oldInner);
        Object result = null;
        ExternalContext extContext = getFacesContext().getExternalContext();

        Map myMap = new HashMap();
        TestBean myBean = new TestBean();
        myBean.setOne("one");
        myMap.put("myBean", myBean);
        extContext.getRequestMap().put("myMap", myMap);

        //
        // Get tests
        //

        valueBinding = this.create("myMap.myBean.one");
        result = valueBinding.getValue(getFacesContext());
        assertEquals("one", result);

        valueBinding = this.create("myMap[\"myBean\"].one");
        result = valueBinding.getValue(getFacesContext());
        assertTrue("one".equals(result));

        valueBinding = this.create("myMap.myBean['one']");
        result = valueBinding.getValue(getFacesContext());
        assertTrue("one".equals(result));

        // Simple tests, verify that bracket and dot operators work
        valueBinding = this.create("TestBean.inner");
        getFacesContext().getExternalContext().getSessionMap().put("TestBean",
                testBean);
        result = valueBinding.getValue(getFacesContext());
        assertTrue(result == oldInner);

        valueBinding = this.create("TestBean[\"inner\"]");
        result = valueBinding.getValue(getFacesContext());
        assertTrue(result == oldInner);

        valueBinding = this.create("TestBean[\"inner\"].customers[1]");
        result = valueBinding.getValue(getFacesContext());
        assertTrue(result instanceof String);
        assertTrue(result.equals("Jerry"));

        // try out the implicit objects
        valueBinding = this.create("sessionScope.TestBean.inner");
        result = valueBinding.getValue(getFacesContext());
        assertTrue(result == oldInner);

        valueBinding = this.create("header.ELHeader");
        result = valueBinding.getValue(getFacesContext());
        assertTrue(requestsHaveSameAttributeSet(
                (HttpServletRequest) getFacesContext().getExternalContext()
                        .getRequest(), (HttpServletRequest) request));
        assertTrue(request.getHeader("ELHeader").equals("ELHeader"));
        assertTrue(result.equals("ELHeader"));

        valueBinding = this.create("param.ELParam");
        result = valueBinding.getValue(getFacesContext());
        assertTrue(result.equals("ELParam"));

        String multiparam[] = null;
        valueBinding = this.create("paramValues.multiparam");
        multiparam = (String[]) valueBinding.getValue(getFacesContext());
        assertTrue(null != multiparam);
        assertTrue(2 == multiparam.length);
        assertTrue(multiparam[0].equals("one"));
        assertTrue(multiparam[1].equals("two"));

        valueBinding = this.create("headerValues.multiheader");
        String[] multiHeader = (String[]) valueBinding
                .getValue(getFacesContext());
        assertTrue(null != multiHeader);
        assertTrue(1 == multiHeader.length);
        assertTrue(multiHeader[0].equals("1,2"));

        valueBinding = this.create("initParam.testInitParam");
        result = valueBinding.getValue(getFacesContext());
        assertTrue(null != result);
        assertTrue(result.equals("testInitParam"));

        // PENDING(craigmcc) - Comment out this test because on my platform
        // the getRequestCookies() call returns null
        /*
         * valueBinding.setRef("cookie.cookie"); result =
         * valueBinding.getValue(getFacesContext()); assertTrue(null != result);
         * assertTrue(result instanceof Cookie); assertTrue(((Cookie)
         * result).getValue().equals("monster"));
         */

    }

    public void beginELSet(WebRequest theRequest)
    {
        populateRequest(theRequest);
    }

    public void testELSet() throws Exception
    {
        // migrated to test-agnostic-el.
    }
    
    public void testNullReference() throws Exception
    {
        try
        {
            getFacesContext().getApplication().createValueBinding(null);
            fail();
        }
        catch (NullPointerException npe) {}
        catch (Exception e) { fail("Should have thrown an NPE"); };
    }

    public void testLiterals() throws Exception
    {
        ValueBinding vb = null;
        Object result = null;
        ExternalContext extContext = getFacesContext().getExternalContext();
        
        vb = getFacesContext().getApplication().createValueBinding("test");
        assertEquals("test", vb.getValue(getFacesContext()));
        
        assertEquals(String.class, vb.getType(getFacesContext()));
        try
        {
            vb.setValue(getFacesContext(), "other");
            fail("Literal's setValue(..) should have thrown a EvaluationException");
        }
        catch (javax.faces.el.EvaluationException ee) {}
    }

    public void testReadOnly_singleCase() throws Exception
    {

        // these are mutable Maps
        /*
         * properties on these maps are mutable, but not the object itself....
         * see
         */
        valueBinding = this.create("applicationScope");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("sessionScope");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("requestScope");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));

        // these are immutable Maps
        valueBinding = this.create("param");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("paramValues");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("header");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("headerValues");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("cookie");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("initParam");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
    }

    public void testReadOnly_multipleCase() throws Exception
    {

        // these are mutable Maps
        valueBinding = this.create("applicationScope.value");
        valueBinding.setValue(getFacesContext(), "value");
        String value = (String) valueBinding.getValue(getFacesContext());
        assertTrue(!valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("sessionScope.value");
        assertTrue(!valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("requestScope.value");
        assertTrue(!valueBinding.isReadOnly(getFacesContext()));

        // these are immutable Maps
        valueBinding = this.create("param.value");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("paramValues.value");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("header.value");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("headerValues.value");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("cookie.value");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("initParam.value");
        assertTrue(valueBinding.isReadOnly(getFacesContext())); 

        // tree
        // create a dummy root for the tree.
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(
                getFacesContext(), null);
        page.setId("root");
        page.setViewId("newTree");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);
        valueBinding = this.create("view.childCount");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));

        com.sun.faces.cactus.TestBean testBean = (com.sun.faces.cactus.TestBean) getFacesContext().getExternalContext()
                .getSessionMap().get("TestBean");
        assertTrue(null != testBean);
        valueBinding = this.create("TestBean.readOnly");
        assertTrue(valueBinding.isReadOnly(getFacesContext()));
        valueBinding = this.create("TestBean.one");
        assertTrue(!valueBinding.isReadOnly(getFacesContext()));

        InnerBean inner = new InnerBean();
        testBean.setInner(inner);
        valueBinding = this.create("TestBean[\"inner\"].customers[1]");
        assertTrue(!valueBinding.isReadOnly(getFacesContext()));

    }

    public void testGetType_singleCase() throws Exception
    {

        // these are mutable Maps
        valueBinding = this.create("applicationScope");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("sessionScope");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("requestScope");
        assertTrue(valueBinding.getType(getFacesContext()) == null);

        // these are immutable Maps
        valueBinding = this.create("param");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("paramValues");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("header");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("headerValues");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("cookie");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
        valueBinding = this.create("initParam");
        assertTrue(valueBinding.getType(getFacesContext()) == null);
    }

    public void beginGetType_multipleCase(WebRequest theRequest)
    {
        populateRequest(theRequest);
    }

    public void testGetType_multipleCase() throws Exception
    {
        String property = "testvalueBindingImpl_property";
        getFacesContext().getExternalContext().getApplicationMap().put(
                property, property);

        getFacesContext().getExternalContext().getSessionMap().put(property,
                property);

        getFacesContext().getExternalContext().getRequestMap().put(property,
                property);

        // these are mutable Maps
        valueBinding = this.create("applicationScope." + property);
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));
        valueBinding = this.create("sessionScope." + property);
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));
        valueBinding = this.create("requestScope." + property);
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));

        // these are immutable Maps
        valueBinding = this.create("param." + "ELParam");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));
        valueBinding = this.create("paramValues.multiparam");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));

        valueBinding = this.create("header.ELHeader");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));
        valueBinding = this.create("headerValues.multiheader");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));
       // assertTrue(java.util.Enumeration.class.isAssignableFrom(valueBinding
       //         .getType(getFacesContext())));
        // PENDING(craigmcc) - Comment out this test because on my platform
        // the getRequestCookies() call returns null
        /*
         * valueBinding = this.create("cookie.cookie");
         * assertTrue(valueBinding.getType(getFacesContext()).getName().equals("javax.servlet.http.Cookie"));
         */
        valueBinding = this
                .create("initParam['jakarta.faces.STATE_SAVING_METHOD']");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));   

        // tree
        // create a dummy root for the tree.
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(
                getFacesContext(), null);
        page.setId("root");
        page.setViewId("newTree");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);
        valueBinding = this.create("view");
        Class c = valueBinding.getType(getFacesContext());
        assertTrue(c == null);

        com.sun.faces.cactus.TestBean testBean = (com.sun.faces.cactus.TestBean) getFacesContext().getExternalContext()
                .getSessionMap().get("TestBean");
        assertTrue(null != testBean);
        valueBinding = this.create("TestBean.readOnly");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.String"));
        valueBinding = this.create("TestBean.one");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.String"));

        InnerBean inner = new InnerBean();
        testBean.setInner(inner);
        valueBinding = this.create("TestBean[\"inner\"].customers[1]");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));

        valueBinding = this.create("TestBean[\"inner\"]");
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "com.sun.faces.cactus.TestBean$InnerBean"));

        int[] intArray =
        { 1, 2, 3 };
        getFacesContext().getExternalContext().getRequestMap().put("intArray",
                intArray);
        valueBinding = this.create("requestScope.intArray");
       
        assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
                "java.lang.Object"));
       // assertTrue(valueBinding.getType(getFacesContext()).getName().equals(
       //         "[I"));
    }

    public void testGetScopePositive() throws Exception
    {
        TestBean testBean = new TestBean();
        getFacesContext().getExternalContext().getApplicationMap().put(
                "TestApplicationBean", testBean);

        valueBinding = this.create("TestApplicationBean");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope("TestApplicationBean", null));

        valueBinding = this.create("TestApplicationBean.one");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope("TestApplicationBean.one",
                null));

        valueBinding = this.create("TestApplicationBean.inner.two");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope(
                "TestApplicationBean.inner.two", null));

        valueBinding = this.create("applicationScope.TestApplicationBean");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope(
                "applicationScope.TestApplicationBean", null));
        valueBinding = this
                .create("applicationScope.TestApplicationBean.inner.two");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope(
                "applicationScope.TestApplicationBean.inner.two", null));

        getFacesContext().getExternalContext().getSessionMap().put(
                "TestSessionBean", testBean);
        valueBinding = this.create("TestSessionBean");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope("TestSessionBean", null));

        valueBinding = this.create("TestSessionBean.one");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope("TestSessionBean.one", null));

        valueBinding = this.create("TestSessionBean.inner.two");
        assertEquals(ELUtils.Scope.SESSION, ELUtils
             .getScope("TestSessionBean.inner.two", null));

        valueBinding = this.create("sessionScope.TestSessionBean");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope("sessionScope.TestSessionBean",
                null));

        valueBinding = this.create("sessionScope.TestSessionBean.inner.two");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope(
                "sessionScope.TestSessionBean.inner.two", null));

        getFacesContext().getExternalContext().getRequestMap().put(
                "TestRequestBean", testBean);
        valueBinding = this.create("TestRequestBean");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope("TestRequestBean", null));

        valueBinding = this.create("TestRequestBean.one");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope("TestRequestBean.one", null));

        valueBinding = this.create("TestRequestBean.inner.two");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils
             .getScope("TestRequestBean.inner.two", null));

        valueBinding = this.create("requestScope.TestRequestBean");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope("requestScope.TestRequestBean",
                null));

        valueBinding = this.create("requestScope.TestRequestBean.inner.two");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope(
                "requestScope.TestRequestBean.inner.two", null));

        valueBinding = this.create("TestNoneBean");
        assertEquals(null, ELUtils.getScope("TestNoneBean", null));

        valueBinding = this.create("TestNoneBean.one");
        assertEquals(null, ELUtils.getScope("TestNoneBean.one", null));
        valueBinding = this.create("TestNoneBean.inner.two");
        assertEquals(null, ELUtils.getScope("TestNoneBean.inner.two", null));

    }

    public void testGetScopeNegative() throws Exception {
        ValueBinding valueBinding = null;
        String property = null;
        /*
        property = "[]";
        valueBinding = this.factory.createValueBinding(property);
        assertNull(Util.getScope(property, null));
        property = "][";
        assertNull(Util.getScope(property, null));
        property = "";
        assertNull(Util.getScope(property, null));
        property = null;
        assertNull(Util.getScope(property, null));
        property = "foo.sessionScope";
        assertNull(Util.getScope(property, null));        
        */

    }

    // negative test for case when valueRef is merely
    // one of the reserved scope names.
    public void testReservedScopeIdentifiers() throws Exception
    {
        boolean exceptionThrown = false;

        try
        {
            valueBinding = this.create("applicationScope");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("sessionScope");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("requestScope");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("facesContext");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("cookie");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("header");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("headerValues");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("initParam");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("param");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("paramValues");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("view");
            valueBinding.setValue(getFacesContext(), "value");
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testInvalidExpression() throws Exception
    {

        boolean exceptionThrown = false;
        try
        {
            valueBinding = this.create("");
            valueBinding.getValue(getFacesContext());
        }
        catch (FacesException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("!");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("..");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create(".foo");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("()");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("applicationScope}");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(!exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("applicationScope >= sessionScope");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueBinding = this.create("foo applicationScope");
            valueBinding.getValue(getFacesContext());
        }
        catch (PropertyNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }

    public void testStateHolderSmall() throws Exception
    {
        StateHolderSaver saver = null;
        ValueBinding binding = getFacesContext().getApplication()
                .createValueBinding("#{TestBean.indexProperties[0]}");

        assertEquals("ValueBinding not expected value", "Justyna",
                (String) binding.getValue(getFacesContext()));
        saver = new StateHolderSaver(getFacesContext(), binding);
        binding = null;
        binding = (ValueBinding) saver.restore(getFacesContext());
        assertEquals("ValueBinding not expected value", "Justyna",
                (String) binding.getValue(getFacesContext()));
    }

    public void testStateHolderMedium() throws Exception
    {
        UIViewRoot root = null;
        UIForm form = null;
        UIInput input = null;
        Object state = null;
        getFacesContext().setViewRoot(
                root = Util.getViewHandler(getFacesContext()).createView(
                        getFacesContext(), null));
        getFacesContext().getViewRoot().setLocale(Locale.US);
        root.getChildren().add(form = new UIForm());
        form.getChildren().add(input = new UIInput());
        input.setValueBinding("buckaroo", getFacesContext().getApplication()
                .createValueBinding("#{TestBean.indexProperties[0]}"));
        state = root.processSaveState(getFacesContext());

        // synthesize the tree structure
        getFacesContext().setViewRoot(
                root = Util.getViewHandler(getFacesContext()).createView(
                        getFacesContext(), null));
        getFacesContext().getViewRoot().setLocale(Locale.US);
        root.getChildren().add(form = new UIForm());
        form.getChildren().add(input = new UIInput());
        root.processRestoreState(getFacesContext(), state);

        assertEquals("ValueBinding not expected value", "Justyna",
                (String) input.getValueBinding("buckaroo").getValue(
                        getFacesContext()));

    }

    public void testGetExpressionString() throws Exception
    {
        Application app = (Application) getFacesContext()
                .getApplication();
        String ref = null;
        ValueBinding vb = null;

        ref = "#{NewCustomerFormHandler.minimumAge}";
        vb = app.createValueBinding(ref);
        assertEquals(ref, vb.getExpressionString());

        ref = "minimum age is #{NewCustomerFormHandler.minimumAge}";
        vb = app.createValueBinding(ref);
        assertEquals(ref, vb.getExpressionString());
    }

    class StateHolderSaver extends Object implements Serializable
    {

        protected String className = null;

        protected Object savedState = null;

        public StateHolderSaver(FacesContext context, Object toSave)
        {
            className = toSave.getClass().getName();

            if (toSave instanceof StateHolder)
            {
                // do not save an attached object that is marked transient.
                if (!((StateHolder) toSave).isTransient())
                {
                    savedState = ((StateHolder) toSave).saveState(context);
                }
                else
                {
                    className = null;
                }
            }
        }

        /**
         * @return the restored {@link StateHolder}instance.
         */

        public Object restore(FacesContext context)
                throws IllegalStateException
        {
            Object result = null;
            Class toRestoreClass = null;
            if (className == null)
            {
                return null;
            }

            try
            {
                toRestoreClass = loadClass(className, this);
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalStateException(e.getMessage());
            }

            if (null != toRestoreClass)
            {
                try
                {
                    result = toRestoreClass.newInstance();
                }
                catch (InstantiationException e)
                {
                    throw new IllegalStateException(e.getMessage());
                }
                catch (IllegalAccessException a)
                {
                    throw new IllegalStateException(a.getMessage());
                }
            }

            if (null != result && null != savedState
                    && result instanceof StateHolder)
            {
                // don't need to check transient, since that was done on
                // the saving side.
                ((StateHolder) result).restoreState(context, savedState);
            }
            return result;
        }

        private Class loadClass(String name, Object fallbackClass)
                throws ClassNotFoundException
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null)
            {
                loader = fallbackClass.getClass().getClassLoader();
            }
            return loader.loadClass(name);
        }
    }

} // end of class TestValueBindingImpl
