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

// ViewHandlerResponseWrapper.java

package com.sun.faces.application;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * <p>
 * This class is used by {@link jakarta.faces.application.ViewHandler#createView} to obtain the text that exists after
 * the &lt;f:view&gt; tag.
 * </p>
 */
public class ViewHandlerResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayWebOutputStream basos;
    private WebPrintWriter pw;
    private CharArrayWriter caw;
    private int status = HttpServletResponse.SC_OK;

    public ViewHandlerResponseWrapper(HttpServletResponse wrapped) {
        super(wrapped);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        super.sendError(sc, msg);
        status = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        super.sendError(sc);
        status = sc;
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        status = sc;
    }

    @Override
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        status = sc;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public boolean isBytes() {
        return null != basos;
    }

    public boolean isChars() {
        return null != caw;
    }

    public byte[] getBytes() {
        byte[] result = null;
        if (null != basos) {
            result = basos.toByteArray();
        }
        return result;
    }

    public char[] getChars() {
        char[] result = null;
        if (null != caw) {
            result = caw.toCharArray();
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "null";
        if (null != caw) {
            result = caw.toString();
        } else if (null != basos) {
            result = basos.toString();
        }
        return result;
    }

    public void flushContentToWrappedResponse() throws IOException {
        ServletResponse wrapped = getResponse();
        if (null != caw) {
            pw.flush();
            caw.writeTo(wrapped.getWriter());
            caw.reset();
        } else if (null != basos) {
            try {
                basos.writeTo(wrapped.getWriter(), wrapped.getCharacterEncoding());
            } catch (IllegalStateException ise) {
                basos.writeTo(wrapped.getOutputStream());
            }
            basos.resetByteArray();
        }

    }

    public void flushToWriter(Writer writer, String encoding) throws IOException {
        if (null != caw) {
            pw.flush();
            caw.writeTo(writer);
            caw.reset();
        } else if (null != basos) {
            basos.writeTo(writer, encoding);
            basos.resetByteArray();
        }
        writer.flush();
    }

    public void resetBuffers() throws IOException {
        if (null != caw) {
            caw.reset();
        } else if (null != basos) {
            basos.resetByteArray();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (pw != null && !pw.isComitted() && !isCommitted()) {
            throw new IllegalStateException();
        } else if (pw != null && (pw.isComitted() || isCommitted())) {
            return ByteArrayWebOutputStream.NOOP_STREAM;
        }
        if (null == basos) {
            basos = new ByteArrayWebOutputStream();
        }
        return basos;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (basos != null && !basos.isCommitted() && !isCommitted()) {
            throw new IllegalStateException();
        } else if (basos != null && (basos.isCommitted() || isCommitted())) {
            return new WebPrintWriter(WebPrintWriter.NOOP_WRITER);
        }
        if (null == pw) {
            caw = new CharArrayWriter(1024);
            pw = new WebPrintWriter(caw);
        }

        return pw;
    }

} // end of class ViewHandlerResponseWrapper
