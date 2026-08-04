/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package com.sun.faces.context;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * Tests for {@link UrlBuilder}.
 *
 * <p>Covers how a seed URL is split into path, query string and fragment, how each segment is normalized (trimmed,
 * leading separator stripped, empty reduced to absent), and how the segments are reassembled by {@code createUrl}
 * together with any added parameters.
 */
class UrlBuilderTest {

    @BeforeEach
    void setCurrentFacesContext() {
        FacesContext facesContext = mock(FacesContext.class);
        when(facesContext.getExternalContext()).thenReturn(mock(ExternalContext.class));
        CurrentFacesContext.set(facesContext);
    }

    @AfterEach
    void clearCurrentFacesContext() {
        CurrentFacesContext.set(null);
    }

    // -------- seed URL parsing ----------------------------------------------

    @Test
    void seedUrl_pathOnly() {
        assertEquals("https://example.com/page", buildUrl("https://example.com/page"));
    }

    @Test
    void seedUrl_queryString() {
        assertEquals("https://example.com/page?foo=bar", buildUrl("https://example.com/page?foo=bar"));
        assertEquals("https://example.com/page?foo=bar&baz=bak", buildUrl("https://example.com/page?foo=bar&baz=bak"));
    }

    @Test
    void seedUrl_fragment() {
        assertEquals("https://example.com/page#foo", buildUrl("https://example.com/page#foo"));
    }

    @Test
    void seedUrl_queryStringAndFragment() {
        assertEquals("https://example.com/page?foo=bar#baz", buildUrl("https://example.com/page?foo=bar#baz"));
    }

    /**
     * The fragment is split off before the query string, so a question mark inside the fragment stays part of the
     * fragment and does not become a query string of its own.
     */
    @Test
    void seedUrl_questionMarkInsideFragmentIsNotAQueryString() {
        assertEquals("https://example.com#?foo=bar", buildUrl("https://example.com#?foo=bar"));
        assertEquals("https://example.com?foo=bar#?baz=bak", buildUrl("https://example.com?foo=bar#?baz=bak"));
        assertEquals("https://example.com#/foo/bar?baz=bak&ban=bar", buildUrl("https://example.com#/foo/bar?baz=bak&ban=bar"));
    }

    @Test
    void seedUrl_emptyOrBlankIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new UrlBuilder(null, UTF_8.name()));
        assertThrows(IllegalArgumentException.class, () -> new UrlBuilder("", UTF_8.name()));
        assertThrows(IllegalArgumentException.class, () -> new UrlBuilder("   ", UTF_8.name()));
    }

    // -------- empty segments (issue 5904) -----------------------------------

    /**
     * A seed URL ending in a bare hash mark leaves an empty fragment behind, which must be dropped rather than throw a
     * {@link StringIndexOutOfBoundsException} while stripping the leading hash mark.
     */
    @Test
    void seedUrl_trailingFragmentSeparatorIsDropped() {
        assertEquals("https://example.com/", buildUrl("https://example.com/#"));
        assertEquals("https://example.com/page", buildUrl("https://example.com/page#"));
    }

    /**
     * A seed URL ending in a bare question mark leaves an empty query string behind, which must be dropped rather than
     * throw a {@link StringIndexOutOfBoundsException} while stripping the leading question mark.
     */
    @Test
    void seedUrl_trailingQueryStringSeparatorIsDropped() {
        assertEquals("https://example.com/", buildUrl("https://example.com/?"));
        assertEquals("https://example.com/page", buildUrl("https://example.com/page?"));
    }

    @Test
    void seedUrl_trailingQueryStringAndFragmentSeparatorAreDropped() {
        assertEquals("https://example.com/page", buildUrl("https://example.com/page?#"));
    }

    @Test
    void seedUrl_blankSegmentsAreDropped() {
        assertEquals("https://example.com/page", buildUrl("https://example.com/page#   "));
        assertEquals("https://example.com/page", buildUrl("https://example.com/page?   "));
    }

    /**
     * Only one leading hash mark is stripped, so a doubled one at the end of the seed URL reduces to an empty fragment
     * and is dropped altogether.
     */
    @Test
    void seedUrl_doubledTrailingFragmentSeparatorIsDropped() {
        assertEquals("https://example.com/page", buildUrl("https://example.com/page##"));
    }

    @Test
    void seedUrl_doubledTrailingQueryStringSeparatorIsDropped() {
        assertEquals("https://example.com/page", buildUrl("https://example.com/page??"));
    }

    /**
     * Only one leading separator is stripped from an extracted segment, so anything beyond the first one is content.
     */
    @Test
    void seedUrl_onlyOneLeadingSeparatorIsStripped() {
        assertEquals("https://example.com/page##", buildUrl("https://example.com/page###"));
        assertEquals("https://example.com/page??", buildUrl("https://example.com/page???"));
        assertEquals("https://example.com/page#top", buildUrl("https://example.com/page##top"));
        assertEquals("https://example.com/page?a=1", buildUrl("https://example.com/page??a=1"));
    }

    /**
     * Reproduces the reported failure in its original shape: {@code ExternalContext#encodeRedirectURL} on a base URL
     * that ends in a bare hash mark.
     */
    @Test
    void seedUrl_trailingFragmentSeparatorWithParameters() {
        UrlBuilder builder = new UrlBuilder("https://example.com/#", UTF_8.name());
        builder.addParameters(parameters("foo", "bar"));
        assertEquals("https://example.com/?foo=bar", builder.createUrl());
    }

    // -------- setFragment ---------------------------------------------------

    @Test
    void setFragment_isAppendedAfterHashMark() {
        assertEquals("https://example.com#foo", buildUrl("https://example.com", builder -> builder.setFragment("foo")));
    }

    @Test
    void setFragment_leadingHashMarkIsStrippedOnce() {
        assertEquals("https://example.com#foo", buildUrl("https://example.com", builder -> builder.setFragment("#foo")));
        assertEquals("https://example.com##foo", buildUrl("https://example.com", builder -> builder.setFragment("##foo")));
    }

    @Test
    void setFragment_isTrimmed() {
        assertEquals("https://example.com#foo", buildUrl("https://example.com", builder -> builder.setFragment("  foo  ")));
        assertEquals("https://example.com#foo", buildUrl("https://example.com", builder -> builder.setFragment("  #foo  ")));
    }

    @Test
    void setFragment_emptyOrBlankOrNullIsDropped() {
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setFragment(null)));
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setFragment("")));
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setFragment("   ")));
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setFragment("#")));
    }

    @Test
    void setFragment_replacesTheSeedFragment() {
        assertEquals("https://example.com#bar", buildUrl("https://example.com#foo", builder -> builder.setFragment("bar")));
        assertEquals("https://example.com", buildUrl("https://example.com#foo", builder -> builder.setFragment("")));
    }

    // -------- setQueryString ------------------------------------------------

    @Test
    void setQueryString_isAppendedAfterQuestionMark() {
        assertEquals("https://example.com?foo=bar", buildUrl("https://example.com", builder -> builder.setQueryString("foo=bar")));
    }

    @Test
    void setQueryString_leadingQuestionMarkIsStrippedOnce() {
        assertEquals("https://example.com?foo=bar", buildUrl("https://example.com", builder -> builder.setQueryString("?foo=bar")));
        assertEquals("https://example.com??foo=bar", buildUrl("https://example.com", builder -> builder.setQueryString("??foo=bar")));
    }

    @Test
    void setQueryString_isTrimmed() {
        assertEquals("https://example.com?foo=bar", buildUrl("https://example.com", builder -> builder.setQueryString("  foo=bar  ")));
        assertEquals("https://example.com?foo=bar", buildUrl("https://example.com", builder -> builder.setQueryString("  ?foo=bar  ")));
    }

    @Test
    void setQueryString_emptyOrBlankOrNullIsDropped() {
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setQueryString(null)));
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setQueryString("")));
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setQueryString("   ")));
        assertEquals("https://example.com", buildUrl("https://example.com", builder -> builder.setQueryString("?")));
    }

    @Test
    void setQueryString_replacesTheSeedQueryString() {
        assertEquals("https://example.com?baz=bak", buildUrl("https://example.com?foo=bar", builder -> builder.setQueryString("baz=bak")));
        assertEquals("https://example.com", buildUrl("https://example.com?foo=bar", builder -> builder.setQueryString("")));
    }

    // -------- parameters ----------------------------------------------------

    @Test
    void addParameters_startsTheQueryString() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page", UTF_8.name());
        builder.addParameters(parameters("foo", "bar"));
        assertEquals("https://example.com/page?foo=bar", builder.createUrl());
    }

    @Test
    void addParameters_extendsTheSeedQueryString() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page?foo=bar", UTF_8.name());
        builder.addParameters(parameters("baz", "bak"));
        assertEquals("https://example.com/page?foo=bar&baz=bak", builder.createUrl());
    }

    @Test
    void addParameters_areAppendedBeforeTheFragment() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page#anchor", UTF_8.name());
        builder.addParameters(parameters("foo", "bar"));
        assertEquals("https://example.com/page?foo=bar#anchor", builder.createUrl());
    }

    @Test
    void addParameters_valuesAreEncoded() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page", UTF_8.name());
        builder.addParameters("foo", singletonList("?bar&baz=bak#anchor"));
        assertEquals("https://example.com/page?foo=%3Fbar%26baz%3Dbak%23anchor", builder.createUrl());
    }

    @Test
    void addParameters_multipleValuesAreRepeated() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page", UTF_8.name());
        builder.addParameters("foo", asList("bar", "baz"));
        assertEquals("https://example.com/page?foo=bar&foo=baz", builder.createUrl());
    }

    @Test
    void addParameters_nullValuesAreSkipped() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page", UTF_8.name());
        builder.addParameters("foo", asList("bar", null));
        assertEquals("https://example.com/page?foo=bar", builder.createUrl());
    }

    @Test
    void addParameters_emptyOrBlankNameIsRejected() {
        UrlBuilder builder = new UrlBuilder("https://example.com/page", UTF_8.name());
        assertThrows(IllegalArgumentException.class, () -> builder.addParameters(null, singletonList("bar")));
        assertThrows(IllegalArgumentException.class, () -> builder.addParameters("", singletonList("bar")));
        assertThrows(IllegalArgumentException.class, () -> builder.addParameters("   ", singletonList("bar")));
    }

    // -------- helpers -------------------------------------------------------

    private static String buildUrl(String url) {
        return new UrlBuilder(url, UTF_8.name()).createUrl();
    }

    private static String buildUrl(String url, Consumer<UrlBuilder> customizer) {
        UrlBuilder builder = new UrlBuilder(url, UTF_8.name());
        customizer.accept(builder);
        return builder.createUrl();
    }

    private static Map<String, List<String>> parameters(String name, String value) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put(name, singletonList(value));
        return parameters;
    }

    /**
     * Gives access to the protected {@code FacesContext#setCurrentInstance}, which is otherwise unreachable from a test
     * in another package.
     */
    private abstract static class CurrentFacesContext extends FacesContext {

        static void set(FacesContext facesContext) {
            setCurrentInstance(facesContext);
        }
    }
}
