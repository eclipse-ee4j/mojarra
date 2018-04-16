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

// TestRenderKitFactory.java

package com.sun.faces.renderkit;

import com.sun.faces.cactus.ServletFacesTestCase;

import javax.faces.FactoryFinder;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import java.util.Iterator;

/**
 * <B>TestRenderKitFactory</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestRenderKitFactory extends ServletFacesTestCase {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//
    private RenderKitFactoryImpl renderKitFactory = null;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestRenderKitFactory() {
        super("TestRenderKitFactory");
    }


    public TestRenderKitFactory(String name) {
        super(name);
    }
//
// Class methods
//

//
// General Methods
//

    public void testFactory() {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);

        // 1. Verify "getRenderKit" returns the same RenderKit instance
        //    if called multiple times with the same identifier.
        //  
        RenderKit renderKit1 = renderKitFactory.getRenderKit(getFacesContext(),
                                                             RenderKitFactory.HTML_BASIC_RENDER_KIT);
        RenderKit renderKit2 = renderKitFactory.getRenderKit(getFacesContext(),
                                                             RenderKitFactory.HTML_BASIC_RENDER_KIT);
        assertTrue(renderKit1 == renderKit2);

        // 2. Verify "addRenderKit" adds instances.. /
        //
        renderKitFactory.addRenderKit("Foo", renderKit1);
        renderKitFactory.addRenderKit("Bar", renderKit2);

        // Verify renderkit instance replaced with last identifier..
        //
        renderKitFactory.addRenderKit("BarBar", renderKit2);
        RenderKit rkit = renderKitFactory.getRenderKit(getFacesContext(),
                                                       "BarBar");
        assertTrue(rkit != null);
        assertTrue(rkit == renderKit2);

        // 3. Verify "getRenderKit" returns null if
        //    RenderKit not found for renderkitid...
        //
        RenderKit renderKit4 = renderKitFactory.getRenderKit(getFacesContext(),
                                                             "Gamma");
        assertTrue(renderKit4 == null);
    }


    public void testDefaultExists() {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    
        // 1. Verify "default" renderkit..
        //
        RenderKit renderKit;
        String id = null;
        Iterator iter = renderKitFactory.getRenderKitIds();
        boolean exists = false;
        while (iter.hasNext()) {
            id = (String) iter.next();
            if (id.equals(RenderKitFactory.HTML_BASIC_RENDER_KIT)) {
                exists = true;
                break;
            }
        }
        assertTrue(exists);
    }


    public void testExceptions() {
        renderKitFactory = new RenderKitFactoryImpl();
        RenderKit rKit = null;

        rKit = renderKitFactory.getRenderKit(getFacesContext(),
                                             RenderKitFactory.HTML_BASIC_RENDER_KIT);

        // Verify NPE for "addRenderKit"
        //
        boolean exceptionThrown = false;
        try {
            renderKitFactory.addRenderKit(null, rKit);
            exceptionThrown = false;
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            renderKitFactory.addRenderKit("foo", null);
            exceptionThrown = false;
        } catch (NullPointerException e1) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
	
        // Verify null parameter exception for "getRenderKit"
        //
        exceptionThrown = false;
        try {
            rKit = renderKitFactory.getRenderKit(null, null);
        } catch (NullPointerException e2) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            rKit = renderKitFactory.getRenderKit(getFacesContext(), null);
        } catch (NullPointerException e4) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }


} // end of class TestRenderKitFactory
