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

package com.sun.faces.facelets.tag.jsp;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;
import javax.faces.webapp.FacesServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;




public class ForwardHandler extends TagHandler {

    private final TagAttribute page;
    private static final Logger LOGGER =
          Logger.getLogger("jakarta.faces.webapp", "jakarta.faces.LogStrings");


    public ForwardHandler(TagConfig config) {
        super(config);

        this.page = this.getRequiredAttribute("page");

    }

    public void apply(FaceletContext ctx, UIComponent component) throws IOException {
        nextHandler.apply(ctx, component);
        FacesContext context = ctx.getFacesContext();
        ExternalContext extContext = context.getExternalContext();
        // Test for portlet or servlet
        Object obj = extContext.getContext();
        if (obj instanceof ServletContext) {
            ServletContext servletContext = (ServletContext) obj;
            String path = this.page.getValue(ctx);
            RequestDispatcher rd = servletContext.getRequestDispatcher(path);
            final Map<String, ValueExpression> params = ParamHandler.getParams(context, component);
            HttpServletRequest req = (HttpServletRequest) extContext.getRequest();
            if (!params.isEmpty()) {
                req = new WrapHttpServletRequestToAddParams(context, params, req);
            }
            FacesContextFactory facesContextFactory = null;
            Lifecycle lifecycle = null;

            // Acquire our FacesContextFactory instance
            try {
                facesContextFactory = (FacesContextFactory)
                    FactoryFinder.getFactory
                    (FactoryFinder.FACES_CONTEXT_FACTORY);
            } catch (FacesException e) {
                ResourceBundle rb = LOGGER.getResourceBundle();
                String msg = rb.getString("severe.webapp.facesservlet.init_failed");
                Throwable rootCause = (e.getCause() != null) ? e.getCause() : e;
                LOGGER.log(Level.SEVERE, msg, rootCause);
                throw new IOException(msg);
            }

            // Acquire our Lifecycle instance
            try {
                LifecycleFactory lifecycleFactory = (LifecycleFactory)
                      FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
                String lifecycleId;

                // This is a bug.  Custom lifecycles configured via a <init-parameter>
                // are not available at this point.  The correct solution
                // would be to have some way to get the currently active Lifecycle
                // instance.
                lifecycleId = servletContext.getInitParameter
                                 (FacesServlet.LIFECYCLE_ID_ATTR);

                if (lifecycleId == null) {
                    lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
                }
                lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
            } catch (FacesException e) {
                Throwable rootCause = e.getCause();
                if (rootCause == null) {
                    throw e;
                } else {
                    throw new IOException(e.getMessage(), rootCause);
                }
            }

            FacesContext newFacesContext = null;

            try {
                newFacesContext = facesContextFactory.
                        getFacesContext(servletContext, req,
                                        (ServletResponse) extContext.getResponse(),
                                        lifecycle);
                WrapFacesContextToAllowSetCurrentInstance.doSetCurrentInstance(newFacesContext);
                rd.forward(req, (ServletResponse) extContext.getResponse());
            } catch (ServletException ex) {
                throw new IOException(ex);
            } finally {
                if (null != newFacesContext) {
                    newFacesContext.release();
                }
                WrapFacesContextToAllowSetCurrentInstance.doSetCurrentInstance(context);
                context.responseComplete();
            }

        }

    }

    private static final class WrapFacesContextToAllowSetCurrentInstance extends FacesContextWrapper {

        private FacesContext wrapped;

        public WrapFacesContextToAllowSetCurrentInstance(FacesContext wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public FacesContext getWrapped() {
            return this.wrapped;
        }

        private static void doSetCurrentInstance(FacesContext currentInstance) {
            setCurrentInstance(currentInstance);
        }


    }

}
