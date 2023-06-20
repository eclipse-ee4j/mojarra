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

import static java.util.Arrays.asList;

import java.util.List;

/**
 * Copier that copies an object trying a variety of strategies until one succeeds.
 * <p>
 * The strategies that will be attempted in order are:
 * <ol>
 * <li>Serialization
 * <li>Cloning
 * <li>Copy constructor
 * <li>New instance
 * </ol>
 *
 * @since 2.3
 * @author Arjan Tijms
 *
 */
public class MultiStrategyCopier implements Copier {

    private static final List<Copier> COPIERS = List.of( // Note: copier instances used here must be thread-safe!
            new SerializationCopier(), new CloneCopier(), new CopyCtorCopier(), new NewInstanceCopier());

    @Override
    public Object copy(Object object) {

        for (Copier copier : COPIERS) {

            try {
                return copier.copy(object);
            } catch (Exception ignore) {
                continue;
            }

        }

        throw new IllegalStateException("Can't copy object of type " + object.getClass() + ". No copier appeared to be capable of copying it.");
    }

}
