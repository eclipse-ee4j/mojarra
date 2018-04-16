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

// TestPropertyResolverImpl.java

package com.sun.faces.el;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.TestBean;
import com.sun.faces.util.Util;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;


/**
 * <B>TestPropertyResolverImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestPropertyResolverImpl extends ServletFacesTestCase {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

    private ElBean bean = null;
    private PropertyResolver resolver = null;

//
// Constructors and Initializers    
//

    public TestPropertyResolverImpl() {
        super("TestFacesContext");
    }


    public TestPropertyResolverImpl(String name) {
        super(name);
    }
//
// Class methods
//

//
// Methods from TestCase
//

    public void setUp() {
        super.setUp();
        bean = new ElBean();
        resolver = getFacesContext().getApplication().getPropertyResolver();
    }


    public void tearDown() {
        resolver = null;
        bean = null;
        super.tearDown();
    }

//
// General Methods
//

    // Negative getValue() tests on a JavaBean base object
    public void testNegative() throws Exception {

        Object value = null;


        // ---------- Should Return Null ----------
        value = resolver.getValue(bean, null);
        assertNull(value);

        value = resolver.getValue(null, "booleanProperty");
        assertNull(value);

        boolean exceptionThrown = false;
        try {
            value = resolver.getValue(null, null);
        } catch (javax.faces.el.EvaluationException ee) {
            exceptionThrown = true;  
        }
        exceptionThrown = false;
        
        try {
            value = resolver.getValue(bean.getIntArray(), -1);
        } catch (javax.faces.el.EvaluationException ee) {
            exceptionThrown = true;  
        }
        exceptionThrown = false;
        
        try {
            value = resolver.getValue(bean.getIntArray(), 3);
        } catch (javax.faces.el.EvaluationException ee) {
            exceptionThrown = true;  
        }
        exceptionThrown = false;
        
        try {
            value = resolver.getValue(bean.getIntList(), -1);
        } catch (javax.faces.el.EvaluationException ee) {
            exceptionThrown = true;  
        }
        exceptionThrown = false;
        
        try {
           value = resolver.getValue(bean.getIntList(), 5);
        } catch (javax.faces.el.EvaluationException ee) {
            exceptionThrown = true;  
        }
        exceptionThrown = false;
            
        // ---------- Should throw EvaluationException

        try {
            value = resolver.getValue(bean, "nullStringProperty");
            fail("Should have thrown EvaluationException");
        } catch (EvaluationException e) {
            ; // Expected result
        }

        // ---------- Should Throw PropertyNotFoundException

        try {
            value = resolver.getValue(bean, "dontExist");
            fail("Should have thrown EvaluationException");
        } catch (EvaluationException e) {
            ; // Expected result
        }

    }

    public void testPristine() {

        // PENDING - test pristine condition of a new instance

    }


    // -------------------------------------------------- Indexed Variant Tests


    // Positive getValue() tests for the indexed variant against an array
    public void testIndexedGetArray() throws Exception {

        Object value = null;
        int intArray[] = bean.getIntArray();
        assertEquals(3, intArray.length);
        
        value = resolver.getValue(intArray, -1);
        assertNull(value);
        
        value = resolver.getValue(null, 0);
        assertNull(value);

        value = resolver.getValue(intArray, 0);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(1, ((Integer) value).intValue());

        value = resolver.getValue(intArray, 1);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(2, ((Integer) value).intValue());

        value = resolver.getValue(intArray, 2);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(3, ((Integer) value).intValue());
    }


    // Positive getValue() tests for the indexed variant against a List
    public void testIndexedGetList() throws Exception {

        Object value = null;
        List intList = bean.getIntList();
        assertEquals(5, intList.size());
        
        value = resolver.getValue(intList, -1);
        assertNull(value);
        
        value = resolver.getValue(null, 0);
        assertNull(value);

        value = resolver.getValue(intList, 0);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(10, ((Integer) value).intValue());

        value = resolver.getValue(intList, 1);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(20, ((Integer) value).intValue());

        value = resolver.getValue(intList, 2);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(30, ((Integer) value).intValue());

        value = resolver.getValue(intList, 3);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(40, ((Integer) value).intValue());

        value = resolver.getValue(intList, 4);
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals(50, ((Integer) value).intValue());

    }


    // Positive setValue() tests for the indexed variant against an array
    public void testIndexedSetArray() throws Exception {

        Object value = new Integer(300);
        int intArray[] = bean.getIntArray();
        assertEquals(3, intArray.length);
        
        resolver.setValue(intArray, 0, value);
        assertEquals(300, intArray[0]);
        
        Object[] objArray = new Object[] {"val"};
        resolver.setValue(objArray, 0, null);
        assertNull(objArray[0]);
        
        try
        {
            resolver.setValue(null, 0, value);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.setValue(intArray, -1, value);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.setValue(intArray, 100, value);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
    }


    // Positive setValue() tests for the indexed variant against a List
    public void testIndexedSetList() throws Exception {

        Object value = new Object();
        List intList = bean.getIntList();
        assertEquals(5, intList.size());
        
        resolver.setValue(intList, 0, value);
        assertNotNull(intList.get(0));
        
        resolver.setValue(intList, 0, null);
        assertNull(intList.get(0));
        
        try
        {
            resolver.setValue(null, 0, value);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.setValue(intList, -1, value);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.setValue(intList, 100, value);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}

    }


    // Positive getType() tests for the indexed variant against an array
    public void testIndexedTypeArray() throws Exception {

        Class type = null;
        int intArray[] = bean.getIntArray();
        assertEquals(3, intArray.length);
        
        type = resolver.getType(intArray, 0);
        assertEquals(Integer.TYPE, type);
        
        try
        {
            resolver.getType(null, 0);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.getType(intArray, -1);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            // do we really need to check this?
            resolver.getType(intArray, 100);
            //fail();
        }
        catch (PropertyNotFoundException pnfe) {}
    }


    // Positive getType() tests for the indexed variant against a List
    public void testIndexedTypeList() throws Exception {

        Class type = null;
        List intList = bean.getIntList();
        assertEquals(5, intList.size());
        
        type = resolver.getType(intList, 0);
        assertEquals(Integer.class, type);
        
        try
        {
            resolver.getType(null, 0);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.getType(intList, -1);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
        
        try
        {
            resolver.getType(intList, 100);
            fail();
        }
        catch (PropertyNotFoundException pnfe) {}
    }


    // --------------------------------------------------- String Variant Tests


    // Postitive getValue() tests on a JavaBean base object
    public void testStringGetBean() throws Exception {

        Object value = null;

        value = resolver.getValue(bean, "booleanProperty");
        assertNotNull(value);
        assertTrue(value instanceof Boolean);
        assertEquals(true, ((Boolean) value).booleanValue());

        value = resolver.getValue(bean, "byteProperty");
        assertNotNull(value);
        assertTrue(value instanceof Byte);
        assertEquals((byte) 123, ((Byte) value).byteValue());

        value = resolver.getValue(bean, "doubleProperty");
        assertNotNull(value);
        assertTrue(value instanceof Double);
        assertEquals((double) 654.321, ((Double) value).doubleValue(), 0.005);

        value = resolver.getValue(bean, "floatProperty");
        assertNotNull(value);
        assertTrue(value instanceof Float);
        assertEquals((float) 123.45, ((Float) value).floatValue(), 0.5);

        value = resolver.getValue(bean, "intProperty");
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals((int) 1234, ((Integer) value).intValue());

        value = resolver.getValue(bean, "longProperty");
        assertNotNull(value);
        assertTrue(value instanceof Long);
        assertEquals((long) 54321, ((Long) value).longValue());

        value = resolver.getValue(bean, "nestedProperty");
        assertNotNull(value);
        assertTrue(value instanceof ElBean);
        assertEquals("This is a String", ((ElBean) value).getStringProperty());

        value = resolver.getValue(bean, "shortProperty");
        assertNotNull(value);
        assertTrue(value instanceof Short);
        assertEquals((short) 321, ((Short) value).shortValue());

        value = resolver.getValue(bean, "stringProperty");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("This is a String", (String) value);

    }


    // Positive getValue() tests on a Map base object
    public void testStringGetMap() throws Exception {

        getFacesContext().getExternalContext().getRequestMap().put("testValue",
                                                                   this);
        assertTrue(this == resolver.getValue(
            getFacesContext().getExternalContext().getRequestMap(),
            "testValue"));

    }


    // Postitive setValue() tests on a JavaBean base object
    public void testStringSetBean() throws Exception {

        Object value = null;

        resolver.setValue(bean, "booleanProperty", Boolean.FALSE);
        value = resolver.getValue(bean, "booleanProperty");
        assertNotNull(value);
        assertTrue(value instanceof Boolean);
        assertEquals(false, ((Boolean) value).booleanValue());

        resolver.setValue(bean, "byteProperty", new Byte((byte) 124));
        value = resolver.getValue(bean, "byteProperty");
        assertNotNull(value);
        assertTrue(value instanceof Byte);
        assertEquals((byte) 124, ((Byte) value).byteValue());

        resolver.setValue(bean, "doubleProperty", new Double(333.333));
        value = resolver.getValue(bean, "doubleProperty");
        assertNotNull(value);
        assertTrue(value instanceof Double);
        assertEquals((double) 333.333, ((Double) value).doubleValue(), 0.005);

        resolver.setValue(bean, "floatProperty", new Float(22.11));
        value = resolver.getValue(bean, "floatProperty");
        assertNotNull(value);
        assertTrue(value instanceof Float);
        assertEquals((float) 22.11, ((Float) value).floatValue(), 0.5);

        resolver.setValue(bean, "intProperty", new Integer(4321));
        value = resolver.getValue(bean, "intProperty");
        assertNotNull(value);
        assertTrue(value instanceof Integer);
        assertEquals((int) 4321, ((Integer) value).intValue());

        resolver.setValue(bean, "longProperty", new Long(12345));
        value = resolver.getValue(bean, "longProperty");
        assertNotNull(value);
        assertTrue(value instanceof Long);
        assertEquals((long) 12345, ((Long) value).longValue());

        resolver.setValue(bean, "nestedProperty", new ElBean());
        value = resolver.getValue(bean, "nestedProperty");
        assertNotNull(value);
        assertTrue(value instanceof ElBean);

        resolver.setValue(bean, "shortProperty", new Short((short) 123));
        value = resolver.getValue(bean, "shortProperty");
        assertNotNull(value);
        assertTrue(value instanceof Short);
        assertEquals((short) 123, ((Short) value).shortValue());

        resolver.setValue(bean, "stringProperty", "That was a STRING");
        value = resolver.getValue(bean, "stringProperty");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("That was a STRING", (String) value);

    }


    // Positive setValue() tests on a Map base object
    public void testStringSetMap() throws Exception {

        // PENDING - insert tests here

    }


    // Postitive getValue() tests on a JavaBean base object
    public void testStringTypeBean() throws Exception {

        Class value = null;

        value = resolver.getType(bean, "booleanProperty");
        assertNotNull(value);
        assertEquals(Boolean.TYPE, value);

        value = resolver.getType(bean, "byteProperty");
        assertNotNull(value);
        assertEquals(Byte.TYPE, value);

        value = resolver.getType(bean, "doubleProperty");
        assertNotNull(value);
        assertEquals(Double.TYPE, value);

        value = resolver.getType(bean, "floatProperty");
        assertNotNull(value);
        assertEquals(Float.TYPE, value);

        value = resolver.getType(bean, "intProperty");
        assertNotNull(value);
        assertEquals(Integer.TYPE, value);

        value = resolver.getType(bean, "longProperty");
        assertNotNull(value);
        assertEquals(Long.TYPE, value);

        bean.setNestedProperty(new ElBean());
        value = resolver.getType(bean, "nestedProperty");
        assertNotNull(value);
        assertEquals(ElBean.class, value);

        value = resolver.getType(bean, "shortProperty");
        assertNotNull(value);
        assertEquals(Short.TYPE, value);

        value = resolver.getType(bean, "stringProperty");
        assertNotNull(value);
        assertEquals(String.class, value);

    }


    // Positive getValue() tests on a Map base object
    public void testStringTypeMap() throws Exception {

        // PENDING - insert tests here

    }


    public void testReadOnlyObject() {
        ExternalContext ec = getFacesContext().getExternalContext();

        // these are mutable Maps
       
        assertTrue(!resolver.isReadOnly(ec.getApplicationMap(), "hello"));
        assertTrue(!resolver.isReadOnly(ec.getSessionMap(), "hello"));
        assertTrue(!resolver.isReadOnly(ec.getRequestMap(), "hello")); 

        // these are immutable Maps
        assertTrue(resolver.isReadOnly(ec.getRequestParameterMap(), "hello"));
        assertTrue(resolver.isReadOnly(ec.getRequestParameterValuesMap(),
                                       "hello"));
        assertTrue(resolver.isReadOnly(ec.getRequestHeaderMap(), "hello"));
        assertTrue(resolver.isReadOnly(ec.getRequestHeaderValuesMap(),
                                       "hello"));
        assertTrue(resolver.isReadOnly(ec.getRequestCookieMap(), "hello"));
        assertTrue(resolver.isReadOnly(ec.getInitParameterMap(), "hello")); 

        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        assertTrue(resolver.isReadOnly(root, "childCount"));

        com.sun.faces.cactus.TestBean testBean = (com.sun.faces.cactus.TestBean) ec.getSessionMap().get("TestBean");
        assertTrue(resolver.isReadOnly(testBean, "readOnly"));
        assertTrue(!resolver.isReadOnly(testBean, "one"));
    }


    public void testReadOnlyIndex() {
        // PENDING(edburns): implement readonly index tests.
    }


    public void testType() {
        // PENDING(edburns): implement type tests
    }
    
    public void testConversion() throws Exception
    {
         ElBean bean = new ElBean(); 
       
        this.conversionTest(bean, "booleanProperty", null, false);
        this.conversionTest(bean, "booleanProperty", "5", false);
        this.conversionTest(bean, "booleanProperty", new Character('c'), false);
        this.conversionTest(bean, "booleanProperty", Boolean.TRUE, true);
        this.conversionTest(bean, "booleanProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "booleanProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "booleanProperty", new Byte((byte) 5), false);
        this.conversionTest(bean, "booleanProperty", new Short((short) 5), false);
        this.conversionTest(bean, "booleanProperty", new Integer(5), false);
        this.conversionTest(bean, "booleanProperty", new Long(5), false);
        this.conversionTest(bean, "booleanProperty", new Float(5), false);
        this.conversionTest(bean, "booleanProperty", new Double(5), false);
        
        this.conversionTest(bean, "byteProperty", null, false);       
        this.conversionTest(bean, "byteProperty", "5", false);        
        this.conversionTest(bean, "byteProperty", new Character('c'), false);
        this.conversionTest(bean, "byteProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "byteProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "byteProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "byteProperty", new Byte((byte) 5), true);
        this.conversionTest(bean, "byteProperty", new Short((short) 5), false);
        this.conversionTest(bean, "byteProperty", new Integer(5), false);
        this.conversionTest(bean, "byteProperty", new Long(5), false);
        this.conversionTest(bean, "byteProperty", new Float(5), false);
        this.conversionTest(bean, "byteProperty", new Double(5), false);
        
        this.conversionTest(bean, "characterProperty", null, false);
        this.conversionTest(bean, "characterProperty", "5", false);
        this.conversionTest(bean, "characterProperty", new Character('c'), true);
        this.conversionTest(bean, "characterProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "characterProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "characterProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "characterProperty", new Byte((byte) 5), false);
        this.conversionTest(bean, "characterProperty", new Short((short) 5), false);
        this.conversionTest(bean, "characterProperty", new Integer(5), false);
        this.conversionTest(bean, "characterProperty", new Long(5), false);
        this.conversionTest(bean, "characterProperty", new Float(5), false);
        this.conversionTest(bean, "characterProperty", new Double(5), false);
        
        this.conversionTest(bean, "doubleProperty", null, false);
        this.conversionTest(bean, "doubleProperty", "5", false);        
        this.conversionTest(bean, "doubleProperty", new Character('c'), true);
        this.conversionTest(bean, "doubleProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "doubleProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "doubleProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "doubleProperty", new Byte((byte) 5), true);
        this.conversionTest(bean, "doubleProperty", new Short((short) 5), true);
        this.conversionTest(bean, "doubleProperty", new Integer(5), true);
        this.conversionTest(bean, "doubleProperty", new Long(5), true);
        this.conversionTest(bean, "doubleProperty", new Float(5), true);
        this.conversionTest(bean, "doubleProperty", new Double(5), true);
        
        this.conversionTest(bean, "floatProperty", null, false);
        this.conversionTest(bean, "floatProperty", "5", false);
        this.conversionTest(bean, "floatProperty", new Character('c'), true);
        this.conversionTest(bean, "floatProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "floatProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "floatProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "floatProperty", new Byte((byte) 5), true);
        this.conversionTest(bean, "floatProperty", new Short((short) 5), true);
        this.conversionTest(bean, "floatProperty", new Integer(5), true);
        this.conversionTest(bean, "floatProperty", new Long(5), true);
        this.conversionTest(bean, "floatProperty", new Float(5), true);
        this.conversionTest(bean, "floatProperty", new Double(5), false);
        
        this.conversionTest(bean, "longProperty", null, false);
        this.conversionTest(bean, "longProperty", "5", false);
        this.conversionTest(bean, "longProperty", new Character('c'), true);
        this.conversionTest(bean, "longProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "longProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "longProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "longProperty", new Byte((byte) 5), true);
        this.conversionTest(bean, "longProperty", new Short((short) 5), true);
        this.conversionTest(bean, "longProperty", new Integer(5), true);
        this.conversionTest(bean, "longProperty", new Long(5), true);
        this.conversionTest(bean, "longProperty", new Float(5), false);
        this.conversionTest(bean, "longProperty", new Double(5), false);
        
        this.conversionTest(bean, "shortProperty", null, false);
        this.conversionTest(bean, "shortProperty", "5", false);
        this.conversionTest(bean, "shortProperty", new Character('c'), false);
        this.conversionTest(bean, "shortProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "shortProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "shortProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "shortProperty", new Byte((byte) 5), true);
        this.conversionTest(bean, "shortProperty", new Short((short) 5), true);
        this.conversionTest(bean, "shortProperty", new Integer(5), false);
        this.conversionTest(bean, "shortProperty", new Long(5), false);
        this.conversionTest(bean, "shortProperty", new Float(5), false);
        this.conversionTest(bean, "shortProperty", new Double(5), false);
        
        this.conversionTest(bean, "stringProperty", null, true);
        this.conversionTest(bean, "stringProperty", "5", true);
        this.conversionTest(bean, "stringProperty", new Character('c'), false);
        this.conversionTest(bean, "stringProperty", Boolean.TRUE, false);
        this.conversionTest(bean, "stringProperty", new BigInteger("5"), false);
        this.conversionTest(bean, "stringProperty", new BigDecimal("5"), false);
        this.conversionTest(bean, "stringProperty", new Byte((byte) 5), false);
        this.conversionTest(bean, "stringProperty", new Short((short) 5), false);
        this.conversionTest(bean, "stringProperty", new Integer(5), false);
        this.conversionTest(bean, "stringProperty", new Long(5), false);
        this.conversionTest(bean, "stringProperty", new Float(5), false);
        this.conversionTest(bean, "stringProperty", new Double(5), false); 
    } 
    
    protected void conversionTest(Object bean, String property, Object value, boolean valid) throws Exception
    {
        if (valid)
        {
            try
            {
                this.resolver.setValue(bean, property, value);
            }
            catch (Exception e)
            {
                
                Class type = this.resolver.getType(bean, property);
                throw e;
               // throw new Exception("Conversion to "+type+" should not have failed for type "+((value != null) ? value.getClass() : null), e);
            }
        }
        else
        {
            try
            {
                this.resolver.setValue(bean, property, value);
                fail("Conversion to "+this.resolver.getType(bean, property)+" should have failed for type "+((value != null) ? value.getClass() : null));
            }
            catch (FacesException e)
            {
                // good, should have ended up here
            }
        }
    }

} // end of class TestPropertyResolverImpl
