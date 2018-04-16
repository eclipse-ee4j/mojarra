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

import javax.faces.bean.ManagedBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.faces.event.ValueChangeEvent;

@ManagedBean
@RequestScoped
public class Issue3833Bean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final List<String> list1 = Arrays.asList("1", "2");
    private static final List<String> list2 = Arrays.asList("3", "4");

    private static final List<List<String>> list = Arrays.asList(list1, list2);

    private String message = "";

    public List<List<String>> getList() {
      return list;
    }

    public List<String> getShortList() {
        return list1;
    }

    public void listener(ValueChangeEvent vce) {
        setMessage(vce.getOldValue() + " -> " + vce.getNewValue());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
