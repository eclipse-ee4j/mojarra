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

// TestSaveStateInPage.java

package com.sun.faces.lifecycle;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.apache.cactus.WebRequest;

import com.sun.faces.application.StateManagerImpl;
import com.sun.faces.application.ViewHandlerImpl;
import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.cactus.TestingUtil;
import com.sun.faces.util.Util;


/**
 * <B>TestSaveStateInPage</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestSaveStateInPage extends JspFacesTestCase {

//
// Protected Constants
//

    public static final String TEST_URI = "/greeting.jsp";


    public String getExpectedOutputFilename() {
        return "SaveState_correct";
    }


    public static final String ignore[] = {
        "<form id=\"helloForm\" method=\"post\" action=\"/test/faces/greeting.jsp;jsessionid=09AF72F7E5EA209865AFFAB72D0F7B33\">"
    };


    public String[] getLinesToIgnore() {
        return ignore;
    }


    public boolean sendResponseToFile() {
        return true;
    }

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

    public TestSaveStateInPage() {
        super("TestRenderResponsePhase");
	initLocalHostPath();
    }


    public TestSaveStateInPage(String name) {
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


    public void beginSaveStateInPage(WebRequest theRequest) {
//        theRequest.setURL(localHostPath, null, null, TEST_URI, null);
    }


    public void testSaveStateInPage() {
//
//        boolean result = false;
//        UIComponentBase root = null;
//        String value = null;
//        Phase renderResponse = new RenderResponsePhase();
//        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
//        page.setId("root");
//        page.setLocale(Locale.US);
//        page.setViewId(TEST_URI);
//        getFacesContext().setViewRoot(page);
//
//        renderResponse.execute(getFacesContext());
//        assertTrue(!(getFacesContext().getRenderResponse()) &&
//                   !(getFacesContext().getResponseComplete()));
//
//        assertTrue(verifyExpectedOutput());
    }


    public void testSaveStateInClient() {
//        // PENDING (visvan) add test case to make sure no state is saved when
//        // root is marked transient.
//        // precreate tree and set it in session and make sure the tree is
//        // restored from session.
//        //getFacesContext().setViewRoot(null);
//        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
//        root.setLocale(Locale.US);
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
//        userName1.setTransient(true);
//        panel1.getChildren().add(userName1);
//
//        UIInput userName2 = new UIInput();
//        userName2.setId("userName2");
//        panel1.getChildren().add(userName2);
//
//        UIInput userName3 = new UIInput();
//        userName3.setTransient(true);
//        panel1.getFacets().put("userName3", userName3);
//
//        UIInput userName4 = new UIInput();
//        panel1.getFacets().put("userName4", userName4);
//
//        getFacesContext().setViewRoot(root);
//
//        ViewHandlerImpl viewHandler = new ViewHandlerImpl();
//        StateManagerImpl stateManager = new StateManagerImpl();
//
//        TreeStructure structRoot =
//            new TreeStructure(((UIComponent) getFacesContext().getViewRoot()));
//        stateManager.buildTreeStructureToSave(getFacesContext(),
//                                              ((UIComponent) root),
//                                              structRoot, null);
//
//        // make sure restored tree structure is correct
//        UIViewRoot viewRoot = (UIViewRoot) structRoot.createComponent();
//        assertTrue(null != viewRoot);
//        stateManager.restoreComponentTreeStructure(structRoot,
//                                                   ((UIComponent) viewRoot));
//
//        List structureList = new ArrayList();
//        TestingUtil.invokePrivateMethod("captureChild",
//                                        new Class[] {List.class, Integer.TYPE, UIComponent.class},
//                                        new Object[] { structureList, 0, root },
//                                        StateManagerImpl.class,
//                                        stateManager);
//        Object[] structArray = structureList.toArray();
//        UIViewRoot viewRoot = (UIViewRoot) TestingUtil.invokePrivateMethod("restoreTree",
//                                                                           new Class[] { Object[].class },
//                                                                           new Object[] { structArray },
//                                                                           StateManagerImpl.class,
//                                                                           stateManager);
//
//        UIComponent component = (UIComponent) viewRoot.getChildren().get(0);
//        assertTrue(component instanceof UIForm);
//        assertTrue(component.getId().equals("basicForm"));
//
//        UIForm uiform = (UIForm) component;
//        component = (UIComponent) uiform.getChildren().get(0);
//        assertTrue(component instanceof UIPanel);
//        assertTrue(component.getId().equals("panel1"));
//
//        UIPanel uipanel = (UIPanel) component;
//        component = (UIComponent) uipanel.getChildren().get(0);
//        assertTrue(component instanceof UIInput);
//        assertTrue(component.getId().equals("userName2"));
//
//        // make sure that the transient property is not persisted as well as the
//        // namespace is preserved.
//        basicForm = (UIForm) viewRoot.findComponent("basicForm");
//        assertTrue(basicForm != null);
//
//        userName = (UIInput) basicForm.findComponent("userName");
//        assertTrue(userName == null);
//
//        panel1 = (UIPanel) basicForm.findComponent("panel1");
//        assertTrue(panel1 != null);
//
//        userName1 = (UIInput) panel1.findComponent("userName1");
//        assertTrue(userName1 == null);
//
//        userName2 = (UIInput) panel1.findComponent("userName2");
//        assertTrue(userName2 != null);
//
//        // make sure facets work correctly when marked transient.
//        Map facetList = panel1.getFacets();
//        assertTrue(!(facetList.containsKey("userName3")));
//        assertTrue(facetList.containsKey("userName4"));
    }


} // end of class TestRenderResponsePhase
