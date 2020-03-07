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

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;

public class MockApplicationFactory extends ApplicationFactory {

    public MockApplicationFactory(ApplicationFactory oldImpl) {
        System.setProperty(FactoryFinder.APPLICATION_FACTORY,
                this.getClass().getName());
    }

    public MockApplicationFactory() {
    }

    private Application application = null;

    @Override
    public Application getApplication() {
        if (application == null) {
            application = new MockApplication();
        }
        return (application);
    }

    @Override
    public void setApplication(Application application) {
        this.application = application;
    }
}
