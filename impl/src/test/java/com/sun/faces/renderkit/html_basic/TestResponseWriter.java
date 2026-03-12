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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.io.Writer;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseWriter;

/**
 * A test ResponseWriter used for testing renderers.
 */
public class TestResponseWriter extends ResponseWriter {

    private Writer writer;

    /**
     * Constructor.
     *
     * @param writer the writer.
     */
    public TestResponseWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public String getContentType() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "text/html";
    }

    @Override
    public String getCharacterEncoding() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "UTF-8";
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startDocument() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endDocument() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Start the element.
     *
     * @param name the name.
     * @param component the component.
     * @throws IOException when an I/O error occurs.
     */
    @Override
    public void startElement(String name, UIComponent component) throws IOException {
        writer.write("<" + name);
    }

    /**
     * End the element.
     *
     * @param name the name.
     * @throws IOException when an I/O error occurs.
     */
    @Override
    public void endElement(String name) throws IOException {
        writer.write("</" + name + ">");
    }

    /**
     * Write an attribute.
     *
     * @param name the name.
     * @param value the value.
     * @param property the property.
     * @throws IOException when an I/O error occurs.
     */
    @Override
    public void writeAttribute(String name, Object value, String property) throws IOException {
        writer.write(" " + name + "=\"" + value.toString() + "\"");
    }

    @Override
    public void writeURIAttribute(String name, Object value, String property) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeComment(Object comment) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeText(Object text, String property) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeText(char[] text, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResponseWriter cloneWithWriter(Writer writer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
