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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.faces.component.html.HtmlPanelGroup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * An id is valid when it starts with a letter or an underscore and continues with letters, digits, hyphens or
 * underscores, where letter and digit mean what {@link Character#isLetter(char)} and {@link Character#isDigit(char)}
 * mean. Almost every id a build validates is ASCII, which the check answers without consulting {@link Character}, so
 * these pin the two down to the same verdicts on both sides of that boundary.
 */
class UIComponentIdValidationTest {

    private static final char ARABIC_INDIC_DIGIT_ZERO = '٠';
    private static final char E_ACUTE = 'é';
    private static final char EURO_SIGN = '€';

    @ParameterizedTest
    @ValueSource(strings = { "_", "a", "Z", "j_id_2c", "form-1", "_1", "é", "aé", "a٠" })
    void anIdOfLettersDigitsHyphensAndUnderscoresIsAccepted(String id) {
        UIComponent component = new HtmlPanelGroup();

        component.setId(id);

        assertEquals(id, component.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1abc", "-abc", "a*c", " abc", "a c", "a.c", "a:c" })
    void anIdOutsideThatAlphabetIsRejected(String id) {
        UIComponent component = new HtmlPanelGroup();

        assertThrows(IllegalArgumentException.class, () -> component.setId(id));
    }

    @Test
    void aNonAsciiDigitIsRejectedAsTheFirstCharacterButAcceptedAfterIt() {
        UIComponent component = new HtmlPanelGroup();

        assertThrows(IllegalArgumentException.class, () -> component.setId(String.valueOf(ARABIC_INDIC_DIGIT_ZERO)));

        component.setId(E_ACUTE + String.valueOf(ARABIC_INDIC_DIGIT_ZERO));
    }

    @Test
    void aNonAsciiCharacterThatIsNeitherLetterNorDigitIsRejected() {
        UIComponent component = new HtmlPanelGroup();

        assertThrows(IllegalArgumentException.class, () -> component.setId(String.valueOf(EURO_SIGN)));
        assertThrows(IllegalArgumentException.class, () -> component.setId("a" + EURO_SIGN));
    }
}
