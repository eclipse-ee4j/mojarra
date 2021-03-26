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

package jakarta.faces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TypedCollections {

    /**
     * Dynamically check that the members of the collection are all instances of the given type (or null).
     */
    private static boolean checkCollectionMembers(Collection<?> c, Class<?> type) {
        for (Object element : c) {
            if (element != null && !type.isInstance(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Dynamically check that the members of the collection are all instances of the given type (or null), and that the
     * collection itself is of the given collection type.
     *
     * @param <E> the collection's element type
     * @param c the collection to cast
     * @param type the class of the collection's element type.
     * @return the dynamically-type checked collection.
     * @throws java.lang.ClassCastException
     */
    static <E, TypedC extends Collection<E>> TypedC dynamicallyCastCollection(Collection<?> c, Class<E> type, Class<TypedC> collectionType) {
        if (c == null) {
            return null;
        }

        if (!collectionType.isInstance(c)) {
            throw new ClassCastException(c.getClass().getName());
        }

        assert checkCollectionMembers(c, type) : "The collection contains members with a type other than " + type.getName();

        return collectionType.cast(c);
    }

    /**
     * Dynamically check that the members of the list are all instances of the given type (or null).
     *
     * @param <E> the list's element type
     * @param list the list to cast
     * @param type the class of the list's element type.
     * @return the dynamically-type checked list.
     * @throws java.lang.ClassCastException
     */
    @SuppressWarnings("unchecked")
    static <E> List<E> dynamicallyCastList(List<?> list, Class<E> type) {
        return dynamicallyCastCollection(list, type, List.class);
    }

    /**
     * Dynamically check that the members of the set are all instances of the given type (or null).
     *
     * @param <E> the set's element type
     * @param set the set to cast
     * @param type the class of the set's element type.
     * @return the dynamically-type checked set.
     * @throws java.lang.ClassCastException
     */
    @SuppressWarnings("unchecked")
    static <E> Set<E> dynamicallyCastSet(Set<?> set, Class<E> type) {
        return dynamicallyCastCollection(set, type, Set.class);
    }

    /**
     * Dynamically check that the keys and values in the map are all instances of the correct types (or null).
     *
     * @param <K> the map's key type
     * @param <V> the map's value type
     * @param map the map to cast
     * @param keyType the class of the map's key type.
     * @param valueType the class of the map's value type.
     * @return the dynamically-type checked map.
     * @throws java.lang.ClassCastException
     */
    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> dynamicallyCastMap(Map<?, ?> map, Class<K> keyType, Class<V> valueType) {
        if (map == null) {
            return null;
        }
        assert checkCollectionMembers(map.keySet(), keyType) : "The map contains keys with a type other than " + keyType.getName();
        assert checkCollectionMembers(map.values(), valueType) : "The map contains values with a type other than " + valueType.getName();

        return (Map<K, V>) map;
    }
}
