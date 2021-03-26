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

// TestRenderers_3.java

package com.sun.faces.renderkit.html_basic;

import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.RIConstants;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContextFactory;
import javax.faces.convert.Converter;
import javax.faces.convert.NumberConverter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;

/**
 * Test encode and decode methods in Renderer classes.
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestRenderers_3 extends JspFacesTestCase {
    
    //
    // Instance Variables
    //
    private Application application;
    
    //
    // Protected Constants
    //
    public static String DATE_STR = "Jan 12, 1952";
    
    public static String NUMBER_STR = "47%";
    
    
    public boolean sendWriterToFile() {
        return true;
    }
    
    
    public String getExpectedOutputFilename() {
        return "CorrectRenderersResponse_3";
    }
    
    //
    // Class Variables
    //
    
    //
    // Instance Variables
    //
    private FacesContextFactory facesContextFactory = null;
    
    // Attribute Instance Variables
    // Relationship Instance Variables
    //
    // Constructors and Initializers
    //
    
    public TestRenderers_3() {
        super("TestRenderers_3");
    }
    
    
    public TestRenderers_3(String name) {
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
        UIViewRoot xmlTree = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        xmlTree.setViewId("viewId");
        xmlTree.setLocale(Locale.US);
        xmlTree.getChildren().add(new UICommand());
        getFacesContext().setViewRoot(xmlTree);
        Object view =
                Util.getStateManager(getFacesContext()).saveSerializedView(getFacesContext());
        getFacesContext().getExternalContext().getRequestMap().put(RIConstants.SAVED_STATE, view);
        assertTrue(null != getFacesContext().getResponseWriter());
    }
    
    
    public void beginRenderers(WebRequest theRequest) {
        theRequest.addParameter("myMenu", "Blue");
        theRequest.addParameter("myListbox", "Blue");
        theRequest.addParameter("myCheckboxlist", "Blue");
        theRequest.addParameter("myOnemenu", "Blue");
        // parameters to test hidden renderer
        theRequest.addParameter("myNumberHidden", NUMBER_STR);
        theRequest.addParameter("myInputDateHidden", DATE_STR);
        
    }
    
    
    public void testRenderers() {
        
        try {
            // create a dummy root for the tree.
            UIViewRoot root = getFacesContext().getViewRoot();
            root.setId("root");
            
            testSelectManyMenuRenderer(root);
            testSelectManyListboxRenderer(root);
            testSelectManyCheckboxListRenderer(root);
            testSelectOneMenuRenderer(root);
            testHiddenRenderer(root);
            assertTrue(verifyExpectedOutput());
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue(false);
            return;
        }
    }
    
    
    public void testSelectManyListboxRenderer(UIComponent root)
    throws IOException {
        System.out.println("Testing SelectManyListboxRenderer");
        UISelectMany selectMany = new UISelectMany();
        UISelectItems uiSelectItems = new UISelectItems();
        selectMany.setValue(null);
        selectMany.setId("myListbox");
        SelectItem item1 = new SelectItem("Red", "Red", null);
        SelectItem item2 = new SelectItem("Blue", "Blue", null);
        
        SelectItem item3 = new SelectItem("Green", "Green", null);
        SelectItem item4 = new SelectItem("Yellow", "Yellow", null);
        SelectItem[] itemsArray = {item3, item4};
        SelectItemGroup itemGroup = new SelectItemGroup("group", null, true,
                itemsArray);
        SelectItem[] selectItems = {item1, item2, itemGroup};
        Object selectedValues[] = null;
        uiSelectItems.setValue(selectItems);
        uiSelectItems.setId("manyListitems");
        selectMany.getChildren().add(uiSelectItems);
        root.getChildren().add(selectMany);
        
        ListboxRenderer selectManyListboxRenderer =
                new ListboxRenderer();
        
        // test decode method
        System.out.println("    Testing decode method... ");
        selectManyListboxRenderer.decode(getFacesContext(), selectMany);
        selectedValues = (Object[]) selectMany.getSubmittedValue();
        assertTrue(null != selectedValues);
        assertTrue(1 == selectedValues.length);
        assertTrue(((String) selectedValues[0]).equals("Blue"));
        
        // test convert method
        Object[] convertedValues =
                (Object[]) selectManyListboxRenderer.getConvertedValue(
                getFacesContext(),
                selectMany,
                selectMany.getSubmittedValue());
        assertTrue(null != convertedValues);
        assertTrue(1 == convertedValues.length);
        assertTrue(((String) convertedValues[0]).equals("Blue"));
        
        // test encode method
        
        System.out.println("    Testing encode method... ");
        selectManyListboxRenderer.encodeBegin(getFacesContext(), selectMany);
        selectManyListboxRenderer.encodeEnd(getFacesContext(), selectMany);
        getFacesContext().getResponseWriter().writeText("\n", null);
        getFacesContext().getResponseWriter().flush();
        
    }
    
    
    public void testSelectManyCheckboxListRenderer(UIComponent root)
    throws IOException {
        System.out.println("Testing SelectManyCheckboxListRenderer");
        UISelectMany selectMany = new UISelectMany();
        selectMany.getAttributes().put("enabledClass", "enabledClass");
        selectMany.getAttributes().put("disabledClass", "disabledClass");
        selectMany.getAttributes().put("styleClass", "styleClass");
        selectMany.getAttributes().put("tabindex", new Integer(5));
        selectMany.getAttributes().put("title", "title");
        
        UISelectItems uiSelectItems = new UISelectItems();
        selectMany.setValue(null);
        selectMany.setId("myCheckboxlist");
        SelectItem item1 = new SelectItem("Red", "Red", null);
        item1.setDisabled(true);
        SelectItem item2 = new SelectItem("Blue", "Blue", null);
        
        SelectItem item3 = new SelectItem("Green", "Green", null);
        SelectItem item4 = new SelectItem("Yellow", "Yellow", null);
        SelectItem[] itemsArray = {item3, item4};
        SelectItemGroup itemGroup = new SelectItemGroup("group", null, true,
                itemsArray);
        SelectItem[] selectItems = {item1, item2, itemGroup};
        Object selectedValues[] = null;
        uiSelectItems.setValue(selectItems);
        selectMany.getChildren().add(uiSelectItems);
        root.getChildren().add(selectMany);
        
        SelectManyCheckboxListRenderer selectManyCheckboxListRenderer =
                new SelectManyCheckboxListRenderer();
        
        // test decode method
        
        System.out.println("    Testing decode method... ");
        selectManyCheckboxListRenderer.decode(getFacesContext(), selectMany);
        selectedValues = (Object[]) selectMany.getSubmittedValue();
        assertTrue(null != selectedValues);
        assertTrue(1 == selectedValues.length);
        assertTrue(((String) selectedValues[0]).equals("Blue"));
        
        // test convert method
        Object[] convertedValues =
                (Object[]) selectManyCheckboxListRenderer.getConvertedValue(
                getFacesContext(),
                selectMany,
                selectMany.getSubmittedValue());
        assertTrue(null != convertedValues);
        assertTrue(1 == convertedValues.length);
        assertTrue(((String) convertedValues[0]).equals("Blue"));
        
        
        // test encode method
        System.out.println("    Testing encode method... ");
        selectManyCheckboxListRenderer.encodeBegin(getFacesContext(),
                selectMany);
        selectManyCheckboxListRenderer.encodeEnd(getFacesContext(),
                selectMany);
        getFacesContext().getResponseWriter().writeText("\n", null);
        getFacesContext().getResponseWriter().flush();
        
    }
    
    
    public void testSelectManyMenuRenderer(UIComponent root)
    throws IOException {
        System.out.println("Testing SelectManyMenuRenderer");
        UISelectMany selectMany = new UISelectMany();
        UISelectItems uiSelectItems = new UISelectItems();
        selectMany.setValue(null);
        selectMany.setId("myMenu");
        SelectItem item1 = new SelectItem("Red", "Red", null);
        SelectItem item2 = new SelectItem("Blue", "Blue", null);
        SelectItem item3 = new SelectItem("Green", "Green", null);
        SelectItem item4 = new SelectItem("Yellow", "Yellow", null);
        SelectItem[] selectItems = {item1, item2, item3, item4};
        Object selectedValues[] = null;
        uiSelectItems.setValue(selectItems);
        uiSelectItems.setId("manyMenuitems");
        selectMany.getChildren().add(uiSelectItems);
        root.getChildren().add(selectMany);
        
        MenuRenderer selectManyMenuRenderer =
                new MenuRenderer();
        
        // test decode method
        System.out.println("    Testing decode method... ");
        selectManyMenuRenderer.decode(getFacesContext(), selectMany);
        selectedValues = (Object[]) selectMany.getSubmittedValue();
        assertTrue(null != selectedValues);
        assertTrue(1 == selectedValues.length);
        assertTrue(((String) selectedValues[0]).equals("Blue"));
        
        // test convert method
        Object[] convertedValues =
                (Object[]) selectManyMenuRenderer.getConvertedValue(
                getFacesContext(),
                selectMany,
                selectMany.getSubmittedValue());
        assertTrue(null != convertedValues);
        assertTrue(1 == convertedValues.length);
        assertTrue(((String) convertedValues[0]).equals("Blue"));
        
        // test encode method
        System.out.println("    Testing encode method... ");
        selectManyMenuRenderer.encodeBegin(getFacesContext(), selectMany);
        selectManyMenuRenderer.encodeEnd(getFacesContext(), selectMany);
        getFacesContext().getResponseWriter().writeText("\n", null);
        getFacesContext().getResponseWriter().flush();
        
    }
    
    
    public void testSelectOneMenuRenderer(UIComponent root)
    throws IOException {
        System.out.println("Testing SelectOneMenuRenderer");
        UISelectOne selectOne = new UISelectOne();
        UISelectItems uiSelectItems = new UISelectItems();
        selectOne.setValue(null);
        selectOne.setId("myOnemenu");
        SelectItem item1 = new SelectItem("Red", "Red", null);
        SelectItem item2 = new SelectItem("Blue", "Blue", null);
        SelectItem item3 = new SelectItem("Green", "Green", null);
        SelectItem item4 = new SelectItem("Yellow", "Yellow", null);
        SelectItem[] selectItems = {item1, item2, item3, item4};
        String selectedValue = null;
        uiSelectItems.setValue(selectItems);
        uiSelectItems.setId("manySelectOneitems");
        selectOne.getChildren().add(uiSelectItems);
        root.getChildren().add(selectOne);
        
        MenuRenderer selectOneMenuRenderer =
                new MenuRenderer();
        
        // test decode method
        System.out.println("    Testing decode method... ");
        selectOneMenuRenderer.decode(getFacesContext(), selectOne);
        assertTrue("Blue".equals(selectOne.getSubmittedValue()));
        
        // test convert method
        Object value = selectOneMenuRenderer.getConvertedValue(
                getFacesContext(),
                selectOne,
                selectOne.getSubmittedValue());
        assertTrue("Blue".equals(value));
        
        // test encode method
        System.out.println("    Testing encode method... ");
        selectOneMenuRenderer.encodeBegin(getFacesContext(), selectOne);
        selectOneMenuRenderer.encodeEnd(getFacesContext(), selectOne);
        getFacesContext().getResponseWriter().writeText("\n", null);
        getFacesContext().getResponseWriter().flush();
        
    }
    
    
    public void testHiddenRenderer(UIComponent root) throws IOException {
        System.out.println("Testing Input_DateRenderer");
        UIInput input1 = new UIInput();
        input1.setValue(null);
        input1.setId("myInputDateHidden");
        Converter converter = application.createConverter(
                "jakarta.faces.DateTime");
        input1.setConverter(converter);
        input1.getAttributes().put("dateStyle", "medium");
        root.getChildren().add(input1);
        HiddenRenderer hiddenRenderer = new HiddenRenderer();
        
        DateFormat dateformatter =
                DateFormat.getDateInstance(DateFormat.MEDIUM,
                getFacesContext().getViewRoot()
                .getLocale());
        dateformatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        // test hidden renderer with converter set to date
        // test decode method
        System.out.println("    Testing decode method...");
        hiddenRenderer.decode(getFacesContext(), input1);
        Date date = (Date) hiddenRenderer.getConvertedValue(getFacesContext(),
                input1,
                input1.getSubmittedValue());
        assertTrue(null != date);
        assertTrue(DATE_STR.equals(dateformatter.format(date)));
        
        // test encode method
        System.out.println("    Testing encode method...");
        hiddenRenderer.encodeBegin(getFacesContext(), input1);
        hiddenRenderer.encodeEnd(getFacesContext(), input1);
        getFacesContext().getResponseWriter().flush();
        
        // test hidden renderer with converter set to number
        UIInput input2 = new UIInput();
        input2.setValue(null);
        input2.setId("myNumberHidden");
        converter = application.createConverter("jakarta.faces.Number");
        ((NumberConverter) converter).setType("percent");
        input2.setConverter(converter);
        root.getChildren().add(input2);
        
        NumberFormat numberformatter =
                NumberFormat.getPercentInstance(getFacesContext().
                getViewRoot().getLocale());
        // test decode method
        System.out.println("    Testing decode method...");
        hiddenRenderer.decode(getFacesContext(), input2);
        Number number = (Number) hiddenRenderer.getConvertedValue(
                getFacesContext(),
                input2,
                input2.getSubmittedValue());
        
        assertTrue(null != number);
        System.out.println("NUMBER_STR:" + NUMBER_STR);
        System.out.println("NUMBERFORMATTER:" + numberformatter.format(number));
        assertTrue(NUMBER_STR.equals(numberformatter.format(number)));
        
        // test encode method
        System.out.println("    Testing encode method...");
        hiddenRenderer.encodeBegin(getFacesContext(), input2);
        hiddenRenderer.encodeEnd(getFacesContext(), input2);
        getFacesContext().getResponseWriter().flush();
        
    }
} // end of class TestRenderers_3
