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

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

@FacesComponent( value = "com.sun.faces.test.servlet30.ajax.Issue2179Component2" )
public class Issue2179Component2 extends UIInput {

    public Issue2179Component2() {
        setRendererType("jakarta.faces.Text");
    }

    public String getFamily() {
        return ("jakarta.faces.Input");
    }

    public void decode(FacesContext context) {
        throw new RuntimeException("decode: A RuntimeException Has Occurred!"); 
    }
}
