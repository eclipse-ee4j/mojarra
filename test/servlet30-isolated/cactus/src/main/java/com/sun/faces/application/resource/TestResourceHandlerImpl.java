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

package com.sun.faces.application.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Override;
import java.lang.String;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import javax.faces.application.*;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ResourceWrapper;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.Util;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.InitFacesContext;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests com.sun.faces.application.resource.ResourceHandlerImpl
 */
public class TestResourceHandlerImpl extends ServletFacesTestCase {

    /* HTTP Date format required by the HTTP/1.1 RFC */
    private static final String RFC1123_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public TestResourceHandlerImpl() {
        super("TestResourceHandlerImpl");
        initLocalHostPath();
    }

    public TestResourceHandlerImpl(String name) {
        super(name);
        initLocalHostPath();
    }

    private String path = "localhost:8080";

    private void initLocalHostPath() {
        String containerPort = System.getProperty("container.port");
        if (null == containerPort || 0 == containerPort.length()) {
            containerPort = "8080";
        }
        path = "localhost:" + containerPort;
    }
    
    public void testAll() {
        
    }

    @Override
    public void setUp() {
        try {
            Method reInitializeFactoryManager = FactoryFinder.class.getDeclaredMethod("reInitializeFactoryManager", (Class<?>[]) null);
            reInitializeFactoryManager.setAccessible(true);
            reInitializeFactoryManager.invoke(null, (Object[]) null);

            FacesContext oldContext = FacesContext.getCurrentInstance();
            if (null != oldContext && (oldContext instanceof InitFacesContext)) {
                // JAVASERVERFACES-2140
                assert (Util.isUnitTestModeEnabled());
                System.out.println("Re-initializing ExternalContext with ServletContext from cactus: " + getConfig().getServletContext());
                System.out.flush();
                ((InitFacesContext) oldContext).reInitializeExternalContext(getConfig().getServletContext());
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }
        super.setUp();
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods

    // ---------------------------------------------------------- Helper Methods

    private byte[] getBytes(URL url) throws Exception {

        return getBytes(url, false);

    }

    private byte[] getBytes(URL url, boolean compress) throws Exception {
        URLConnection c = url.openConnection();
        c.setUseCaches(false);
        InputStream in = c.getInputStream();
        return ((compress) ? getCompressedBytes(in) : getBytes(in));
    }

    private byte[] getBytes(InputStream in) throws Exception {

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (int i = in.read(); i != -1; i = in.read()) {
            o.write(i);
        }
        in.close();
        return o.toByteArray();

    }

    private byte[] getCompressedBytes(InputStream in) throws Exception {

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        GZIPOutputStream compress = new GZIPOutputStream(o);
        for (int i = in.read(); i != -1; i = in.read()) {
            compress.write(i);
        }
        compress.flush();
        compress.close();
        return o.toByteArray();

    }

    // ----------------------------------------------------------- Inner Classes

    private static class TestResponseWrapper extends HttpServletResponseWrapper {

        private TestServletOutputStream out;

        public byte[] getBytes() {
            return out.getBytes();
        }

        public TestResponseWrapper(HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
        }

        public ServletOutputStream getOutputStream() throws IOException {
            out = new TestServletOutputStream(super.getOutputStream());
            return out;
        }

        private class TestServletOutputStream extends ServletOutputStream {
            private ServletOutputStream wrapped;
            private ByteArrayOutputStream out = new ByteArrayOutputStream();

            public TestServletOutputStream(ServletOutputStream wrapped) {
                this.wrapped = wrapped;
            }

            public void write(int b) throws IOException {
                wrapped.write(b);
                out.write(b);
            }

            public void write(byte b[]) throws IOException {
                wrapped.write(b);
                out.write(b);
            }

            public void write(byte b[], int off, int len) throws IOException {
                wrapped.write(b, off, len);
                out.write(b, off, len);
            }

            public void flush() throws IOException {
                wrapped.flush();
                out.flush();
            }

            public void close() throws IOException {
                wrapped.close();
                out.close();
            }

            public byte[] getBytes() {
                return out.toByteArray();
            }

            // @Override
            // public boolean isReady() {
            // throw new UnsupportedOperationException("Not supported");
            // }
            //
            // @Override
            // public void setWriteListener(WriteListener wl) {
            // throw new UnsupportedOperationException("Not supported");
            // }
        }
    }

}
