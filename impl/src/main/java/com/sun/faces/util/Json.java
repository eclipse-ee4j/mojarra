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

package com.sun.faces.util;

import static com.sun.faces.util.Json.Option.SKIP_NULL_VALUES;
import static com.sun.faces.util.Json.Option.USE_RFC1123_DATE;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.EnumSet.copyOf;
import static java.util.EnumSet.noneOf;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.json.stream.JsonGenerator;

/**
 * <p>
 * Generic JSON encoder using jakarta.json API.
 * <p>
 * This supports the standard types {@link Boolean}, {@link Number}, {@link Character}, {@link CharSequence},
 * {@link Date}, {@link LocalDate} and {@link Instant}. If the given object type does not match any of them, then it
 * will attempt to inspect the object as a JavaBean using the {@link Introspector}, whereby the public properties
 * (public getters) will be encoded as a JS object. It also supports arrays, {@link Collection}s and {@link Map}s of
 * them, even nested ones. The dates are formatted as ISO8601 instant via {@link DateTimeFormatter#ISO_INSTANT}, so you
 * can if necessary just pass the value straight to <code>new Date(value)</code> in JavaScript.
 * <p>
 * Below encoding options are available:
 * <ul>
 * <li>{@link Option#SKIP_NULL_VALUES}: skip null values in arrays, collections, maps and beans. This may reduce an
 * unnecessarily bloated JSON object.
 * <li>{@link Option#USE_RFC1123_DATE}: format dates as RFC1123 via {@link DateTimeFormatter#RFC_1123_DATE_TIME}. This
 * may improve compatibility with older web browsers.
 * </ul>
 *
 * @author Bauke Scholtz
 * @since 2.3
 */
public class Json {

    private static final String ERROR_INVALID_BEAN = "Cannot introspect object of type '%s' as bean.";
    private static final String ERROR_INVALID_GETTER = "Cannot invoke getter of property '%s' of bean '%s'.";

    public enum Option {

        /**
         * Skip null values in arrays, collections, maps and beans. This may reduce an unnecessarily bloated JSON object.
         */
        SKIP_NULL_VALUES,

        /**
         * Format dates as RFC1123 via {@link DateTimeFormatter#RFC_1123_DATE_TIME}. This may improve compatibility with older
         * web browsers.
         */
        USE_RFC1123_DATE;

    }

    /**
     * Encodes the given object as JSON and returns a string in JSON format. The encoded object will be available as
     * <code>data</code> property of the JS object in the returned JSON string.
     *
     * @param object The object to be encoded as JSON.
     * @param options The encoding options.
     * @return The JSON-encoded representation of the given object.
     * @throws IllegalArgumentException When given object or one of its properties cannot be inspected as a JavaBean.
     */
    public static String encode(Object object, Option... options) {
        StringWriter writer = new StringWriter();
        encode(object, writer, options);
        return writer.toString();
    }

    /**
     * Encodes the given object as JSON while streaming the string in JSON format to the given writer. The encoded object
     * will be available as <code>data</code> property of the JS object in the returned JSON string.
     *
     * @param object The object to be encoded as JSON.
     * @param writer The writer to stream the encoded output to.
     * @param options The encoding options.
     * @throws IllegalArgumentException When given object or one of its properties cannot be inspected as a JavaBean.
     */
    public static void encode(Object object, Writer writer, Option... options) {
        try (JsonGenerator generator = jakarta.json.Json.createGenerator(writer)) {
            generator.writeStartObject();
            encode("data", object, generator, options.length == 0 ? noneOf(Option.class) : copyOf(asList(options)));
            generator.writeEnd();
        }
    }

    private static void encode(String name, Object object, JsonGenerator generator, EnumSet<Option> options) {
        if (object == null) {
            encodeNull(name, generator);
        } else if (object instanceof Boolean) {
            encodeBoolean(name, (Boolean) object, generator);
        } else if (object instanceof BigDecimal) {
            encodeBigDecimal(name, (BigDecimal) object, generator);
        } else if (object instanceof Double) {
            encodeDouble(name, (Double) object, generator);
        } else if (object instanceof BigInteger) {
            encodeBigInteger(name, (BigInteger) object, generator);
        } else if (object instanceof Integer) {
            encodeInteger(name, (Integer) object, generator);
        } else if (object instanceof Number) {
            encodeLong(name, ((Number) object).longValue(), generator);
        } else if (object instanceof Character) {
            encodeString(name, ((Character) object).toString(), generator);
        } else if (object instanceof CharSequence) {
            encodeString(name, ((CharSequence) object).toString(), generator);
        } else if (object instanceof Date) {
            encodeInstant(name, ((Date) object).toInstant().atZone(UTC).toInstant(), generator, options);
        } else if (object instanceof LocalDate) {
            encodeInstant(name, ((LocalDate) object).atStartOfDay(UTC).toInstant(), generator, options);
        } else if (object instanceof Instant) {
            encodeInstant(name, (Instant) object, generator, options);
        } else if (object.getClass().isArray()) {
            encodeArray(name, object, generator, options);
        } else if (object instanceof Collection<?>) {
            encodeCollection(name, (Collection<?>) object, generator, options);
        } else if (object instanceof Map<?, ?>) {
            encodeMap(name, (Map<?, ?>) object, generator, options);
        } else if (object instanceof Class<?>) {
            encodeString(name, ((Class<?>) object).getName(), generator);
        } else if (object instanceof Record) {
            encodeRecord(name, (Record) object, generator, options);
        } else {
            encodeBean(name, object, generator, options);
        }
    }

    private static void encodeNull(String name, JsonGenerator generator) {
        if (name == null) {
            generator.writeNull();
        } else {
            generator.writeNull(name);
        }
    }

    private static void encodeBoolean(String name, Boolean value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeBigDecimal(String name, BigDecimal value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeDouble(String name, double value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeBigInteger(String name, BigInteger value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeInteger(String name, int value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeLong(String name, long value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeString(String name, String value, JsonGenerator generator) {
        if (name == null) {
            generator.write(value);
        } else {
            generator.write(name, value);
        }
    }

    private static void encodeInstant(String name, Instant value, JsonGenerator generator, EnumSet<Option> options) {
        encodeString(name, (options.contains(USE_RFC1123_DATE) ? RFC_1123_DATE_TIME : ISO_INSTANT).format(value), generator);
    }

    private static void encodeArray(String name, Object array, JsonGenerator generator, EnumSet<Option> options) {
        if (name == null) {
            generator.writeStartArray();
        } else {
            generator.writeStartArray(name);
        }

        boolean skipNullValues = options.contains(SKIP_NULL_VALUES);

        for (int i = 0; i < Array.getLength(array); i++) {
            Object value = Array.get(array, i);

            if (!(value == null && skipNullValues)) {
                encode(null, value, generator, options);
            }
        }

        generator.writeEnd();
    }

    private static void encodeCollection(String name, Collection<?> collection, JsonGenerator generator, EnumSet<Option> options) {
        if (name == null) {
            generator.writeStartArray();
        } else {
            generator.writeStartArray(name);
        }

        boolean skipNullValues = options.contains(SKIP_NULL_VALUES);

        for (Object value : collection) {
            if (!(value == null && skipNullValues)) {
                encode(null, value, generator, options);
            }
        }

        generator.writeEnd();
    }

    private static void encodeMap(String name, Map<?, ?> map, JsonGenerator generator, EnumSet<Option> options) {
        if (name == null) {
            generator.writeStartObject();
        } else {
            generator.writeStartObject(name);
        }

        boolean skipNullValues = options.contains(SKIP_NULL_VALUES);

        for (Entry<?, ?> entry : map.entrySet()) {
            Object value = entry.getValue();

            if (!(value == null && skipNullValues)) {
                encode(String.valueOf(entry.getKey()), value, generator, options);
            }
        }

        generator.writeEnd();
    }

    private static void encodeRecord(String name, Record instance, JsonGenerator generator, EnumSet<Option> options) {
        if (name == null) {
            generator.writeStartObject();
        } else {
            generator.writeStartObject(name);
        }

        boolean skipNullValues = options.contains(SKIP_NULL_VALUES);

        for (RecordComponent component : instance.getClass().getRecordComponents()) {
            Object value;

            try {
                value = component.getAccessor().invoke(instance);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_GETTER, component.getName(), instance.getClass()), e);
            }

            if (!(value == null && skipNullValues)) {
                encode(component.getName(), value, generator, options);
            }
        }

        generator.writeEnd();

    }
    
    private static void encodeBean(String name, Object bean, JsonGenerator generator, EnumSet<Option> options) {
        BeanInfo beanInfo;

        try {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException(String.format(ERROR_INVALID_BEAN, bean.getClass()), e);
        }

        if (name == null) {
            generator.writeStartObject();
        } else {
            generator.writeStartObject(name);
        }

        boolean skipNullValues = options.contains(SKIP_NULL_VALUES);

        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
            if (property.getReadMethod() == null || "class".equals(property.getName())) {
                continue;
            }

            Object value;

            try {
                value = property.getReadMethod().invoke(bean);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(ERROR_INVALID_GETTER, property.getName(), bean.getClass()), e);
            }

            if (!(value == null && skipNullValues)) {
                encode(property.getName(), value, generator, options);
            }
        }

        generator.writeEnd();
    }

}
