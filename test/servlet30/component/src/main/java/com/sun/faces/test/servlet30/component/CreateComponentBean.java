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

package com.sun.faces.test.servlet30.component;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@ApplicationScoped
public class CreateComponentBean {

    public String getDoCreateComponent() {

        FacesContext context = FacesContext.getCurrentInstance();

        HtmlInputText inputText = (HtmlInputText) context.getApplication()
                                                         .getViewHandler()
                                                         .getViewDeclarationLanguage(context, context.getViewRoot()
                                                         .getViewId())
                                                         .createComponent(context, "http://java.sun.com/jsf/html", "inputText", null);

        if ("javax.faces.Text".equals(inputText.getRendererType())) {
            return "SUCCESS";
        }

        return "FAILED";
    }
}
