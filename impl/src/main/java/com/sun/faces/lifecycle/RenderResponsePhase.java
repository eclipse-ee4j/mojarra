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

// RenderResponsePhase.java

package com.sun.faces.lifecycle;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.DebugUtil;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PostRenderViewEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <B>Lifetime And Scope</B>
 * <P>
 * Same lifetime and scope as DefaultLifecycleImpl.
 *
 */

public class RenderResponsePhase extends Phase {

    // Log instance for this class
    private static Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    // ---------------------------------------------------------- Public Methods

    @Override
    public void execute(FacesContext facesContext) throws FacesException {

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Entering RenderResponsePhase");
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("About to render view " + facesContext.getViewRoot().getViewId());
        }
        // For requests intended to produce a partial response, we need prohibit
        // writing any content outside of the view itself (f:view).
        facesContext.getPartialViewContext();

        try {

            ViewHandler vh = facesContext.getApplication().getViewHandler();

            ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(facesContext, facesContext.getViewRoot().getViewId());
            if (vdl != null) {
                vdl.buildView(facesContext, facesContext.getViewRoot());
            }

            Application application = facesContext.getApplication();
            boolean viewIdsUnchanged;

            do {
                String beforePublishViewId = facesContext.getViewRoot().getViewId();

                // The before render event on the view root is a special case to keep door open for navigation
                // this must be called *after* PDL.buildView() and before VH.renderView()
                application.publishEvent(facesContext, PreRenderViewEvent.class, facesContext.getViewRoot());

                String afterPublishViewId = facesContext.getViewRoot().getViewId();

                viewIdsUnchanged = beforePublishViewId == null && afterPublishViewId == null
                        || beforePublishViewId != null && afterPublishViewId != null && beforePublishViewId.equals(afterPublishViewId);
                if (facesContext.getResponseComplete()) {
                    return;
                }
            } while (!viewIdsUnchanged);

            // render the view
            vh.renderView(facesContext, facesContext.getViewRoot());

            application.publishEvent(facesContext, PostRenderViewEvent.class, facesContext.getViewRoot());

        } catch (IOException e) {
            throw new FacesException(e.getMessage(), e);
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "+=+=+=+=+=+= View structure printout for " + facesContext.getViewRoot().getViewId());
            DebugUtil.printTree(facesContext.getViewRoot(), LOGGER, Level.FINEST);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Exiting RenderResponsePhase");
        }

    }

    @Override
    public PhaseId getId() {

        return PhaseId.RENDER_RESPONSE;

    }

// The testcase for this class is TestRenderResponsePhase.java

} // end of class RenderResponsePhase
