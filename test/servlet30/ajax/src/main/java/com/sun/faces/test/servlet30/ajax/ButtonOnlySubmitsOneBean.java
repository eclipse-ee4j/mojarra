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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Named;


@Named
@RequestScoped
public class ButtonOnlySubmitsOneBean {

    private List<String> strings;

    @PostConstruct
    public void init() {
        strings = new ArrayList<>();
        strings.add("value1");
        strings.add("value2");
    }

    public void listener(ActionEvent event) {
        strings.remove(0);
    }

    public String getValue() {
        StringBuilder result = new StringBuilder();
        for (String string : strings) {
            result.append(string);
            result.append(",");
        }
        return result.toString();
    }
}
