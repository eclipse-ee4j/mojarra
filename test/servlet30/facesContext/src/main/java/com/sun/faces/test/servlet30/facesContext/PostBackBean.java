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
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.*;

/**
 * The managed bean for the postBack tests.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "postBackBean")
@RequestScoped
public class PostBackBean implements Serializable {

    public String getPostBackResult1() {
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
        String key = "com.sun.faces.context.FacesContextImpl_POST_BACK";
        assertTrue(!context.isPostback());
        assertTrue(context.getAttributes().containsKey(key));
        assertTrue(Boolean.FALSE.equals(context.getAttributes().get(key)));
        return "PASSED";
    }

    public String getPostBackResult2() {
        FacesContext context = FacesContext.getCurrentInstance();

        /*
         * Actual test.
         */
        String key = "com.sun.faces.context.FacesContextImpl_POST_BACK";
        if (context.isPostback()) {
            assertTrue(context.getAttributes().containsKey(key));
            assertTrue(Boolean.TRUE.equals(context.getAttributes().get(key)));
            return "PASSED";
        }
        return "";
    }
}
