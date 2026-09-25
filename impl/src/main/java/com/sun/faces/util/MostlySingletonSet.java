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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Non-thread safe implementation of {@link Set} for use when most of the time there
 * is only one element, but sometimes there are more than one.
 *
 * <p>
 * Invariant: {@code inner} is {@code null} when empty, an immutable
 * {@link Collections#singleton(Object)} when holding exactly one element added to an empty set,
 * otherwise a {@link HashSet} (which may temporarily hold a single element).
 */
public class MostlySingletonSet<E> implements Set<E>, Serializable {

    private static final long serialVersionUID = 2818326518724772145L;

    private Set<E> inner;

    public MostlySingletonSet() {

    }

    @Override
    public boolean add(E e) {
        boolean modified = true;
        if (inner == null) {
            inner = Collections.singleton(e);
        } else {
            // If we need to transition from one to more-than-one
            if (inner.size() == 1) {
                Set<E> set = newHashSet(2);
                set.add(inner.iterator().next());
                inner = set;
            }
            modified = inner.add(e);
        }

        return modified;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = true;

        if (inner == null && c.size() == 1) {
            inner = Collections.singleton(c.iterator().next());
        }
        else if (inner == null) {
            // c is empty or has more than one element: go straight to a mutable set
            if (c.isEmpty()) {
                return false;
            }
            Set<E> set = newHashSet(c.size());
            set.addAll(c);
            inner = set;
        }
        else {
            // If we need to transition from one to more-than-one
            if (inner.size() == 1) {
                Set<E> set = newHashSet(1 + c.size());
                set.add(inner.iterator().next());
                inner = set;
            }
            modified = inner.addAll(c);
        }
        return modified;
    }

    @Override
    public void clear() {
        if (inner != null) {
            // If we need to transition from more-than-one to zero
            if (inner.size() > 1) {
                inner.clear();
            }
            inner = null;
        }
    }

    @Override
    public boolean remove(Object o) {
        boolean modified = false;

        if (inner != null) {
            if (inner.size() == 1) {
                // If we need to transition from one to zero
                modified = Objects.equals(inner.iterator().next(), o);
                if (modified) {
                    inner = null;
                }

            } else {
                modified = inner.remove(o);
                if (modified && inner.size() == 1) {
                    Set<E> set = Collections.singleton(inner.iterator().next());
                    inner.clear();
                    inner = set;
                }
            }

        }

        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        if (null != inner) {
            if (inner.size() == 1) {
                // May throw NPE per spec for Collection.removeAll()
                Iterator<?> incomingIter = c.iterator();
                E oneAndOnlyElement = inner.iterator().next();
                // Iterate over the incoming collection
                // looking for a member that is equal to our one and only
                // element.
                while (incomingIter.hasNext()) {
                    Object cur = incomingIter.next();
                    if (oneAndOnlyElement != null) {
                        // This handles null == cur.
                        if (modified = oneAndOnlyElement.equals(cur)) {
                            break;
                        }
                    } else {
                        // oneAndOnlyElement is null
                        if (modified = cur == null) {
                            break;
                        }
                    }
                }
                if (modified) {
                    inner = null;
                }
            } else {
                modified = inner.removeAll(c);
                if (modified && inner.isEmpty()) {
                    inner = null;
                }

            }
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;

        if (null != inner) {
            if (1 == inner.size()) {
                Iterator<?> incomingIter = c.iterator();
                E oneAndOnlyElement = inner.iterator().next();
                // Iterate over the incoming collection
                // looking for a member that is equal to our one and only
                // element. If found, we take no action, otherwise
                // we remove the oneAndOnlyElement.
                boolean found = false;
                while (incomingIter.hasNext()) {
                    Object cur = incomingIter.next();
                    if (oneAndOnlyElement != null) {
                        if (found = oneAndOnlyElement.equals(cur)) {
                            break;
                        }
                    } else {
                        if (found = cur == null) {
                            break;
                        }
                    }
                }
                if (modified = !found) {
                    inner = null;
                }

            } else {
                modified = inner.retainAll(c);
                if (modified && inner.isEmpty()) {
                    inner = null;
                }
            }
        }

        return modified;
    }

    @Override
    public boolean contains(Object o) {
        boolean contains = false;

        if (null != inner) {
            contains = inner.contains(o);
        }

        return contains;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return inner != null ? inner.containsAll(c) : c.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        boolean empty = true;

        if (inner != null) {
            empty = inner.isEmpty();
        }

        return empty;
    }

    @Override
    public int size() {
        int size = 0;
        if (inner != null) {
            size = inner.size();
        }
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Set)) {
            return false;
        }
        Set<?> other = (Set<?>) obj;
        return inner != null ? inner.equals(other) : other.isEmpty();
    }

    @Override
    public int hashCode() {
        // Per Set contract: sum of the element hash codes
        return inner != null ? inner.hashCode() : 0;
    }

    @Override
    public String toString() {
        return inner != null ? inner.toString() : "empty";
    }

    @Override
    public Iterator<E> iterator() {
        return inner != null ? new InnerIterator() : Collections.emptyIterator();
    }

    @Override
    public Object[] toArray() {
        return inner != null ? inner.toArray() : new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (inner != null) {
            return inner.toArray(a);
        }
        if (a.length > 0) {
            a[0] = null; // per Collection.toArray(T[]) contract
        }
        return a;
    }

    /**
     * Creates the mutable set used once there is more than one element.
     * <p>
     * Mostly-singleton: keep the table small (at least 8 buckets) with load factor 1.0,
     * so that {@code expectedSize} elements fit without resizing.
     */
    private static <E> Set<E> newHashSet(int expectedSize) {
        return new HashSet<>(Math.max(8, expectedSize), 1.0f);
    }

    /**
     * Supports {@link Iterator#remove()} also on the immutable singleton,
     * and restores {@code inner == null} when the set becomes empty.
     */
    private final class InnerIterator implements Iterator<E> {

        private final Set<E> source = inner;
        private final Iterator<E> delegate = source.iterator();
        private boolean canRemove;

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public E next() {
            E next = delegate.next(); // throws NoSuchElementException when exhausted
            canRemove = true;
            return next;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            canRemove = false;

            if (source instanceof HashSet) {
                delegate.remove();
                if (source.isEmpty() && inner == source) {
                    inner = null;
                }
            }
            else if (inner == source) {
                // Immutable singleton: removing its only element empties the set
                inner = null;
            }
        }
    }

}