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

package com.sun.faces.facelets.tag.jsf.core;

import com.sun.faces.RIConstants;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

import javax.el.MethodExpression;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container for all JavaServer Faces core and custom component actions used on
 * a page. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/view.html">tag
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class ViewHandler extends TagHandlerImpl {
    
    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    private final static Class[] LISTENER_SIG = new Class[] { PhaseEvent.class };

    private final TagAttribute locale;

    private final TagAttribute renderKitId;
    
    private final TagAttribute contentType;
    
    private final TagAttribute encoding;

    private final TagAttribute beforePhase;

    private final TagAttribute afterPhase;
    
    private final TagAttribute transientFlag;
    
    /**
     * Stores the contracts tag attribute.
     */
    private final TagAttribute contracts;

    /**
     * @param config
     */
    public ViewHandler(TagConfig config) {
        super(config);
        this.locale = this.getAttribute("locale");
        this.renderKitId = this.getAttribute("renderKitId");
        this.contentType = this.getAttribute("contentType");
        this.encoding = this.getAttribute("encoding");
        TagAttribute testForNull = this.getAttribute("beforePhase");
        this.beforePhase = (null == testForNull) ? 
                         this.getAttribute("beforePhaseListener") : testForNull;
        testForNull = this.getAttribute("afterPhase");
        this.afterPhase = (null == testForNull) ?
                         this.getAttribute("afterPhaseListener") : testForNull;
        this.contracts = this.getAttribute("contracts");
        this.transientFlag = this.getAttribute("transient");
    }

    /**
     * See taglib documentation.
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        if (root != null) {
            if (this.renderKitId != null) {
                String v = this.renderKitId.getValue(ctx);
                root.setRenderKitId(v);
            }
            if (this.contentType != null) {
                String v = this.contentType.getValue(ctx);
                ctx.getFacesContext().getAttributes().put("facelets.ContentType", v);
            }
            if (this.encoding != null) {
                String v = this.encoding.getValue(ctx);
                ctx.getFacesContext().getAttributes().put(RIConstants.FACELETS_ENCODING_KEY, v);
                root.getAttributes().put(RIConstants.FACELETS_ENCODING_KEY, v);
            }
            if (this.beforePhase != null) {
                MethodExpression m = this.beforePhase
                        .getMethodExpression(ctx, null, LISTENER_SIG);
                root.setBeforePhaseListener(m);
            }
            if (this.afterPhase != null) {
                MethodExpression m = this.afterPhase
                        .getMethodExpression(ctx, null, LISTENER_SIG);
                root.setAfterPhaseListener(m);
            }

            if (this.contracts != null) {
                /*
                 * JAVASERVERFACES-3139: We are relaxing when the contracts
                 * attribute can be used. In Development mode we will still 
                 * blurb a message that the user is not using it at the top
                 * level, which could cause problems.
                 */
                if (ctx.getFacesContext().getAttributes().containsKey("com.sun.faces.uiCompositionCount") &&
                        LOGGER.isLoggable(Level.INFO) && 
                        ctx.getFacesContext().getApplication().getProjectStage().equals(ProjectStage.Development)) {
                        LOGGER.log(Level.INFO, "f:view contracts attribute found, but not used at top level");
                }
                String contractsValue = this.contracts.getValue(ctx);
                if (contractsValue != null) {
                    List<String> contractList = Arrays.asList(contractsValue.split(","));
                    ctx.getFacesContext().setResourceLibraryContracts(contractList);
                }
            }
            
            if (this.transientFlag != null) {
                Boolean b = Boolean.valueOf(this.transientFlag.getValue(ctx));
                root.setTransient(b);
            }

            String viewId = root.getViewId();

            // At this point in the lifecycle we should have a non-null/empty
            // view id.  The partial state saving check below requires this.
            assert(null != viewId);
            assert(0 < viewId.length());

        }

        /*
         * Fixes https://java.net/jira/browse/JAVASERVERFACES-3021.
         * 
         * The rational behind moving this here is that we need to make sure
         * we establish the locale in all cases.
         */
        if (this.locale != null && root != null) {
            try {
                root.setLocale(ComponentSupport.getLocale(ctx, this.locale));
            } catch (TagAttributeException tae) {
                Object result = this.locale.getObject(ctx);
                if (null == result) {
                    Locale l = Locale.getDefault();
                    // Special case for bugdb 13582626
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, 
                                "Using {0} for locale because expression {1} returned null.", 
                                new Object[]{l, this.locale.toString()});
                    }
                    root.setLocale(l);
                }
            }
        }        

        this.nextHandler.apply(ctx, parent);
    }

}
