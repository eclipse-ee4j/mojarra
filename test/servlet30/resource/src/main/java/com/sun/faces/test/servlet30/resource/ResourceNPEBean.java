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

package com.sun.faces.test.servlet30.resource;

import javax.faces.application.ResourceHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "resourceNPEBean")
@RequestScoped
public class ResourceNPEBean {

    private String exists = "false";

    public ResourceNPEBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceHandler handler = context.getApplication().getResourceHandler();
        if (handler.libraryExists("FooBar")) {
            exists = "true";
        } else {
            exists = "false";
        }
    }

    public String getLibraryExists() {
        return exists;
    }

    public void setLibraryExists(String exists) {
        this.exists = exists;
    }
}
