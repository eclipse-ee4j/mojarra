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

package com.sun.faces.component;

import java.util.Map;
import javax.faces.application.Application;
import javax.inject.Named;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;

@Named("viewScopedBean")
@ViewScoped
public class ViewScopedComponentBindingBean {

    private static final String REQUEST_KEY = "com.sun.faces.component.ViewScopedComponentBindingBeanKey";

    public ViewScopedComponentBindingBean() {
        Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        StringBuilder sb = (StringBuilder) requestMap.get(REQUEST_KEY);
        if (null == sb) {
            sb = new StringBuilder();
            requestMap.put(REQUEST_KEY, sb);
        }
        sb.append(" ctor called ");

    }

    public String getCtorMessage() {
        String result = "";

        Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        StringBuilder sb = (StringBuilder) requestMap.get(REQUEST_KEY);
        if (null != sb) {
            result = sb.toString();
        }

        return result;

    }

    private UICommand button = null;

    public void setCommandButton(UICommand button) {
        this.button = button;
    }

    public UICommand getCommandButton() {
        if (null == button) {
            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();
            button = (UICommand) app.createComponent(context, "javax.faces.Command", "javax.faces.Button");
        }
        return button;
    }
}
