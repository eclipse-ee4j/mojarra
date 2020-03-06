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

package com.sun.faces.test.servlet30.flash;

import java.util.Map;
import jakarta.annotation.PreDestroy;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostKeepFlashValueEvent;
import javax.faces.event.PostPutFlashValueEvent;
import javax.faces.event.PreClearFlashEvent;
import javax.faces.event.PreRemoveFlashValueEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import java.io.Serializable;

@ManagedBean
@SessionScoped
public class SessionScopedBean implements Serializable {

    private FlashListener listener;

    public SessionScopedBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        listener = new FlashListener();

        app.subscribeToEvent(PreRemoveFlashValueEvent.class, listener);
        app.subscribeToEvent(PostKeepFlashValueEvent.class, listener);
        app.subscribeToEvent(PreClearFlashEvent.class, listener);
        app.subscribeToEvent(PostPutFlashValueEvent.class, listener);

    }

    @PreDestroy
    public void destroy() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();

        app.unsubscribeFromEvent(PreRemoveFlashValueEvent.class, listener);
        app.unsubscribeFromEvent(PostKeepFlashValueEvent.class, listener);
        app.unsubscribeFromEvent(PreClearFlashEvent.class, listener);
        app.unsubscribeFromEvent(PostPutFlashValueEvent.class, listener);

    }

    public String getMessage() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        String result = (sessionMap.containsKey("builder")) ? ((StringBuilder) sessionMap.get("builder")).toString() : "no message";

        return result;
    }

    public static class FlashListener implements SystemEventListener {

        public boolean isListenerForSource(Object source) {
            return ((source instanceof String) || (source instanceof Map));
        }

        public void processEvent(SystemEvent event) throws AbortProcessingException {
            appendMessage("[received " + event.getClass().getName() + " source:"
                    + event.getSource() + "]");

        }

        private void appendMessage(String message) {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
            StringBuilder builder;
            builder = (StringBuilder) sessionMap.get("builder");
            if (null == builder) {
                builder = new StringBuilder();
                sessionMap.put("builder", builder);
            }
            builder.append(message);
        }

    }

}
