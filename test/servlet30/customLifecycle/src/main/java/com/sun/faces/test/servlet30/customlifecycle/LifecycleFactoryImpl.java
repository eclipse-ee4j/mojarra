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

package com.sun.faces.test.servlet30.customlifecycle;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

public class LifecycleFactoryImpl extends LifecycleFactory {

    public LifecycleFactoryImpl(LifecycleFactory previous) {
        super(previous);

        try {
            getWrapped().addLifecycle("com.sun.faces.test.servlet30.customlifecycle.NewLifecycle", new NewLifecycle("com.sun.faces.test.servlet30.customlifecycle.NewLifecycle"));
            getWrapped().addLifecycle("com.sun.faces.test.servlet30.customlifecycle.AlternateLifecycle", new NewLifecycle("com.sun.faces.test.servlet30.customlifecycle.AlternateLifecycle"));
        } catch (Throwable e) {
            throw new FacesException(e);
        }
    }

    @Override
    public void addLifecycle(String lifecycleId, Lifecycle lifecycle) {
        getWrapped().addLifecycle(lifecycleId, lifecycle);
    }

    @Override
    public Lifecycle getLifecycle(String lifecycleId) {
        return getWrapped().getLifecycle(lifecycleId);
    }

    @Override
    public Iterator<String> getLifecycleIds() {
        return getWrapped().getLifecycleIds();
    }
}
