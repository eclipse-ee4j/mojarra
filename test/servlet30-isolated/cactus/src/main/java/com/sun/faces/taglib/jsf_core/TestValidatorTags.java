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

// TestValidatorTags.java

package com.sun.faces.taglib.jsf_core;

import java.util.Iterator;
import java.util.Locale;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.lifecycle.ApplyRequestValuesPhase;
import com.sun.faces.lifecycle.Phase;
import com.sun.faces.lifecycle.ProcessValidationsPhase;
import com.sun.faces.lifecycle.RenderResponsePhase;
import com.sun.faces.util.Util;

import org.apache.cactus.WebRequest;

/**
 * <B>TestValidatorTags</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestValidatorTags extends JspFacesTestCase {

//
// Protected Constants
//

    public static final String TEST_URI = "/TestValidatorTags.jsp";
    public static final String OUTOFBOUNDS1_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "outOfBounds1";
    public static final String OUTOFBOUNDS1_VALUE = "3.1415";
    public static final String INBOUNDS1_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "inBounds1";
    public static final String INBOUNDS1_VALUE = "10.25";
    public static final String OUTOFBOUNDS2_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "outOfBounds2";
    public static final String OUTOFBOUNDS2_VALUE = "fox";
    public static final String INBOUNDS2_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "inBounds2";
    public static final String INBOUNDS2_VALUE = "alligator22";
    public static final String OUTOFBOUNDS3_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "outOfBounds3";
    public static final String OUTOFBOUNDS3_VALUE = "30000";
    public static final String INBOUNDS3_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "inBounds3";
    public static final String INBOUNDS3_VALUE = "1100";
    public static final String REQUIRED1_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "required1";
    public static final String REQUIRED1_VALUE = "required";
    public static final String REQUIRED2_ID = "validatorForm" +
        NamingContainer.SEPARATOR_CHAR +
        "required2";
    public static final String REQUIRED2_VALUE = "required";


    public boolean sendResponseToFile() {
        return false;
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

    public TestValidatorTags() {
        super("TestValidatorTags");
	initLocalHostPath();
    }


    public TestValidatorTags(String name) {
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


    public void beginValidators(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test", "/faces", TEST_URI, null);
        theRequest.addParameter(OUTOFBOUNDS1_ID, OUTOFBOUNDS1_VALUE);
        theRequest.addParameter(INBOUNDS1_ID, INBOUNDS1_VALUE);
        theRequest.addParameter(OUTOFBOUNDS2_ID, OUTOFBOUNDS2_VALUE);
        theRequest.addParameter(INBOUNDS2_ID, INBOUNDS2_VALUE);
        theRequest.addParameter(OUTOFBOUNDS3_ID, OUTOFBOUNDS3_VALUE);
        theRequest.addParameter(INBOUNDS3_ID, INBOUNDS3_VALUE);
        theRequest.addParameter(REQUIRED1_ID, "");
        theRequest.addParameter(REQUIRED2_ID, "");
        theRequest.addParameter("validatorForm", "validatorForm");

    }


    public void setUp() {
        super.setUp();
    }


    public void testValidators() {
        // Verify the parmeters are as expected
        String paramVal = (String) (getFacesContext().getExternalContext()
            .getRequestParameterMap()).get(OUTOFBOUNDS1_ID);
        assertTrue(OUTOFBOUNDS1_VALUE.equals(paramVal));
//    assertTrue(OUTOFBOUNDS1_VALUE.equals(getFacesContext().getServletRequest().getParameter(OUTOFBOUNDS1_ID)));

        boolean result = false;
        String value = null;
        Phase
            renderResponse = new RenderResponsePhase(),
            processValidations = new ProcessValidationsPhase(),
            applyRequestValues = new ApplyRequestValuesPhase();

        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), TEST_URI);
        page.setLocale(Locale.US);       
        getFacesContext().setViewRoot(page);

        // This builds the tree, and usefaces saves it in the session
        renderResponse.execute(getFacesContext());
        assertTrue(!(getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        // This causes the components to be set to valid
        applyRequestValues.execute(getFacesContext());
        assertTrue(!(getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        // process the validations
        processValidations.execute(getFacesContext());
        // We know there are validation errors on the page
        assertTrue(getFacesContext().getRenderResponse());

        // verify the messages have been added correctly.
        UIComponent comp = null;
        Iterator messages = null;

        assertTrue(null != (messages = getFacesContext().getMessages()));
        assertTrue(messages.hasNext());

        // check the messages for each component in the page
        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(
                        OUTOFBOUNDS1_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(INBOUNDS1_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(!messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(
                        OUTOFBOUNDS2_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(INBOUNDS2_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(!messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(
                        OUTOFBOUNDS3_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(INBOUNDS3_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(!messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(REQUIRED1_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(messages.hasNext());

        assertTrue(null !=
                   (comp =
                    getFacesContext().getViewRoot().findComponent(REQUIRED2_ID)));
        assertTrue(
            null !=
            (messages =
             getFacesContext().getMessages(comp.getClientId(getFacesContext()))));
        assertTrue(messages.hasNext());

    }


} // end of class TestValidatorTags
