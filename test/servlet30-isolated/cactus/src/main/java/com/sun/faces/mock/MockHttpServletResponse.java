/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockHttpServletResponse.java,v 1.1 2005/10/18 17:47:57 edburns Exp $
 */



package com.sun.faces.mock;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;



// Mock Object for HttpServletResponse (Version 2.3)
public class MockHttpServletResponse implements HttpServletResponse {



    private String encoding = "ISO-8859-1";
    private String contentType = "text/html";
    private int status;


    // -------------------------------------------- HttpServletResponse Methods


    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }


    public void addDateHeader(String name, long value) {
        throw new UnsupportedOperationException();
    }


    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }


    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }


    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException();
    }


    public String encodeRedirectUrl(String url) {
        return (encodeRedirectURL(url));
    }


    public String encodeRedirectURL(String url) {
        return (url);
    }


    public String encodeUrl(String url) {
        return (encodeURL(url));
    }


    public String encodeURL(String url) {
        return (url);
    }


    public void sendError(int status) {
        this.status = status;
    }


    public void sendError(int status, String message) {
        this.status = status;
    }


    public void sendRedirect(String location) {
        throw new UnsupportedOperationException();
    }


    public void setDateHeader(String name, long value) {
        throw new UnsupportedOperationException();
    }


    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }


    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }


    public void setStatus(int status) {
        this.status = status;
    }


    public void setStatus(int status, String message) {
        this.status = status;
    }


    // ------------------------------------------------ ServletResponse Methods


    public void flushBuffer() {
        throw new UnsupportedOperationException();
    }


    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }


    public String getCharacterEncoding() {
        return (this.encoding);
    }

    public String getContentType() {
	return (this.contentType);
    }

    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }


    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public int getStatus() {
        return status;
    }


    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException();
    }


    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }


    public void reset() {
        throw new UnsupportedOperationException();
    }


    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }


    public void setBufferSize(int size) {
        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String charset) {
        this.encoding = charset;
    }

    public void setContentLength(int length) {
        throw new UnsupportedOperationException();
    }


    public void setContentType(String type) {
        contentType = type;
    }


    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    public String getHeader(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Collection<String> getHeaders(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
