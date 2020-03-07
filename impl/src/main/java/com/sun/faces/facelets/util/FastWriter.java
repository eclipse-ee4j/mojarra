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

package com.sun.faces.facelets.util;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Jacob Hookom
 */
public final class FastWriter extends Writer {

    private char[] buff;
    private int size;

    public FastWriter() {
        this(1024);
    }

    public FastWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Initial Size cannot be less than 0");
        }
        buff = new char[initialSize];
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    private void overflow(int len) {
        if (size + len > buff.length) {
            char[] next = new char[(size + len) * 2];
            System.arraycopy(buff, 0, next, 0, size);
            buff = next;
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        overflow(len);
        System.arraycopy(cbuf, off, buff, size, len);
        size += len;
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        this.write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(int c) throws IOException {
        overflow(1);
        buff[size] = (char) c;
        size++;
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        this.write(str.toCharArray(), off, len);
    }

    @Override
    public void write(String str) throws IOException {
        this.write(str.toCharArray(), 0, str.length());
    }

    public void reset() {
        size = 0;
    }

    @Override
    public String toString() {
        return new String(buff, 0, size);
    }
}
