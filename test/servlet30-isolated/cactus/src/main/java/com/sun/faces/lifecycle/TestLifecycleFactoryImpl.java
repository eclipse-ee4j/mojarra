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

// TestLifecycleFactoryImpl.java

package com.sun.faces.lifecycle;

import com.sun.faces.cactus.ServletFacesTestCase;
import org.apache.cactus.ServletTestCase;

import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

import java.util.Iterator;

/**
 * <B>TestLifecycleFactoryImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestLifecycleFactoryImpl extends ServletFacesTestCase {

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

    public TestLifecycleFactoryImpl() {
        super("TestLifecycleFactoryImpl");
    }


    public TestLifecycleFactoryImpl(String name) {
        super(name);
    }

//
// Class methods
//

//
// General Methods
//

    public void testDefault() {
        LifecycleFactoryImpl factory = new LifecycleFactoryImpl();
        Lifecycle life = null, life2 = null;

        assertTrue(factory != null);

        // Make sure the default instance exists
        life = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        assertTrue(null != life);

        // Make sure multiple requests for the same name give the same
        // instance.
        life2 = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        assertTrue(life == life2);
    }


    public void testIdIterator() {
        LifecycleFactoryImpl factory = new LifecycleFactoryImpl();

        String
            l1 = "l1",
            l2 = "l2",
            l3 = "l3";
        LifecycleImpl
            life1 = new LifecycleImpl(),
            life2 = new LifecycleImpl(),
            life3 = new LifecycleImpl();
        int i = 0;
        Iterator iter = null;

        factory.addLifecycle(l1, life1);
        factory.addLifecycle(l2, life2);
        factory.addLifecycle(l3, life3);

        iter = factory.getLifecycleIds();
        while (iter.hasNext()) {
            iter.next();
            i++;
        }

        assertTrue(4 == i);
    }


    public void testIllegalArgumentException() {
        LifecycleFactoryImpl factory = new LifecycleFactoryImpl();
        Lifecycle life = null;
        assertTrue(factory != null);

        boolean exceptionThrown = false;
        // Try to get an IllegalArgumentException
        try {
            LifecycleImpl lifecycle = new LifecycleImpl();
            factory.addLifecycle("bogusId", lifecycle);
            factory.addLifecycle("bogusId", lifecycle);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        } catch (UnsupportedOperationException e) {
            assertTrue(false);
        }
        assertTrue(exceptionThrown);
    }


} // end of class TestLifecycleFactoryImpl
