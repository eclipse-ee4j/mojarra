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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A simple PrintWriter implementation to allow us to query whether or not the writer has been flushed or closed. This
 * is necessary to better mimic the actual Servlet response.
 */
public class WebPrintWriter extends PrintWriter {

    public static final Writer NOOP_WRITER = new NoOpWriter();
    private boolean committed;

    public WebPrintWriter(Writer delegate) {
        super(delegate);
    }

    @Override
    public void close() {
        committed = true;
    }

    @Override
    public void flush() {
        committed = true;
    }

    public boolean isComitted() {
        return committed;
    }

    // ----------------------------------------------------------- Inner Classes

    private static class NoOpWriter extends Writer {

        public NoOpWriter() {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
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

        @Override
        public void write(int c) throws IOException {
            // no-op
        }

        @Override
        public void write(char[] cbuf) throws IOException {
            // no-op
        }

        @Override
        public void write(String str) throws IOException {
            // no-op
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            // no-op
        }

        @Override
        public Writer append(CharSequence csq) throws IOException {
            return this;
        }

        @Override
        public Writer append(CharSequence csq, int start, int end) throws IOException {
            return this;
        }

        @Override
        public Writer append(char c) throws IOException {
            return this;
        }
    }
}
