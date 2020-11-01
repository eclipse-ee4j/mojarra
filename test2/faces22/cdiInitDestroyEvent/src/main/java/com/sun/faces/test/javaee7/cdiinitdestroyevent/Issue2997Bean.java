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

import java.io.Serializable;

import jakarta.annotation.PreDestroy;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@FlowScoped("flow-with-templates")
public class Issue2997Bean implements Serializable {

    private static final long serialVersionUID = -7181467406646852183L;

    @Inject
    private UserBean userBean;
    private String name;

    public Issue2997Bean() {
        name = currentTimeMillis() + "issue2997Bean";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PreDestroy
    public void preDestroy() {
        userBean.setDestroyIssue2997FlowMessage("" + currentTimeMillis() + getName());
    }

}
