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
package com.sun.faces.application.resource;

import static com.sun.faces.application.resource.ResourceHandlerImpl.isExcluded;
import static com.sun.faces.application.resource.ResourceHandlerImpl.parseExcludedExtensions;
import static jakarta.faces.application.ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests the parsing and matching of the {@link jakarta.faces.application.ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME}
 * init parameter, which is a space separated list of plain file extension suffixes.
 */
class ResourceHandlerImplExcludesTest {

    private final Map<String, Object> appMap = new HashMap<>();

    private String[] parse(String excludesParam) {
        return parseExcludedExtensions(appMap, excludesParam);
    }

    // --- parseExcludedExtensions ---

    @Test
    void parsesDefaultValue() {
        assertArrayEquals(new String[] { ".class", ".jsp", ".jspx", ".properties", ".xhtml", ".groovy" },
                parse(RESOURCE_EXCLUDES_DEFAULT_VALUE));
    }

    /**
     * A leading, trailing or repeated space must not yield an empty suffix, as that would match every resource id and
     * thus exclude the whole application.
     */
    @Test
    void skipsEmptyTokens() {
        assertArrayEquals(new String[] { ".class", ".xhtml" }, parse("  .class   .xhtml  "));
        assertArrayEquals(new String[0], parse(""));
        assertArrayEquals(new String[0], parse("   "));
    }

    // --- isExcluded ---

    @Test
    void excludesConfiguredSuffixes() {
        String[] excludes = parse(RESOURCE_EXCLUDES_DEFAULT_VALUE);

        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/foo.xhtml"));
        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/com/example/Foo.class"));
        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/messages.properties"));
        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/foo.groovy"));
    }

    @Test
    void doesNotExcludeOtherSuffixes() {
        String[] excludes = parse(RESOURCE_EXCLUDES_DEFAULT_VALUE);

        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/foo.js"));
        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/foo.css"));
        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/foo.png"));
    }

    /**
     * The suffix must be matched at the end only, not anywhere in the resource id.
     */
    @Test
    void doesNotExcludeSuffixOccurringAsInfix() {
        String[] excludes = parse(RESOURCE_EXCLUDES_DEFAULT_VALUE);

        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/foo.xhtml.js"));
        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/.class/foo.png"));
    }

    /**
     * A line terminator in the resource id must not let it escape the exclusion.
     */
    @Test
    void excludesResourceIdContainingLineTerminator() {
        String[] excludes = parse(RESOURCE_EXCLUDES_DEFAULT_VALUE);

        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/foo\nbar.class"));
        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/foo\r\nbar.properties"));
    }

    @Test
    void excludesNothingWhenNoneConfigured() {
        String[] excludes = parse("");

        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/foo.xhtml"));
        assertFalse(isExcluded(excludes, ""));
    }

    @Test
    void honoursCustomExcludes() {
        String[] excludes = parse(".md .txt");

        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/README.md"));
        assertTrue(isExcluded(excludes, "/jakarta.faces.resource/notes.txt"));
        assertFalse(isExcluded(excludes, "/jakarta.faces.resource/foo.xhtml"));
    }
}
