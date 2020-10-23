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
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpSession;

@ManagedBean(name = "issue2648Bean")
@SessionScoped
public class Issue2648Bean implements Serializable {

    public void processAjax(AjaxBehaviorEvent event) throws AbortProcessingException {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = fctx.getExternalContext();
        HttpSession session = (HttpSession)ectx.getSession(true);
        String temp1 = session.getServletContext().getContextPath() + "/faces/issue2648-1.xhtml";
        String temp2 = session.getServletContext().getContextPath() + "/faces/issue2648-2.xhtml";
        try {
            boolean complete1 = fctx.getResponseComplete(); 
            ectx.redirect(temp1); 
            boolean complete2 = fctx.getResponseComplete(); 
            ectx.redirect(temp2); 
            boolean complete3 = fctx.getResponseComplete(); 
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                session.setAttribute("IllegalStateException", "true");
            }
        }
    }

    public void process(ActionEvent event) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = fctx.getExternalContext();
        HttpSession session = (HttpSession)ectx.getSession(true);
        String temp1 = session.getServletContext().getContextPath() + "/faces/issue2648-1.xhtml";
        String temp2 = session.getServletContext().getContextPath() + "/faces/issue2648-2.xhtml";
        try {
            boolean complete1 = fctx.getResponseComplete(); 
            ectx.redirect(temp1); 
            boolean complete2 = fctx.getResponseComplete(); 
            ectx.redirect(temp2); 
            boolean complete3 = fctx.getResponseComplete(); 
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
