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

package com.sun.faces.systest.model.ajax;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named("ajaxtag")
@SessionScoped
public class AjaxTagValuesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer count;
    private Boolean checked;
    private String text = "";
    private String[] outArray = { "out1", ":form2:out2", ":out3" };
    private Collection<String> outSet = new LinkedHashSet<String>(Arrays.asList(outArray));
    private String render = "out1";
    private String[] checkedvalues = {};

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Integer getCount() {
        return count++;
    }

    public void setCheckedValues(String[] values) {
        this.checkedvalues = values;
    }

    public String[] getCheckedValues() {
        return checkedvalues;
    }

    public String getCheckedValuesString() {
        String v = "Value: ";
        for (String value : checkedvalues) {
            v = v + value;
        }
        return v;
    }

    public void reset(ActionEvent ae) {
        count = 0;
        checked = false;
        text = "";
    }

    public Collection<String> getRenderList() {
        return outSet;
    }

    public String getRenderOne() {
        return render;
    }

    private String ajaxEvent = "valueChange";

    public void setAjaxEvent(String ajaxEvent) {
        this.ajaxEvent = ajaxEvent;
    }

    public String getAjaxEvent() {
        return ajaxEvent;
    }

}
