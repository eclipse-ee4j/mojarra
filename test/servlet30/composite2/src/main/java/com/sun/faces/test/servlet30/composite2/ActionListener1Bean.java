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

package com.sun.faces.test.servlet30.composite2;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.inject.Inject;
import javax.inject.Named;


@Named
@SessionScoped
public class ActionListener1Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    protected ActionListener a, b, c;

    @Inject
    private FacesContext context;

    public ActionListener1Bean() {
        a = new ActionListenerImpl("a called ");
        b = new ActionListenerImpl("b called ");
        c = new ActionListenerImpl("c called ");
    }

    public ActionListener getLoginEventListener() {
        return a;
    }

    public ActionListener getLoginEventListener2() {
        return b;
    }

    public ActionListener getCancelEventListener() {
        return c;
    }

    private void appendMessage(String message) {
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        StringBuilder builder = (StringBuilder) requestMap.get("builder");
        if (builder == null) {
            builder = new StringBuilder();
            requestMap.put("builder", builder);
        }

        builder.append(message);
    }

    public String getMessage() {
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        return requestMap.containsKey("builder") ? ((StringBuilder) requestMap.get("builder")).toString() : "no message";
    }

    private class ActionListenerImpl implements ActionListener {

        private final String message;

        private ActionListenerImpl(String message) {
            this.message = message;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            ActionListener1Bean.this.appendMessage(message);
        }
    }
}
