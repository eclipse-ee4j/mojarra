/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.Test;

/**
 * Every method here overrides a {@link Writer} method whose behavior the base class already specifies, so each test pins one clause of that specification
 * against the {@link StringBuilder} call it delegates to.
 */
public class FastStringWriterTest {

    /**
     * {@link Writer#write(int)} writes the character in the 16 low-order bits and ignores the 16 high-order ones.
     */
    @Test
    public void writeIntWritesLowSixteenBitsOnly() throws IOException {
        FastStringWriter writer = new FastStringWriter();
        writer.write(0xFFFF0041);

        assertEquals("A", writer.toString());
    }

    /**
     * {@link Writer#write(char[])} writes the whole array.
     */
    @Test
    public void writeCharArrayWritesWholeArray() throws IOException {
        FastStringWriter writer = new FastStringWriter();
        writer.write(new char[] { 'a', 'b', 'c' });

        assertEquals("abc", writer.toString());
    }

    /**
     * {@link Writer#write(char[], int, int)} takes an offset and a length, where {@link StringBuilder} takes an offset and a length as well, so the arguments
     * pass through unchanged.
     */
    @Test
    public void writeCharArrayPortionTakesOffsetAndLength() throws IOException {
        FastStringWriter writer = new FastStringWriter();
        writer.write(new char[] { 'a', 'b', 'c', 'd', 'e', 'f' }, 1, 3);

        assertEquals("bcd", writer.toString());
    }

    /**
     * A range outside the array is rejected, which is what allows the bounds check to be left to {@link StringBuilder}.
     */
    @Test
    public void writeCharArrayPortionRejectsRangeOutsideTheArray() {
        FastStringWriter writer = new FastStringWriter();
        char[] cbuf = { 'a', 'b', 'c' };

        assertThrows(IndexOutOfBoundsException.class, () -> writer.write(cbuf, 1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> writer.write(cbuf, -1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> writer.write(cbuf, 0, -1));
    }

    /**
     * {@link Writer#write(String, int, int)} takes an offset and a length, where {@link StringBuilder} takes a start and an end, so the length has to be
     * converted.
     */
    @Test
    public void writeStringPortionConvertsLengthToEndIndex() {
        FastStringWriter writer = new FastStringWriter();
        writer.write("abcdef", 1, 3);

        assertEquals("bcd", writer.toString());
    }

    /**
     * A range outside the string is rejected.
     */
    @Test
    public void writeStringPortionRejectsRangeOutsideTheString() {
        FastStringWriter writer = new FastStringWriter();

        assertThrows(IndexOutOfBoundsException.class, () -> writer.write("abc", 1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> writer.write("abc", -1, 2));
    }

    /**
     * {@link Writer#append(CharSequence)} and {@link Writer#append(CharSequence, int, int)} treat a <code>null</code> sequence as if it contained the four
     * characters <code>null</code>.
     */
    @Test
    public void appendTreatsNullAsTheFourCharactersNull() throws IOException {
        FastStringWriter writer = new FastStringWriter();
        writer.append(null);

        assertEquals("null", writer.toString());

        writer = new FastStringWriter();
        writer.append(null, 1, 3);

        assertEquals("ul", writer.toString());
    }

    /**
     * {@link Writer#append(CharSequence, int, int)} takes a start and an end, both passed through unchanged.
     */
    @Test
    public void appendSubSequenceTakesStartAndEnd() throws IOException {
        FastStringWriter writer = new FastStringWriter();
        writer.append(new StringBuilder("hello"), 1, 4);

        assertEquals("ell", writer.toString());
    }

    /**
     * Every <code>append</code> returns the writer itself, so calls chain.
     */
    @Test
    public void appendReturnsThisSoCallsChain() throws IOException {
        FastStringWriter writer = new FastStringWriter();

        assertSame(writer, writer.append('a'));
        assertSame(writer, writer.append("bc"));
        assertSame(writer, writer.append("xdey", 1, 3));
        assertEquals("abcde", writer.toString());
    }

    /**
     * {@link FastStringWriter#reset()} empties the buffer without replacing it, so a reference handed out through {@link FastStringWriter#getBuffer()} stays
     * valid.
     */
    @Test
    public void resetEmptiesTheBufferInPlace() {
        FastStringWriter writer = new FastStringWriter();
        StringBuilder buffer = writer.getBuffer();
        writer.write("abc");
        writer.reset();
        writer.write("de");

        assertEquals("de", writer.toString());
        assertSame(buffer, writer.getBuffer());
    }

}
