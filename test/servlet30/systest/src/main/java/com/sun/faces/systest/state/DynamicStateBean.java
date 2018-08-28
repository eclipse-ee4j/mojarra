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

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.validator.ValidatorException;

@Named
@RequestScoped
public class DynamicStateBean {

    public void validateDeletion(FacesContext context, UIComponent comp, Object val) {
        // The button should not be here on postback
        UIComponent button = findButton(context);
        if (null != button) {
            throw new ValidatorException(new FacesMessage("cbutton should not be found"));
        }

    }

    public void validateAddition(FacesContext context, UIComponent comp, Object val) {
        // The button should not be here on postback
        UIComponent button = findButton(context);
        if (null == button) {
            throw new ValidatorException(new FacesMessage("cbutton should be found"));
        }

    }

    public void beforeRenderDeletion(ComponentSystemEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        UIComponent buttonParent = null, button = findButton(context);
        if (null != button) {
            buttonParent = button.getParent();
            buttonParent.getChildren().remove(button);
        }
    }

    public void beforeRenderAddition(ComponentSystemEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        UIComponent form = findForm(context);
        HtmlCommandButton button;
        if (null == (button = (HtmlCommandButton) findButton(context))) {
            button = new HtmlCommandButton();
            button.setId("cbutton");
            button.setValue("added button");
            form.getChildren().add(button);
        }
    }

    public void transientRoot(ActionEvent ae) {

        UIComponent button = ae.getComponent();
        UIComponent addto = button.findComponent("addto");

        HtmlPanelGroup transientRoot = new HtmlPanelGroup();
        transientRoot.setTransient(true);
        transientRoot.setId("troot");
        StateComponent text = new StateComponent();
        text.setValue("transient parent");
        text.setId("text");
        HtmlPanelGroup group = new HtmlPanelGroup();
        group.setId("group");
        StateComponent text2 = new StateComponent();
        text2.setValue(" test");
        text2.setId("text2");
        group.getChildren().add(text2);
        transientRoot.getChildren().add(text);
        transientRoot.getChildren().add(group);
        addto.getChildren().add(transientRoot);

    }

    private UIComponent findButton(FacesContext context) {
        char sep = UINamingContainer.getSeparatorChar(context);
        UIComponent result = null;
        result = context.getViewRoot().findComponent(sep + "form" + sep + "cbutton");
        return result;
    }

    private UIComponent findForm(FacesContext context) {
        char sep = UINamingContainer.getSeparatorChar(context);
        UIComponent result = null;
        result = context.getViewRoot().findComponent(sep + "form");
        return result;
    }

    public static class StateComponent extends HtmlOutputText {

        @Override
        public Object saveState(FacesContext context) {

            throw new FacesException(
                    "saveState(FacesContext) was incorrectly called for component with client ID: " + this.getClientId(context));
        }

    }

}
