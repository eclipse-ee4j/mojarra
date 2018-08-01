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
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named
@RequestScoped
public class Issue2395Bean {

    private HtmlPanelGroup panelGroup;

    public HtmlPanelGroup getPanelGroup() {
        return panelGroup;
    }

    public void setPanelGroup(HtmlPanelGroup panelGroup) {
        this.panelGroup = panelGroup;
    }

    public void doAdd(ActionEvent event) {
        HtmlOutputText out = new HtmlOutputText();
        out.setValue("I was dynamically added");
        getPanelGroup().getChildren().add(out);
    }

    public void doAddRemove(ActionEvent event) {
        HtmlOutputText out = new HtmlOutputText();
        getPanelGroup().getChildren().add(out);
        out.setValue("I was dynamically added");
        getPanelGroup().getChildren().remove(out);
    }

    public void doAddRemoveAdd(ActionEvent event) {
        HtmlOutputText out = new HtmlOutputText();
        out.setValue("I was dynamically added");
        getPanelGroup().getChildren().add(out);
        getPanelGroup().getChildren().remove(out);
        getPanelGroup().getChildren().add(out);
    }

    public void doRemove(ActionEvent event) {
        getPanelGroup().getChildren().remove(0);
    }

    public void doRemoveAdd(ActionEvent event) {
        UIComponent component = getPanelGroup().getChildren().remove(0);
        getPanelGroup().getChildren().add(component);
    }

    public void doRemoveAddRemove(ActionEvent event) {
        UIComponent component = getPanelGroup().getChildren().remove(0);
        getPanelGroup().getChildren().add(component);
        getPanelGroup().getChildren().remove(component);
    }
}
