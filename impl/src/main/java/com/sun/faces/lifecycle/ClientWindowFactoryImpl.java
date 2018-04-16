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

package com.sun.faces.lifecycle;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.lifecycle.ClientWindow;
import javax.faces.lifecycle.ClientWindowFactory;

import com.sun.faces.config.WebConfiguration;

public class ClientWindowFactoryImpl extends ClientWindowFactory {
    
    private boolean isClientWindowEnabled = false;
    private WebConfiguration config = null;

    public ClientWindowFactoryImpl() {
        super(null);
        FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().subscribeToEvent(PostConstructApplicationEvent.class,
                         Application.class, new PostConstructApplicationListener());
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
            ClientWindowFactoryImpl.this.postConstructApplicationInitialization();
        }
        
    }
    
    private void postConstructApplicationInitialization() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        config = WebConfiguration.getInstance(extContext);
        String optionValue = config.getOptionValue(WebConfiguration.WebContextInitParameter.ClientWindowMode);
        
        isClientWindowEnabled = (null != optionValue) && "url".equals(optionValue);
    }
    
    
    @Override
    public ClientWindow getClientWindow(FacesContext context) {
        if (!isClientWindowEnabled) {
            return null;
        }
        
        return new ClientWindowImpl();
    }
}
