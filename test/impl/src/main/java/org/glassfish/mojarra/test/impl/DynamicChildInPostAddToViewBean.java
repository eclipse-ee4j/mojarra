/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.test.impl;

import java.io.Serializable;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Adds the same child to the same container from two different <code>postAddToView</code> listeners: one attached to
 * the view root, one attached to the container itself. Both listeners run on every view build, so neither adds
 * conditionally.
 */
@Named
@ViewScoped
public class DynamicChildInPostAddToViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CONTAINER_ID = "form:container";
    private static final String CHILD_ID = "dynamicInput";
    private static final String CHILD_STYLE_CLASS = "dynamic-child";

    private String value;

    public void addViaViewRoot(ComponentSystemEvent event) {
        addChild(event.getComponent().findComponent(CONTAINER_ID));
    }

    public void addViaContainer(ComponentSystemEvent event) {
        addChild(event.getComponent());
    }

    private void addChild(UIComponent container) {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();

        HtmlInputText input = (HtmlInputText) application.createComponent(HtmlInputText.COMPONENT_TYPE);
        input.setId(CHILD_ID);
        input.setStyleClass(CHILD_STYLE_CLASS);
        input.setValueExpression("value", application.getExpressionFactory()
                .createValueExpression(context.getELContext(), "#{dynamicChildInPostAddToViewBean.value}", String.class));

        container.getChildren().add(input);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
