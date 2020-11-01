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

package com.sun.faces.test.javaee7.cdiinitdestroyevent;

import static java.lang.System.currentTimeMillis;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;

@Dependent
public class SessionLogger {

    @Inject
    private UserBean userBean;

    @Inject
    private AppBean appBean;

    public void observeSessionStart(@Observes @Initialized(SessionScoped.class) HttpSession event) {
        userBean.setInitMessage("" + currentTimeMillis());
    }

    public void observeSessionEnd(@Observes @Destroyed(SessionScoped.class) HttpSession event) {
        appBean.setSessionDestroyedMessage("" + currentTimeMillis());
    }

}
