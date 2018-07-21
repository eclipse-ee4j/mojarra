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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named
@SessionScoped
public class Issue2500Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean visible;
    private String msg;

    public void toggle(ActionEvent ae) {
        if (visible) {
            visible = false;
        } else if (!visible) {
            visible = true;
        }

        determineViewStateValueOccurences();
    }

    private void determineViewStateValueOccurences() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String[]> requestParams = context.getExternalContext().getRequestParameterValuesMap();
        Set<Entry<String, String[]>> s = requestParams.entrySet();
        Iterator<Entry<String, String[]>> it = s.iterator();
        while (it.hasNext()) {
            Entry<String, String[]> m = it.next();
            String key = m.getKey();
            String[] values = m.getValue();
            if (key.equals("javax.faces.ViewState")) {
                if (null != values) {
                    if (values.length > 1) {
                        msg = "javax.faces.ViewState Has More than One Value";
                    } else if (values.length == 1) {
                        msg = "javax.faces.ViewState Has One Value";
                    }
                }
                break;
            }
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
