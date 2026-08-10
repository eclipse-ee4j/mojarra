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

package com.sun.faces.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Util.split splits around a character, so it must produce exactly what splitting around that character quoted as a
 * regular expression produces -- quoting being what makes it literal -- including for the limit, which decides whether
 * trailing empty strings survive.
 */
class UtilSplitTest {

    @ParameterizedTest
    @CsvSource(quoteCharacter = '\'', value = {
            // the delimiters the implementation itself passes
            "'a,b,c', ',', 0",
            "'a:b:c', ':', 0",
            "'a b c', ' ', 0",
            "'a/b/c', '/', 0",
            "'a;b;c', ';', 0",
            "'a&b&c', '&', 0",
            "'k=v=w', '=', 0",
            "'k=v=w', '=', 2",
            // characters that would be metacharacters if this were a regular expression
            "'a.b.c', '.', 0",
            "'a|b|c', '|', 0",
            "'a*b*c', '*', 0",
            "'a+b+c', '+', 0",
            "'a$b$c', '$', 0",
            "'a^b^c', '^', 0",
            "'a(b(c', '(', 0",
            "'a[b[c', '[', 0",
            "'a{b{c', '{', 0",
            "'a?b?c', '?', 0",
            "'a\\b\\c', '\\', 0",
            // the delimiter is absent, or is the whole string
            "'nodelimiter', ',', 0",
            "',', ',', 0",
            "'', ',', 0",
            // leading and trailing empties, where the limit semantics live
            "'a,b,,,', ',', 0",
            "'a,b,,,', ',', -1",
            "'a,b,,,', ',', 2",
            "'a,b,,,', ',', 3",
            "',,a,b', ',', 0",
            "',,a,b', ',', -1",
            "'a,b', ',', 1",
    })
    void splitsAroundTheCharacterTakenLiterally(String toSplit, char delimiter, int splitLimit) {
        assertArrayEquals(Pattern.compile(Pattern.quote(String.valueOf(delimiter))).split(toSplit, splitLimit),
                Util.split(toSplit, delimiter, splitLimit));
    }

    /**
     * The two-argument overload is the one most call sites use, and it must agree with a limit of zero.
     */
    @Test
    void theTwoArgumentOverloadSplitsWithoutALimit() {
        assertArrayEquals(Util.split("a,b,,,", ',', 0), Util.split("a,b,,,", ','));
        assertArrayEquals(Util.split("a.b.c", '.', 0), Util.split("a.b.c", '.'));
    }

    /**
     * A character delimiter cannot be a regular expression, which is the reason the method takes one. The case that
     * would differ under regular expression semantics gets an assertion of its own rather than only being covered
     * against a quoted oracle.
     */
    @Test
    void aMetacharacterDelimiterIsNotTreatedAsARegularExpression() {
        assertArrayEquals(new String[] { "a", "b", "c" }, Util.split("a.b.c", '.'));
        assertArrayEquals(new String[] { "abc" }, Util.split("abc", '.'));
        assertArrayEquals(new String[] { "a", "b" }, Util.split("a\\b", '\\'));
    }
}
