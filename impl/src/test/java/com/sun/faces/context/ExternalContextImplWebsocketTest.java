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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import static com.sun.faces.RIConstants.FACES_PREFIX;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * The JUnit tests for websocket.
 */
public class ExternalContextImplWebsocketTest extends JUnitFacesTestCaseBase {

    public ExternalContextImplWebsocketTest() {
        super("ExternalContextImplWebsocketTest");
    }

    @Test
    public void testEncodeWebsocketURLOverHTTP() {
        ExternalContextImpl externalContext = prepareExternalContext("http://host/site.xhtml");

        assertEquals("ws://host/test", externalContext.encodeWebsocketURL("/test"));
    }

    @Test
    public void testEncodeWebsocketURLOverHTTPWithPort() {
        ExternalContextImpl externalContext = prepareExternalContext("http://host:1234/site.xhtml");

        assertEquals("ws://host:1234/test", externalContext.encodeWebsocketURL("/test"));
    }

    @Test
    public void testEncodeWebsocketURLOverHTTPS() {
        ExternalContextImpl externalContext = prepareExternalContext("https://host/site.xhtml");

        assertEquals("wss://host/test", externalContext.encodeWebsocketURL("/test"));
    }

    @Test
    public void testEncodeWebsocketURLOverHTTPSWithPort() {
        ExternalContextImpl externalContext = prepareExternalContext("https://host:1234/site.xhtml");

        assertEquals("wss://host:1234/test", externalContext.encodeWebsocketURL("/test"));
    }

    private ExternalContextImpl prepareExternalContext(String requestURL) {
        facesContext.getAttributes().put(FACES_PREFIX + "ExternalContextImpl.PUSH_SUPPORTED", Boolean.TRUE);

        ServletContext servletContext = PowerMock.createNiceMock(ServletContext.class);
        HttpServletRequest request = PowerMock.createNiceMock(HttpServletRequest.class);
        HttpServletResponse response = PowerMock.createNiceMock(HttpServletResponse.class);

        ApplicationAssociate applicationAssociate = PowerMock.createMock(ApplicationAssociate.class);
        expect(servletContext.getAttribute(RIConstants.FACES_PREFIX + "ApplicationAssociate")).andReturn(applicationAssociate);

        Capture<String> encodeCapture = new Capture<>(CaptureType.ALL);
        expect(response.encodeURL(capture(encodeCapture))).andAnswer(encodeCapture::getValue);
        expect(request.getRequestURL()).andReturn(new StringBuffer(requestURL));

        replay(servletContext, request, response);

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        facesContext.setExternalContext(externalContext);

        return externalContext;
    }
}
