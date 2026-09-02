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

package org.glassfish.mojarra.util;

import static org.glassfish.mojarra.util.Util.trimToNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Util.trimToNull answers with the trimmed string, or with null when nothing is left. Two distinct sets of characters
 * count as trimmable: the control characters below U+0020, which only {@link String#trim()} removes, and the Unicode
 * spaces above U+007F, which only {@link String#strip()} removes. Callers write the answer into a URL unencoded, so
 * neither set may be left at either end of the returned string, whatever the string holds in between.
 */
class UtilTrimToNullTest {

    private static final String NUL = "\u0000";
    private static final String ESCAPE = "\u001B";
    private static final String EN_QUAD = "\u2000";
    private static final String IDEOGRAPHIC_SPACE = "\u3000";

    @Test
    void nullAndEmptyBecomeNull() {
        assertNull(trimToNull(null));
        assertNull(trimToNull(""));
    }

    @Test
    void whitespaceOnlyBecomesNull() {
        assertNull(trimToNull("   "));
        assertNull(trimToNull("\t\r\n"));
    }

    @Test
    void controlCharactersOnlyBecomeNull() {
        assertNull(trimToNull(NUL));
        assertNull(trimToNull(ESCAPE + NUL));
    }

    @Test
    void unicodeSpacesOnlyBecomeNull() {
        assertNull(trimToNull(EN_QUAD));
        assertNull(trimToNull(IDEOGRAPHIC_SPACE + EN_QUAD));
    }

    @Test
    void surroundingWhitespaceAndControlCharactersAreRemoved() {
        assertEquals("foo", trimToNull("  foo  "));
        assertEquals("foo", trimToNull(NUL + "foo" + NUL));
        assertEquals("foo", trimToNull(" " + ESCAPE + "foo" + NUL + " "));
    }

    @Test
    void surroundingUnicodeSpacesAreRemoved() {
        assertEquals("foo", trimToNull(EN_QUAD + "foo" + EN_QUAD));
        assertEquals("foo", trimToNull(IDEOGRAPHIC_SPACE + " foo " + EN_QUAD));
    }

    /**
     * Neither trimmable set covers the other, so a run mixing them must be consumed whole from either end.
     */
    @Test
    void surroundingMixOfBothSetsIsRemoved() {
        assertEquals("foo", trimToNull(EN_QUAD + NUL + "foo" + NUL + EN_QUAD));
        assertEquals("foo", trimToNull(NUL + EN_QUAD + "foo" + EN_QUAD + NUL));
    }

    @Test
    void innerWhitespaceIsKept() {
        assertEquals("foo bar", trimToNull("  foo bar  "));
    }

    @Test
    void alreadyTrimmedIsUnchanged() {
        assertEquals("foo", trimToNull("foo"));
    }
}
