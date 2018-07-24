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

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class Injection implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean initCalled;
    private int postConstructCalled;

    @Inject
    private Foo foo;

    @PostConstruct
    public void concall() {
        postConstructCalled++;
        initCalled = foo != null;
    }

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

    public String getPostConstructCalled() {
        return Integer.toString(postConstructCalled);
    }
}
