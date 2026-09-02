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
package org.eclipse.mojarra.test.issue5989;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

/**
 * The Facelets compiler collects consecutive markup into a single text unit and flushes it whenever a child of that
 * unit starts, and once more when the unit itself ends. Both flushes drop the collected text when it consists solely of
 * whitespace, and both must otherwise emit it verbatim, because whitespace next to an inline child is content.
 *
 * The view exercises every shape that reaches those two flushes: literal text, text carrying an expression, text in
 * front of a tag handler rather than a component, whitespace which is not a plain space, whitespace-only text, text at
 * the end of a component body, and text which never gets a child at all.
 *
 * https://github.com/eclipse-ee4j/mojarra/issues/5989
 */
class Issue5989IT extends BaseIT {

    /**
     * The whitespace between text and a child which follows it is content and must be rendered verbatim, no matter
     * whether the text is literal, carries an expression, or precedes a tag handler instead of a component, and no
     * matter which whitespace character it is.
     */
    @Test
    void testWhitespaceBeforeChildIsPreserved() {
        String body = getResponseBody("issue5989.xhtml");

        assertEquals("before component after", getElementContent(body, "literalTextBeforeComponent"), body);
        assertEquals("before component after", getElementContent(body, "dynamicTextBeforeComponent"), body);
        assertEquals("before component after", getElementContent(body, "literalTextBeforeTagHandler"), body);
        assertEquals("before\tcomponent after", getElementContent(body, "tabBeforeComponent"), body);
    }

    /**
     * Text which consists solely of whitespace carries no content of its own, so the flush drops it and two children
     * separated by nothing else end up adjacent.
     */
    @Test
    void testWhitespaceOnlyTextBeforeChildIsDropped() {
        String body = getResponseBody("issue5989.xhtml");

        assertEquals("beforeafter", getElementContent(body, "whitespaceOnlyBetweenComponents"), body);
    }

    /**
     * The text unit is flushed a second time when it ends, which is the case for text trailing the last child of a
     * component body. That flush must leave the whitespace alone as well.
     */
    @Test
    void testTrailingWhitespaceAtEndOfComponentBodyIsPreserved() {
        String body = getResponseBody("issue5989.xhtml");

        assertEquals("before component after\t", getElementContent(body, "trailingTextAtEndOfComponentBody"), body);
    }

    /**
     * Markup which never gets a child is flushed at the surrounding element boundaries, which is a flush that never
     * drops anything, so its trailing whitespace survives too.
     */
    @Test
    void testTrailingWhitespaceWithoutChildIsPreserved() {
        String body = getResponseBody("issue5989.xhtml");

        assertEquals("before\t", getElementContent(body, "trailingTextWithoutChild"), body);
    }

    /**
     * Returns the content between the opening and the closing tag of the element with the given id.
     */
    private static String getElementContent(String body, String id) {
        int idIndex = body.indexOf("id=\"" + id + "\"");
        if (idIndex == -1) {
            throw new IllegalStateException("No element with id " + id + " in response:\n" + body);
        }

        int contentStart = body.indexOf('>', idIndex) + 1;
        return body.substring(contentStart, body.indexOf('<', contentStart));
    }
}
