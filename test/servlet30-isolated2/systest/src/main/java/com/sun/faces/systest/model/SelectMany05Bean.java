/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.systest.model;

import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.faces.model.SelectItem;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.FacesException;

public class SelectMany05Bean {

    // As IBM j9 JRE/JDK does not use a static instance for reverseOrder()
    // we need to keep a static instance to make equals() work
    private static final Comparator<String> REVERSE_COMPARATOR = Collections.reverseOrder();

    private final Collection<SelectItem> possibleValues;
    private Set<String> setValues;
    private SortedSet<String> sortedSetValues;
    private List<String> listValues;
    private Collection<String> collectionValues;
    private String[] arrayValues;
    private SortedSet<String> initialSortedSetValues;
    private Collection<String> initialCollectionValues;
    private Set<String> initialSetValues;
    private List<String> initialListValues;
    private Collection<String> collectionFromHintValues;
    private Collection<String> collectionFromHintValues2;
    private Object someValues;
    private Collection<HobbitBean> hobbitCollection;
    private DataModel<HobbitBean> hobbitDataModel;

    // ------------------------------------------------------------ Constructors

    public SelectMany05Bean() {
        HobbitBean[] hobbits = getHobbitBeanArray();

        Set<SelectItem> items = new LinkedHashSet<SelectItem>();
        for (HobbitBean hobbit : hobbits) {
            items.add(new SelectItem(hobbit.getName()));
        }
        hobbitCollection = new TreeSet<HobbitBean>();
        hobbitCollection.addAll(Arrays.asList(hobbits));
        possibleValues = Collections.unmodifiableSet(items);
        initialSortedSetValues = new TreeSet<String>(REVERSE_COMPARATOR);
        initialSortedSetValues.add("Pippin");
        initialSortedSetValues.add("Frodo");
        initialCollectionValues = new LinkedHashSet<String>(2);
        initialCollectionValues.add("Bilbo");
        initialCollectionValues.add("Merry");
        initialSetValues = new CopyOnWriteArraySet<String>(); // not Cloneable
        initialSetValues.add("Frodo");
        initialListValues = new Vector<String>();
        initialListValues.add("Bilbo");
        initialListValues.add("Pippin");
        initialListValues.add("Merry");
        hobbitDataModel = new ListDataModel<HobbitBean>(new ArrayList<HobbitBean>(Arrays.asList(hobbits)));

    }

    protected HobbitBean[] getHobbitBeanArray() {
        HobbitBean[] hobbits = { new HobbitBean("Bilbo", "Bilbo - <Ring Finder>"), new HobbitBean("Frodo", "Frodo - <Ring Bearer>"),
                new HobbitBean("Merry", "Merry - <Trouble Maker>"), new HobbitBean("Pippin", "Pipping - <Trouble Maker>") };
        return hobbits;
    }

    // ---------------------------------------------------------- Public Methods

    public Collection<HobbitBean> getHobbitCollection() {
        return hobbitCollection;
    }

    public DataModel<HobbitBean> getHobbitDataModel() {
        return hobbitDataModel;
    }

    public Set<String> getSetValues() {
        return setValues;
    }

    public void setSetValues(Set<String> setValues) {
        if (!(setValues instanceof HashSet)) {
            throw new FacesException("[setSetValues] Error: Expected value to be ArrayList");
        }
        this.setValues = setValues;
    }

    public List<String> getListValues() {
        return listValues;
    }

    public void setListValues(List<String> listValues) {
        if (!(listValues instanceof ArrayList)) {
            throw new FacesException("[setListValues] Error: Expected value to be ArrayList");
        }
        this.listValues = listValues;
    }

    public String[] getArrayValues() {
        return arrayValues;
    }

    public void setArrayValues(String[] arrayValues) {
        this.arrayValues = arrayValues;
    }

    public SortedSet<String> getSortedSetValues() {
        return sortedSetValues;
    }

    public void setSortedSetValues(SortedSet<String> sortedSetValues) {
        if (!(sortedSetValues instanceof TreeSet)) {
            throw new FacesException("[setSortedSetValues] Error: Expected value to be TreeSet");
        }
        if (((TreeSet) sortedSetValues).comparator() != null) {
            throw new FacesException("[setSortedSetValues] Error: Expected null comparator");
        }
        this.sortedSetValues = sortedSetValues;
    }

    public Collection<String> getCollectionValues() {
        return collectionValues;
    }

    public void setCollectionValues(Collection<String> collectionValues) {
        if (!(collectionValues instanceof ArrayList)) {
            throw new FacesException("[setCollectionValues] Error: Expected value to be ArrayList");
        }
        this.collectionValues = collectionValues;
    }

    public Collection<SelectItem> getPossibleValues() {
        return possibleValues;
    }

    public Collection<?> getEmptyCollection() {
        return Collections.emptyList();
    }

    ////////////////////////////////////////////////////////////////////////////

    public SortedSet<String> getInitialSortedSetValues() {
        return initialSortedSetValues;
    }

    public void setInitialSortedSetValues(SortedSet<String> initialSortedSetValues) {
        if (!(initialSortedSetValues instanceof TreeSet)) {
            throw new FacesException("[setInitialSortedSetValues] Error: Expected value to be TreeSet");
        }

        if (!REVERSE_COMPARATOR.equals(initialSortedSetValues.comparator())) {
            throw new FacesException("[setInitialSortedSetValues] Error: Comparator is not equivalent to Collections.reverseOrder()");
        }

// This comparison fails on AIX platform, so we do the above comparison
//        if (!Collections.reverseOrder().equals(((TreeSet) initialSortedSetValues).comparator())) {
//            throw new FacesException("[setInitialSortedSetValues] Error: Comparator is not equivalent to Collections.reverseOrder()");
//        }
        this.initialSortedSetValues = initialSortedSetValues;
    }

    public Collection<String> getInitialCollectionValues() {
        return initialCollectionValues;
    }

    public void setInitialCollectionValues(Collection<String> initialCollectionValues) {
        if (!(initialCollectionValues instanceof LinkedHashSet)) {
            throw new FacesException("[setCollectionValues] Error: Expected value to be LinkedHashSet");
        }
        this.initialCollectionValues = initialCollectionValues;
    }

    public Set<String> getInitialSetValues() {
        return initialSetValues;
    }

    public void setInitialSetValues(Set<String> initialSetValues) {
        if (!(initialSetValues instanceof CopyOnWriteArraySet)) {
            throw new FacesException("[initialSetValues] Error: Expected value to be CopyOnWriteArraySet");
        }
        this.initialSetValues = initialSetValues;
    }

    public List<String> getInitialListValues() {
        return initialListValues;
    }

    public void setInitialListValues(List<String> initialListValues) {
        if (!(initialListValues instanceof Vector)) {
            throw new FacesException("[initialListValues] Error: Expected value to be Vector");
        }
        this.initialListValues = initialListValues;
    }

    ////////////////////////////////////////////////////////////////////////////

    public Collection<String> getCollectionFromHintValues() {
        return collectionFromHintValues;
    }

    public void setCollectionFromHintValues(Collection<String> collectionFromHintValues) {
        if (!(collectionFromHintValues instanceof LinkedList)) {
            throw new FacesException("[collectionFromHintValues] Error: Expected value to be LinkedList");
        }
        this.collectionFromHintValues = collectionFromHintValues;
    }

    public Collection<String> getCollectionFromHintValues2() {
        return collectionFromHintValues2;
    }

    public void setCollectionFromHintValues2(Collection<String> collectionFromHintValues) {
        if (!(collectionFromHintValues instanceof LinkedList)) {
            throw new FacesException("[collectionFromHintValues2] Error: Expected value to be LinkedList");
        }
        this.collectionFromHintValues2 = collectionFromHintValues;
    }

    public Class<? extends Collection> getCollectionType() {
        return LinkedList.class;
    }

    ////////////////////////////////////////////////////////////////////////////

    private int[] selectedIntValues;
    private int[] intOptions = new int[] { 1, 2, 3, 4 };

    public int[] getSelectedIntValues() {
        return selectedIntValues;
    }

    public void setSelectedIntValues(int[] selectedIntValues) {
        this.selectedIntValues = selectedIntValues;
    }

    public int[] getIntOptions() {
        return intOptions;
    }

    ////////////////////////////////////////////////////////////////////////////

    private Integer[] selectedIntegerValues;
    private Integer[] integerOptions = new Integer[] { 1, 2, 3, 4 };

    public Integer[] getSelectedIntegerValues() {
        return selectedIntegerValues;
    }

    public void setSelectedIntegerValues(Integer[] selectedIntegerValues) {
        this.selectedIntegerValues = selectedIntegerValues;
    }

    public Integer[] getIntegerOptions() {
        return integerOptions;
    }

    ////////////////////////////////////////////////////////////////////////////

    public Object getSomeValues() {
        return someValues;
    }

    public void setSomeValues(Object someValues) {
        // validate the case where the type is Object.class. The logic should
        // default the value to Object[].
        if (!someValues.getClass().isArray()) {
            throw new FacesException("[someValues] Error: Expected value to be an array type");
        }
        this.someValues = someValues;
    }

    // ---------------------------------------------------------- Nested Classes

    public static final class HobbitBean implements Comparable {

        private String name;
        private String bio;

        // -------------------------------------------------------- Constructors

        public HobbitBean(String name, String bio) {
            this.name = name;
            this.bio = bio;
        }

        public String getName() {
            return name;
        }

        public String getBio() {
            return bio;
        }

        @Override
        public String toString() {
            return name;
        }

        // --------------------------------------------- Methods from Comparable

        @Override
        public int compareTo(Object o) {
            return name.compareTo(((HobbitBean) o).name);
        }
    }
}
