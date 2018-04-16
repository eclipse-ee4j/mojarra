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

package com.sun.faces.renderkit.html_basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.faces.FacesException;

import com.sun.faces.cactus.ServletFacesTestCase;


public class MenuRendererTestCase extends ServletFacesTestCase {
    
    

    // ----------------------------------------------------------- Setup Methods


    public MenuRendererTestCase() {
        super("MenuRendererTestCase.java");
    }


    public MenuRendererTestCase(String name) {
        super(name);
    }


    // ------------------------------------------------------------ Test Methods


    public void testCreateCollection() {

        TestMenuRenderer r = new TestMenuRenderer();

        // null instance using interface for the fallback should
        // result in a null return
        assertNull(r.createCollection(null, setClass()));

        Collection<Object> c = r.createCollection(new HashSet<Object>(), arrayListClass());
        assertNotNull(c);
        assertTrue(c instanceof HashSet);
        assertTrue(c.isEmpty());
    }

    public void testCloneValue() {

        TestMenuRenderer r = new TestMenuRenderer();

        Collection<String> clonableCollection = new ArrayList<String>();
        clonableCollection.add("foo");

        Collection cloned = r.cloneValue(clonableCollection);
        assertNotNull(cloned);
        assertTrue(cloned.isEmpty());

        Collection nonClonableCollection = new CopyOnWriteArraySet();
        assertNull(r.cloneValue(nonClonableCollection));

    }

    public void testBestGuess() {

        TestMenuRenderer r = new TestMenuRenderer();
        assertTrue(r.bestGuess(setClass(),  1) instanceof HashSet);
        assertTrue(r.bestGuess(listClass(), 1) instanceof ArrayList);
        assertTrue(r.bestGuess(sortedSetClass(), 1) instanceof TreeSet);
        assertTrue(r.bestGuess(queueClass(), 1) instanceof LinkedList);
        assertTrue(r.bestGuess(collectionClass(), 1) instanceof ArrayList);
        
    }
    
    @SuppressWarnings("unchecked")
    Class<? extends Set<Object>> setClass() {
        @SuppressWarnings("rawtypes")
        Class clazz = Set.class;
        return (Class<? extends Set<Object>>) clazz;
    }
    
    @SuppressWarnings("unchecked")
    Class<? extends Collection<Object>> collectionClass() {
        @SuppressWarnings("rawtypes")
        Class clazz = Collection.class;
        return (Class<? extends Collection<Object>>) clazz;
    }
    
    @SuppressWarnings("unchecked")
    Class<? extends Queue<Object>> queueClass() {
        @SuppressWarnings("rawtypes")
        Class clazz = Queue.class;
        return (Class<? extends Queue<Object>>) clazz;
    }
    
    @SuppressWarnings("unchecked")
    Class<? extends SortedSet<Object>> sortedSetClass() {
        @SuppressWarnings("rawtypes")
        Class clazz = SortedSet.class;
        return (Class<? extends SortedSet<Object>>) clazz;
    }
    
    @SuppressWarnings("unchecked")
    Class<? extends List<Object>> arrayListClass() {
        @SuppressWarnings("rawtypes")
        Class clazz = ArrayList.class;
        return (Class<? extends List<Object>>) clazz;
    }
    
    @SuppressWarnings("unchecked")
    Class<? extends List<Object>> listClass() {
        @SuppressWarnings("rawtypes")
        Class clazz = List.class;
        return (Class<? extends List<Object>>) clazz;
    }

    public void testCreateCollectionFromHint() {

        TestMenuRenderer r = new TestMenuRenderer();

        assertTrue(r.createCollectionFromHint("java.util.ArrayList") instanceof ArrayList);
        assertTrue(r.createCollectionFromHint(LinkedList.class) instanceof LinkedList);
        try {
            r.createCollectionFromHint(java.util.Set.class);
            assertTrue(false);
        } catch (FacesException fe) {
            // expected
        }

        try {
            r.createCollectionFromHint(new Date());
            assertTrue(false);
        } catch (FacesException fe) {
            // expected
        }
    }


    // ---------------------------------------------------------- Nested Classes

    private static final class TestMenuRenderer extends MenuRenderer {

        @Override
        public Collection<Object> createCollection(Collection<Object> collection, Class<? extends Collection<Object>> fallBackType) {
            return super.createCollection(collection, fallBackType);
        }

        @Override
        public Collection<Object> createCollectionFromHint(Object collectionTypeHint) {
            return super.createCollectionFromHint(collectionTypeHint);
        }

        @Override
        public Collection<Object> bestGuess(Class<? extends Collection<Object>> type, int initialSize) {
            return super.bestGuess(type, initialSize);
        }

        @Override
        protected Collection<Object> cloneValue(Object value) {
            return super.cloneValue(value);
        }
    }
}
