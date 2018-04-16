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

// TestViewTag.java

package com.sun.faces.taglib.jsf_core;

import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.lifecycle.Phase;
import com.sun.faces.lifecycle.RenderResponsePhase;
import com.sun.faces.util.Util;
import com.sun.faces.cactus.TestingUtil;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebRequest;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.jstl.core.Config;

import java.util.Locale;

/**
 * <B>TestViewTag</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestViewTag extends JspFacesTestCase {

//
// Protected Constants
//

    public static final String TEST_URI = "/TestViewTag.jsp";
    public static final String TEST_URI2 = "/TestViewTag2.jsp";

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

    public TestViewTag() {
        super("TestViewTag");
	initLocalHostPath();
    }


    public TestViewTag(String name) {
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

    public void beginViewTag(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test", "/faces", TEST_URI, null);
    }


    public void testViewTag() {
        boolean result = false;
        String value = null;
        Locale expectedLocale = new Locale("ps", "PS");
        Phase renderResponse = new RenderResponsePhase();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setId("root");
        page.setLocale(Locale.US);
        page.setViewId(TEST_URI);
        page.setLocale(Locale.CANADA_FRENCH);
        getFacesContext().setViewRoot(page);

        Config.set((ServletRequest)
            getFacesContext().getExternalContext().getRequest(),
                   Config.FMT_LOCALE, Locale.CANADA_FRENCH);

        try {
            renderResponse.execute(getFacesContext());
        } catch (FacesException fe) {
            System.out.println(fe.getMessage());
            if (null != fe.getCause()) {
                fe.getCause().printStackTrace();
            } else {
                fe.printStackTrace();
            }
        }
        assertEquals("locale not as expected", expectedLocale,
                     page.getLocale());
        assertEquals("locale not as expected", expectedLocale,
                     Config.get((ServletRequest)
            getFacesContext().getExternalContext().
            getRequest(),
                                Config.FMT_LOCALE));
    }


    public void beginViewTagVB(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test", "/faces", TEST_URI2, null);
    }


    public void testViewTagVB() {
        boolean result = false;
        String value = null;
        Locale expectedLocale = new Locale("ps", "PS", "Traditional");
        request.setAttribute("locale", expectedLocale);
        Phase renderResponse = new RenderResponsePhase();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setId("root");
        page.setLocale(Locale.US);
        page.setViewId(TEST_URI2);
        getFacesContext().setViewRoot(page);

        try {
            renderResponse.execute(getFacesContext());
        } catch (FacesException fe) {
            System.out.println(fe.getMessage());
            if (null != fe.getCause()) {
                fe.getCause().printStackTrace();
            } else {
                fe.printStackTrace();
            }
        }
        assertEquals("locale not as expected", expectedLocale,
                     page.getLocale());
    }


    public void testGetLocaleFromString() {
        ViewTag viewTag = new ViewTag();
        Locale locale = (Locale) 
            TestingUtil.invokePrivateMethod("getLocaleFromString",
                                            new Class[] { String.class },
                                            new Object[] { "fr-FR" },
                                            ViewTag.class,
                                            viewTag);        
        assertTrue(locale.equals(new Locale("fr", "FR")));

        
        locale = (Locale)
            TestingUtil.invokePrivateMethod("getLocaleFromString",
                                            new Class[] { String.class },
                                            new Object[] { "fr_FR" },
                                            ViewTag.class,
                                            viewTag);
        assertTrue(locale.equals(new Locale("fr", "FR")));

        
        locale = (Locale)
            TestingUtil.invokePrivateMethod("getLocaleFromString",
                                            new Class[] {String.class},
                                            new Object[] {"fr"},
                                            ViewTag.class,
                                            viewTag);
        assertTrue(locale.equals(new Locale("fr", "")));

       
        locale = (Locale)
            TestingUtil.invokePrivateMethod("getLocaleFromString",
                                            new Class[] {String.class},
                                            new Object[] {"testLocale"},
                                            ViewTag.class,
                                            viewTag);
        assertTrue(locale.equals(Locale.getDefault()));
    }

} // end of class TestViewTag
