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

// TestFacesContextFactoryImpl.java

package com.sun.faces.context;

import com.sun.faces.cactus.ServletFacesTestCase;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

/**
 * <B>TestFacesContextFactoryImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestFacesContextFactoryImpl extends ServletFacesTestCase {

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

    public TestFacesContextFactoryImpl() {
        super("TestFacesContextFactory");
    }


    public TestFacesContextFactoryImpl(String name) {
        super(name);
    }
//
// Class methods
//

//
// Methods from TestCase
//

//
// General Methods
//

    public void testCreateMethods() {
        boolean gotException = false;
        FacesContext facesContext = null;
        FacesContextFactoryImpl facesContextFactory = null;

        // create FacesContextFactory.
        facesContextFactory = new FacesContextFactoryImpl();

        try {
            facesContext = facesContextFactory.getFacesContext(null, null, null,
                                                               null);
        } catch (FacesException fe) {
            gotException = true;
        } catch (NullPointerException ee) {
            gotException = true;
        }
        assertTrue(gotException);

        LifecycleFactory factory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        assertTrue(null != factory);
        Lifecycle lifecycle =
            factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        assertTrue(null != lifecycle);

        gotException = false;
        try {
            facesContext = facesContextFactory.getFacesContext(
                config.getServletContext(),
                request,
                response,
                lifecycle);
        } catch (FacesException fe) {
            gotException = true;
        }
        assertTrue(gotException == false);

    }

} // end of class TestFacesContextFactoryImpl
