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

// TestLifecycleImpl_initial.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.JspFacesTestCase;
import org.apache.cactus.WebRequest;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

/**
 * <B>TestLifecycleImpl_initial</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestLifecycleImpl_initial extends JspFacesTestCase {

//
// Protected Constants
//

    public static final String TEST_URI = "/greeting.jsp";


    public String getExpectedOutputFilename() {
        return "TestLifecycleImpl_initial_correct";
    }


    public static final String ignore[] = {
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

    public TestLifecycleImpl_initial() {
        super("TestLifecycleImpl_initial");
	initLocalHostPath();
    }


    public TestLifecycleImpl_initial(String name) {
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


    protected void initWebRequest(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test", "/faces", TEST_URI, null);
    }


    public void beginExecuteInitial(WebRequest theRequest) {
        initWebRequest(theRequest);
    }


    public void testExecuteInitial() {
        boolean result = false;
        LifecycleImpl life = new LifecycleImpl();

	Object oldRequest = facesService.wrapRequestToHideParameters();
        ViewHandler vh = getFacesContext().getApplication().getViewHandler();
        getFacesContext().setViewRoot(vh.createView(getFacesContext(), "/greeting.jsp"));

        try {
            life.execute(getFacesContext());
	    facesService.unwrapRequestToShowParameters(oldRequest);
            life.render(getFacesContext());
        } catch (FacesException e) {
            System.err.println("Root Cause: " + e.getCause());
            if (null != e.getCause()) {
                e.getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }

            assertTrue(e.getMessage(), false);
        }

        assertTrue(verifyExpectedOutput());

    }


} // end of class TestLifecycleImpl_initial
