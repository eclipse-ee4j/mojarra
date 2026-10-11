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
 * Differential test: the escaping of {@link HtmlUtils} must produce exactly the output of the reference implementation
 * ({@link LegacyHtmlUtils}) for every flag combination, on inputs built from the characters every branch cares about.
 * Inputs from {@link HtmlUtils#INTRINSIC_SCAN_MIN_LENGTH} chars cover the String.indexOf and SWAR scan, shorter ones the scan per
 * char.
 */
class HtmlUtilsEquivalenceTest {

    private static final String[] PIECES = { "a", "Z", "0", " ", "s", "script:", "javascript:alert(1)", "<", ">", "&", "&{", "\"", "'", "{",
            "\t", "\n", "\r", "\f", "\u0001", "\u001F", "\u007F", "\u0085", "\u009F", " ", "é", "ÿ", "Ā", "€",
            "あ", "😀", "\uD83D", "\uDE00", "�", "￾", "￿", "]]>", "Lorem ipsum dolor sit amet ",
            "consectetur adipiscing elit, sed do eiusmod tempor " };

    /** Characters every branch cares about. U+0100 and U+0109 have a control char as low byte, which the SWAR scan flags. */
    private static final String[] POSITION_PROBES = { "<", ">", "&", "&{", "\"", "\t", "\n", "\f", "\u0001", "\u001F", "\u007F", "\u0085",
            "\u0109", "\u0100", "é", "あ", "\uD83D", "\uDE00", "😀", "\uFFFE", "script:", "javascript:x" };

    private static final int SWAR_WORD = 8;
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
    void everyProbeAtEveryPositionMatchesLegacy() throws IOException {
        for (int length = 1; length <= HtmlUtils.INTRINSIC_SCAN_MIN_LENGTH + SWAR_WORD * 4; length++) {
            for (String probe : POSITION_PROBES) {
                for (int pos = 0; pos < length; pos++) {
                    assertMatchesLegacy("a".repeat(pos) + probe + "b".repeat(Math.max(0, length - pos - 1)));
                }
            }
        }
    }

    @Test
    void twoProbesAroundSwarWordBoundariesMatchLegacy() throws IOException {
        for (String first : POSITION_PROBES) {
            for (String second : POSITION_PROBES) {
                for (int a = 0; a < SWAR_WORD * 2; a++) {
                    for (int b = a + 1; b <= a + SWAR_WORD; b++) {
                        assertMatchesLegacy("x".repeat(HtmlUtils.INTRINSIC_SCAN_MIN_LENGTH + a) + first + "y".repeat(b - a) + second + "z".repeat(SWAR_WORD));
                    }
                }
            }
        }
    }

    private static void assertMatchesLegacy(String text) throws IOException {
        for (int flags = 0; flags < 16; flags++) {
            boolean escapeUnicode = (flags & 1) != 0;
            boolean escapeIso = (flags & 2) != 0;
            boolean forXml = (flags & 4) != 0;
            boolean scriptEnabled = (flags & 8) != 0;
            if (!scriptEnabled) {
                assertEquals(legacyText(text, escapeUnicode, escapeIso, forXml, 256), newText(text, escapeUnicode, escapeIso, forXml, 256),
                        describe(text, flags));
            }
            StringWriter expected = new StringWriter();
            LegacyHtmlUtils.writeAttribute(expected, escapeUnicode, escapeIso, text, new char[256], scriptEnabled, forXml);
            StringWriter actual = new StringWriter();
            HtmlUtils.writeAttribute(actual, escapeUnicode, escapeIso, text, new char[256], scriptEnabled, forXml);
            assertEquals(expected.toString(), actual.toString(), describe(text, flags));
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
