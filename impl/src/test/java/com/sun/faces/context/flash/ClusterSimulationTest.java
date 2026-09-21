/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.context.flash;

import static jakarta.faces.event.PhaseId.RENDER_RESPONSE;
import static jakarta.faces.event.PhaseId.RESTORE_VIEW;
import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.faces.application.Application;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import javax.naming.InitialContext;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * <p>
 * Two application nodes serving one replicated session, as a cluster does.
 * </p>
 */
public class ClusterSimulationTest {

    private static final String ENABLE_DISTRIBUTABLE = "com.sun.faces.enableDistributable";

    private static final String FLASH_SECRET_KEY_NAME = "java:comp/env/faces/FlashSecretKey";

    /**
     * The key every node of the cluster is configured with, without which a node cannot read a flash cookie another
     * one wrote.
     */
    private static final String FLASH_SECRET_KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    private static final String MESSAGE_KEY = "message";

    private static final String MESSAGE = "Saved successfully";

    /**
     * <p>
     * One node of the cluster, with an application map of its own, and so a flash of its own.
     * </p>
     */
    private static class Node {

        private final Map<String, Object> applicationMap = new HashMap<>();

        private Node() {
            applicationMap.put(ENABLE_DISTRIBUTABLE, Boolean.TRUE);
        }
    }

    /**
     * <p>
     * One browser, with the attributes of its session on whichever node currently holds it, and its cookies.
     * </p>
     */
    private static class Session {

        private final String id;
        private final Map<String, Object> cookies = new HashMap<>();
        private Map<String, Object> attributes = new HashMap<>();

        private Session(String id) {
            this.id = id;
        }
    }

    /**
     * <p>
     * A value an application can put in the flash which does not reach another node, a JPA entity and a CDI proxy
     * being the everyday examples.
     * </p>
     */
    private static class Unserializable {
    }

    /**
     * A session reaching a node without the flashes it was carrying leaves that node serving every session which
     * arrives after it.
     *
     * https://github.com/eclipse-ee4j/mojarra/issues/6024
     */
    @Test
    public void testSessionWhoseFlashesDoNotReachTheNodeLeavesItServingTheNextOne() {
        Node nodeA = new Node();
        Node nodeB = new Node();
        Session failingOver = new Session("failing-over-session-id");

        request(nodeA, failingOver, "entity", new Unserializable(), null, true);
        replicate(failingOver);
        assertNull(request(nodeB, failingOver, null, null, "entity", false));

        assertDoesNotThrow(() -> request(nodeB, new Session("arriving-session-id"), MESSAGE_KEY, MESSAGE, null, false));
    }

    /**
     * The flashes of a session are its own, so a session reaching a node without the ones it was carrying leaves the
     * flashes of the sessions already on that node readable.
     *
     * https://github.com/eclipse-ee4j/mojarra/issues/6024
     */
    @Test
    public void testSessionWhoseFlashesDoNotReachTheNodeLeavesTheFlashesOfOtherSessionsAlone() {
        Node nodeA = new Node();
        Node nodeB = new Node();
        Session resident = new Session("resident-session-id");
        Session failingOver = new Session("failing-over-session-id");

        request(nodeB, resident, MESSAGE_KEY, MESSAGE, null, true);
        request(nodeA, failingOver, "entity", new Unserializable(), null, true);
        replicate(failingOver);
        request(nodeB, failingOver, null, null, "entity", false);

        assertEquals(MESSAGE, request(nodeB, resident, null, null, MESSAGE_KEY, false));
    }

    /**
     * A flash written before a redirect is there to be read after it, on whichever node serves the request which
     * follows.
     */
    @Test
    public void testFlashSurvivesAPostRedirectGetAcrossNodes() {
        Node nodeA = new Node();
        Node nodeB = new Node();
        Session user = new Session("user-session-id");

        request(nodeA, user, MESSAGE_KEY, MESSAGE, null, true);
        replicate(user);

        assertEquals(MESSAGE, request(nodeB, user, null, null, MESSAGE_KEY, false));
    }

    /**
     * A flash written before a redirect is there to be read after it on the node which wrote it, which is what the
     * request served by another node has to match.
     */
    @Test
    public void testFlashSurvivesAPostRedirectGetOnOneNode() {
        Node node = new Node();
        Session user = new Session("user-session-id");

        request(node, user, MESSAGE_KEY, MESSAGE, null, true);

        assertEquals(MESSAGE, request(node, user, null, null, MESSAGE_KEY, false));
    }

    /**
     * <p>
     * Runs one request of a browser against a node, and answers what the flash read back.
     * </p>
     */
    private static Object request(Node node, Session session, String writeKey, Object writeValue, String readKey, boolean redirect) {
        try (MockedConstruction<InitialContext> mockedInitialContext = mockConstruction(InitialContext.class,
                (mockedContext, context) -> when(mockedContext.lookup(FLASH_SECRET_KEY_NAME)).thenReturn(FLASH_SECRET_KEY));
                MockedStatic<FacesContext> mockedStaticFacesContext = mockStatic(FacesContext.class)) {

            FacesContext context = mock(FacesContext.class);
            ExternalContext extContext = mock(ExternalContext.class);
            ServletContext servletContext = mock(ServletContext.class);

            mockedStaticFacesContext.when(FacesContext::getCurrentInstance).thenReturn(context);
            when(context.getExternalContext()).thenReturn(extContext);
            when(context.getAttributes()).thenReturn(new HashMap<>());
            when(context.getApplication()).thenReturn(mock(Application.class));
            when(context.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);
            when(extContext.getContext()).thenReturn(servletContext);
            when(extContext.getApplicationMap()).thenReturn(node.applicationMap);
            when(extContext.getSessionMap()).thenReturn(session.attributes);
            when(extContext.getSession(false)).thenReturn(mock(HttpSession.class));
            when(extContext.getSessionId(false)).thenReturn(session.id);
            when(extContext.getRequestCookieMap()).thenReturn(session.cookies);
            when(extContext.getRequestContextPath()).thenReturn("");
            when(servletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
            when(servletContext.getInitParameter(any())).thenReturn(null);
            when(servletContext.getInitParameter(ENABLE_DISTRIBUTABLE)).thenReturn(Boolean.TRUE.toString());
            doAnswer(invocation -> session.cookies.put(invocation.getArgument(0),
                    new Cookie(invocation.getArgument(0), invocation.getArgument(1))))
                    .when(extContext).addResponseCookie(any(), any(), any());

            ELFlash flash = ELFlash.getFlash(extContext, true);
            flash.doPrePhaseActions(context);
            Object read = readKey != null ? flash.get(readKey) : null;

            if (writeKey != null) {
                flash.put(writeKey, writeValue);
            }
            when(context.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);
            flash.doLastPhaseActions(context, redirect);
            return read;
        }
    }

    /**
     * <p>
     * Replicates a session to another node, keeping the attributes which serialize and dropping the ones which do
     * not.
     * </p>
     */
    private static void replicate(Session session) {
        forEachSessionHelper(session, sessionHelper -> sessionHelper.sessionWillPassivate(null));
        Map<String, Object> replicated = new LinkedHashMap<>();

        session.attributes.forEach((name, value) -> {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
                    output.writeObject(value);
                }
                try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                    replicated.put(name, input.readObject());
                }
            } catch (IOException | ClassNotFoundException e) {
                return;
            }
        });

        session.attributes = replicated;
        forEachSessionHelper(session, sessionHelper -> sessionHelper.sessionDidActivate(null));
    }

    private static void forEachSessionHelper(Session session, Consumer<SessionHelper> action) {
        session.attributes.values().stream()
                .filter(SessionHelper.class::isInstance)
                .map(SessionHelper.class::cast)
                .forEach(action);
    }

}
