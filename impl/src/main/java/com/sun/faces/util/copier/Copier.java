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

package com.sun.faces.util.copier;

/**
 * Interface that is to be implement by classes that know how to copy an object.
 * <p>
 * This contract makes no guarantee about the level of copying that is done. Copies can be deep, shallow, just a new
 * instance of the same type or anything in between. It generally depends on the exact purpose of the copied object what
 * level of copying is needed, and different implementations of this interface can facilitate for this difference.
 *
 * @since 2.3
 * @author Arjan Tijms
 *
 */
public interface Copier {

    /**
     * Return an object that's logically a copy of the given object.
     * <p>
     *
     * @param object the object to be copied
     * @return a copy of the given object
     */
    Object copy(Object object);

}
