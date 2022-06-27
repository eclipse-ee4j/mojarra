/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

// TestUtil_local.java
package com.sun.faces.util;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * <B>TestUtil_local.java</B> is a class ...
 * 
 * <B>Lifetime And Scope</B>
 * <P>
 *
 */
public class TestUtil_local extends TestCase {

//
// Protected Constants
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
    public TestUtil_local() {
        super("TestUtil_local.java");
    }

    public TestUtil_local(String name) {
        super(name);
    }

//
// Class methods
//
//
// General Methods
//
    public void testGetLocaleFromString() {
        // positive tests
        assertNotNull(Util.getLocaleFromString("ps"));
        assertNotNull(Util.getLocaleFromString("tg_AF"));
        assertNotNull(Util.getLocaleFromString("tk_IQ-Traditional"));
        assertNotNull(Util.getLocaleFromString("tk-IQ_Traditional"));

        try {
            Util.getLocaleFromString("aoeuhoentuhtnhtnhoaenhnhu");
            fail();
        } catch (Exception exception) {
        }

        try {
            Util.getLocaleFromString("ps_PS-at-pq-wp");
        } catch (Exception exception) {
        }

        try {
            Util.getLocaleFromString("tg-AF-oe-12");
        } catch (Exception exception) {
        }

        try {
            Util.getLocaleFromString("12-34");
        } catch (Exception exception) {
        }

        try {
            Util.getLocaleFromString("12-");
        } catch (Exception exception) {
        }
    }
    
    public void testSplit() {
        String[] result = null;
        
        result = Util.split(new HashMap<String,Object>(), "fooBarKey=Zm9vQmFyVmFsdWU====", "=", 2);
        assertEquals(2, result.length);
        assertEquals(result[1], "Zm9vQmFyVmFsdWU====");
        
        result = Util.split(new HashMap<String,Object>(), "fooBarKey=Zm9vQmFyVmFsdWU=", "=", 2);
        assertEquals(2, result.length);
        assertEquals(result[1], "Zm9vQmFyVmFsdWU=");
        
        result = Util.split(new HashMap<String,Object>(), "fooBarKey2=Zm9vQmFyVmFsdWUy", "=", 2);
        assertEquals(2, result.length);
        assertEquals(result[1], "Zm9vQmFyVmFsdWUy");
    }

} // end of class TestUtil_local
