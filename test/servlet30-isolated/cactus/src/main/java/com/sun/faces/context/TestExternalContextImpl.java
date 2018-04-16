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

// TestExternalContextImpl.java

package com.sun.faces.context;

import com.sun.faces.cactus.ServletFacesTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.faces.context.ExternalContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * <B>TestExternalContextImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestExternalContextImpl extends ServletFacesTestCase {

    // These constants identify positions in the boolean supported array;
    // Each one is named after the existing method in the Map interface;
    // For example, "ApplicationMap" implementation of Map interface
    // supports "put" method, but "RequestParameterMap" implementation may not;
    // So, for "ApplicationMap" you could say:
    //    supported[PUT]=true;
    // Intitially, all array elements are set to false;

    public static final int CLEAR = 0;
    public static final int CONTAINS_KEY = 1;
    public static final int CONTAINS_VALUE = 2;
    public static final int ENTRY_SET = 3;
    public static final int GET = 4;
    public static final int HASH_CODE = 5;
    public static final int IS_EMPTY = 6;
    public static final int KEY_SET = 7;
    public static final int PUT = 8;
    public static final int PUT_ALL = 9;
    public static final int REMOVE = 10;
    public static final int SIZE = 11;
    public static final int VALUES = 12;

    public boolean[] supported = new boolean[13];

    public TestExternalContextImpl() {
        super("TestExternalContext");
	initLocalHostPath();
    }


    public TestExternalContextImpl(String name) {
        super(name);
	initLocalHostPath();
    }

    private String localHostPath = "localhost:8080";

    private void initLocalHostPath() {
	String containerPort = System.getProperty("container.port");
	if (null == containerPort || 0 == containerPort.length()) {
	    containerPort = "8080";
	}
	localHostPath = "localhost:" + containerPort;
    }



    public void initializeSupported() {
        for (int i = 0; i < supported.length; i++) {
            supported[i] = false;
        }
    }


    // ------------------------------------------------------------- TestMethods


    ////////////////////////////////////////////////////////////////////////////
    // Tests for methods added in 2.0


    public void beginGetRequestContentLength(WebRequest req) {
        req.addParameter("foo", "bar", WebRequest.POST_METHOD);
    }

    public void testGetRequestContentLength() {

        ExternalContext ctx = getFacesContext().getExternalContext();
        assertEquals(Integer.valueOf(ctx.getRequestContentLength()), Integer.valueOf(7));
        
    }


    public void testAddResponseCookie() {

        ExternalContext ctx = getFacesContext().getExternalContext();

        // first cookie - no properties
        ctx.addResponseCookie("cookie1", "value1", null);

        // second cookie - empty map
        ctx.addResponseCookie("cookie2", "value2", Collections.<String,Object>emptyMap());

        // third cookie - domain specified
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("domain", "snoozer");
        ctx.addResponseCookie("cookie3", "value3", m);

        // fourth cookie - max age
        m.clear();
        m.put("maxAge", 360);
        ctx.addResponseCookie("cookie4", "value4", m);

        // fifth cookie - path
        m.clear();
        m.put("path", "/foo");
        ctx.addResponseCookie("cookie5", "value5", m);

        // sixth cookie - secure
        m.clear();
        m.put("secure", true);
        ctx.addResponseCookie("cookie6", "value6", m);

        // seventh cookie - multiple values
        m.clear();
        m.put("maxAge", 40);
        m.put("path", "/foobar");
        m.put("domain", "snoozer");
        ctx.addResponseCookie("cookie7", "value7", m);

        // invalid map property results in IllegalArgumentException
        m.clear();
        m.put("invalid", "invalid");
        try {
            ctx.addResponseCookie("cookie8", "value8", m);
            assertTrue(false);
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }

    }

    public void endAddResponseCookie(WebResponse res) {
        org.apache.cactus.Cookie c = res.getCookie("cookie1");
        assertNotNull(c);
        assertTrue("value1".equals(c.getValue()));
        assertTrue("localhost".equals(c.getDomain()));
        assertTrue("/test-servlet30_isolated-cactus".equals(c.getPath()));
        assertTrue(!c.isSecure());
        assertNull(c.getExpiryDate());

        c = res.getCookie("cookie2");
        assertNotNull(c);
        assertTrue("value2".equals(c.getValue()));
        assertTrue("localhost".equals(c.getDomain()));
        assertTrue("/test-servlet30_isolated-cactus".equals(c.getPath()));
        assertTrue(!c.isSecure());
        assertNull(c.getExpiryDate());

        c = res.getCookie("cookie3");
        assertNotNull(c);
        assertTrue("value3".equals(c.getValue()));
        assertTrue("snoozer".equals(c.getDomain()));
        assertTrue("/test-servlet30_isolated-cactus".equals(c.getPath()));
        assertTrue(!c.isSecure());
        assertNull(c.getExpiryDate());

        c = res.getCookie("cookie4");
        assertNotNull(c);
        assertTrue("value4".equals(c.getValue()));
        assertTrue("localhost".equals(c.getDomain()));
        assertTrue("/test-servlet30_isolated-cactus".equals(c.getPath()));
        assertTrue(!c.isSecure());
        assertNotNull(c.getExpiryDate());

        c = res.getCookie("cookie5");
        assertNotNull(c);
        assertTrue("value5".equals(c.getValue()));
        assertTrue("localhost".equals(c.getDomain()));
        assertTrue("/foo".equals(c.getPath()));
        assertTrue(!c.isSecure());
        assertNull(c.getExpiryDate());

        c = res.getCookie("cookie6");
        assertNotNull(c);
        assertTrue("value6".equals(c.getValue()));
        assertTrue("localhost".equals(c.getDomain()));
        assertTrue("/test-servlet30_isolated-cactus".equals(c.getPath()));
        assertTrue(c.isSecure());
        assertNull(c.getExpiryDate());

        c = res.getCookie("cookie7");
        assertNotNull(c);
        assertTrue("value7".equals(c.getValue()));
        assertTrue("snoozer".equals(c.getDomain()));
        assertTrue("/foobar".equals(c.getPath()));
        assertTrue(!c.isSecure());
        assertNotNull(c.getExpiryDate());
    }


    public void testInvalidateSession() {
        ExternalContext ctx = getFacesContext().getExternalContext();
        Map<String,Object> map = ctx.getSessionMap();
        map.put("foo", "bar");
        ctx.invalidateSession();
        try {
            session.getAttribute("foor");
            assertTrue(false);
        } catch (IllegalStateException ignored) {

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    public void testIsSecure() {
        ExternalContext ctx = getFacesContext().getExternalContext();
        assertTrue(ctx.isSecure() == request.isSecure());    
    }

    public void testRequestScheme() {
        ExternalContext ctx = getFacesContext().getExternalContext();
        assertTrue(ctx.getRequestScheme().equals(request.getScheme()));
    }

    public void testServerPort() {
        ExternalContext ctx = getFacesContext().getExternalContext();
        assertTrue(ctx.getRequestServerPort() == request.getServerPort());
    }

    public void testServerName() {
        ExternalContext ctx = getFacesContext().getExternalContext();
        assertTrue(ctx.getRequestServerName().equals(request.getServerName()));
    }

    public void testGetResponseOutputStream() throws Exception {
        ExternalContext ctx = getFacesContext().getExternalContext();
        assertTrue(ctx.getResponseOutputStream() != null);
    }

    public void testResponseContentType() {
        ExternalContext ctx = getFacesContext().getExternalContext();
        ctx.setResponseContentType("text/plain");
        response.getContentType().contains("text/plain");
    }


    ////////////////////////////////////////////////////////////////////////////

//PENDING(rogerk) the unit test for ExternalContext should cast the Object instances
// to the expected type.  It should test put and get.  It should test the
// UnsupportedOperationException is thrown when expected, etc.

// Tests constructor's ability to create contained objects...
    public void testConstructor() {
        ExternalContextImpl ecImpl = new ExternalContextImpl(
            getConfig().getServletContext(),
            getRequest(),
            getResponse());

        System.out.println("Testing getSession not null...");
        assertTrue(null != ecImpl.getSession(false));
        System.out.println("Testing getContext not null...");
        assertTrue(null != ecImpl.getContext());
        System.out.println("Testing getRequest not null...");
        assertTrue(null != ecImpl.getRequest());
        System.out.println("Testing getResponse not null...");
        assertTrue(null != ecImpl.getResponse());
        System.out.println("Testing getApplicationMap not null...");
        assertTrue(null != ecImpl.getApplicationMap());
        System.out.println("Testing getSessionMap not null...");
        assertTrue(null != ecImpl.getSessionMap());
        System.out.println("Testing getRequestMap not null...");
        assertTrue(null != ecImpl.getRequestMap());
    }


    public void testServletContext() {
        getConfig().getServletContext().setAttribute("foo", "bar");
        ServletContext sc = (ServletContext) getFacesContext()
            .getExternalContext()
            .getContext();
        assertTrue(null != sc.getAttribute("foo"));
        assertTrue(
            null !=
            getFacesContext().getExternalContext().getApplicationMap().get(
                "foo"));
    }


    public void testGetSession() {
        Object session = getFacesContext().getExternalContext().getSession(
            false);
        assertTrue(null != session);
        assertTrue(session instanceof HttpSession);
    }


    public void testGetRequest() {
        Object request = getFacesContext().getExternalContext().getRequest();
        assertTrue(null != request);
        assertTrue(request instanceof ServletRequest);
    }


    public void testGetResponse() {
        Object response = getFacesContext().getExternalContext().getResponse();
        assertTrue(null != response);
        assertTrue(response instanceof ServletResponse);
    }


    public void beginGetRequestParameterNames(WebRequest theRequest) {
        theRequest.addParameter("One", "one");
        theRequest.addParameter("Two", "two");
        theRequest.addParameter("Three", "three");
    }


    public void testGetRequestParameterNames() {
        Iterator iter = getFacesContext().getExternalContext()
            .getRequestParameterNames();
        boolean oneFound = false, twoFound = false, threeFound = false;
        while (iter.hasNext()) {
            String paramName = (String) iter.next();
            if (paramName.equals("One")) {
                oneFound = true;
            } else if (paramName.equals("Two")) {
                twoFound = true;
            } else if (paramName.equals("Three")) {
                threeFound = true;
            }
        }
        assertTrue(oneFound && twoFound && threeFound);
    }


    public void beginGetLocale(WebRequest theRequest) {
        theRequest.addHeader("Accept-Language", "de");
    }


    public void testGetLocale() {
        Locale locale = getFacesContext().getExternalContext()
            .getRequestLocale();
        assertTrue(locale.getLanguage().equals("de"));
    }


    public void beginGetRequestPathInfo(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test-servlet30_isolated-cactus", "/foo", "/bar", null);
    }


    public void testGetRequestPathInfo() {
        String pathInfo = getFacesContext().getExternalContext()
            .getRequestPathInfo();
        assertTrue(pathInfo.equals("/bar"));
    }

    // PENDING(craigmcc) - Comment out this test because on my platform
    // the getRequestCookies() call returns null
    /*
    public void beginGetRequestCookies(WebRequest theRequest) {
        theRequest.addCookie("One", "one");
        theRequest.addCookie("Two", "two");
        theRequest.addCookie("Three", "three");
    }

    public void testGetRequestCookies() {
        Cookie[] cookies = getFacesContext().getExternalContext().getRequestCookies();
        boolean oneNFound=false,twoNFound=false,threeNFound=false;
        boolean oneVFound=false,twoVFound=false,threeVFound=false;
        for (int i=0; i<cookies.length; i++) {
            if (cookies[i].getName().equals("One")) {
                oneNFound = true;
            } else if (cookies[i].getName().equals("Two")) {
                twoNFound = true;
            } else if (cookies[i].getName().equals("Three")) {
                threeNFound = true;
            }
            if (cookies[i].getValue().equals("one")) {
                oneVFound = true;
            } else if (cookies[i].getValue().equals("two")) {
                twoVFound = true;
            } else if (cookies[i].getValue().equals("three")) {
                threeVFound = true;
            }
        }
        assertTrue(oneNFound && twoNFound && threeNFound &&
            oneVFound && twoVFound && threeVFound);
    }
    */

    public void beginGetRequestContextPath(WebRequest theRequest) {
        theRequest.setURL(localHostPath, "/test-servlet30_isolated-cactus", "/foo", "/bar", null);
    }


    public void testGetRequestContextPath() {
        String contextPath = getFacesContext().getExternalContext()
            .getRequestContextPath();
        assertTrue(contextPath.equals("/test-servlet30_isolated-cactus"));
    }


    public void testGetInitParameter() {
        String expectedValue = config.getServletContext().getInitParameter(
            "testInitParam");
        String value = getFacesContext().getExternalContext().getInitParameter(
            "testInitParam");
        assertTrue(expectedValue.equals(value));
    }


    public void testGetResourcePaths() {
        Set paths = getFacesContext().getExternalContext().getResourcePaths(
            "/");
        Object[] pathArray = paths.toArray();
        assertTrue(pathArray.length > 0);
        boolean foundIt = false;
        // assert that WEB-INF is found (at least)
        for (int i = 0; i < pathArray.length; i++) {
            if (pathArray[i].equals("/WEB-INF/")) {
                foundIt = true;
                break;
            }
        }
        assertTrue(foundIt);
    }


    public void testGetResourceAsStream() {
        InputStream is = getFacesContext().getExternalContext()
            .getResourceAsStream("/WEB-INF/web.xml");
        assertTrue(null != is);
    }

    //PENDNG(rogerk) not sure how to handle these...

    public void testEncodeActionURL() {
    }


    public void testEncodeResourceURL() {
    }


    public void testEncodeNamespaceURL() {
    }


    public void testEncodeURL() {
    }


    public void testDispatchMessage() {
    }


    public void testApplicatonMap() {
        System.out.println("Testing ApplicationMap...");
        Map applicationMap = getFacesContext().getExternalContext()
            .getApplicationMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[PUT] = true;
        supported[REMOVE] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        supported[PUT_ALL] = true;
        supported[CLEAR] = true;

        testUnsupportedExceptions(applicationMap, supported);

        System.out.println(
            "    Testing supported methods of ApplicationMap...");
        applicationMap.put("foo", "bar");
        String value = (String) applicationMap.get("foo");
        assertTrue(value.equals("bar"));
        String removed = (String) applicationMap.remove("foo");
        assertTrue(null == (String) applicationMap.get("foo"));
        assertTrue(removed.equals("bar"));
        applicationMap.put("foo", "bar");
        assertTrue(applicationMap.containsKey("foo"));
        assertTrue(applicationMap.containsValue("bar"));
        assertTrue(!applicationMap.entrySet().isEmpty());
        assertTrue(!applicationMap.values().isEmpty());
        assertTrue(!applicationMap.keySet().isEmpty());
        assertTrue(applicationMap.size() >= 1);
        assertTrue(
            applicationMap.hashCode() ==
            getFacesContext().getExternalContext().getApplicationMap()
            .hashCode());
        assertTrue(applicationMap.equals(
            getFacesContext().getExternalContext().getApplicationMap()));
        assertTrue(!applicationMap.equals(null));
        assertTrue(!applicationMap.equals(new HashMap()));
        applicationMap.remove("foo");

       if (applicationMap.isEmpty()) {
            applicationMap.put("some", "value");
        }
        /* Commented out since it appears that certain attributes
           cannot be removed from the ServletContext when running against
           glassfish
        Map cloneMap = new HashMap(applicationMap);
        applicationMap.clear();
        assertTrue(applicationMap.isEmpty());
        applicationMap.putAll(cloneMap);
        assertTrue(!applicationMap.isEmpty());*/

        // ensure EntrySet operations reflect on the underlying map.
        applicationMap.put("key1", "value1");
        Set entrySet = applicationMap.entrySet();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                entrySet.remove(entry);
            }
        }
        assertTrue(applicationMap.get("key1") == null);

        applicationMap.put("key1", "value1");

        entrySet = applicationMap.entrySet();
        int currentSize = applicationMap.size();
        ArrayList list = new ArrayList();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                list.add(entry);
                break;
            }
        }
        entrySet.removeAll(list);
        assertTrue(applicationMap.size() == (currentSize - 1));

        //Map cloneMap = new HashMap(applicationMap);
        applicationMap.put("key1", "value1");
        list = new ArrayList();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                list.add(entry);
                break;
            }
        }
         /* Commented out since it appears that certain attributes
           cannot be removed from the ServletContext when running against
           glassfish
        assertTrue(entrySet.retainAll(list));
        assertTrue(applicationMap.size() == 1);
        applicationMap.clear();
        applicationMap.putAll(cloneMap); */

        // next validate Iterator.remove goes through to the backing Map.
        applicationMap.put("key1", "value1");
        for (Iterator i = applicationMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                i.remove();
            }
        }
        assertTrue(applicationMap.get("key1") == null);

        applicationMap.put("key1", "value1");
        for (Iterator i = applicationMap.keySet().iterator(); i.hasNext(); ) {
            String entry = (String) i.next();
            if ("key1".equals(entry)) {
                i.remove();
            }
        }
        assertTrue(applicationMap.get("key1") == null);

        applicationMap.put("key1", "value1");
        applicationMap.put("key2", "value1");
        for (Iterator i = applicationMap.values().iterator(); i.hasNext(); ) {
            Object val = i.next();
            if ("value1".equals(val)) {
                i.remove();
            }
        }
        assertTrue(applicationMap.get("key1") == null);
        assertTrue(applicationMap.get("key2") == null);


        // ensure IllegalStateException if Iterator isn't properly positioned
        Iterator i = applicationMap.entrySet().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        i = applicationMap.keySet().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        i = applicationMap.values().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        // Breaks V3 - JSP taglib cache is stored in servlet context.
        // If this is removed, JSPs won't compile anymore
        
        // ensure IllegalStateException if Iterator.remove() is called more than
        // once per each next() call
        //i = applicationMap.entrySet().iterator();
        //i.next();
        //i.remove();
        //try {
        //    i.remove();
        //    assertTrue(false);
        //} catch (Exception e) {
        //    assertTrue(e instanceof IllegalStateException);
        //}

        //i = applicationMap.keySet().iterator();
        //i.next();
        //i.remove();
        //try {
        //    i.remove();
        //    assertTrue(false);
        //} catch (Exception e) {
        //    assertTrue(e instanceof IllegalStateException);
        //}
        //
        //i = applicationMap.values().iterator();
        //i.next();
        //i.remove();
        //try {
        //    i.remove();
        //    assertTrue(false);
        //} catch (Exception e) {
        //    assertTrue(e instanceof IllegalStateException);
        //}
    }


    public void testSessionMap() {
        System.out.println("Testing SessionMap...");
        Map sessionMap = getFacesContext().getExternalContext().getSessionMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[PUT] = true;
        supported[REMOVE] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        supported[PUT_ALL] = true;
        supported[CLEAR] = true;

        testUnsupportedExceptions(sessionMap, supported);

        System.out.println("    Testing supported methods of SessionMap...");
        sessionMap.put("foo", "bar");
        String value = (String) sessionMap.get("foo");
        assertTrue(value.equals("bar"));
        String removed = (String) sessionMap.remove("foo");
        assertTrue(null == (String) sessionMap.get("foo"));
        assertTrue(removed.equals("bar"));
        sessionMap.put("foo", "bar");
        assertTrue(sessionMap.containsKey("foo"));
        assertTrue(sessionMap.containsValue("bar"));
        assertTrue(!sessionMap.entrySet().isEmpty());
        assertTrue(!sessionMap.values().isEmpty());
        assertTrue(!sessionMap.keySet().isEmpty());
        assertTrue(sessionMap.size() >= 1);
        assertTrue(sessionMap.hashCode() ==
                   getFacesContext().getExternalContext().getSessionMap()
                   .hashCode());
        assertTrue(sessionMap.equals(
            getFacesContext().getExternalContext().getSessionMap()));
        assertTrue(!sessionMap.equals(null));
        assertTrue(!sessionMap.equals(new HashMap()));
        sessionMap.remove("foo");
        if (sessionMap.isEmpty()) {
            sessionMap.put("some", "value");
        }
        Map cloneMap = new HashMap(sessionMap);
        sessionMap.clear();
        assertTrue(sessionMap.isEmpty());
        sessionMap.putAll(cloneMap);
        assertTrue(!sessionMap.isEmpty());

        // ensure EntrySet operations reflect on the underlying map.
        sessionMap.put("key1", "value1");
        Set entrySet = sessionMap.entrySet();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                entrySet.remove(entry);
            }
        }
        assertTrue(sessionMap.get("key1") == null);

        sessionMap.put("key1", "value1");

        entrySet = sessionMap.entrySet();
        int currentSize = sessionMap.size();
        ArrayList list = new ArrayList();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                list.add(entry);
                break;
            }
        }
        entrySet.removeAll(list);
        assertTrue(sessionMap.size() == (currentSize - 1));

        cloneMap = new HashMap(sessionMap);
        sessionMap.put("key1", "value1");
        list = new ArrayList();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                list.add(entry);
                break;
            }
        }
        assertTrue(entrySet.retainAll(list));
        assertTrue(sessionMap.size() == 1);
        sessionMap.clear();
        sessionMap.putAll(cloneMap);

        // next validate Iterator.remove goes through to the backing Map.
        cloneMap = new HashMap(sessionMap);
        for (Iterator i = sessionMap.entrySet().iterator(); i.hasNext(); ) {
            i.next();
            i.remove();
        }
        assertTrue(sessionMap.isEmpty());
        sessionMap.putAll(cloneMap);

        sessionMap.put("key1", "value1");
        for (Iterator i = sessionMap.keySet().iterator(); i.hasNext(); ) {
            String entry = (String) i.next();
            if ("key1".equals(entry)) {
                i.remove();
            }
        }
        assertTrue(sessionMap.get("key1") == null);

        sessionMap.put("key1", "value1");
        sessionMap.put("key2", "value1");
        for (Iterator i = sessionMap.values().iterator(); i.hasNext(); ) {
            Object val = i.next();
            if ("value1".equals(val)) {
                i.remove();
            }
        }
        assertTrue(sessionMap.get("key1") == null);
        assertTrue(sessionMap.get("key2") == null);

        // ensure IllegalStateException if Iterator isn't properly positioned
        Iterator i = sessionMap.entrySet().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        i = sessionMap.keySet().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        i = sessionMap.values().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        // ensure IllegalStateException if Iterator.remove() is called more than
        // once per each next() call
        sessionMap.put("key1", "value1");
        i = sessionMap.entrySet().iterator();
        i.next();
        i.remove();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        sessionMap.put("key1", "value1");
        i = sessionMap.keySet().iterator();
        i.next();
        i.remove();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        sessionMap.put("key1", "value1");
        i = sessionMap.values().iterator();
        i.next();
        i.remove();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }


    public void testRequestMap() {
        System.out.println("Testing RequestMap...");
        Map requestMap = getFacesContext().getExternalContext().getRequestMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[PUT] = true;
        supported[REMOVE] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        supported[PUT_ALL] = true;
        supported[CLEAR] = true;
        testUnsupportedExceptions(requestMap, supported);

        System.out.println("    Testing supported methods of RequestMap...");
        requestMap.put("foo", "bar");
        String value = (String) requestMap.get("foo");
        assertTrue(value.equals("bar"));
        String removed = (String) requestMap.remove("foo");
        assertTrue(null == (String) requestMap.get("foo"));
        assertTrue(removed.equals("bar"));
        requestMap.put("foo", "bar");
        assertTrue(requestMap.containsKey("foo"));
        assertTrue(requestMap.containsValue("bar"));
        assertTrue(!requestMap.entrySet().isEmpty());
        assertTrue(!requestMap.values().isEmpty());
        assertTrue(!requestMap.keySet().isEmpty());
        assertTrue(requestMap.size() >= 1);
        assertTrue(requestMap.hashCode() ==
                   getFacesContext().getExternalContext().getRequestMap()
                   .hashCode());
        assertTrue(requestMap.equals(
            getFacesContext().getExternalContext().getRequestMap()));
        assertTrue(!requestMap.equals(null));
        assertTrue(!requestMap.equals(new HashMap()));
        requestMap.remove("foo");

        if (requestMap.isEmpty()) {
            requestMap.put("some", "value");
        }
        Map cloneMap = new HashMap(requestMap);
        requestMap.clear();
        assertTrue(requestMap.isEmpty());
        requestMap.putAll(cloneMap);
        assertTrue(!requestMap.isEmpty());

        // ensure EntrySet operations reflect on the underlying map.
        requestMap.put("key1", "value1");
        Set entrySet = requestMap.entrySet();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                entrySet.remove(entry);
            }
        }
        assertTrue(requestMap.get("key1") == null);

        requestMap.put("key1", "value1");

        entrySet = requestMap.entrySet();
        int currentSize = requestMap.size();
        ArrayList list = new ArrayList();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                list.add(entry);
                break;
            }
        }
        entrySet.removeAll(list);
        assertTrue(requestMap.size() == (currentSize - 1));

        cloneMap = new HashMap(requestMap);
        requestMap.put("key1", "value1");
        list = new ArrayList();
        for (Iterator i = entrySet.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            if ("key1".equals(entry.getKey())) {
                list.add(entry);
                break;
            }
        }
        assertTrue(entrySet.retainAll(list));
        assertTrue(requestMap.size() == 1);
        requestMap.clear();
        requestMap.putAll(cloneMap);

        // next validate Iterator.remove goes through to the backing Map.
        cloneMap = new HashMap(requestMap);
        for (Iterator i = requestMap.entrySet().iterator(); i.hasNext(); ) {
            i.next();
            i.remove();
        }
        assertTrue(requestMap.isEmpty());
        requestMap.putAll(cloneMap);

        requestMap.put("key1", "value1");
        for (Iterator i = requestMap.keySet().iterator(); i.hasNext(); ) {
            String entry = (String) i.next();
            if ("key1".equals(entry)) {
                i.remove();
            }
        }
        assertTrue(requestMap.get("key1") == null);

        requestMap.put("key1", "value1");
        requestMap.put("key2", "value1");
        for (Iterator i = requestMap.values().iterator(); i.hasNext(); ) {
            Object val = i.next();
            if ("value1".equals(val)) {
                i.remove();
            }
        }
        assertTrue(requestMap.get("key1") == null);
        assertTrue(requestMap.get("key2") == null);

        // ensure IllegalStateException if Iterator isn't properly positioned
        requestMap.put("key1", "value1");
        Iterator i = requestMap.entrySet().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        requestMap.put("key1", "value1");
        i = requestMap.keySet().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        requestMap.put("key1", "value1");
        i = requestMap.values().iterator();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        // ensure IllegalStateException if Iterator.remove() is called more than
        // once per each next() call
        requestMap.put("key1", "value1");
        i = requestMap.entrySet().iterator();
        i.next();
        i.remove();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        requestMap.put("key1", "value1");
        i = requestMap.keySet().iterator();
        i.next();
        i.remove();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

        requestMap.put("key1", "value1");
        i = requestMap.values().iterator();
        i.next();
        i.remove();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }


    public void beginRequestParameterMap(WebRequest theRequest) {
        theRequest.addParameter("foo", "bar");
    }


    public void testRequestParameterMap() {
        System.out.println("Testing RequestParameterMap...");
        Map requestParameterMap = getFacesContext().getExternalContext()
            .getRequestParameterMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        testUnsupportedExceptions(requestParameterMap, supported);

        System.out.println(
            "    Testing supported methods of RequestParameterMap...");
        assertTrue(requestParameterMap.get("foo") instanceof String);
        String value = (String) requestParameterMap.get("foo");
        assertTrue(value.equals("bar"));
        assertTrue(requestParameterMap.containsKey("foo"));
        assertTrue(requestParameterMap.containsValue("bar"));
        assertTrue(!requestParameterMap.entrySet().isEmpty());
        assertTrue(!requestParameterMap.values().isEmpty());
        assertTrue(!requestParameterMap.keySet().isEmpty());
        assertTrue(requestParameterMap.size() >= 1);
        assertTrue(
            requestParameterMap.hashCode() ==
            getFacesContext().getExternalContext().getRequestParameterMap()
            .hashCode());
        assertTrue(requestParameterMap.equals(
            getFacesContext().getExternalContext().getRequestParameterMap()));
        assertTrue(!requestParameterMap.equals(null));
        assertTrue(!requestParameterMap.equals(new HashMap()));

        // ensure we can't modify the map using Iterator.remove();
        Iterator i = requestParameterMap.entrySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestParameterMap.keySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestParameterMap.values().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        // ensure we can't remove elements from the Set.
        try {
            requestParameterMap.entrySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestParameterMap.keySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestParameterMap.values().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }


    public void beginRequestParameterValuesMap(WebRequest theRequest) {
        theRequest.addParameter("foo", "one");
        theRequest.addParameter("foo", "two");
        theRequest.addParameter("foo", "three");
    }


    public void testRequestParameterValuesMap() {
        System.out.println("Testing RequestParameterValuesMap...");
        Map requestParameterValuesMap = getFacesContext().getExternalContext()
            .getRequestParameterValuesMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        testUnsupportedExceptions(requestParameterValuesMap, supported);

        System.out.println(
            "    Testing supported methods of RequesParameterValuesMap...");
        assertTrue(requestParameterValuesMap.get("foo") instanceof String[]);
        String[] returnValues = (String[]) requestParameterValuesMap.get("foo");
        String[] values = {"one", "two", "three", };
        assertTrue(Arrays.equals(values, returnValues));
        assertTrue(requestParameterValuesMap.containsKey("foo"));
        assertTrue(
            requestParameterValuesMap.containsValue(
                request.getParameterValues("foo")));
        assertTrue(!requestParameterValuesMap.entrySet().isEmpty());
        assertTrue(!requestParameterValuesMap.values().isEmpty());
        assertTrue(!requestParameterValuesMap.keySet().isEmpty());
        assertTrue(requestParameterValuesMap.size() >= 1);
        assertTrue(
            requestParameterValuesMap.hashCode() ==
            getFacesContext().getExternalContext()
            .getRequestParameterValuesMap()
            .hashCode());
        assertTrue(
            requestParameterValuesMap.equals(
                getFacesContext().getExternalContext()
                .getRequestParameterValuesMap()));
        assertTrue(!requestParameterValuesMap.equals(null));
        assertTrue(!requestParameterValuesMap.equals(new HashMap()));

        // ensure we can't modify the map using Iterator.remove();
        Iterator i = requestParameterValuesMap.entrySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestParameterValuesMap.keySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestParameterValuesMap.values().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        // ensure we can't remove elements from the Set.
        try {
            requestParameterValuesMap.entrySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestParameterValuesMap.keySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestParameterValuesMap.values().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }


    public void beginRequestHeaderMap(WebRequest theRequest) {
        theRequest.addHeader("foo", "bar");
    }


    public void testRequestHeaderMap() {
        System.out.println("Testing RequestHeaderMap...");
        Map requestHeaderMap = getFacesContext().getExternalContext()
            .getRequestHeaderMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        testUnsupportedExceptions(requestHeaderMap, supported);

        System.out.println(
            "    Testing supported methods of RequesHeaderMap...");
        assertTrue(requestHeaderMap.get("foo") instanceof String);
        String value = (String) requestHeaderMap.get("foo");
        assertTrue(value.equals("bar"));
        assertTrue(requestHeaderMap.containsKey("foo"));
        assertTrue(requestHeaderMap.containsValue("bar"));
        assertTrue(!requestHeaderMap.entrySet().isEmpty());
        assertTrue(!requestHeaderMap.values().isEmpty());
        assertTrue(!requestHeaderMap.keySet().isEmpty());
        assertTrue(requestHeaderMap.size() >= 1);
        assertTrue(
            requestHeaderMap.hashCode() ==
            getFacesContext().getExternalContext().getRequestHeaderMap()
            .hashCode());
        assertTrue(requestHeaderMap.equals(
            getFacesContext().getExternalContext().getRequestHeaderMap()));
        assertTrue(!requestHeaderMap.equals(null));
        assertTrue(!requestHeaderMap.equals(new HashMap()));

         // ensure we can't modify the map using Iterator.remove();
        Iterator i = requestHeaderMap.entrySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestHeaderMap.keySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestHeaderMap.values().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        // ensure we can't remove elements from the Set.
        try {
            requestHeaderMap.entrySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestHeaderMap.keySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestHeaderMap.values().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }


    public void beginRequestHeaderValuesMap(WebRequest theRequest) {
        theRequest.addHeader("foo", "one");
        theRequest.addHeader("foo", "two");
        theRequest.addHeader("foo", "three");
    }


    public void testRequestHeaderValuesMap() {
        System.out.println("Testing RequestHeaderValuesMap...");
        Map requestHeaderValuesMap = getFacesContext().getExternalContext()
            .getRequestHeaderValuesMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        testUnsupportedExceptions(requestHeaderValuesMap, supported);

        System.out.println(
            "    Testing supported methods of RequesHeaderValuesMap...");
        assertTrue(requestHeaderValuesMap.get("One") instanceof String[]);
        String[] mapValues = (String[])requestHeaderValuesMap.get("foo");
        String returnValues = null;
        for (int k=0; k<mapValues.length; k++) {
            returnValues = mapValues[k];
        }

        String value = "one,two,three";
        assertTrue(returnValues.equals(value));
        assertTrue(requestHeaderValuesMap.containsKey("foo"));
        Enumeration headerEnum = request.getHeaders("foo");
        List headerList = new ArrayList();
        while (headerEnum.hasMoreElements()) {
            headerList.add((String)headerEnum.nextElement());
        }
        assertTrue(
            requestHeaderValuesMap.containsValue(headerList.toArray(new String[headerList.size()])));
        assertTrue(!requestHeaderValuesMap.containsValue(null));
        assertTrue(!requestHeaderValuesMap.containsValue(new Integer(1)));
        assertTrue(!requestHeaderValuesMap.containsValue(new String[]{"one", "two", "three"}));
        assertTrue(requestHeaderValuesMap.containsValue(new String[]{"one,two,three"}));
        assertTrue(!requestHeaderValuesMap.containsValue(new String[]{"one,three,two"}));
        assertTrue(!requestHeaderValuesMap.containsValue(new String[]{"one,two,three", "four"}));
        assertTrue(!requestHeaderValuesMap.entrySet().isEmpty());
        assertTrue(!requestHeaderValuesMap.values().isEmpty());
        assertTrue(!requestHeaderValuesMap.keySet().isEmpty());
        assertTrue(requestHeaderValuesMap.size() >= 1);
        assertTrue(
            requestHeaderValuesMap.hashCode() ==
            getFacesContext().getExternalContext().getRequestHeaderValuesMap()
            .hashCode());
        assertTrue(
            requestHeaderValuesMap.equals(
                getFacesContext().getExternalContext()
                .getRequestHeaderValuesMap()));
        assertTrue(!requestHeaderValuesMap.equals(null));
        assertTrue(!requestHeaderValuesMap.equals(new HashMap()));

         // ensure we can't modify the map using Iterator.remove();
        Iterator i = requestHeaderValuesMap.entrySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestHeaderValuesMap.keySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = requestHeaderValuesMap.values().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        // ensure we can't remove elements from the Set.
        try {
            requestHeaderValuesMap.entrySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestHeaderValuesMap.keySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            requestHeaderValuesMap.values().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    public void testNoCookieEnum() {

        Map requestCookieMap = getFacesContext().getExternalContext().getRequestCookieMap();
        try {
            Set entries = requestCookieMap.entrySet();
            for (Object o : entries) {
                // iterate to cause the failure
            }
        } catch (Exception e) {
            assertTrue(false);
        }
    }


    public void beginRequestCookieMap(WebRequest theRequest) {
    }

    public void testRequestCookieMap() {
    }


    public void testInitParameterMap() {
        System.out.println("Testing InitParameterMap...");
        String expectedValue = config.getServletContext().getInitParameter(
            "testInitParam");
        Map initParameterMap = getFacesContext().getExternalContext()
            .getInitParameterMap();
        System.out.println("    Testing UnsupportedOperationException(s)...");
        initializeSupported();
        supported[GET] = true;
        supported[CONTAINS_KEY] = true;
        supported[CONTAINS_VALUE] = true;
        supported[ENTRY_SET] = true;
        supported[VALUES] = true;
        supported[HASH_CODE] = true;
        supported[IS_EMPTY] = true;
        supported[KEY_SET] = true;
        supported[SIZE] = true;
        testUnsupportedExceptions(initParameterMap, supported);

        System.out.println(
            "    Testing supported methods of InitParameterMap...");
        assertTrue(initParameterMap.get("testInitParam") instanceof String);
        assertTrue(
            (initParameterMap.get("testInitParam")).equals(expectedValue));
        assertTrue(initParameterMap.containsKey("testInitParam"));
        assertTrue(initParameterMap.containsValue(expectedValue));
        assertTrue(!initParameterMap.entrySet().isEmpty());
        assertTrue(!initParameterMap.values().isEmpty());
        assertTrue(!initParameterMap.keySet().isEmpty());
        assertTrue(initParameterMap.size() >= 1);
        assertTrue(
            initParameterMap.hashCode() ==
            getFacesContext().getExternalContext().getInitParameterMap()
            .hashCode());
        assertTrue(initParameterMap.equals(
            getFacesContext().getExternalContext().getInitParameterMap()));
        assertTrue(!initParameterMap.equals(null));
        assertTrue(!initParameterMap.equals(new HashMap()));

         // ensure we can't modify the map using Iterator.remove();
        Iterator i = initParameterMap.entrySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = initParameterMap.keySet().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        i = initParameterMap.values().iterator();
        i.next();
        try {
            i.remove();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

        // ensure we can't remove elements from the Set.
        try {
            initParameterMap.entrySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            initParameterMap.keySet().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }

         // ensure we can't remove elements from the Set.
        try {
            initParameterMap.values().remove("test");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }


    private void testUnsupportedExceptions(Map map, boolean[] supported) {

        boolean exceptionThrown = false;

        if (!supported[CLEAR]) {
            exceptionThrown = false;
            try {
                map.clear();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[CONTAINS_KEY]) {
            exceptionThrown = false;
            try {
                map.containsKey("foo");
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[CONTAINS_VALUE]) {
            exceptionThrown = false;
            try {
                map.containsValue("foo");
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[ENTRY_SET]) {
            exceptionThrown = false;
            try {
                map.entrySet();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[GET]) {
            exceptionThrown = false;
            try {
                map.get("foo");
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[HASH_CODE]) {
            exceptionThrown = false;
            try {
                map.hashCode();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[IS_EMPTY]) {
            exceptionThrown = false;
            try {
                map.isEmpty();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[KEY_SET]) {
            exceptionThrown = false;
            try {
                map.keySet();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[PUT]) {
            exceptionThrown = false;
            try {
                map.put("foo", "bar");
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[PUT_ALL]) {
            exceptionThrown = false;
            try {
                map.putAll(new HashMap());
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[REMOVE]) {
            exceptionThrown = false;
            try {
                map.remove("foo");
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }

        if (!supported[SIZE]) {
            exceptionThrown = false;
            try {
                map.size();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);

        }

        if (!supported[VALUES]) {
            exceptionThrown = false;
            try {
                map.values();
            } catch (UnsupportedOperationException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        }
    }

} // end of class TestExternalContextImpl
