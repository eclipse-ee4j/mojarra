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

// TestConverters.java

package com.sun.faces.convert;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectMany;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContextFactory;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.NumberConverter;

import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.cactus.TestingUtil;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

/**
 * Test encode and decode methods in Renderer classes.
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestConverters extends JspFacesTestCase {

    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //
    private FacesContextFactory facesContextFactory = null;

    // Attribute Instance Variables
    // Relationship Instance Variables

    protected Application application;
    //
    // Constructors and Initializers    
    //

    public TestConverters() {
        super("TestConverters");
    }


    public TestConverters(String name) {
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
        ApplicationFactory aFactory =
            (ApplicationFactory) FactoryFinder.getFactory(
                FactoryFinder.APPLICATION_FACTORY);
        application = aFactory.getApplication();
        UIViewRoot viewRoot = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        viewRoot.setViewId("viewId");
        viewRoot.setLocale(Locale.US);
        getFacesContext().setViewRoot(viewRoot);
    }


    public void beginConverters(WebRequest theRequest) {

    }


    public void testConverters() {

        try {
            // create a dummy root for the tree.
            UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
            root.setId("root");

            testDateConverter(root);
            testNumberConverter(root);
            testConverterInheritance(root);
            testBooleanConverter(root);
            testConverterInheritance(root);
            //assertTrue(verifyExpectedOutput());
            testDoubleConverter(root);
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue(false);
            return;
        }
    }


    public static class TestBean extends Object {

        boolean[] booleans = null;

        public TestBean() {
            super();
        }

        public boolean[] getBooleans() {
            return booleans;
        }


        public void setBooleans(boolean[] newBooleans) {
            booleans = newBooleans;
        }


        byte[] bytes = null;


        public byte[] getBytes() {
            return bytes;
        }


        public void setBytes(byte[] newBytes) {
            bytes = newBytes;
        }


        char[] chars = null;


        public char[] getChars() {
            return chars;
        }


        public void setChars(char[] newChars) {
            chars = newChars;
        }


        short[] shorts = null;


        public short[] getShorts() {
            return shorts;
        }


        public void setShorts(short[] newShorts) {
            shorts = newShorts;
        }


        int[] ints = null;


        public int[] getInts() {
            return ints;
        }


        public void setInts(int[] newInts) {
            ints = newInts;
        }


        long[] longs = null;


        public long[] getLongs() {
            return longs;
        }


        public void setLongs(long[] newLongs) {
            longs = newLongs;
        }


        float[] floats = null;


        public float[] getFloats() {
            return floats;
        }


        public void setFloats(float[] newFloats) {
            floats = newFloats;
        }


        double[] doubles = null;


        public double[] getDoubles() {
            return doubles;
        }


        public void setDoubles(double[] newDoubles) {
            doubles = newDoubles;
        }


        String[] strings = null;


        public String[] getStrings() {
            return strings;
        }


        public void setStrings(String[] newStrings) {
            strings = newStrings;
        }


        Date[] dates = null;


        public Date[] getDates() {
            return dates;
        }


        public void setDates(Date[] newDates) {
            dates = newDates;
        }


        Number[] numbers = null;


        public Number[] getNumbers() {
            return numbers;
        }


        public void setNumbers(Number[] newNumbers) {
            numbers = newNumbers;
        }


        List stringList = null;


        public List getStringList() {
            return stringList;
        }


        public void setStringList(List newStringList) {
            stringList = newStringList;
        }

    }


    public void beginUISelectMany(WebRequest theRequest) {

        // primitives
        theRequest.addParameter("bool", "false");
        theRequest.addParameter("bool", "true");
        theRequest.addParameter("bool", "false");
        theRequest.addParameter("bool2", "false");

        theRequest.addParameter("byte", Byte.toString(Byte.MIN_VALUE));
        theRequest.addParameter("byte", Byte.toString(Byte.MAX_VALUE));
        theRequest.addParameter("byte", "1");
        theRequest.addParameter("char", "Q");
        theRequest.addParameter("char", "A");
        theRequest.addParameter("char", "z");


        theRequest.addParameter("short", Short.toString(Short.MIN_VALUE));
        theRequest.addParameter("short", Short.toString(Short.MAX_VALUE));
        theRequest.addParameter("short",
                                Short.toString((short) (Byte.MAX_VALUE + 1)));

        theRequest.addParameter("int", Integer.toString(Integer.MIN_VALUE));
        theRequest.addParameter("int", Integer.toString(Integer.MAX_VALUE));
        theRequest.addParameter("int",
                                Integer.toString(Short.MAX_VALUE + 1));

        theRequest.addParameter("float", Float.toString(Float.MIN_VALUE));
        theRequest.addParameter("float", Float.toString(Float.MAX_VALUE));
        theRequest.addParameter("float",
                                Float.toString(Integer.MAX_VALUE + 1));

        theRequest.addParameter("long", Long.toString(Long.MIN_VALUE));
        theRequest.addParameter("long", Long.toString(Long.MAX_VALUE));
        theRequest.addParameter("long",
                                Long.toString(Integer.MAX_VALUE + 1));

        theRequest.addParameter("double", Double.toString(Double.MIN_VALUE));
        theRequest.addParameter("double", Double.toString(Double.MAX_VALUE));
        theRequest.addParameter("double",
                                Double.toString(Long.MAX_VALUE + 1));

        // Objects
        theRequest.addParameter("str", "value1");
        theRequest.addParameter("str", "value2");
        theRequest.addParameter("str", "value3");

        theRequest.addParameter("str2", "");

        theRequest.addParameter("date", "Jan 1, 1967");
        theRequest.addParameter("date", "May 26, 2003");
        theRequest.addParameter("date", "Aug 19, 1946");

        theRequest.addParameter("num", "12%");
        theRequest.addParameter("num", "3.14");
        theRequest.addParameter("num", "49.99");

        theRequest.addParameter("stringList", "value1");
        theRequest.addParameter("stringList", "value2");
        theRequest.addParameter("stringList", "value3");

    }


    public void testUISelectMany() throws Exception {
        // create the test bean
        TestBean bean = new TestBean();
        getFacesContext().getExternalContext().getRequestMap().put("bean",
                                                                   bean);
        // create a dummy root for the tree.
        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        root.setId("root");
        root.setLocale(Locale.US);
        getFacesContext().setViewRoot(root);

        // test model type of boolean []
        UISelectMany booleanv = new UISelectMany();
        booleanv.setId("bool");
        booleanv.setRendererType("javax.faces.Checkbox");
        booleanv.setValueExpression("value",
            (getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),  "#{bean.booleans}",
            Boolean.class)));
        root.getChildren().add(booleanv);
        booleanv.getChildren().add(newUISelectItem(Boolean.TRUE));
        booleanv.getChildren().add(newUISelectItem(Boolean.FALSE));
        booleanv.decode(getFacesContext());
        booleanv.validate(getFacesContext());
        booleanv.updateModel(getFacesContext());
        assertNotNull(bean.getBooleans());
        assertTrue(bean.getBooleans()[0] == false);
        assertTrue(bean.getBooleans()[1] == true);
        assertTrue(bean.getBooleans()[2] == false);

        // test model type of boolean []
        booleanv = new UISelectMany();
        booleanv.setId("bool2");
        booleanv.setRendererType("javax.faces.Checkbox");
        booleanv.setValueExpression("value",
            (getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.booleans}",
            Object.class)));
        root.getChildren().add(booleanv);
        booleanv.getChildren().add(newUISelectItem(Boolean.TRUE));
        booleanv.getChildren().add(newUISelectItem(Boolean.FALSE));
        booleanv.decode(getFacesContext());
        booleanv.validate(getFacesContext());
        booleanv.updateModel(getFacesContext());
        assertNotNull(bean.getBooleans());
        assertTrue(bean.getBooleans()[0] == false);
        assertTrue(bean.getBooleans().length == 1);

        // test model type of byte []
        UISelectMany bytev = new UISelectMany();
        bytev.setId("byte");
        bytev.setRendererType("javax.faces.Checkbox");
       
        bytev.setValueExpression("value", 
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(), "#{bean.bytes}", 
            Object.class));
        bytev.getChildren().add(newUISelectItem(new Byte(Byte.MIN_VALUE)));
        bytev.getChildren().add(newUISelectItem(new Byte(Byte.MAX_VALUE)));
        bytev.getChildren().add(newUISelectItem(new Byte((byte) 1)));
        bytev.getChildren().add(newUISelectItem(new Byte((byte) -1)));
        root.getChildren().add(bytev);
        bytev.decode(getFacesContext());
        bytev.validate(getFacesContext());
        bytev.updateModel(getFacesContext());
        assertNotNull(bean.getBytes());
        assertTrue(bean.getBytes()[0] == Byte.MIN_VALUE);
        assertTrue(bean.getBytes()[1] == Byte.MAX_VALUE);
        assertTrue(bean.getBytes()[2] == 1);

        // test model type of char []
        UISelectMany charv = new UISelectMany();
        charv.setId("char");
        charv.setRendererType("javax.faces.Checkbox");
        charv.setValueExpression("value",  
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.chars}", 
            Object.class));
        root.getChildren().add(charv);
        charv.getChildren().add(newUISelectItem(new Character('Q')));
        charv.getChildren().add(newUISelectItem(new Character('A')));
        charv.getChildren().add(newUISelectItem(new Character('Z')));
        charv.getChildren().add(newUISelectItem(new Character('z')));
        charv.decode(getFacesContext());
        charv.validate(getFacesContext());
        charv.updateModel(getFacesContext());
        assertNotNull(bean.getChars());
        assertTrue(bean.getChars()[0] == 'Q');
        assertTrue(bean.getChars()[1] == 'A');
        assertTrue(bean.getChars()[2] == 'z');

        // test model type of short []
        UISelectMany shortv = new UISelectMany();
        shortv.setId("short");
        shortv.setRendererType("javax.faces.Checkbox");
        shortv.setValueExpression("value",  
        getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.shorts}", Object.class));
        root.getChildren().add(shortv);
        shortv.getChildren().add(
            newUISelectItem(new Short((short) (Byte.MAX_VALUE + 1))));
        shortv.getChildren().add(newUISelectItem(new Short((short) 100)));
        shortv.getChildren().add(newUISelectItem(new Short(Short.MIN_VALUE)));
        shortv.getChildren().add(newUISelectItem(new Short(Short.MAX_VALUE)));
        shortv.decode(getFacesContext());
        shortv.validate(getFacesContext());
        shortv.updateModel(getFacesContext());
        assertNotNull(bean.getShorts());
        assertTrue(bean.getShorts()[0] == Short.MIN_VALUE);
        assertTrue(bean.getShorts()[1] == Short.MAX_VALUE);
        assertTrue(bean.getShorts()[2] == Byte.MAX_VALUE + 1);

        // test model type of int []
        UISelectMany intv = new UISelectMany();
        intv.setId("int");
        intv.setRendererType("javax.faces.Checkbox");
        intv.setValueExpression("value", 
             getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.ints}",
             Object.class));
        root.getChildren().add(intv);
        intv.getChildren().add(
            newUISelectItem(new Integer(Short.MAX_VALUE + 1)));
        intv.getChildren().add(newUISelectItem(new Integer(100)));
        intv.getChildren().add(newUISelectItem(new Integer(Integer.MIN_VALUE)));
        intv.getChildren().add(newUISelectItem(new Integer(Integer.MAX_VALUE)));
        intv.decode(getFacesContext());
        intv.validate(getFacesContext());
        intv.updateModel(getFacesContext());
        assertNotNull(bean.getInts());
        assertTrue(bean.getInts()[0] == Integer.MIN_VALUE);
        assertTrue(bean.getInts()[1] == Integer.MAX_VALUE);
        assertTrue(bean.getInts()[2] == Short.MAX_VALUE + 1);

        // test model type of float []
        UISelectMany floatv = new UISelectMany();
        floatv.setId("float");
        floatv.setRendererType("javax.faces.Checkbox");
        floatv.setValueExpression("value",  
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.floats}", 
            Object.class));
        root.getChildren().add(floatv);
        floatv.getChildren().add(
            newUISelectItem(new Float(Integer.MAX_VALUE + 1)));
        floatv.getChildren().add(newUISelectItem(new Float(100)));
        floatv.getChildren().add(newUISelectItem(new Float(Float.MIN_VALUE)));
        floatv.getChildren().add(newUISelectItem(new Float(Float.MAX_VALUE)));
        floatv.decode(getFacesContext());
        floatv.validate(getFacesContext());
        floatv.updateModel(getFacesContext());
        assertNotNull(bean.getFloats());
        assertTrue(bean.getFloats()[0] == Float.MIN_VALUE);
        assertTrue(bean.getFloats()[1] == Float.MAX_VALUE);
        assertTrue(bean.getFloats()[2] == Integer.MAX_VALUE + 1);

        // test model type of long []
        UISelectMany longv = new UISelectMany();
        longv.setId("long");
        longv.setRendererType("javax.faces.Checkbox");
        longv.setValueExpression("value",  
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.longs}", 
            Object.class));
        root.getChildren().add(longv);
        longv.getChildren().add(
            newUISelectItem(new Long(Integer.MAX_VALUE + 1)));
        longv.getChildren().add(newUISelectItem(new Long(100)));
        longv.getChildren().add(newUISelectItem(new Long(Long.MIN_VALUE)));
        longv.getChildren().add(newUISelectItem(new Long(Long.MAX_VALUE)));
        longv.decode(getFacesContext());
        longv.validate(getFacesContext());
        longv.updateModel(getFacesContext());
        assertNotNull(bean.getLongs());
        assertTrue(bean.getLongs()[0] == Long.MIN_VALUE);
        assertTrue(bean.getLongs()[1] == Long.MAX_VALUE);
        assertTrue(bean.getLongs()[2] == Integer.MAX_VALUE + 1);

        // test model type of double []
        UISelectMany doublev = new UISelectMany();
        doublev.setId("double");
        doublev.setRendererType("javax.faces.Checkbox");
        doublev.setValueExpression("value",
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.doubles}", 
            Object.class));
        root.getChildren().add(doublev);
        doublev.getChildren().add(
            newUISelectItem(new Double(Long.MAX_VALUE + 1)));
        doublev.getChildren().add(newUISelectItem(new Double(100)));
        doublev.getChildren().add(
            newUISelectItem(new Double(Double.MIN_VALUE)));
        doublev.getChildren().add(
            newUISelectItem(new Double(Double.MAX_VALUE)));
        doublev.decode(getFacesContext());
        doublev.validate(getFacesContext());
        doublev.updateModel(getFacesContext());
        assertNotNull(bean.getDoubles());
        assertTrue(bean.getDoubles()[0] == Double.MIN_VALUE);
        assertTrue(bean.getDoubles()[1] == Double.MAX_VALUE);
        assertTrue(bean.getDoubles()[2] == Long.MAX_VALUE + 1);

        // test model type of String []
        UISelectMany str = new UISelectMany();
        str.setId("str");
        str.setRendererType("javax.faces.Checkbox");
        str.setValueExpression("value", 
             getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.strings}", 
             Object.class));
        root.getChildren().add(str);
        str.getChildren().add(newUISelectItem("value1"));
        str.getChildren().add(newUISelectItem("value2"));
        str.getChildren().add(newUISelectItem("value3"));
        str.getChildren().add(newUISelectItem("value4"));
        str.decode(getFacesContext());
        str.validate(getFacesContext());
        str.updateModel(getFacesContext());
        assertNotNull(bean.getStrings());
        assertTrue("value1".equals(bean.getStrings()[0]));

        // test model type of Date []
        UISelectMany date = new UISelectMany();
        Converter dateConv = Util.getConverterForIdentifer(
            "javax.faces.DateTime", getFacesContext());
        assertNotNull(dateConv);
        date.setConverter(dateConv);
        date.setId("date");
        date.setRendererType("javax.faces.Checkbox");
        date.setValueExpression("value",  
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.dates}", 
            Object.class));
        root.getChildren().add(date);

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            date.getChildren().add(newUISelectItem(df.parse("19670101")));
            date.getChildren().add(newUISelectItem(df.parse("20030526")));
            date.getChildren().add(newUISelectItem(df.parse("19460819")));
            date.getChildren().add(newUISelectItem(df.parse("17760704")));
        } catch (ParseException e) {
            assertTrue(e.getMessage(), false);
        }

        date.decode(getFacesContext());
        date.validate(getFacesContext());
        date.updateModel(getFacesContext());
        assertNotNull(bean.getDates());
        Object expected = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            expected = df.parse("19460819");
        } catch (ParseException e) {
            assertTrue(e.getMessage(), false);
        }
        assertEquals("bean.getDates()[2] not as expected: ",
                     expected, bean.getDates()[2]);

        // test model type of Number []
        UISelectMany number = new UISelectMany();
        Converter numberConv = Util.getConverterForIdentifer(
            "javax.faces.Number", getFacesContext());
        assertNotNull(numberConv);
        number.setConverter(numberConv);
        number.setId("num");
        number.setRendererType("javax.faces.Checkbox");
        number.setValueExpression("value",
            getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.numbers}", 
            Object.class));
        root.getChildren().add(number);
        number.getChildren().add(newUISelectItem(new Double(3.14)));
        number.getChildren().add(newUISelectItem(new Double(49.99)));
        number.getChildren().add(newUISelectItem(new Long(12)));
        number.getChildren().add(newUISelectItem(new Double(-145.5)));
        number.decode(getFacesContext());
        number.validate(getFacesContext());
        number.updateModel(getFacesContext());
        assertNotNull(bean.getNumbers());
        try {
            DecimalFormat df = new DecimalFormat("'$'##.##", new DecimalFormatSymbols(Locale.US));
            expected = df.parse("$49.99");
        } catch (ParseException e) {
            assertTrue(e.getMessage(), false);
        }
        assertTrue(expected.equals(bean.getNumbers()[2]));

        // test model type of List of Strings
        UISelectMany stringList = new UISelectMany();
        stringList.setId("stringList");
        stringList.setRendererType("javax.faces.Checkbox");
        stringList.setValueExpression("value",
             getFacesContext().getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),"#{bean.stringList}", 
             Object.class));
        root.getChildren().add(stringList);
        stringList.getChildren().add(newUISelectItem("value1"));
        stringList.getChildren().add(newUISelectItem("value2"));
        stringList.getChildren().add(newUISelectItem("value3"));
        stringList.getChildren().add(newUISelectItem("value4"));
        stringList.decode(getFacesContext());
        stringList.validate(getFacesContext());
        stringList.updateModel(getFacesContext());
        assertNotNull(bean.getStringList());
        assertTrue(bean.getStringList().get(0).equals("value1"));
        assertTrue(bean.getStringList().get(1).equals("value2"));
        assertTrue(bean.getStringList().get(2).equals("value3"));

    }
    
    //
    // General Methods
    //

    public void testDateConverter(UIViewRoot root) throws ConverterException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        System.out.println("Testing DateConverter");
        UIInput text = new UIInput();
        text.setId("my_input_date");
        root.getChildren().add(text);

        Converter converter = null;
        converter = application.createConverter("javax.faces.DateTime");

        // date
        String stringToConvert = "Jan 1, 1967";
        Object obj = converter.getAsObject(getFacesContext(), text,
                                           stringToConvert);
        assertTrue(obj instanceof java.util.Date);
        String str = converter.getAsString(getFacesContext(), text, obj);
        // make sure we end up with the same string we started with..
        assertTrue(str.equals(stringToConvert));

        // time
        converter = application.createConverter("javax.faces.DateTime");
        ((DateTimeConverter) converter).setType("time");
        text = new UIInput();
        text.setId("my_input_time");
        stringToConvert = "10:10:10 AM";
        obj = converter.getAsObject(getFacesContext(), text, stringToConvert);
        assertTrue(obj instanceof java.util.Date);
        str = converter.getAsString(getFacesContext(), text, obj);
        // make sure we end up with the same string we started with..
        assertTrue(str.equals(stringToConvert));

        // datetime
        converter = application.createConverter("javax.faces.DateTime");
        ((DateTimeConverter) converter).setType("both");
        text = new UIInput();
        text.setId("my_input_datetime");
        stringToConvert = "Jan 1, 1967 10:10:10 AM";
        obj = converter.getAsObject(getFacesContext(), text, stringToConvert);
        assertTrue(obj instanceof java.util.Date);
        str = converter.getAsString(getFacesContext(), text, obj);
        // make sure we end up with the same string we started with..
        assertTrue(str.equals(stringToConvert));

        // test bogus type....
        boolean exceptionThrown = false;
        try {
            ((DateTimeConverter)converter).setType("foobar");
            obj = converter.getAsObject(getFacesContext(), text, stringToConvert);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        
        // test NullPointerException (if either context or component arg is null)
        exceptionThrown = false;
        try {
            obj = converter.getAsObject(null, text, stringToConvert);
        } catch (NullPointerException npe) {
            exceptionThrown= true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            obj = converter.getAsObject(getFacesContext(), null, stringToConvert);
        } catch (NullPointerException npe) {
            exceptionThrown= true;
        }
        assertTrue(exceptionThrown);
       
        exceptionThrown = false;
        try {
            str = converter.getAsString(null, text, obj);
        } catch (NullPointerException npe) {
            exceptionThrown= true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            str = converter.getAsString(getFacesContext(), null, obj);
        } catch (NullPointerException npe) {
            exceptionThrown= true;
        }
        assertTrue(exceptionThrown);
    }

    public void testDateTimeConverterGMTTimeZone() throws Exception {

        UIInput input = new UIInput();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date now = new Date();
        String formatted = df.format(now);
        Date parsed = df.parse(formatted);

        // control setup, now test the converter.
        Application app = getFacesContext().getApplication();
        DateTimeConverter converter = (DateTimeConverter) app.createConverter("javax.faces.DateTime");
        converter.setType("both");
        assertNotNull(converter);
        assertTrue(TimeZone.getTimeZone("GMT").toString(), TimeZone.getTimeZone("GMT").equals(converter.getTimeZone()));
        assertTrue(formatted, formatted.equals(converter.getAsString(getFacesContext(), input, parsed)));
        assertTrue(parsed.toString(), parsed.equals(converter.getAsObject(getFacesContext(), input, formatted)));

        app.addConverter(java.util.Date.class, "javax.faces.convert.DateTimeConverter");
        converter = (DateTimeConverter) app.createConverter(java.util.Date.class);
        converter.setType("both");
        assertNotNull(converter);
        assertTrue(TimeZone.getTimeZone("GMT").toString(), TimeZone.getTimeZone("GMT").equals(converter.getTimeZone()));
        assertTrue(formatted, formatted.equals(converter.getAsString(getFacesContext(), input, parsed)));
        assertTrue(parsed.toString(), parsed.equals(converter.getAsObject(getFacesContext(), input, formatted)));
    }


    public void testDateTimeConverterDefaultTimeZone() throws Exception {
        /*
         * This needs to be rewritten to NOT assume a specific Application implementation.
        TestingUtil.setPrivateField("passDefaultTimeZone",
                Application.class,                                    application,
                                    Boolean.TRUE);
        TestingUtil.setPrivateField("systemTimeZone",
                Application.class,                                    application,
                                    TimeZone.getDefault());
        UIInput input = new UIInput();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH);
        df.setTimeZone(TimeZone.getDefault());
        Date now = new Date();
        String formatted = df.format(now);
        Date parsed = df.parse(formatted);

        // control setup, now test the converter.
        Application app = getFacesContext().getApplication();
        DateTimeConverter converter = (DateTimeConverter) app.createConverter("javax.faces.DateTime");
        converter.setType("both");
        assertNotNull(converter);
        assertTrue(TimeZone.getDefault().toString(), TimeZone.getDefault().equals(converter.getTimeZone()));
        assertTrue(formatted, formatted.equals(converter.getAsString(getFacesContext(), input, parsed)));
        assertTrue(parsed.toString(), parsed.equals(converter.getAsObject(getFacesContext(), input, formatted)));

        app.addConverter(java.util.Date.class, "javax.faces.convert.DateTimeConverter");
        converter = (DateTimeConverter) app.createConverter(java.util.Date.class);
        converter.setType("both");
        assertNotNull(converter);
        assertTrue(TimeZone.getDefault().toString(), TimeZone.getDefault().equals(converter.getTimeZone()));
        assertTrue(formatted, formatted.equals(converter.getAsString(getFacesContext(), input, parsed)));
        assertTrue(parsed.toString(), parsed.equals(converter.getAsObject(getFacesContext(), input, formatted)));
         */
    }



    public void testNumberConverter(UIViewRoot root) throws ConverterException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        System.out.println("Tesing NumberConverter");
        UIInput text = new UIInput();
        text.setId("my_input_number");
        root.getChildren().add(text);

        Converter converter = application.createConverter("javax.faces.Number");

        String stringToConvert = "99.9";
        Object obj = converter.getAsObject(getFacesContext(), text,
                                           stringToConvert);
        assertTrue(obj instanceof java.lang.Number);
        String str = converter.getAsString(getFacesContext(), text, obj);
        assertTrue(str.equals(stringToConvert));

    }

    public void testNumberConverterSpacesNBSP() throws Exception {
        UIInput text = new UIInput();
        NumberConverter converter = (NumberConverter) application.createConverter("javax.faces.Number");
        converter.setType("currency");
        converter.setLocale(Locale.FRANCE);
        String toConv = "12 345,68 " + '\u20aC';
        Number number = (Number) converter.getAsObject(getFacesContext(),
                                                       text,
                                                       toConv);
        assertTrue(number != null);

        converter.setType("number");
        toConv = "5" + "\u00a0" + "000" + "\u00a0" + "000,01";
        number = (Number) converter.getAsObject(getFacesContext(),
                                                text,
                                                toConv);
        assertTrue("Number was: " + number.toString() + ", expected 5000000.01",
                   "5000000.01".equals(number.toString()));
    }

    public void testDoubleConverter(UIViewRoot root) throws ConverterException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        System.out.println("Tesing DoubleConverter");
        Converter converter = application.createConverter("javax.faces.Double");
        assertNotNull(converter);
    }


    public void testBooleanConverter(UIViewRoot root)
        throws ConverterException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        System.out.println("Tesing BooleanConverter");
        UIInput text = new UIInput();
        text.setId("my_input_boolean");
        root.getChildren().add(text);

        Converter converter = application.createConverter(
            java.lang.Boolean.class);

        String stringToConvert = "true";
        Object obj = converter.getAsObject(getFacesContext(), text,
                                           stringToConvert);
        assertTrue(obj instanceof java.lang.Boolean);

        String str = converter.getAsString(getFacesContext(), text, obj);

        assertTrue(str.equals(stringToConvert));

    }


    /**
     * Test to verify that a class that registers itself is properly found
     * using the search mechanism. The J2SE classes are used for their
     * inheritance hierarchy to test that converters registered to
     * interfaces and superclasses are properly found.
     * <p/>
     * This test is meant for inheritance lookup only.
     */
    public void testConverterInheritance(UIViewRoot root)
        throws ConverterException,
        InstantiationException, IllegalAccessException, ClassNotFoundException {
        System.out.println("Testing ConverterInheritance");

        Converter converter;
        UIInput text = new UIInput();
        text.setId("my_date_converter");
        root.getChildren().add(text);

        //java.lang.Integer extends java.lang.Number. 
        //Test to show that the standard converter registered to 
        //java.lang.Integer should chosen over the inherited 
        //java.lang.Number converter
        application.addConverter(java.lang.Number.class,
                                 "javax.faces.convert.NumberConverter");
        converter = application.createConverter(java.lang.Integer.class);
        assertTrue(converter != null);
        assertTrue(converter instanceof javax.faces.convert.IntegerConverter);


        //java.sql.Date extends java.util.Date
        //Test to find converter registered to java.util.Date
        application.addConverter(java.util.Date.class,
                                 "javax.faces.convert.DateTimeConverter");
        converter = null;
        converter = application.createConverter(java.sql.Date.class);
        assertTrue(converter != null);

        //java.util.HashSet is a subclass of java.util.AbstractSet which is
        //a subclass of java.util.AbstractCollection 
        //Test to find the converter registered to java.util.AbstractCollection
        application.addConverter(java.util.AbstractCollection.class,
                                 "javax.faces.convert.DateTimeConverter");
        converter = null;
        try {
            converter = application.createConverter(java.util.HashSet.class);
        } catch (javax.faces.FacesException fe) {
            
        }
        assertTrue(converter != null);


        //java.lang.String implements java.lang.CharSequence
        //Test to find the converter registered to java.lang.CharSequence
        application.addConverter(java.text.CharacterIterator.class,
                                 "javax.faces.convert.CharacterConverter");
        converter = null;
        converter =
            application.createConverter(
                java.text.StringCharacterIterator.class);
        assertTrue(converter != null);

        //java.text.StringCharacterIterator implements 
        //java.text.CharacterIterator which has a super-interface
        //java.lang.Cloneable
        //Test to find the converter registered to java.lang.Cloneable
        application.addConverter(java.lang.Cloneable.class,
                                 "javax.faces.convert.CharacterConverter");
        converter = null;
        converter =
            application.createConverter(
                java.text.StringCharacterIterator.class);
        assertTrue(converter != null);
    }


    static private UISelectItem newUISelectItem(Object value) {
        UISelectItem item = new UISelectItem();
        item.setItemValue(value);
        item.setItemLabel(value.toString());
        return item;
    }

} // end of class TestConverters
