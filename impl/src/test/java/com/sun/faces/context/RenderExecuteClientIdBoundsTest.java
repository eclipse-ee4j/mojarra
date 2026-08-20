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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockRenderKit;
import com.sun.faces.mock.MockServletContext;
import com.sun.faces.renderkit.RenderKitUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.render.RenderKitFactory;

/**
 * The jakarta.faces.partial.render / .execute request parameters are bounded before they reach PartialVisitContext:
 * any whitespace separates ids, empty tokens are dropped, duplicates collapse, ids beyond the length limit are
 * rejected and the id count is capped. These bounds keep a single request from expanding into a disproportionately
 * large PartialVisitContext and stay well clear of legitimate ajax traffic.
 */
class RenderExecuteClientIdBoundsTest {

    private MockHttpServletRequest request;
    private FacesContextImpl facesContext;
    private String renderParam;

    @BeforeEach
    void setUp() {
        FactoryFinder.releaseFactories();
        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY, "com.sun.faces.mock.MockRenderKitFactory");
        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY, "com.sun.faces.mock.MockApplicationFactory");
        request = new MockHttpServletRequest();
        facesContext = new FacesContextImpl(
                new ExternalContextImpl(new MockServletContext(), request, new MockHttpServletResponse()),
                new LifecycleImpl());

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, new MockRenderKit());

        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(viewRoot);

        renderParam = RenderKitUtils.getParameterName(facesContext, PartialViewContext.PARTIAL_RENDER_PARAM_NAME);
    }

    @AfterEach
    void tearDown() throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, new Object[] { null });
        FactoryFinder.releaseFactories();
    }

    private Collection<String> renderIds(String value) {
        request.addParameter(renderParam, value);
        return new PartialViewContextImpl(facesContext).getRenderIds();
    }

    @Test
    void anyWhitespaceSeparatesIdsAndEmptyTokensAreDropped() {
        assertEquals(List.of("a", "b", "c", "d"), List.copyOf(renderIds("a\nb\tc  d")));
    }

    @Test
    void duplicateIdsAreCollapsed() {
        assertEquals(List.of("form:input"), List.copyOf(renderIds("form:input form:input form:input")));
    }

    @Test
    void anOverlongIdIsRejected() {
        assertTrue(renderIds(":".repeat(100_000)).isEmpty());
    }

    @Test
    void anOverlongIdDoesNotEvictValidOnes() {
        assertEquals(List.of("form:input"), List.copyOf(renderIds("form:input " + ":".repeat(100_000))));
    }

    @Test
    void theIdCountIsCapped() {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            value.append("id").append(i).append(' ');
        }
        assertEquals(256, renderIds(value.toString()).size());
    }
}
