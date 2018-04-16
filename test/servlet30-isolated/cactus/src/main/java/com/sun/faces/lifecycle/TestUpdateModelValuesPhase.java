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

// TestUpdateModelValuesPhase.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.context.ExceptionHandlerImpl;
import com.sun.faces.el.ELUtils;
import com.sun.faces.util.Util;

import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import java.util.Locale;

/**
 * <B>TestUpdateModelValuesPhase</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestUpdateModelValuesPhase extends ServletFacesTestCase {

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

//
// Constructors and Initializers    
//

    public TestUpdateModelValuesPhase() {
        super("TestUpdateModelValuesPhase");
    }


    public TestUpdateModelValuesPhase(String name) {
        super(name);
    }

//
// Class methods
//

//
// General Methods
//

    public void testUpdateNormal() {
////DebugUtil.waitForDebugger();
//        UIForm form = null;
//        TestUIInput userName = null;
//        TestUIInput userName1 = null;
//        TestUIInput userName2 = null;
//        com.sun.faces.cactus.TestBean testBean = (com.sun.faces.cactus.TestBean)
//            (getFacesContext().getExternalContext().getSessionMap()).get(
//                "TestBean");
//        String value = null;
//        Phase updateModelValues = new UpdateModelValuesPhase();
//        form = new UIForm();
//        form.setId("form");
//        form.setSubmitted(true);
//        userName = new TestUIInput();
//        userName.setId("userName");
//        userName.setValue("one");
//        userName.setValueExpression("value",
//                                 ELUtils.createValueExpression("#{TestBean.one}"));
//        userName.testSetValid(true);
//        form.getChildren().add(userName);
//        userName1 = new TestUIInput();
//        userName1.setId("userName1");
//        userName1.setValue("one");
//        userName1.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.one}"));
//        userName1.testSetValid(true);
//        form.getChildren().add(userName1);
//        userName2 = new TestUIInput();
//        userName2.setId("userName2");
//        userName2.setValue("one");
//        userName2.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.one}"));
//        userName2.testSetValid(true);
//        form.getChildren().add(userName2);
//
//        UIViewRoot viewRoot = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
//        viewRoot.setLocale(Locale.US);
//        viewRoot.getChildren().add(form);
//        viewRoot.setViewId("updateModel.xul");
//        getFacesContext().setViewRoot(viewRoot);
//
//        try {
//            updateModelValues.execute(getFacesContext());
//        } catch (Throwable e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//        assertTrue(!(getFacesContext().getRenderResponse()) &&
//                   !(getFacesContext().getResponseComplete()));
//        Object localvalue = userName.getLocalValue();
//        assertTrue(localvalue == null);
//
//        assertTrue(testBean.getOne().equals("one"));
//        assertTrue(!getFacesContext().getMessages().hasNext());
    }


    public void testUpdateFailed() {
//        UIForm form = null;
//        TestUIInput userName = null;
//        TestUIInput userName1 = null;
//        TestUIInput userName2 = null;
//        TestUIInput userName3 = null;
//        String value = null;
//        Phase
//            updateModelValues = new UpdateModelValuesPhase();
//        form = new UIForm();
//        form.setId("form");
//        form.setSubmitted(true);
//        userName = new TestUIInput();
//        userName.setId("userName");
//        userName.setValue("one");
//        userName.testSetValid(true);
//        userName.setValueExpression("value",
//                                 ELUtils.createValueExpression("#{TestBean.two}"));
//        form.getChildren().add(userName);
//        userName1 = new TestUIInput();
//        userName1.setId("userName1");
//        userName1.setValue("one");
//        userName1.testSetValid(true);
//        userName1.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.one}"));
//        form.getChildren().add(userName1);
//        userName2 = new TestUIInput();
//        userName2.setId("userName2");
//        userName2.setValue("one");
//        userName2.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.one}"));
//        userName2.testSetValid(true);
//        form.getChildren().add(userName2);
//        userName3 = new TestUIInput();
//        userName3.setId("userName3");
//        userName3.setValue("four");
//        userName3.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.four}"));
//        userName3.testSetValid(true);
//        form.getChildren().add(userName3);
//
//        UIViewRoot viewRoot = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
//        viewRoot.setLocale(Locale.US);
//        viewRoot.getChildren().add(form);
//        viewRoot.setViewId("updateModel.xul");
//        getFacesContext().setViewRoot(viewRoot);
//
//        // This stage will go to render, since there was at least one error
//        // during component updates...
//        try {
//            updateModelValues.execute(getFacesContext());
//        } catch (Throwable e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//        getFacesContext().getExceptionHandler().handle();
//
//        assertTrue(getFacesContext().getRenderResponse());
//
//        assertTrue(true == (getFacesContext().getMessages().hasNext()));
//
//        //assertions for our default update failed message
//        assertTrue(true == (getFacesContext().getMessages("form:userName3").hasNext()));
//        java.util.Iterator iter = getFacesContext().getMessages("form:userName3");
//        javax.faces.application.FacesMessage msg = null;
//        javax.faces.application.FacesMessage expectedMsg = 
//            com.sun.faces.util.MessageFactory.getMessage(getFacesContext(), "javax.faces.component.UIInput.UPDATE",
//            new Object[] {com.sun.faces.util.MessageFactory.getLabel(getFacesContext(), userName3)}); 
//        while (iter.hasNext()) {
//            msg = (javax.faces.application.FacesMessage)iter.next();
//        }    
//        assertTrue(msg.getSummary().equals(expectedMsg.getSummary()));
    }

    public void testUpdateFailed2() {
//        UIForm form = null;
//        TestUIInput userName = null;
//        TestUIInput userName1 = null;
//        TestUIInput userName2 = null;
//        TestUIInput userName3 = null;
//        String value = null;
//        Phase
//            updateModelValues = new UpdateModelValuesPhase();
//        form = new UIForm();
//        form.setId("form");
//        form.setSubmitted(true);
//        userName = new TestUIInput();
//        userName.setId("userName");
//        userName.setValue("one");
//        userName.testSetValid(true);
//        userName.setValueExpression("value",
//                                 ELUtils.createValueExpression("#{TestBean.two}"));
//        form.getChildren().add(userName);
//        userName1 = new TestUIInput();
//        userName1.setId("userName1");
//        userName1.setValue("one");
//        userName1.testSetValid(true);
//        userName1.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.one}"));
//        form.getChildren().add(userName1);
//        userName2 = new TestUIInput();
//        userName2.setId("userName2");
//        userName2.setValue("one");
//        userName2.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.one}"));
//        userName2.testSetValid(true);
//        form.getChildren().add(userName2);
//        userName3 = new TestUIInput();
//        userName3.setId("userName3");
//        userName3.setValue("four");
//        userName3.setValueExpression("value",
//                                  ELUtils.createValueExpression("#{TestBean.four}"));
//        userName3.testSetValid(true);
//        form.getChildren().add(userName3);
//
//        UIViewRoot viewRoot = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
//        viewRoot.setLocale(Locale.US);
//        viewRoot.getChildren().add(form);
//        viewRoot.setViewId("updateModel.xul");
//        getFacesContext().setViewRoot(viewRoot);
//
//        getFacesContext().setExceptionHandler(new ExceptionHandlerImpl());
//
//
//        // This stage will go to render, since there was at least one error
//        // during component updates...
//        try {
//            updateModelValues.execute(getFacesContext());
//        } catch (Throwable e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//        
//        boolean exceptionThrown = false;
//        try {
//            getFacesContext().getExceptionHandler().handle();
//        } catch (Throwable t) {
//            exceptionThrown = true;
//        }
//
//        assertTrue(exceptionThrown);
//
//        assertTrue(false == (getFacesContext().getMessages().hasNext()));
    }    

    public static class TestUIInput extends UIInput {

        public void testSetValid(boolean validState) {
            this.setValid(validState);
        }

    }

} // end of class TestUpdateModelValuesPhase
