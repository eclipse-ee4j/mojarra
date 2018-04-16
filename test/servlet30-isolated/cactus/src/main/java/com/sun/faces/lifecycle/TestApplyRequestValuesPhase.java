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

// TestApplyRequestValuesPhase.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.ServletFacesTestCase;
import org.apache.cactus.WebRequest;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UICommand;

/**
 * <B>TestApplyRequestValuesPhase</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestApplyRequestValuesPhase extends ServletFacesTestCase {

//
// Protected Constants
//

    public static final String TEST_URI = "/components.jsp";

//
// Class Variables
//

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestApplyRequestValuesPhase() {
        super("TestApplyRequestValuesPhase");
	initLocalHostPath();
    }


    public TestApplyRequestValuesPhase(String name) {
        super(name);
	initLocalHostPath();
    }

    private String localHostPath = "localhost:8080";

    private void initLocalHostPath() {
	String containerPort = System.getProperty("container.port");
	if (null == containerPort || 0 == containerPort.length()) {
	    containerPort = "8080";
	}
	localHostPath = "localhost:" + containerPort;
    }



//
// Class methods
//

//
// General Methods
//

    public void beginCallback(WebRequest theRequest) {
        theRequest.setURL(localHostPath, null, null, TEST_URI, null);
        theRequest.addParameter(
            "basicForm" + NamingContainer.SEPARATOR_CHAR + "userName", "jerry");
         theRequest.addParameter(
            "basicForm" + NamingContainer.SEPARATOR_CHAR + "testCmd", "submit");
          theRequest.addParameter(
            "basicForm" + NamingContainer.SEPARATOR_CHAR + "testInt", "10");
        theRequest.addParameter("basicForm", "basicForm");

    }


    public void testCallback() {
        UIComponent root = null;
        String value = null;
        Phase
            restoreView = new RestoreViewPhase(),
            applyValues = new ApplyRequestValuesPhase();

        // 1. Set the root of the view ...
        //
	root = getFacesContext().getApplication().getViewHandler().createView(getFacesContext(), TEST_URI);
	getFacesContext().setViewRoot((UIViewRoot) root);
	getFacesContext().renderResponse();

        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));
        assertTrue(null != getFacesContext().getViewRoot());

        // 2. Add components to tree
        //
        root = getFacesContext().getViewRoot();
        UIForm basicForm = new UIForm();
        basicForm.setId("basicForm");
        UIInput userName = new UIInput();
        userName.setId("userName");
        root.getChildren().add(basicForm);
        basicForm.getChildren().add(userName);

        // 3. Apply values
        //
        applyValues.execute(getFacesContext());
        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        root = getFacesContext().getViewRoot();
        try {
            userName = (UIInput) basicForm.findComponent("userName");
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            assertTrue("Can't find userName in tree", false);
        }
        assertTrue(null != userName);
        assertTrue(null != (value = (String) userName.getSubmittedValue()));
        assertTrue(value.equals("jerry"));
        
        testImmediate(basicForm);
    }
    
    public void testImmediate(UIForm basicForm) {
        
        Phase
            restoreView = new RestoreViewPhase(),
            applyValues = new ApplyRequestValuesPhase();

        
         // add a UICommand with "immediate" attribute set
        UICommand testCmd = new UICommand();
        testCmd.setId("testCmd");
        testCmd.setImmediate(true);
        basicForm.getChildren().add(testCmd);
        
        //verify immediate attribute works correctly.
        System.out.println("Testing 'immediate' attribute on UIInput and UICommand");
        UIInput testInt = new UIInput();
        testInt.setConverter(new javax.faces.convert.IntegerConverter());
        testInt.setRequired(true);
        testInt.setId("testInt");
        testInt.setImmediate(true);
        basicForm.getChildren().add(testInt); 
        
        // 3. Apply values
        //
        Integer testNumber = new Integer(10);
        applyValues.execute(getFacesContext());
        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        UIComponent root = getFacesContext().getViewRoot();
        try {
            testInt = (UIInput) basicForm.findComponent("testInt");
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            assertTrue("Can't find testInt in tree", false);
        }
        
        //make sure the value is converted and validated after Apply request 
        // values phase.
        assertTrue(null != testInt);
        assertTrue(null != testInt.getLocalValue());
        assertTrue(testInt.isValid());
        assertTrue(testNumber.equals((Integer) testInt.getValue()));
        testInt.setValue(null);
        
        // immediate "false" on command button but set on UIInput
        testCmd.setImmediate(false);
        applyValues.execute(getFacesContext());
        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        root = getFacesContext().getViewRoot();
        try {
            testInt = (UIInput) basicForm.findComponent("testInt");
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            assertTrue("Can't find testInt in tree", false);
        }
        
        //make sure the value is converted and validated after Apply request 
        // values phase.
        assertTrue(null != testInt);
        assertTrue(null != testInt.getLocalValue());
        assertTrue(testInt.isValid());
        assertTrue(testNumber.equals((Integer) testInt.getValue()));
        testInt.setValue(null);
        
        // immediate "true" on command and not set on UIInput.
        testInt.setImmediate(false);
        testCmd.setImmediate(true);
        applyValues.execute(getFacesContext());
        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        root = getFacesContext().getViewRoot();
        try {
            testInt = (UIInput) basicForm.findComponent("testInt");
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            assertTrue("Can't find testInt in tree", false);
        }
        
        //make sure the value is converted and validated after Apply request 
        // values phase.
        assertTrue(null != testInt);
        assertTrue(null == testInt.getValue());
        assertTrue(testInt.isValid());
    }


} // end of class TestApplyRequestValuesPhase
