/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.renderkit;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.util.Util;

import javax.faces.component.UICommand;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import java.io.StringWriter;
import java.util.Collections;

public class TestRenderKitUtils extends ServletFacesTestCase {


    // ------------------------------------------------------------ Constructors


    public TestRenderKitUtils() {
        super();    
    }


    public TestRenderKitUtils(String name) {
        super(name);
    }


    // ------------------------------------------------------------ Test Methods


    public void testOnClickBackslashEscaping() throws Exception {

        String input = "return confirm('foo\\');";
        String expectedResult ="return confirm(\\'foo\\\\\\');";
        HtmlCommandLink link = new HtmlCommandLink();
        link.setOnclick(input);
        StringWriter capture = new StringWriter();
        ResponseWriter current = getFacesContext().getResponseWriter();
        if (current == null) {
            RenderKit renderKit =
                  RenderKitUtils.getCurrentRenderKit(getFacesContext());
            current = renderKit.createResponseWriter(capture, null, null);
            getFacesContext().setResponseWriter(current);
        } else {
            getFacesContext().setResponseWriter(current.cloneWithWriter(capture));
        }

        getFacesContext().getResponseWriter().startElement("link", link);
        RenderKitUtils.renderOnclick(getFacesContext(),
                                     link,
                                     null,
                                     "form",
                                     true);
        getFacesContext().getResponseWriter().endElement("link");

        String actualResult = capture.toString();
        assertTrue(actualResult.contains(expectedResult));

    }
}
