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

// TestExternalContextFactoryImpl.java

package com.sun.faces.context;

import com.sun.faces.cactus.ServletFacesTestCase;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

/**
 * <B>TestExternalContextFactoryImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestExternalContextFactoryImpl extends ServletFacesTestCase {

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

    public TestExternalContextFactoryImpl() {
        super("TestExternalContextFactory");
    }


    public TestExternalContextFactoryImpl(String name) {
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
        ExternalContext externalContext = null;
        ExternalContextFactoryImpl externalContextFactory = null;

        // create ExternalContextFactory.
        externalContextFactory = new ExternalContextFactoryImpl();

        try {
            externalContext = externalContextFactory.getExternalContext(null, null, null);
        } catch (FacesException fe) {
            gotException = true;
        } catch (NullPointerException ee) {
            gotException = true;
        }
        assertTrue(gotException);

    }

} // end of class TestExternalContextFactoryImpl
