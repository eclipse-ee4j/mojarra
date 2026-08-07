/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;

import org.glassfish.mojarra.lifecycle.LifecycleImpl;
import org.glassfish.mojarra.mock.MockCDIProvider;
import org.glassfish.mojarra.mock.MockHttpServletRequest;
import org.glassfish.mojarra.mock.MockHttpServletResponse;
import org.glassfish.mojarra.mock.MockRenderKit;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Whether a request is a postback is asked once per component tag while a view is being built, so the answer is
 * resolved once and reused for the lifetime of the {@link FacesContext}. It therefore has to stay stable even when the
 * request state it was derived from changes afterwards.
 */
class FacesContextImplPostbackTest {

    private MockHttpServletRequest request;
    private FacesContextImpl facesContext;

    @BeforeEach
    void setUp() {
        CDI.setCDIProvider(new MockCDIProvider());
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY, "org.glassfish.mojarra.mock.MockRenderKitFactory");
        request = new MockHttpServletRequest();
        facesContext = new FacesContextImpl(
                new ExternalContextImpl(new MockServletContext(), request, new MockHttpServletResponse()),
                new LifecycleImpl());

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, new MockRenderKit());
        } catch (IllegalArgumentException alreadyRegistered) {
            // The factory outlives a single test and rejects a second registration under the same id.
        }

        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(viewRoot);
    }

    @Test
    void theResolvedAnswerSurvivesLaterChangesToTheRequest() {
        assertFalse(facesContext.isPostback());

        // MockResponseStateManager inherits ResponseStateManager.isPostback, which reports any request carrying
        // parameters as a postback -- so a request that gains one would flip the unresolved answer.
        request.addParameter("jakarta.faces.ViewState", "stateless");

        assertFalse(facesContext.isPostback());
    }

    @Test
    void aRequestCarryingStateIsAPostback() {
        request.addParameter("jakarta.faces.ViewState", "stateless");

        assertTrue(facesContext.isPostback());
    }
}
