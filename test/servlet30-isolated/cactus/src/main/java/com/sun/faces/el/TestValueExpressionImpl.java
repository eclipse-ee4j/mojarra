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

// TestValueExpressionImpl.java
package com.sun.faces.el;

import com.sun.faces.TestBean;
import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.cactus.TestBean.InnerBean;
import com.sun.faces.util.Util;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.cactus.WebRequest;

/**
 * <B>TestValueExpressionImpl </B> is a class ... <p/><B>Lifetime And Scope </B>
 * <P>
 */

public class TestValueExpressionImpl extends ServletFacesTestCase
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
    protected ValueExpression valueExpression;
    
    // Attribute Instance Variables

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //

    public TestValueExpressionImpl()
    {
        super("TestValueExpressionImpl");
    }

    public TestValueExpressionImpl(String name)
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

    protected ValueExpression create(String ref) throws Exception
    {
        return (getFacesContext().getApplication().getExpressionFactory().
            createValueExpression(getFacesContext().getELContext(),("#{" + ref + "}"), Object.class));
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

        valueExpression = this.create("myMap.myBean.one");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertEquals("one", result);

        valueExpression = this.create("myMap[\"myBean\"].one");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue("one".equals(result));

        valueExpression = this.create("myMap.myBean['one']");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue("one".equals(result));

        // Simple tests, verify that bracket and dot operators work
        valueExpression = this.create("TestBean.inner");
        getFacesContext().getExternalContext().getSessionMap().put("TestBean",
                testBean);
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(result == oldInner);

        valueExpression = this.create("TestBean[\"inner\"]");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(result == oldInner);

        valueExpression = this.create("TestBean[\"inner\"].customers[1]");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(result instanceof String);
        assertTrue(result.equals("Jerry"));

        // try out the implicit objects
        valueExpression = this.create("sessionScope.TestBean.inner");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(result == oldInner);

        valueExpression = this.create("header.ELHeader");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(requestsHaveSameAttributeSet(
                (HttpServletRequest) getFacesContext().getExternalContext()
                        .getRequest(), (HttpServletRequest) request));
        assertTrue(request.getHeader("ELHeader").equals("ELHeader"));
        assertTrue(result.equals("ELHeader"));

        valueExpression = this.create("param.ELParam");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(result.equals("ELParam"));

        String multiparam[] = null;
        valueExpression = this.create("paramValues.multiparam");
        multiparam = (String[]) valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(null != multiparam);
        assertTrue(2 == multiparam.length);
        assertTrue(multiparam[0].equals("one"));
        assertTrue(multiparam[1].equals("two"));

        valueExpression = this.create("headerValues.multiheader");
        String[] multiHeader = (String[]) valueExpression
                .getValue(getFacesContext().getELContext());
        assertTrue(null != multiHeader);
        assertTrue(1 == multiHeader.length);
        assertTrue(multiHeader[0].equals("1,2"));

        valueExpression = this.create("initParam.testInitParam");
        result = valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(null != result);
        assertTrue(result.equals("testInitParam"));

        // PENDING(craigmcc) - Comment out this test because on my platform
        // the getRequestCookies() call returns null
        /*
         * valueExpression.setRef("cookie.cookie"); result =
         * valueExpression.getValue(getFacesContext().getELContext()); assertTrue(null != result);
         * assertTrue(result instanceof Cookie); assertTrue(((Cookie)
         * result).getValue().equals("monster"));
         */

    }

    public void beginELSet(WebRequest theRequest)
    {
        populateRequest(theRequest);
    }
    
    public void testNullReference() throws Exception
    {
        boolean exceptionThrown = false;
        // no exception should be thrown as per the EL spec if expression is null.
        try
        {
            getFacesContext().getApplication().getExpressionFactory().
                createValueExpression(getFacesContext().getELContext(),null, Object.class);
        }
        catch (NullPointerException npe) {
            exceptionThrown = false;
        }
        catch (ELException ee) { exceptionThrown= true; };
        assertTrue(exceptionThrown);
    }

    public void testLiterals() throws Exception
    {
        ValueExpression vb = null;
        Object result = null;
        ExternalContext extContext = getFacesContext().getExternalContext();
        
        vb = getFacesContext().getApplication().getExpressionFactory().
            createValueExpression(getFacesContext().getELContext(),"test", Object.class);
        assertEquals("test", vb.getValue(getFacesContext().getELContext()));
        
        assertEquals(String.class, vb.getType(getFacesContext().getELContext()));
        try
        {
            vb.setValue(getFacesContext().getELContext(), "other");
            fail("Literal's setValue(..) should have thrown a EvaluationException");
        }
        catch (javax.el.ELException ee) {}
    }

    public void testReadOnly_singleCase() throws Exception
    {

        // these are mutable Maps
        /*
         * properties on these maps are mutable, but not the object itself....
         * see
         */
        valueExpression = this.create("applicationScope");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("sessionScope");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("requestScope");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("viewScope");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));

        // these are immutable Maps
        valueExpression = this.create("param");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("paramValues");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("header");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("headerValues");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("cookie");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("initParam");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
    }

    public void testReadOnly_multipleCase() throws Exception
    {

        // these are mutable Maps
        valueExpression = this.create("applicationScope.value");
        valueExpression.setValue(getFacesContext().getELContext(), "value");
        String value = (String) valueExpression.getValue(getFacesContext().getELContext());
        assertTrue(!valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("sessionScope.value");
        assertTrue(!valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("requestScope.value");
        assertTrue(!valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("viewScope.value");
        assertTrue(!valueExpression.isReadOnly(getFacesContext().getELContext()));

        // these are immutable Maps
        valueExpression = this.create("param.value");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("paramValues.value");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("header.value");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("headerValues.value");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("cookie.value");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("initParam.value");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext())); 

        // tree
        // create a dummy root for the tree.
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(
                getFacesContext(), null);
        page.setId("root");
        page.setViewId("newTree");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);
        valueExpression = this.create("view.childCount");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));

        com.sun.faces.cactus.TestBean testBean = (com.sun.faces.cactus.TestBean) getFacesContext().getExternalContext()
                .getSessionMap().get("TestBean");
        assertTrue(null != testBean);
        valueExpression = this.create("TestBean.readOnly");
        assertTrue(valueExpression.isReadOnly(getFacesContext().getELContext()));
        valueExpression = this.create("TestBean.one");
        assertTrue(!valueExpression.isReadOnly(getFacesContext().getELContext()));

        InnerBean inner = new InnerBean();
        testBean.setInner(inner);
        valueExpression = this.create("TestBean[\"inner\"].customers[1]");
        assertTrue(!valueExpression.isReadOnly(getFacesContext().getELContext()));

    }

    public void testGetType_singleCase() throws Exception
    {

        // these are mutable Maps
        valueExpression = this.create("applicationScope");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("sessionScope");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("requestScope");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("viewScope");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);

        // these are immutable Maps
        valueExpression = this.create("param");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("paramValues");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("header");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("headerValues");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("cookie");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
        valueExpression = this.create("initParam");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()) == null);
    }

    public void beginGetType_multipleCase(WebRequest theRequest)
    {
        populateRequest(theRequest);
    }

    public void testGetType_multipleCase() throws Exception
    {
        String property = "testValueExpressionImpl_property";
        getFacesContext().getExternalContext().getApplicationMap().put(
                property, property);

        getFacesContext().getExternalContext().getSessionMap().put(property,
                property);

        getFacesContext().getExternalContext().getRequestMap().put(property,
                property);

        getFacesContext().getViewRoot().getViewMap().put(property, property);

        // these are mutable Maps
        valueExpression = this.create("applicationScope." + property);
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
        valueExpression = this.create("sessionScope." + property);
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
        valueExpression = this.create("requestScope." + property);
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
        valueExpression = this.create("viewScope." + property);
        valueExpression.setValue(getFacesContext().getELContext(), property);
        assertTrue(getFacesContext().getViewRoot().getViewMap().containsKey(property));
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));

        // these are immutable Maps
        valueExpression = this.create("param." + "ELParam");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
        valueExpression = this.create("paramValues.multiparam");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));

        valueExpression = this.create("header.ELHeader");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
        valueExpression = this.create("headerValues.multiheader");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
       // assertTrue(java.util.Enumeration.class.isAssignableFrom(valueExpression
       //         .getType(getFacesContext().getELContext())));
        // PENDING(craigmcc) - Comment out this test because on my platform
        // the getRequestCookies() call returns null
        /*
         * valueExpression = this.create("cookie.cookie");
         * assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals("javax.servlet.http.Cookie"));
         */
        valueExpression = this
                .create("initParam['javax.faces.STATE_SAVING_METHOD']");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));   

        // tree
        // create a dummy root for the tree.
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(
                getFacesContext(), null);
        page.setId("root");
        page.setViewId("newTree");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);
        valueExpression = this.create("view");
        Class c = valueExpression.getType(getFacesContext().getELContext());
        assertTrue(c == null);

        com.sun.faces.cactus.TestBean testBean = (com.sun.faces.cactus.TestBean) getFacesContext().getExternalContext()
                .getSessionMap().get("TestBean");
        assertTrue(null != testBean);
        valueExpression = this.create("TestBean.readOnly");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.String"));
        valueExpression = this.create("TestBean.one");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.String"));

        InnerBean inner = new InnerBean();
        testBean.setInner(inner);
        valueExpression = this.create("TestBean[\"inner\"].customers[1]");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));

        valueExpression = this.create("TestBean[\"inner\"]");
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "com.sun.faces.cactus.TestBean$InnerBean"));

        int[] intArray =
        { 1, 2, 3 };
        getFacesContext().getExternalContext().getRequestMap().put("intArray",
                intArray);
        valueExpression = this.create("requestScope.intArray");
       
        assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
                "java.lang.Object"));
       // assertTrue(valueExpression.getType(getFacesContext().getELContext()).getName().equals(
       //         "[I"));
    }

    public void testGetScopePositive() throws Exception
    {
        TestBean testBean = new TestBean();
        getFacesContext().getExternalContext().getApplicationMap().put(
                "TestApplicationBean", testBean);

        valueExpression = this.create("TestApplicationBean");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope("TestApplicationBean", null));

        valueExpression = this.create("TestApplicationBean.one");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope("TestApplicationBean.one",
                null));

        valueExpression = this.create("TestApplicationBean.inner.two");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope(
                "TestApplicationBean.inner.two", null));

        valueExpression = this.create("applicationScope.TestApplicationBean");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope(
                "applicationScope.TestApplicationBean", null));
        valueExpression = this
                .create("applicationScope.TestApplicationBean.inner.two");
        assertEquals(ELUtils.Scope.APPLICATION, ELUtils.getScope(
                "applicationScope.TestApplicationBean.inner.two", null));

        getFacesContext().getExternalContext().getSessionMap().put(
                "TestSessionBean", testBean);
        valueExpression = this.create("TestSessionBean");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope("TestSessionBean", null));

        valueExpression = this.create("TestSessionBean.one");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope("TestSessionBean.one", null));

        valueExpression = this.create("TestSessionBean.inner.two");
        assertEquals(ELUtils.Scope.SESSION, ELUtils
             .getScope("TestSessionBean.inner.two", null));

        valueExpression = this.create("sessionScope.TestSessionBean");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope("sessionScope.TestSessionBean",
                null));

        valueExpression = this.create("sessionScope.TestSessionBean.inner.two");
        assertEquals(ELUtils.Scope.SESSION, ELUtils.getScope(
                "sessionScope.TestSessionBean.inner.two", null));

        getFacesContext().getExternalContext().getRequestMap().put(
                "TestRequestBean", testBean);
        valueExpression = this.create("TestRequestBean");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope("TestRequestBean", null));

        valueExpression = this.create("TestRequestBean.one");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope("TestRequestBean.one", null));

        valueExpression = this.create("TestRequestBean.inner.two");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils
             .getScope("TestRequestBean.inner.two", null));

        valueExpression = this.create("requestScope.TestRequestBean");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope("requestScope.TestRequestBean",
                null));

        valueExpression = this.create("requestScope.TestRequestBean.inner.two");
        assertEquals(ELUtils.Scope.REQUEST, ELUtils.getScope(
                "requestScope.TestRequestBean.inner.two", null));

        assertEquals(ELUtils.Scope.VIEW, ELUtils.getScope("viewScope.foo", null));

        valueExpression = this.create("TestNoneBean");
        assertNull(ELUtils.getScope("TestNoneBean", null));

        valueExpression = this.create("TestNoneBean.one");
        assertNull(ELUtils.getScope("TestNoneBean.one", null));
        valueExpression = this.create("TestNoneBean.inner.two");
        assertNull(ELUtils.getScope("TestNoneBean.inner.two", null));

    }

    public void testGetScopeNegative() throws Exception {
        ValueExpression valueExpression = null;
        String property = null;
        /*
        property = "[]";
        valueExpression = this.factory.createValueExpression(property);
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
            valueExpression = this.create("applicationScope");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("sessionScope");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("requestScope");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("facesContext");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("cookie");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("header");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("headerValues");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("initParam");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("param");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("paramValues");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try
        {
            valueExpression = this.create("view");
            valueExpression.setValue(getFacesContext().getELContext(), "value");
        }
        catch (ELException ee)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testInvalidExpression() throws Exception
    {

//        boolean exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (ELException e)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("!");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("..");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create(".foo");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("()");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("[]");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("applicationScope}");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(!exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("applicationScope >= sessionScope");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        exceptionThrown = false;
//        try
//        {
//            valueExpression = this.create("foo applicationScope");
//            valueExpression.getValue(getFacesContext().getELContext());
//        }
//        catch (PropertyNotFoundException e)
//        {
//            exceptionThrown = true;
//        }
//        catch (ELException ee)
//        {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);

    }

   /* public void testStateHolderSmall() throws Exception
    {
        StateHolderSaver saver = null;
        ValueExpression binding = getFacesContext().getApplication().getExpressionFactory().
                createValueExpression("#{TestBean.indexProperties[0]}", Object.class, null);

        assertEquals("ValueExpression not expected value", "Justyna",
                (String) binding.getValue(getFacesContext().getELContext()));
        saver = new StateHolderSaver(getFacesContext(), binding);
        binding = null;
        binding = (ValueExpression) saver.restore(getFacesContext());
        assertEquals("ValueExpression not expected value", "Justyna",
                (String) binding.getValue(getFacesContext().getELContext()));
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
        root.getChildren().add(form = new UIForm());
        form.getChildren().add(input = new UIInput());
        input.setValueExpression("buckaroo", (getFacesContext().getApplication().getExpressionFactory().
                createValueExpression("#{TestBean.indexProperties[0]}", Object.class, null)));
        state = root.processSaveState(getFacesContext());

        // synthesize the tree structure
        getFacesContext().setViewRoot(
                root = Util.getViewHandler(getFacesContext()).createView(
                        getFacesContext(), null));
        root.getChildren().add(form = new UIForm());
        form.getChildren().add(input = new UIInput());
        root.processRestoreState(getFacesContext(), state);

        assertEquals("ValueExpression not expected value", "Justyna",
                (String) input.createValueExpression("buckaroo").getValue(
                        getFacesContext().getELContext()));

    } */

    public void testGetExpressionString() throws Exception
    {
        Application app = (Application) getFacesContext()
                .getApplication();
        String ref = null;
        ValueExpression vb = null;

        ref = "#{NewCustomerFormHandler.minimumAge}";
        vb = app.getExpressionFactory().createValueExpression(getFacesContext().getELContext(),ref, Object.class);
        assertEquals(ref, vb.getExpressionString());

        ref = "minimum age is #{NewCustomerFormHandler.minimumAge}";
        vb = app.getExpressionFactory().createValueExpression(getFacesContext().getELContext(),ref, Object.class);
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
                System.out.println("ClassNotFound Exception");
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
                    System.out.println("Instantiation Exception");
                    e.printStackTrace();
                    throw new IllegalStateException(e.getMessage());
                }
                catch (IllegalAccessException a)
                {
                    System.out.println("IleegalAccess Exception");
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

} // end of class TestValueExpressionImpl
