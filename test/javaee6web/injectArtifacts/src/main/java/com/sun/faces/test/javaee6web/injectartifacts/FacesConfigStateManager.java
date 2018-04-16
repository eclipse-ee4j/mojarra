/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee6web.injectartifacts;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.StateManager;
import javax.faces.application.StateManagerWrapper;

public class FacesConfigStateManager extends StateManagerWrapper {

    private StateManager wrapped;

    public FacesConfigStateManager(StateManager wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public StateManager getWrapped() {
        return this.wrapped;
    }

    private String postConstructCalled;

    @PostConstruct
    private void doPostConstruct() {
        postConstructCalled = "@PostConstruct called";

    }

    @Resource(name = "injectedMessage")
    private String injectedMessage;

    public String getInjectedMessage() {
        return injectedMessage + " " + postConstructCalled;
    }

}
