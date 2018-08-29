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

import javax.inject.Named;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;

@Named("ajaxcount")
@SessionScoped
public class CountBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer count1 = 0;
    private Integer count2 = 0;
    private Integer count3 = 0;
    private Integer count4 = 0;
    private Integer count5 = 0;

    public void resetCount(ActionEvent ae) {
        count1 = 0;
        count2 = 0;
        count3 = 0;
        count4 = 0;
        count5 = 0;
    }

    public Integer getCount1() {
        return count1++;
    }

    public Integer getCount2() {
        return count2++;
    }

    public Integer getCount3() {
        return count3++;
    }

    public Integer getCount4() {
        return count4++;
    }

    public Integer getCount5() {
        return count5++;
    }
}
