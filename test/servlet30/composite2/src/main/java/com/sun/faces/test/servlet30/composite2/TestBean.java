/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates.
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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
package com.sun.faces.test.servlet30.composite2;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;


@Named
@SessionScoped
public class TestBean implements Serializable {
    private static final long serialVersionUID = 8797989409085180214L;

    private String myname = "ok";
    private String targets = "cancel sub:command";
    private String targetsEL = "cancelEL sub:commandEL";
    private String event = "action";
    private boolean mydefault = true;

    public String doAction() {
        System.out.println("TestBean#doAction");
        return null;
    }

    public String getMyname() {
        return myname;
    }

    public String getTargets() {
        return targets;
    }

    public String getTargetsEL() {
        return targetsEL;
    }

    public String getEvent() {
        return event;
    }

    public boolean isMydefault() {
        return mydefault;
    }

}
