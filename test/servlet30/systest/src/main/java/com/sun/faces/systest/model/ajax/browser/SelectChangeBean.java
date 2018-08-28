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

package com.sun.faces.systest.model.ajax.browser;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("selectChange")
@RequestScoped
@SuppressWarnings("unused")
public class SelectChangeBean {

    private String string = "Pending";
    private String[] sarray;
    private boolean bool;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String[] getSarray() {
        return sarray;
    }

    public void setSarray(String[] sarray) {
        this.sarray = sarray;
        if (sarray.length == 0) {
            string = "Pending";
        } else {
            string = "";
        }
        for (String str : sarray) {
            if (!"".equals(string)) {
                string = string + " ";
            }
            string = string + str;
        }
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
        if (bool) {
            string = "PASSED";
        } else {
            string = "Pending";
        }
    }
}
