/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;

/**
 * A view state is valid only for the view it was saved for: restoring it into another view would apply the state of one
 * component tree to another one, and replay the dynamically added components of the former into the latter.
 */
class StateHelperViewIdTest extends JUnitFacesTestCaseBase {

    private static final String VIEW_ID = "/view.xhtml";
    private static final String OTHER_VIEW_ID = "/other.xhtml";
    private static final Object[] STATE = { "structure", "state" };

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockExternalContext sessionAwareContext = new MockExternalContext(servletContext, request, response) {
            @Override
            public Object getSession(boolean create) {
                return session;
            }
        };
        externalContext = sessionAwareContext;
        facesContext = new MockFacesContext(sessionAwareContext, lifecycle);
        facesContext.setApplication(new MockApplication() {
            @Override
            public ProjectStage getProjectStage() {
                return ProjectStage.Development;
            }
        });
        servletContext.addInitParameter("com.sun.faces.disableClientStateEncryption", "true");
        servletContext.removeAttribute(WebConfiguration.class.getName());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void stateIsRestoredForTheViewItWasSavedFor(boolean client) throws Exception {
        StateHelper helper = helper(client);
        postViewState(write(helper));

        assertArrayEquals(STATE, (Object[]) helper.getState(facesContext, VIEW_ID));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void stateIsNotRestoredForAnotherView(boolean client) throws Exception {
        StateHelper helper = helper(client);
        postViewState(write(helper));

        assertNull(helper.getState(facesContext, OTHER_VIEW_ID));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void clientStateWithoutViewIdHashIsNotRestored(boolean compress) throws Exception {
        servletContext.addInitParameter("com.sun.faces.compressViewState", String.valueOf(compress));
        servletContext.removeAttribute(WebConfiguration.class.getName());
        facesContext.setViewRoot(new UIViewRoot());
        postViewState(serializedWithoutViewIdHash(compress));

        assertNull(new ClientSideStateHelper().getState(facesContext, VIEW_ID));
    }

    private static StateHelper helper(boolean client) {
        return client ? new ClientSideStateHelper() : new ServerSideStateHelper();
    }

    private String write(StateHelper helper) throws Exception {
        UIViewRoot root = new UIViewRoot();
        root.setViewId(VIEW_ID);
        facesContext.setViewRoot(root);
        StringBuilder out = new StringBuilder();
        helper.writeState(facesContext, STATE, out);
        return out.toString();
    }

    private void postViewState(String value) {
        externalContext.setRequestParameterMap(Map.of(VIEW_STATE_PARAM.getName(facesContext), value));
    }

    private static String serializedWithoutViewIdHash(boolean compress) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(compress ? new GZIPOutputStream(bytes) : bytes)) {
            out.writeObject(STATE[0]);
            out.writeObject(STATE[1]);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

}
