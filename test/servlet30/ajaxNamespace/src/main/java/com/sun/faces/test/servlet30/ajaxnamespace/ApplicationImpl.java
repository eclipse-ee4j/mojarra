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

package com.sun.faces.test.servlet30.ajaxnamespace;

import static javax.faces.component.UIViewRoot.COMPONENT_TYPE;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.component.UIComponent;

public class ApplicationImpl extends ApplicationWrapper {

    public ApplicationImpl(Application parent) {
        super(parent);
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        if (COMPONENT_TYPE.equals(componentType)) {
            return new NamingContainerViewRoot();
        }

        return getWrapped().createComponent(componentType);
    }
}
