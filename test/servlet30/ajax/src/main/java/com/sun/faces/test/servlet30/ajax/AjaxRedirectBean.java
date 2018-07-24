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

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class AjaxRedirectBean {

    public void causeRedirect() {
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();

        StringBuilder sb = new StringBuilder();
        sb.append(extContext.getRequestScheme())
          .append("://")
          .append(extContext.getRequestServerName())
          .append(":")
          .append(extContext.getRequestServerPort())
          .append(extContext.getRequestContextPath())
          .append("/ajaxRedirect02.html");

        try {
            extContext.redirect(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(AjaxRedirectBean.class.getName()).log(SEVERE, null, ex);
        }

    }

}
