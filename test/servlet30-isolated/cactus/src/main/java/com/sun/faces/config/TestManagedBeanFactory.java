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

package com.sun.faces.config;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.cactus.TestingUtil;
import com.sun.faces.TestBean;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.ManagedBeanInfo;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.el.ELUtils;

import com.sun.faces.util.Util;
import javax.el.ValueExpression;

import javax.faces.FacesException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Unit tests for Managed Bean Factory.</p>
 */

public class TestManagedBeanFactory extends ServletFacesTestCase {


    public static String beanName = "com.sun.faces.TestBean";

    // ----------------------------------------------------- Instance Variables

    TestBean testBean;

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this test case.
     */
    public TestManagedBeanFactory() {
        super("TestManagedBeanFactory");
    }


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public TestManagedBeanFactory(String name) {

        super(name);

    }


    // --------------------------------------------------- Overall Test Methods


    // ------------------------------------------------ Individual Test Methods



    // Test managed bean 
    public void testNoProperty() throws Exception {
        //Testing with no properties set
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   null,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);
        assertNotNull(beanManager.create(beanName, getFacesContext()));
        BeanBuilder builder = beanManager.getBuilder(beanName);
        assertTrue(ELUtils.Scope.SESSION.toString().equals(builder.getScope()));
    }


    public void testSimpleProperty() throws Exception {
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("one",
                                                 null,
                                                 "one",
                                                 null,
                                                 null);
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        list.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        assertTrue(testBean.getOne().equals("one"));

        //make sure scope is stored properly
        BeanBuilder builder = beanManager.getBuilder(beanName);
        assertTrue(ELUtils.Scope.SESSION.toString().equals(builder.getScope()));
    }


    public void testPrimitiveProperty() throws Exception {

        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);

        boolean testBoolean = true;
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("boolProp",
                                                 null,
                                                 Boolean.toString(testBoolean),
                                                 null,
                                                 null);
        list.add(property);

        byte testByte = 100;
        property = new ManagedBeanInfo.ManagedProperty("byteProp",
                                                 null,
                                                 Byte.valueOf(testByte).toString(),
                                                 null,
                                                 null);
        list.add(property);

        char testChar = 'z';
        property = new ManagedBeanInfo.ManagedProperty("charProp",
                                                 null,
                                                 Character.valueOf(testChar).toString(),
                                                 null,
                                                 null);
        list.add(property);

        double testDouble = 11.278D;
        property = new ManagedBeanInfo.ManagedProperty("doubleProp",
                                                 null,
                                                 Double.valueOf(testDouble).toString(),
                                                 null,
                                                 null);
        list.add(property);

        float testFloat = 45.789F;
        property = new ManagedBeanInfo.ManagedProperty("floatProp",
                                                 null,
                                                 Float.valueOf(testFloat).toString(),
                                                 null,
                                                 null);
        list.add(property);

        int testInt = 42;
        property = new ManagedBeanInfo.ManagedProperty("intProp",
                                                 null,
                                                 Integer.valueOf(testInt).toString(),
                                                 null,
                                                 null);
        list.add(property);

        long testLong = 3147893289L;
        property = new ManagedBeanInfo.ManagedProperty("longProp",
                                                 null,
                                                 Long.valueOf(testLong).toString(),
                                                 null,
                                                 null);
        list.add(property);

        short testShort = 25432;
        property = new ManagedBeanInfo.ManagedProperty("shortProp",
                                                 null,
                                                 Short.valueOf(testShort).toString(),
                                                 null,
                                                 null);
        list.add(property);

        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        assertTrue(testBean.getBoolProp() == testBoolean);
        assertTrue(testBean.getByteProp() == testByte);
        assertTrue(testBean.getCharProp() == testChar);
        assertTrue(testBean.getDoubleProp() == testDouble);
        assertTrue(testBean.getFloatProp() == testFloat);
        assertTrue(testBean.getIntProp() == testInt);
        assertTrue(testBean.getLongProp() == testLong);
        assertTrue(testBean.getShortProp() == testShort);

        //make sure scope is stored properly
        BeanBuilder builder = beanManager.getBuilder(beanName);
        assertTrue(ELUtils.Scope.SESSION.toString().equals(builder.getScope()));
    }
    
    public void testSimpleNumericProperty() throws Exception {
        // If a property value is "" ensure numeric properties
        // are set to 0.
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);

        boolean testBoolean = true;
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("boolProp",
                                                 null,
                                                 Boolean.toString(testBoolean),
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("byteProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("charProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("doubleProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("floatProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("intProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("longProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        property = new ManagedBeanInfo.ManagedProperty("shortProp",
                                                 null,
                                                 "",
                                                 null,
                                                 null);
        list.add(property);

        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));
                 
        assertTrue(testBean.getByteProp() == 0); 
        assertTrue(testBean.getCharProp() == 0);
        assertTrue(testBean.getDoubleProp() == 0);
        assertTrue(testBean.getFloatProp() == 0);
        assertTrue(testBean.getIntProp() == 0);
        assertTrue(testBean.getLongProp() == 0);
        assertTrue(testBean.getShortProp() == 0);
    }


    public void testIndexProperty() throws Exception {
        List<String> values = new ArrayList<String>(2);
        values.add("foo");
        values.add("bar");
        ManagedBeanInfo.ListEntry listEntry =
             new ManagedBeanInfo.ListEntry(null, values);

        List<ManagedBeanInfo.ManagedProperty> properties =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(2);
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("indexProperties",
                                                 null,
                                                 null,
                                                 null,
                                                 listEntry);
        properties.add(property);

        property = new ManagedBeanInfo.ManagedProperty("indexPropertiesNull",
                                                       null,
                                                       null,
                                                       null,
                                                       listEntry);
        properties.add(property);


        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "request",
                                                   null,
                                                   null,
                                                   properties,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        ArrayList props = (ArrayList) testBean.getIndexProperties();
        assertTrue(props.get(5).equals("foo"));
        assertTrue(props.get(6).equals("bar"));

        // setter shouldn't be called if bean getter returns List
        assertTrue(!testBean.getListSetterCalled());

        // setter should be called if bean getter returned null
        assertTrue(testBean.getListNullSetterCalled());

        //make sure scope is stored properly
        BeanBuilder builder = beanManager.getBuilder(beanName);
        assertTrue(ELUtils.Scope.REQUEST.toString().equals(builder.getScope()));
    }


    public void testMapProperty() throws Exception {

        Map<String,String> entry = new HashMap(1, 1.0f);
        entry.put("name", "Justyna");
        ManagedBeanInfo.MapEntry mapEntry =
             new ManagedBeanInfo.MapEntry(null,
                                          null,
                                          entry);

        List<ManagedBeanInfo.ManagedProperty> properties =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(2);
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("mapProperty",
                                                 null,
                                                 null,
                                                 mapEntry,
                                                 null);
        properties.add(property);

        property = new ManagedBeanInfo.ManagedProperty("mapPropertyNull",
                                                       null,
                                                       null,
                                                       mapEntry,
                                                       null);
        properties.add(property);


        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "request",
                                                   null,
                                                   null,
                                                   properties,
                                                   null);


        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        HashMap mapProperty = (HashMap)
            testBean.getMapProperty();
        assertTrue(((String) mapProperty.get("name")).equals("Justyna"));

        // setter shouldn't be called if bean getter returns Map
        assertTrue(!testBean.getMapPropertySetterCalled());

        // setter should be called if bean getter returned null
        assertTrue(testBean.getMapPropertyNullSetterCalled());

        //make sure scope is stored properly
        BeanBuilder builder = beanManager.getBuilder(beanName);
        assertTrue(ELUtils.Scope.REQUEST.toString().equals(builder.getScope()));
    }


    public void testIndexTypeProperty() throws Exception {
        List<String> val = new ArrayList<String>(1);
        val.add("23");
        ManagedBeanInfo.ListEntry listEntry =
             new ManagedBeanInfo.ListEntry("java.lang.Integer", val);
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("indexIntegerProperties",
                                                 "java.lang.Integer",
                                                 null,
                                                 null,
                                                 listEntry);
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        list.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        ArrayList properties = (ArrayList) testBean.getIndexIntegerProperties();
        assertTrue(properties.get(1) instanceof Integer);

        Integer integer = new Integer("23");
        assertTrue(properties.get(2).equals(integer));
    }


    public void testMapTypeProperty() throws Exception {

        Map<String,String> entry = new HashMap(1, 1.0f);
        entry.put("name", "23");
        ManagedBeanInfo.MapEntry mapEntry =
             new ManagedBeanInfo.MapEntry("java.lang.String",
                                          "java.lang.Integer",
                                          entry);

        List<ManagedBeanInfo.ManagedProperty> properties =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("mapProperty",
                                                 null,
                                                 null,
                                                 mapEntry,
                                                 null);
        properties.add(property);

        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "request",
                                                   null,
                                                   null,
                                                   properties,
                                                   null);


        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        HashMap mapProperty = (HashMap)
            testBean.getMapProperty();

        assertTrue(mapProperty.get("name") instanceof Integer);

        Integer integer = new Integer("23");
        assertTrue(mapProperty.get("name").equals(integer));

    }


    public void testValueRefProperty() throws Exception {

        TestBean testBean = new TestBean();
        testBean.setOne("one");
        getFacesContext().getExternalContext().getSessionMap().put(
            "TestRefBean", testBean);

        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("one",
                                                 null,
                                                 "#{TestRefBean.one}",
                                                 null,
                                                 null);
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        list.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        assertTrue(testBean.getOne().equals("one"));

    }


    public void testValueRefScope() throws Exception {
        //Testing value ref scope

        TestBean testBean = new TestBean();
        testBean.setOne("one");
        boolean exceptionThrown = false;

        //testing with:
        //  valueref in application scope
        //  managed bean in session scope
        getFacesContext().getExternalContext().getApplicationMap().put(
            "TestRefBean", testBean);


        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("one",
                                                 null,
                                                 "#{TestRefBean.one}",
                                                 null,
                                                 null);
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        list.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        //testing with a property set
        assertNotNull(testBean = (TestBean) beanManager.create(beanName,
                                                               getFacesContext()));

        //make sure bean instantiated properly. Get property back from bean.
        assertTrue(testBean.getOne().equals("one"));


        //testing with:
        //  valueref in request scope
        //  managed bean in application scope
        getFacesContext().getExternalContext().getApplicationMap()
             .remove("TestRefBean");
        getFacesContext().getExternalContext().getRequestMap().put(
            "TestRefBean", testBean);

        bean = new ManagedBeanInfo(beanName,
                                   beanName,
                                   "application",
                                   null,
                                   null,
                                   list,
                                   null);
        beanManager.register(bean);

        exceptionThrown = false;
        try {
            //testing with a property set
            beanManager.create(beanName, getFacesContext());
            fail("Should have thrown FacesException");
        } catch (FacesException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        //cleanup
        getFacesContext().getExternalContext().getRequestMap().remove(
            "TestRefBean");

        //testing with:
        //  valueref in session scope
        //  managed bean in no scope
        getFacesContext().getExternalContext().getSessionMap().put(
            "TestRefBean", testBean);

       bean = new ManagedBeanInfo(beanName,
                                   beanName,
                                   "none",
                                   null,
                                   null,
                                   list,
                                   null);
        beanManager.register(bean);

        exceptionThrown = false;
        try {
            beanManager.create(beanName, getFacesContext());
            fail("Should have thrown FacesException");
        } catch (FacesException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }

    public void testViewScope() throws Exception {
        TestBean testBean = new TestBean();
        testBean.setOne("one");

        getFacesContext().getExternalContext().getRequestMap().put("TestRefBean", testBean);
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("one",
                                                 null,
                                                 "#{TestRefBean.one}",
                                                 null,
                                                 null);
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        list.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "view",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);

        try {
            // request scope is shorter than view scope, so creation should fail
            beanManager.create(beanName, getFacesContext());
            assertTrue(false);
        } catch (Exception ignored) {
        }

        bean = new ManagedBeanInfo(beanName,
                                   beanName,
                                   "view",
                                   null,
                                   null,
                                   null,
                                   null);
        beanManager.getRegisteredBeans().remove(beanName);
        beanManager.register(bean);

        Object beanObject = beanManager.create(beanName, getFacesContext());
        assertNotNull(beanObject);
        assertTrue(getFacesContext().getViewRoot().getViewMap().containsKey(beanName));
        
    }
    
    public void testNoneScope() throws Exception {
        //Testing value ref scope
        TestBean testBean = new TestBean();
        testBean.setOne("one");
        boolean exceptionThrown = false;

        //  valueref in request scope
        //  managed bean in none scope
        // this should fail
        getFacesContext().getExternalContext().getRequestMap().put(
            "TestRefBean", testBean);

        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("one",
                                                 null,
                                                 "#{TestRefBean.one}",
                                                 null,
                                                 null);
        List<ManagedBeanInfo.ManagedProperty> list =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        list.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "none",
                                                   null,
                                                   null,
                                                   list,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);


        exceptionThrown = false;
        try {
            beanManager.create(beanName, getFacesContext());
            fail("Should have thrown FacesException");
        } catch (FacesException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        //cleanup
        getFacesContext().getExternalContext().getRequestMap().remove(
            "TestRefBean");

        //  valueref in none scope
        //  managed bean in none scope
        // this should pass
        ValueExpression valueExpression1 = 
        ELUtils.createValueExpression("#{testBean.customerBean.name}");
        exceptionThrown = false;
        try {
            valueExpression1.getValue(getFacesContext().getELContext());
        } catch (FacesException ex) {
            exceptionThrown = true;
        }
        assertTrue(!exceptionThrown);
      
    }

    public void testMixedBean() throws Exception {
        ValueExpression vb =
            ELUtils.createValueExpression(
                "#{mixedBean}");
        TestBean bean = (TestBean) vb.getValue(getFacesContext().getELContext());
        assertEquals("mixed value Bobby \" \\  \\\" Orr", bean.getProp());

        vb =
            ELUtils.createValueExpression(
                "#{mixedBean.prop}");
        assertEquals(bean.getProp(), (String) vb.getValue(getFacesContext().getELContext()));
    } 

    public void testMixedBeanNegative() throws Exception {
        ValueExpression vb =
            ELUtils.createValueExpression(
                "#{threeBeanSaladNegative}");
	boolean exceptionThrown = false;
	try {
	    TestBean bean = (TestBean) vb.getValue(getFacesContext().getELContext());
	    assertTrue(false);
	}
	catch (FacesException pnfe) {
	    exceptionThrown = true;
	}

	assertTrue(exceptionThrown);
    }

    public void testMixedBeanPositive() throws Exception {
        ValueExpression vb =
            ELUtils.createValueExpression(
                "#{threeBeanSaladPositive}");
        TestBean bean = (TestBean) vb.getValue(getFacesContext().getELContext());
        assertEquals("request request session session none none", 
		     bean.getProp());

        vb =
            ELUtils.createValueExpression(
                "#{threeBeanSaladPositive.prop}");
        assertEquals(bean.getProp(), (String) vb.getValue(getFacesContext().getELContext()));
    }


    public void testConstructorException() {
        // constructor of this bean throws ann exception. Make sure the
        // exception is not swallowed.
        ValueExpression valueExpression1 = 
           ELUtils.createValueExpression("#{exceptionBean.one}");
        boolean exceptionThrown = false;
        try {
            valueExpression1.getValue(getFacesContext().getELContext());
        } catch (FacesException ex) {
            Throwable t = ex.getCause();
            exceptionThrown = true;
            assertTrue((t.getMessage().
                indexOf("TestConstructorException Passed")) != -1);
        }   
        assertTrue(exceptionThrown);
        
    }
    
    public void testIsInjectable() throws Exception {

        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "session",
                                                   null,
                                                   null,
                                                   null,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);
        BeanBuilder builder = beanManager.getBuilder(beanName);
        Boolean isInjectable = (Boolean) Util.classHasAnnotations(TestBean.class);
        assertTrue(!isInjectable);

        bean = new ManagedBeanInfo(beanName,
                                   "com.sun.faces.config.TestManagedBeanFactory$InjectionBean",
                                   "request",
                                   null,
                                   null,
                                   null,
                                   null);
        beanManager.register(bean);

        isInjectable = (Boolean) Util.classHasAnnotations(InjectionBean.class);

        assertTrue(isInjectable);
    }

    public void testViewScopeAnnotationCallBacks() throws Exception {

        BeanManager beanManager =
             ApplicationAssociate.getInstance(getFacesContext().getExternalContext()).getBeanManager();
        ManagedBeanInfo bean = new ManagedBeanInfo("viewBean",
                                                   "com.sun.faces.config.TestManagedBeanFactory$InjectionBean",
                                                   "view",
                                                   null,
                                                   null,
                                                   null,
                                                   null);
        beanManager.register(bean);
        InjectionBean injectionBean = (InjectionBean) beanManager.create("viewBean", getFacesContext());
        assertTrue(injectionBean.initCalled);
        getFacesContext().getViewRoot().getViewMap().clear();
        assertTrue(injectionBean.destroyCalled);

    }



    /**
     * For Issue 761.
     */
    public void testManagedPropertyMixedVERegresssion() throws Exception {

        Map<String,Object> requestMap = getFacesContext().getExternalContext().getRequestMap();
        requestMap.put("val", "String");
        List<ManagedBeanInfo.ManagedProperty> properties =
             new ArrayList<ManagedBeanInfo.ManagedProperty>(1);
        ManagedBeanInfo.ManagedProperty property =
             new ManagedBeanInfo.ManagedProperty("modelLabel",
                                                 null,
                                                 "#{'this'} is a String",
                                                 null,
                                                 null);
        properties.add(property);
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "request",
                                                   null,
                                                   null,
                                                   properties,
                                                   null);
        BeanManager beanManager =
             ApplicationAssociate.getCurrentInstance().getBeanManager();
        beanManager.register(bean);
        testBean = (TestBean) beanManager.create(beanName, getFacesContext());
        assertTrue("this is a String", "this is a String".equals(testBean.getModelLabel()));

    }


    public void testManagedBeanCustomScope() throws Exception {

        BeanManager beanManager =
              ApplicationAssociate.getCurrentInstance().getBeanManager();
        testBean = (TestBean) beanManager.create("customScopeBean", getFacesContext());
        Map<String,Object> requestMap = getFacesContext().getExternalContext().getRequestMap();
        assertTrue(testBean == requestMap.get("customScopeBean"));

        // invalid scope sanity check
        ManagedBeanInfo bean = new ManagedBeanInfo(beanName,
                                                   beanName,
                                                   "#{myScope",
                                                   null,
                                                   null,
                                                   null,
                                                   null);
        beanManager.register(bean);
        try {
            beanManager.create(beanName, getFacesContext());
            fail();
        } catch (Exception e) {

        }

        bean = new ManagedBeanInfo(beanName,
                                   beanName,
                                   "myScope",
                                   null,
                                   null,
                                   null,
                                   null);
        beanManager.register(bean);
        try {
            beanManager.create(beanName, getFacesContext());
            fail();
        } catch (Exception e) {

        }
        
    }


	
	
    /************* PENDING(edburns): rewrite to exercise new edge case
     * detection.

     public void testInvalidPropertyConfiguration() throws Exception {
     // If a ConfigManagedPropertyValue has a value that requires
     // converstion from String (the default type) to another type,
     // say Integer as an example, and the CMPV's value category
     // isn't set to Value, conversion will not take place.  Thus, an
     // error should occur when creating a new instanced of the
     // managed bean.

     // no value category set
     bean = new ManagedBeanBean();
     bean.setManagedBeanClass(beanName);
     bean.setManagedBeanScope("session");

     boolean testBoolean = true;
     property = new ManagedPropertyBean();
     property.setPropertyName("boolProp");
     propertyv = new ConfigManagedBeanPropertyValue();
     propertyv.setValue((new Boolean(testBoolean)).toString());
     property.setValue(propertyv);
     bean.addManagedProperty(property);

     mbf = new ManagedBeanFactory(bean);

     boolean exceptionThrown = false;
     try {
     mbf.newInstance();
     } catch (FacesException fe) {
     exceptionThrown = true;
     }
     assertTrue(exceptionThrown);


     // value category set to VALUE_BINDING
     bean = new ManagedBeanBean();
     bean.setManagedBeanClass(beanName);
     bean.setManagedBeanScope("session");

     property = new ManagedPropertyBean();
     property.setPropertyName("boolProp");
     propertyv = new ConfigManagedBeanPropertyValue();
     propertyv.setValueCategory(ConfigManagedBeanPropertyValue.VALUE_BINDING);
     propertyv.setValue((new Boolean(testBoolean)).toString());
     property.setValue(propertyv);
     bean.addManagedProperty(property);

     mbf = new ManagedBeanFactory(bean);

     exceptionThrown = false;
     try {
     mbf.newInstance();
     } catch (FacesException fe) {
     exceptionThrown = true;
     }
     assertTrue(exceptionThrown);

     }

     public void testExceptions() throws Exception {

     bean = new ManagedBeanBean();
     bean.setManagedBeanClass("foo");
     bean.setManagedBeanScope("session");

     property = new ManagedPropertyBean();
     property.setPropertyName("one");

     propertyv = new ConfigManagedBeanPropertyValue();
     propertyv.setValue("one");

     property.setValue(propertyv);
     boolean exceptionThrown = false;
     try {
     bean.addManagedProperty(property);
     } catch (FacesException fe) {
     exceptionThrown = true;
     }
     assertTrue(exceptionThrown);
     exceptionThrown = false;
     try {
     property = null;
     bean.addManagedProperty(property);
     } catch (NullPointerException npe) {
     exceptionThrown = true;
     }
     assertTrue(exceptionThrown);
     }
     ***********/
    
      public static class InjectionBean {

        private boolean initCalled;
        private boolean destroyCalled;

        @PostConstruct void init() {
            initCalled = true;
        }

        @PreDestroy void destroy() {
            destroyCalled = true;
        }

        public boolean getInit() {
            return initCalled;
        }

        public boolean getDestroy() {
            return destroyCalled;
        }

    } // END ProtectedBean
}
