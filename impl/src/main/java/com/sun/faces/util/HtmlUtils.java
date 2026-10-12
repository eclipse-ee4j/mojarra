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
import static java.lang.Character.isSurrogate;
import static java.lang.Character.toCodePoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.Set;

import com.sun.faces.RIConstants;

/**
 * Utility class for HTML. Kudos to Adam Winer (Oracle) for much of this code.
 */
public final class HtmlUtils {
    private HtmlUtils() {}

    private static final String ISO_8859_1 = StandardCharsets.ISO_8859_1.name();

    private static final Set<String> UTF_CHARSET = Set.of("UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE",
            "x-UTF-16LE-BOM", "X-UTF-16LE-BOM", "X-UTF-32BE-BOM", "X-UTF-32LE-BOM", "");

    // -------------------------------------------------
    // The following methods include the handling of
    // escape characters....
    // -------------------------------------------------

    public static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, boolean forXml) throws IOException {
        writeText(out, escapeUnicode, escapeIsocode, text, 0, text.length, forXml);
    }

    // ---------------------------------------------------------------- escaping core
    //
    // Every character falls in one of two classes:
    //   NEEDS_HANDLING  escaped, dropped or otherwise handled one by one,
    //   UNCHANGED       written unchanged: ASCII printable, TAB, LF, CR, DEL, C1 controls, and non-ASCII characters
    //                   whenever the writer does not escape them.
    // UNCHANGED characters extend the current run, so an accented, CJK or multi-line text is written in bulk.
    //
    // A String is first scanned in place; when nothing needs handling (by far the most common case) it is written as
    // is, without copying it into the char buffer. Long inputs are scanned with String.indexOf (a SIMD intrinsic) plus a
    // SWAR test for control characters. The scan is conservative: it may stop early on a character that turns out to be
    // UNCHANGED, after which the exact loop takes over, so it never changes the output.

    private static final byte NEEDS_HANDLING = 0;
    private static final byte UNCHANGED = 1;

    private static final byte[] TEXT_CLASS = new byte[128];
    private static final byte[] ATTRIBUTE_CLASS = new byte[128];

    static {
        for (int c = 0x20; c < 0x7F; c++) {
            TEXT_CLASS[c] = UNCHANGED;
        }
        TEXT_CLASS['<'] = TEXT_CLASS['>'] = TEXT_CLASS['&'] = NEEDS_HANDLING;
        TEXT_CLASS['\t'] = TEXT_CLASS['\n'] = TEXT_CLASS['\r'] = TEXT_CLASS[0x7F] = UNCHANGED;
        // FF (0x0C) is written in HTML but dropped in XML; it stays NEEDS_HANDLING so the slow path decides.
        System.arraycopy(TEXT_CLASS, 0, ATTRIBUTE_CLASS, 0, 128);
        ATTRIBUTE_CLASS['"'] = NEEDS_HANDLING;
    }

    /** Minimum length from which a String is scanned with String.indexOf plus SWAR (measured break-even ~40). */
    static final int INTRINSIC_SCAN_MIN_LENGTH = 48;

    private static final long SWAR_ONES = 0x0101010101010101L;
    private static final long SWAR_HIGHS = 0x8080808080808080L;
    private static final long SWAR_SPACES = SWAR_ONES * 0x20;
    private static final VarHandle LONGS = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final ThreadLocal<byte[]> SWAR_BUFFER = ThreadLocal.withInitial(() -> new byte[1024]);

    private static byte classOf(char ch, byte[] asciiClasses, boolean escapeUnicode, boolean escapeIsocode, boolean forXml) {
        if (ch < 0x80) {
            return asciiClasses[ch];
        }
        if (ch < 0xA0) {
            return UNCHANGED;
        }
        if (ch <= 0xFF) {
            return escapeIsocode ? NEEDS_HANDLING : UNCHANGED;
        }
        if (escapeUnicode) {
            return NEEDS_HANDLING;
        }
        // In XML, surrogates are kept only in valid pairs and U+FFFE/U+FFFF are dropped: decided on the slow path.
        if (forXml && ch >= 0xD800 && (ch <= 0xDFFF || ch >= 0xFFFE)) {
            return NEEDS_HANDLING;
        }
        return UNCHANGED;
    }

    /**
     * Index of the first character of {@code text} that may need handling (conservative), or its length if none.
     * {@code attribute} adds the double quote; script: detection is the caller's business.
     */
    private static int scan(String text, boolean attribute, boolean escapeUnicode, boolean escapeIsocode, boolean forXml) {
        int length = text.length();
        boolean nonAsciiSensitive = escapeUnicode || escapeIsocode;

        if (length >= INTRINSIC_SCAN_MIN_LENGTH && !nonAsciiSensitive && !forXml) {
            return intrinsicScan(text, attribute);
        }

        byte[] classes = attribute ? ATTRIBUTE_CLASS : TEXT_CLASS;
        for (int i = 0; i < length; i++) {
            if (classOf(text.charAt(i), classes, escapeUnicode, escapeIsocode, forXml) == NEEDS_HANDLING) {
                return i;
            }
        }
        return length;
    }

    /**
     * Conservative scan of an HTML (non-XML, non-escaping) String: String.indexOf, which HotSpot replaces with a SIMD
     * stub, finds the special characters, and the low byte of each char (copied with the deprecated but arraycopy-fast
     * String.getBytes) is tested 8 at a time for control characters. The low-byte copy can only cause false positives.
     */
    @SuppressWarnings("deprecation")
    private static int intrinsicScan(String text, boolean attribute) {
        int length = text.length();
        int found = indexOf(text, '<', length);
        found = indexOf(text, '>', found);
        found = indexOf(text, '&', found);
        if (attribute) {
            found = indexOf(text, '"', found);
        }

        byte[] bytes = SWAR_BUFFER.get();
        if (bytes.length < found) {
            bytes = new byte[found];
        }
        text.getBytes(0, found, bytes, 0);
        int i = 0;
        for (; i + 8 <= found; i += 8) {
            long x = (long) LONGS.get(bytes, i);
            long controls = (x - SWAR_SPACES) & ~x & SWAR_HIGHS;
            if (controls != 0) {
                return i + (Long.numberOfTrailingZeros(controls) >>> 3);
            }
        }
        for (; i < found; i++) {
            if ((bytes[i] & 0xFF) < 0x20) {
                return i;
            }
        }
        return found;
    }

    /** Index of {@code ch} in {@code text} before {@code limit}, or {@code limit}. */
    private static int indexOf(String text, char ch, int limit) {
        int index = text.indexOf(ch);
        return index >= 0 && index < limit ? index : limit;
    }

    /**
     * Write char array text, escaping HTML special characters as needed.
     *
     * @param out the writer to emit to
     * @param escapeUnicode if true, characters above U+00FF are emitted as numeric character references
     * @param escapeIsocode if true, chars in [0xA0, 0xFF] are emitted as named ISO-8859-1 entities
     * @param text the input characters
     * @param start start offset into {@code text}
     * @param length number of characters to write
     * @param forXml if true, drop characters not valid in XML (per XML 1.0 spec)
     */
    public static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, int start, int length, boolean forXml) throws IOException {
        writeText(out, escapeUnicode, escapeIsocode, text, start, start, start + length, forXml);
    }

    /**
     * The exact text loop. Characters in [start, from) are known not to need handling and form the initial run.
     */
    private static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, int start, int from, int end, boolean forXml) throws IOException {
        int runStart = start;

        for (int i = from; i < end; i++) {
            char ch = text[i];

            if (classOf(ch, TEXT_CLASS, escapeUnicode, escapeIsocode, forXml) != NEEDS_HANDLING) {
                continue;
            }

            // Flush the pending run before handling this character.
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
            } else if (ch <= 0xff) {
                // only reached with escapeIsocode (otherwise UNCHANGED)
                out.write(sISO8859_1_Entities[ch - 0xA0]);
            } else {
                i = writeAboveLatin1(out, escapeUnicode, text, i, end, forXml);
                runStart = i + 1;
            }
        }

        if (runStart < end) {
            out.write(text, runStart, end - runStart);
        }
    }

    /**
     * Write String text, escaping HTML special characters as needed. When nothing needs escaping the String is written
     * as is; otherwise it is copied into {@code textBuff} (or a new array when it does not fit) and escaped from there.
     */
    public static void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, String text, char[] textBuff, boolean forXml) throws IOException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        int from = scan(text, false, escapeUnicode, escapeIsocode, forXml);
        if (from == length) {
            out.write(text);
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeText(out, escapeUnicode, escapeIsocode, target, 0, from, length, forXml);
    }

    /**
     * Write String attribute, escaping HTML special characters. When nothing needs escaping the String is written as
     * is; otherwise it is copied into {@code textBuff} (or a new array when it does not fit) and escaped from there.
     */
    public static void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, String text, char[] textBuff,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        if (!isScriptInAttributeValueEnabled && isScriptURL(text)) {
            return;
        }
        int from = scan(text, true, escapeUnicode, escapeIsocode, forXml);
        if (from == length) {
            out.write(text);
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeAttribute(out, escapeUnicode, escapeIsocode, target, 0, from, length, forXml);
    }

    /**
     * Write char array attribute, escaping HTML special characters as needed. Differences from {@code writeText}:
     * <ul>
     *   <li>The {@code "} double quote is escaped to {@code &quot;}</li>
     *   <li>An ampersand immediately followed by an open brace is NOT escaped (HTML 4 spec B.7.1 -
     *       Netscape-style JavaScript object literal in attribute value)</li>
     *   <li>When {@code !isScriptInAttributeValueEnabled}, a value which {@link #isScriptURL(CharSequence)} is not
     *       written at all, as a defense against JavaScript-URL injection.</li>
     * </ul>
     */
    public static void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, int start, int length,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {
        if (!isScriptInAttributeValueEnabled && isScriptURL(CharBuffer.wrap(text, start, length))) {
            return;
        }

        writeAttribute(out, escapeUnicode, escapeIsocode, text, start, start, start + length, forXml);
    }

    /**
     * The exact attribute loop. Characters in [start, from) are known not to need handling and form the initial run.
     */
    private static void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] text, int start, int from, int end,
            boolean forXml) throws IOException {
        int runStart = start;

        for (int i = from; i < end; i++) {
            char ch = text[i];

            if (classOf(ch, ATTRIBUTE_CLASS, escapeUnicode, escapeIsocode, forXml) != NEEDS_HANDLING) {
                continue;
            }

            // Flush the pending run before handling this character.
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
            } else if (ch <= 0xff) {
                // only reached with escapeIsocode (otherwise UNCHANGED)
                out.write(sISO8859_1_Entities[ch - 0xA0]);
            } else {
                i = writeAboveLatin1(out, escapeUnicode, text, i, end, forXml);
                runStart = i + 1;
            }
        }

        if (runStart < end) {
            out.write(text, runStart, end - runStart);
        }
    }

    /**
     * Writes the character above U+00FF at {@code text[i]}: dropped when {@code forXml} and XML does not allow it, else as
     * a numeric character reference when {@code escapeUnicode}, else as is. A surrogate pair is one character.
     *
     * @return the index of the last char written or dropped, which is {@code i + 1} for an escaped surrogate pair
     */
    private static int writeAboveLatin1(Writer out, boolean escapeUnicode, char[] text, int i, int end, boolean forXml) throws IOException {
        char ch = text[i];

        if (forXml && !(isAllowedXmlCharacter(ch) || isAllowedSurrogateCharacter(ch, i, text))) {
            return i;
        }

        if (!escapeUnicode) {
            out.write(ch);
            return i;
        }

        if (isHighSurrogate(ch) && i + 1 < end && isLowSurrogate(text[i + 1])) {
            writeDecRefDirect(out, toCodePoint(ch, text[i + 1]));
            return i + 1;
        }

        if (forXml && isSurrogate(ch)) {
            return i;
        }

        writeDecRefDirect(out, ch);
        return i;
    }

    /**
     * Emits a numeric character reference {@code &#NNN;} for the given code point directly via
     * {@link Writer#write(int)} calls -- no intermediate buffer. Always uses the numeric form,
     * which is universally valid across HTML 4, HTML5, XHTML and XML (whereas named entities
     * like {@code &euro;} are invalid in XHTML/XML without a DTD declaration).
     */
    private static void writeDecRefDirect(Writer out, int codePoint) throws IOException {
        out.write(DEC_REF_START);
        writeDecimal(out, codePoint);
        out.write(';');
    }

    private static void writeDecimal(Writer out, int value) throws IOException {
        if (value >= 10) {
            writeDecimal(out, value / 10);
        }
        out.write('0' + value % 10);
    }

    /**
     * Returns whether the given attribute value is a URL whose scheme runs script, the way a browser parses the scheme:
     * leading whitespace and control characters are ignored, tabs and line breaks are ignored anywhere, and the scheme
     * is case-insensitive. So {@code " JAVA\tSCRIPT:alert(1)"} is one, and {@code "Transcript: page 1"} is not. The
     * characters which {@code writeAttribute} drops are ignored anywhere too, because the browser never sees them.
     *
     * @param value the attribute value
     * @return whether the scheme of the value is one of {@code javascript} and {@code vbscript}
     */
    public static boolean isScriptURL(CharSequence value) {
        int start = 0;

        while (start < value.length() && (value.charAt(start) == ' ' || isIgnoredInScheme(value.charAt(start)))) {
            start++;
        }

        for (String scheme : SCRIPT_SCHEMES) {
            if (hasScheme(value, start, scheme)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasScheme(CharSequence value, int start, String scheme) {
        int matched = 0;

        for (int i = start; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (isIgnoredInScheme(ch)) {
                continue;
            }

            if (matched == scheme.length()) {
                return ch == ':';
            }

            if (toLowerAscii(ch) != scheme.charAt(matched++)) {
                return false;
            }
        }

        return false;
    }

    /**
     * Whether a browser ignores the given char in a scheme, or {@code writeAttribute} may drop it. This covers the
     * {@code forXml} drops in HTML mode too, and surrogate pairs, which only makes the guard drop a few more harmless
     * values.
     */
    private static boolean isIgnoredInScheme(char ch) {
        return ch < ' ' || !isAllowedXmlCharacter(ch);
    }

    private static char toLowerAscii(char ch) {
        return ch >= 'A' && ch <= 'Z' ? (char) (ch + ('a' - 'A')) : ch;
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

    /**
     * Writes a string into URL-encoded format out to a Writer.
     *
     * <p>All characters before the start of the query string will be encoded using UTF-8.
     * Characters after the start of the query string will be encoded using the client-defined
     * {@code queryEncoding} -- this needs to match the encoding the server will use to decode
     * the query string (HTML forms generate query strings using the character encoding the HTML
     * itself was generated in).
     *
     * <p>All characters will be encoded as needed for URLs, with the exception of the percent
     * symbol ({@code %}). Because that's the character used for escaping itself, attempting to
     * escape it would double-encode anything already pre-encoded. It also may be necessary to
     * pre-escape some characters; in particular, the first {@code ?} is treated as the start of
     * the query string.
     *
     * <p>Delegates to {@link #writeURL(Writer, char[], int, int, String)} via {@code getChars} --
     * a single range-emit pass over the char buffer is faster than per-char {@code charAt} +
     * branch-per-char for any non-trivial input, and avoids duplicating the per-character logic.
     *
     * @param out a Writer for the output
     * @param text the unencoded (or partially encoded) String
     * @param textBuff scratch buffer, reused when sized large enough to hold {@code text}
     * @param queryEncoding the character set encoding for after the first question mark
     */
    public static void writeURL(Writer out, String text, char[] textBuff, String queryEncoding) throws IOException, UnsupportedEncodingException {
        int length = text.length();
        if (length == 0) {
            return;
        }
        char[] target = (length > textBuff.length) ? new char[length] : textBuff;
        text.getChars(0, length, target, 0);
        writeURL(out, target, 0, length, queryEncoding);
    }

    /**
     * Writes a char-array slice into URL-encoded format out to a Writer.
     *
     * <p>Range-emit strategy: walk the input character by character, tracking the start of the
     * current safe run, and bulk-write safe runs to the underlying Writer instead of dispatching
     * a single {@code out.write(ch)} per character. The safe-run check is one bounds-check plus
     * two exclusions ({@code ch > 32 && ch < 127 && ch != '"' && ch != '?'}). Inputs consisting
     * entirely of unreserved ASCII (the common case) collapse to a single underlying write.
     *
     * <p>For chars outside the safe run:
     * <ul>
     *   <li>{@code ?} -- writes {@code ?}, then delegates the remainder to
     *       {@link #encodeURIString(Writer, char[], String, int, int)} using {@code queryEncoding}
     *       and returns.</li>
     *   <li>{@code "} -- escaped to {@code %22} (HTML attribute value safety).</li>
     *   <li>{@code ch < 33} or {@code ch > 126} -- percent-encoded through UTF-8.</li>
     * </ul>
     *
     * <p>Importantly, {@code %} is NOT encoded: encoding it would double-encode anything already
     * pre-encoded and would defeat callers who need to embed already-encoded {@code ?}/{@code &}
     * etc.
     *
     * @param out a Writer for the output
     * @param textBuff char[] containing the content to write
     * @param start the offset in {@code textBuff} at which to start
     * @param len the number of chars to process starting at {@code start}
     * @param queryEncoding the character set encoding for after the first question mark
     */
    public static void writeURL(Writer out, char[] textBuff, int start, int len, String queryEncoding) throws IOException, UnsupportedEncodingException {
        int end = start + len;
        int runStart = start;
        FastByteArrayOutputStream buf = null;
        OutputStreamWriter writer = null;
        char[] charArray = null;

        for (int i = start; i < end; i++) {
            char ch = textBuff[i];

            if (ch > 32 && ch < 127 && ch != '"' && ch != '?') {
                continue;
            }

            if (i > runStart) {
                out.write(textBuff, runStart, i - runStart);
            }

            if (ch == '?') {
                out.write('?');
                encodeURIString(out, textBuff, queryEncoding, i + 1, end);
                return;
            }
            if (ch == '"') {
                out.write("%22");
                runStart = i + 1;
                continue;
            }
            if (buf == null) {
                buf = new FastByteArrayOutputStream(MAX_BYTES_PER_CHAR);
                writer = new OutputStreamWriter(buf, StandardCharsets.UTF_8);
                charArray = new char[1];
            }
            encodeCharPercentBytes(out, ch, buf, writer, charArray);
            runStart = i + 1;
        }

        if (runStart < end) {
            out.write(textBuff, runStart, end - runStart);
        }
    }

    public static void writeTextForXML(Writer out, String text) throws IOException {
        int len = text.length();
        char[] textBuffer = new char[Math.max(128, len)];
        HtmlUtils.writeText(out, true, true, text, textBuffer, true);
    }

    public static void writeUnescapedTextForXML(Writer out, String text) throws IOException {
        final int length = text.length();

        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);

            if (isAllowedXmlCharacter(ch) || isAllowedSurrogateCharacter(ch, i, text)) {
                out.write(ch);
            }
        }
    }

    /**
     * Encodes a char-array slice into URI-encoded form (rather similar to {@link java.net.URLEncoder}).
     *
     * <p>Range-emit: walks the input tracking the start of the current safe run, and bulk-writes
     * safe runs to the underlying Writer. A character is "safe" if it's in {@link #DONT_ENCODE_SET}
     * AND not {@code &} (needs lookahead for {@code &amp;} non-double-escape) AND not {@code #}
     * (sets fragment mode). Within fragment mode (after the first {@code #}, per RFC 3986 section
     * 3.5) the {@code ?} character is also safe.
     *
     * <p>Characters not in the safe run are dispatched as follows:
     * <ul>
     *   <li>{@code &} -- if immediately followed by {@code amp;}, write the bare {@code &}
     *       (don't double-escape); otherwise emit {@code &amp;}.</li>
     *   <li>{@code #} -- write {@code #} and enter fragment mode.</li>
     *   <li>Anything else -- percent-encode through the requested external encoding.</li>
     * </ul>
     *
     * <p>The {@link FastByteArrayOutputStream}/{@link OutputStreamWriter} pair used for non-ASCII
     * percent-encoding is lazily allocated on first need and shared across all encode calls in
     * a single invocation -- matching the original allocation amortization.
     */
    private static void encodeURIString(Writer out, char[] textBuff, String encoding, int start, int end) throws IOException {
        int runStart = start;
        boolean fragment = false;
        FastByteArrayOutputStream buf = null;
        OutputStreamWriter writer = null;
        char[] charArray = null;

        for (int i = start; i < end; i++) {
            char ch = textBuff[i];

            if (DONT_ENCODE_SET.get(ch) && ch != '&' && ch != '#') {
                continue;
            }
            if (fragment && ch == '?') {
                continue;
            }

            if (i > runStart) {
                out.write(textBuff, runStart, i - runStart);
            }
            runStart = i + 1;

            if (ch == '#') {
                out.write('#');
                fragment = true; // RFC 3986 section 3.5
                continue;
            }
            if (ch == '&') {
                if (i + 1 < end && isAmpEscaped(textBuff, i + 1)) {
                    out.write('&');
                } else {
                    out.write(AMP_CHARS);
                }
                continue;
            }

            if (buf == null) {
                buf = new FastByteArrayOutputStream(MAX_BYTES_PER_CHAR);
                writer = new OutputStreamWriter(buf, encoding != null ? encoding : RIConstants.CHAR_ENCODING);
                charArray = new char[1];
            }
            encodeCharPercentBytes(out, ch, buf, writer, charArray);
        }

        if (runStart < end) {
            out.write(textBuff, runStart, end - runStart);
        }
    }

    /**
     * Percent-encode a single char by routing it through {@code writer} (which wraps {@code buf}
     * with the desired charset) and emitting {@code %XX} for each resulting byte. The shared
     * {@code buf}/{@code writer}/{@code charArray} are reused across calls in the same enclosing
     * encode loop.
     */
    private static void encodeCharPercentBytes(Writer out, char ch, FastByteArrayOutputStream buf,
            OutputStreamWriter writer, char[] charArray) throws IOException {
        try {
            // OutputStreamWriter#write(char) always allocates a one-element char array; we reuse our own.
            charArray[0] = ch;
            writer.write(charArray, 0, 1);
            writer.flush();
        } catch (IOException e) {
            buf.reset();
            return;
        }
        byte[] ba = buf.getBuffer();
        for (int j = 0, size = buf.size(); j < size; j++) {
            writeURIDoubleHex(out, ba[j] + 256);
        }
        buf.reset();
    }

    private static boolean isAmpEscaped(char[] text, int idx) {
        if (idx + AMP_CHARS.length - 1 > text.length) {
            return false;
        }
        for (int i = 1, ix = idx; i < AMP_CHARS.length; i++, ix++) {
            if (text[ix] != AMP_CHARS[i]) {
                return false;
            }
        }
        return true;
    }

    private static void writeURIDoubleHex(Writer out, int i) throws IOException {
        out.write('%');
        out.write(intToHex((i >> 4) % 0x10));
        out.write(intToHex(i % 0x10));
    }

    private static char intToHex(int i) {
        if (i < 10) {
            return (char) ('0' + i);
        } else {
            return (char) ('A' + (i - 10));
        }
    }

    private static final char[] AMP_CHARS = "&amp;".toCharArray();
    private static final char[] QUOT_CHARS = "&quot;".toCharArray();
    private static final char[] GT_CHARS = "&gt;".toCharArray();
    private static final char[] LT_CHARS = "&lt;".toCharArray();
    private static final char[] DEC_REF_START = "&#".toCharArray();
    private static final String[] SCRIPT_SCHEMES = { "javascript", "vbscript" };
    private static final int MAX_BYTES_PER_CHAR = 10;
    private static final BitSet DONT_ENCODE_SET = new BitSet(256);

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

    // ----------------------------------------------------------
    // The following is used to verify encodings
    // ----------------------------------------------------------
    //
    public static boolean validateEncoding(String encoding) {
        return Charset.isSupported(encoding);
    }

    // ----------------------------------------------------------
    // Check if the given encoding is the ISO-8859-1 encoding
    // ----------------------------------------------------------
    //
    public static boolean isISO8859_1encoding(String encoding) {
        return ISO_8859_1.equals(encoding);
    }

    // ----------------------------------------------------------
    // Check if the given encoding is a UTF encoding
    // ----------------------------------------------------------
    //
    public static boolean isUTFencoding(String encoding) {
        return encoding != null && UTF_CHARSET.contains(encoding);
    }

    // ----------------------------------------------------------
    // The following is used to verify "empty" Html elements.
    // "Empty" Html elements are those that do not require an
    // ending tag. For example, <br> or <hr>...
    // ----------------------------------------------------------

    public static boolean isEmptyElement(String name) {
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

    private static final char _LAST_EMPTY_ELEMENT_START = 'p';
    private static final String[][] emptyElementArr = new String[_LAST_EMPTY_ELEMENT_START + 1][];

    private static final String[] aNames = new String[] { "area", };

    private static final String[] bNames = new String[] { "br", "base", "basefont", };

    private static final String[] cNames = new String[] { "col", };

    private static final String[] fNames = new String[] { "frame", };

    private static final String[] hNames = new String[] { "hr", };

    private static final String[] iNames = new String[] { "img", "input", "isindex", };

    private static final String[] lNames = new String[] { "link", };

    private static final String[] mNames = new String[] { "meta", };

    private static final String[] pNames = new String[] { "param", };

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
     * Unsynchronized {@link OutputStream} that accumulates into a {@code byte[]} and hands that
     * array out directly, so a percent-encoded character can be read back without a defensive copy.
     * </p>
     */
    private static final class FastByteArrayOutputStream extends OutputStream {

        private byte[] buf;
        private int count;

        FastByteArrayOutputStream(int size) {
            if (size < 0) {
                throw new IllegalArgumentException("Negative initial size: " + size);
            }
            buf = new byte[size];
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity - buf.length > 0) {
                grow(minCapacity);
            }
        }

        private void grow(int minCapacity) {
            int oldCapacity = buf.length;
            int newCapacity = oldCapacity << 1;

            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity < 0) {
                if (minCapacity < 0) { // overflow
                    throw new OutOfMemoryError();
                }
                newCapacity = Integer.MAX_VALUE;
            }
            buf = Arrays.copyOf(buf, newCapacity);
        }

        @Override
        public void write(int b) {
            ensureCapacity(count + 1);
            buf[count] = (byte) b;
            count += 1;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            Objects.checkFromIndexSize(off, len, b.length);
            ensureCapacity(count + len);
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }

        public void reset() {
            count = 0;
        }

        public int size() {
            return count;
        }

        /**
         * Obtain access to the underlying byte array to prevent unnecessary copy.
         *
         * @return the internal <code>buf</code>
         */
        public byte[] getBuffer() {
            return buf;
        }

    }

}
