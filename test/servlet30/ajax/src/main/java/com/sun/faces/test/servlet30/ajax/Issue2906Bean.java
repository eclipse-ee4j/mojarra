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
import java.util.List;
import java.util.Vector;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class Issue2906Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int currentView = 1;

    public String chooseView(int newView) {
        currentView = newView;
        return null;
    }

    public List<Integer> getCurrentView() {
        Vector<Integer> v = new Vector<Integer>();

        int counter = 10 - currentView;
        if (counter <= 0) {
            counter = 1;
        }

        for (int index = 0; index <= counter; index++) {
            v.add(currentView + 1);
        }

        return v;
    }
}
