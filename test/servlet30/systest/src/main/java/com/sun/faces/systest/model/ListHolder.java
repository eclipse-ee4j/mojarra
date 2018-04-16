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

package com.sun.faces.systest.model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;


@ManagedBean(name = "listholder")
@SessionScoped
public class ListHolder implements Serializable {

    private List<String[]> list = new ArrayList<String[]>(6);

    {
        list.add(new String[]{"c1"});
        list.add(new String[]{"c1_1"});
        list.add(new String[]{"c1_2"});
    }

    public List getList() {
        return list;
    }
}
