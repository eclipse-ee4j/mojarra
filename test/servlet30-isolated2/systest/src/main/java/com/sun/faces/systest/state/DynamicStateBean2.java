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

package com.sun.faces.systest.state;

import javax.inject.Named;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;

@Named
public class DynamicStateBean2 {

    private String value;

    public DynamicStateBean2() {
        value = "default value";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void render() {

        // <h:form id="form">
        // <h:panelGroup id="viewPanel"/>
        // </h:form>

        UIComponent viewPanel = FacesContext.getCurrentInstance().getViewRoot().findComponent("form:viewPanel");

        viewPanel.getChildren().clear();

        UIComponent childPanel = new HtmlPanelGroup();
        childPanel.setId("childPanel");
        viewPanel.getChildren().add(childPanel);

        // Add a textinput to the inner most panel with a
        // binding to the the value property of this bean.
        // ...
        // <h:panelGroup id="viewPanel">
        // <h:panelGroup id="childPanel"/>
        // <h:textInput value="#{render.value}"/>
        // </h:panelGroup>
        // </h:panelGroup>
        // ...

        UIComponent textInput = new HtmlInputText();
        textInput.setId("textInput");
        textInput.setValueExpression("value", FacesContext.getCurrentInstance().getApplication().getExpressionFactory()
                .createValueExpression(FacesContext.getCurrentInstance().getELContext(), "#{dynamicStateBean2.value}", Object.class));

        childPanel.getChildren().add(textInput);
    }

    public void render2() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent viewPanel = ctx.getViewRoot().findComponent("form:viewPanel");

        viewPanel.getChildren().clear();

        HtmlCommandButton btn = (HtmlCommandButton) ctx.getApplication().createComponent(HtmlCommandButton.COMPONENT_TYPE);
        btn.setValue("dynamically added button");

        // Using a non-generated identifier will not cause
        // the exception to be thrown.

        // btn.setId("btn");

        viewPanel.getChildren().add(btn);

    }

}
