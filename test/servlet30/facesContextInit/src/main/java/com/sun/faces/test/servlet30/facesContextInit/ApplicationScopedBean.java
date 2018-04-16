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

package com.sun.faces.test.servlet30.facesContextInit;

import java.io.Serializable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@ManagedBean(eager = true)
@ApplicationScoped
public class ApplicationScopedBean implements Serializable {

    private static final long serialVersionUID = -7637392264151341963L;

    private String result = "FAILURE";

    public ApplicationScopedBean() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash startupFlash = context.getExternalContext().getFlash();
        result = null != startupFlash ? "SUCCESS" : "FAILURE";

        context.getApplication().subscribeToEvent(PreDestroyApplicationEvent.class, new SystemEventListener() {

            @Override
            public void processEvent(SystemEvent event) throws AbortProcessingException {
                ApplicationScopedBean.this.testPredestroy();
            }

            @Override
            public boolean isListenerForSource(Object source) {
                return true;
            }
        });

    }

    public String getResult() {
        return result;
    }

    public void testPredestroy() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash startupFlash = context.getExternalContext().getFlash();
        result = null != startupFlash ? "SUCCESS" : "FAILURE";

    }
}
