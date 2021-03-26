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

package com.sun.faces.test.javaee6web.injection;

import jakarta.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

@ManagedBean(name = "injection")
@SessionScoped
public class Injection {

    private boolean initCalled = false;

    private int postConstructCalled = 0;

    @Inject
    private Foo foo;

    @Inject
    public void initialize(Foo foo) {
        initCalled = foo != null;
    }

    public boolean isInitCalled() {
        return initCalled;
    }

    public boolean isFooInjected() {
        return foo != null;
    }

    @PostConstruct
    public void concall() {
        postConstructCalled++;
    }

    public String getPostConstructCalled() {
        return Integer.toString(postConstructCalled);
    }
}
