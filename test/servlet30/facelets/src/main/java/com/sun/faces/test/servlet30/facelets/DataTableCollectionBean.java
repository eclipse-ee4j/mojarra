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

package com.sun.faces.test.servlet30.facelets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name = "dataTableCollectionBean")
@RequestScoped
public class DataTableCollectionBean implements Serializable {
    
    Collection<DataTableCollectionItem> users;

    public Collection<DataTableCollectionItem> getUsers() {
        return users;
    }

    public void setUsers(Collection<DataTableCollectionItem> users) {
        this.users = users;
    }
    
    public DataTableCollectionBean() {
        users = new MyCollection(new ArrayList<DataTableCollectionItem>());
        DataTableCollectionItem b;
        for (int i = 0; i < 3; i++) {
            b = new DataTableCollectionItem();
            b.setFirstName("First" + i);
            b.setLastName("Last"+i);
            users.add(b);
        }
    }
    
    private static class MyCollection implements Collection {
        
        private Collection inner;
        
        private MyCollection(Collection c) {
            inner = c;
        }

        @Override
        public boolean add(Object e) {
            return inner.add(e);
        }

        @Override
        public boolean addAll(Collection c) {
            return inner.addAll(c);
        }

        @Override
        public void clear() {
            inner.clear();
        }

        @Override
        public boolean contains(Object o) {
            return inner.contains(o);
        }

        @Override
        public boolean containsAll(Collection c) {
            return inner.containsAll(c);
        }

        @Override
        public boolean isEmpty() {
            return inner.isEmpty();
        }

        @Override
        public Iterator iterator() {
            return inner.iterator();
        }

        @Override
        public boolean remove(Object o) {
            return inner.remove(o);
        }

        @Override
        public boolean removeAll(Collection c) {
            return inner.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection c) {
            return inner.retainAll(c);
        }

        @Override
        public int size() {
            return inner.size();
        }

        @Override
        public Object[] toArray() {
            return inner.toArray();
        }

        @Override
        public Object[] toArray(Object[] a) {
            return inner.toArray(a);
        }   
    }    
}
