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

package com.sun.faces.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The JUnit tests for the ExternalContextImpl class.
 */
public class ExternalContextImplTest {

    /**
     * Test getRequestCookieMap method.
     */
    @Test
    public void testGetRequestCookieMap() {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
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
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("foo", "bar");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

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
     * Test getRequestCookieMap method (test the unmodifiable nature of the
     * returned map).
     */
    @Test
    public void testGetRequestCookieMap3() {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("foo", "bar");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        Map<String, Object> requestCookieMap = externalContext.getRequestCookieMap();

        Iterator<Entry<String, Object>> entryIterator = requestCookieMap.entrySet().iterator();
        entryIterator.next();
        try {
            entryIterator.remove();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        Iterator<String> keyIterator = requestCookieMap.keySet().iterator();
        keyIterator.next();
        try {
            keyIterator.remove();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        Iterator<Object> valueIterator = requestCookieMap.values().iterator();
        valueIterator.next();
        try {
            valueIterator.remove();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        try {
            requestCookieMap.keySet().remove("test");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        try {
            requestCookieMap.values().remove("test");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    /**
     * Test getRequestCookieMap method (test the unsupported methods throw an
     * UnsupportedOperationException).
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetRequestCookieMap4() {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("foo", "bar");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, request, response);
        Map<String, Object> requestCookieMap = externalContext.getRequestCookieMap();
        boolean exceptionThrown = false;
        try {
            requestCookieMap.clear();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        verifySupplier(() -> requestCookieMap.put("foot", "bar"));
        verifyConsumer(m -> requestCookieMap.putAll((Map<? extends String, ? extends Object>) m), new HashMap<>());
        verifySupplier(() -> requestCookieMap.remove("foo"));
    }

    /**
     * Test that responseReset discards render output which is still buffered in the response output writer. The writer
     * is deliberately held on to across the reset, as that is what the response writer created earlier in the request
     * does.
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
     * Test that render output which has already been drained to the container's writer is beyond the reach of
     * responseReset, which matches the container's own semantics.
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
     * Test that release drains buffered render output to the container's writer without flushing it. Flushing would
     * commit the response, even when nothing is left to write, and thereby defeat the error page of a request which is
     * being aborted.
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

    private static class FlushRecordingWriter extends StringWriter {

        private boolean flushed;

        @Override
        public void flush() {
            flushed = true;
            super.flush();
        }

    }

    private ExternalContextImpl createExternalContext(StringWriter container) throws IOException {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
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
        } catch (UnsupportedOperationException e) {
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
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }
}
