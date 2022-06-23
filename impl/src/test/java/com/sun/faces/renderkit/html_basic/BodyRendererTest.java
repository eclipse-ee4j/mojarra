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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * The JUnit tests for the BodyRenderer class.
 */
public class BodyRendererTest {

    /**
     * Test decode method.
     */
    @Test
    public void testDecode() {
        BodyRenderer bodyRenderer = new BodyRenderer();
        bodyRenderer.decode(null, null);
    }

    /**
     * Test encodeBegin method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEncodeBegin() throws Exception {
        StringWriter writer = new StringWriter();
        ResponseWriter testResponseWriter = new TestResponseWriter(writer);
        FacesContext facesContext = PowerMock.createPartialMock(FacesContext.class, "getResponseWriter");
        BodyRenderer bodyRenderer = new BodyRenderer();
        HtmlBody htmlBody = new HtmlBody();
        htmlBody.getAttributes().put("styleClass", "myclass");
        
        expect(facesContext.getResponseWriter()).andReturn(testResponseWriter).anyTimes();
        
        PowerMock.replay(facesContext);
        bodyRenderer.encodeBegin(facesContext, htmlBody);
        PowerMock.verify(facesContext);
        String html = writer.toString();
        assertTrue(html.contains("<body"));
        assertTrue(html.contains("class=\"myclass\""));
    }

    /**
     * Test encodeChildren method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testEncodeChildren() throws Exception {
        BodyRenderer bodyRenderer = new BodyRenderer();
        bodyRenderer.encodeChildren(null, null);
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
        Application application = PowerMock.createMock(Application.class);
        BodyRenderer bodyRenderer = new BodyRenderer();
        HtmlBody htmlBody = new HtmlBody();
        
        expect(facesContext.getApplication()).andReturn(application).anyTimes();
        expect(facesContext.getClientIdsWithMessages()).andReturn(Collections.EMPTY_LIST.iterator()).anyTimes();
        expect(facesContext.getResponseWriter()).andReturn(testResponseWriter).anyTimes();
        expect(facesContext.getViewRoot()).andReturn(viewRoot).anyTimes();
        expect(facesContext.isProjectStage(ProjectStage.Development)).andReturn(false).anyTimes();
        expect(viewRoot.getComponentResources(facesContext, "body")).andReturn(Collections.EMPTY_LIST).anyTimes();
       
        PowerMock.replay(facesContext, viewRoot, application);
        bodyRenderer.encodeEnd(facesContext, htmlBody);
        PowerMock.verify(facesContext, viewRoot, application);
        String html = writer.toString();
        assertTrue(html.contains("</body>"));
    }

    /**
     * Test getRendersChildren method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testGetRendersChildren() throws Exception {
        BodyRenderer bodyRenderer = new BodyRenderer();
        assertFalse(bodyRenderer.getRendersChildren());
    }
}
