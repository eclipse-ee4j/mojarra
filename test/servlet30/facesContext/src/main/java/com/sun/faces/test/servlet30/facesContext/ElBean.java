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

package com.sun.faces.test.servlet30.facesContext;

import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.lifecycle.LifecycleImpl;
import java.io.Serializable;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.*;

/**
 * The managed bean for the EL tests.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "elBean")
@RequestScoped
public class ElBean implements Serializable {

    public String getElResult1() {
        FacesContext currentContext = FacesContext.getCurrentInstance();
        ExternalContextImpl externalContext =
                new ExternalContextImpl(
                (ServletContext) currentContext.getExternalContext().getContext(),
                (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                (HttpServletResponse) currentContext.getExternalContext().getResponse());
        LifecycleImpl lifecycle = new LifecycleImpl();

        FacesContextImpl context = new FacesContextImpl(externalContext, lifecycle);

        /*
         * Actual test.
         */
        ELContextListenerImpl listener = new ELContextListenerImpl();
        Application app = context.getApplication();
        app.addELContextListener(listener);
        context.getELContext();
        assertTrue(listener.wasInvoked());
        return "PASSED";
    }

    private static final class ELContextListenerImpl implements ELContextListener {

        private boolean invoked;

        public void contextCreated(ELContextEvent elContextEvent) {
            invoked = true;
        }

        public boolean wasInvoked() {
            return invoked;
        }
    }
}
