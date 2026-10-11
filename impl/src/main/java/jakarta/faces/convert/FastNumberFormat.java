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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Formats integers and {@link BigDecimal}s exactly as a given {@link DecimalFormat} does, without its synchronized
 * {@link StringBuffer} appended one character at a time and its synchronized digit list: what {@link DecimalFormat}
 * does in fixed point notation, for the configurations and values covered here.
 *
 * <p>
 * Covered: a plain {@link DecimalFormat} (not a subclass) with ASCII digits, no exponent, no multiplier, rounding
 * {@code HALF_EVEN}, {@code HALF_UP}, {@code HALF_DOWN} or {@code DOWN}, at most {@value #MAX_DIGITS} minimum integer
 * and fraction digits; values {@link Long}, {@link Integer}, {@link Short}, {@link Byte}, a {@link BigInteger} that fits
 * a long, and {@link BigDecimal}, with no more integer digits than the maximum. Anything else is left to the
 * {@link DecimalFormat}. The separators and affixes are taken from what the {@link DecimalFormat} itself prints, and a
 * formatter that does not print exactly what the {@link DecimalFormat} prints for a set of probe values is not used.
 */
final class FastNumberFormat {

    private static final int MAX_DIGITS = 50;

    private static final Object[] PROBES = { 0L, 1L, -1L, 7, (short) -12, (byte) 99, 1234L, -1234567L, 123456789012345L, new BigDecimal("0"),
            new BigDecimal("-0"), new BigDecimal("0.5"), new BigDecimal("0.005"), new BigDecimal("-0.001"), new BigDecimal("0.0004"),
            new BigDecimal("0.0006"), new BigDecimal("1.25"), new BigDecimal("-2.5"), new BigDecimal("999.995"), new BigDecimal("1234.5678"),
            new BigDecimal("-98765.4321"), new BigDecimal("1E+3"), new BigDecimal("1.50"), new BigDecimal("100"), new BigDecimal("0.125"),
            new BigDecimal("12345678.9"), BigInteger.valueOf(42) };

    private final String positivePrefix;
    private final String positiveSuffix;
    private final String negativePrefix;
    private final String negativeSuffix;
    private final char grouping;
    private final char decimal;
    private final int groupingSize;
    private final boolean decimalSeparatorAlwaysShown;
    private final int minIntegerDigits;
    private final int maxIntegerDigits;
    private final int minFractionDigits;
    private final int maxFractionDigits;
    private final RoundingMode roundingMode;
    /** The fraction of an integer: the minimum fraction digits, zeros. */
    private final String integerFraction;

    private FastNumberFormat(DecimalFormat format, char grouping, char decimal) {
        positivePrefix = format.getPositivePrefix();
        positiveSuffix = format.getPositiveSuffix();
        negativePrefix = format.getNegativePrefix();
        negativeSuffix = format.getNegativeSuffix();
        this.grouping = grouping;
        this.decimal = decimal;
        groupingSize = format.isGroupingUsed() ? format.getGroupingSize() : 0;
        decimalSeparatorAlwaysShown = format.isDecimalSeparatorAlwaysShown();
        minIntegerDigits = format.getMinimumIntegerDigits();
        maxIntegerDigits = format.getMaximumIntegerDigits();
        minFractionDigits = format.getMinimumFractionDigits();
        maxFractionDigits = format.getMaximumFractionDigits();
        roundingMode = format.getRoundingMode();
        integerFraction = "0".repeat(minFractionDigits);
    }

    /**
     * @param format a configured number format, not modified
     * @return a formatter printing what <code>format</code> prints, or <code>null</code> when it is not covered
     */
    static FastNumberFormat of(NumberFormat format) {
        if (format == null || format.getClass() != DecimalFormat.class) {
            return null;
        }
        DecimalFormat decimalFormat = (DecimalFormat) format;
        RoundingMode roundingMode = decimalFormat.getRoundingMode();
        if (decimalFormat.getMultiplier() != 1 || decimalFormat.toPattern().indexOf('E') >= 0 || decimalFormat.getDecimalFormatSymbols().getZeroDigit() != '0'
                || roundingMode != RoundingMode.HALF_EVEN && roundingMode != RoundingMode.HALF_UP && roundingMode != RoundingMode.HALF_DOWN
                        && roundingMode != RoundingMode.DOWN
                || decimalFormat.getMinimumIntegerDigits() > MAX_DIGITS || decimalFormat.getMaximumFractionDigits() > MAX_DIGITS
                || decimalFormat.getMaximumIntegerDigits() < 1) {
            return null;
        }

        // The separators the format prints (the monetary ones for a currency format): "1<grouping>2<decimal>5".
        DecimalFormat probe = (DecimalFormat) decimalFormat.clone();
        probe.setGroupingUsed(true);
        probe.setGroupingSize(1);
        probe.setMinimumIntegerDigits(1);
        probe.setMaximumIntegerDigits(MAX_DIGITS);
        probe.setMinimumFractionDigits(1);
        probe.setMaximumFractionDigits(1);
        probe.setDecimalSeparatorAlwaysShown(false);
        String printed = probe.format(new BigDecimal("12.5"));
        String prefix = probe.getPositivePrefix();
        String suffix = probe.getPositiveSuffix();
        if (!printed.startsWith(prefix) || !printed.endsWith(suffix) || printed.length() != prefix.length() + 5 + suffix.length()) {
            return null;
        }
        String number = printed.substring(prefix.length(), prefix.length() + 5);
        if (number.charAt(0) != '1' || number.charAt(2) != '2' || number.charAt(4) != '5') {
            return null;
        }

        FastNumberFormat fast = new FastNumberFormat(decimalFormat, number.charAt(1), number.charAt(3));
        for (Object value : PROBES) {
            String expected;
            try {
                expected = decimalFormat.format(value);
            } catch (RuntimeException e) {
                return null;
            }
            String actual = fast.format(value);
            if (actual != null && !actual.equals(expected)) {
                return null;
            }
        }
        return fast;
    }

    /**
     * @param value the value to format
     * @return what the {@link DecimalFormat} prints, or <code>null</code> when the value is not covered
     */
    String format(Object value) {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte
                || value instanceof BigInteger && ((BigInteger) value).bitLength() < 64) {
            long number = ((Number) value).longValue();
            if (number == Long.MIN_VALUE) {
                return null;
            }
            boolean negative = number < 0;
            String digits = number == 0 ? "" : Long.toString(negative ? -number : number);
            return format(negative, digits, integerFraction);
        }

        if (value instanceof BigDecimal) {
            BigDecimal number = (BigDecimal) value;
            boolean negative = number.signum() < 0;
            String plain = number.abs().setScale(maxFractionDigits, roundingMode).toPlainString();
            int point = plain.indexOf('.');
            String integer = point < 0 ? plain : plain.substring(0, point);
            String fraction = point < 0 ? "" : plain.substring(point + 1);
            int end = fraction.length();
            while (end > minFractionDigits && fraction.charAt(end - 1) == '0') {
                end--;
            }
            return format(negative, "0".equals(integer) ? "" : integer, fraction.substring(0, end));
        }

        return null;
    }

    /**
     * @param integer the integer digits, without leading zeros, empty for zero
     * @param fraction the fraction digits, at least the minimum, without trailing zeros beyond it
     */
    private String format(boolean negative, String integer, String fraction) {
        int count = Math.max(minIntegerDigits, integer.length());
        if (count > maxIntegerDigits) {
            return null;
        }

        StringBuilder result = new StringBuilder(count + count / 3 + fraction.length() + 16);
        result.append(negative ? negativePrefix : positivePrefix);

        int padding = count - integer.length();
        for (int i = count - 1; i >= 0; i--) {
            int index = count - 1 - i;
            result.append(index < padding ? '0' : integer.charAt(index - padding));
            if (groupingSize > 0 && i > 0 && i % groupingSize == 0) {
                result.append(grouping);
            }
        }

        boolean fractionPresent = !fraction.isEmpty();
        if (!fractionPresent && count == 0) {
            result.append('0');
        }
        if (decimalSeparatorAlwaysShown || fractionPresent) {
            result.append(decimal);
        }
        result.append(fraction);

        result.append(negative ? negativeSuffix : positiveSuffix);
        return result.toString();
    }
}
