/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue6075;

import java.io.Serializable;

import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue6075 implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean shown;
    private int added;

    public boolean isShown() {
        return shown;
    }

    public void show() {
        shown = true;
    }

    public void add() {
        var text = new HtmlOutputText();
        text.setValue("added" + ++added);

        var panel = new HtmlPanelGroup();
        panel.getChildren().add(text);

        FacesContext.getCurrentInstance().getViewRoot().findComponent("form:container").getChildren().add(panel);
    }

    public void resolveParentClientId(ComponentSystemEvent event) {
        event.getComponent().getParent().getClientId();
    }
}
