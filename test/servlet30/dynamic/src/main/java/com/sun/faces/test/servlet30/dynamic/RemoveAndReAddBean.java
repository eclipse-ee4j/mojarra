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

import com.sun.faces.facelets.tag.jsf.ComponentSupport;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@ManagedBean(name = "removeAndReAddBean")
@RequestScoped
public class RemoveAndReAddBean implements Serializable {

    private String childrenListEmpty = "INITIAL render";
    
    private String markIdEqual = "INITIAL render";

    public void actionListener(ActionEvent event) {
        UIComponent outputText = FacesContext.getCurrentInstance().getViewRoot().findComponent("form:outputText");
        UIComponent parent = outputText.getParent();
        String markId = (String) outputText.getAttributes().get(ComponentSupport.MARK_CREATED);
        int index = parent.getChildren().indexOf(outputText);
        parent.getChildren().remove(outputText);
        Set<UIComponent> set = (Set) parent.getAttributes().get(ComponentSupport.REMOVED_CHILDREN);
        parent.getChildren().add(index, outputText);
        String restoredId = (String) outputText.getAttributes().get(ComponentSupport.MARK_CREATED);
        markIdEqual = markId.equals(restoredId) ? "TRUE" : "FALSE";
        if (set != null) {
            childrenListEmpty = set.isEmpty() ? "TRUE" : "FALSE";
        } else {
            childrenListEmpty = "TRUE";
        }
    }

    public int getIndex() {
        int result = -1;
        UIComponent outputText = FacesContext.getCurrentInstance().getViewRoot().findComponent("form:outputText");
        if (outputText != null) {
            result = outputText.getParent().getChildren().indexOf(outputText);
        }
        return result;
    }
    
    public String getChildrenListEmpty() {
        return childrenListEmpty;
    }

    public String getMarkIdEqual() {
        return markIdEqual;
    }

    public void setMarkIdEqual(String markIdEqual) {
        this.markIdEqual = markIdEqual;
    }
}
