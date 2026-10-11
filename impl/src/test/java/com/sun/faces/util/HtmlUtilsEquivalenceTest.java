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

package com.sun.faces.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Differential test: the scanning/SIMD escaping of {@link HtmlUtils} must produce exactly the output of the previous
 * implementation ({@link LegacyHtmlUtils}) for every flag combination, on random inputs built from the characters every
 * branch cares about. Run it with {@code -DargLine="--add-modules jdk.incubator.vector"} to cover the Vector API scanner
 * as well; without it, the String.indexOf/SWAR and scalar scans are covered.
 */
class HtmlUtilsEquivalenceTest {

    private static final String[] PIECES = { "a", "Z", "0", " ", "s", "script:", "javascript:alert(1)", "<", ">", "&", "&{", "\"", "'", "{",
            "\t", "\n", "\r", "\f", "\u0001", "\u001F", "\u007F", "\u0085", "\u009F", " ", "é", "ÿ", "Ā", "€",
            "あ", "😀", "\uD83D", "\uDE00", "�", "￾", "￿", "]]>", "Lorem ipsum dolor sit amet ",
            "consectetur adipiscing elit, sed do eiusmod tempor " };

    private static final int ITERATIONS = 20_000;

    @Test
    void textMatchesLegacy() throws IOException {
        Random random = new Random(42);
        for (int n = 0; n < ITERATIONS; n++) {
            String text = randomText(random);
            for (int flags = 0; flags < 8; flags++) {
                boolean escapeUnicode = (flags & 1) != 0;
                boolean escapeIso = (flags & 2) != 0;
                boolean forXml = (flags & 4) != 0;
                String context = describe(text, flags);

                assertEquals(legacyText(text, escapeUnicode, escapeIso, forXml, 128), newText(text, escapeUnicode, escapeIso, forXml, 128), context);
                assertEquals(legacyText(text, escapeUnicode, escapeIso, forXml, 8), newText(text, escapeUnicode, escapeIso, forXml, 8), context);

                char[] padded = ("xy" + text + "zw").toCharArray();
                StringWriter expected = new StringWriter();
                LegacyHtmlUtils.writeText(expected, escapeUnicode, escapeIso, padded, 2, text.length(), forXml);
                StringWriter actual = new StringWriter();
                HtmlUtils.writeText(actual, escapeUnicode, escapeIso, padded, 2, text.length(), forXml);
                assertEquals(expected.toString(), actual.toString(), "char[] " + context);
            }
        }
    }

    @Test
    void attributeMatchesLegacy() throws IOException {
        Random random = new Random(4242);
        for (int n = 0; n < ITERATIONS; n++) {
            String text = randomText(random);
            for (int flags = 0; flags < 16; flags++) {
                boolean escapeUnicode = (flags & 1) != 0;
                boolean escapeIso = (flags & 2) != 0;
                boolean forXml = (flags & 4) != 0;
                boolean scriptEnabled = (flags & 8) != 0;
                String context = describe(text, flags);

                for (int bufferSize : new int[] { 128, 8 }) {
                    StringWriter expected = new StringWriter();
                    LegacyHtmlUtils.writeAttribute(expected, escapeUnicode, escapeIso, text, new char[bufferSize], scriptEnabled, forXml);
                    StringWriter actual = new StringWriter();
                    HtmlUtils.writeAttribute(actual, escapeUnicode, escapeIso, text, new char[bufferSize], scriptEnabled, forXml);
                    assertEquals(expected.toString(), actual.toString(), context);
                }

                char[] padded = ("xy" + text + "zw").toCharArray();
                StringWriter expected = new StringWriter();
                LegacyHtmlUtils.writeAttribute(expected, escapeUnicode, escapeIso, padded, 2, text.length(), scriptEnabled, forXml);
                StringWriter actual = new StringWriter();
                HtmlUtils.writeAttribute(actual, escapeUnicode, escapeIso, padded, 2, text.length(), scriptEnabled, forXml);
                assertEquals(expected.toString(), actual.toString(), "char[] " + context);
            }
        }
    }

    @Test
    void longCleanAndDirtyInputsMatchLegacy() throws IOException {
        // Lengths around every scan threshold and vector width, with the interesting character at every position.
        for (int length = 1; length <= 140; length++) {
            for (String probe : new String[] { "<", "&", "\"", "\n", "\u0001", "é", "あ", "\uD83D", "￾" }) {
                for (int pos = 0; pos < length; pos += Math.max(1, length / 13)) {
                    String text = "a".repeat(pos) + probe + "b".repeat(Math.max(0, length - pos - 1));
                    for (int flags = 0; flags < 8; flags++) {
                        boolean escapeUnicode = (flags & 1) != 0;
                        boolean escapeIso = (flags & 2) != 0;
                        boolean forXml = (flags & 4) != 0;
                        assertEquals(legacyText(text, escapeUnicode, escapeIso, forXml, 256), newText(text, escapeUnicode, escapeIso, forXml, 256),
                                describe(text, flags));
                        StringWriter expected = new StringWriter();
                        LegacyHtmlUtils.writeAttribute(expected, escapeUnicode, escapeIso, text, new char[256], false, forXml);
                        StringWriter actual = new StringWriter();
                        HtmlUtils.writeAttribute(actual, escapeUnicode, escapeIso, text, new char[256], false, forXml);
                        assertEquals(expected.toString(), actual.toString(), describe(text, flags));
                    }
                }
            }
        }
    }

    private static String legacyText(String text, boolean escapeUnicode, boolean escapeIso, boolean forXml, int bufferSize) throws IOException {
        StringWriter out = new StringWriter();
        LegacyHtmlUtils.writeText(out, escapeUnicode, escapeIso, text, new char[bufferSize], forXml);
        return out.toString();
    }

    private static String newText(String text, boolean escapeUnicode, boolean escapeIso, boolean forXml, int bufferSize) throws IOException {
        StringWriter out = new StringWriter();
        HtmlUtils.writeText(out, escapeUnicode, escapeIso, text, new char[bufferSize], forXml);
        return out.toString();
    }

    private static String randomText(Random random) {
        StringBuilder builder = new StringBuilder();
        int pieces = random.nextInt(4) == 0 ? random.nextInt(60) : random.nextInt(12);
        // Mostly clean runs, like real markup, with sprinkled specials.
        for (int i = 0; i < pieces; i++) {
            builder.append(random.nextInt(3) == 0 ? PIECES[random.nextInt(PIECES.length)] : PIECES[random.nextInt(3)].repeat(1 + random.nextInt(20)));
        }
        return builder.toString();
    }

    private static String describe(String text, int flags) {
        StringBuilder codes = new StringBuilder();
        for (char c : text.toCharArray()) {
            codes.append(c >= 0x20 && c < 0x7F ? String.valueOf(c) : String.format("\\u%04X", (int) c));
        }
        return "flags=" + flags + " text=" + codes;
    }
}
