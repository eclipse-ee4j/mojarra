/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.facelets;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@SessionScoped
@Named
@PermitAll
public class SessionController implements Serializable {
    private static final long serialVersionUID = -5419091806376438376L;
    
    private final String template = "foo";

    public String getTemplate() {
        return template;
    }
    
    public List<String> getTestString() {
        return asList(new String[] {"a", "b", "c"});
    }    
}
