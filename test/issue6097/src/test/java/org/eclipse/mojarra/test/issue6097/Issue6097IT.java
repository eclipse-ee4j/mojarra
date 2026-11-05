/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue6097;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

class Issue6097IT extends BaseIT {

    private static final Pattern TEXT_ELEMENT = Pattern.compile("<span id=\"text\"[^>]*>");

    /**
     * When a standard HTML component gets an attribute set and then another one bound after the view was built, then
     * each attribute must be rendered once. The raw response is asserted, since a browser keeps only one of two equal
     * attributes.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6097">issue 6097</a>
     */
    @Test
    void testAttributesChangedAfterBuildAreRenderedOnce() {
        Matcher text = TEXT_ELEMENT.matcher(getResponseBody("issue6097.xhtml"));
        assertTrue(text.find(), "text element is rendered");

        String element = text.group();
        assertEquals(1, countAttribute(element, "class"), element);
        assertEquals(1, countAttribute(element, "title"), element);
        assertTrue(element.contains(" class=\"changed\""), element);
        assertTrue(element.contains(" title=\"Title\""), element);
    }

    private static long countAttribute(String element, String name) {
        return Pattern.compile("\\s" + name + "=\"").matcher(element).results().count();
    }
}
