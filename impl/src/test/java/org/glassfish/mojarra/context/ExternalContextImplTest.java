/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import jakarta.faces.context.FacesContext;
import jakarta.faces.push.PushContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * The JUnit tests for the ExternalContextImpl class.
 */
public class ExternalContextImplTest {

    private static final String WEBSOCKET_PATH = "/ctx/jakarta.faces.push/x";

    /**
     * Test getRequestCookieMap method.
     */
    @Test
    public void testGetRequestCookieMap() {
        ServletContext servletContext = mockServletContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);

        assertNotNull(externalContext.getRequestCookieMap());
    }

    /**
     * Test getRequestCookieMap method (test supported methods).
     */
    @Test
    public void testGetRequestCookieMap2() {
        ServletContext servletContext = mockServletContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("foo", "bar");
        when(request.getCookies()).thenReturn(new Cookie[] { cookie });

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        Map<String, Object> requestCookieMap = externalContext.getRequestCookieMap();
        assertTrue(requestCookieMap.get("foo") instanceof Cookie);
        Cookie value = (Cookie) requestCookieMap.get("foo");

        assertTrue(value.getValue().equals("bar"));
        assertTrue(requestCookieMap.containsKey("foo"));
        assertTrue(requestCookieMap.containsValue(requestCookieMap.get("foo")));
        assertTrue(!requestCookieMap.entrySet().isEmpty());
        assertTrue(!requestCookieMap.values().isEmpty());
        assertTrue(!requestCookieMap.keySet().isEmpty());
        assertTrue(requestCookieMap.size() >= 1);
        assertTrue(!requestCookieMap.equals(new HashMap<>()));
    }

    /**
     * Test getRequestCookieMap method (test the unmodifiable nature of the returned map).
     */
    @Test
    public void testGetRequestCookieMap3() {
        ServletContext servletContext = mockServletContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("foo", "bar");
        when(request.getCookies()).thenReturn(new Cookie[] { cookie });

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        Map<String, Object> requestCookieMap = externalContext.getRequestCookieMap();

        Iterator<Entry<String, Object>> entryIterator = requestCookieMap.entrySet().iterator();
        entryIterator.next();
        try {
            entryIterator.remove();
            fail();
        }
        catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        Iterator<String> keyIterator = requestCookieMap.keySet().iterator();
        keyIterator.next();
        try {
            keyIterator.remove();
            fail();
        }
        catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        Iterator<Object> valueIterator = requestCookieMap.values().iterator();
        valueIterator.next();
        try {
            valueIterator.remove();
            fail();
        }
        catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        try {
            requestCookieMap.keySet().remove("test");
            fail();
        }
        catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        try {
            requestCookieMap.values().remove("test");
            fail();
        }
        catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    /**
     * Test getRequestCookieMap method (test the unsupported methods throw an UnsupportedOperationException).
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetRequestCookieMap4() {
        ServletContext servletContext = mockServletContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("foo", "bar");
        when(request.getCookies()).thenReturn(new Cookie[] { cookie });

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        Map<String, Object> requestCookieMap = externalContext.getRequestCookieMap();
        boolean exceptionThrown = false;
        try {
            requestCookieMap.clear();
        }
        catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        verifySupplier(() -> requestCookieMap.put("foot", "bar"));
        verifyConsumer(m -> requestCookieMap.putAll((Map<? extends String, ? extends Object>) m), new HashMap<>());
        verifySupplier(() -> requestCookieMap.remove("foo"));
    }

    /**
     * Test that responseReset discards render output which is still buffered in the response output writer. The writer is deliberately held on to across the
     * reset, as that is what the response writer created earlier in the request does.
     */
    @Test
    public void testResponseResetDiscardsBufferedOutput() throws IOException {
        StringWriter container = new StringWriter();
        ExternalContextImpl externalContext = createExternalContext(container);
        Writer writer = externalContext.getResponseOutputWriter();

        writer.write("aborted");
        externalContext.responseReset();
        writer.write("replacement");
        writer.flush();

        assertEquals("replacement", container.toString());
    }

    /**
     * Test that responseSendError discards render output which is still buffered in the response output writer.
     */
    @Test
    public void testResponseSendErrorDiscardsBufferedOutput() throws IOException {
        StringWriter container = new StringWriter();
        ExternalContextImpl externalContext = createExternalContext(container);
        Writer writer = externalContext.getResponseOutputWriter();

        writer.write("aborted");
        externalContext.responseSendError(500, null);
        writer.flush();

        assertEquals("", container.toString());
    }

    /**
     * Test that render output which has already been drained to the container's writer is beyond the reach of responseReset, which matches the container's own
     * semantics.
     */
    @Test
    public void testResponseResetDoesNotDiscardAlreadyDrainedOutput() throws IOException {
        StringWriter container = new StringWriter();
        ExternalContextImpl externalContext = createExternalContext(container);
        Writer writer = externalContext.getResponseOutputWriter();
        String drained = "x".repeat(8192);

        writer.write(drained);
        externalContext.responseReset();
        writer.flush();

        assertEquals(drained, container.toString());
    }

    /**
     * Test that release drains buffered render output to the container's writer without flushing it. Flushing would commit the response, even when nothing is
     * left to write, and thereby defeat the error page of a request which is being aborted.
     */
    @Test
    public void testReleaseDrainsWithoutFlushing() throws IOException {
        FlushRecordingWriter container = new FlushRecordingWriter();
        ExternalContextImpl externalContext = createExternalContext(container);

        externalContext.getResponseOutputWriter().write("rendered");
        externalContext.release();

        assertEquals("rendered", container.toString());
        assertFalse(container.flushed, "container writer must not be flushed by release()");
    }

    /**
     * Test that encodeWebsocketURL derives the websocket scheme from the request scheme: {@code https} gives {@code wss} and {@code http} gives {@code ws}, as
     * a browser refuses {@code ws} on a page served over {@code https}. The port comes from the context param when that is positive, else from the request.
     */
    @ParameterizedTest
    @CsvSource(
        {
            "https://host:8443/ctx/page.xhtml,          , wss://host:8443/ctx/jakarta.faces.push/x",
            "https://host/ctx/page.xhtml,               , wss://host/ctx/jakarta.faces.push/x",
            "https://host:443/ctx/page.xhtml,           , wss://host:443/ctx/jakarta.faces.push/x",
            "http://host:8080/ctx/page.xhtml,           , ws://host:8080/ctx/jakarta.faces.push/x",
            "http://host/ctx/page.xhtml,                , ws://host/ctx/jakarta.faces.push/x",
            "http://host:80/ctx/page.xhtml,             , ws://host:80/ctx/jakarta.faces.push/x",
            "HTTPS://host/ctx/page.xhtml,               , wss://host/ctx/jakarta.faces.push/x",
            "HTTP://host/ctx/page.xhtml,                , ws://host/ctx/jakarta.faces.push/x",
            "https://[::1]:8443/ctx/page.xhtml,         , wss://[::1]:8443/ctx/jakarta.faces.push/x",
            "https://http.example.com/ctx/page.xhtml,   , wss://http.example.com/ctx/jakarta.faces.push/x",
            "http://https.example.com/ctx/page.xhtml,   , ws://https.example.com/ctx/jakarta.faces.push/x",
            "https://host:8443/ctx/page.xhtml?a=b#c,    , wss://host:8443/ctx/jakarta.faces.push/x",
            "https://host:8443/ctx/page.xhtml,      9443, wss://host:9443/ctx/jakarta.faces.push/x",
            "https://host/ctx/page.xhtml,           9443, wss://host:9443/ctx/jakarta.faces.push/x",
            "http://host:8080/ctx/page.xhtml,       9080, ws://host:9080/ctx/jakarta.faces.push/x",
            "http://host/ctx/page.xhtml,            9080, ws://host:9080/ctx/jakarta.faces.push/x",
            "https://host:8443/ctx/page.xhtml,         0, wss://host:8443/ctx/jakarta.faces.push/x",
            "https://host:8443/ctx/page.xhtml,        -1, wss://host:8443/ctx/jakarta.faces.push/x",
            "http://host:8080/ctx/page.xhtml,          0, ws://host:8080/ctx/jakarta.faces.push/x",
        }
    )
    public void testEncodeWebsocketURLSchemeAndPort(String requestURL, String configuredPort, String expected) {
        assertEquals(expected, encodeWebsocketURL(requestURL, configuredPort, WEBSOCKET_PATH, UnaryOperator.identity()));
    }

    /**
     * Test that only the scheme of the request is replaced, so an occurrence of "http" elsewhere in the URL survives.
     */
    @ParameterizedTest
    @CsvSource(
        {
            "https://host/page.xhtml, /http/jakarta.faces.push/x,      wss://host/http/jakarta.faces.push/x",
            "https://host/page.xhtml, /https/jakarta.faces.push/http,  wss://host/https/jakarta.faces.push/http",
            "http://host/page.xhtml,  /http/jakarta.faces.push/https,  ws://host/http/jakarta.faces.push/https",
        }
    )
    public void testEncodeWebsocketURLKeepsHttpOutsideScheme(String requestURL, String url, String expected) {
        assertEquals(expected, encodeWebsocketURL(requestURL, null, url, UnaryOperator.identity()));
    }

    /**
     * Test that the websocket URL is passed through the response URL encoding, which is what {@code ExternalContext#encodeResourceURL} contributes.
     */
    @Test
    public void testEncodeWebsocketURLAppliesResponseEncoding() {
        String encoded = encodeWebsocketURL("https://host/ctx/page.xhtml", null, WEBSOCKET_PATH, url -> url + ";jsessionid=abc");

        assertEquals("wss://host/ctx/jakarta.faces.push/x;jsessionid=abc", encoded);
    }

    /**
     * Test that a request URL which cannot be parsed yields the input URL unchanged.
     */
    @Test
    public void testEncodeWebsocketURLReturnsInputOnMalformedRequestURL() {
        assertEquals(WEBSOCKET_PATH, encodeWebsocketURL("not-a-url", null, WEBSOCKET_PATH, UnaryOperator.identity()));
    }

    /**
     * Test that a null URL is refused with a NullPointerException, as the method contract demands.
     */
    @Test
    public void testEncodeWebsocketURLRejectsNull() {
        assertThrows(NullPointerException.class, () -> encodeWebsocketURL("https://host/ctx/page.xhtml", null, null, UnaryOperator.identity()));
    }

    private String encodeWebsocketURL(String requestURL, String configuredPort, String url, UnaryOperator<String> responseEncoder) {
        ServletContext servletContext = mockServletContext();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(servletContext.getInitParameter(PushContext.WEBSOCKET_ENDPOINT_PORT_PARAM_NAME)).thenReturn(configuredPort);
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        when(response.encodeURL(anyString())).thenAnswer(invocation -> responseEncoder.apply(invocation.getArgument(0)));

        try (MockedStatic<FacesContext> facesContext = mockStatic(FacesContext.class)) {
            FacesContext context = mock(FacesContext.class, RETURNS_DEEP_STUBS);
            when(context.getExternalContext().getContext()).thenReturn(servletContext);
            facesContext.when(FacesContext::getCurrentInstance).thenReturn(context);
            return new ExternalContextImpl(servletContext, request, response).encodeWebsocketURL(url);
        }
    }

    private static class FlushRecordingWriter extends StringWriter {

        private boolean flushed;

        @Override
        public void flush() {
            flushed = true;
            super.flush();
        }

    }

    private ExternalContextImpl createExternalContext(StringWriter container) throws IOException {
        ServletContext servletContext = mockServletContext();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(container));
        return new ExternalContextImpl(servletContext, request, response);
    }

    /**
     * Verify that the passed consumer throws an UnsupportedOperationException.
     *
     * @param consumer the consumer.
     * @param argument the argument.
     */
    private void verifyConsumer(Consumer<Object> consumer, Object argument) {
        boolean exceptionThrown = false;

        try {
            consumer.accept(argument);
        }
        catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    /**
     * Verify that the passed supplier throws an UnsupportedOperationException.
     *
     * @param supplier the supplier.
     */
    private void verifySupplier(Supplier<?> supplier) {
        boolean exceptionThrown = false;

        try {
            supplier.get();
        }
        catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    /**
     * ExternalContextImpl builds a WebConfiguration, which walks the init parameter names, so a bare mock is not enough.
     */
    private static ServletContext mockServletContext() {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        when(servletContext.getInitParameterNames()).thenAnswer(invocation -> Collections.enumeration(Collections.emptyList()));
        return servletContext;
    }

}
