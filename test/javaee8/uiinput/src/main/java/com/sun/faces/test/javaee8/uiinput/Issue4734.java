/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to the Eclipse Foundation.
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
package com.sun.faces.test.javaee8.uiinput;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class Issue4734 implements Serializable {

    private static final long serialVersionUID = 1L;

    private Issue4734Entity entity;

    @PostConstruct
    public void init() {
        entity = new Issue4734Entity(); // WARNING: unnatural approach, you'd expect that the converter would be used on f:viewParam, but it is part of the actual issue.
    }

    public Issue4734Entity getEntity() {
        return entity;
    }

    public void setEntity(Issue4734Entity entity) {
        this.entity = entity;
    }
}