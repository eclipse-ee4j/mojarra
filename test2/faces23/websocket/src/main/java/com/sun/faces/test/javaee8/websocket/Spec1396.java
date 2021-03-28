/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee8.websocket;

import java.io.Serializable;

import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Spec1396 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject @Push
    private PushContext push;

    @Inject @Push
    private PushContext user;

    @Inject @Push
    private PushContext view;

    private String ajaxOutput;

    public void send(){
        push.send("pushed!");

        user.send("pushed!", "user");

        ajaxOutput = "pushed!";
        view.send("renderAjaxOutput");
    }

    public String getAjaxOutput() {
        return ajaxOutput;
    }

}
