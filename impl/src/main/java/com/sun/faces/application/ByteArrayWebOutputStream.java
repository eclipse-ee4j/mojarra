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

package com.sun.faces.application;

import static java.util.logging.Level.FINE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

/**
 * This steam converts byte content to character. This implementation allows us to query whether or not the writer has
 * been flushed or closed. This is necessary to better mimic the actual Servlet response.
 */
class ByteArrayWebOutputStream extends ServletOutputStream {

    public static final ServletOutputStream NOOP_STREAM = new NoOpOutputStream();

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private final DirectByteArrayOutputStream baos;
    private boolean committed;

    public ByteArrayWebOutputStream() {
        baos = new DirectByteArrayOutputStream(1024);
    }

    @Override
    public void write(int n) {
        baos.write(n);
    }

    /**
     * <p>
     * It's important to not expose this as reset.
     * </p>
     */

    public void resetByteArray() {
        baos.reset();
    }

    public byte[] toByteArray() {
        return baos.toByteArray();
    }

    /**
     * Converts the buffered bytes into chars based on the specified encoding and writes them to the provided Writer.
     *
     * @param writer target Writer
     * @param encoding character encoding
     */
    public void writeTo(Writer writer, String encoding) {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine("Converting buffered ServletOutputStream bytes" + " to chars using " + encoding);
        }

        ByteBuffer byteBuffer = baos.getByteBuffer();
        CharsetDecoder decoder = Charset.forName(encoding).newDecoder();

        try {
            CharBuffer charBuffer = decoder.decode(byteBuffer);
            writer.write(charBuffer.array());
        } catch (IOException ioe) {
            throw new FacesException(ioe);
        }
    }

    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void close() throws IOException {
        committed = true;
    }

    @Override
    public void flush() throws IOException {
        committed = true;
    }

    /**
     * <p>
     * Write the buffered bytes to the provided OutputStream.
     * </p>
     *
     * @param stream the stream to write to
     */
    public void writeTo(OutputStream stream) {
        try {
            stream.write(baos.getByteBuffer().array());
        } catch (IOException ioe) {
            throw new FacesException(ioe);
        }
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setWriteListener(WriteListener wl) {
        throw new UnsupportedOperationException("Not supported");
    }

    private static class DirectByteArrayOutputStream extends ByteArrayOutputStream {

        // -------------------------------------------------------- Constructors

        public DirectByteArrayOutputStream(int initialCapacity) {
            super(initialCapacity);
        }

        // ------------------------------------------------------- PublicMethods

        /**
         * Return the buffer backing this ByteArrayOutputStream as a ByteBuffer.
         *
         * @return buf wrapped in a ByteBuffer
         */
        public ByteBuffer getByteBuffer() {
            return ByteBuffer.wrap(buf, 0, count);
        }

    }

    private static class NoOpOutputStream extends ServletOutputStream {

        @Override
        public void write(int b) throws IOException {
            // no-op
        }

        @Override
        public void write(byte b[]) throws IOException {
            // no-op
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            // no-op
        }

        @Override
        public void flush() throws IOException {
            // no-op
        }

        @Override
        public void close() throws IOException {
            // no-op
        }

        protected NoOpOutputStream() {
            // no-op
        }

        @Override
        public void print(String s) throws IOException {
            // no-op
        }

        @Override
        public void print(boolean b) throws IOException {
            // no-op
        }

        @Override
        public void print(char c) throws IOException {
            // no-op
        }

        @Override
        public void print(int i) throws IOException {
            // no-op
        }

        @Override
        public void print(long l) throws IOException {
            // no-op
        }

        @Override
        public void print(float v) throws IOException {
            // no-op
        }

        @Override
        public void print(double v) throws IOException {
            // no-op
        }

        @Override
        public void println() throws IOException {
            // no-op
        }

        @Override
        public void println(String s) throws IOException {
            // no-op
        }

        @Override
        public void println(boolean b) throws IOException {
            // no-op
        }

        @Override
        public void println(char c) throws IOException {
            // no-op
        }

        @Override
        public void println(int i) throws IOException {
            // no-op
        }

        @Override
        public void println(long l) throws IOException {
            // no-op
        }

        @Override
        public void println(float v) throws IOException {
            // no-op
        }

        @Override
        public void println(double v) throws IOException {
            // no-op
        }

        @Override
        public boolean isReady() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setWriteListener(WriteListener wl) {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
