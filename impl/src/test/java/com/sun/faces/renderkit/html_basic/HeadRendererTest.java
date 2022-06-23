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

package com.sun.faces.renderkit.html_basic;

import java.io.StringWriter;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlHead;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * The JUnit tests for the HeadRenderer class.
 */
public class HeadRendererTest {

    /**
     * Test decode method.
     */
    @Test
    public void testDecode() {
        HeadRenderer headRenderer = new HeadRenderer();
        headRenderer.decode(null, null);
    }

    /**
     * Test encodeBegin method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEncodeBegin() throws Exception {
        //
        // TODO: Note we are not testing this method as its complexity is too 
        // high, because it uses WebConfiguration.getInstance to get
        // configuration information that should be readily available to the
        // renderer through either the FacesContext or the component being
        // rendered.
        //
    }

    /**
     * Test encodeChildren method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEncodeChildren() throws Exception {
        HeadRenderer headRenderer = new HeadRenderer();
        headRenderer.encodeChildren(null, null);
    }

    /**
     * Test encodeEnd method.
     *
     * <p>
     * TODO: Note we are not testing the rendering of the component resources as
     * the underlying code is too complex for unit testing and needs to be
     * simplified.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEncodeEnd() throws Exception {
        StringWriter writer = new StringWriter();
        ResponseWriter testResponseWriter = new TestResponseWriter(writer);
        FacesContext facesContext = PowerMock.createPartialMockForAllMethodsExcept(FacesContext.class, "getCurrentInstance");
        UIViewRoot viewRoot = PowerMock.createMock(UIViewRoot.class);
        HeadRenderer headRenderer = new HeadRenderer();
        HtmlHead htmlHead = new HtmlHead();
        
        expect(facesContext.getResponseWriter()).andReturn(testResponseWriter).anyTimes();
        expect(facesContext.getViewRoot()).andReturn(viewRoot).anyTimes();
        expect(viewRoot.getComponentResources(facesContext, "head")).andReturn(Collections.EMPTY_LIST).anyTimes();
        
        PowerMock.replay(facesContext, viewRoot);
        headRenderer.encodeEnd(facesContext, htmlHead);
        PowerMock.verify(facesContext, viewRoot);
        String html = writer.toString();
        assertTrue(html.contains("</head>"));
    }
}
