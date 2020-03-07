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

package com.sun.faces.application.view;

import static com.sun.faces.util.Util.getFacesMapping;
import static com.sun.faces.util.Util.getStateManager;
import static com.sun.faces.util.Util.isPrefixMapped;
import static com.sun.faces.util.Util.notNull;
import static jakarta.faces.component.UIViewRoot.COMPONENT_TYPE;
import static java.util.logging.Level.FINE;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p>
 * This represents how a particular page description language is to be rendered/restored.
 * <p>
 */
public abstract class ViewHandlingStrategy extends ViewDeclarationLanguage {

    private static final Logger logger = FacesLogger.APPLICATION.getLogger();

    protected ApplicationAssociate associate;
    protected WebConfiguration webConfig;

    // ------------------------------------------------------------ Constructors

    public ViewHandlingStrategy() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        webConfig = WebConfiguration.getInstance(ctx.getExternalContext());
        associate = ApplicationAssociate.getInstance(ctx.getExternalContext());
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @see ViewDeclarationLanguage#restoreView(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public UIViewRoot restoreView(FacesContext ctx, String viewId) {

        ExternalContext extContext = ctx.getExternalContext();

        String mapping = getFacesMapping(ctx);
        UIViewRoot viewRoot = null;

        // Mapping could be null if a non-faces request triggered this response.
        if (extContext.getRequestPathInfo() == null && mapping != null && isPrefixMapped(mapping)) {
            // This was probably an initial request.
            // Send them off to the root of the web application.
            try {
                ctx.responseComplete();
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, "Response Complete for" + viewId);
                }
                if (!extContext.isResponseCommitted()) {
                    extContext.redirect(extContext.getRequestContextPath());
                }
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        } else {
            // this is necessary to allow decorated impls.
            ViewHandler outerViewHandler = ctx.getApplication().getViewHandler();
            String renderKitId = outerViewHandler.calculateRenderKitId(ctx);

            viewRoot = getStateManager(ctx).restoreView(ctx, viewId, renderKitId);
        }

        return viewRoot;
    }

    /**
     * @see ViewDeclarationLanguage#createView(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public UIViewRoot createView(FacesContext ctx, String viewId) {

        notNull("context", ctx);

        UIViewRoot result = (UIViewRoot) ctx.getApplication().createComponent(COMPONENT_TYPE);

        Locale locale = null;
        String renderKitId = null;

        // use the locale from the previous view if is was one which will be
        // the case if this is called from NavigationHandler. There wouldn't be
        // one for the initial case.
        if (ctx.getViewRoot() != null) {
            locale = ctx.getViewRoot().getLocale();
            renderKitId = ctx.getViewRoot().getRenderKitId();
        }

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "Created new view for " + viewId);
        }

        // PENDING(): not sure if we should set the RenderKitId here.
        // The UIViewRoot ctor sets the renderKitId to the default
        // one.
        // If there was no locale from the previous view, calculate the locale for this view.
        if (locale == null) {
            locale = ctx.getApplication().getViewHandler().calculateLocale(ctx);
            if (logger.isLoggable(FINE)) {
                logger.fine("Locale for this view as determined by calculateLocale " + locale.toString());
            }
        } else {
            if (logger.isLoggable(FINE)) {
                logger.fine("Using locale from previous view " + locale.toString());
            }
        }

        if (renderKitId == null) {
            renderKitId = ctx.getApplication().getViewHandler().calculateRenderKitId(ctx);

            if (logger.isLoggable(FINE)) {
                logger.fine("RenderKitId for this view as determined by calculateRenderKitId " + renderKitId);
            }
        } else {
            if (logger.isLoggable(FINE)) {
                logger.fine("Using renderKitId from previous view " + renderKitId);
            }
        }

        result.setLocale(locale);
        result.setRenderKitId(renderKitId);
        result.setViewId(viewId);

        return result;
    }

    /**
     *
     * @param viewId the view ID
     * @return <code>true</code> if this <code>ViewHandlingStrategy</code> handles the the view type represented by
     * <code>viewId</code>
     */
    public abstract boolean handlesViewId(String viewId);

}
