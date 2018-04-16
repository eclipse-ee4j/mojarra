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

package com.sun.faces.context;

import javax.faces.FactoryFinder;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import com.sun.faces.cactus.ServletFacesTestCase;

/**
 * <B>TestPartialResponseWriter.java</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestPartialResponseWriter extends ServletFacesTestCase // ServletTestCase
{

//
// Protected Constants
//

// Class Variables
//

//
// Instance Variables
//
    private ResponseWriter writer = null;
    private PartialResponseWriter pWriter = null;
    private RenderKit renderKit = null;
    private StringWriter sw = null;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestPartialResponseWriter() {
        super("TestPartialResponseWriter.java");
    }


    public TestPartialResponseWriter(String name) {
        super(name);
    }

//
// Class methods
//

//
// General Methods
//
    public void setUp() {
        super.setUp();
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                  RenderKitFactory.HTML_BASIC_RENDER_KIT);
        sw = new StringWriter();
        writer = renderKit.createResponseWriter(sw, "text/html", "ISO-8859-1");
        pWriter = new PartialResponseWriter(writer);
    }

    // Tests that the <extension> element is placed correctly in the partial response output

    public void testExtension() {
        try {
            pWriter.startDocument();
            pWriter.startUpdate(PartialResponseWriter.VIEW_STATE_MARKER);
            pWriter.write("foo");
            pWriter.endUpdate();
            pWriter.startExtension(Collections.<String, String>emptyMap());
            pWriter.startElement("data", null);
            pWriter.endElement("data");
            pWriter.endExtension();
            pWriter.endDocument();

            assertTrue(sw.toString().indexOf("</update><extension><data></data></extension></changes></partial-response>") >= 0);
        } catch (IOException e) {
            assertTrue(false);
        }
            

    }
}
