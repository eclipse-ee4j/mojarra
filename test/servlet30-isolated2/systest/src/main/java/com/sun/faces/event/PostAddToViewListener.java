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

package com.sun.faces.event;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.inject.Named;

@Named
@SessionScoped
public class PostAddToViewListener implements SystemEventListener, Serializable {

    private static final long serialVersionUID = 1L;
    private boolean installed;

    public String getInstallEvent() {
        if (!installed) {
            FacesContext.getCurrentInstance().getApplication().subscribeToEvent(PostAddToViewEvent.class, HtmlInputText.class, this);
            installed = true;
        }

        return "";
    }

    public String getUninstallEvent() {
        FacesContext.getCurrentInstance().getApplication().unsubscribeFromEvent(PostAddToViewEvent.class, HtmlInputText.class, this);
        installed = false;
        return "";
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        requestMap.put("message", event.getClass().getName());

    }

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof HtmlInputText;
    }

}
