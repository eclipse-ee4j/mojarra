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

package com.sun.faces.util;

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;

import java.io.IOException;
import java.io.Writer;

/**
 * Frozen copy of the escaping methods of {@link HtmlUtils}: {@code writeText} and {@code writeAttribute} with their helpers.
 * {@link HtmlUtilsEquivalenceTest} uses it as the reference output, so it must never be changed to follow {@link HtmlUtils}.
 */
final class LegacyHtmlUtils {
    private LegacyHtmlUtils() {}

    public static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, boolean forXml) throws IOException {
        writeText(out, escapeUnicode, escapeIsocode, text, 0, text.length, forXml);
    }

    /**
     * Write char array text, escaping HTML special characters as needed.
     *
     * <p>Uses a range-emit strategy: walks the input character by character, tracking the start
     * of the current safe run. When a character requires escaping (or dropping), the pending
     * safe run is bulk-written to the underlying writer via {@code Writer.write(char[], off, len)},
     * the escape sequence is emitted, and a new run begins. At the end the remaining tail is
     * flushed. For plain ASCII content this collapses to a single underlying write.
     *
     * @param out the writer to emit to
     * @param escapeUnicode if true, chars &gt; 0xFF are emitted as numeric character references
     * @param escapeIsocode if true, chars in [0xA0, 0xFF] are emitted as named ISO-8859-1 entities
     * @param text the input characters
     * @param start start offset into {@code text}
     * @param length number of characters to write
     * @param forXml if true, drop characters not valid in XML (per XML 1.0 spec)
     */
    public static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, int start, int length, boolean forXml) throws IOException {
        int end = start + length;
        int runStart = start;

        for (int i = start; i < end; i++) {
            char ch = text[i];

            // Fast path: ASCII printable except <>& (writeText does NOT escape '"' or "'").
            // Hits 99%+ of characters in typical HTML5+UTF-8 output.
            if (ch >= 0x20 && ch < 0x7f && ch != '<' && ch != '>' && ch != '&') {
                continue;
            }

            // Flush the pending safe run before handling this character.
            if (i > runStart) {
                out.write(text, runStart, i - runStart);
            }
            runStart = i + 1;

            if (ch < 0x20) {
                if (isPrintableControlChar(ch, forXml)) {
                    out.write(ch);
                }
                // else: drop (already advanced runStart past it)
            } else if (ch == '<') {
                out.write(LT_CHARS);
            } else if (ch == '>') {
                out.write(GT_CHARS);
            } else if (ch == '&') {
                out.write(AMP_CHARS);
            } else if (ch < 0xA0) {
                // 0x7F (DEL) and 0x80-0x9F (Latin-1 Supplement control range): pass through as-is,
                // matching the legacy behavior. These weren't on the fast path because ch < 0x7f
                // bounded it.
                out.write(ch);
            } else if (ch <= 0xff) {
                if (escapeIsocode) {
                    out.write(sISO8859_1_Entities[ch - 0xA0]);
                } else {
                    out.write(ch);
                }
            } else {
                // ch > 0xff
                if (escapeUnicode) {
                    writeDecRefDirect(out, ch);
                } else if (forXml && !(isAllowedXmlCharacter(ch) || isAllowedSurrogateCharacter(ch, i, text))) {
                    // drop (already advanced runStart)
                } else {
                    out.write(ch);
                }
            }
        }

        if (runStart < end) {
            out.write(text, runStart, end - runStart);
        }
    }

    /**
     * Write String text, escaping HTML special characters as needed. Routes through the char[]
     * variant via {@link String#getChars(int, int, char[], int)}; the {@code textBuff} parameter
     * is reused unless the input exceeds its capacity (uncommon for typical attribute values).
     */
    public static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, String text, char[] textBuff, boolean forXml) throws IOException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeText(out, escapeUnicode, escapeIsocode, target, 0, length, forXml);
    }

    /**
     * Write String attribute, escaping HTML special characters. Routes through the char[] variant
     * via {@link String#getChars(int, int, char[], int)}; the {@code textBuff} parameter is reused
     * unless the input exceeds its capacity.
     */
    public static void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, String text, char[] textBuff,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeAttribute(out, escapeUnicode, escapeIsocode, target, 0, length, isScriptInAttributeValueEnabled, forXml);
    }

    /**
     * Write char array attribute, escaping HTML special characters as needed.
     *
     * <p>Range-emit strategy (see {@link #writeText(Writer, boolean, boolean, char[], int, int, boolean)}):
     * walks the input character by character, tracking the start of the current safe run, and
     * bulk-writes safe runs to the underlying writer. Differences from {@code writeText}:
     * <ul>
     *   <li>The {@code "} double quote is escaped to {@code &quot;}</li>
     *   <li>An ampersand immediately followed by an open brace is NOT escaped (HTML 4 spec B.7.1 -
     *       Netscape-style JavaScript object literal in attribute value)</li>
     *   <li>When {@code !isScriptInAttributeValueEnabled} (default), encountering the literal string
     *       {@code "script:"} in the value causes the method to return WITHOUT writing the pending
     *       safe run -- effectively dropping the entire attribute output as a defence against
     *       JavaScript-URL injection.</li>
     * </ul>
     */
    public static void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, int start, int length,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {
        int end = start + length;
        int runStart = start;

        for (int i = start; i < end; i++) {
            char ch = text[i];

            // Fast path: ASCII printable except <>&" (writeAttribute escapes '"' but not "'").
            // 's' is also fast-path; the script:-injection check happens in the slow path on
            // any hit, since the security check needs to fire BEFORE flushing the safe run.
            if (ch >= 0x20 && ch < 0x7f && ch != '<' && ch != '>' && ch != '&' && ch != '"') {
                // Special case: 's' may begin the literal "script:". Check inline so we can
                // abort BEFORE flushing the safe run (matching the legacy buffer-discard behavior
                // when the script:-disabled path returns mid-method).
                if (ch == 's' && !isScriptInAttributeValueEnabled && i + 6 < end
                        && text[i + 1] == 'c' && text[i + 2] == 'r' && text[i + 3] == 'i'
                        && text[i + 4] == 'p' && text[i + 5] == 't' && text[i + 6] == ':') {
                    return;
                }
                continue;
            }

            // Flush the pending safe run before handling this character.
            if (i > runStart) {
                out.write(text, runStart, i - runStart);
            }
            runStart = i + 1;

            if (ch < 0x20) {
                if (isPrintableControlChar(ch, forXml)) {
                    out.write(ch);
                }
                // else: drop (already advanced runStart past it)
            } else if (ch == '<') {
                out.write(LT_CHARS);
            } else if (ch == '>') {
                out.write(GT_CHARS);
            } else if (ch == '&') {
                // HTML 4.0 section B.7.1: '&{' is the start of a JS object literal in attribute
                // values; preserve the '&' as-is.
                if (i + 1 < end && text[i + 1] == '{') {
                    out.write('&');
                } else {
                    out.write(AMP_CHARS);
                }
            } else if (ch == '"') {
                out.write(QUOT_CHARS);
            } else if (ch < 0xA0) {
                // 0x7F (DEL) and 0x80-0x9F: pass through as-is, matching legacy behavior.
                out.write(ch);
            } else if (ch <= 0xff) {
                if (escapeIsocode) {
                    out.write(sISO8859_1_Entities[ch - 0xA0]);
                } else {
                    out.write(ch);
                }
            } else {
                // ch > 0xff
                if (escapeUnicode) {
                    writeDecRefDirect(out, ch);
                } else if (forXml && !(isAllowedXmlCharacter(ch) || isAllowedSurrogateCharacter(ch, i, text))) {
                    // drop (runStart already advanced)
                } else {
                    out.write(ch);
                }
            }
        }

        if (runStart < end) {
            out.write(text, runStart, end - runStart);
        }
    }

    /**
     * Emits a numeric character reference {@code &#NNN;} for the given character directly via
     * {@link Writer#write(int)} calls -- no intermediate buffer. Always uses the numeric form,
     * which is universally valid across HTML 4, HTML5, XHTML and XML (whereas named entities
     * like {@code &euro;} are invalid in XHTML/XML without a DTD declaration).
     */
    private static void writeDecRefDirect(Writer out, char ch) throws IOException {
        out.write(DEC_REF_START);
        int v = ch;
        if (v >= 10000) {
            out.write('0' + v / 10000);
            v %= 10000;
            out.write('0' + v / 1000);
            v %= 1000;
            out.write('0' + v / 100);
            v %= 100;
            out.write('0' + v / 10);
            v %= 10;
            out.write('0' + v);
        } else if (v >= 1000) {
            out.write('0' + v / 1000);
            v %= 1000;
            out.write('0' + v / 100);
            v %= 100;
            out.write('0' + v / 10);
            v %= 10;
            out.write('0' + v);
        } else if (v >= 100) {
            out.write('0' + v / 100);
            v %= 100;
            out.write('0' + v / 10);
            v %= 10;
            out.write('0' + v);
        } else if (v >= 10) {
            out.write('0' + v / 10);
            v %= 10;
            out.write('0' + v);
        } else {
            out.write('0' + v);
        }
        out.write(';');
    }

    private static boolean isPrintableControlChar(char ch, boolean forXml) {
        return (ch == 0x09 || ch == 0x0A || (ch == 0x0C && !forXml) || ch == 0x0D);

    }

    static boolean isAllowedXmlCharacter(char ch) {
        // See https://www.w3.org/TR/xml/#charsets Character Range
        return ch < 0x20 ? isPrintableControlChar(ch, true) : ch <= 0xD7FF || ch >= 0xE000 && ch <= 0xFFFD; 
    }

    private static boolean isAllowedSurrogateCharacter(char ch, int index, Object originalTextOrChars) {
        if (isHighSurrogate(ch)) {
            return isLowSurrogate(charAt(originalTextOrChars, index + 1));
        }
        else if (isLowSurrogate(ch)) {
            return isHighSurrogate(charAt(originalTextOrChars, index - 1));
        }
        else {
            return false;
        }
    }

    private static final char NO_CHAR = (char) -1;

    private static char charAt(Object originalTextOrChars, int index) {
        if (index < 0) {
            return NO_CHAR;
        }
        else if (originalTextOrChars instanceof String) {
            String text = (String) originalTextOrChars;
            return index < text.length() ? text.charAt(index) : NO_CHAR;
        }
        else {
            char[] chars = (char[]) originalTextOrChars;
            return index < chars.length ? chars[index] : NO_CHAR;
        }
    }

    private static final char[] AMP_CHARS = "&amp;".toCharArray();
    private static final char[] QUOT_CHARS = "&quot;".toCharArray();
    private static final char[] GT_CHARS = "&gt;".toCharArray();
    private static final char[] LT_CHARS = "&lt;".toCharArray();
    private static final char[] DEC_REF_START = "&#".toCharArray();

    //
    // Entities from HTML 4.0, section 24.2.1; character codes 0xA0 to 0xFF
    //
    private static final char[][] sISO8859_1_Entities = new char[][] { "&nbsp;".toCharArray(), "&iexcl;".toCharArray(), "&cent;".toCharArray(),
            "&pound;".toCharArray(), "&curren;".toCharArray(), "&yen;".toCharArray(), "&brvbar;".toCharArray(), "&sect;".toCharArray(), "&uml;".toCharArray(),
            "&copy;".toCharArray(), "&ordf;".toCharArray(), "&laquo;".toCharArray(), "&not;".toCharArray(), "&shy;".toCharArray(), "&reg;".toCharArray(),
            "&macr;".toCharArray(), "&deg;".toCharArray(), "&plusmn;".toCharArray(), "&sup2;".toCharArray(), "&sup3;".toCharArray(), "&acute;".toCharArray(),
            "&micro;".toCharArray(), "&para;".toCharArray(), "&middot;".toCharArray(), "&cedil;".toCharArray(), "&sup1;".toCharArray(), "&ordm;".toCharArray(),
            "&raquo;".toCharArray(), "&frac14;".toCharArray(), "&frac12;".toCharArray(), "&frac34;".toCharArray(), "&iquest;".toCharArray(),
            "&Agrave;".toCharArray(), "&Aacute;".toCharArray(), "&Acirc;".toCharArray(), "&Atilde;".toCharArray(), "&Auml;".toCharArray(),
            "&Aring;".toCharArray(), "&AElig;".toCharArray(), "&Ccedil;".toCharArray(), "&Egrave;".toCharArray(), "&Eacute;".toCharArray(),
            "&Ecirc;".toCharArray(), "&Euml;".toCharArray(), "&Igrave;".toCharArray(), "&Iacute;".toCharArray(), "&Icirc;".toCharArray(),
            "&Iuml;".toCharArray(), "&ETH;".toCharArray(), "&Ntilde;".toCharArray(), "&Ograve;".toCharArray(), "&Oacute;".toCharArray(),
            "&Ocirc;".toCharArray(), "&Otilde;".toCharArray(), "&Ouml;".toCharArray(), "&times;".toCharArray(), "&Oslash;".toCharArray(),
            "&Ugrave;".toCharArray(), "&Uacute;".toCharArray(), "&Ucirc;".toCharArray(), "&Uuml;".toCharArray(), "&Yacute;".toCharArray(),
            "&THORN;".toCharArray(), "&szlig;".toCharArray(), "&agrave;".toCharArray(), "&aacute;".toCharArray(), "&acirc;".toCharArray(),
            "&atilde;".toCharArray(), "&auml;".toCharArray(), "&aring;".toCharArray(), "&aelig;".toCharArray(), "&ccedil;".toCharArray(),
            "&egrave;".toCharArray(), "&eacute;".toCharArray(), "&ecirc;".toCharArray(), "&euml;".toCharArray(), "&igrave;".toCharArray(),
            "&iacute;".toCharArray(), "&icirc;".toCharArray(), "&iuml;".toCharArray(), "&eth;".toCharArray(), "&ntilde;".toCharArray(),
            "&ograve;".toCharArray(), "&oacute;".toCharArray(), "&ocirc;".toCharArray(), "&otilde;".toCharArray(), "&ouml;".toCharArray(),
            "&divide;".toCharArray(), "&oslash;".toCharArray(), "&ugrave;".toCharArray(), "&uacute;".toCharArray(), "&ucirc;".toCharArray(),
            "&uuml;".toCharArray(), "&yacute;".toCharArray(), "&thorn;".toCharArray(), "&yuml;".toCharArray() };
}
