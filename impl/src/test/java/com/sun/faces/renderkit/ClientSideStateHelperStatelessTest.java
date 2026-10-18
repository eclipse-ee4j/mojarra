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

package com.sun.faces.renderkit;

import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.VIEW_STATE_PARAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;

class ClientSideStateHelperStatelessTest extends JUnitFacesTestCaseBase {

    private static final String ENCODED_STATE = "encoded";
    private static final Object[] DECODED_STATE = { "structure", "state" };
    private static final String VIEW_ID = "/test.xhtml";

    private CountingHelper helper;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, new MockRenderKit());
        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setViewId(VIEW_ID);
        viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(viewRoot);
        helper = new CountingHelper();
    }

    /**
     * Asking whether a postback is stateless and then reading its state must decode the view state parameter only once.
     */
    @Test
    void stateIsDecodedOnceForIsStatelessFollowedByGetState() throws Exception {
        postViewState(ENCODED_STATE);
        ResponseStateManagerImpl manager = responseStateManagerWith(helper);

        assertFalse(manager.isStateless(facesContext, VIEW_ID));
        assertSame(DECODED_STATE, manager.getState(facesContext, VIEW_ID));
        assertEquals(1, helper.decodeCount);
    }

    /**
     * A view state parameter of {@code stateless} marks the postback as stateless without decoding anything.
     */
    @Test
    void statelessViewStateParameterIsStateless() {
        postViewState("stateless");

        assertTrue(helper.isStateless(facesContext, VIEW_ID));
        assertEquals(0, helper.decodeCount);
    }

    /**
     * Any other view state parameter marks the postback as stateful.
     */
    @Test
    void encodedViewStateParameterIsNotStateless() {
        postViewState(ENCODED_STATE);

        assertFalse(helper.isStateless(facesContext, VIEW_ID));
        assertEquals(0, helper.decodeCount);
    }

    /**
     * Whether a request is stateless can only be determined on a postback.
     */
    @Test
    void isStatelessOnNonPostbackThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> helper.isStateless(facesContext, VIEW_ID));
    }

    private static ResponseStateManagerImpl responseStateManagerWith(StateHelper helper) throws ReflectiveOperationException {
        ResponseStateManagerImpl manager = new ResponseStateManagerImpl();
        Field field = ResponseStateManagerImpl.class.getDeclaredField("helper");
        field.setAccessible(true);
        field.set(manager, helper);
        return manager;
    }

    private void postViewState(String value) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(VIEW_STATE_PARAM.getName(facesContext), value);
        externalContext.setRequestParameterMap(parameters);
    }

    private static class CountingHelper extends ClientSideStateHelper {

        private int decodeCount;

        @Override
        protected Object doGetState(FacesContext ctx, String stateString) {
            decodeCount++;
            return DECODED_STATE;
        }
    }
}
