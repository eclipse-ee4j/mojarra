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

package com.sun.faces.test.servlet30.charactercombat;

import java.util.Iterator;

import javax.el.ELContext;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.inject.Named;

/**
 * <p>
 * Backing bean for wizard style navigation. This class provides methods that you can point to from
 * your wizard buttons that will return true or false depending on the current page in the
 * application.
 * </p>
 */
@Named
@RequestScoped
public class WizardButtons {

    /**
     * <p>
     * Check to see whether the current page should have a back button
     * </p>
     *
     * @return true if the current page has a "back" page.
     */
    public boolean isHasBack() {
        FacesContext realContext = FacesContext.getCurrentInstance(), copyContext = createShadowFacesContext(realContext);
        NavigationHandler nav = copyContext.getApplication().getNavigationHandler();
        nav.handleNavigation(copyContext, null, "back");
        return compareUIViewRoots(realContext.getViewRoot(), copyContext.getViewRoot());
    }

    /**
     * <p>
     * Check to see whether the current page should have a next button
     * </p>
     *
     * @return true if the current page has a "next" page.
     */
    public boolean isHasNext() {
        FacesContext realContext = FacesContext.getCurrentInstance(), copyContext = createShadowFacesContext(realContext);
        NavigationHandler nav = copyContext.getApplication().getNavigationHandler();
        nav.handleNavigation(copyContext, null, "next");
        return compareUIViewRoots(realContext.getViewRoot(), copyContext.getViewRoot());
    }

    /**
     * <p>
     * Check to see whether the current page should have a finish button
     * </p>
     *
     * @return true if the current page should have a "finish" button instead of a "next" button
     */
    public boolean isFinishPage() {
        FacesContext realContext = FacesContext.getCurrentInstance(), copyContext = createShadowFacesContext(realContext), nextCopyContext;
        NavigationHandler nav = copyContext.getApplication().getNavigationHandler();
        // get the next outcome
        nav.handleNavigation(copyContext, null, "next");
        nextCopyContext = createShadowFacesContext(copyContext);
        nav.handleNavigation(nextCopyContext, null, "next");
        return compareUIViewRoots(copyContext.getViewRoot(), nextCopyContext.getViewRoot());
    }

    /**
     * <p>
     * Get the label for the "next" button.
     * </p>
     *
     * @return String next button label
     */
    public String getNextLabel() {
        String result = "Next >";
        if (isFinishPage()) {
            result = "Finish";
        }
        return result;
    }

    /**
     * <p>
     * Take two View roots and compare them.
     * </p>
     *
     * @param one the first ViewRoot
     * @param two the second ViewRoot
     *
     * @return boolean the result of the comparison.
     */
    public boolean compareUIViewRoots(UIViewRoot one, UIViewRoot two) {
        if (null == one && null == two) {
            return true;
        }
        if (null != one && null != two) {
            if (null == one.getViewId() && null == two.getViewId()) {
                return true;
            }
            if (null != one.getViewId() && null != two.getViewId()) {
                return one.getViewId().equals(two.getViewId());
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * <p>
     * createShadowFacesContext creates a shallow copy of the argument FacesContext, but with a deep
     * copy of the viewRoot property. This allows us to call the NavigationHandler.handleNavigaton
     * method without modifying the real FacesContext.
     * </p>
     *
     * @param context the FacesContext to be copied
     *
     * @return FacesContext shallow copy of FacesContext
     */
    public FacesContext createShadowFacesContext(FacesContext context) {
        // final FacesContext oldContext = context;

        class FacesContextImpl extends FacesContext {
            private FacesContext oldContext = null;
            private UIViewRoot root = null;
            private Application application = null;

            public FacesContextImpl(FacesContext context) {
                this.oldContext = context;
                this.root = oldContext.getViewRoot();
                this.application = oldContext.getApplication();
            }

            @Override
            public Application getApplication() {
                return this.application;
            }

            @Override
            public Iterator<String> getClientIdsWithMessages() {
                return oldContext.getClientIdsWithMessages();
            }

            @Override
            public ExternalContext getExternalContext() {
                return oldContext.getExternalContext();
            }

            @Override
            public Severity getMaximumSeverity() {
                return oldContext.getMaximumSeverity();
            }

            @Override
            public Iterator<FacesMessage> getMessages() {
                return oldContext.getMessages();
            }

            @Override
            public Iterator<FacesMessage> getMessages(String clientId) {
                return oldContext.getMessages(clientId);
            }

            @Override
            public RenderKit getRenderKit() {
                return oldContext.getRenderKit();
            }

            @Override
            public boolean getRenderResponse() {
                return oldContext.getRenderResponse();
            }

            @Override
            public boolean getResponseComplete() {
                return oldContext.getResponseComplete();
            }

            @Override
            public ResponseStream getResponseStream() {
                return oldContext.getResponseStream();
            }

            @Override
            public void setResponseStream(ResponseStream responseStream) {
                oldContext.setResponseStream(responseStream);
            }

            @Override
            public ResponseWriter getResponseWriter() {
                return oldContext.getResponseWriter();
            }

            @Override
            public void setResponseWriter(ResponseWriter responseWriter) {
                oldContext.setResponseWriter(responseWriter);
            }

            @Override
            public UIViewRoot getViewRoot() {
                return this.root;
            }

            @Override
            public void setViewRoot(UIViewRoot root) {
                this.root = root;
            }

            @Override
            public void addMessage(String clientId, FacesMessage message) {
                oldContext.addMessage(clientId, message);
            }

            @Override
            public void release() {
            }

            @Override
            public void renderResponse() {
            }

            @Override
            public ELContext getELContext() {
                return oldContext.getELContext();
            }

            @Override
            public void responseComplete() {
            }
        }

        return new FacesContextImpl(context);
    }
}
