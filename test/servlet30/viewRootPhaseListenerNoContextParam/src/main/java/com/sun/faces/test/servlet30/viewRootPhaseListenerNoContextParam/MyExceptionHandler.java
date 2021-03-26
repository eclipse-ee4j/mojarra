/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.viewRootPhaseListenerNoContextParam;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author frederick.kaempfer
 */
public class MyExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler parent;

    static final Logger logger = Logger.getLogger(MyExceptionHandler.class.getName());

    public MyExceptionHandler(ExceptionHandler parent) {
        this.parent = parent;
    }

    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();) {

            ExceptionQueuedEvent event = it.next();
            ExceptionQueuedEventContext context
                    = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();

            logger.log(Level.WARNING, "Exception handled by exception handler: " + t.getMessage());
            appendMessage(context.getContext(), t);
            it.remove();
        }

        getWrapped().handle();
    }
    
    public static final String KEY = "exceptionHandlerMessage";
    
    private void appendMessage(FacesContext context, Throwable t) {
        Map<Object, Object> attrs = context.getAttributes();
        String cur = attrs.containsKey(KEY) ? (String) attrs.get(KEY) : "";
        cur = cur + " " + t.getMessage();
        attrs.put(KEY, cur);
        HttpServletResponse resp = (HttpServletResponse) context.getExternalContext().getResponse();
        resp.addHeader(KEY, t.getMessage());
        
    }

    @Override
    public ExceptionHandler getWrapped() {
        return parent;
    }

}
