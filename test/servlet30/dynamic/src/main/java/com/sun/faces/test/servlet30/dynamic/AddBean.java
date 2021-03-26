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

package com.sun.faces.test.servlet30.dynamic;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

@ManagedBean(name = "addBean")
public class AddBean {

    private static final List<String>   LIST;

    static {
        LIST = new ArrayList<String>();
        LIST.add( "Foo" );
        LIST.add( "Bar" );
        LIST.add( "Baz" );
    }

    public List<String> getList() {
        return LIST;
    }

    /**
     * Save.
     */
    public void save() {
        // Do nothing - just a way to postback.
    }

    /**
     * Appends an OutputText component to another component.
     */
    public void addComponent() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent group = ctx.getViewRoot().findComponent("dynamicForm" +
            UINamingContainer.getSeparatorChar(ctx) +  "group");
        HtmlOutputText output = new HtmlOutputText();
        output.setValue("OUTPUT");
        group.getChildren().add(output);
    }

}
