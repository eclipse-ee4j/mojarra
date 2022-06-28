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

package com.sun.faces.mock;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * <p>
 * General purpose <code>Enumeration</code> wrapper around an
 * <code>Iterator</code> specified to our controller.</p>
 */
public class MockEnumeration<T> implements Enumeration<T> {

    public MockEnumeration(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    protected Iterator<T> iterator;

    @Override
    public boolean hasMoreElements() {
        return (iterator.hasNext());
    }

    @Override
    public T nextElement() {
        return (iterator.next());
    }
}
