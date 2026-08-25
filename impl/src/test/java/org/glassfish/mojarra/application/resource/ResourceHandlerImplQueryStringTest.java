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
package org.glassfish.mojarra.application.resource;

import static org.glassfish.mojarra.application.resource.ResourceHandlerImpl.getQueryString;
import static org.glassfish.mojarra.application.resource.ResourceHandlerImpl.removeQueryString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Tests that a resource name carrying a query string, as specified in section 2.6.1.3 "Resource Identifiers" of the
 * Jakarta Faces Specification Document, is split into the file identifying part, which resolves the resource, and the
 * query string, which is appended to the request path.
 */
class ResourceHandlerImplQueryStringTest {

    @Test
    void keepsResourceNameWithoutQueryString() {
        assertNull(removeQueryString(null));
        assertEquals("", removeQueryString(""));
        assertEquals("theme.css", removeQueryString("theme.css"));
        assertEquals("css/theme.css", removeQueryString("css/theme.css"));
    }

    @Test
    void removesQueryString() {
        assertEquals("theme.css", removeQueryString("theme.css?v=1"));
        assertEquals("css/theme.css", removeQueryString("css/theme.css?v=1"));
        assertEquals("theme.css", removeQueryString("theme.css?first=1&second=2"));
        assertEquals("theme.css", removeQueryString("theme.css?"));
    }

    /**
     * A resource name is a file name, so a '?' in any but the first position still starts the query string.
     */
    @Test
    void removesQueryStringFromFirstQuestionMarkOn() {
        assertEquals("theme.css", removeQueryString("theme.css?v=1?v=2"));
        assertEquals("", removeQueryString("?v=1"));
    }

    @Test
    void hasNoQueryStringWithoutQuestionMark() {
        assertNull(getQueryString(null));
        assertNull(getQueryString(""));
        assertNull(getQueryString("theme.css"));
        assertNull(getQueryString("css/theme.css"));
    }

    /**
     * A '?' in the last position introduces an empty query string, which is no query string at all.
     */
    @Test
    void hasNoQueryStringWithTrailingQuestionMark() {
        assertNull(getQueryString("theme.css?"));
        assertNull(getQueryString("?"));
    }

    @Test
    void keepsQueryStringWithoutItsIntroducingQuestionMark() {
        assertEquals("v=1", getQueryString("theme.css?v=1"));
        assertEquals("v=1", getQueryString("css/theme.css?v=1"));
        assertEquals("first=1&second=2", getQueryString("theme.css?first=1&second=2"));
        assertEquals("v=1?v=2", getQueryString("theme.css?v=1?v=2"));
    }
}
