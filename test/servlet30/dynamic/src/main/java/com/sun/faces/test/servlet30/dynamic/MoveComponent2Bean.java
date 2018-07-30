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

package com.sun.faces.test.servlet30.dynamic;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class MoveComponent2Bean {

    private String text = "This should be bold";

    public void update() {
        text = "The text should remain bold";
    }

    public String getStateSavingMode() {
        return FacesContext.getCurrentInstance().getViewRoot().initialStateMarked() ? "PSS" : "FSS";
    }

    public String getText() {
        return text;
    }
}
