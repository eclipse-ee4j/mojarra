/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static jakarta.faces.convert.MessageFactory.getLabel;
import static jakarta.faces.convert.MessageFactory.getMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <span class="changed_modified_2_0_rev_a changed_modified_2_3">{@link Converter}</span> implementation for
 * <code>java.util.Date</code> values.
 * </p>
 *
 * <p>
 * The <code>getAsObject()</code> method parses a String into a <code>java.util.Date</code>, according to the following
 * algorithm:
 * </p>
 * <ul>
 * <li>If the specified String is null, return a <code>null</code>. Otherwise, trim leading and trailing whitespace before
 * proceeding.</li>
 * <li>If the specified String - after trimming - has a zero length, return <code>null</code>.</li>
 * <li>If the <code>locale</code> property is not null, use that <code>Locale</code> for managing parsing. Otherwise, use the
 * <code>Locale</code> from the <code>UIViewRoot</code>.</li>
 *
 * <li>If a <code>pattern</code> has been specified, its syntax must conform the rules specified by
 * <code>java.text.SimpleDateFormat</code> <span class="changed_added_2_3">or {@code
 * java.time.format.DateTimeFormatter}. Which of these two formatters is used depends on the value of {@code type}.</span> Such a
 * pattern will be used to parse, and the <code>type</code>, <code>dateStyle</code>, and <code>timeStyle</code> properties will
 * be ignored, <span class="changed_added_2_3">unless the value of {@code
 * type} is one of the {@code java.time} specific values listed in {@link #setType}. In this case,
 * {@code DateTimeFormatter.ofPattern(String, Locale)} must be called, passing the value of {@code pattern} as the first argument
 * and the current {@code Locale} as the second argument, and this formatter must be used to parse the incoming
 * value.</span></li>
 *
 * <li>If a <code>pattern</code> has not been specified, parsing will be based on the <code>type</code> property, which expects a
 * date value, a time value, both, <span class="changed_added_2_3">or one of several values specific to classes in
 * {@code java.time} as listed in {@link #setType}.</span> Any date and time values included will be parsed in accordance to the
 * styles specified by <code>dateStyle</code> and <code>timeStyle</code>, respectively.</li>
 * <li>If a <code>timezone</code> has been specified, it must be passed to the underlying <code>DateFormat</code> instance.
 * Otherwise the "GMT" timezone is used.</li>
 * <li>In all cases, parsing must be non-lenient; the given string must strictly adhere to the parsing format.</li>
 * </ul>
 *
 * <p>
 * The <code>getAsString()</code> method expects a value of type <code>java.util.Date</code> (or a subclass), and creates a
 * formatted String according to the following algorithm:
 * </p>
 * <ul>
 * <li>If the specified value is null, return a zero-length String.</li>
 * <li>If the specified value is a String, return it unmodified.</li>
 * <li>If the <code>locale</code> property is not null, use that <code>Locale</code> for managing formatting. Otherwise, use the
 * <code>Locale</code> from the <code>UIViewRoot</code>.</li>
 * <li>If a <code>timezone</code> has been specified, it must be passed to the underlying <code>DateFormat</code> instance.
 * Otherwise the "GMT" timezone is used.</li>
 *
 * <li>If a <code>pattern</code> has been specified, its syntax must conform the rules specified by
 * <code>java.text.SimpleDateFormat</code> <span class="changed_added_2_3">or {@code
 * java.time.format.DateTimeFormatter}. Which of these two formatters is used depends on the value of {@code type}.</span> Such a
 * pattern will be used to format, and the <code>type</code>, <code>dateStyle</code>, and <code>timeStyle</code> properties will
 * be ignored, <span class="changed_added_2_3">unless the value of {@code
 * type} is one of the {@code java.time} specific values listed in {@link #setType}. In this case, {@code
 * DateTimeFormatter.ofPattern(String, Locale)} must be called, passing the value of {@code pattern} as the first argument and
 * the current {@code Locale} as the second argument, and this formatter must be used to format the outgoing value.</span></li>
 *
 * <li>If a <code>pattern</code> has not been specified, formatting will be based on the <code>type</code> property, which
 * includes a date value, a time value, both or into the formatted String. Any date and time values included will be formatted in
 * accordance to the styles specified by <code>dateStyle</code> and <code>timeStyle</code>, respectively.</li>
 * </ul>
 */

public class DateTimeConverter implements Converter, PartialStateHolder {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard converter id for this converter.
     * </p>
     */
    public static final String CONVERTER_ID = "jakarta.faces.DateTime";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to
     * <code>Date</code> fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by an example value.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced this
     * message.</li>
     * </ul>
     */
    public static final String DATE_ID = "jakarta.faces.converter.DateTimeConverter.DATE";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to
     * <code>Time</code> fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by an example value.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced this
     * message.</li>
     * </ul>
     */
    public static final String TIME_ID = "jakarta.faces.converter.DateTimeConverter.TIME";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to
     * <code>DateTime</code> fails. The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by an example value.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced this
     * message.</li>
     * </ul>
     */
    public static final String DATETIME_ID = "jakarta.faces.converter.DateTimeConverter.DATETIME";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion of the
     * <code>DateTime</code> value to <code>String</code> fails. The message format string for this message may optionally include
     * the following placeholders:
     * <ul>
     * <li><code>{0}</code> relaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced this
     * message.</li>
     * </ul>
     */
    public static final String STRING_ID = "jakarta.faces.converter.STRING";

    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    // ------------------------------------------------------ Instance Variables

    private String dateStyle = "default";
    private Locale locale;
    private String pattern;
    private String timeStyle = "default";
    private TimeZone timeZone = DEFAULT_TIME_ZONE;
    private String type = "date";

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the style to be used to format or parse dates. If not set, the default value, <code>default</code>, is returned.
     * </p>
     *
     * @return the style
     */
    public String getDateStyle() {
        return dateStyle;
    }

    /**
     * <p>
     * Set the style to be used to format or parse dates. Valid values are <code>default</code>, <code>short</code>,
     * <code>medium</code>, <code>long</code>, and <code>full</code>. An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.
     * </p>
     *
     * @param dateStyle The new style code
     */
    public void setDateStyle(String dateStyle) {
        clearInitialState();
        this.dateStyle = dateStyle;
    }

    /**
     * <p>
     * Return the <code>Locale</code> to be used when parsing or formatting dates and times. If not explicitly set, the
     * <code>Locale</code> stored in the {@link jakarta.faces.component.UIViewRoot} for the current request is returned.
     * </p>
     *
     * @return the {@code Locale}
     */
    public Locale getLocale() {
        if (locale == null) {
            locale = getLocale(FacesContext.getCurrentInstance());
        }

        return locale;
    }

    /**
     * <p>
     * Set the <code>Locale</code> to be used when parsing or formatting dates and times. If set to <code>null</code>, the
     * <code>Locale</code> stored in the {@link jakarta.faces.component.UIViewRoot} for the current request will be utilized.
     * </p>
     *
     * @param locale The new <code>Locale</code> (or <code>null</code>)
     */
    public void setLocale(Locale locale) {
        clearInitialState();
        this.locale = locale;

    }

    /**
     * <p>
     * Return the format pattern to be used when formatting and parsing dates and times.
     * </p>
     *
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * <p>
     * Set the format pattern to be used when formatting and parsing dates and times. Valid values are those supported by
     * <code>java.text.SimpleDateFormat</code>. An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.
     * </p>
     *
     * @param pattern The new format pattern
     */
    public void setPattern(String pattern) {
        clearInitialState();
        this.pattern = pattern;

    }

    /**
     * <p>
     * Return the style to be used to format or parse times. If not set, the default value, <code>default</code>, is returned.
     * </p>
     *
     * @return the time style
     */
    public String getTimeStyle() {
        return timeStyle;
    }

    /**
     * <p>
     * Set the style to be used to format or parse times. Valid values are <code>default</code>, <code>short</code>,
     * <code>medium</code>, <code>long</code>, and <code>full</code>. An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.
     * </p>
     *
     * @param timeStyle The new style code
     */
    public void setTimeStyle(String timeStyle) {
        clearInitialState();
        this.timeStyle = timeStyle;
    }

    /**
     * <p>
     * Return the <code>TimeZone</code> used to interpret a time value. If not explicitly set, the default time zone of
     * <code>GMT</code> returned.
     * </p>
     *
     * @return the {@code TimeZone}
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * <p>
     * Set the <code>TimeZone</code> used to interpret a time value.
     * </p>
     *
     * @param timeZone The new time zone
     */
    public void setTimeZone(TimeZone timeZone) {
        clearInitialState();
        this.timeZone = timeZone;
    }

    /**
     * <p>
     * Return the type of value to be formatted or parsed. If not explicitly set, the default type, <code>date</code> is returned.
     * </p>
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * <p>
     * <span class="changed_modified_2_3">Set</span> the type of value to be formatted or parsed. Valid values are <code>both</code>,
     * <code>date</code>, <code>time</code> <span class="changed_added_2_3">{@code localDate}, {@code
     * localDateTime}, {@code localTime}, {@code offsetTime}, {@code
     * offsetDateTime}, or {@code zonedDateTime}. The values starting with "local", "offset" and "zoned" correspond to Java SE 8 Date
     * Time API classes in package <code>java.time</code> with the name derived by upper casing the first letter. For example,
     * <code>java.time.LocalDate</code> for the value <code>"localDate"</code>.</span> An invalid value will cause a
     * {@link ConverterException} when <code>getAsObject()</code> or <code>getAsString()</code> is called.
     * </p>
     *
     * @param type The new date style
     */
    public void setType(String type) {
        clearInitialState();
        this.type = type;
    }

    // ------------------------------------------------------- Converter Methods

    /**
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (context == null || component == null) {
            throw new NullPointerException();
        }

        Object returnValue = null;
        FormatWrapper parser = null;

        try {

            // If the specified value is null or zero-length, return null
            if (value == null) {
                return null;
            }

            value = value.trim();
            if (value.length() < 1) {
                return null;
            }

            // Identify the Locale to use for parsing
            Locale locale = getLocale(context);

            // Create and configure the parser to be used
            parser = getDateFormat(locale);
            if (timeZone != null) {
                parser.setTimeZone(timeZone);
            }

            // Perform the requested parsing
            returnValue = parser.parse(value);
        } catch (ParseException | DateTimeParseException e) {
            if (type != null) {
                switch (type) {
                case "date":
                case "localDate":
                    throw new ConverterException(
                        getMessage(context, DATE_ID, value, parser.formatNow(), getLabel(context, component)),
                        e);
                case "time":
                case "localTime":
                case "offsetTime":
                    throw new ConverterException(
                        getMessage(context, TIME_ID, value, parser.formatNow(), getLabel(context, component)),
                        e);
                case "both":
                case "localDateTime":
                case "offsetDateTime":
                case "zonedDateTime":
                    throw new ConverterException(getMessage(context, DATETIME_ID, value, parser.formatNow(),
                        getLabel(context, component)), e);
                }
            }
        } catch (Exception e) {
            throw new ConverterException(e);
        }

        return returnValue;
    }

    private static class FormatWrapper {

        private final DateFormat dateFormat;
        private final DateTimeFormatter dateTimeFormatter;
        private final TemporalQuery<Object> from;

        private FormatWrapper(DateFormat dataFormat) {
            this.dateFormat = dataFormat;
            dateTimeFormatter = null;
            from = null;
        }

        private FormatWrapper(DateTimeFormatter dateTimeFormatter, TemporalQuery<Object> from) {
            dateFormat = null;
            this.dateTimeFormatter = dateTimeFormatter;
            this.from = from;
        }

        private Object parse(CharSequence text) throws ParseException {
            return dateFormat != null ? dateFormat.parse((String) text) : dateTimeFormatter.parse(text, from);
        }

        private String format(Object obj) {
            return dateFormat != null ? dateFormat.format(obj) : dateTimeFormatter.format((TemporalAccessor) obj);
        }

        private String formatNow() {
            return dateFormat != null ? dateFormat.format(new Date()) : dateTimeFormatter.format(ZonedDateTime.now());
        }

        private void setTimeZone(TimeZone zone) {
            if (dateFormat != null) {
                dateFormat.setTimeZone(zone);
            }
        }
    }

    /**
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        try {

            // If the specified value is null, return a zero-length String
            if (value == null) {
                return "";
            }

            // If the incoming value is still a string, play nice
            // and return the value unmodified
            if (value instanceof String) {
                return (String) value;
            }

            // Identify the Locale to use for formatting
            Locale locale = getLocale(context);

            // Create and configure the formatter to be used
            FormatWrapper formatter = getDateFormat(locale);
            if (null != timeZone) {
                formatter.setTimeZone(timeZone);
            }

            // Perform the requested formatting
            return formatter.format(value);

        } catch (ConverterException e) {
            throw new ConverterException(getMessage(context, STRING_ID, value, getLabel(context, component)),
                e);
        } catch (Exception e) {
            throw new ConverterException(getMessage(context, STRING_ID, value, getLabel(context, component)),
                e);
        }
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Return a <code>DateFormat</code> instance to use for formatting and parsing in this {@link Converter}.
     * </p>
     *
     * @param locale The <code>Locale</code> used to select formatting and parsing conventions
     * @throws ConverterException if no instance can be created
     */
    private FormatWrapper getDateFormat(Locale locale) {

        // PENDING(craigmcc) - Implement pooling if needed for performance?

        if (pattern == null && type == null) {
            throw new IllegalArgumentException("Either pattern or type must" + " be specified.");
        }

        DateFormat df = null;
        DateTimeFormatter dtf = null;
        TemporalQuery from = null;
        if (pattern != null && !isJavaTimeType(type)) {
            df = new SimpleDateFormat(pattern, locale);
        } else if (type.equals("both")) {
            df = DateFormat.getDateTimeInstance(getStyle(dateStyle), getStyle(timeStyle), locale);
        } else if (type.equals("date")) {
            df = DateFormat.getDateInstance(getStyle(dateStyle), locale);
        } else if (type.equals("time")) {
            df = DateFormat.getTimeInstance(getStyle(timeStyle), locale);
        } else if (type.equals("localDate")) {
            if (null != pattern) {
                dtf = DateTimeFormatter.ofPattern(pattern, locale);
            } else {
                dtf = DateTimeFormatter.ofLocalizedDate(getFormatStyle(dateStyle)).withLocale(locale);
            }
            from = LocalDate::from;
        } else if (type.equals("localDateTime")) {
            if (null != pattern) {
                dtf = DateTimeFormatter.ofPattern(pattern, locale);
            } else {
                dtf = DateTimeFormatter.ofLocalizedDateTime(getFormatStyle(dateStyle), getFormatStyle(timeStyle)).withLocale(locale);
            }
            from = LocalDateTime::from;
        } else if (type.equals("localTime")) {
            if (null != pattern) {
                dtf = DateTimeFormatter.ofPattern(pattern, locale);
            } else {
                dtf = DateTimeFormatter.ofLocalizedTime(getFormatStyle(timeStyle)).withLocale(locale);
            }
            from = LocalTime::from;
        } else if (type.equals("offsetTime")) {
            if (null != pattern) {
                dtf = DateTimeFormatter.ofPattern(pattern, locale);
            } else {
                dtf = DateTimeFormatter.ISO_OFFSET_TIME.withLocale(locale);
            }
            from = OffsetTime::from;
        } else if (type.equals("offsetDateTime")) {
            if (null != pattern) {
                dtf = DateTimeFormatter.ofPattern(pattern, locale);
            } else {
                dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withLocale(locale);
            }
            from = OffsetDateTime::from;
        } else if (type.equals("zonedDateTime")) {
            if (null != pattern) {
                dtf = DateTimeFormatter.ofPattern(pattern, locale);
            } else {
                dtf = DateTimeFormatter.ISO_ZONED_DATE_TIME.withLocale(locale);
            }
            from = ZonedDateTime::from;
        } else {
            // PENDING(craigmcc) - i18n
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        if (null != df) {
            df.setLenient(false);
            return new FormatWrapper(df);
        } else if (null != dtf) {
            return new FormatWrapper(dtf, from);
        }

        // PENDING(craigmcc) - i18n
        throw new IllegalArgumentException("Invalid type: " + type);
    }

    private static boolean isJavaTimeType(String type) {
        boolean result = false;
        if (null != type && type.length() > 1) {
            char c = type.charAt(0);
            result = c == 'l' || c == 'o' || c == 'z';
        }

        return result;
    }

    /**
     * <p>
     * Return the <code>Locale</code> we will use for localizing our formatting and parsing processing.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     */
    private Locale getLocale(FacesContext context) {

        // PENDING(craigmcc) - JSTL localization context?
        Locale locale = this.locale;
        if (locale == null) {
            locale = context.getViewRoot().getLocale();
        }
        return locale;

    }

    /**
     * <p>
     * Return the style constant for the specified style name.
     * </p>
     *
     * @param name Name of the style for which to return a constant
     * @throws ConverterException if the style name is not valid
     */
    private static int getStyle(String name) {

        if (null != name) {
            switch (name) {
            case "default":
                return DateFormat.DEFAULT;
            case "short":
                return DateFormat.SHORT;
            case "medium":
                return DateFormat.MEDIUM;
            case "long":
                return DateFormat.LONG;
            case "full":
                return DateFormat.FULL;
            }
        }
        // PENDING(craigmcc) - i18n
        throw new ConverterException("Invalid style '" + name + '\'');
    }

    private static FormatStyle getFormatStyle(String name) {
        if (null != name) {
            switch (name) {
            case "default":
            case "medium":
                return FormatStyle.MEDIUM;
            case "short":
                return FormatStyle.SHORT;
            case "long":
                return FormatStyle.LONG;
            case "full":
                return FormatStyle.FULL;
            }
        }
        // PENDING(craigmcc) - i18n
        throw new ConverterException("Invalid style '" + name + '\'');

    }

    // ----------------------------------------------------- StateHolder Methods

    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!initialStateMarked()) {
            Object values[] = new Object[6];
            values[0] = dateStyle;
            values[1] = locale;
            values[2] = pattern;
            values[3] = timeStyle;
            values[4] = timeZone;
            values[5] = type;
            return values;
        }
        return null;

    }

    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (state != null) {
            Object values[] = (Object[]) state;
            dateStyle = (String) values[0];
            locale = (Locale) values[1];
            pattern = (String) values[2];
            timeStyle = (String) values[3];
            timeZone = (TimeZone) values[4];
            type = (String) values[5];
        }

    }

    private boolean transientFlag;

    @Override
    public boolean isTransient() {
        return transientFlag;
    }

    @Override
    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }

    private boolean initialState;

    @Override
    public void markInitialState() {
        initialState = true;
    }

    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    @Override
    public void clearInitialState() {
        initialState = false;
    }
}
