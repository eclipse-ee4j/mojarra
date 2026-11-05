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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
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
 * <p>
 * The state is an optional time marker, then a tag, and then the component state map of the view unless that is empty. A state in another format expires the
 * view.
 */
class ClientSideStateHelperTest extends JUnitFacesTestCaseBase {

    private static final String PLAIN_STATE_MARKER = "plain-state-marker";
    private static final String STATE_TIMEOUT_DISABLED = "-1";
    private static final String STATE_TIMEOUT_ENABLED = "60";
    private static final byte EMPTY_STATE = 1;
    private static final byte COMPONENT_STATE = 2;

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

        helper.writeState(facesContext, Map.of("form", "state"), out);
        postViewState(out.toString());

        assertEquals("stateless", out.toString());
        assertEquals("stateless", helper.getState(facesContext, "/view.xhtml"));
    }

    /**
     * A component state is read back as written, with or without the state timeout.
     */
    @ParameterizedTest
    @ValueSource(strings = { STATE_TIMEOUT_DISABLED, STATE_TIMEOUT_ENABLED })
    void componentStateRoundTrips(String stateTimeout) throws Exception {
        configure(stateTimeout, false);

        assertEquals(Map.of("form", "state"), roundTrip(Map.of("form", "state")));
    }

    /**
     * An empty component state is read back as an empty state, and not as a missing state which would expire the view.
     */
    @ParameterizedTest
    @ValueSource(strings = { STATE_TIMEOUT_DISABLED, STATE_TIMEOUT_ENABLED })
    void emptyComponentStateRoundTrips(String stateTimeout) throws Exception {
        configure(stateTimeout, false);

        assertEquals(Map.of(), roundTrip(new HashMap<>()));
    }

    /**
     * A state which holds an unused slot in front of the component state expires the view.
     */
    @ParameterizedTest
    @ValueSource(strings = { STATE_TIMEOUT_DISABLED, STATE_TIMEOUT_ENABLED })
    void stateInPreviousFormatExpiresView(String stateTimeout) throws Exception {
        configure(stateTimeout, true);
        boolean timestamped = !STATE_TIMEOUT_DISABLED.equals(stateTimeout);
        byte[] previous = serialize(oos -> {
            if (timestamped) {
                oos.writeLong(System.currentTimeMillis());
            }
            oos.writeObject(null);
            oos.writeObject(new HashMap<>(Map.of("form", "state")));
        });

        postViewState(Base64.getEncoder().encodeToString(previous));

        assertNull(new ClientSideStateHelper().getState(facesContext, "/view.xhtml"));
    }

    /**
     * An empty component state is written as its tag alone.
     */
    @ParameterizedTest
    @ValueSource(strings = { STATE_TIMEOUT_DISABLED, STATE_TIMEOUT_ENABLED })
    void emptyComponentStateIsWrittenAsTagAlone(String stateTimeout) throws Exception {
        configure(stateTimeout, true);
        StringBuilder out = new StringBuilder();
        new ClientSideStateHelper().writeState(facesContext, new HashMap<>(), out);
        byte[] written = Base64.getDecoder().decode(out.toString());

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(written))) {
            if (!STATE_TIMEOUT_DISABLED.equals(stateTimeout)) {
                ois.readLong();
            }
            assertEquals(EMPTY_STATE, ois.readByte());
            assertEquals(-1, ois.read());
        }
    }

    /**
     * A state without the tag in front of the component state expires the view.
     */
    @ParameterizedTest
    @ValueSource(strings = { STATE_TIMEOUT_DISABLED, STATE_TIMEOUT_ENABLED })
    void untaggedStateExpiresView(String stateTimeout) throws Exception {
        configure(stateTimeout, true);
        boolean timestamped = !STATE_TIMEOUT_DISABLED.equals(stateTimeout);
        byte[] untagged = serialize(oos -> {
            if (timestamped) {
                oos.writeLong(System.currentTimeMillis());
            }
            oos.writeObject(new HashMap<>(Map.of("form", "state")));
        });

        postViewState(Base64.getEncoder().encodeToString(untagged));

        assertNull(new ClientSideStateHelper().getState(facesContext, "/view.xhtml"));
    }

    /**
     * A state written with the state timeout and read without it expires the view, because the first byte of its time marker is no tag.
     */
    @ParameterizedTest
    @ValueSource(bytes = { EMPTY_STATE, COMPONENT_STATE })
    void timestampedStateReadWithoutTimeoutExpiresView(byte tag) throws Exception {
        configure(STATE_TIMEOUT_DISABLED, true);
        byte[] timestamped = serialize(oos -> {
            oos.writeLong(System.currentTimeMillis());
            writeTagged(oos, tag);
        });

        postViewState(Base64.getEncoder().encodeToString(timestamped));

        assertNull(new ClientSideStateHelper().getState(facesContext, "/view.xhtml"));
    }

    /**
     * A state written without the state timeout and read with it expires the view, because it has no time marker.
     */
    @ParameterizedTest
    @ValueSource(bytes = { EMPTY_STATE, COMPONENT_STATE })
    void untimestampedStateReadWithTimeoutExpiresView(byte tag) throws Exception {
        configure(STATE_TIMEOUT_ENABLED, true);
        byte[] untimestamped = serialize(oos -> writeTagged(oos, tag));

        postViewState(Base64.getEncoder().encodeToString(untimestamped));

        assertNull(new ClientSideStateHelper().getState(facesContext, "/view.xhtml"));
    }

    private static void writeTagged(ObjectOutputStream oos, byte tag) throws IOException {
        oos.writeByte(tag);
        if (tag == COMPONENT_STATE) {
            oos.writeObject(new HashMap<>(Map.of("form", "state")));
        }
    }

    /**
     * Configures the state timeout, and a plain state, which is neither encrypted nor compressed, so a test can compare and craft its bytes.
     */
    private void configure(String stateTimeout, boolean plain) {
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, plain ? "Development" : "Production");
        servletContext.addInitParameter(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING.getName(), String.valueOf(plain));
        servletContext.addInitParameter(MojarraContextParam.COMPRESS_VIEW_STATE.getName(), String.valueOf(!plain));
        servletContext.addInitParameter(MojarraContextParam.CLIENT_STATE_TIMEOUT.getName(), stateTimeout);
        facesContext.setViewRoot(new UIViewRoot());
    }

    private Object roundTrip(Map<String, Object> state) throws IOException {
        StringBuilder out = new StringBuilder();
        new ClientSideStateHelper().writeState(facesContext, state, out);
        postViewState(out.toString());
        return new ClientSideStateHelper().getState(facesContext, "/view.xhtml");
    }

    private static byte[] serialize(SerializationStep step) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
            step.writeTo(oos);
        }
        return bytes.toByteArray();
    }

    @FunctionalInterface
    private interface SerializationStep {

        void writeTo(ObjectOutputStream oos) throws IOException;

    }

    private String write(String stage, String clientStateDebugging, boolean compress) throws Exception {
        servletContext.addInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, stage);
        servletContext.addInitParameter(MojarraContextParam.COMPRESS_VIEW_STATE.getName(), String.valueOf(compress));
        if (clientStateDebugging != null) {
            servletContext.addInitParameter(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING.getName(), clientStateDebugging);
        }
        facesContext.setViewRoot(new UIViewRoot());
        StringBuilder out = new StringBuilder();
        new ClientSideStateHelper().writeState(facesContext, Map.of(PLAIN_STATE_MARKER, "state"), out);
        return out.toString();
    }

    private void assertRoundTrip(String written) throws Exception {
        postViewState(written);
        Object state = new ClientSideStateHelper().getState(FacesContext.getCurrentInstance(), "/view.xhtml");

        assertNotNull(state, "state is read back");
        assertEquals(Map.of(PLAIN_STATE_MARKER, "state"), state);
    }

    private void postViewState(String value) {
        externalContext.setRequestParameterMap(Map.of(VIEW_STATE_PARAM.getName(facesContext), value));
    }

    private static boolean isPlainSerializedState(byte[] bytes, boolean compressed) {
        try {
            return deserializeState(bytes, compressed).containsKey(PLAIN_STATE_MARKER);
        }
        catch (Exception e) {
            return false;
        }
    }

    private static Map<?, ?> deserializeState(byte[] bytes, boolean compressed) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(compressed ? new GZIPInputStream(in) : in)) {
            if (ois.readByte() != COMPONENT_STATE) {
                return Map.of();
            }
            return (Map<?, ?>) ois.readObject();
        }
    }

}
