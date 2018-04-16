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

// TestLoadBundleTag.java

package com.sun.faces.taglib.jsf_core;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.Util;

import javax.faces.component.UIViewRoot;
import javax.faces.application.Application;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import java.util.Map;
import java.util.Locale;

public class TestLoadBundleTag extends ServletFacesTestCase {

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

    public TestLoadBundleTag() {
        super("TestLoadBundleTag.java");
    }


    public TestLoadBundleTag(String name) {
        super(name);
    }

//
// Class methods
//

//
// General Methods
//

    public void testLoadBundle() throws Exception {
        getFacesContext().setViewRoot(Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null));
        getFacesContext().getViewRoot().setLocale(Locale.US);
        LoadBundleTag tag = new LoadBundleTag();
        ExpressionFactory factory =
            getFacesContext().getApplication().getExpressionFactory();
        ValueExpression expr =
            factory.createValueExpression(getFacesContext().getELContext(),
                "com.sun.faces.TestMessages",String.class);
                                         
        tag.setBasename(expr);
        tag.setVar("messages");
        tag.doStartTag();
        assertEquals("Didn't get expected value",
                     ((Map) getFacesContext().getExternalContext()
                            .getRequestMap()
                            .get("messages")).get("buckaroo"),
                     "banzai");
        assertEquals("Didn't get expected value",
                     ((Map) getFacesContext().getExternalContext()
                            .getRequestMap()
                            .get("messages")).get("john"),
                     "bigboote");
        assertEquals("???notpresent???",
                     ((Map) getFacesContext().getExternalContext()
                            .getRequestMap()
                            .get("messages")).get("notpresent"));

    }


    //test out full Map contract implementation of LoadBundleTag
    public void testLoadBundleMap() throws Exception {
        boolean gotException = false;
        Object key = "buckaroo";
        Object value = "banzai";
        ExpressionFactory factory =
            getFacesContext().getApplication().getExpressionFactory();
        ValueExpression expr =
            factory.createValueExpression(getFacesContext().getELContext(),
                "com.sun.faces.TestMessages",String.class);
                                          
        getFacesContext().setViewRoot(Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null));
        getFacesContext().getViewRoot().setLocale(Locale.US);
        LoadBundleTag tag = new LoadBundleTag();
        tag.setBasename(expr);
        tag.setVar("messages");
        tag.doStartTag();
        Map testMap = (Map) getFacesContext().getExternalContext()
            .getRequestMap()
            .get("messages");

        LoadBundleTag tag2 = new LoadBundleTag();
        tag2.setBasename(expr);
        tag2.setVar("messages2");
        tag2.doStartTag();
        Map testMap2 = (Map) getFacesContext().getExternalContext()
            .getRequestMap()
            .get("messages2");

        try {
            testMap.clear();
        } catch (UnsupportedOperationException ex) {
            gotException = true;
        }
        assertTrue("Map.clear() should not be supported for immutable Map",
                   gotException);
        gotException = false;

        assertTrue("key not in Map", testMap.containsKey(key));
        assertTrue("value not in Map", testMap.containsValue(value));
        assertTrue("entrySet not correct for Map",
                   testMap.entrySet().equals(testMap2.entrySet()));
        assertTrue("Same maps are not equal", testMap.equals(testMap2));
        assertEquals("value not in Map", testMap.get(key), value);
        //two equal sets should have same hashcode
        assertTrue("HashCode not valid",
                   testMap.hashCode() == testMap2.hashCode());
        assertFalse("Map should not be empty", testMap.isEmpty());
        assertTrue("keySet not valid", testMap.keySet().contains(key));
        try {
            testMap.put(key, value);
        } catch (UnsupportedOperationException ex) {
            gotException = true;
        }
        assertTrue("Map.put() should not be supported for immutable Map",
                   gotException);
        gotException = false;

        try {
            testMap.putAll(new java.util.HashMap());
        } catch (UnsupportedOperationException ex) {
            gotException = true;
        }
        assertTrue("Map.putAll() should not be supported for immutable Map",
                   gotException);
        gotException = false;

        try {
            testMap.remove(key);
        } catch (UnsupportedOperationException ex) {
            gotException = true;
        }
        assertTrue("Map.remove() should not be supported for immutable Map",
                   gotException);
        gotException = false;

        assertTrue("Map size incorrect", testMap.size() == 4);
        assertTrue("values from Map incorrect",
                   testMap.values().contains(value));

    }

} // end of class TestLoadBundleTag
