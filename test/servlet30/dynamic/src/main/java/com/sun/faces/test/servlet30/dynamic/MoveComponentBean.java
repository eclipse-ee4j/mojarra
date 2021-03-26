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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ManagedBean(name = "moveComponentBean")
public class MoveComponentBean {

    /**
     * Move the child.
     *
     * @param actionEvent the action event.
     */
    public void moveChild(ActionEvent actionEvent) {
        UIComponent destinationContainer = actionEvent.getComponent().getParent();
        UIComponent viewRoot = FacesContext.getCurrentInstance().getViewRoot();

        UIComponent moveableChild = destinationContainer.findComponent("outputText2");

        if (moveableChild == null) {
            moveableChild = viewRoot.findComponent("form1:outputText2");
        }

        if (moveableChild == null) {
            moveableChild = viewRoot.findComponent("form1:subview1:outputText2");
        }
        
        if (moveableChild == null) {
            moveableChild = viewRoot.findComponent("form1:subview2:outputText2");
        }
        
        if (moveableChild == null) {
            moveableChild = viewRoot.findComponent("form1:subview2:subview2b:outputText2");
        }

        if (moveableChild != null) {
            moveableChild.getParent().getChildren().remove(moveableChild);
            destinationContainer.getChildren().add(0, moveableChild);
        }
    }
}
