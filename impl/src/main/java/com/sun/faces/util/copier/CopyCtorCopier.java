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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Copier that copies an object using its copy constructor.
 * <p>
 * A copy constructor is a constructor that takes an object of the same type as the object that's to be constructed.
 * This constructor then initializes itself using the values of this other instance.
 *
 * @since 2.3
 * @author Arjan Tijms
 *
 */
public class CopyCtorCopier implements Copier {

    @Override
    public Object copy(Object object) {

        try {
            Constructor<?> copyConstructor = object.getClass().getConstructor(object.getClass());

            return copyConstructor.newInstance(object);

        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

    }

}
