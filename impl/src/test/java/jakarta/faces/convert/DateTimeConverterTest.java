/*
 * Copyright (c) Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opentest4j.TestAbortedException;

/**
 * The JUnit tests for the DateTimeConverter class.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
 */
public class DateTimeConverterTest {

    private FacesContext facesContext;
    private MockedStatic<FacesContext> facesContextStatic;

    @BeforeEach
    public void setUp() {
        facesContext = Mockito.mock(FacesContext.class);
        UIViewRoot viewRoot = Mockito.mock(UIViewRoot.class);
        Application application = Mockito.mock(Application.class);

        Mockito.when(facesContext.getViewRoot()).thenReturn(viewRoot);
        Mockito.when(facesContext.getApplication()).thenReturn(application);
        Mockito.when(viewRoot.createUniqueId()).thenReturn("test");
        Mockito.when(viewRoot.getLocale()).thenReturn(Locale.US);

        facesContextStatic = Mockito.mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    }

    @AfterEach
    public void tearDown() {
        facesContextStatic.close();
    }

    /**
     * Test that localTime parsing accepts user input with regular space before AM/PM.
     *
     * On JDK 21+, DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM) with Locale.US
     * produces a pattern that uses NNBSP (U+202F) between time and AM/PM marker.
     * Users naturally type regular spaces (U+0020), causing a parse failure.
     *
     * @see <a href="https://bugs.openjdk.org/browse/JDK-8324308">JDK-8324308</a>
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testLocalTimeParsingWithRegularSpaceBeforeAmPm() {
        requireNnbspInPattern(null, FormatStyle.MEDIUM);

        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("localTime");
        converter.setLocale(Locale.US);

        UIPanel component = new UIPanel();

        // This is what a user would type: regular space (U+0020) before AM.
        String userInput = "10:30:00 AM";

        // This should succeed but currently throws ConverterException on JDK 21+
        // because the formatter expects NNBSP (U+202F) instead of regular space.
        Object result = converter.getAsObject(facesContext, component, userInput);

        assertNotNull(result, "Parsing '10:30:00 AM' with regular space should succeed");
        assertInstanceOf(LocalTime.class, result);
        assertEquals(LocalTime.of(10, 30, 0), result);
    }

    /**
     * Test that localTime parsing works with NNBSP (the JDK 21+ character).
     * This verifies the formatter itself works — the input just uses the "right" character.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testLocalTimeParsingWithNnbsp() {
        requireNnbspInPattern(null, FormatStyle.MEDIUM);

        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("localTime");
        converter.setLocale(Locale.US);

        UIPanel component = new UIPanel();

        // Input with NNBSP — this should always work on JDK 21+.
        String inputWithNnbsp = "10:30:00\u202fAM";

        Object result = converter.getAsObject(facesContext, component, inputWithNnbsp);

        assertNotNull(result);
        assertInstanceOf(LocalTime.class, result);
        assertEquals(LocalTime.of(10, 30, 0), result);
    }

    /**
     * Test that localDateTime parsing accepts user input with regular space before AM/PM.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testLocalDateTimeParsingWithRegularSpaceBeforeAmPm() {
        requireNnbspInPattern(FormatStyle.MEDIUM, FormatStyle.MEDIUM);

        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("localDateTime");
        converter.setLocale(Locale.US);

        UIPanel component = new UIPanel();

        // Format a known value to get the expected formatted string, then replace NNBSP with regular space
        // to simulate user input.
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.US);
        LocalDateTime testDateTime = LocalDateTime.of(2024, 3, 15, 10, 30, 0);
        String formatted = formatter.format(testDateTime);
        String userInput = formatted.replace('\u202f', ' ');

        Object result = converter.getAsObject(facesContext, component, userInput);

        assertNotNull(result, "Parsing localDateTime with regular space should succeed");
        assertInstanceOf(LocalDateTime.class, result);
        assertEquals(testDateTime, result);
    }

    /**
     * Test that getAsString and then getAsObject roundtrip works when the formatted output
     * is manually typed by a user (i.e., NNBSP replaced with regular space).
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testLocalTimeRoundtripWithRegularSpace() {
        requireNnbspInPattern(null, FormatStyle.MEDIUM);

        DateTimeConverter converter = new DateTimeConverter();
        converter.setType("localTime");
        converter.setLocale(Locale.US);

        UIPanel component = new UIPanel();

        LocalTime originalTime = LocalTime.of(14, 45, 30);

        // getAsString produces formatted output (may contain NNBSP on JDK 21+)
        String formatted = converter.getAsString(facesContext, component, originalTime);
        assertNotNull(formatted);

        // Simulate a user copying the displayed value but the browser/OS normalizing NNBSP to regular space
        String userInput = formatted.replace('\u202f', ' ');

        // This roundtrip should work
        Object parsed = converter.getAsObject(facesContext, component, userInput);

        assertNotNull(parsed, "Roundtrip with regular space should succeed");
        assertEquals(originalTime, parsed);
    }

    private static void requireNnbspInPattern(FormatStyle dateStyle, FormatStyle timeStyle) {
        String localizedPattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                dateStyle, timeStyle, IsoChronology.INSTANCE, Locale.US);
        if (!localizedPattern.contains("\u202f")) {
            throw new TestAbortedException("JDK 21+ required: localized pattern does not contain NNBSP");
        }
    }
}
