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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.junit.LogRecords;
import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

class ClientSideStateHelperEncryptionTest extends JUnitFacesTestCaseBase {

    private static final String WEB_CONFIG_KEY = WebConfiguration.class.getName();
    private static final String PROJECT_STAGE = "jakarta.faces.PROJECT_STAGE";
    private static final String DISABLE_ENCRYPTION = "com.sun.faces.disableClientStateEncryption";
    private static final String COMPRESS = "com.sun.faces.compressViewState";
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
                return ProjectStage.valueOf(servletContext.getInitParameter(PROJECT_STAGE));
            }
        });
        servletContext.removeAttribute(WEB_CONFIG_KEY);
    }

    /**
     * In Development stage, the parameter disables encryption: the written state is plain Base64 of the serialized state, and it is read back unchanged.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void developmentStageWithDisabledEncryptionWritesPlainStateAndReadsItBack(boolean compress) throws Exception {
        String written = write("Development", "true", compress);

        byte[] bytes = Base64.getDecoder().decode(written);
        assertEquals(PLAIN_STATE_MARKER, deserializeStructure(bytes, compress), "state is plain Base64 of the serialized state");
        assertRoundTrip(written);
    }

    /**
     * Outside Development stage, the parameter is ignored: the state stays encrypted and still round-trips.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void productionStageIgnoresDisabledEncryptionAndKeepsStateEncrypted(boolean compress) throws Exception {
        String written = write("Production", "true", compress);

        byte[] bytes = Base64.getDecoder().decode(written);
        assertFalse(isPlainSerializedState(bytes, compress), "state is not plain Base64 of the serialized state");
        assertRoundTrip(written);
    }

    /**
     * Outside Development stage, the ignored parameter is reported once as a WARNING which names the parameter and the stage.
     */
    @Test
    void productionStageWithDisabledEncryptionLogsWarning() throws Exception {
        servletContext.addInitParameter(PROJECT_STAGE, "Production");
        servletContext.addInitParameter(DISABLE_ENCRYPTION, "true");

        List<LogRecord> warnings = LogRecords.capture(FacesLogger.APPLICATION.getLogger(), Level.WARNING, ClientSideStateHelper::new);

        assertEquals(1, warnings.size());
        String message = warnings.get(0).getMessage();
        assertTrue(message.contains("faces.config.webconfig.param.development_only"), message);
        assertTrue(Arrays.asList(warnings.get(0).getParameters()).contains(DISABLE_ENCRYPTION), message);
        assertTrue(Arrays.asList(warnings.get(0).getParameters()).contains(ProjectStage.Production), message);
    }

    /**
     * The parameter is not reported when it is honored in Development stage, nor when it does not disable encryption.
     */
    @ParameterizedTest
    @CsvSource({ "Development, true", "Production, false", "Production," })
    void noWarningWhenParameterIsHonoredOrNotEnabled(String stage, String disableEncryption) throws Exception {
        servletContext.addInitParameter(PROJECT_STAGE, stage);
        if (disableEncryption != null) {
            servletContext.addInitParameter(DISABLE_ENCRYPTION, disableEncryption);
        }

        List<LogRecord> warnings = LogRecords.capture(FacesLogger.APPLICATION.getLogger(), Level.WARNING, ClientSideStateHelper::new);

        assertTrue(warnings.isEmpty(), () -> warnings.get(0).getMessage());
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
    void statelessViewRoundTrips(String disableEncryption) throws Exception {
        servletContext.addInitParameter(PROJECT_STAGE, "Development");
        servletContext.addInitParameter(DISABLE_ENCRYPTION, disableEncryption);
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

    private String write(String stage, String disableEncryption, boolean compress) throws Exception {
        servletContext.addInitParameter(PROJECT_STAGE, stage);
        servletContext.addInitParameter(COMPRESS, String.valueOf(compress));
        if (disableEncryption != null) {
            servletContext.addInitParameter(DISABLE_ENCRYPTION, disableEncryption);
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
        } catch (Exception e) {
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
