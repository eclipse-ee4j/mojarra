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

package org.glassfish.mojarra.io;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>
 * A {@link Writer} implementation backed by a {@link StringBuilder}.
 * </p>
 *
 * <p>
 * This class is not thread safe.
 * </p>
 */
public class FastStringWriter extends Writer {

    protected final StringBuilder builder;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Constructs a new <code>FastStringWriter</code> instance using the default capacity of <code>16</code>.
     * </p>
     */
    public FastStringWriter() {
        builder = new StringBuilder();
    }

    /**
     * <p>
     * Constructs a new <code>FastStringWriter</code> instance using the specified <code>initialCapacity</code>.
     * </p>
     *
     * @param initialCapacity specifies the initial capacity of the buffer
     *
     * @throws IllegalArgumentException if initialCapacity is less than zero
     */
    public FastStringWriter(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        builder = new StringBuilder(initialCapacity);
    }

    // ----------------------------------------------------- Methods from Writer

    /**
     * <p>
     * Write a portion of an array of characters.
     * </p>
     * @param cbuf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     * @throws IndexOutOfBoundsException if <code>off</code> and <code>len</code> fall outside <code>cbuf</code>
     * @throws IOException
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        builder.append(cbuf, off, len);
    }

    /**
     * <p>
     * This is a no-op.
     * </p>
     *
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
    }

    /**
     * <p>
     * This is a no-op.
     * </p>
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Write a single character. The 16 high-order bits of <code>c</code> are ignored.
     * @param c the character to be written
     */
    @Override
    public void write(int c) throws IOException {
        builder.append((char) c);
    }

    /**
     * Write a string.
     *
     * @param str String to be written
     */
    @Override
    public void write(String str) {
        builder.append(str);
    }

    /**
     * Write an array of characters.
     *
     * @param cbuf Array of characters to be written
     */
    @Override
    public void write(char[] cbuf) throws IOException {
        builder.append(cbuf);
    }

    /**
     * Write a portion of a string.
     *
     * @param str A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     * @throws IndexOutOfBoundsException if <code>off</code> and <code>len</code> fall outside <code>str</code>
     */
    @Override
    public void write(String str, int off, int len) {
        builder.append(str, off, off + len);
    }

    /**
     * Return the <code>StringBuilder</code> itself.
     *
     * @return StringBuilder holding the current buffer value.
     */
    public StringBuilder getBuffer() {
        return builder;
    }

    /** @return the buffer's current value as a string. */
    @Override
    public String toString() {
        return builder.toString();
    }

    public void reset() {
        builder.setLength(0);
    }


    // ------------------------------------------------- Append Methods

    /**
     * Append a character sequence. A <code>null</code> argument appends the four characters <code>null</code>.
     *
     * @param csq the character sequence to append
     * @return this writer
     */
    @Override
    public Writer append(CharSequence csq) throws IOException {
        builder.append(csq);
        return this;
    }

    /**
     * Append a subsequence of a character sequence. A <code>null</code> argument is appended as if it contained the
     * four characters <code>null</code>.
     *
     * @param csq the character sequence to append from
     * @param start index of the first character to append
     * @param end index after the last character to append
     * @return this writer
     * @throws IndexOutOfBoundsException if <code>start</code> and <code>end</code> fall outside <code>csq</code>
     */
    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        builder.append(csq, start, end);
        return this;
    }

    /**
     * Append a single character.
     *
     * @param c the character to append
     * @return this writer
     */
    @Override
    public Writer append(char c) throws IOException {
        builder.append(c);
        return this;
    }

}
