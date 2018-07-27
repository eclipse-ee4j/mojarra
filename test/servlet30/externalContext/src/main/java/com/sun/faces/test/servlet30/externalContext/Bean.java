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

package com.sun.faces.test.servlet30.externalContext;

import static java.lang.Boolean.TRUE;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FacesContext facesContext;

    public String getName() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("httpOnly", TRUE);
        facesContext.getExternalContext().addResponseCookie("csftaebName", "csftaebValue", m);
        return "TestBean";
    }

    public String submit() {
        String stringWithSpecialChars = "日א";
        return "issue2440?param=" + stringWithSpecialChars + "&faces-redirect=true";
    }

    public String getStringWithSpecialCharacters() {
        String stringWithSpecialChars = "日א";
        return stringWithSpecialChars;
    }
}
