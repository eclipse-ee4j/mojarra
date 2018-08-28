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

package com.sun.faces.test.servlet30.nesteddatatables;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 *
 * @author edburns
 */
public class NestedBean {

    /** Creates a new instance of NestedBean */
    public NestedBean() {
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void executeLink(ActionEvent event) {
        String whichLink = "You clicked on link: " + id;
        System.out.println(whichLink);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("whichLink", whichLink);
    }

}
