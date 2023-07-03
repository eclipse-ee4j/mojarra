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

import jakarta.faces.model.SelectItem;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author asmirnov@exadel.com
 *
 */
public class CollectionsUtils {

    private CollectionsUtils() {
        // this class contains static methods only.
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... a) {
        return new HashSet<>(asList(a));
    }

    @SafeVarargs
    public static <T> T[] ar(T... ts) {
        return ts;
    }

    public static <T> T[] ar() {
        return null;
    }

    public static <T, V> ConstMap<T, V> map() {
        return new ConstMap<>();
    }

    public static class ConstMap<T, V> extends HashMap<T, V> {

        private static final long serialVersionUID = 7233295794116070299L;

        public ConstMap() {
            super(50, 1.0F);
        }

        public ConstMap<T, V> add(T key, V value) {
            put(key, value);
            return this;
        }

        public Map<T, V> fix() {
            return unmodifiableMap(this);
        }
    }

    /**
     * @return an unmodifiable Iterator over the passed typed array
     */
    public static <T> Iterator<T> asIterator(T[] items) {
        return unmodifiableIterator(Stream.of(items).iterator());
    }

    /**
     * @return an unmodifiable Iterator over the passed array of SelectItem
     */
    public static <T extends SelectItem> Iterator<T> asIterator(T[] items) {
        return unmodifiableIterator(Stream.of(items).iterator());
    }

    /**
     * @return an Iterator over the passed Enumeration with no remove support
     */
    public static <T> Iterator<T> unmodifiableIterator(Enumeration<T> enumeration) {
        return unmodifiableIterator(enumeration.asIterator());
    }

    /**
     * @return an Iterator over the passed Iterator with no remove support
     */
    public static <T> Iterator<T> unmodifiableIterator(Iterator<T> iterator) {
        return new UnmodifiableIterator<>(iterator);
    }

    public static class UnmodifiableIterator<T> implements Iterator<T> {

        private final Iterator<T> iterator;

        public UnmodifiableIterator(Iterator<T> iterator) {this.iterator = iterator;}

        @Override public boolean hasNext() {return iterator.hasNext();}
        @Override public T next() {return iterator.next();}
        @Override public void remove() {throw new UnsupportedOperationException();}
    }

}
