/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockLifecycleFactory.java,v 1.1 2005/10/18 17:47:58 edburns Exp $
 */



package com.sun.faces.mock;


import java.util.Iterator;
import java.util.ArrayList;
import javax.faces.FactoryFinder;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.lifecycle.Lifecycle;

public class MockLifecycleFactory extends LifecycleFactory {
    public MockLifecycleFactory(LifecycleFactory oldImpl) {
	System.setProperty(FactoryFinder.LIFECYCLE_FACTORY, 
			   this.getClass().getName());
    }
    public MockLifecycleFactory() {}

    public void addLifecycle(String lifecycleId,
			     Lifecycle lifecycle) {}
    public Lifecycle getLifecycle(String lifecycleId) {
	return new MockLifecycle();
    }

    public Iterator getLifecycleIds() {
	ArrayList result = new ArrayList(1);
	result.add(LifecycleFactory.DEFAULT_LIFECYCLE);
	return result.iterator();
    }

    
}
