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

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class RemoveReaddMoveBean {

    @Inject
    private FacesContext context;

    public void update() {
        UIViewRoot root = context.getViewRoot();
        UIComponent panelGroupA = root.findComponent(":a");
        UIComponent inputText = root.findComponent(":inputText");
        UIComponent panelGroupB = root.findComponent(":b");

        panelGroupA.getChildren().remove(inputText);
        panelGroupA.getChildren().add(inputText);
        panelGroupB.getChildren().add(inputText);
    }

    public String getStateSavingMode() {
        return context.getViewRoot().initialStateMarked() ? "PSS" : "FSS";
    }

    public String getHasMarkChildrenModified() {
        boolean result = context.getViewRoot()
                                .findComponent(":b")
                                .getAttributes()
                                .containsKey("com.sun.faces.facelets.MARK_CHILDREN_MODIFIED");

        return Boolean.valueOf(result).toString();

    }

}
