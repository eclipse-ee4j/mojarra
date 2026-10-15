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
package org.eclipse.mojarra.test.issue6060;

import java.io.Serializable;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue6060 implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean built;
    private int generation;
    private String value;

    public void build() {
        if (!built) {
            built = true;
            addPanel();
        }
    }

    public void rebuild() {
        getContainer().getChildren().clear();
        addPanel();
    }

    private void addPanel() {
        var context = FacesContext.getCurrentInstance();

        var panel = new HtmlPanelGroup();
        panel.setId("panel" + ++generation);
        panel.setLayout("block");
        getContainer().getChildren().add(panel);

        var field = new HtmlInputText();
        field.setId("field");
        field.setValueExpression("value", context.getApplication().getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{issue6060.value}", String.class));
        panel.getChildren().add(field);
    }

    private static UIComponent getContainer() {
        return FacesContext.getCurrentInstance().getViewRoot().findComponent("form:container");
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
