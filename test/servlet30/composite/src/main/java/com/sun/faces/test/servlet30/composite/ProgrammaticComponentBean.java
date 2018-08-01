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

package com.sun.faces.test.servlet30.composite;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class ProgrammaticComponentBean {

    public String getTest() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("pi", (float) 3.14);
        attrs.put("pagecontent", "" + System.currentTimeMillis());

        FacesContext context = FacesContext.getCurrentInstance();

        UIComponent c = context.getApplication()
                               .getViewHandler()
                               .getViewDeclarationLanguage(context, context.getViewRoot().getViewId())
                               .createComponent(context,
                                   "http://java.sun.com/jsf/composite/programmatic",
                                   "programmaticComponent", attrs);

        return null == c ? "FAILURE" : "SUCCESS";
    }
}
