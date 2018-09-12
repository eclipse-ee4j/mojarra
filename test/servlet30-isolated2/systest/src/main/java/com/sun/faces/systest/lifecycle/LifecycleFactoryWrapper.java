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

package com.sun.faces.systest.lifecycle;

import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import java.util.Iterator;

public class LifecycleFactoryWrapper extends LifecycleFactory {

    public LifecycleFactoryWrapper() {
    }

    private LifecycleFactory oldFactory = null;

    public LifecycleFactoryWrapper(LifecycleFactory yourOldFactory) {
        oldFactory = yourOldFactory;
    }

    @Override
    public void addLifecycle(String lifecycleId, Lifecycle lifecycle) {
        oldFactory.addLifecycle(lifecycleId, lifecycle);
    }

    @Override
    public Lifecycle getLifecycle(String lifecycleId) {
        return oldFactory.getLifecycle(lifecycleId);
    }

    @Override
    public Iterator<String> getLifecycleIds() {
        return oldFactory.getLifecycleIds();
    }

    @Override
    public String toString() {
        return "LifecycleFactoryWrapper";
    }

}
