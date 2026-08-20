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

package com.sun.faces.facelets.tag.jstl.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

/**
 * The element of a row the build which restores a view reproduced, read live from the items the model holds now, and
 * stood in for where the model no longer holds it.
 *
 * <p>
 * The rows an iteration rendered are reproduced from the row count and, for a {@link Map}, the keys, never from the
 * elements themselves: each row reads its own element live, by position or by key, so that it edits what the model
 * holds now rather than a copy of what it held. An element the model no longer holds leaves its row without one, and
 * resolving that row's var to {@code null} fails the postback outright - on the first property of it any expression
 * reads, in whichever phase reads it first. Resolving it to an item which reads {@code null} and swallows what is
 * written to it lets the postback complete instead, the value submitted for a row whose element is gone going where
 * that element went. {@link ForEachHandler} reports the rows this happens to.
 */
final class RestoredItemValueExpression extends ValueExpression {

    private static final long serialVersionUID = 1L;

    /**
     * Stands in for the element of a row the items no longer hold: reads {@code null} for every property and swallows
     * every value written to one. A {@link Map} needs no {@code ELResolver} of its own to be read and written that
     * way, and an empty one is what an expression over a row without an element asks for.
     */
    private static final class VanishedItem extends AbstractMap<Object, Object> implements Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return Set.of();
        }

        @Override
        public Object put(Object key, Object value) {
            return null;
        }

        @Override
        public String toString() {
            return "";
        }
    }

    /**
     * Stands in for the entry of a row the map no longer holds: the key it was rendered for, which the row still
     * names, over a value which reads {@code null} and swallows what is written to it.
     */
    private static final class VanishedEntry implements Map.Entry<Object, Object>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Object key;

        private VanishedEntry(Object key) {
            this.key = key;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public Object setValue(Object value) {
            return null;
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private static final Object VANISHED_ITEM = new VanishedItem();

    /**
     * How a row reaches its element, which is what the expression reading it was built for. Items which have become
     * another one of these hold nothing that expression can read, however many elements they carry.
     */
    enum Access {

        /** By index, over a {@link List} or an array. */
        INDEXED,

        /** By iterating to the index, over any other {@link java.util.Collection}. */
        ITERATED,

        /** By key, over a {@link Map}. */
        KEYED;
    }

    private final ValueExpression items;
    private final ValueExpression element;
    private final Object position;
    private final Access access;
    private final int start;

    /**
     * @param items the items expression of the iteration this row belongs to.
     * @param element the expression which reads this row's element from those items.
     * @param position where this row reads its element, a {@link Map} key or an index.
     * @param access how the element expression reaches the element, which the position does not tell: a map may be
     * keyed by the very type an index has, and an index reads a list by another route than a set.
     * @param start the index the iteration begins at, which only {@link Access#ITERATED} walks past to reach its own.
     */
    RestoredItemValueExpression(ValueExpression items, ValueExpression element, Object position, Access access, int start) {
        this.items = items;
        this.element = element;
        this.position = position;
        this.access = access;
        this.start = start;
    }

    /**
     * Whether the items hold an element where this row reads one, which is what makes it the row that was rendered
     * rather than one whose element is gone. The items may also have become something else entirely than what they
     * were iterated as, which the expression reading them was not built for and no position reaches.
     *
     * @param context the {@link ELContext} to resolve the items against.
     * @return whether the items hold an element where this row reads one.
     */
    boolean supplies(ELContext context) {
        Object current = IterationBaseCache.getValue(context, items);

        if (current == null) {
            return false;
        }

        if (access == Access.KEYED) {
            if (!(current instanceof Map)) {
                return false;
            }

            try {
                return ((Map<?, ?>) current).containsKey(position);
            } catch (ClassCastException | NullPointerException e) {
                // A map is free to reject a key it cannot hold rather than answer for it, which is an answer as well.
                return false;
            }
        }

        int index = (Integer) position;

        if (index < 0) {
            return false;
        }

        if (access == Access.ITERATED) {
            // Walking to an element hands out the ones the range begins past as well - one of them where it begins at
            // the first - and then one per index, all of which have to be there for the walk to reach its own.
            return current instanceof Collection && Math.max(start, 1) + index <= ((Collection<?>) current).size();
        }

        if (current instanceof List) {
            return index < ((List<?>) current).size();
        }

        return current.getClass().isArray() && index < Array.getLength(current);
    }

    /**
     * The stand-in for the element of a row the given items no longer hold, for where the row reads it without an
     * expression of its own to read it through.
     *
     * @param position where the row reads its element, a {@link Map} key or an index.
     * @param keyed whether the position is a key rather than an index.
     * @return the stand-in for the element of a row the items no longer hold.
     */
    static Object vanishedItem(Object position, boolean keyed) {
        return keyed ? new VanishedEntry(position) : VANISHED_ITEM;
    }

    @Override
    public Object getValue(ELContext context) {
        if (supplies(context)) {
            return element.getValue(context);
        }

        context.setPropertyResolved(true);
        // A row of a map still names the entry it was rendered for, which is what the rest of the row reads the model
        // by: standing in for it without its key would read and write the model under no key at all.
        return access == Access.KEYED ? new VanishedEntry(position) : VANISHED_ITEM;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        if (supplies(context)) {
            element.setValue(context, value);
        } else {
            context.setPropertyResolved(true);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        return supplies(context) && element.isReadOnly(context);
    }

    @Override
    public Class<?> getType(ELContext context) {
        return supplies(context) ? element.getType(context) : Object.class;
    }

    @Override
    public Class<?> getExpectedType() {
        return Object.class;
    }

    @Override
    public String getExpressionString() {
        return element.getExpressionString();
    }

    @Override
    public boolean equals(Object object) {
        return element.equals(object);
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return false;
    }
}
