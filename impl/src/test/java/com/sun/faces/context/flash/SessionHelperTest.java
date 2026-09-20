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

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;
import static com.sun.faces.context.flash.ELFlash.CONSTANTS.KeepAllMessagesAttributeName;
import static com.sun.faces.context.flash.SessionHelper.FLASH_SESSIONACTIVATIONLISTENER_ATTRIBUTE_NAME;
import static jakarta.faces.event.PhaseId.RESTORE_VIEW;
import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.faces.application.Application;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class SessionHelperTest {

    private static final String FLASH_INNER_MAP_KEY = ELFlash.FLASH_ATTRIBUTE_NAME + "FIM";

    private static final String SESSION_ID = "0A1B2C3D4E5F60718293A4B5C6D7E8F9";
    private static final String OTHER_SESSION_ID = "F9E8D7C6B5A4938271605F4E3D2C1B0A";
    private static final String REPLICATED_SEQUENCE_NUMBER = "7";
    private static final String IN_FLIGHT_SEQUENCE_NUMBER = "8";

    private static final int CONCURRENT_REQUESTS = 16;
    private static final int CONTENTION_ROUNDS = 5;
    private static final long RUN_TIMEOUT_SECONDS = 10;

    private MockedStatic<FacesContext> mockedStaticFacesContext;
    private FacesContext mockedFacesContext;
    private ExternalContext mockedExternalContext;
    private ServletContext mockedServletContext;

    private Map<Object, Object> contextMap;
    private Map<String, Object> applicationMap;
    private Map<String, Object> sessionMap;
    private Map<String, Object> cookieMap;

    @BeforeEach
    public void setup() {
        mockedStaticFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext = mock(FacesContext.class);
        mockedExternalContext = mock(ExternalContext.class);
        mockedServletContext = mock(ServletContext.class);
        contextMap = new HashMap<>();
        applicationMap = new HashMap<>();
        sessionMap = new ConcurrentHashMap<>();
        cookieMap = new HashMap<>();

        mockedStaticFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockedFacesContext);
        when(mockedFacesContext.getExternalContext()).thenReturn(mockedExternalContext);
        when(mockedFacesContext.getAttributes()).thenReturn(contextMap);
        when(mockedFacesContext.getApplication()).thenReturn(mock(Application.class));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);
        when(mockedExternalContext.getContext()).thenReturn(mockedServletContext);
        when(mockedExternalContext.getApplicationMap()).thenReturn(applicationMap);
        when(mockedExternalContext.getSessionMap()).thenReturn(sessionMap);
        when(mockedExternalContext.getSession(false)).thenReturn(mock(HttpSession.class));
        when(mockedExternalContext.getSessionId(false)).thenReturn(SESSION_ID);
        when(mockedExternalContext.getRequestCookieMap()).thenReturn(cookieMap);
        when(mockedExternalContext.getRequestContextPath()).thenReturn("");
        when(mockedServletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
        when(mockedServletContext.getInitParameter(any())).thenReturn(null);

        applicationMap.put(EnableDistributable.getQualifiedName(), Boolean.TRUE);
    }

    @AfterEach
    public void teardown() {
        mockedStaticFacesContext.close();
    }

    /**
     * An activated session takes back the flashes it carries.
     */
    @Test
    public void testActivatedSessionAdoptsTheFlashesItCarries() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        Map<String, Object> replicatedFlash = new HashMap<>();
        sessionMap.put(FLASH_INNER_MAP_KEY, new HashMap<>(Map.of(REPLICATED_SEQUENCE_NUMBER, replicatedFlash)));
        getSessionHelper().sessionDidActivate(null);

        ELFlash.getFlash(mockedExternalContext, true);

        assertSame(replicatedFlash, flash.getOwnerFlashInnerMap(mockedExternalContext).get(REPLICATED_SEQUENCE_NUMBER));
    }

    /**
     * A session can already have a request writing flashes on this node when the one carrying its replicated flashes
     * lands, so taking those back adds to what the owner has here.
     */
    @Test
    public void testAdoptingReplicatedFlashesKeepsWhatTheOwnerAlreadyHas() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        Map<String, Object> inFlightFlash = new HashMap<>();
        flash.getOwnerFlashInnerMap(mockedExternalContext).put(IN_FLIGHT_SEQUENCE_NUMBER, inFlightFlash);
        Map<String, Object> replicatedFlash = new HashMap<>();
        sessionMap.put(FLASH_INNER_MAP_KEY, new HashMap<>(Map.of(REPLICATED_SEQUENCE_NUMBER, replicatedFlash)));
        getSessionHelper().sessionDidActivate(null);

        ELFlash.getFlash(mockedExternalContext, true);

        Map<String, Map<String, Object>> flashes = flash.getOwnerFlashInnerMap(mockedExternalContext);
        assertSame(inFlightFlash, flashes.get(IN_FLIGHT_SEQUENCE_NUMBER));
        assertSame(replicatedFlash, flashes.get(REPLICATED_SEQUENCE_NUMBER));
    }

    /**
     * A session carries only the flashes filed under its own owner, so activating it leaves the flashes of every
     * other session on the node exactly where they are.
     */
    @Test
    public void testActivatedSessionLeavesTheFlashesOfOtherSessionsAlone() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        when(mockedExternalContext.getSessionId(false)).thenReturn(OTHER_SESSION_ID);
        Map<String, Map<String, Object>> otherSessionFlashes = flash.getOwnerFlashInnerMap(mockedExternalContext);
        otherSessionFlashes.put(REPLICATED_SEQUENCE_NUMBER, new HashMap<>());
        when(mockedExternalContext.getSessionId(false)).thenReturn(SESSION_ID);

        sessionMap.put(FLASH_INNER_MAP_KEY, new HashMap<>(Map.of(REPLICATED_SEQUENCE_NUMBER, new HashMap<>())));
        getSessionHelper().sessionDidActivate(null);
        ELFlash.getFlash(mockedExternalContext, true);

        when(mockedExternalContext.getSessionId(false)).thenReturn(OTHER_SESSION_ID);
        assertSame(otherSessionFlashes, flash.getOwnerFlashInnerMap(mockedExternalContext));
    }

    /**
     * The flash inner map is application scoped, so an activated session which does not carry one leaves the map
     * already in place serving every session on the node, and the flash stays usable afterwards.
     *
     * https://github.com/eclipse-ee4j/mojarra/issues/6024
     */
    @Test
    public void testActivatedSessionWithoutFlashInnerMapKeepsTheExistingOne() {
        ELFlash flash = activateSessionWithoutReplicatedFlashInnerMap();
        Map<String, Map<String, Object>> flashes = flash.getOwnerFlashInnerMap(mockedExternalContext);

        ELFlash.getFlash(mockedExternalContext, true);

        assertSame(flashes, flash.getOwnerFlashInnerMap(mockedExternalContext));
        assertSame(flashes, sessionMap.get(FLASH_INNER_MAP_KEY));
        flash.setKeepMessages(true);
        assertEquals(true, flash.get(KeepAllMessagesAttributeName.toString()));
    }

    /**
     * Both activation listener callbacks mark the helper the same way, so a passivated helper whose session
     * attributes have since been removed republishes them, exactly as an activated one does.
     *
     * https://github.com/eclipse-ee4j/mojarra/issues/6024
     */
    @Test
    public void testPassivatedHelperWhoseSessionAttributesWereRemovedRepublishesThem() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        Map<String, Map<String, Object>> flashes = flash.getOwnerFlashInnerMap(mockedExternalContext);
        SessionHelper sessionHelper = getSessionHelper();
        sessionHelper.remove(mockedExternalContext);
        sessionHelper.sessionWillPassivate(null);

        sessionHelper.update(mockedExternalContext, flash);

        assertSame(flashes, flash.getOwnerFlashInnerMap(mockedExternalContext));
        assertSame(sessionHelper, sessionMap.get(FLASH_SESSIONACTIVATIONLISTENER_ATTRIBUTE_NAME));
        assertSame(flashes, sessionMap.get(FLASH_INNER_MAP_KEY));
    }

    /**
     * The activation is consumed by the first update, so a later one leaves the session map alone.
     */
    @Test
    public void testActivationIsConsumedByTheFirstUpdate() {
        ELFlash flash = activateSessionWithoutReplicatedFlashInnerMap();
        Map<String, Map<String, Object>> flashes = flash.getOwnerFlashInnerMap(mockedExternalContext);

        ELFlash.getFlash(mockedExternalContext, true);
        sessionMap.put(FLASH_INNER_MAP_KEY, new HashMap<>(Map.of(REPLICATED_SEQUENCE_NUMBER, new HashMap<>())));
        ELFlash.getFlash(mockedExternalContext, true);

        assertSame(flashes, flash.getOwnerFlashInnerMap(mockedExternalContext));
        assertNull(flashes.get(REPLICATED_SEQUENCE_NUMBER));
    }

    /**
     * One activation is one adoption however many requests the session has in flight when it lands, so the
     * activation flag has to be read and cleared as one step.
     */
    @Test
    public void testConcurrentUpdatesAdoptTheReplicatedFlashInnerMapAtMostOnce() throws Exception {
        ELFlash.getFlash(mockedExternalContext, true);
        SessionHelper sessionHelper = getSessionHelper();
        AtomicInteger adoptions = new AtomicInteger();
        ELFlash flash = new ELFlash(mockedExternalContext) {
            @Override
            void restoreOwnerFlashInnerMap(ExternalContext extContext, Map<String, Map<String, Object>> ownerFlashInnerMap) {
                adoptions.incrementAndGet();
                super.restoreOwnerFlashInnerMap(extContext, ownerFlashInnerMap);
            }
        };

        for (int round = 0; round < CONTENTION_ROUNDS; round++) {
            adoptions.set(0);
            Map<String, Map<String, Object>> replicatedFlashes = new HashMap<>();
            replicatedFlashes.put(REPLICATED_SEQUENCE_NUMBER, new HashMap<>());
            sessionMap.put(FLASH_INNER_MAP_KEY, replicatedFlashes);
            sessionHelper.sessionDidActivate(null);

            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(CONCURRENT_REQUESTS);

            for (int request = 0; request < CONCURRENT_REQUESTS; request++) {
                new Thread(() -> {
                    try {
                        start.await();
                        sessionHelper.update(mockedExternalContext, flash);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            assertTrue(done.await(RUN_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(1, adoptions.get());
            assertNotNull(flash.getOwnerFlashInnerMap(mockedExternalContext).get(REPLICATED_SEQUENCE_NUMBER));
        }
    }

    /**
     * Asking for the flash without creating it must not go looking for a session helper to update, there
     * being no flash to update it with.
     */
    @Test
    public void testFlashWhichIsNotCreatedIsNotReplicated() {
        assertNull(ELFlash.getFlash(mockedExternalContext, false));
        assertTrue(sessionMap.isEmpty());
    }

    private ELFlash activateSessionWithoutReplicatedFlashInnerMap() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        assertNotNull(flash.getOwnerFlashInnerMap(mockedExternalContext));
        getSessionHelper().sessionDidActivate(null);
        sessionMap.remove(FLASH_INNER_MAP_KEY);
        return flash;
    }

    private SessionHelper getSessionHelper() {
        SessionHelper sessionHelper = SessionHelper.getInstance(mockedExternalContext);
        assertNotNull(sessionHelper);
        return sessionHelper;
    }

}
