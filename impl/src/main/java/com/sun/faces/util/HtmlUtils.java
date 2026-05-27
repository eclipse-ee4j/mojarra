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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.sun.faces.RIConstants;

/**
 * Utility class for HTML. Kudos to Adam Winer (Oracle) for much of this code.
 */
public class HtmlUtils {

    private final static Set<String> UTF_CHARSET = new HashSet<>(Arrays.asList("UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE",
            "x-UTF-16LE-BOM", "X-UTF-32BE-BOM", "X-UTF-32LE-BOM", ""));

    // -------------------------------------------------
    // The following methods include the handling of
    // escape characters....
    // -------------------------------------------------

    static public void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buffer, char[] text, boolean forXml) throws IOException {
        writeText(out, escapeUnicode, escapeIsocode, buffer, text, 0, text.length, forXml);
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
     * <p>The {@code buff} parameter is unused (kept for binary compatibility with callers).
     *
     * @param out the writer to emit to
     * @param escapeUnicode if true, chars &gt; 0xFF are emitted as numeric character references
     * @param escapeIsocode if true, chars in [0xA0, 0xFF] are emitted as named ISO-8859-1 entities
     * @param buff unused (legacy)
     * @param text the input characters
     * @param start start offset into {@code text}
     * @param length number of characters to write
     * @param forXml if true, drop characters not valid in XML (per XML 1.0 spec)
     */
    static public void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, char[] text, int start, int length, boolean forXml) throws IOException {
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
    static public void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, String text, char[] textBuff, boolean forXml) throws IOException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeText(out, escapeUnicode, escapeIsocode, buff, target, 0, length, forXml);
    }

    /**
     * Write String attribute, escaping HTML special characters. Routes through the char[] variant
     * via {@link String#getChars(int, int, char[], int)}; the {@code textBuff} parameter is reused
     * unless the input exceeds its capacity.
     */
    static public void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, String text, char[] textBuff,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeAttribute(out, escapeUnicode, escapeIsocode, buff, target, 0, length, isScriptInAttributeValueEnabled, forXml);
    }

    /**
     * Write char array attribute, escaping HTML special characters as needed.
     *
     * <p>Range-emit strategy (see {@link #writeText(Writer, boolean, boolean, char[], char[], int, int, boolean)}):
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
     *
     * <p>The {@code buff} parameter is unused (kept for binary compatibility).
     */
    static public void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, char[] text, int start, int length,
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

    //
    // Buffering scheme: we use a tremendously simple buffering
    // scheme that greatly reduces the number of calls into the
    // Writer/PrintWriter. In practice this has produced significant
    // measured performance gains (at least in JDK 1.3.1).
    //

    private HtmlUtils() {
    }

    /**
     * Writes a string into URL-encoded format out to a Writer.
     * 
     * All characters before the start of the query string will be encoded using UTF-8.
     * 
     * Characters after the start of the query string will be encoded using a client-defined encoding. You'll need to use
     * the encoding that the server will expect. (HTML forms will generate query strings using the character encoding that
     * the HTML itself was generated in.)
     * 
     * All characters will be encoded as needed for URLs, with the exception of the percent symbol ("%"). Because this is
     * the character itself used for escaping, attempting to escape this character would cause this code to double-escape
     * some strings. It also may be necessary to pre-escape some characters. In particular, a question mark ("?") is
     * considered the start of the query string.
     * 
     *
     * <p>
     * NOTE: This is method is duplicated below. The difference being the acceptance of a char[] for the text to write. Any
     * changes made here, should be made below.
     * </p>
     *
     * @param out a Writer for the output
     * @param text the unencoded (or partially encoded) String
     * @param queryEncoding the character set encoding for after the first question mark
     */
    static public void writeURL(Writer out, String text, char[] textBuff, String queryEncoding) throws IOException, UnsupportedEncodingException {

        int length = text.length();
        if (length >= 16) {
            text.getChars(0, length, textBuff, 0);
            writeURL(out, textBuff, 0, length, queryEncoding);
        } else {
            for (int i = 0; i < length; i++) {
                char ch = text.charAt(i);

                if (ch < 33 || ch > 126) {
                    if (ch == ' ') {
                        out.write('+');
                    } else {
                        textBuff[i] = ch;
                        encodeURIString(out, textBuff, "UTF-8", i, i + 1);
                    }
                }
                // DO NOT encode '%'. If you do, then for starters,
                // we'll double-encode anything that's pre-encoded.
                // And, what's worse, there becomes no way to use
                // characters that must be encoded if you
                // don't want them to be interpreted, like '?' or '&'.
                // else if('%' == ch)
                // {
                // writeURIDoubleHex(out, ch);
                // }
                else if (ch == '"') {
                    out.write("%22");
                }
                // Everything in the query parameters will be decoded
                // as if it were in the request's character set. So use
                // the real encoding for those!
                else if (ch == '?') {
                    out.write('?');
                    encodeURIString(out, text, queryEncoding, i + 1);
                    return;
                } else {
                    out.write(ch);
                }
            }
        }
    }

    /**
     * Writes a string into URL-encoded format out to a Writer.
     * 
     * All characters before the start of the query string will be encoded using UTF-8.
     * 
     * Characters after the start of the query string will be encoded using a client-defined encoding. You'll need to use
     * the encoding that the server will expect. (HTML forms will generate query strings using the character encoding that
     * the HTML itself was generated in.)
     * 
     * All characters will be encoded as needed for URLs, with the exception of the percent symbol ("%"). Because this is
     * the character itself used for escaping, attempting to escape this character would cause this code to double-escape
     * some strings. It also may be necessary to pre-escape some characters. In particular, a question mark ("?") is
     * considered the start of the query string.
     * 
     * <p>
     * NOTE: This is method is duplicated above. The difference being the acceptance of a String for the text to write. Any
     * changes made here, should be made above.
     * </p>
     *
     * @param out a Writer for the output
     * @param textBuff char[] containing the content to write
     * @param queryEncoding the character set encoding for after the first question mark
     */
    static public void writeURL(Writer out, char[] textBuff, int start, int len, String queryEncoding) throws IOException, UnsupportedEncodingException {

        int end = start + len;
        for (int i = start; i < end; i++) {
            char ch = textBuff[i];

            if (ch < 33 || ch > 126) {
                encodeURIString(out, textBuff, "UTF-8", i, i + 1);
            }
            // DO NOT encode '%'. If you do, then for starters,
            // we'll double-encode anything that's pre-encoded.
            // And, what's worse, there becomes no way to use
            // characters that must be encoded if you
            // don't want them to be interpreted, like '?' or '&'.
            // else if('%' == ch)
            // {
            // writeURIDoubleHex(out, ch);
            // }
            else if (ch == '"') {
                out.write("%22");
            }
            // Everything in the query parameters will be decoded
            // as if it were in the request's character set. So use
            // the real encoding for those!
            else if (ch == '?') {
                out.write('?');
                encodeURIString(out, textBuff, queryEncoding, i + 1, end);
                return;
            } else {
                out.write(ch);
            }
        }
    }

    static public void writeTextForXML(Writer out, String text, char[] outbuf) throws IOException {
        char[] textBuffer = new char[128];
        int len = text.length();
        if (textBuffer.length < len) {
            textBuffer = new char[len * 2];
        }
        HtmlUtils.writeText(out, true, true, outbuf, text, textBuffer, true);
    }

    static public void writeUnescapedTextForXML(Writer out, String text) throws IOException {
        final int length = text.length();

        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);

            if (isAllowedXmlCharacter(ch) || isAllowedSurrogateCharacter(ch, i, text)) {
                out.write(ch);
            }
        }
    }

    // Encode a String into URI-encoded form. This code will
    // appear rather (ahem) similar to java.net.URLEncoder
    // This is duplicated below accepting a char[] for the content
    // to write. Any changes here, should be made there as well.
    static private void encodeURIString(Writer out, String text, String encoding, int start) throws IOException {
        MyByteArrayOutputStream buf = null;
        OutputStreamWriter writer = null;
        char[] charArray = null;
        boolean fragment = false;

        int length = text.length();
        for (int i = start; i < length; i++) {
            char ch = text.charAt(i);
            if (DONT_ENCODE_SET.get(ch) || (fragment && ch == '?')) {
                if (ch == '#') {
                    fragment = true; // RFC3986 section 3.5
                }

                if (ch == '&') {
                    if (i + 1 < length && isAmpEscaped(text, i + 1)) {
                        out.write(ch);
                        continue;
                    }
                    out.write(AMP_CHARS);
                } else {
                    out.write(ch);
                }
            } else {
                if (buf == null) {
                    buf = new MyByteArrayOutputStream(MAX_BYTES_PER_CHAR);
                    if (encoding != null) {
                        writer = new OutputStreamWriter(buf, encoding);
                    } else {
                        writer = new OutputStreamWriter(buf, RIConstants.CHAR_ENCODING);
                    }
                    charArray = new char[1];
                }

                // convert to external encoding before hex conversion
                try {
                    // An inspection of OutputStreamWriter reveals
                    // that write(char) always allocates a one element
                    // character array. We can reuse our own.
                    charArray[0] = ch;
                    writer.write(charArray, 0, 1);
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }

                byte[] ba = buf.getBuf();
                for (int j = 0, size = buf.size(); j < size; j++) {
                    writeURIDoubleHex(out, ba[j] + 256);
                }

                buf.reset();
            }
        }
    }

    // Encode a String into URI-encoded form. This code will
    // appear rather (ahem) similar to java.net.URLEncoder
    // This is duplicated above accepting a String for the content
    // to write. Any changes here, should be made there as well.
    static private void encodeURIString(Writer out, char[] textBuff, String encoding, int start, int end) throws IOException {
        MyByteArrayOutputStream buf = null;
        OutputStreamWriter writer = null;
        char[] charArray = null;
        boolean fragment = false;

        for (int i = start; i < end; i++) {
            char ch = textBuff[i];
            if (DONT_ENCODE_SET.get(ch) || (fragment && ch == '?')) {
                if (ch == '#') {
                    fragment = true; // RFC3986 section 3.5
                }

                if (ch == '&') {
                    if (i + 1 < end && isAmpEscaped(textBuff, i + 1)) {
                        out.write(ch);
                        continue;
                    }
                    out.write(AMP_CHARS);
                } else {
                    out.write(ch);
                }
            } else {
                if (buf == null) {
                    buf = new MyByteArrayOutputStream(MAX_BYTES_PER_CHAR);
                    if (encoding != null) {
                        writer = new OutputStreamWriter(buf, encoding);
                    } else {
                        writer = new OutputStreamWriter(buf, RIConstants.CHAR_ENCODING);
                    }
                    charArray = new char[1];
                }

                // convert to external encoding before hex conversion
                try {
                    // An inspection of OutputStreamWriter reveals
                    // that write(char) always allocates a one element
                    // character array. We can reuse our own.
                    charArray[0] = ch;
                    writer.write(charArray, 0, 1);
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }

                byte[] ba = buf.getBuf();
                for (int j = 0, size = buf.size(); j < size; j++) {
                    writeURIDoubleHex(out, ba[j] + 256);
                }

                buf.reset();
            }
        }
    }

    // NOTE: Any changes made to this method should be made
    // in the associated method that accepts a char[] instead
    // of String
    static private boolean isAmpEscaped(String text, int idx) {
        for (int i = 1, ix = idx; i < AMP_CHARS.length; i++, ix++) {
            if (text.charAt(ix) == AMP_CHARS[i]) {
                continue;
            }
            return false;
        }
        return true;
    }

    // NOTE: Any changes made to this method should be made
    // in the associated method that accepts a String instead
    // of char[]
    static private boolean isAmpEscaped(char[] text, int idx) {
        for (int i = 1, ix = idx; i < AMP_CHARS.length; i++, ix++) {
            if (text[ix] == AMP_CHARS[i]) {
                continue;
            }
            return false;
        }
        return true;
    }

    static private void writeURIDoubleHex(Writer out, int i) throws IOException {
        out.write('%');
        out.write(intToHex((i >> 4) % 0x10));
        out.write(intToHex(i % 0x10));
    }

    static private char intToHex(int i) {
        if (i < 10) {
            return (char) ('0' + i);
        } else {
            return (char) ('A' + (i - 10));
        }
    }

    static private final char[] AMP_CHARS = "&amp;".toCharArray();
    static private final char[] QUOT_CHARS = "&quot;".toCharArray();
    static private final char[] GT_CHARS = "&gt;".toCharArray();
    static private final char[] LT_CHARS = "&lt;".toCharArray();
    static private final char[] DEC_REF_START = "&#".toCharArray();
    static private final int MAX_BYTES_PER_CHAR = 10;
    static private final BitSet DONT_ENCODE_SET = new BitSet(256);

    // See: http://www.ietf.org/rfc/rfc2396.txt
    // We're not fully along for that ride either, but we do encode
    // ' ' as '%20', and don't bother encoding '~' or '/'
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            DONT_ENCODE_SET.set(i);
        }

        for (int i = 'A'; i <= 'Z'; i++) {
            DONT_ENCODE_SET.set(i);
        }

        for (int i = '0'; i <= '9'; i++) {
            DONT_ENCODE_SET.set(i);
        }

        // Don't encode '%' - we don't want to double encode anything.
        DONT_ENCODE_SET.set('%');
        // Ditto for '+', which is an encoded space
        DONT_ENCODE_SET.set('+');

        DONT_ENCODE_SET.set('#');
        DONT_ENCODE_SET.set('&');
        DONT_ENCODE_SET.set('=');
        DONT_ENCODE_SET.set('-');
        DONT_ENCODE_SET.set('_');
        DONT_ENCODE_SET.set('.');
        DONT_ENCODE_SET.set('*');
        DONT_ENCODE_SET.set('~');
        DONT_ENCODE_SET.set('/');
        DONT_ENCODE_SET.set('\'');
        DONT_ENCODE_SET.set('!');
        DONT_ENCODE_SET.set('(');
        DONT_ENCODE_SET.set(')');
        DONT_ENCODE_SET.set(';');
    }

    //
    // Entities from HTML 4.0, section 24.2.1; character codes 0xA0 to 0xFF
    //
    static private char[][] sISO8859_1_Entities = new char[][] { "&nbsp;".toCharArray(), "&iexcl;".toCharArray(), "&cent;".toCharArray(),
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

    // ----------------------------------------------------------
    // The following is used to verify encodings
    // ----------------------------------------------------------
    //
    static public boolean validateEncoding(String encoding) {
        return Charset.isSupported(encoding);
    }

    // ----------------------------------------------------------
    // Check if the given encoding is the ISO-8859-1 encoding
    // ----------------------------------------------------------
    //
    static public boolean isISO8859_1encoding(String encoding) {
        return "ISO-8859-1".equals(encoding);
    }

    // ----------------------------------------------------------
    // Check if the given encoding is a UTF encoding
    // ----------------------------------------------------------
    //
    static public boolean isUTFencoding(String encoding) {
        return UTF_CHARSET.contains(encoding);
    }

    // ----------------------------------------------------------
    // The following is used to verify "empty" Html elements.
    // "Empty" Html elements are those that do not require an
    // ending tag. For example, <br> or <hr>...
    // ----------------------------------------------------------

    static public boolean isEmptyElement(String name) {
        char firstChar = name.charAt(0);
        if (firstChar > _LAST_EMPTY_ELEMENT_START) {
            return false;
        }

        // Can we improve performance here? It's certainly slower to use
        // a HashMap, at least if we can't assume the input name is lowercased.
        String[] array = emptyElementArr[firstChar];
        if (array != null) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (name.equalsIgnoreCase(array[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    static private char _LAST_EMPTY_ELEMENT_START = 'p';
    static private String[][] emptyElementArr = new String[_LAST_EMPTY_ELEMENT_START + 1][];

    static private String[] aNames = new String[] { "area", };

    static private String[] bNames = new String[] { "br", "base", "basefont", };

    static private String[] cNames = new String[] { "col", };

    static private String[] fNames = new String[] { "frame", };

    static private String[] hNames = new String[] { "hr", };

    static private String[] iNames = new String[] { "img", "input", "isindex", };

    static private String[] lNames = new String[] { "link", };

    static private String[] mNames = new String[] { "meta", };

    static private String[] pNames = new String[] { "param", };

    static {
        emptyElementArr['a'] = aNames;
        emptyElementArr['A'] = aNames;
        emptyElementArr['b'] = bNames;
        emptyElementArr['B'] = bNames;
        emptyElementArr['c'] = cNames;
        emptyElementArr['C'] = cNames;
        emptyElementArr['f'] = fNames;
        emptyElementArr['F'] = fNames;
        emptyElementArr['h'] = hNames;
        emptyElementArr['H'] = hNames;
        emptyElementArr['i'] = iNames;
        emptyElementArr['I'] = iNames;
        emptyElementArr['l'] = lNames;
        emptyElementArr['L'] = lNames;
        emptyElementArr['m'] = mNames;
        emptyElementArr['M'] = mNames;
        emptyElementArr['p'] = pNames;
        emptyElementArr['P'] = pNames;
    }

    // ----------------------------------------------------------- Inner Classes

    /**
     * <p>
     * Private implementation of ByteArrayOutputStream.
     * </p>
     */
    private static class MyByteArrayOutputStream extends ByteArrayOutputStream {

        public MyByteArrayOutputStream(int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * Obtain access to the underlying byte array to prevent unecessary temp object creation.
         *
         * @return <code>buf</code>
         */
        public byte[] getBuf() {
            return buf;
        }

    }

}
