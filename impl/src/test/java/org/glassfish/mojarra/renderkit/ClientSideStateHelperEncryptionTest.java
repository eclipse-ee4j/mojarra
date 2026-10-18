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

package org.glassfish.mojarra.renderkit;

import static org.glassfish.mojarra.renderkit.RenderKitUtils.PredefinedPostbackParameter.VIEW_STATE_PARAM;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.config.MojarraContextParam;
import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.glassfish.mojarra.mock.MockApplication;
import org.glassfish.mojarra.mock.MockExternalContext;
import org.glassfish.mojarra.mock.MockFacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Client side state is written and read back whether or not {@link MojarraContextParam#ENABLE_CLIENT_STATE_DEBUGGING} drops the {@link ByteArrayGuard}. That
 * the parameter is honored only in Development stage is covered by {@code DevelopmentOnlyContextParamTest}.
 */
class ClientSideStateHelperEncryptionTest extends JUnitFacesTestCaseBase {

    private static final String PLAIN_STATE_MARKER = "plain-state-marker";

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
                return ProjectStage.valueOf(servletContext.getInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME));
            }

        });
    }

    /**
     * Default configuration encrypts the state in every stage.
     */
    @ParameterizedTest
    @ValueSource(strings = { "Development", "Production" })
    void defaultConfigurationEncryptsState(String stage) throws Exception {
        String written = write(stage, null, true);

        assertFalse(isPlainSerializedState(Base64.getDecoder().decode(written), true));
        assertRoundTrip(written);
    }

    /**
     * A stateless view is written and read back as the stateless marker, whether or not the state is encrypted.
     */
    @ParameterizedTest
    @ValueSource(strings = { "true", "false" })
    void statelessViewRoundTrips(String clientStateDebugging) throws Exception {
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, "Development");
        servletContext.addInitParameter(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING.getName(), clientStateDebugging);
        ClientSideStateHelper helper = new ClientSideStateHelper();
        UIViewRoot root = new UIViewRoot();
        root.setTransient(true);
        facesContext.setViewRoot(root);
        StringBuilder out = new StringBuilder();

        helper.writeState(facesContext, new Object[] { "structure", "state" }, out);
        postViewState(out.toString());

        assertEquals("stateless", out.toString());
        assertEquals("stateless", helper.getState(facesContext, "/view.xhtml"));
    }

    private String write(String stage, String clientStateDebugging, boolean compress) throws Exception {
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, stage);
        servletContext.addInitParameter(MojarraContextParam.COMPRESS_VIEW_STATE.getName(), String.valueOf(compress));
        if (clientStateDebugging != null) {
            servletContext.addInitParameter(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING.getName(), clientStateDebugging);
        }
        facesContext.setViewRoot(new UIViewRoot());
        StringBuilder out = new StringBuilder();
        new ClientSideStateHelper().writeState(facesContext, new Object[] { PLAIN_STATE_MARKER, "state" }, out);
        return out.toString();
    }

    private void assertRoundTrip(String written) throws Exception {
        postViewState(written);
        Object[] state = (Object[]) new ClientSideStateHelper().getState(FacesContext.getCurrentInstance(), "/view.xhtml");

        assertNotNull(state, "state is read back");
        assertArrayEquals(new Object[] { PLAIN_STATE_MARKER, "state" }, state);
    }

    private void postViewState(String value) {
        externalContext.setRequestParameterMap(Map.of(VIEW_STATE_PARAM.getName(facesContext), value));
    }

    private static boolean isPlainSerializedState(byte[] bytes, boolean compressed) {
        try {
            return PLAIN_STATE_MARKER.equals(deserializeStructure(bytes, compressed));
        }
        catch (Exception e) {
            return false;
        }
    }

    private static Object deserializeStructure(byte[] bytes, boolean compressed) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(compressed ? new GZIPInputStream(in) : in)) {
            return ois.readObject();
        }
    }

}
