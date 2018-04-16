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

package com.sun.faces.application;

import com.sun.faces.cactus.ServletFacesTestCase;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;


/**
 * This class tests the <code>StateManagerImpl</code> class
 * functionality.
 */
public class TestHAStateManagerImpl extends ServletFacesTestCase {

     public static final String TEST_URI = "/test.jsp";
    //
    // Constructors/Initializers
    //
    public TestHAStateManagerImpl() {
        super("TestStateManagerImpl");
    }


    public TestHAStateManagerImpl(String name) {
        super(name);
    }
    
    private Application application = null;
    
    public void setUp() {
        super.setUp();
        ApplicationFactory aFactory =
            (ApplicationFactory) FactoryFinder.getFactory(
                FactoryFinder.APPLICATION_FACTORY);
        application = (Application) aFactory.getApplication();
        application.setViewHandler(new ViewHandlerImpl());
        application.setStateManager(new StateManagerImpl());
    }
    
    //
    // Test Methods
    //
    
    
    public void testHighAvailabilityStateSaving1() {
       
//        // precreate tree and set it in session and make sure the tree is
//        // restored from session.
//        UIViewRoot root = application.getViewHandler().createView(getFacesContext(), null);
//        root.setViewId(TEST_URI);
//
//        UIForm basicForm = new UIForm();
//        basicForm.setId("basicForm");
//        UIInput userName = new UIInput();
//
//        userName.setId("userName");
//        userName.setTransient(true);
//        root.getChildren().add(basicForm);
//        basicForm.getChildren().add(userName);
//
//        UIPanel panel1 = new UIPanel();
//        panel1.setId("panel1");
//        basicForm.getChildren().add(panel1);
//
//        UIInput userName1 = new UIInput();
//        userName1.setId("userName1");
//        panel1.getChildren().add(userName1);
//
//        getFacesContext().setViewRoot(root);
//
//        StateManager stateManager =
//            getFacesContext().getApplication().getStateManager();
//        
//        SerializedView state = stateManager.saveSerializedView(getFacesContext());
//        
//        // make sure that the value of viewId attribute in session is an
//        // instance of SerializedView.
//        Object result = session.getAttribute(TEST_URI);
//        assertTrue(result instanceof SerializedView);
//        
//        root = stateManager.restoreView(getFacesContext(), TEST_URI,
//                                 RenderKitFactory.HTML_BASIC_RENDER_KIT);
//       
//        assertTrue(root != null);
//        basicForm = (UIForm) root.findComponent("basicForm");
//        assertTrue(basicForm != null);
//
//        userName = (UIInput) basicForm.findComponent("userName");
//        assertTrue(userName == null);
//
//        panel1 = (UIPanel) basicForm.findComponent("panel1");
//        assertTrue(panel1 != null);
//
//        userName1 = (UIInput) panel1.findComponent("userName1");
//        assertTrue(userName1 != null);
    }

}
