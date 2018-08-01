/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates.
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

package com.sun.faces.test.servlet30.dynamic;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named
@SessionScoped
public class Issue1414Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    public void temporaryMoveComponent(ActionEvent ae) {

        System.out.println("Temporarily moving a component");

        UIComponent button = ae.getComponent();
        UIComponent movefrom = button.findComponent("movefrom");
        UIComponent moveto = button.findComponent("moveto");

        if (movefrom.getChildren().isEmpty()) {
            UIComponent moveme = moveto.getChildren().get(0);
            moveto.getChildren().remove(moveme);
            movefrom.getChildren().add(moveme);
        } else {
            UIComponent moveme = movefrom.getChildren().get(0);
            movefrom.getChildren().remove(moveme);
            moveto.getChildren().add(moveme);
        }
    }
}
