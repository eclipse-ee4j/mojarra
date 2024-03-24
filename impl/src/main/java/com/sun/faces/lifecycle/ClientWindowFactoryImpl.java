/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.lifecycle;

import com.sun.faces.application.JavaFlowLoaderHelper;

import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.lifecycle.ClientWindowFactory;

public class ClientWindowFactoryImpl extends ClientWindowFactory {

    private boolean isClientWindowEnabled = false;

    public ClientWindowFactoryImpl() {
        super(null);
        FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().subscribeToEvent(PostConstructApplicationEvent.class, Application.class, new PostConstructApplicationListener());
    }

    public ClientWindowFactoryImpl(boolean ignored) {
        super(null);
        isClientWindowEnabled = false;
    }

    private class PostConstructApplicationListener implements SystemEventListener {

        @Override
        public boolean isListenerForSource(Object source) {
            return source instanceof Application;
        }

        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            postConstructApplicationInitialization();
        }

    }

    private void postConstructApplicationInitialization() {
        FacesContext context = FacesContext.getCurrentInstance();
        String optionValue = ContextParam.CLIENT_WINDOW_MODE.getValue(context);
        isClientWindowEnabled = "url".equals(optionValue) || JavaFlowLoaderHelper.isClientWindowModeForciblyEnabled(context);
    }

    @Override
    public ClientWindow getClientWindow(FacesContext context) {
        if (!isClientWindowEnabled) {
            return null;
        }

        return new ClientWindowImpl();
    }
}
