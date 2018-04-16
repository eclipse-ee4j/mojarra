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

// TestPhase.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import java.util.Locale;


/**
 * <B>TestPhase</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestPhase extends ServletFacesTestCase {

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

    public TestPhase() {
        super("TestPhase");
	initLocalHostPath();
    }


    public TestPhase(String name) {
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

    public void beginExecute(WebRequest theRequest) {
        theRequest.setURL(localHostPath, null, null, TEST_URI, null);
        theRequest.addParameter(
            "basicForm" + NamingContainer.SEPARATOR_CHAR + "userName", "jerry");
    }


    public void testExecute() {

        Phase restoreView = new RestoreViewPhase();
	Object oldRequest = facesService.wrapRequestToHideParameters();
	
        try {
            restoreView.execute(getFacesContext());
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        }

	facesService.unwrapRequestToShowParameters(oldRequest);

        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));
        assertTrue(null != getFacesContext().getViewRoot());

        // 2. Add components to tree
        //
        UIComponent root = getFacesContext().getViewRoot();
        UIForm basicForm = new UIForm();
        basicForm.setId("basicForm");
        UIInput userName = new UIInput();
        userName.setId("userName");
        root.getChildren().add(basicForm);
        basicForm.getChildren().add(userName);

        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.getChildren().add(basicForm);
        page.setViewId("root");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);

        Phase applyValues = new ApplyRequestValuesPhase();

        try {
            applyValues.execute(getFacesContext());
        } catch (Throwable e) {
            System.out.println("Throwable: " + e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
        assertTrue((getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));
    }

} // end of class TestPhase
