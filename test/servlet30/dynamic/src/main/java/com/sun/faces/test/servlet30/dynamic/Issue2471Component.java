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

package com.sun.faces.test.servlet30.dynamic;

import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

/**
 * The component for issue #2471.
 */
@FacesComponent("com.sun.faces.test.servlet30.dynamic.Issue2471Component")
@ResourceDependency(target = "head", name = "issue2471.js")
public class Issue2471Component extends UIComponentBase {

    public Issue2471Component() {
    }

    @Override
    public String getFamily() {
        return "com.sun.faces.test.servlet30.dynamic.Issue2471Component";
    }

    @Override
    public Object saveTransientState(FacesContext context) {
        return null;
    }

    @Override
    public void restoreTransientState(FacesContext context, Object state) {
    }
}
