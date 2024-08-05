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
     * Write char array text.
     */
    static public void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, char[] text, int start, int length, boolean forXml) throws IOException {
        int buffLength = buff.length;
        int buffIndex = 0;

        int end = start + length;
        for (int i = start; i < end; i++) {
            buffIndex = writeTextChar(out, escapeUnicode, escapeIsocode, text[i], buffIndex, buff, buffLength, forXml);
        }

        flushBuffer(out, buff, buffIndex);
    }

    /**
     * Write String text.
     */
    static public void writeText(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, String text, char[] textBuff, boolean forXml) throws IOException {

        int length = text.length();

        if (length >= 16) {
            text.getChars(0, length, textBuff, 0);
            writeText(out, escapeUnicode, escapeIsocode, buff, textBuff, 0, length, forXml);
        } else {
            int buffLength = buff.length;
            int buffIndex = 0;
            for (int i = 0; i < length; i++) {
                char ch = text.charAt(i);
                buffIndex = writeTextChar(out, escapeUnicode, escapeIsocode, ch, buffIndex, buff, buffLength, forXml);
            }
            flushBuffer(out, buff, buffIndex);
        }

    }

    private static int writeTextChar(Writer out, boolean escapeUnicode, boolean escapeIsocode, char ch, int buffIndex, char[] buff, int buffLength, boolean forXml)
            throws IOException {
        int nextIndex;
        if (ch <= 0x1f) {
            if (!isPrintableControlChar(ch, forXml)) {
                return buffIndex;
            }
        }
        if (ch < 0xA0) {
            // If "?" or over, no escaping is needed (this covers
            // most of the Latin alphabet)
            if (ch >= 0x3f) {
                nextIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
            } else if (ch >= 0x27) { // If above "'"...
                // If between "'" and ";", no escaping is needed
                if (ch < 0x3c) {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                } else if (ch == '<') {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, LT_CHARS);
                } else if (ch == '>') {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, GT_CHARS);
                } else {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                }
            } else {
                if (ch == '&') {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, AMP_CHARS);
                } else if (ch == '"') {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, "\"".toCharArray());
                } else {
                    nextIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                }
            }
        } else if (ch <= 0xff) {
            if (escapeIsocode) {
                // ISO-8859-1 entities: encode as needed
                nextIndex = addToBuffer(out, buff, buffIndex, buffLength, sISO8859_1_Entities[ch - 0xA0]);
            } else {
                nextIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
            }
        } else {
            if (escapeUnicode) {
                // UNICODE entities: encode as needed
                nextIndex = _writeDecRef(out, buff, buffIndex, buffLength, ch);
            } else {
                if (forXml && !isAllowedXmlCharacter(ch)) {
                    return buffIndex;
                }

                nextIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
            }
        }
        return nextIndex;
    }

    /**
     * Write a string attribute. Note that this code is duplicated below for character arrays - change both places if you
     * make any changes!!!
     */
    static public void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, String text, char[] textBuff,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {

        int length = text.length();
        if (length >= 16) {
            if (length > textBuff.length) {
                // resize our buffer
                textBuff = new char[length * 2];
            }
            text.getChars(0, length, textBuff, 0);
            writeAttribute(out, escapeUnicode, escapeIsocode, buff, textBuff, 0, length, isScriptInAttributeValueEnabled, forXml);
        } else {
            int buffLength = buff.length;
            int buffIndex = 0;
            for (int i = 0; i < length; i++) {
                char ch = text.charAt(i);

                if (ch <= 0x1f) {
                    if (!isPrintableControlChar(ch, forXml)) {
                        continue;
                    }
                }
                // Tilde or less...
                if (ch < 0xA0) {
                    // If "?" or over, no escaping is needed (this covers
                    // most of the Latin alphabet)
                    if (ch >= 0x3f) {
                        if (ch == 's') {
                            // If putting scripts in attribute values
                            // has been disabled (the defualt), look for
                            // script: in the attribute value.
                            // ensure the attribute value is long enough
                            // to accomodate "script:"
                            if (!isScriptInAttributeValueEnabled && i + 6 < text.length()) {
                                if ('c' == text.charAt(i + 1) && 'r' == text.charAt(i + 2) && 'i' == text.charAt(i + 3) && 'p' == text.charAt(i + 4)
                                        && 't' == text.charAt(i + 5) && ':' == text.charAt(i + 6)) {
                                    return;
                                }
                            }
                        }
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                    } else if (ch >= 0x27) { // If above "'"...
                        // If between "'" and ";", no escaping is needed
                        if (ch < 0x3c) {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                        } else if (ch == '<') {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, LT_CHARS);
                        } else if (ch == '>') {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, GT_CHARS);
                        } else {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                        }
                    } else {
                        if (ch == '&') {
                            // HTML 4.0, section B.7.1: ampersands followed by
                            // an open brace don't get escaped
                            if (i + 1 < length && text.charAt(i + 1) == '{') {
                                buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                            } else {
                                buffIndex = addToBuffer(out, buff, buffIndex, buffLength, AMP_CHARS);
                            }
                        } else if (ch == '"') {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, QUOT_CHARS);
                        } else {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                        }
                    }
                } else if (ch <= 0xff) {
                    if (escapeIsocode) {
                        // ISO-8859-1 entities: encode as needed
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, sISO8859_1_Entities[ch - 0xA0]);
                    } else {
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                    }
                } else {
                    if (escapeUnicode) {
                        // UNICODE entities: encode as needed
                        buffIndex = _writeDecRef(out, buff, buffIndex, buffLength, ch);
                    } else {
                        if (forXml && !isAllowedXmlCharacter(ch)) {
                            continue;
                        }

                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                    }
                }
            }

            flushBuffer(out, buff, buffIndex);
        }
    }

    /**
     * Write a character array attribute. Note that this code is duplicated above for string - change both places if you
     * make any changes!!!
     */
    static public void writeAttribute(Writer out, boolean escapeUnicode, boolean escapeIsocode, char[] buff, char[] text, int start, int length,
            boolean isScriptInAttributeValueEnabled, boolean forXml) throws IOException {
        int buffLength = buff.length;
        int buffIndex = 0;

        int end = start + length;
        for (int i = start; i < end; i++) {
            char ch = text[i];

            // "Application Program Command" or less...
            if (ch <= 0x1f) {
                if (!isPrintableControlChar(ch, forXml)) {
                    continue;
                }
            }
            if (ch < 0xA0) {
                // If "?" or over, no escaping is needed (this covers
                // most of the Latin alphabet)
                if (ch >= 0x3f) {
                    if (ch == 's') {
                        // If putting scripts in attribute values
                        // has been disabled (the defualt), look for
                        // script: in the attribute value.
                        // ensure the attribute value is long enough
                        // to accomodate "script:"
                        if (!isScriptInAttributeValueEnabled && i + 6 < text.length) {
                            if ('c' == text[i + 1] && 'r' == text[i + 2] && 'i' == text[i + 3] && 'p' == text[i + 4] && 't' == text[i + 5]
                                    && ':' == text[i + 6]) {
                                return;
                            }
                        }
                    }

                    buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                } else if (ch >= 0x27) { // If above "'"...
                    if (ch < 0x3c) {
                        // If between "'" and ";", no escaping is needed
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                    } else if (ch == '<') {
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, LT_CHARS);
                    } else if (ch == '>') {
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, GT_CHARS);
                    } else {
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                    }
                } else {
                    if (ch == '&') {
                        // HTML 4.0, section B.7.1: ampersands followed by
                        // an open brace don't get escaped
                        if (i + 1 < end && text[i + 1] == '{') {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                        } else {
                            buffIndex = addToBuffer(out, buff, buffIndex, buffLength, AMP_CHARS);
                        }
                    } else if (ch == '"') {
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, QUOT_CHARS);
                    } else {
                        buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                    }
                }
            } else if (ch <= 0xff) {
                if (escapeIsocode) {
                    // ISO-8859-1 entities: encode as needed
                    buffIndex = addToBuffer(out, buff, buffIndex, buffLength, sISO8859_1_Entities[ch - 0xA0]);
                } else {
                    buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                }
            } else {
                if (escapeUnicode) {
                    // UNICODE entities: encode as needed
                    buffIndex = _writeDecRef(out, buff, buffIndex, buffLength, ch);
                } else {
                    if (forXml && !isAllowedXmlCharacter(ch)) {
                        continue;
                    }

                    buffIndex = addToBuffer(out, buff, buffIndex, buffLength, ch);
                }
            }
        }

        flushBuffer(out, buff, buffIndex);
    }

    static private boolean isPrintableControlChar(int ch, boolean forXml) {

        return (ch == 0x09 || ch == 0x0A || (ch == 0x0C && !forXml) || ch == 0x0D);

    }

    public static boolean isAllowedXmlCharacter(int ch) {
        // See https://www.w3.org/TR/xml/#charsets Character Range
        return ch < 0x20 ? isPrintableControlChar(ch, true) : ch <= 0xD7FF || ch >= 0xE000 && ch <= 0xFFFD; 
    }

    /**
     * Writes a character as a decimal escape. Hex escapes are smaller than the decimal version, but Netscape didn't support
     * hex escapes until 4.7.4.
     */
    static private int _writeDecRef(Writer out, char[] buffer, int bufferIndex, int bufferLength, char ch) throws IOException {
        if (ch == '\u20ac') {
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, EURO_CHARS);
            return bufferIndex;
        }
        bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, DEC_REF_START);
        // Formerly used String.valueOf(). This version tests out
        // about 40% faster in a microbenchmark (and on systems where GC is
        // going gonzo, it should be even better)
        int i = ch;
        if (i > 10000) {
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 10000));
            i = i % 10000;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 1000));
            i = i % 1000;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 100));
            i = i % 100;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 10));
            i = i % 10;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i));
        } else if (i > 1000) {
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 1000));
            i = i % 1000;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 100));
            i = i % 100;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 10));
            i = i % 10;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i));
        } else {
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 100));
            i = i % 100;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i / 10));
            i = i % 10;
            bufferIndex = addToBuffer(out, buffer, bufferIndex, bufferLength, (char) ('0' + i));
        }

        return addToBuffer(out, buffer, bufferIndex, bufferLength, ';');

    }

    //
    // Buffering scheme: we use a tremendously simple buffering
    // scheme that greatly reduces the number of calls into the
    // Writer/PrintWriter. In practice this has produced significant
    // measured performance gains (at least in JDK 1.3.1).
    //

    /**
     * Add a character to the buffer, flushing the buffer if the buffer is full, and returning the new buffer index
     */
    private static int addToBuffer(Writer out, char[] buffer, int bufferIndex, int bufferLength, char ch) throws IOException {
        if (bufferIndex >= bufferLength) {
            out.write(buffer, 0, bufferIndex);
            bufferIndex = 0;
        }

        buffer[bufferIndex] = ch;

        return bufferIndex + 1;
    }

    /**
     * Add an array of characters to the buffer, flushing the buffer if the buffer is full, and returning the new buffer
     * index.
     */
    private static int addToBuffer(Writer out, char[] buffer, int bufferIndex, int bufferLength, char[] toAdd) throws IOException {

        if (bufferIndex >= bufferLength || toAdd.length + bufferIndex >= bufferLength) {
            out.write(buffer, 0, bufferIndex);
            bufferIndex = 0;
        }
        System.arraycopy(toAdd, 0, buffer, bufferIndex, toAdd.length);
        return bufferIndex + toAdd.length;

    }

    /**
     * Flush the contents of the buffer to the output stream and return the reset buffer index
     */
    private static int flushBuffer(Writer out, char[] buffer, int bufferIndex) throws IOException {
        if (bufferIndex > 0) {
            out.write(buffer, 0, bufferIndex);
        }

        return 0;
    }

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

            if (isAllowedXmlCharacter(ch)) {
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

        int length = text.length();
        for (int i = start; i < length; i++) {
            char ch = text.charAt(i);
            if (DONT_ENCODE_SET.get(ch)) {
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

        for (int i = start; i < end; i++) {
            char ch = textBuff[i];
            if (DONT_ENCODE_SET.get(ch)) {
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
    static private final char[] EURO_CHARS = "&euro;".toCharArray();
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
