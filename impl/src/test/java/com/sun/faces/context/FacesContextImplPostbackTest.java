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

package com.sun.faces.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;

import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockRenderKit;
import com.sun.faces.mock.MockServletContext;
import org.junit.jupiter.api.AfterEach;
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
        // The factories are process-wide, and setFactory is silently ignored once one has been obtained.
        FactoryFinder.releaseFactories();
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY, "com.sun.faces.mock.MockRenderKitFactory");
        request = new MockHttpServletRequest();
        facesContext = new FacesContextImpl(
                new ExternalContextImpl(new MockServletContext(), request, new MockHttpServletResponse()),
                new LifecycleImpl());

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, new MockRenderKit());

        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(viewRoot);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Constructing a FacesContext makes it the current one for the thread. FacesContextImpl.release() would clear
        // that but needs a CDI environment, so reach the protected setter the way the other tests in this tree do.
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, new Object[] { null });

        FactoryFinder.releaseFactories();
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
