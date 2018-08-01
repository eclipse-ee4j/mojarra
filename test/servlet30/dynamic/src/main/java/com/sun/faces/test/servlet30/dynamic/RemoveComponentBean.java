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

package com.sun.faces.test.servlet30.dynamic;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class RemoveComponentBean {

    @Inject
    private FacesContext context;

    public void validateDeletion(FacesContext context, UIComponent comp, Object value) {
        UIComponent button = findButton(context);
        if (button != null) {
            throw new ValidatorException(new FacesMessage("cbutton should not be found"));
        }
    }

    public void removePreRenderComponent(ComponentSystemEvent event) {
        UIComponent buttonParent;
        UIComponent button = findButton(context);

        if (button != null) {
            buttonParent = button.getParent();
            buttonParent.getChildren().remove(button);
        }
    }

    private UIComponent findButton(FacesContext context) {
        char sep = UINamingContainer.getSeparatorChar(context);
        UIComponent result = context.getViewRoot().findComponent(sep + "form" + sep + "cbutton");
        return result;
    }
}
