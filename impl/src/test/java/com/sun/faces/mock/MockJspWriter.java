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

package com.sun.faces.mock;

import java.io.IOException;
import java.io.StringWriter;
import jakarta.servlet.jsp.JspWriter;

// Mock Object for JspWriter
public class MockJspWriter extends JspWriter {

    // Public constructor for convenient setup
    public MockJspWriter(int bufferSize, boolean autoFlush) {
        super(bufferSize, autoFlush);
        writer = new StringWriter(bufferSize);
    }

    StringWriter writer;
    String lineSeparator = System.getProperty("line.separator");
    boolean closed = false;
    boolean flushed = false;

    // Mock Accessor for retrieving the contents that have been written
    public String getBuffer() {
        return (writer.getBuffer().toString());
    }

    // ------------------------------------------------------ JspWriter Methods
    public void clear() throws IOException {
        if (flushed) {
            throw new IOException("Already flushed");
        }
        writer = new StringWriter(getBufferSize());
    }

    @Override
    public void clearBuffer() throws IOException {
        writer = new StringWriter(getBufferSize());
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            flush();
            closed = true;
        }
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Already closed");
        }
        flushed = true;
    }

    @Override
    public int getRemaining() {
        return (getBufferSize() - writer.getBuffer().length());
    }

    @Override
    public void newLine() throws IOException {
        write(lineSeparator);
    }

    @Override
    public void print(boolean b) throws IOException {
        write(b ? "true" : "false");
    }

    @Override
    public void print(char c) throws IOException {
        write(String.valueOf(c));
    }

    @Override
    public void print(char c[]) throws IOException {
        write(c);
    }

    @Override
    public void print(double d) throws IOException {
        write(String.valueOf(d));
    }

    @Override
    public void print(float f) throws IOException {
        write(String.valueOf(f));
    }

    @Override
    public void print(int i) throws IOException {
        write(String.valueOf(i));
    }

    @Override
    public void print(long l) throws IOException {
        write(String.valueOf(l));
    }

    @Override
    public void print(Object o) throws IOException {
        write(String.valueOf(o));
    }

    @Override
    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    @Override
    public void println() throws IOException {
        newLine();
    }

    @Override
    public void println(boolean b) throws IOException {
        print(b);
        newLine();
    }

    @Override
    public void println(char c) throws IOException {
        print(c);
        newLine();
    }

    @Override
    public void println(char c[]) throws IOException {
        print(c);
        newLine();
    }

    @Override
    public void println(double d) throws IOException {
        print(d);
        newLine();
    }

    @Override
    public void println(float f) throws IOException {
        print(f);
        newLine();
    }

    @Override
    public void println(int i) throws IOException {
        print(i);
        newLine();
    }

    @Override
    public void println(long l) throws IOException {
        print(l);
        newLine();
    }

    @Override
    public void println(Object o) throws IOException {
        print(o);
        newLine();
    }

    @Override
    public void println(String s) throws IOException {
        print(s);
        newLine();
    }

    // --------------------------------------------------------- Writer Methods
    @Override
    public void write(char c[]) throws IOException {
        if (closed) {
            throw new IOException("Already closed");
        }
        writer.write(c);
    }

    @Override
    public void write(char c[], int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Already closed");
        }
        writer.write(c, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        if (closed) {
            throw new IOException("Already closed");
        }
        writer.write(c);
    }

    @Override
    public void write(String s) throws IOException {
        if (closed) {
            throw new IOException("Already closed");
        }
        writer.write(s);
    }

    @Override
    public void write(String s, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Already closed");
        }
        writer.write(s, off, len);
    }
}
