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

package com.sun.faces.test.servlet30.dynamictransientparent;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named
@ApplicationScoped
public class StateBean {

    public void temporaryMoveComponent(ActionEvent ae) {
        UIComponent button = ae.getComponent();
        UIComponent moveme = button.findComponent("moveme");
        UIComponent moveto = button.findComponent("moveto");
        UIComponent parent = moveme.getParent();

        parent.getChildren().remove(moveme);
        moveto.getChildren().add(moveme);
        moveto.getChildren().remove(moveme);
        parent.getChildren().add(moveme);
    }

    public void transientRoot(ActionEvent ae) {
        UIComponent button = ae.getComponent();
        UIComponent addto = button.findComponent("addto");

        HtmlPanelGroup transientRoot = new HtmlPanelGroup();
        transientRoot.setTransient(true);
        HtmlOutputText text = new HtmlOutputText();
        text.setValue("transient parent");
        transientRoot.getChildren().add(text);
        addto.getChildren().add(transientRoot);
    }

}
