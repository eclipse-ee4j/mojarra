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

package com.sun.faces.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class Bean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String valueFromQueryParamAtCtorTime;
    private List<Foo> foos;
    private Foo selectedFoo;

    public Bean() {
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        valueFromQueryParamAtCtorTime = extContext.getRequestParameterMap().get("pageWithViewScopedBean");
        foos = new ArrayList<Foo>();
        foos.add(new Foo("Shirley"));
        foos.add(new Foo("Stan"));
        foos.add(new Foo("Cole"));

    }

    public String getBob() {
        return "Bob created with param " + valueFromQueryParamAtCtorTime;
    }

    public List<Foo> getFoos() {
        return foos;
    }

    public void setFoos(List<Foo> foos) {
        this.foos = foos;
    }

    public Foo getSelectedFoo() {
        return selectedFoo;
    }

    public void setSelectedFoo(Foo selectedFoo) {
        this.selectedFoo = selectedFoo;
    }

}
