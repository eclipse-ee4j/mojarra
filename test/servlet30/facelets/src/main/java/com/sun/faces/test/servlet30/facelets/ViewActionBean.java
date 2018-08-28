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

package com.sun.faces.test.servlet30.facelets;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class ViewActionBean {

    @Inject
    @ManagedProperty("#{facesContext}")
    private FacesContext context;

    public String action() {
        return "viewActionResult";
    }

    public String pageA() {
        getContext().getExternalContext().getRequestMap().put("message", "pageA action");
        return "pageA";
    }

    public String pageAExplicitRedirect() {
        getContext().getExternalContext().getRequestMap().put("message", "pageA explicit redirect");
        return "pageAExplicitRedirect";
    }

    public String returnEmpty() {
        getContext().getExternalContext().getRequestMap().put("message", "pageA empty");
        return "";
    }

    public String returnNull() {
        getContext().getExternalContext().getRequestMap().put("message", "pageA null");
        return null;
    }

    public FacesContext getContext() {
        return context;
    }

    public void setContext(FacesContext context) {
        this.context = context;
    }

    private static class ActionListenerImpl implements ActionListener {

        private final String id;

        private ActionListenerImpl(String id) {
            this.id = id;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

            String message = (String) sessionMap.get("message");
            message = (null == message) ? "" : message + " ";
            message = message + id + " " + event.getComponent().getId();
            sessionMap.put("message", message);
        }
    }

    public ActionListener getActionListener1() {
        return new ActionListenerImpl("1");
    }

    public ActionListener getActionListener2() {
        return new ActionListenerImpl("2");
    }

    public void actionListenerMethod(ActionEvent event) {
        ActionListenerImpl actionListener = new ActionListenerImpl("method");
        actionListener.processAction(event);
    }
}
