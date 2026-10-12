/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.faces.util.HtmlUtils.isAllowedXmlCharacter;
import static java.lang.Character.toChars;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

/**
 * Tests for {@link HtmlUtils}.
 *
 * <p>Covers the per-character escape paths in {@code writeText} and {@code writeAttribute}
 * (regression guard for any refactor of the escape logic -- range-emit, pre-scan, sorted-table
 * approaches, etc. must produce byte-identical output), the URL writing variants in
 * {@code writeURL}, and the XML-mode variants ({@code writeTextForXML},
 * {@code writeUnescapedTextForXML}, {@code writeAttribute} with forXml=true).
 */
class HtmlUtilsTest {

    private static final char[] TEXT_BUFFER = new char[1024];

    // -------- writeText -----------------------------------------------------

    @Test
    void writeText_plainAscii_passesUnchanged() throws IOException {
        assertEquals("Hello World", writeText("Hello World"));
    }

    @Test
    void writeText_emptyString() throws IOException {
        assertEquals("", writeText(""));
    }

    @Test
    void writeText_escapesLt() throws IOException {
        assertEquals("a &lt; b", writeText("a < b"));
    }

    @Test
    void writeText_escapesGt() throws IOException {
        assertEquals("a &gt; b", writeText("a > b"));
    }

    @Test
    void writeText_escapesAmp() throws IOException {
        assertEquals("a &amp; b", writeText("a & b"));
    }

    @Test
    void writeText_quoteIsPassedThroughUnchanged() throws IOException {
        // writeText (unlike writeAttribute) does NOT escape double quotes -- it writes them as-is.
        assertEquals("\"x\"", writeText("\"x\""));
    }

    @Test
    void writeText_singleQuoteIsPassedThroughUnchanged() throws IOException {
        assertEquals("'x'", writeText("'x'"));
    }

    @Test
    void writeText_mixedHtmlSpecials() throws IOException {
        assertEquals("&lt;a&gt; &amp; &lt;/b&gt;", writeText("<a> & </b>"));
    }

    @Test
    void writeText_consecutiveSpecials() throws IOException {
        assertEquals("&lt;&gt;&amp;", writeText("<>&"));
    }

    @Test
    void writeText_longPlainStringWithGetCharsPath() throws IOException {
        // Strings >= 16 chars take the text.getChars() path internally.
        String input = "The quick brown fox jumps over the lazy dog";
        assertEquals(input, writeText(input));
    }

    @Test
    void writeText_longStringWithEscapes() throws IOException {
        String input = "The quick brown <fox> jumps over & the lazy dog";
        assertEquals("The quick brown &lt;fox&gt; jumps over &amp; the lazy dog", writeText(input));
    }

    @Test
    void writeText_tabNewlineCarriageReturnArePreserved() throws IOException {
        assertEquals("a\tb\nc\rd", writeText("a\tb\nc\rd"));
    }

    @Test
    void writeText_otherControlCharsAreDropped() throws IOException {
        // Control chars (< 0x20) that aren't \t \n \r (or \f outside XML) are dropped.
        assertEquals("ab", writeText("a\u0001b"));
        assertEquals("ab", writeText("a\u0007b"));
    }

    @Test
    void writeText_allControlCharactersHandledConsistently() throws IOException {
        // Comprehensive sweep over all 32 C0 control chars in both short and long inputs.
        // Preserved: \t (0x09), \n (0x0A), \f (0x0C), \r (0x0D). All others dropped.
        for (int i = 0; i < 32; i++) {
            String shortInput = "b" + (char) i + "b";
            StringWriter sw = new StringWriter();
            HtmlUtils.writeText(sw, false, false, shortInput, new char[1024], false);
            int expectedLen = (i == 9 || i == 10 || i == 12 || i == 13) ? 3 : 2;
            assertEquals(expectedLen, sw.toString().length(), "short input failed for control char " + i);

            String longInput = shortInput + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
            sw = new StringWriter();
            HtmlUtils.writeText(sw, false, false, longInput, new char[1024], false);
            int expectedLongLen = (i == 9 || i == 10 || i == 12 || i == 13) ? 34 : 33;
            assertEquals(expectedLongLen, sw.toString().length(), "long input failed for control char " + i);
        }
    }

    @Test
    void writeText_delAndC1ControlsPassThrough() throws IOException {
        // 0x7F (DEL) and 0x80-0x9F (C1 control range) are passed through as-is per the legacy
        // contract -- the slow-path branch `ch < 0xA0` catches them after the fast path bounded
        // ch < 0x7f.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, "a\u007fb\u0080c\u009fd".toCharArray(), false);
        assertEquals("a\u007fb\u0080c\u009fd", sw.toString());
    }

    @Test
    void writeText_forXmlDropsInvalidXmlChars() throws IOException {
        // With forXml=true and escapeUnicode=false, chars not valid in XML 1.0 are dropped.
        // U+FFFE and U+FFFF are not valid XML chars.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, false, false, "a\ufffeb\uffffc".toCharArray(), true);
        assertEquals("abc", sw.toString());
    }

    @Test
    void writeText_largeInputCorrectAcrossLegacyBufferBoundary() throws IOException {
        // Inputs larger than the legacy 1024-char buffer used to force mid-stream flushes in
        // the old code; the new range-emit collapses to a single bulk write. Verify correct
        // output for an input >2000 chars with periodic escape chars.
        StringBuilder sb = new StringBuilder(2200);
        StringBuilder expected = new StringBuilder(2800);
        for (int i = 0; i < 100; i++) {
            sb.append("abcdefghij<klmnopqrst>");
            expected.append("abcdefghij&lt;klmnopqrst&gt;");
        }
        assertEquals(expected.toString(), writeText(sb.toString()));
    }

    @Test
    void writeText_iso8859ChrPassesThroughWhenEscapeIsocodeFalse() throws IOException {
        // U+00E9 = é; with escapeIsocode=false the char is written as-is.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, false, "café".toCharArray(), false);
        assertEquals("café", sw.toString());
    }

    @Test
    void writeText_iso8859ChrEntityWhenEscapeIsocodeTrue() throws IOException {
        // U+00E9 = é → &eacute; when escapeIsocode=true.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, "café".toCharArray(), false);
        assertEquals("caf&eacute;", sw.toString());
    }

    @Test
    void writeText_unicodeChrEntityWhenEscapeUnicodeTrue() throws IOException {
        // U+3042 = あ → &#12354; when escapeUnicode=true.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, "char あ".toCharArray(), false);
        assertEquals("char &#12354;", sw.toString());
    }

    @Test
    void writeText_euroEmittedAsNumericReference() throws IOException {
        // U+20AC = €: emitted as the universal numeric reference &#8364; (valid in HTML 4,
        // HTML5, XHTML and XML). Legacy code emitted &euro; -- correct in HTML but invalid in
        // XHTML/XML without DTD. Reachable only when escapeUnicode=true (non-UTF output
        // encoding); modern HTML5+UTF-8 setups write the raw 3-byte UTF-8 sequence.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, "price €".toCharArray(), false);
        assertEquals("price &#8364;", sw.toString());
    }

    /**
     * A character above U+FFFF is one surrogate pair in Java, and one numeric character reference in HTML and XML. A
     * reference to a lone surrogate is no valid character.
     */
    @Test
    void writeText_supplementaryCharacterIsOneNumericReference() throws IOException {
        assertEquals("a&#128512;b", writeText("a😀b"));
        assertEquals("a&#128512;b", writeTextString("a😀b"));
        assertEquals("a&#128512;b", writeAttribute("a😀b"));
    }

    @Test
    void writeText_surrogatePairSplitByTheEndOfTheSliceIsNoPair() throws IOException {
        StringWriter html = new StringWriter();
        HtmlUtils.writeText(html, true, true, "a😀".toCharArray(), 0, 2, false);
        assertEquals("a&#55357;", html.toString());

        StringWriter xml = new StringWriter();
        HtmlUtils.writeText(xml, true, true, "a😀".toCharArray(), 0, 2, true);
        assertEquals("a", xml.toString());
    }

    /**
     * XML does not allow a lone surrogate, U+FFFE and U+FFFF, not even as a character reference, so forXml drops them
     * whether or not escapeUnicode is set.
     */
    @Test
    void writeText_forXmlDropsDisallowedCharactersAlsoWhenEscapingUnicode() throws IOException {
        for (String disallowed : new String[] { "\uD83D", "\uDE00", "\uFFFE", "\uFFFF" }) {
            for (boolean escapeUnicode : new boolean[] { false, true }) {
                StringWriter text = new StringWriter();
                HtmlUtils.writeText(text, escapeUnicode, true, ("a" + disallowed + "b").toCharArray(), true);
                assertEquals("ab", text.toString(), "text escapeUnicode=" + escapeUnicode);

                StringWriter attribute = new StringWriter();
                HtmlUtils.writeAttribute(attribute, escapeUnicode, true, "a" + disallowed + "b", new char[16], true, true);
                assertEquals("ab", attribute.toString(), "attribute escapeUnicode=" + escapeUnicode);
            }
        }
    }

    @Test
    void writeText_unicodeChrPassesThroughWhenEscapeUnicodeFalse() throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, false, false, "char あ".toCharArray(), false);
        assertEquals("char あ", sw.toString());
    }

    @Test
    void writeText_stringEntryEmptySpecialCases() throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, "", TEXT_BUFFER, false);
        assertEquals("", sw.toString());
    }

    @Test
    void writeText_stringEntryLongMatchesCharArrayEntry() throws IOException {
        String input = "<div class=\"x\">The quick brown fox</div>";
        String fromString = writeTextString(input);
        String fromArray = writeText(input);
        assertEquals(fromString, fromArray);
        assertEquals("&lt;div class=\"x\"&gt;The quick brown fox&lt;/div&gt;", fromString);
    }

    // -------- writeAttribute -----------------------------------------------

    @Test
    void writeAttribute_plainAscii_passesUnchanged() throws IOException {
        assertEquals("form:input123", writeAttribute("form:input123"));
    }

    @Test
    void writeAttribute_escapesLt() throws IOException {
        assertEquals("a &lt; b", writeAttribute("a < b"));
    }

    @Test
    void writeAttribute_escapesGt() throws IOException {
        assertEquals("a &gt; b", writeAttribute("a > b"));
    }

    @Test
    void writeAttribute_escapesAmp() throws IOException {
        assertEquals("a &amp; b", writeAttribute("a & b"));
    }

    @Test
    void writeAttribute_escapesQuoteToQuot() throws IOException {
        // writeAttribute, unlike writeText, escapes double quotes to &quot;.
        assertEquals("&quot;x&quot;", writeAttribute("\"x\""));
    }

    @Test
    void writeAttribute_ampersandFollowedByBraceIsNotEscaped() throws IOException {
        // HTML 4.0 section B.7.1: '&{' is the start of a JS object literal; don't escape the '&'.
        assertEquals("&{js: true}", writeAttribute("&{js: true}"));
    }

    @Test
    void writeAttribute_longStringWithGetCharsPath() throws IOException {
        // String entry path takes the >= 16 char branch.
        String input = "The quick brown fox jumps over the lazy dog";
        assertEquals(input, writeAttribute(input));
    }

    @Test
    void writeAttribute_longStringWithEscapes() throws IOException {
        String input = "background: url(\"x.png\"); color: <red>";
        assertEquals("background: url(&quot;x.png&quot;); color: &lt;red&gt;", writeAttribute(input));
    }

    @Test
    void writeAttribute_scriptInValueDroppedWhenDisabled() throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeAttribute(sw, true, true, "javascript:alert(1)", new char[1024], false, false);
        assertEquals("", sw.toString());
    }

    @Test
    void writeAttribute_scriptInValueKeptWhenEnabled() throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeAttribute(sw, true, true, "javascript:alert(1)", new char[1024], true, false);
        assertEquals("javascript:alert(1)", sw.toString());
    }

    /**
     * A browser ignores leading whitespace and control characters, ignores tabs and line breaks anywhere in the scheme,
     * and matches the scheme case-insensitively, so each of these runs script.
     */
    @Test
    void writeAttribute_scriptSchemeDroppedWhateverItsCaseAndWhitespace() throws IOException {
        for (String value : new String[] { "JAVASCRIPT:alert(1)", "JavaScript:alert(1)", " javascript:alert(1)", "\u0001javascript:alert(1)",
                "java\tscr\nipt:alert(1)", "VBScript:msgbox(1)", "java\u0000script:alert(1)",
                "java\u0001script:alert(1)", "javascr\u000Bipt:alert(1)" }) {
            assertEquals("", writeAttributeWithScriptsDisabled(value), value);
        }
    }

    /**
     * forXml drops more characters than HTML, so a scheme split by one of those runs script too.
     */
    @Test
    void writeAttribute_scriptSchemeSplitByCharacterWhichForXmlDropsIsDropped() throws IOException {
        for (String value : new String[] { "\uFFFEjavascript:alert(1)", "java\uFFFFscript:alert(1)", "java\uD800script:alert(1)",
                "java\u000Cscript:alert(1)" }) {
            StringWriter sw = new StringWriter();
            HtmlUtils.writeAttribute(sw, false, false, value, new char[1024], false, true);
            assertEquals("", sw.toString(), value);
        }
    }

    /**
     * Only a script scheme at the start of the value runs script, so "script:" anywhere else is written.
     */
    @Test
    void writeAttribute_scriptOutsideTheSchemeIsKept() throws IOException {
        assertEquals("Transcript: page 1", writeAttributeWithScriptsDisabled("Transcript: page 1"));
        assertEquals("noscript:x", writeAttributeWithScriptsDisabled("noscript:x"));
        assertEquals("see javascript:alert(1)", writeAttributeWithScriptsDisabled("see javascript:alert(1)"));
        assertEquals("java script:alert(1)", writeAttributeWithScriptsDisabled("java script:alert(1)"));
        assertEquals("javascripts:alert(1)", writeAttributeWithScriptsDisabled("javascripts:alert(1)"));
        assertEquals("&lt;script:alert(1)", writeAttributeWithScriptsDisabled("<script:alert(1)"));
    }

    @Test
    void writeAttribute_iso8859Entity() throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeAttribute(sw, true, true, "café", new char[1024], true, false);
        assertEquals("caf&eacute;", sw.toString());
    }

    @Test
    void writeAttribute_unicodeEntity() throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeAttribute(sw, true, true, "char あ", new char[1024], true, false);
        assertEquals("char &#12354;", sw.toString());
    }

    @Test
    void writeAttribute_stringEntryLongMatchesCharArrayEntry() throws IOException {
        // The String overload routes >=16-char inputs through getChars + char[] path. Verify both
        // paths produce identical output for the same input.
        String input = "url(\"path/with/<special>&chars\")";
        StringWriter sw1 = new StringWriter();
        HtmlUtils.writeAttribute(sw1, true, true, input, new char[1024], false, false);
        StringWriter sw2 = new StringWriter();
        HtmlUtils.writeAttribute(sw2, true, true, input.toCharArray(), 0, input.length(), false, false);
        assertEquals(sw1.toString(), sw2.toString());
        assertEquals("url(&quot;path/with/&lt;special&gt;&amp;chars&quot;)", sw1.toString());
    }

    @Test
    void writeAttribute_allControlCharactersHandledConsistently() throws IOException {
        // Mirror of writeText_allControlCharactersHandledConsistently for writeAttribute.
        for (int i = 0; i < 32; i++) {
            String shortInput = "b" + (char) i + "b";
            StringWriter sw = new StringWriter();
            HtmlUtils.writeAttribute(sw, false, false, shortInput, new char[1024], false, false);
            int expectedLen = (i == 9 || i == 10 || i == 12 || i == 13) ? 3 : 2;
            assertEquals(expectedLen, sw.toString().length(), "short input failed for control char " + i);

            String longInput = shortInput + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
            sw = new StringWriter();
            HtmlUtils.writeAttribute(sw, false, false, longInput, new char[1024], false, false);
            int expectedLongLen = (i == 9 || i == 10 || i == 12 || i == 13) ? 34 : 33;
            assertEquals(expectedLongLen, sw.toString().length(), "long input failed for control char " + i);
        }
    }

    // -------- writeURL -----------------------------------------------------

    @Test
    void writeURL_plainAsciiPath() throws IOException {
        assertEquals("/app/path", writeURL("/app/path"));
    }

    @Test
    void writeURL_pathWithQuery() throws IOException {
        assertEquals("/x?y=z", writeURL("/x?y=z"));
    }

    @Test
    void writeURL_queryAmpersandIsEscapedToEntity() throws IOException {
        // Within the query string, '&' is HTML-entity escaped (writes via the same response that
        // outputs HTML attribute values), not URL-percent-encoded.
        assertEquals("/x?a=1&amp;b=2", writeURL("/x?a=1&b=2"));
    }

    @Test
    void writeURL_queryAmpersandAlreadyEscapedNotDoubled() throws IOException {
        // If the input already contains '&amp;', leave it alone -- don't double-escape.
        assertEquals("/x?a=1&amp;b=2", writeURL("/x?a=1&amp;b=2"));
    }

    @Test
    void writeURL_fragment() throws IOException {
        assertEquals("/x?a=1#frag", writeURL("/x?a=1#frag"));
    }

    @Test
    void writeURL_doubleQuotePercentEncoded() throws IOException {
        assertEquals("/x%22y", writeURL("/x\"y"));
    }

    @Test
    void writeURL_utf8NonAsciiPercentEncoded() throws IOException {
        // U+00E9 (é) = UTF-8 0xC3 0xA9 → %C3%A9
        assertEquals("/caf%C3%A9", writeURL("/café"));
    }

    @Test
    void writeURL_longPathRoutesThroughCharArrayVariant() throws IOException {
        // Inputs >= 16 chars route through the char[] variant via text.getChars.
        String input = "/long/app/path/with/many/segments";
        assertEquals(input, writeURL(input));
    }

    @Test
    void writeURL_spaceInShortPathPercentEncoded() throws IOException {
        // Both short (<16) and long (>=16) inputs percent-encode space as %20.
        // (Legacy <16 char-path emitted '+' for space -- inconsistent with the >=16 path,
        // now unified on the RFC 3986-correct %20.)
        assertEquals("/a%20b", writeURL("/a b"));
        assertEquals("/with%20space/path/segment", writeURL("/with space/path/segment"));
    }

    @Test
    void writeURL_truncatedAmpAtEndDoesNotCrash() throws IOException {
        // Trailing "&am" or "&amp" (no closing ';') near end-of-input would have run off the end
        // of the buffer in the legacy isAmpEscaped because the caller-side guard only verified
        // the byte AFTER the '&' was in bounds. The new isAmpEscaped does its own bounds check
        // and falls back to entity-escaping the bare '&'.
        assertEquals("/x?a=1&amp;am", writeURL("/x?a=1&am"));
        assertEquals("/x?a=1&amp;amp", writeURL("/x?a=1&amp"));
    }

    @Test
    void writeURL_questionMarkAtStartOfInput() throws IOException {
        // '?' as the very first character: exercises the empty-safe-run branch (i == runStart).
        assertEquals("?a=1&amp;b=2", writeURL("?a=1&b=2"));
    }

    @Test
    void writeURL_textBuffSmallerThanInputStillWorks() throws IOException {
        // The String entry allocates a fresh char[] when the caller's textBuff is too small to
        // hold the input. Locks in the fallback path.
        StringWriter sw = new StringWriter();
        HtmlUtils.writeURL(sw, "/this/is/a/long/enough/path?a=1&b=2", new char[1], "UTF-8");
        assertEquals("/this/is/a/long/enough/path?a=1&amp;b=2", sw.toString());
    }

    @Test
    void writeURL_charArrayEntryDirect() throws IOException {
        // Verify the char[] entry produces the same output as the String entry for the same input.
        String input = "/x?a=1&b=2";
        StringWriter sw1 = new StringWriter();
        HtmlUtils.writeURL(sw1, input, new char[1024], "UTF-8");
        StringWriter sw2 = new StringWriter();
        HtmlUtils.writeURL(sw2, input.toCharArray(), 0, input.length(), "UTF-8");
        assertEquals(sw1.toString(), sw2.toString());
        assertEquals("/x?a=1&amp;b=2", sw1.toString());
    }

    @Test
    void writeURL_noParams() throws IOException {
        assertEquals("http://www.google.com", writeURL("http://www.google.com"));
    }

    @Test
    void writeURL_oneParam() throws IOException {
        assertEquals("http://www.google.com?joe=10", writeURL("http://www.google.com?joe=10"));
    }

    @Test
    void writeURL_twoParamsAmpersandEscaped() throws IOException {
        assertEquals("http://www.google.com?joe=10&amp;fred=20", writeURL("http://www.google.com?joe=10&fred=20"));
    }

    @Test
    void writeURL_paramAmpersandAlreadyEntityEscapedNotDoubled() throws IOException {
        assertEquals("/index.jsf?joe=10&amp;fred=20", writeURL("/index.jsf?joe=10&amp;fred=20"));
    }

    @Test
    void writeURL_paramAmpersandShortNameEscaped() throws IOException {
        assertEquals("/index.jsf?joe=10&amp;f=20", writeURL("/index.jsf?joe=10&f=20"));
    }

    @Test
    void writeURL_trailingAmpersand() throws IOException {
        // Misplaced trailing '&' -- preserved as escaped entity even at end-of-string.
        assertEquals("/index.jsf?joe=10&amp;f=20&amp;", writeURL("/index.jsf?joe=10&f=20&"));
    }

    @Test
    void writeURL_trailingEntityEscapedAmpersand() throws IOException {
        // Trailing '&' that's already entity-escaped collapses with following '&' -- not necessarily
        // semantically right, but documents current behavior.
        assertEquals("/index.jsf?joe=10&amp;f=20&amp;", writeURL("/index.jsf?joe=10&f=20&amp;"));
    }

    @Test
    void writeURL_nonAsciiInUriPathPercentEncoded() throws IOException {
        assertEquals(
                "/%CE%B5%CE%BB/%D7%99%D7%AA/%D0%BA%D0%B8/%D9%8A%D8%A9%D9%8F%E2%80%8E%E2%80%8E/%ED%95%9C%EA%B8%80/index.jsf",
                writeURL("/ελ/ית/ки/يةُ‎‎/한글/index.jsf"));
    }

    @Test
    void writeURL_nonAsciiInQueryStringPercentEncoded() throws IOException {
        assertEquals(
                "/index.jsf?greek=%CE%B5%CE%BB&amp;cyrillic=%D0%BA%D0%B8&amp;hebrew=%D7%99%D7%AA&amp;arabic=%D9%8A%D8%A9%D9%8F%E2%80%8E%E2%80%8E&amp;korean=%ED%95%9C%EA%B8%80",
                writeURL("/index.jsf?greek=ελ&cyrillic=ки&hebrew=ית&arabic=يةُ‎‎&korean=한글"));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5585
     */
    @Test
    void writeURL_questionMarkInFragmentIsPreserved() throws IOException {
        // RFC 3986 section 3.5: '?' inside a fragment is just a regular char (not a query start).
        assertEquals("https://server.com/sap?query#fragment?p=v",
                writeURL("https://server.com/sap?query#fragment?p=v"));
        assertEquals("https://server.com/sap?query=foo%3Fbar#fragment?p=v",
                writeURL("https://server.com/sap?query=foo?bar#fragment?p=v"));
    }

    // -------- Every character at every position ----------------------------

    /**
     * How {@code writeText} and {@code writeAttribute} write each character class, per flag. The output for a combination
     * of flags follows from the single flags: {@code escapeIsocode} writes a named entity for U+00A0 to U+00FF,
     * {@code escapeUnicode} a numeric reference from U+0100, and {@code forXml} drops what XML does not allow, whatever
     * the other flags.
     */
    private record Escape(String probe, String plain, String isocode, String unicode, String xml) {

        static Escape unchanged(String probe) {
            return new Escape(probe, probe, probe, probe, probe);
        }

        static Escape always(String probe, String escaped) {
            return new Escape(probe, escaped, escaped, escaped, escaped);
        }

        String expected(int flags) {
            if (forXml(flags) && xml.isEmpty()) {
                return xml;
            }
            if (escapeIsocode(flags) && !isocode.equals(plain)) {
                return isocode;
            }
            if (escapeUnicode(flags) && !unicode.equals(plain)) {
                return unicode;
            }
            return forXml(flags) ? xml : plain;
        }
    }

    private static final List<Escape> ESCAPES = List.of(
            Escape.always("<", "&lt;"),
            Escape.always(">", "&gt;"),
            Escape.always("&", "&amp;"),
            Escape.always("]]>", "]]&gt;"),
            Escape.unchanged("'"),
            Escape.unchanged("{"),
            Escape.unchanged("\t"),
            Escape.unchanged("\n"),
            Escape.unchanged("\r"),
            new Escape("\f", "\f", "\f", "\f", ""),
            Escape.always("\u0001", ""),
            Escape.always("\u001F", ""),
            Escape.unchanged("\u007F"),
            Escape.unchanged("\u0080"),
            Escape.unchanged("\u0085"),
            Escape.unchanged("\u009F"),
            new Escape("\u00A0", "\u00A0", "&nbsp;", "\u00A0", "\u00A0"),
            new Escape("é", "é", "&eacute;", "é", "é"),
            new Escape("ÿ", "ÿ", "&yuml;", "ÿ", "ÿ"),
            // The low byte of these two is a control character, which the SWAR scan flags.
            new Escape("\u0100", "\u0100", "\u0100", "&#256;", "\u0100"),
            new Escape("\u0109", "\u0109", "\u0109", "&#265;", "\u0109"),
            new Escape("€", "€", "€", "&#8364;", "€"),
            new Escape("あ", "あ", "あ", "&#12354;", "あ"),
            new Escape("😀", "😀", "😀", "&#128512;", "😀"),
            new Escape("\uD83D", "\uD83D", "\uD83D", "&#55357;", ""),
            new Escape("\uDE00", "\uDE00", "\uDE00", "&#56832;", ""),
            new Escape("\uFFFD", "\uFFFD", "\uFFFD", "&#65533;", "\uFFFD"),
            new Escape("\uFFFE", "\uFFFE", "\uFFFE", "&#65534;", ""),
            new Escape("\uFFFF", "\uFFFF", "\uFFFF", "&#65535;", ""),
            Escape.unchanged("script:"),
            Escape.unchanged("javascript:x"));

    private static final List<Escape> TEXT_ESCAPES = concat(ESCAPES, Escape.unchanged("\""), Escape.always("&{", "&amp;{"));

    private static final List<Escape> ATTRIBUTE_ESCAPES = concat(ESCAPES, Escape.always("\"", "&quot;"), Escape.unchanged("&{"));

    private static final int SWAR_WORD = 8;
    private static final int FLAG_COMBINATIONS = 8;

    /**
     * The short scan runs per char, the long scan from {@link HtmlUtils#INTRINSIC_SCAN_MIN_LENGTH} chars per word of
     * {@link #SWAR_WORD} chars, so these lengths put every probe at every offset of both.
     */
    private static final int[] SWEEP_LENGTHS = IntStream.concat(
            IntStream.rangeClosed(1, SWAR_WORD * 2),
            IntStream.rangeClosed(HtmlUtils.INTRINSIC_SCAN_MIN_LENGTH - SWAR_WORD, HtmlUtils.INTRINSIC_SCAN_MIN_LENGTH + SWAR_WORD))
            .toArray();

    @Test
    void writeText_everyCharacterAtEveryPosition() throws IOException {
        for (Escape escape : TEXT_ESCAPES) {
            for (int length : SWEEP_LENGTHS) {
                for (int pos = 0; pos < length; pos++) {
                    String before = "a".repeat(pos);
                    String after = "b".repeat(Math.max(0, length - pos - 1));
                    for (int flags = 0; flags < FLAG_COMBINATIONS; flags++) {
                        assertText(before + escape.probe() + after, before + escape.expected(flags) + after, flags);
                    }
                }
            }
        }
    }

    @Test
    void writeAttribute_everyCharacterAtEveryPosition() throws IOException {
        for (Escape escape : ATTRIBUTE_ESCAPES) {
            for (int length : SWEEP_LENGTHS) {
                for (int pos = 0; pos < length; pos++) {
                    String before = "a".repeat(pos);
                    String after = "b".repeat(Math.max(0, length - pos - 1));
                    for (int flags = 0; flags < FLAG_COMBINATIONS; flags++) {
                        assertAttribute(before + escape.probe() + after, before + escape.expected(flags) + after, flags);
                    }
                }
            }
        }
    }

    /**
     * Two characters in the same or adjacent SWAR words past {@link HtmlUtils#INTRINSIC_SCAN_MIN_LENGTH}, where the scan
     * must not skip the second after finding the first.
     */
    @Test
    void everyTwoCharactersAroundSwarWordBoundaries() throws IOException {
        String before = "x".repeat(HtmlUtils.INTRINSIC_SCAN_MIN_LENGTH);
        String after = "z".repeat(SWAR_WORD);
        for (Escape first : ESCAPES) {
            for (Escape second : ESCAPES) {
                for (int a = 0; a < SWAR_WORD; a++) {
                    for (int gap = 1; gap <= SWAR_WORD; gap++) {
                        String lead = before + "w".repeat(a);
                        String between = "y".repeat(gap);
                        for (int flags : new int[] { 0, FLAG_COMBINATIONS - 1 }) {
                            String expected = lead + first.expected(flags) + between + second.expected(flags) + after;
                            String text = lead + first.probe() + between + second.probe() + after;
                            assertText(text, expected, flags);
                            assertAttribute(text, expected, flags);
                        }
                    }
                }
            }
        }
    }

    /**
     * With scripts in attribute values disabled, "javascript:" at the start of the value drops all of it, and anywhere
     * else is written.
     */
    @Test
    void writeAttribute_scriptAtEveryPositionDropsOnlyASchemeAtTheStart() throws IOException {
        for (int length : SWEEP_LENGTHS) {
            for (int pos = 0; pos < length; pos++) {
                String text = "a".repeat(pos) + "javascript:x" + "b".repeat(Math.max(0, length - pos - 1));
                StringWriter writer = new StringWriter();
                HtmlUtils.writeAttribute(writer, false, false, text, new char[SWAR_WORD], false, false);
                assertEquals(pos == 0 ? "" : text, writer.toString(), text);
            }
        }
    }

    private static void assertText(String text, String expected, int flags) throws IOException {
        String context = describe(text, flags);

        StringWriter fromString = new StringWriter();
        HtmlUtils.writeText(fromString, escapeUnicode(flags), escapeIsocode(flags), text, new char[SWAR_WORD], forXml(flags));
        assertEquals(expected, fromString.toString(), context);

        StringWriter fromChars = new StringWriter();
        HtmlUtils.writeText(fromChars, escapeUnicode(flags), escapeIsocode(flags), ("<>" + text + "<>").toCharArray(), 2, text.length(), forXml(flags));
        assertEquals(expected, fromChars.toString(), "char[] " + context);
    }

    private static void assertAttribute(String text, String expected, int flags) throws IOException {
        String context = describe(text, flags);

        StringWriter fromString = new StringWriter();
        HtmlUtils.writeAttribute(fromString, escapeUnicode(flags), escapeIsocode(flags), text, new char[SWAR_WORD], true, forXml(flags));
        assertEquals(expected, fromString.toString(), context);

        StringWriter fromChars = new StringWriter();
        HtmlUtils.writeAttribute(fromChars, escapeUnicode(flags), escapeIsocode(flags), ("<>" + text + "<>").toCharArray(), 2, text.length(), true,
                forXml(flags));
        assertEquals(expected, fromChars.toString(), "char[] " + context);
    }

    private static boolean escapeUnicode(int flags) {
        return (flags & 1) != 0;
    }

    private static boolean escapeIsocode(int flags) {
        return (flags & 2) != 0;
    }

    private static boolean forXml(int flags) {
        return (flags & 4) != 0;
    }

    private static List<Escape> concat(List<Escape> escapes, Escape... more) {
        List<Escape> all = new ArrayList<>(escapes);
        all.addAll(List.of(more));
        return List.copyOf(all);
    }

    private static String describe(String text, int flags) {
        StringBuilder codes = new StringBuilder();
        for (char c : text.toCharArray()) {
            codes.append(c >= 0x20 && c < 0x7F ? String.valueOf(c) : String.format("\\u%04X", (int) c));
        }
        return "escapeUnicode=" + escapeUnicode(flags) + " escapeIsocode=" + escapeIsocode(flags) + " forXml=" + forXml(flags) + " text=" + codes;
    }

    // -------- XML mode variants --------------------------------------------

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/4516
     * https://github.com/eclipse-ee4j/mojarra/issues/5464
     */
    @Test
    void testAllowedXmlCharacter() {
        for (char c = 0x0000; c <= 0x0008; c++) {
            assertFalse(isAllowedXmlCharacter(c));
        }

        assertTrue(isAllowedXmlCharacter((char) 0x0009));
        assertTrue(isAllowedXmlCharacter((char) 0x000A));

        assertFalse(isAllowedXmlCharacter((char) 0x000B));
        assertFalse(isAllowedXmlCharacter((char) 0x000C));

        assertTrue(isAllowedXmlCharacter((char) 0x000D));

        for (char c = 0x000E; c <= 0x0019; c++) {
            assertFalse(isAllowedXmlCharacter(c));
        }

        for (char c = 0x0020; c <= 0xD7FF; c++) {
            assertTrue(isAllowedXmlCharacter(c));
        }

        for (char c = 0xD800; c <= 0xDFFF; c++) {
            assertFalse(isAllowedXmlCharacter(c));
        }

        for (char c = 0xE000; c <= 0xFFFD; c++) {
            assertTrue(isAllowedXmlCharacter(c));
        }

        assertFalse(isAllowedXmlCharacter((char) 0xFFFE));
        assertFalse(isAllowedXmlCharacter((char) 0xFFFF));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5562
     */
    @Test
    void testAllowedSurrogateChars() {
        for (char low1 = 0xDC00; low1 <= 0xDFFF; low1++) {
            assertEquals("", writeUnescapedTextForXML(new String(new char[] { low1 })));

            for (char high2 = 0xD800; high2 <= 0xDBFF; high2++) {
                assertEquals("", writeUnescapedTextForXML(new String(new char[] { low1, high2 })));
            }
            for (char low2 = 0xDC00; low2 <= 0xDFFF; low2++) {
                assertEquals("", writeUnescapedTextForXML(new String(new char[] { low1, low2 })));
            }
        }

        for (char high1 = 0xD800; high1 <= 0xDBFF; high1++) {
            assertEquals("", writeUnescapedTextForXML(new String(new char[] { high1 })));

            for (char high2 = 0xD800; high2 <= 0xDBFF; high2++) {
                assertEquals("", writeUnescapedTextForXML(new String(new char[] { high1, high2 })));
            }
            for (char low2 = 0xDC00; low2 <= 0xDFFF; low2++) {
                assertNotEquals("", writeUnescapedTextForXML(new String(new char[] { high1, low2 }))); // This is the only combination which is allowed.
            }
        }
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5562
     */
    @Test
    void testWriteEmojisAsUnescapedTextForXML() {
        assertEquals("𝄞", writeUnescapedTextForXML("𝄞"));
        assertEquals("𝄞", writeUnescapedTextForXML(new String(toChars(0x1D11E))));
        assertEquals("😎", writeUnescapedTextForXML("😎"));
        assertEquals("😎", writeUnescapedTextForXML(new String(toChars(0x1F60E))));
        assertEquals("🎉", writeUnescapedTextForXML("🎉"));
        assertEquals("🎉", writeUnescapedTextForXML(new String(toChars(0x1F389))));
        assertEquals("🚀", writeUnescapedTextForXML("🚀"));
        assertEquals("🚀", writeUnescapedTextForXML(new String(toChars(0x1F680))));
        assertEquals("🐻", writeUnescapedTextForXML("🐻"));
        assertEquals("🐻", writeUnescapedTextForXML(new String(toChars(0x1F43B))));
        assertEquals("🐻‍❄️", writeUnescapedTextForXML("🐻‍❄️"));
        assertEquals("🐻‍❄️", writeUnescapedTextForXML(new String(new int[] { 0x1F43B, 0x200D, 0x2744, 0xFE0F }, 0, 4)));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5562
     */
    @Test
    void testWriteEmojisAsTextForXML() {
        assertEquals("&#119070;", writeTextForXML("𝄞"));
        assertEquals("&#128526;", writeTextForXML("😎"));
        assertEquals("&#127881;", writeTextForXML("🎉"));
        assertEquals("&#128640;", writeTextForXML("🚀"));
        assertEquals("&#128059;", writeTextForXML("🐻"));
        assertEquals("&#128059;&#8205;&#10052;&#65039;", writeTextForXML("🐻‍❄️"));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5562
     */
    @Test
    void testWriteEmojisAsAttributeForXML() {
        assertEquals("&#119070;", writeAttributeForXML("𝄞"));
        assertEquals("&#128526;", writeAttributeForXML("😎"));
        assertEquals("&#127881;", writeAttributeForXML("🎉"));
        assertEquals("&#128640;", writeAttributeForXML("🚀"));
        assertEquals("&#128059;", writeAttributeForXML("🐻"));
        assertEquals("&#128059;&#8205;&#10052;&#65039;", writeAttributeForXML("🐻‍❄️"));
    }

    // -------- Encoding checks -----------------------------------------------

    @Test
    void isUTFencoding_recognizesEveryUtfCharset() {
        assertTrue(HtmlUtils.isUTFencoding("UTF-8"));
        assertTrue(HtmlUtils.isUTFencoding("UTF-16"));
        assertTrue(HtmlUtils.isUTFencoding("UTF-16BE"));
        assertTrue(HtmlUtils.isUTFencoding("UTF-16LE"));
        assertTrue(HtmlUtils.isUTFencoding("UTF-32"));
        assertTrue(HtmlUtils.isUTFencoding("UTF-32BE"));
        assertTrue(HtmlUtils.isUTFencoding("UTF-32LE"));
    }

    /**
     * An absent encoding is treated as UTF, so unicode is not escaped by default.
     */
    @Test
    void isUTFencoding_emptyStringCountsAsUtf() {
        assertTrue(HtmlUtils.isUTFencoding(""));
    }

    /**
     * A null or unrecognized encoding is not a UTF encoding.
     */
    @Test
    void isUTFencoding_nullAndNonUtfAreFalse() {
        assertFalse(HtmlUtils.isUTFencoding(null));
        assertFalse(HtmlUtils.isUTFencoding("ISO-8859-1"));
        assertFalse(HtmlUtils.isUTFencoding("US-ASCII"));
        assertFalse(HtmlUtils.isUTFencoding("windows-1252"));
    }

    /**
     * HtmlResponseWriter uppercases the encoding before asking, so the BOM aliases must be
     * recognized in upper case as well as in the mixed case the JDK canonically uses.
     */
    @Test
    void isUTFencoding_bomAliasesMatchInBothCases() {
        assertTrue(HtmlUtils.isUTFencoding("x-UTF-16LE-BOM"));
        assertTrue(HtmlUtils.isUTFencoding("X-UTF-16LE-BOM"));
        assertTrue(HtmlUtils.isUTFencoding("X-UTF-32BE-BOM"));
        assertTrue(HtmlUtils.isUTFencoding("X-UTF-32LE-BOM"));
    }

    @Test
    void isISO8859_1encoding_matchesOnlyTheCanonicalName() {
        assertTrue(HtmlUtils.isISO8859_1encoding("ISO-8859-1"));
        assertFalse(HtmlUtils.isISO8859_1encoding(null));
        assertFalse(HtmlUtils.isISO8859_1encoding("iso-8859-1"));
        assertFalse(HtmlUtils.isISO8859_1encoding("latin1"));
        assertFalse(HtmlUtils.isISO8859_1encoding("UTF-8"));
    }

    // -------- Helpers -------------------------------------------------------

    private static String writeText(String input) throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, input.toCharArray(), false);
        return sw.toString();
    }

    private static String writeTextString(String input) throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeText(sw, true, true, input, TEXT_BUFFER, false);
        return sw.toString();
    }

    private static String writeAttribute(String input) throws IOException {
        // isScriptInAttributeValueEnabled=true to disable the script:-detection abort logic
        // for general-purpose tests (separate tests cover that special case explicitly).
        StringWriter sw = new StringWriter();
        HtmlUtils.writeAttribute(sw, true, true, input, new char[1024], true, false);
        return sw.toString();
    }

    private static String writeAttributeWithScriptsDisabled(String input) throws IOException {
        StringWriter sw = new StringWriter();
        HtmlUtils.writeAttribute(sw, false, false, input, new char[1024], false, false);
        return sw.toString();
    }

    private static String writeURL(String input) {
        return write(output -> HtmlUtils.writeURL(output, input, new char[input.length()], UTF_8.name()));
    }

    private static String writeUnescapedTextForXML(String string) {
        return write(output -> HtmlUtils.writeUnescapedTextForXML(output, string));
    }

    private static String writeTextForXML(String string) {
        return write(output -> HtmlUtils.writeTextForXML(output, string));
    }

    private static String writeAttributeForXML(String string) {
        return write(output -> HtmlUtils.writeAttribute(output, true, true, string, new char[1024], false, true));
    }

    private static String write(ThrowingConsumer<Writer> output) {
        StringWriter writer = new StringWriter();

        try {
            output.accept(writer);
        }
        catch (Throwable e) {
            fail(e);
        }

        return writer.toString();
    }
}
