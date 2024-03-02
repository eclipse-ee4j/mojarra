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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

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
            requestCookieMap.entrySet().remove("test");
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
