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

// TestApplicationFactoryImpl.java

package com.sun.faces.application;

import com.sun.faces.cactus.JspFacesTestCase;

import com.sun.faces.config.ConfigureListener;
import javax.faces.application.Application;

/**
 * <B>TestApplicationFactoryImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestApplicationFactoryImpl extends JspFacesTestCase {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//
    private ApplicationFactoryImpl applicationFactory = null;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestApplicationFactoryImpl() {
        super("TestApplicationFactoryImpl");
    }


    public TestApplicationFactoryImpl(String name) {
        super(name);
    }
//
// Class methods
//

//
// General Methods
//

    public void testFactory() {
        applicationFactory = new ApplicationFactoryImpl();

        ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());


        // 1. Verify "getApplication" returns the same Application instance
        //    if called multiple times.
        //  
        Application application1 = applicationFactory.getApplication();
        Application application2 = applicationFactory.getApplication();
        assertTrue(application1 == application2);

        // 2. Verify "setApplication" adds instances.. /
        //    and "getApplication" returns the same instance
        //
	ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());
        Application application3 = new ApplicationImpl();
        applicationFactory.setApplication(application3);
        Application application4 = applicationFactory.getApplication();
        assertTrue(application3 == application4);
    }


    public void testSpecCompliance() {
        applicationFactory = new ApplicationFactoryImpl();
	ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());

        assertTrue(null != applicationFactory.getApplication());
    }


    public void testExceptions() {
        applicationFactory = new ApplicationFactoryImpl();

        // 1. Verify NullPointer exception which occurs when attempting
        //    to add a null Application
        //
        boolean thrown = false;
        try {
            applicationFactory.setApplication(null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }


} // end of class TestApplicationFactoryImpl
