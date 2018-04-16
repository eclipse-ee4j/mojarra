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

// TestRendererConversions.java

package com.sun.faces.renderkit.html_basic;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import java.util.Locale;


/**
 * <B>TestRendererConversions</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestRendererConversions extends ServletFacesTestCase {

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

    public TestRendererConversions() {
        super("TestRendererConversions");
	initLocalHostPath();
    }


    public TestRendererConversions(String name) {
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

    public void beginEmptyStrings(WebRequest theRequest) {
        theRequest.setURL(localHostPath, null, null, TEST_URI, null);
        theRequest.addParameter("number", "");
        theRequest.addParameter("date", "");
        theRequest.addParameter("text", "");
        theRequest.addParameter("hidden", "");
        theRequest.addParameter("secret", "");
    }


    public void setUp() {
        super.setUp();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setViewId("viewId");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);
    }


    /**
     * Test the built-in conversion for those renderers that have it.
     */

    public void testEmptyStrings() {
        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        root.setLocale(Locale.US);
        UIInput
            text = new UIInput(),
            hidden = new UIInput(),
            secret = new UIInput();

        text.setId("text");
        hidden.setId("hidden");
        secret.setId("secret");

        text.setRendererType("Text");
        hidden.setRendererType("Hidden");
        secret.setRendererType("Secret");

        root.getChildren().add(text);
        root.getChildren().add(hidden);
        root.getChildren().add(secret);
        TextRenderer textRenderer = new TextRenderer();
        HiddenRenderer hiddenRenderer = new HiddenRenderer();
        SecretRenderer secretRenderer = new SecretRenderer();

        try {
            textRenderer.decode(getFacesContext(), text);
            hiddenRenderer.decode(getFacesContext(), hidden);
            secretRenderer.decode(getFacesContext(), secret);
        } catch (Throwable e) {
            assertTrue(false);
        }
        assertTrue(text.isValid());
        assertTrue(hidden.isValid());
        assertTrue(secret.isValid());
    }


    public void beginNulls(WebRequest theRequest) {
        theRequest.setURL(localHostPath, null, null, TEST_URI, null);
    }


    public void testNulls() {
        testEmptyStrings();
    }


    public void beginBadConversion(WebRequest theRequest) {
        theRequest.setURL(localHostPath, null, null, TEST_URI, null);
    }


    public void testBadConversion() {
        UIComponent root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
    }


} // end of class TestRendererConversions
