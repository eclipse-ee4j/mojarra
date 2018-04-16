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

package com.sun.faces.test.servlet30.customviewrootviewscope;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

public class ApplicationImpl extends ApplicationWrapper {

    private final Application parent;

    public ApplicationImpl(Application parent) {
        this.parent = parent;
    }

    @Override
    public Application getWrapped() {
        return parent;
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        UIComponent result;
        if (UIViewRoot.COMPONENT_TYPE.equals(componentType)) {
            result = new NamingContainerViewRoot();
        } else {
            result = parent.createComponent(componentType);
        }
        return result;
    }
}
