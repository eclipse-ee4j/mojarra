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
import java.util.Set;

/*
 * Non-thread safe implementation of Set for use when most of the time there
 * is only one element, but sometimes there are more than one.
 *
 */
public class MostlySingletonSet<E> implements Set<E>, Serializable {

    private static final long serialVersionUID = 2818326518724772145L;

    private Set<E> inner;

    public MostlySingletonSet() {

    }

    // <editor-fold defaultstate="collapsed" desc="Mutating methods">

    @Override
    public boolean add(E e) {
        boolean result = true;
        if (null == inner) {
            inner = Collections.singleton(e);
        } else {
            // If we need to transition from one to more-than-one
            if (1 == inner.size()) {
                HashSet<E> newSet = new HashSet<>();
                newSet.add(inner.iterator().next());
                inner = newSet;
            }
            result = inner.add(e);
        }

        return result;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = true;

        if (null == inner && 1 == c.size()) {
            inner = (Set<E>) Collections.singleton(c.iterator().next());
        } else {
            // If we need to transition from one to more-than-one
            if (1 == inner.size()) {
                HashSet<E> newSet = new HashSet<>();
                newSet.add(inner.iterator().next());
                inner = newSet;
            }
            result = inner.addAll(c);
        }
        return result;
    }

    @Override
    public void clear() {
        if (null != inner) {
            // If we need to transition from more-than-one to zero
            if (1 < inner.size()) {
                inner.clear();
            }
            inner = null;
        }
    }

    @Override
    public boolean remove(Object o) {
        boolean didRemove = false;

        if (null != inner) {
            if (1 == inner.size()) {
                // If we need to transition from one to zero
                E e = inner.iterator().next();
                // If our element is not null, and the argument is not null
                if (null != e && null != o) {
                    didRemove = e.equals(o);
                } else {
                    didRemove = null == o;
                }
                if (didRemove) {
                    inner = null;
                }

            } else {
                didRemove = inner.remove(o);
                if (didRemove && 1 == inner.size()) {
                    Set<E> newInner = Collections.singleton(inner.iterator().next());
                    inner.clear();
                    inner = newInner;
                }
            }

        }

        return didRemove;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;

        if (null != inner) {
            if (1 == inner.size()) {
                // May throw NPE per spec for Collection.removeAll()
                Iterator incomingIter = c.iterator();
                E oneAndOnlyElement = inner.iterator().next();
                // Iterate over the incoming collection
                // looking for a member that is equal to our one and only
                // element.
                while (incomingIter.hasNext()) {
                    Object cur = incomingIter.next();
                    if (null != oneAndOnlyElement) {
                        // This handles null == cur.
                        if (result = oneAndOnlyElement.equals(cur)) {
                            break;
                        }
                    } else {
                        // oneAndOnlyElement is null
                        if (result = cur == null) {
                            break;
                        }
                    }
                }
                if (result) {
                    inner = null;
                }
            } else {
                result = inner.removeAll(c);
                if (result && 0 == inner.size()) {
                    inner = null;
                }

            }
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean didModify = false;

        if (null != inner) {
            if (1 == inner.size()) {
                Iterator incomingIter = c.iterator();
                E oneAndOnlyElement = inner.iterator().next();
                // Iterate over the incoming collection
                // looking for a member that is equal to our one and only
                // element. If found, we take no action, otherwise
                // we remove the oneAndOnlyElement.
                boolean found = false;
                while (incomingIter.hasNext()) {
                    Object cur = incomingIter.next();
                    if (null != oneAndOnlyElement) {
                        if (found = oneAndOnlyElement.equals(cur)) {
                            break;
                        }
                    } else {
                        if (found = cur == null) {
                            break;
                        }
                    }
                }
                if (didModify = !found) {
                    inner = null;
                }

            } else {
                didModify = inner.retainAll(c);
            }
        }

        return didModify;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-mutating methods">

    @Override
    public boolean contains(Object o) {
        boolean result = false;

        if (null != inner) {
            result = inner.contains(o);
        }

        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean result = false;

        if (null != inner) {
            result = inner.containsAll(c);
        }

        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = true;

        if (null != inner) {
            result = inner.isEmpty();
        }

        return result;
    }

    @Override
    public int size() {
        int size = 0;
        if (null != inner) {
            size = inner.size();
        }
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null) {
            if (obj instanceof MostlySingletonSet) {
                final MostlySingletonSet<E> other = (MostlySingletonSet<E>) obj;
                if (this.inner != other.inner && (this.inner == null || !this.inner.equals(other.inner))) {
                    result = false;
                } else {
                    result = true;
                }
            } else if (obj instanceof Collection) {
                Collection otherCollection = (Collection) obj;

                if (null != inner) {
                    result = inner.equals(otherCollection);
                } else {
                    result = otherCollection.isEmpty();
                }

            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.inner != null ? this.inner.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        String result = "empty";
        if (null != inner) {
            result = inner.toString();
        }
        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Iteration and array">

    @Override
    public Iterator<E> iterator() {
        Iterator<E> result;

        if (null != inner) {
            result = inner.iterator();
        } else {
            result = Collections.EMPTY_SET.iterator();
        }

        return result;
    }

    @Override
    public Object[] toArray() {
        Object[] result = null;
        if (null != inner) {
            result = inner.toArray();
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] result = null;
        if (null != inner) {
            result = inner.toArray(a);
        }
        return result;
    }

    // </editor-fold>

}
