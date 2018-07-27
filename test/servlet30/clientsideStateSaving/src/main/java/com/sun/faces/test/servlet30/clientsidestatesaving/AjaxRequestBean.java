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

package com.sun.faces.test.servlet30.clientsidestatesaving;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;

@Named("ajaxrequest")
@SessionScoped
public class AjaxRequestBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer count = 0;

    private String echo = "echo";
    private String echo1 = "";
    private String echo2 = "";
    private String echo3 = "";
    private String echo4 = "";

    public String getEcho1() {
        return echo1;
    }

    public void setEcho1(String echo1) {
        this.echo1 = echo1;
    }

    public String getEcho2() {
        return echo2;
    }

    public void setEcho2(String echo2) {
        this.echo2 = echo2;
    }

    public String getEcho3() {
        return echo3;
    }

    public void setEcho3(String echo3) {
        this.echo3 = echo3;
    }

    public String getEcho4() {
        return echo4;
    }

    public void setEcho4(String echo4) {
        this.echo4 = echo4;
    }

    public String getEcho() {
        return echo;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public void echoValue(ValueChangeEvent event) {
        String str = (String) event.getNewValue();
        echo = str;
    }

    public void resetEcho(ActionEvent ae) {
        echo = "reset";
        echo1 = "reset";
        echo2 = "reset";
        echo3 = "reset";
        echo4 = "reset";
    }

    public Integer getCount() {
        return count++;
    }

    public void resetCount(ActionEvent ae) {
        count = 0;
    }

}
