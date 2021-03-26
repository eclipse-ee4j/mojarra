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
import java.io.Writer;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseWriter;

public class MockResponseWriter extends ResponseWriter {

    public MockResponseWriter(Writer writer, String encoding) {
        this.writer = writer;
        this.encoding = encoding;
    }

    private Writer writer;
    private String encoding;

    // ---------------------------------------------------------- Writer Methods
    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    public void write(char c) throws IOException {
        writer.write(c);
    }

    @Override
    public void write(char cbuf[], int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        writer.write(c);
    }

    @Override
    public void write(String s) throws IOException {
        writer.write(s);
    }

    @Override
    public void write(String s, int off, int len) throws IOException {
        writer.write(s, off, len);
    }

    // --------------------------------------------------- ResponseWrter Methods
    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getCharacterEncoding() {
        return (this.encoding);
    }

    @Override
    public void startDocument() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endDocument() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startElement(String name,
            UIComponent component) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endElement(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeAttribute(String name, Object value,
            String componentPropertyName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeURIAttribute(String name, Object value, String componentPropertyName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeComment(Object comment) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeText(Object text, String componentProperty) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeText(char text[], int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseWriter cloneWithWriter(Writer writer) {
        throw new UnsupportedOperationException();
    }
}
