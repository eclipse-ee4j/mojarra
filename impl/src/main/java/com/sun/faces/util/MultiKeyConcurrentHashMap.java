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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This code is based off the source for ConcurrentHashMap from JDK 5 with the ability of mapping multiple keys to a
 * single value.
 *
 * <ul>
 * <li>This Map implemenation does not support iteration through keys and/or values.
 * <li>
 * <li>This Map implementation is <em>NOT</em> Serialziable.
 * <li>
 * <li>This cannot be cast as a general Map implementation.</li>
 * </ul>
 */
public class MultiKeyConcurrentHashMap<K, V> {

    /*
     * The basic strategy is to subdivide the table among Segments, each of which itself is a concurrently readable hash
     * table.
     */

    /* ---------------- Constants -------------- */

    /**
     * The default initial number of table slots for this table. Used when not otherwise specified in constructor.
     */
    static int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30 to ensure that entries are indexible using ints.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The default load factor for this table. Used when not otherwise specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The default number of concurrency control segments.
     */
    static final int DEFAULT_SEGMENTS = 16;

    /**
     * The maximum number of segments to allow; used to bound constructor arguments.
     */
    static final int MAX_SEGMENTS = 1 << 16; // slightly conservative

    /**
     * Number of unsynchronized retries in size and containsValue methods before resorting to locking. This is used to avoid
     * unbounded retries if tables undergo continuous modification which would make it impossible to obtain an accurate
     * result.
     */
    static final int RETRIES_BEFORE_LOCK = 2;

    /* ---------------- Fields -------------- */

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the segment.
     */
    final int segmentMask;

    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;

    /**
     * The segments, each of which is a specialized hash table
     */
    final Segment[] segments;

    /* ---------------- Small Utilities -------------- */

    /**
     * Returns a hash code for non-null Objects. Uses the same hash code spreader as most other java.util hash tables.
     *
     * @return the hash code
     */
    static int hash(Object x1, Object x2, Object x3, Object x4) {
        int h = 0;
        // xor one or Object hashcodes
        h ^= x1.hashCode();
        if (x2 != null) {
            h ^= x2.hashCode();
        }
        if (x3 != null) {
            h ^= x3.hashCode();
        }
        if (x4 != null) {
            h ^= x4.hashCode();
        }
        // the following is the standard hashing algorithm included
        // in the original source
        h += ~(h << 9);
        h ^= h >>> 14;
        h += h << 4;
        h ^= h >>> 10;
        return h;
    }

    /**
     * Returns the segment that should be used for key with given hash
     *
     * @param hash the hash code for the key
     *
     * @return the segment
     */
    final Segment<K, V> segmentFor(int hash) {
        // noinspection unchecked
        return segments[hash >>> segmentShift & segmentMask];
    }

    /* ---------------- Inner Classes -------------- */

    /**
     * ConcurrentHashMap list entry. Note that this is never exported out as a user-visible Map.Entry.
     * 
     * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an unsynchronized reader to
     * see null instead of initial value when read via a data race. Although a reordering leading to this is not likely to
     * ever actually occur, the Segment.readValueUnderLock method is used as a backup in case a null (pre-initialized) value
     * is ever seen in an unsynchronized access method.
     */
    static final class HashEntry<K, V> {
        final K key1;
        final K key2;
        final K key3;
        final K key4;
        final int hash;
        volatile V value;
        final HashEntry<K, V> next;

        HashEntry(K key1, K key2, K key3, K key4, int hash, HashEntry<K, V> next, V value) {
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.key4 = key4;
            this.hash = hash;
            this.next = next;
            this.value = value;
        }
    }

    /**
     * Segments are specialized versions of hash tables. This subclasses from ReentrantLock opportunistically, just to
     * simplify some locking and avoid separate construction.
     */
    static final class Segment<K, V> extends ReentrantLock {
        /*
         * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be read without locking.
         * Next fields of nodes are immutable (final). All list additions are performed at the front of each bin. This makes it
         * easy to check changes, and also fast to traverse. When nodes would otherwise be changed, new nodes are created to
         * replace them. This works well for hash tables since the bin lists tend to be short. (The average length is less than
         * two for the default load factor threshold.)
         *
         * Read operations can thus proceed without locking, but rely on selected uses of volatiles to ensure that completed
         * write operations performed by other threads are noticed. For most purposes, the "count" field, tracking the number of
         * elements, serves as that volatile variable ensuring visibility. This is convenient because this field needs to be
         * read in many read operations anyway:
         *
         * - All (unsynchronized) read operations must first read the "count" field, and should not look at table entries if it
         * is 0.
         *
         * - All (synchronized) write operations should write to the "count" field after structurally changing any bin. The
         * operations must not take any action that could even momentarily cause a concurrent read operation to see inconsistent
         * data. This is made easier by the nature of the read operations in Map. For example, no operation can reveal that the
         * table has grown but the threshold has not yet been updated, so there are no atomicity requirements for this with
         * respect to reads.
         *
         * As a guide, all critical volatile reads and writes to the count field are marked in code comments.
         */

        /**
         *
         */
        private static final long serialVersionUID = -5546647604753877171L;

        /**
         * The number of elements in this segment's region.
         */
        volatile int count;

        /**
         * Number of updates that alter the size of the table. This is used during bulk-read methods to make sure they see a
         * consistent snapshot: If modCounts change during a traversal of segments computing size or checking containsValue,
         * then we might have an inconsistent view of state so (usually) must retry.
         */
        int modCount;

        /**
         * The table is rehashed when its size exceeds this threshold. (The value of this field is always (int)(capacity *
         * loadFactor).)
         */
        int threshold;

        /**
         * The per-segment table. Declared as a raw type, casted to HashEntry<K,V> on each use.
         */
        @SuppressWarnings({ "NonSerializableFieldInSerializableClass" })
        volatile HashEntry[] table;

        /**
         * The load factor for the hash table. Even though this value is same for all segments, it is replicated to avoid
         * needing links to outer object.
         *
         * @serial
         */
        final float loadFactor;

        Segment(int initialCapacity, float lf) {
            loadFactor = lf;
            setTable(new HashEntry[initialCapacity]);
        }

        /**
         * Set table to new HashEntry array. Call only while holding lock or in constructor.
         */
        void setTable(HashEntry[] newTable) {
            threshold = (int) (newTable.length * loadFactor);
            table = newTable;
        }

        /**
         * Return properly casted first entry of bin for given hash
         */
        HashEntry<K, V> getFirst(int hash) {
            HashEntry[] tab = table;
            // noinspection unchecked
            return tab[hash & tab.length - 1];
        }

        /**
         * Read value field of an entry under lock. Called if value field ever appears to be null. This is possible only if a
         * compiler happens to reorder a HashEntry initialization with its table assignment, which is legal under memory model
         * but is not known to ever occur.
         */
        V readValueUnderLock(HashEntry<K, V> e) {
            lock();
            try {
                return e.value;
            } finally {
                unlock();
            }
        }

        /* Specialized implementations of map methods */

        V get(Object key1, Object key2, Object key3, Object key4, int hash) {
            if (count != 0) { // read-volatile
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && key1.equals(e.key1) && (key2 == null && e.key2 == null || key2 != null && key2.equals(e.key2))
                            && (key3 == null && e.key3 == null || key3 != null && key3.equals(e.key3))
                            && (key4 == null && e.key4 == null || key4 != null && key4.equals(e.key4))) {
                        V v = e.value;
                        if (v != null) {
                            return v;
                        }
                        return readValueUnderLock(e); // recheck
                    }
                    e = e.next;
                }
            }
            return null;
        }

        boolean containsKey(Object key1, Object key2, Object key3, Object key4, int hash) {
            if (count != 0) { // read-volatile
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && key1.equals(e.key1) && (key2 == null && e.key2 == null || key2 != null && key2.equals(e.key2))
                            && (key3 == null && e.key3 == null || key3 != null && key3.equals(e.key3))
                            && (key4 == null && e.key4 == null || key4 != null && key4.equals(e.key4))) {
                        return true;
                    }
                    e = e.next;
                }
            }
            return false;
        }

        boolean containsValue(Object value) {
            if (count != 0) { // read-volatile
                HashEntry[] tab = table;
                int len = tab.length;
                for (int i = 0; i < len; i++) {
                    for (// noinspection unchecked
                            HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        V v = e.value;
                        if (v == null) // recheck
                        {
                            v = readValueUnderLock(e);
                        }
                        if (value.equals(v)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        boolean replace(K key1, K key2, K key3, K key4, int hash, V oldValue, V newValue) {
            lock();
            try {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null && (e.hash != hash || key1 != null && !key1.equals(e.key1) || key2 != null && !key2.equals(e.key2)
                        || key3 != null && !key3.equals(e.key3) || key4 != null && !key4.equals(e.key4))) {
                    e = e.next;
                }

                boolean replaced = false;
                if (e != null && oldValue.equals(e.value)) {
                    replaced = true;
                    e.value = newValue;
                }
                return replaced;
            } finally {
                unlock();
            }
        }

        V replace(K key1, K key2, K key3, K key4, int hash, V newValue) {
            lock();
            try {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null && (e.hash != hash || key1 != null && !key1.equals(e.key1) || key2 != null && !key2.equals(e.key2)
                        || key3 != null && !key3.equals(e.key3) || key4 != null && !key4.equals(e.key4))) {
                    e = e.next;
                }

                V oldValue = null;
                if (e != null) {
                    oldValue = e.value;
                    e.value = newValue;
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        V put(K key1, K key2, K key3, K key4, int hash, V value, boolean onlyIfAbsent) {
            lock();
            try {
                int c = count;
                if (c++ > threshold) // ensure capacity
                {
                    rehash();
                }
                HashEntry[] tab = table;
                int index = hash & tab.length - 1;
                // noinspection unchecked
                HashEntry<K, V> first = tab[index];
                HashEntry<K, V> e = first;
                while (e != null && (e.hash != hash || key1 != null && !key1.equals(e.key1) || key2 != null && !key2.equals(e.key2)
                        || key3 != null && !key3.equals(e.key3) || key4 != null && !key4.equals(e.key4))) {
                    e = e.next;
                }

                V oldValue;
                if (e != null) {
                    oldValue = e.value;
                    if (!onlyIfAbsent) {
                        e.value = value;
                    }
                } else {
                    oldValue = null;
                    ++modCount;
                    tab[index] = new HashEntry<>(key1, key2, key3, key4, hash, first, value);
                    count = c; // write-volatile
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        void rehash() {
            HashEntry[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY) {
                return;
            }

            /*
             * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the elements from each bin
             * must either stay at same index, or move with a power of two offset. We eliminate unnecessary node creation by
             * catching cases where old nodes can be reused because their next fields won't change. Statistically, at the default
             * threshold, only about one-sixth of them need cloning when a table doubles. The nodes they replace will be garbage
             * collectable as soon as they are no longer referenced by any reader thread that may be in the midst of traversing
             * table right now.
             */

            HashEntry[] newTable = new HashEntry[oldCapacity << 1];
            threshold = (int) (newTable.length * loadFactor);
            int sizeMask = newTable.length - 1;
            for (int i = 0; i < oldCapacity; i++) {
                // We need to guarantee that any existing reads of old Map can
                // proceed. So we cannot yet null out each bin.
                // noinspection unchecked
                HashEntry<K, V> e = oldTable[i];

                if (e != null) {
                    HashEntry<K, V> next = e.next;
                    int idx = e.hash & sizeMask;

                    // Single node on list
                    if (next == null) {
                        newTable[idx] = e;
                    } else {
                        // Reuse trailing consecutive sequence at same slot
                        HashEntry<K, V> lastRun = e;
                        int lastIdx = idx;
                        for (HashEntry<K, V> last = next; last != null; last = last.next) {
                            int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;

                        // Clone all remaining nodes
                        for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                            int k = p.hash & sizeMask;
                            // noinspection unchecked
                            HashEntry<K, V> n = newTable[k];
                            newTable[k] = new HashEntry<>(p.key1, p.key2, p.key3, p.key4, p.hash, n, p.value);
                        }
                    }
                }
            }
            table = newTable;
        }

        /**
         * Remove; match on key only if value null, else match both.
         */
        V remove(Object key1, Object key2, Object key3, Object key4, int hash, Object value) {
            lock();
            try {
                int c = count - 1;
                HashEntry[] tab = table;
                int index = hash & tab.length - 1;
                // noinspection unchecked
                HashEntry<K, V> first = tab[index];
                HashEntry<K, V> e = first;
                while (e != null && (e.hash != hash || key1 != null && !key1.equals(e.key1) || key2 != null && !key2.equals(e.key2)
                        || key3 != null && !key3.equals(e.key3) || key4 != null && !key4.equals(e.key4))) {
                    e = e.next;
                }

                V oldValue = null;
                if (e != null) {
                    V v = e.value;
                    if (value == null || value.equals(v)) {
                        oldValue = v;
                        // All entries following removed node can stay
                        // in list, but all preceding ones need to be
                        // cloned.
                        ++modCount;
                        HashEntry<K, V> newFirst = e.next;
                        for (HashEntry<K, V> p = first; p != e; p = p.next) {
                            newFirst = new HashEntry<>(p.key1, p.key2, p.key3, p.key4, p.hash, newFirst, p.value);
                        }
                        tab[index] = newFirst;
                        count = c; // write-volatile
                    }
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        void clear() {
            if (count != 0) {
                lock();
                try {
                    HashEntry[] tab = table;
                    for (int i = 0; i < tab.length; i++) {
                        tab[i] = null;
                    }
                    ++modCount;
                    count = 0; // write-volatile
                } finally {
                    unlock();
                }
            }
        }
    }

    /* ---------------- Public operations -------------- */

    /**
     * Creates a new, empty map with the specified initial capacity, load factor, and concurrency level.
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate this many
     * elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed when the average
     * number of elements per bin exceeds this threshold.
     * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation performs internal
     * sizing to try to accommodate this many threads.
     *
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor or concurrencyLevel are
     * nonpositive.
     */
    public MultiKeyConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0) {
            throw new IllegalArgumentException();
        }

        if (concurrencyLevel > MAX_SEGMENTS) {
            concurrencyLevel = MAX_SEGMENTS;
        }

        // Find power-of-two sizes best matching arguments
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        this.segments = new Segment[ssize];

        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity) {
            ++c;
        }
        int cap = 1;
        while (cap < c) {
            cap <<= 1;
        }

        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = new Segment<>(cap, loadFactor);
        }
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with default load factor and concurrencyLevel.
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate this many
     * elements.
     *
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public MultiKeyConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
     * Creates a new, empty map with a default initial capacity, load factor, and concurrencyLevel.
     */
    public MultiKeyConcurrentHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        final Segment[] segments = this.segments;
        /*
         * We keep track of per-segment modCounts to avoid ABA problems in which an element in one segment was added and in
         * another removed during traversal, in which case the table was never actually empty at any point. Note the similar use
         * of modCounts in the size() and containsValue() methods, which are the only other methods also susceptible to ABA
         * problems.
         */
        int[] mc = new int[segments.length];
        int mcsum = 0;
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0) {
                return false;
            } else {
                mcsum += mc[i] = segments[i].modCount;
            }
        }
        // If mcsum happens to be zero, then we know we got a snapshot
        // before any modifications at all were made. This is
        // probably common enough to bother tracking.
        if (mcsum != 0) {
            for (int i = 0; i < segments.length; ++i) {
                if (segments[i].count != 0 || mc[i] != segments[i].modCount) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        final Segment[] segments = this.segments;
        long sum = 0;
        long check = 0;
        int[] mc = new int[segments.length];
        // Try a few times to get accurate count. On failure due to
        // continuous async changes in table, resort to locking.
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for (int i = 0; i < segments.length; ++i) {
                sum += segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
            }
            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    check += segments[i].count;
                    if (mc[i] != segments[i].modCount) {
                        check = -1; // force retry
                        break;
                    }
                }
            }
            if (check == sum) {
                break;
            }
        }
        if (check != sum) { // Resort to locking all segments
            sum = 0;
            for (int i = 0; i < segments.length; ++i) {
                segments[i].lock();
            }
            for (int i = 0; i < segments.length; ++i) {
                sum += segments[i].count;
            }
            for (int i = 0; i < segments.length; ++i) {
                segments[i].unlock();
            }
        }
        if (sum > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) sum;
        }
    }

    /**
     * Returns the value to which the specified key is mapped in this table.
     *
     * @param key a key in the table.
     *
     * @return the value to which the key is mapped in this table; <code>null</code> if the key is not mapped to any value in
     * this table.
     *
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public V get(Object key) {
        int hash = hash(key, null, null, null);
        return segmentFor(hash).get(key, null, null, null, hash);
    }

    /**
     * @see #get(Object)
     */
    public V get(Object key1, Object key2) {
        int hash = hash(key1, key2, null, null);
        return segmentFor(hash).get(key1, key2, null, null, hash);
    }

    /**
     * @see #get(Object)
     */
    public V get(Object key1, Object key2, Object key3) {
        int hash = hash(key1, key2, key3, null);
        return segmentFor(hash).get(key1, key2, key3, null, hash);
    }

    /**
     * @see #get(Object)
     */
    public V get(Object key1, Object key2, Object key3, Object key4) {
        int hash = hash(key1, key2, key3, key4);
        return segmentFor(hash).get(key1, key2, key3, key4, hash);
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param key possible key.
     *
     * @return <code>true</code> if and only if the specified object is a key in this table, as determined by the
     * <code>equals</code> method; <code>false</code> otherwise.
     *
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public boolean containsKey(Object key) {
        int hash = hash(key, null, null, null);
        return segmentFor(hash).containsKey(key, null, null, null, hash);
    }

    /**
     * @see #containsKey(Object)
     */
    public boolean containsKey(Object key1, Object key2) {
        int hash = hash(key1, key2, null, null);
        return segmentFor(hash).containsKey(key1, key2, null, null, hash);
    }

    /**
     * @see #containsKey(Object)
     */
    public boolean containsKey(Object key1, Object key2, Object key3) {
        int hash = hash(key1, key2, key3, null);
        return segmentFor(hash).containsKey(key1, key2, key3, null, hash);
    }

    /**
     * @see #containsKey(Object)
     */
    public boolean containsKey(Object key1, Object key2, Object key3, Object key4) {
        int hash = hash(key1, key2, key3, key4);
        return segmentFor(hash).containsKey(key1, key2, key3, key4, hash);
    }

    /**
     * Returns <code>true</code> if this map maps one or more keys to the specified value. Note: This method requires a full
     * internal traversal of the hash table, and so is much slower than method <code>containsKey</code>.
     *
     * @param value value whose presence in this map is to be tested.
     *
     * @return <code>true</code> if this map maps one or more keys to the specified value.
     *
     * @throws NullPointerException if the value is <code>null</code>.
     */
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        // See explanation of modCount use above

        final Segment[] segments = this.segments;
        int[] mc = new int[segments.length];

        // Try a few times without locking
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            int mcsum = 0;
            for (int i = 0; i < segments.length; ++i) {
                mcsum += mc[i] = segments[i].modCount;
                if (segments[i].containsValue(value)) {
                    return true;
                }
            }
            boolean cleanSweep = true;
            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    if (mc[i] != segments[i].modCount) {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if (cleanSweep) {
                return false;
            }
        }
        // Resort to locking all segments
        for (int i = 0; i < segments.length; ++i) {
            segments[i].lock();
        }
        boolean found = false;
        try {
            for (int i = 0; i < segments.length; ++i) {
                if (segments[i].containsValue(value)) {
                    found = true;
                    break;
                }
            }
        } finally {
            for (int i = 0; i < segments.length; ++i) {
                segments[i].unlock();
            }
        }
        return found;
    }

    /**
     * Legacy method testing if some key maps into the specified value in this table. This method is identical in
     * functionality to {@link #containsValue}, and exists solely to ensure full compatibility with class
     * {@link java.util.Hashtable}, which supported this method prior to introduction of the Java Collections framework.
     *
     * @param value a value to search for.
     *
     * @return <code>true</code> if and only if some key maps to the <code>value</code> argument in this table as determined by the
     * <code>equals</code> method; <code>false</code> otherwise.
     *
     * @throws NullPointerException if the value is <code>null</code>.
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * Maps the specified <code>key</code> to the specified <code>value</code> in this table. Neither the key nor the value can be
     * <code>null</code>.
     * 
     * <p>
     * The value can be retrieved by calling the <code>get</code> method with a key that is equal to the original key.
     *
     * @param key the table key.
     * @param value the value.
     *
     * @return the previous value of the specified key in this table, or <code>null</code> if it did not have one.
     *
     * @throws NullPointerException if the key or value is <code>null</code>.
     */
    public V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key, null, null, null);
        return segmentFor(hash).put(key, null, null, null, hash, value, false);
    }

    /**
     * @see #put(Object, Object)
     */
    public V put(K key1, K key2, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key1, key2, null, null);
        return segmentFor(hash).put(key1, key2, null, null, hash, value, false);
    }

    /**
     * @see #put(Object, Object)
     */
    public V put(K key1, K key2, K key3, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key1, key2, key3, null);
        return segmentFor(hash).put(key1, key2, key3, null, hash, value, false);
    }

    /**
     * @see #put(Object, Object)
     */
    public V put(K key1, K key2, K key3, K key4, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key1, key2, key3, key4);
        return segmentFor(hash).put(key1, key2, key3, key4, hash, value, false);
    }

    /**
     * If the specified key is not already associated with a value, associate it with the given value. This is equivalent to
     *
     * <pre>
     * if (!map.containsKey(key))
     *     return map.put(key, value);
     * else
     *     return map.get(key);
     * </pre>
     *
     * Except that the action is performed atomically.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @return previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     *
     * @throws NullPointerException if the specified key or value is <code>null</code>.
     */
    public V putIfAbsent(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key, null, null, null);
        return segmentFor(hash).put(key, null, null, null, hash, value, true);
    }

    /**
     * @see #putIfAbsent(Object, Object)
     */
    public V putIfAbsent(K key1, K key2, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key1, key2, null, null);
        return segmentFor(hash).put(key1, key2, null, null, hash, value, true);
    }

    /**
     * @see #putIfAbsent(Object, Object)
     */
    public V putIfAbsent(K key1, K key2, K key3, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key1, key2, key3, null);
        return segmentFor(hash).put(key1, key2, key3, null, hash, value, true);
    }

    /**
     * @see #putIfAbsent(Object, Object)
     */
    public V putIfAbsent(K key1, K key2, K key3, K key4, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key1, key2, key3, key4);
        return segmentFor(hash).put(key1, key2, key3, key4, hash, value, true);
    }

    /**
     * @see Map#remove(Object)
     */
    public V remove(K key) {
        int hash = hash(key, null, null, null);
        return segmentFor(hash).remove(key, null, null, null, hash, null);
    }

    /**
     * @see Map#remove(Object)
     */
    public V remove(K key1, K key2) {
        int hash = hash(key1, key2, null, null);
        return segmentFor(hash).remove(key1, key2, null, null, hash, null);
    }

    /**
     * @see Map#remove(Object)
     */
    public V remove(K key1, K key2, K key3) {
        int hash = hash(key1, key2, key3, null);
        return segmentFor(hash).remove(key1, key2, null, null, hash, null);
    }

    /**
     * @see Map#remove(Object)
     */
    public V remove(K key1, K key2, K key3, K key4) {
        // we don't have multiple versions of remove here because
        // erasure would cause a collision with boolean remove(Object, Object)
        int hash = hash(key1, key2, key3, key4);
        return segmentFor(hash).remove(key1, key2, key3, key4, hash, null);
    }

    /**
     * Replace entry for key only if currently mapped to given value. Acts as
     *
     * <pre>
     * if (map.get(key).equals(oldValue)) {
     *     map.put(key, newValue);
     *     return true;
     * } else
     *     return false;
     * </pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated.
     * @param oldValue value expected to be associated with the specified key.
     * @param newValue value to be associated with the specified key.
     *
     * @return true if the value was replaced
     *
     * @throws NullPointerException if the specified key or values are <code>null</code>.
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null) {
            throw new NullPointerException();
        }
        int hash = hash(key, null, null, null);
        return segmentFor(hash).replace(key, null, null, null, hash, oldValue, newValue);
    }

    /**
     * Replace entry for key only if currently mapped to some value. Acts as
     *
     * <pre>
     *  if ((map.containsKey(key)) {
     *     return map.put(key, value);
     * } else return null;
     * </pre>
     *
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated.
     * @param value value to be associated with the specified key.
     *
     * @return previous value associated with specified key, or <code>null</code> if there was no mapping for key.
     *
     * @throws NullPointerException if the specified key or value is <code>null</code>.
     */
    public V replace(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hash(key, null, null, null);
        return segmentFor(hash).replace(key, null, null, null, hash, value);
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        for (int i = 0; i < segments.length; ++i) {
            segments[i].clear();
        }
    }

    /**
     * Unsupported
     */
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     */
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     */
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
