package com.sun.faces.util;

import static com.sun.faces.util.HtmlUtils.isAllowedXmlCharacter;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HtmlUtilsTest {

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/4516
     * https://github.com/eclipse-ee4j/mojarra/issues/5464
     */
    @Test
    void testAllowedXmlCharacter() {
        for (int c = 0x0000; c <= 0x0008; c++) {
            assertFalse(isAllowedXmlCharacter(c));
        }

        assertTrue(isAllowedXmlCharacter(0x0009));
        assertTrue(isAllowedXmlCharacter(0x000A));

        assertFalse(isAllowedXmlCharacter(0x000B));
        assertFalse(isAllowedXmlCharacter(0x000C));

        assertTrue(isAllowedXmlCharacter(0x000D));

        for (int c = 0x000E; c <= 0x0019; c++) {
            assertFalse(isAllowedXmlCharacter(c));
        }

        for (int c = 0x0020; c <= 0xD7FF; c++) {
            assertTrue(isAllowedXmlCharacter(c));
        }

        for (int c = 0xD800; c <= 0xDFFF; c++) {
            assertFalse(isAllowedXmlCharacter(c));
        }

        for (int c = 0xE000; c <= 0xFFFD; c++) {
            assertTrue(isAllowedXmlCharacter(c));
        }

        assertFalse(isAllowedXmlCharacter(0xFFFE));
        assertFalse(isAllowedXmlCharacter(0xFFFF));
    }
}
