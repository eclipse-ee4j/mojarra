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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
 * The JUnit tests for the NumberConverter class.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
 */
public class NumberConverterTest {

    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");

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

    private static boolean hasPrefixWithNbsp(Locale locale) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        if (currencyFormat instanceof DecimalFormat) {
            return ((DecimalFormat) currencyFormat).getPositivePrefix().contains("\u00a0");
        }
        return false;
    }

    /**
     * Test that currency parsing accepts user input with regular space (U+0020) when the
     * formatter uses NBSP (U+00A0) in the currency prefix.
     *
     * On JDK 17+, NumberFormat.getCurrencyInstance for pt-BR produces "R$\u00A0" as the
     * positive prefix. Users naturally type "R$ " with a regular space, causing a parse failure.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testBrazilianCurrencyParsingWithRegularSpaceInPrefix() {
        if (!hasPrefixWithNbsp(LOCALE_PT_BR)) {
            throw new TestAbortedException("JDK 17+ required: pt-BR currency prefix does not contain NBSP");
        }

        NumberConverter converter = new NumberConverter();
        converter.setType("currency");
        converter.setLocale(LOCALE_PT_BR);

        UIPanel component = new UIPanel();

        // This is what a user would type: "R$ 1.234,56" with regular space (U+0020).
        String userInput = "R$ 1.234,56";

        // This should succeed but currently throws ConverterException because the
        // formatter expects NBSP (U+00A0) between "R$" and the number.
        Object result = converter.getAsObject(facesContext, component, userInput);

        assertNotNull(result, "Parsing 'R$ 1.234,56' with regular space should succeed");
        assertEquals(1234.56, ((Number) result).doubleValue(), 0.001);
    }

    /**
     * Test that currency parsing works with NBSP (the character the formatter expects).
     * This verifies the formatter itself works — the input uses the "right" character.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testBrazilianCurrencyParsingWithNbsp() {
        if (!hasPrefixWithNbsp(LOCALE_PT_BR)) {
            throw new TestAbortedException("JDK 17+ required: pt-BR currency prefix does not contain NBSP");
        }

        NumberConverter converter = new NumberConverter();
        converter.setType("currency");
        converter.setLocale(LOCALE_PT_BR);

        UIPanel component = new UIPanel();

        // Input with NBSP — this should always work.
        String inputWithNbsp = "R$\u00a01.234,56";

        Object result = converter.getAsObject(facesContext, component, inputWithNbsp);

        assertNotNull(result);
        assertEquals(1234.56, ((Number) result).doubleValue(), 0.001);
    }

    /**
     * Test that getAsString/getAsObject roundtrip works when the formatted output
     * is manually typed by a user (i.e., NBSP replaced with regular space).
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    @Test
    public void testBrazilianCurrencyRoundtripWithRegularSpace() {
        if (!hasPrefixWithNbsp(LOCALE_PT_BR)) {
            throw new TestAbortedException("JDK 17+ required: pt-BR currency prefix does not contain NBSP");
        }

        NumberConverter converter = new NumberConverter();
        converter.setType("currency");
        converter.setLocale(LOCALE_PT_BR);

        UIPanel component = new UIPanel();

        Number originalValue = 99.99;

        // getAsString produces formatted output (contains NBSP)
        String formatted = converter.getAsString(facesContext, component, originalValue);
        assertNotNull(formatted);

        // Simulate user typing: NBSP replaced with regular space
        String userInput = formatted.replace('\u00a0', ' ');

        // This roundtrip should work
        Object parsed = converter.getAsObject(facesContext, component, userInput);

        assertNotNull(parsed, "Roundtrip with regular space should succeed");
        assertEquals(originalValue.doubleValue(), ((Number) parsed).doubleValue(), 0.001);
    }

    /**
     * Test that currency parsing with NBSP in grouping separator still works.
     * Some locales use NBSP as the grouping separator (e.g., fr-FR).
     * This tests the existing HACK 4510618 path.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5399">GitHub issue #5399</a>
     */
    // TODO: uncomment in Faces 5.0: @Test
    public void testFrenchCurrencyParsingWithNbspGroupingSeparator() {
        Locale localeFR = Locale.FRANCE;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(localeFR);
        if (currencyFormat instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) currencyFormat;
            char groupingSep = df.getDecimalFormatSymbols().getGroupingSeparator();
            if (groupingSep != '\u00a0' && groupingSep != '\u202f') {
                throw new TestAbortedException("JDK 17+ required: fr-FR grouping separator is not NBSP/NNBSP");
            }
        }

        NumberConverter converter = new NumberConverter();
        converter.setType("currency");
        converter.setLocale(localeFR);

        UIPanel component = new UIPanel();

        // Format a value, then replace NBSP/NNBSP with regular space to simulate user input
        String formatted = currencyFormat.format(1234.56);
        String userInput = formatted.replace('\u00a0', ' ').replace('\u202f', ' ');

        Object result = converter.getAsObject(facesContext, component, userInput);

        assertNotNull(result, "Parsing French currency with regular spaces should succeed");
        assertEquals(1234.56, ((Number) result).doubleValue(), 0.001);
    }
}
