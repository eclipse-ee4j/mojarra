/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UrlBuilderTest {

    private static final String ID = "a b&c=d'";
    private static final String ENCODED_ID = "a+b%26c%3Dd%27";

    private ClientWindow clientWindow;
    private ExternalContext externalContext;
    private FacesContext facesContext;

    @BeforeEach
    void setUp() throws Exception {
        clientWindow = mock(ClientWindow.class);
        externalContext = mock(ExternalContext.class);
        facesContext = mock(FacesContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getClientWindow()).thenReturn(clientWindow);
        when(clientWindow.isClientWindowRenderModeEnabled(facesContext)).thenReturn(true);
        setCurrentInstance(facesContext);
    }

    @AfterEach
    void tearDown() throws Exception {
        setCurrentInstance(null);
    }

    @Test
    void appendClientWindowEncodesId() {
        when(clientWindow.getId()).thenReturn(ID);

        assertEquals("/page.xhtml?jfwid=" + ENCODED_ID, appendClientWindow("/page.xhtml", false));
        assertEquals("/page.xhtml?a=b&jfwid=" + ENCODED_ID, appendClientWindow("/page.xhtml?a=b", true));
    }

    @Test
    void appendClientWindowEncodesSeparatorOfDefaultId() {
        when(clientWindow.getId()).thenReturn("8d2b6e1a-0d8e-4f2c-9a5f-3c1d2e4f5a6b:0");

        assertEquals("/page.xhtml?jfwid=8d2b6e1a-0d8e-4f2c-9a5f-3c1d2e4f5a6b%3A0", appendClientWindow("/page.xhtml", false));
    }

    @Test
    void appendClientWindowEncodesAndSeparatesAdditionalParameters() {
        Map<String, String> additionalParams = new LinkedHashMap<>();
        additionalParams.put("a'", "1&2");
        additionalParams.put("b", "<3>");
        when(clientWindow.getId()).thenReturn("id");
        when(clientWindow.getQueryURLParameters(facesContext)).thenReturn(additionalParams);

        assertEquals("/page.xhtml?jfwid=id&a%27=1%262&b=%3C3%3E", appendClientWindow("/page.xhtml", false));
    }

    @Test
    void appendClientWindowSkipsUrlWhichAlreadyHasId() {
        when(clientWindow.getId()).thenReturn(ID);

        assertEquals("/page.xhtml?jfwid=id", appendClientWindow("/page.xhtml?jfwid=id", true));
    }

    @Test
    void appendClientWindowSkipsWhenRenderModeIsDisabled() {
        when(clientWindow.getId()).thenReturn(ID);
        when(clientWindow.isClientWindowRenderModeEnabled(facesContext)).thenReturn(false);

        assertEquals("/page.xhtml", appendClientWindow("/page.xhtml", false));
    }

    @Test
    void createUrlEncodesId() {
        when(clientWindow.getId()).thenReturn(ID);

        assertEquals("/page.xhtml?a=b&jfwid=" + ENCODED_ID, new UrlBuilder("/page.xhtml?a=b", UTF_8.name()).createUrl());
    }

    @Test
    void encodeActionURLEncodesId() {
        when(clientWindow.getId()).thenReturn(ID);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.encodeURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(response.getCharacterEncoding()).thenReturn(UTF_8.name());
        ExternalContextImpl externalContextImpl = new ExternalContextImpl(mock(ServletContext.class), mock(HttpServletRequest.class), response);

        assertEquals("/page.xhtml?jfwid=" + ENCODED_ID, externalContextImpl.encodeActionURL("/page.xhtml"));
    }

    private String appendClientWindow(String url, boolean hasQueryString) {
        StringBuilder builder = new StringBuilder(url);
        UrlBuilder.appendClientWindow(facesContext, builder, hasQueryString, UTF_8.name());
        return builder.toString();
    }

    private static void setCurrentInstance(FacesContext context) throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, context);
    }
}
