/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.renderkit.html_basic;

import static jakarta.faces.render.ResponseStateManager.VIEW_STATE_PARAM;
import static java.util.Collections.emptyEnumeration;
import static org.glassfish.mojarra.junit.Serialization.withSerialVersionUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.config.FacesContextParam;
import org.glassfish.mojarra.config.MojarraContextParam;
import org.glassfish.mojarra.renderkit.ServerSideStateHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

public class TestServerSideStateHelper {

    private FacesContext mockedFacesContext;
    private ExternalContext mockedExternalContext;
    private MockedStatic<FacesContext> mockedStaticFacesContext;
    private final Map<String, Object> sessionMap = new HashMap<>();

    @BeforeEach
    public void setup() {
        ServletContext mockedServletContext = mock(ServletContext.class);
        when(mockedServletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
        mockedExternalContext = mock(ExternalContext.class);
        when(mockedExternalContext.getContext()).thenReturn(mockedServletContext);
        mockedFacesContext = mock(FacesContext.class);
        when(mockedFacesContext.getExternalContext()).thenReturn(mockedExternalContext);
        when(mockedFacesContext.getViewRoot()).thenReturn(new UIViewRoot());
        mockedStaticFacesContext = mockStatic(FacesContext.class);
        mockedStaticFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockedFacesContext);
    }

    @AfterEach
    public void teardown() {
        mockedStaticFacesContext.close();
    }

    private void prepareViewStateParam(String viewStateParam) {
        when(mockedExternalContext.getRequestParameterMap()).thenReturn(Map.of(VIEW_STATE_PARAM, viewStateParam));
    }

    @Test
    void testViewStateParam1() {
        prepareViewStateParam("1");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam2() {
        prepareViewStateParam("-1");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam3() {
        prepareViewStateParam("");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam4() {
        prepareViewStateParam("1:");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam5() {
        prepareViewStateParam(":");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam6() {
        prepareViewStateParam(":1");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam7() {
        prepareViewStateParam("1:1");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    @Test
    void testViewStateParam8() {
        prepareViewStateParam("stateless");
        assertDoesNotThrow(() -> new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    /**
     * A component state is read back as written, whether or not the server state is serialized.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void componentStateRoundTrips(boolean serializeServerState) throws Exception {
        prepareSession();
        prepareSerializeServerState(serializeServerState);

        assertEquals(Map.of("form", "state"), roundTrip(new HashMap<>(Map.of("form", "state"))));
    }

    /**
     * An empty component state keeps the session entry of its view as <code>null</code>, so it is read back as an empty state, and not as a missing state which
     * would expire the view, whether or not the server state is serialized.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void emptyComponentStateRoundTrips(boolean serializeServerState) throws Exception {
        prepareSession();
        prepareSerializeServerState(serializeServerState);
        String id = write(new HashMap<>());
        Map<String, Object> viewStates = viewStates(id);

        assertTrue(viewStates.containsKey(viewStateId(id)));
        assertNull(viewStates.get(viewStateId(id)));

        prepareViewStateParam(id);

        assertEquals(Map.of(), new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    /**
     * A view whose session entry was evicted has expired.
     */
    @Test
    void evictedViewHasNoState() throws Exception {
        prepareSession();
        String id = write(new HashMap<>());
        replaceEntry(id, null);

        prepareViewStateParam(id);

        assertNull(new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    /**
     * A session entry which holds an unused slot in front of the component state expires the view.
     */
    @Test
    void stateInPreviousFormatExpiresView() throws Exception {
        prepareSession();
        String id = write(new HashMap<>());
        replaceEntry(id, new Object[] { null, new HashMap<>(Map.of("form", "state")) });

        prepareViewStateParam(id);

        assertNull(new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    /**
     * A serialized session entry which holds a class in another serialized form, such as one saved by another version, expires the view.
     */
    @Test
    void stateInAnotherSerializedFormExpiresView() throws Exception {
        prepareSession();
        prepareSerializeServerState(true);
        ServletContext servletContext = (ServletContext) mockedExternalContext.getContext();
        when(servletContext.getInitParameter(MojarraContextParam.COMPRESS_VIEW_STATE.getName())).thenReturn("false");
        String id = write(new HashMap<>(Map.of("form", new Versioned())));
        byte[] entry = (byte[]) viewStates(id).get(viewStateId(id));
        replaceEntry(id, withSerialVersionUID(entry, Versioned.class.getName(), Versioned.serialVersionUID + 1));

        prepareViewStateParam(id);

        assertNull(new ServerSideStateHelper().getState(mockedFacesContext, null));
    }

    private void prepareSession() {
        Object session = new Object();
        when(mockedExternalContext.getSession(anyBoolean())).thenReturn(session);
        when(mockedExternalContext.getSessionMap()).thenReturn(sessionMap);
        when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
        when(mockedFacesContext.getPartialViewContext()).thenReturn(mock(PartialViewContext.class));
    }

    private Object roundTrip(Map<String, Object> state) throws IOException {
        prepareViewStateParam(write(state));
        return new ServerSideStateHelper().getState(mockedFacesContext, null);
    }

    private String write(Map<String, Object> state) throws IOException {
        StringBuilder id = new StringBuilder();
        new ServerSideStateHelper().writeState(mockedFacesContext, state, id);
        when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
        return id.toString();
    }

    private void prepareSerializeServerState(boolean serializeServerState) {
        ServletContext servletContext = (ServletContext) mockedExternalContext.getContext();
        when(servletContext.getInitParameter(FacesContextParam.SERIALIZE_SERVER_STATE.getName())).thenReturn(String.valueOf(serializeServerState));
    }

    /**
     * Replaces the session entry of the view with the given id, which is the page id and the view state id separated by a colon. A <code>null</code> entry
     * removes it.
     */
    private void replaceEntry(String id, Object entry) {
        if (entry == null) {
            viewStates(id).remove(viewStateId(id));
        }
        else {
            viewStates(id).put(viewStateId(id), entry);
        }
    }

    /**
     * Returns the session entries of the page of the view with the given id.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> viewStates(String id) {
        Map<String, Map<String, Object>> pages = (Map<String, Map<String, Object>>) sessionMap.get(ServerSideStateHelper.STATEFUL_PAGE_MAP);
        return pages.get(id.substring(0, id.indexOf(':')));
    }

    private static String viewStateId(String id) {
        return id.substring(id.indexOf(':') + 1);
    }

    private static final class Versioned implements Serializable {

        private static final long serialVersionUID = 1L;

    }

}
