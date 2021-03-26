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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "actionSourceBean")
@RequestScoped
public class ActionSourceBean {
    /**
     * Get the action listener.
     * 
     * @return the action listener.
     */
    public ActionListener getActionListener() {
        return new ActionListener() {
            /**
             * Process the action.
             * 
             * <p>
             *  This action listener will add messages to the context.
             * </p>
             */
            public void processAction(ActionEvent event)
                    throws AbortProcessingException {
                FacesContext ctx = FacesContext.getCurrentInstance();
                UIComponent source = (UIComponent) event.getSource();
                String cid = source.getClientId(ctx);
                ctx.addMessage(cid,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Action Invoked : " + cid,
                        "Action Invoked : " + cid));
            }
        };

    }
}
