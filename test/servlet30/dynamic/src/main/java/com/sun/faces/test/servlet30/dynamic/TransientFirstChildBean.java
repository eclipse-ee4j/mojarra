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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class TransientFirstChildBean {

    private transient UIComponent parentContainer;

    public UIComponent getParent() {
        return this.parentContainer;
    }

    public void setParent(UIComponent container) {
        this.parentContainer = container;
    }

    public String add() {
        FacesContext context = FacesContext.getCurrentInstance();

        HtmlOutputText dynamicComp = (HtmlOutputText) context.
                getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);

        dynamicComp.setValue("dynamically added; now perform a reload");
        dynamicComp.setStyle("background-color: yellow");

        parentContainer.getChildren().add(dynamicComp);

        return null;
    }
}
