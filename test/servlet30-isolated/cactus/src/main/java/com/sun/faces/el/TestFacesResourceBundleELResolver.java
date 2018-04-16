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

// TestFacesResourceBundleELResolver.java

package com.sun.faces.el;

import com.sun.faces.cactus.ServletFacesTestCase;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.faces.component.UIViewRoot;


public class TestFacesResourceBundleELResolver extends ServletFacesTestCase {
    
     public TestFacesResourceBundleELResolver()
    {
        super("TestFacesResourceBundleELResolver");
    }

    public TestFacesResourceBundleELResolver(String name)
    {
        super(name);
    }
    
    private FacesResourceBundleELResolver resolver = null;
    
    public void setUp() {
        super.setUp();
        UIViewRoot root = new UIViewRoot();
        root.setLocale(Locale.ENGLISH);
        getFacesContext().setViewRoot(root);
        resolver = new FacesResourceBundleELResolver();
    }
    
    public void testGetValuePositive() throws Exception {        
        ResourceBundle bundle1 = (ResourceBundle)
            resolver.getValue(getFacesContext().getELContext(), null,
                "testResourceBundle");
        assertNotNull(bundle1);
        String value = bundle1.getString("value2");
        assertNotNull(value);
        assertEquals("Bob", value);
        
        
    }
    
    public void testGetValueNegative() throws Exception {
        ResourceBundle bundle1 = (ResourceBundle)
            resolver.getValue(getFacesContext().getELContext(), "hello",
                "testResourceBundle");
        assertNull(bundle1);
        boolean exceptionThrown = false;
        try {
            bundle1 = (ResourceBundle)
                resolver.getValue(getFacesContext().getELContext(), null, null);
        }
        catch (PropertyNotFoundException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        
        bundle1 = (ResourceBundle)
            resolver.getValue(getFacesContext().getELContext(), null,
                "nonExistent");
        
        assertNull(bundle1);
    }
    
    public void testGetTypePositive() throws Exception {
        getFacesContext().getELContext().setPropertyResolved(false);
        Class type = resolver.getType(getFacesContext().getELContext(), null, "testResourceBundle");
        assertTrue(getFacesContext().getELContext().isPropertyResolved());
        assertEquals(ResourceBundle.class, type);
        
    }
    
    public void testGetTypeNegative() throws Exception {
        Class result = resolver.getType(getFacesContext().getELContext(), 
                "non-null", "testResourceBundle");
        assertTrue(null == result);
        boolean exceptionThrown = false;
        try {
            resolver.getType(getFacesContext().getELContext(), null, null);
        }
        catch (PropertyNotFoundException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }
    
    public void testSetValeNegative() throws Exception {
        boolean exceptionThrown = false;
        try {
            resolver.setValue(getFacesContext().getELContext(), null, null, null);
        }
        catch (PropertyNotFoundException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            resolver.setValue(getFacesContext().getELContext(), null, "testResourceBundle",
                    "Value");
        }
        catch (PropertyNotWritableException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
            
    }
 
    public void testIsReadOnlyPositive() throws Exception {
        getFacesContext().getELContext().setPropertyResolved(false);
        boolean result = resolver.isReadOnly(getFacesContext().getELContext(),
                null, "testResourceBundle");
        assertTrue(result);
        assertTrue(getFacesContext().getELContext().isPropertyResolved());
        
    }
    
    public void testIsReadOnlyNegative() throws Exception {
        boolean result = resolver.isReadOnly(getFacesContext().getELContext(), 
                "non-null Base", null);
        assertTrue(!result);
        
        getFacesContext().getELContext().setPropertyResolved(false);
        boolean exceptionThrown = false;
        try {
            resolver.isReadOnly(getFacesContext().getELContext(), null, null);
        }
        catch (PropertyNotFoundException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertFalse(getFacesContext().getELContext().isPropertyResolved());
        
        
    }
    
    public void testGetFeatureDescriptorsPositive() throws Exception {
        Iterator iter = resolver.getFeatureDescriptors(getFacesContext().getELContext(), null);
        
        assertNotNull(iter);
        FeatureDescriptor cur = null;
        String 
            displayName1 = "Testo Pesto",
            displayName2 = "Second ResourceBundle";
        String 
            var1 = "testResourceBundle",
            var2 = "testResourceBundle2";
        boolean test = false;
        while (iter.hasNext()) {
            test = false;
            cur = (FeatureDescriptor) iter.next();
            assertEquals(Boolean.TRUE, cur.getValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME));
            assertEquals(ResourceBundle.class, cur.getValue(ELResolver.TYPE));
            assertTrue(!cur.isExpert());
            assertTrue(!cur.isHidden());
            assertTrue(cur.isPreferred());
            assertNotNull(cur.getDisplayName());
            test = cur.getDisplayName().equals(displayName1) ||
                   cur.getDisplayName().equals(displayName2);
            assertTrue(test);
            test = cur.getName().equals(var1) || cur.getName().equals(var2);
            assertTrue(test);
        }
    }

}
