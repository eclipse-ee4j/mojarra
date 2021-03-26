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

package com.sun.faces.test.servlet30.ajax;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

@ManagedBean(name = "bean")
@SessionScoped
public class Bean {

    private final Collection<SelectItem> items;
    private String radioValue = "blue";
    private String status = null;

    public Bean() {
        Set<SelectItem> initialItems = new LinkedHashSet<SelectItem>();
        initialItems.add(new SelectItem("red"));
        initialItems.add(new SelectItem("blue"));
        initialItems.add(new SelectItem("white"));
        items = Collections.unmodifiableSet(initialItems);
    }

    public Collection<SelectItem> getItems() {
        return items;
    }

    public void setRadioValue(String radioValue) {
        this.radioValue = radioValue;
    }

    public String getRadioValue() {
        return radioValue;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void processLink(ActionEvent ae) {
        status = "LINK ACTION";
    }

    public void processRadio(ValueChangeEvent vce) {
        status = "RADIO:"+(String)vce.getNewValue();
    }

    public void processIt(AjaxBehaviorEvent event) {
        setRadioValue("red");
    }
    
    public String getThrowExceptionOnAjax() {
        FacesContext context = FacesContext.getCurrentInstance();
        PartialViewContext partialContext = context.getPartialViewContext();
        if (null != partialContext) {
            if (partialContext.isAjaxRequest()) {
                throw new RuntimeException("Intentionally throwing exception on ajax request");
            }
        }
        
        String result = "not an ajax request";
        
        return result;
    }
}
