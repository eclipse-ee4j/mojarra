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

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;

import org.apache.cactus.WebRequest;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.Util;

/**
 * This class tests the <code>StateManagerImpl</code> class with deprecated methods only - does not contain any of the
 * replacement methods (such as saveView).
 */
public class TestHASDeprStateManagerImpl extends ServletFacesTestCase {

    public static final String TEST_URI = "/greeting.jsp";
    public static final String ignore[] = {};
    
    private Application application;

    // Constructors/Initializers
    
    public TestHASDeprStateManagerImpl() {
        super("TestHASDeprStateManagerImpl");
    }

    public TestHASDeprStateManagerImpl(String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();
        ApplicationFactory aFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = (Application) aFactory.getApplication();
        application.setViewHandler(new ViewHandlerImpl());
        application.setStateManager(new DeprStateManagerImpl());
    }
    
    public String getExpectedOutputFilename() {
        return "TestViewHandlerImpl_correct";
    }

    public String[] getLinesToIgnore() {
        return ignore;
    }

    public boolean sendResponseToFile() {
        return true;
    }

    //
    // Test Methods
    //

    public void beginRender(WebRequest theRequest) {
        String containerPort = System.getProperty("container.port");
        if (null == containerPort || 0 == containerPort.length()) {
            containerPort = "8080";
        }

        theRequest.setURL("localhost:" + containerPort, "/test", "/faces", TEST_URI, null);
    }

    public void testRender() {
//        UIViewRoot newView = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
//        newView.setViewId(TEST_URI);
//        newView.setLocale(Locale.US);
//        getFacesContext().setViewRoot(newView);
//
//        try {
//            ViewHandler viewHandler = Util.getViewHandler(getFacesContext());
//            viewHandler.renderView(getFacesContext(), getFacesContext().getViewRoot());
//        } catch (IOException e) {
//            System.out.println("ViewHandler IOException:" + e);
//        } catch (FacesException fe) {
//            System.out.println("ViewHandler FacesException: " + fe);
//        }
//
//        assertTrue(!(getFacesContext().getRenderResponse()) && !(getFacesContext().getResponseComplete()));
//
//        assertTrue(verifyExpectedOutput());
    }
}
