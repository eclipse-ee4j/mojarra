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

package com.sun.faces.facelets.tag.faces.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.FacesLogger;

import jakarta.el.MethodExpression;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

/**
 * Container for all Jakarta Faces core and custom component actions used on a page.
 * 
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/view.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class ViewHandler extends TagHandlerImpl {

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    private final static Class<?>[] LISTENER_SIG = new Class<?>[] { PhaseEvent.class };

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
        locale = getAttribute("locale");
        renderKitId = getAttribute("renderKitId");
        contentType = getAttribute("contentType");
        encoding = getAttribute("encoding");
        TagAttribute testForNull = getAttribute("beforePhase");
        beforePhase = null == testForNull ? getAttribute("beforePhaseListener") : testForNull;
        testForNull = getAttribute("afterPhase");
        afterPhase = null == testForNull ? getAttribute("afterPhaseListener") : testForNull;
        contracts = getAttribute("contracts");
        transientFlag = getAttribute("transient");
    }

    /**
     * See taglib documentation.
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
        if (root != null) {
            if (renderKitId != null) {
                String v = renderKitId.getValue(ctx);
                root.setRenderKitId(v);
            }
            if (contentType != null) {
                String v = contentType.getValue(ctx);
                ctx.getFacesContext().getAttributes().put("facelets.ContentType", v);
            }
            if (encoding != null) {
                String v = encoding.getValue(ctx);
                ctx.getFacesContext().getAttributes().put(RIConstants.FACELETS_ENCODING_KEY, v);
                root.getAttributes().put(RIConstants.FACELETS_ENCODING_KEY, v);
            }
            if (beforePhase != null) {
                MethodExpression m = beforePhase.getMethodExpression(ctx, null, LISTENER_SIG);
                root.setBeforePhaseListener(m);
            }
            if (afterPhase != null) {
                MethodExpression m = afterPhase.getMethodExpression(ctx, null, LISTENER_SIG);
                root.setAfterPhaseListener(m);
            }

            if (contracts != null) {
                /*
                 * JAVASERVERFACES-3139: We are relaxing when the contracts attribute can be used. In Development mode we will still
                 * blurb a message that the user is not using it at the top level, which could cause problems.
                 */
                if (ctx.getFacesContext().getAttributes().containsKey("com.sun.faces.uiCompositionCount") && LOGGER.isLoggable(Level.INFO)
                        && ctx.getFacesContext().getApplication().getProjectStage().equals(ProjectStage.Development)) {
                    LOGGER.log(Level.INFO, "f:view contracts attribute found, but not used at top level");
                }
                String contractsValue = contracts.getValue(ctx);
                if (contractsValue != null) {
                    List<String> contractList = Arrays.asList(contractsValue.split(","));
                    ctx.getFacesContext().setResourceLibraryContracts(contractList);
                }
            }

            if (transientFlag != null) {
                Boolean b = Boolean.valueOf(transientFlag.getValue(ctx));
                root.setTransient(b);
            }

            String viewId = root.getViewId();

            // At this point in the lifecycle we should have a non-null/empty
            // view id. The partial state saving check below requires this.
            assert null != viewId;
            assert 0 < viewId.length();

        }

        /*
         * Fixes https://java.net/jira/browse/JAVASERVERFACES-3021.
         *
         * The rational behind moving this here is that we need to make sure we establish the locale in all cases.
         */
        if (locale != null && root != null) {
            try {
                root.setLocale(ComponentSupport.getLocale(ctx, locale));
            } catch (TagAttributeException tae) {
                Object result = locale.getObject(ctx);
                if (null == result) {
                    Locale l = Locale.getDefault();
                    // Special case for bugdb 13582626
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "Using {0} for locale because expression {1} returned null.", new Object[] { l, locale.toString() });
                    }
                    root.setLocale(l);
                }
            }
        }

        nextHandler.apply(ctx, parent);
    }

}
