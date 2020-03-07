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

package com.sun.faces.facelets;

import java.io.IOException;
import java.io.Writer;

import com.sun.faces.facelets.util.FastWriter;

/**
 * A class for handling state insertion. Content is written directly to "out" until an attempt to write state; at that
 * point, it's redirected into a buffer that can be picked through in theory, this buffer should be very small, since it
 * only needs to be enough to contain all the content after the close of the first (and, hopefully, only) form.
 * <p>
 * Potential optimizations:
 * <ul>
 * <li>If we created a new FastWriter at each call to writingState(), and stored a List of them, then we'd know that
 * state tokens could only possibly be near the start of each buffer (and might not be there at all). (There might be a
 * close-element before the state token). Then, we'd only need to check the start of the buffer for the state token; if
 * it's there, write out the real state, then blast the rest of the buffer out. This wouldn't even require toString(),
 * which for large buffers is expensive. However, this optimization is only going to be especially meaningful for the
 * multi-form case.</li>
 * <li>More of a FastWriter optimization than a StateWriter, but: it is far faster to create a set of small 1K buffers
 * than constantly reallocating one big buffer.</li>
 * </ul>
 *
 * @author Adam Winer
 */
final class StateWriter extends Writer {

    private int initialSize;
    private Writer out;
    private FastWriter fast;
    private boolean writtenState;

    static public StateWriter getCurrentInstance() {
        return (StateWriter) CURRENT_WRITER.get();
    }

    public StateWriter(Writer initialOut, int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Initial Size cannot be less than 0");
        }

        this.initialSize = initialSize;
        out = initialOut;

        CURRENT_WRITER.set(this);
    }

    /**
     * Mark that state is about to be written. Contrary to what you'd expect, we cannot and should not assume that this
     * location is really going to have state; it is perfectly legit to have a ResponseWriter that filters out content, and
     * ignores an attempt to write out state at this point. So, we have to check after the fact to see if there really are
     * state markers.
     */
    public void writingState() {
        if (!writtenState) {
            writtenState = true;
            out = fast = new FastWriter(initialSize);
        }
    }

    public boolean isStateWritten() {
        return writtenState;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        out.write(cbuf);
    }

    @Override
    public void write(int c) throws IOException {
        out.write(c);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    @Override
    public void write(String str) throws IOException {
        out.write(str);
    }

    public String getAndResetBuffer() {
        if (!writtenState) {
            throw new IllegalStateException("Did not write state;  no buffer is available");
        }

        String result = fast.toString();
        fast.reset();
        return result;
    }

    public void release() {
        CURRENT_WRITER.set(null);
    }

    static private final ThreadLocal CURRENT_WRITER = new ThreadLocal();
}
