/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 * {@link FastNumberFormat} must print exactly what the {@link DecimalFormat} it was made from prints, for every value it
 * covers.
 */
class FastNumberFormatTest {

    private static final Locale[] LOCALES = { Locale.US, Locale.ITALY, Locale.GERMANY, Locale.FRANCE, Locale.forLanguageTag("de-CH"),
            Locale.forLanguageTag("fr-CH"), Locale.forLanguageTag("en-IN"), Locale.forLanguageTag("hi-IN"), Locale.JAPAN,
            Locale.forLanguageTag("ar-EG"), Locale.forLanguageTag("fa-IR"), Locale.forLanguageTag("nl-NL"), Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("sv-SE"), Locale.ROOT };

    private static final String[] PATTERNS = { "#,##0.00", "0.###", "#", "00000", "#,##0.0#;(#,##0.0#)", "¤ #,##0.00", "#.##",
            "#,##0.00 ¤", "'#'0", "0.00%", "0.0E0", "#,##,##0.##", "##0.00‰" };

    private static List<Consumer<DecimalFormat>> configurations() {
        List<Consumer<DecimalFormat>> configurations = new ArrayList<>();
        configurations.add(format -> {
        });
        configurations.add(format -> format.setGroupingUsed(false));
        configurations.add(format -> format.setGroupingUsed(true));
        configurations.add(format -> {
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
        });
        configurations.add(format -> {
            format.setMinimumFractionDigits(0);
            format.setMaximumFractionDigits(4);
        });
        configurations.add(format -> format.setMaximumFractionDigits(0));
        configurations.add(format -> format.setMinimumIntegerDigits(0));
        configurations.add(format -> format.setMinimumIntegerDigits(3));
        configurations.add(format -> format.setMaximumIntegerDigits(4));
        configurations.add(format -> format.setDecimalSeparatorAlwaysShown(true));
        configurations.add(format -> format.setRoundingMode(RoundingMode.HALF_UP));
        configurations.add(format -> format.setRoundingMode(RoundingMode.HALF_DOWN));
        configurations.add(format -> format.setRoundingMode(RoundingMode.DOWN));
        configurations.add(format -> format.setRoundingMode(RoundingMode.UP));
        configurations.add(format -> format.setRoundingMode(RoundingMode.CEILING));
        configurations.add(format -> format.setCurrency(Currency.getInstance("EUR")));
        configurations.add(format -> format.setGroupingSize(4));
        configurations.add(format -> format.setMultiplier(10));
        return configurations;
    }

    private static List<DecimalFormat> formats() {
        List<DecimalFormat> formats = new ArrayList<>();
        for (Locale locale : LOCALES) {
            List<NumberFormat> bases = new ArrayList<>(List.of(NumberFormat.getNumberInstance(locale), NumberFormat.getCurrencyInstance(locale),
                    NumberFormat.getIntegerInstance(locale), NumberFormat.getPercentInstance(locale)));
            for (String pattern : PATTERNS) {
                bases.add(new DecimalFormat(pattern, java.text.DecimalFormatSymbols.getInstance(locale)));
            }
            for (NumberFormat base : bases) {
                if (!(base instanceof DecimalFormat)) {
                    continue;
                }
                for (Consumer<DecimalFormat> configuration : configurations()) {
                    DecimalFormat format = (DecimalFormat) base.clone();
                    configuration.accept(format);
                    formats.add(format);
                }
            }
        }
        return formats;
    }

    private static List<Object> values() {
        List<Object> values = new ArrayList<>(List.of(0, 1, -1, 5, -5, 9, 10, 99, 100, 999, 1000, -1000, 12345, Integer.MAX_VALUE, Integer.MIN_VALUE,
                Long.MAX_VALUE, Long.MIN_VALUE + 1, Long.MIN_VALUE, (short) -300, (byte) 7, BigInteger.valueOf(123456789L),
                BigInteger.TEN.pow(30), new BigDecimal("0"), new BigDecimal("-0"), new BigDecimal("0.00"), new BigDecimal("-0.000"),
                new BigDecimal("0.5"), new BigDecimal("1.5"), new BigDecimal("2.5"), new BigDecimal("-2.5"), new BigDecimal("0.005"),
                new BigDecimal("0.015"), new BigDecimal("0.025"), new BigDecimal("-0.005"), new BigDecimal("0.0005"), new BigDecimal("0.00049"),
                new BigDecimal("0.0004"), new BigDecimal("-0.0004"), new BigDecimal("0.0006"), new BigDecimal("0.00006"), new BigDecimal("999.995"),
                new BigDecimal("999.9949"), new BigDecimal("9999.99999"), new BigDecimal("1E+3"), new BigDecimal("1E+25"), new BigDecimal("-1.2E-7"),
                new BigDecimal("12345678901234567890.123456789"), new BigDecimal("0.1"), new BigDecimal("1.50"), new BigDecimal("1234.5678"),
                12.5, 1.25f, new java.util.concurrent.atomic.AtomicLong(5)));
        Random random = new Random(42);
        for (int i = 0; i < 400; i++) {
            long unscaled = random.nextLong() >> random.nextInt(63);
            values.add(BigDecimal.valueOf(unscaled, random.nextInt(14) - 4));
            values.add(unscaled);
            values.add((int) unscaled);
        }
        return values;
    }

    @Test
    void printsWhatDecimalFormatPrints() {
        List<Object> values = values();
        int covered = 0;
        int formatsCovered = 0;
        for (DecimalFormat format : formats()) {
            FastNumberFormat fast = FastNumberFormat.of(format);
            if (fast == null) {
                continue;
            }
            formatsCovered++;
            for (Object value : values) {
                String text = fast.format(value);
                if (text != null) {
                    covered++;
                    assertEquals(format.format(value), text, () -> format.toPattern() + " " + format.getPositivePrefix() + "|" + format.getDecimalFormatSymbols().getDecimalSeparator() + " "
                            + format.getRoundingMode() + " " + value + " (" + value.getClass().getSimpleName() + ")");
                }
            }
        }
        // the fast path must actually be taken for most of what is covered
        assertEquals(true, formatsCovered > 1000, "formats covered: " + formatsCovered);
        assertEquals(true, covered > 1_000_000, "values covered: " + covered);
    }

    @Test
    void coversTheCommonFormats() {
        for (Locale locale : new Locale[] { Locale.US, Locale.ITALY, Locale.GERMANY, Locale.FRANCE }) {
            assertNotNull(FastNumberFormat.of(NumberFormat.getNumberInstance(locale)), locale.toString());
            assertNotNull(FastNumberFormat.of(NumberFormat.getCurrencyInstance(locale)), locale.toString());
            assertNotNull(FastNumberFormat.of(NumberFormat.getIntegerInstance(locale)), locale.toString());
        }
        assertNull(FastNumberFormat.of(NumberFormat.getPercentInstance(Locale.US)));
        assertNull(FastNumberFormat.of(new DecimalFormat("0.0E0")));
        DecimalFormat up = new DecimalFormat("0.00");
        up.setRoundingMode(RoundingMode.UP);
        assertNull(FastNumberFormat.of(up));

        FastNumberFormat fast = FastNumberFormat.of(NumberFormat.getNumberInstance(Locale.US));
        assertNull(fast.format(12.5));
        assertNull(fast.format(Long.MIN_VALUE));
        assertNull(fast.format(BigInteger.TEN.pow(30)));
        assertEquals("1,234.568", fast.format(new BigDecimal("1234.5678")));
    }
}
