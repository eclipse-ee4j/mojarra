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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKitFactory;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.lifecycle.LifecycleImpl;

/**
 * The managed bean for the render tests.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@Named
@RequestScoped
public class RenderBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String getRenderResult1() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext = new ExternalContextImpl((ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());
            LifecycleImpl lifecycle = new LifecycleImpl();
            FacesContextImpl context = new FacesContextImpl(externalContext, lifecycle);

            /*
             * Actual test.
             */
            context.renderResponse();
            assertTrue(context.getRenderResponse());

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
        return "PASSED";
    }

    public String getRenderResult2() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext = new ExternalContextImpl((ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());
            LifecycleImpl lifecycle = new LifecycleImpl();
            FacesContextImpl context = new FacesContextImpl(externalContext, lifecycle);

            /*
             * Actual test.
             */
            context.responseComplete();
            assertTrue(context.getResponseComplete());

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
        return "PASSED";
    }

    public String getRenderResult3() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext = new ExternalContextImpl((ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());
            LifecycleImpl lifecycle = new LifecycleImpl();
            FacesContextImpl context = new FacesContextImpl(externalContext, lifecycle);

            /*
             * Actual test.
             */
            Application application = context.getApplication();
            UIViewRoot root = (UIViewRoot) application.createComponent(UIViewRoot.COMPONENT_TYPE);

            // if no UIViewRoot then null should be returned
            assertTrue(context.getRenderKit() == null);

            // if UIViewRoot is present but has no RenderKitID, null
            // should be rendered
            context.setViewRoot(root);
            assertTrue(context.getRenderKit() == null);

            // UIViewRoot is present, and has an ID for a non existent
            // RenderKit - null should be returned
            root.setRenderKitId("nosuchkit");
            assertTrue(context.getRenderKit() == null);

            // UIViewRoot with valid RenderKit id should return a RenderKit
            root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
            assertTrue(context.getRenderKit() != null);

        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
        return "PASSED";
    }
}
