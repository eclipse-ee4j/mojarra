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

import static com.sun.faces.context.flash.ELFlash.NO_SESSION_OWNER;
import static jakarta.faces.event.PhaseId.RENDER_RESPONSE;
import static jakarta.faces.event.PhaseId.RESTORE_VIEW;
import static java.util.Collections.emptyEnumeration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import jakarta.faces.application.Application;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class FlashOwnerTest {

    private static final String SESSION_ID = "0A1B2C3D4E5F60718293A4B5C6D7E8F9";
    private static final String OTHER_SESSION_ID = "F9E8D7C6B5A4938271605F4E3D2C1B0A";

    private MockedStatic<FacesContext> mockedStaticFacesContext;
    private FacesContext mockedFacesContext;
    private ExternalContext mockedExternalContext;
    private ServletContext mockedServletContext;

    private Map<String, Object> applicationMap;
    private Map<String, Object> cookieMap;

    @BeforeEach
    public void setup() {
        mockedStaticFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext = mock(FacesContext.class);
        mockedExternalContext = mock(ExternalContext.class);
        mockedServletContext = mock(ServletContext.class);
        applicationMap = new HashMap<>();
        cookieMap = new HashMap<>();

        mockedStaticFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockedFacesContext);
        when(mockedFacesContext.getExternalContext()).thenReturn(mockedExternalContext);
        when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
        when(mockedFacesContext.getApplication()).thenReturn(mock(Application.class));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);
        when(mockedExternalContext.getContext()).thenReturn(mockedServletContext);
        when(mockedExternalContext.getApplicationMap()).thenReturn(applicationMap);
        when(mockedExternalContext.getRequestCookieMap()).thenReturn(cookieMap);
        when(mockedExternalContext.getRequestContextPath()).thenReturn("");
        when(mockedServletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
        when(mockedServletContext.getInitParameter(any())).thenReturn(null);

        doAnswer(invocation -> cookieMap.put(invocation.getArgument(0), new Cookie(invocation.getArgument(0), invocation.getArgument(1))))
                .when(mockedExternalContext).addResponseCookie(any(), any(), any());
    }

    @AfterEach
    public void teardown() {
        mockedStaticFacesContext.close();
    }

    /**
     * Flashes are filed under an owner derived from the session id, so a sequence number issued to one session can
     * never reach the flash of another one. Two nodes hand out sequence numbers from counters of their own, which is
     * what puts the same number in two browsers' cookies in the first place.
     */
    @Test
    public void testOneSessionsSequenceNumberCannotReachAnothersFlash() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);

        Map<String, Map<String, Object>> ownFlashes = flashesOf(flash, SESSION_ID);
        Map<String, Map<String, Object>> otherFlashes = flashesOf(flash, OTHER_SESSION_ID);

        assertNotSame(ownFlashes, otherFlashes);
        assertEquals(2, flash.getFlashInnerMap().size());

        String sharedSequenceNumber = "1";
        otherFlashes.put(sharedSequenceNumber, Map.of("secret", "the other session's value"));

        assertNull(ownFlashes.get(sharedSequenceNumber));
    }

    /**
     * The owner is a token derived from the session id, so a flash cookie for a session whose flashes live on
     * another node carries nothing which identifies that session.
     */
    @Test
    public void testOwnerDoesNotDiscloseTheSessionId() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        flashesOf(flash, SESSION_ID);

        String owner = flash.getFlashInnerMap().keySet().iterator().next();

        assertNotEquals(SESSION_ID, owner);
        assertTrue(SESSION_ID.length() > owner.length());
        assertTrue(owner.matches("[0-9a-f]+"));
    }

    /**
     * A session going away takes its flashes with it, so nothing else has to decide whether they have aged out.
     */
    @Test
    public void testDestroyedSessionDropsItsFlashes() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        flashesOf(flash, SESSION_ID);
        Map<String, Map<String, Object>> survivingFlashes = flashesOf(flash, OTHER_SESSION_ID);
        assertEquals(2, flash.getFlashInnerMap().size());

        when(mockedServletContext.getAttribute(ELFlash.FLASH_ATTRIBUTE_NAME)).thenReturn(flash);
        HttpSession destroyedSession = mock(HttpSession.class);
        when(destroyedSession.getServletContext()).thenReturn(mockedServletContext);
        when(destroyedSession.getId()).thenReturn(SESSION_ID);

        ELFlash.sessionDestroyed(new HttpSessionEvent(destroyedSession));

        assertEquals(1, flash.getFlashInnerMap().size());
        assertSame(survivingFlashes, flash.getFlashInnerMap().get(ownerOf(flash, OTHER_SESSION_ID)));
    }

    /**
     * Containers rotate the session id on authentication, and a login is exactly where a flash tends to be written
     * before the redirect and read after it, so the flashes of a session follow it to its new id.
     */
    @Test
    public void testRotatedSessionIdKeepsItsFlashes() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);
        Map<String, Map<String, Object>> flashesBeforeLogin = flashesOf(flash, SESSION_ID);
        flashesBeforeLogin.put("1", Map.of("message", "Welcome back"));

        when(mockedServletContext.getAttribute(ELFlash.FLASH_ATTRIBUTE_NAME)).thenReturn(flash);
        HttpSession rotatedSession = mock(HttpSession.class);
        when(rotatedSession.getServletContext()).thenReturn(mockedServletContext);
        when(rotatedSession.getId()).thenReturn(OTHER_SESSION_ID);

        ELFlash.sessionIdChanged(new HttpSessionEvent(rotatedSession), SESSION_ID);

        assertEquals(1, flash.getFlashInnerMap().size());
        assertSame(flashesBeforeLogin, flashesOf(flash, OTHER_SESSION_ID));
    }

    /**
     * A request without a session has no owner to be filed under and no session whose end would clean it up, so its
     * flashes share one owner of their own.
     */
    @Test
    public void testSessionlessRequestsShareOneOwner() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);

        Map<String, Map<String, Object>> firstFlashes = flashesOf(flash, null);
        Map<String, Map<String, Object>> secondFlashes = flashesOf(flash, null);

        assertSame(firstFlashes, secondFlashes);
        assertEquals(1, flash.getFlashInnerMap().size());
        assertSame(firstFlashes, flash.getFlashInnerMap().get(NO_SESSION_OWNER));
    }

    /**
     * A view writing a flash on a first visit does so before anything has created a session, so the request which reads
     * it back is the first one with an owner to file the flash under, and it has to find the flash its cookie names.
     * The flash ends up under that owner, which is what makes it claimable once.
     */
    @Test
    public void testFlashWrittenWithoutASessionIsReadableOnceOneExists() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);

        beginRequest(null, RENDER_RESPONSE);
        flash.put("foo", "bar");
        flash.doLastPhaseActions(mockedFacesContext, false);

        beginRequest(SESSION_ID, RESTORE_VIEW);
        flash.doPrePhaseActions(mockedFacesContext);

        assertEquals("bar", flash.get("foo"));
        assertFalse(flash.getFlashInnerMap().get(NO_SESSION_OWNER).values().stream()
                .anyMatch(flashMap -> flashMap.containsKey("foo")));
    }

    /**
     * The sequence number in a flash cookie is what identifies a flash, so a session presenting another session's
     * cookie must be handed a flash of its own rather than the one that cookie names.
     */
    @Test
    public void testFlashWrittenWithASessionCannotBeClaimedByAnother() {
        ELFlash flash = ELFlash.getFlash(mockedExternalContext, true);

        beginRequest(SESSION_ID, RENDER_RESPONSE);
        flash.put("foo", "bar");
        flash.doLastPhaseActions(mockedFacesContext, false);

        beginRequest(OTHER_SESSION_ID, RESTORE_VIEW);
        flash.doPrePhaseActions(mockedFacesContext);

        assertNull(flash.get("foo"));
    }

    private void beginRequest(String sessionId, PhaseId phaseId) {
        when(mockedExternalContext.getSessionId(false)).thenReturn(sessionId);
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(phaseId);
        when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
    }

    private Map<String, Map<String, Object>> flashesOf(ELFlash flash, String sessionId) {
        when(mockedExternalContext.getSessionId(false)).thenReturn(sessionId);
        when(mockedFacesContext.getAttributes()).thenReturn(new HashMap<>());
        flash.keySet();
        return flash.getFlashInnerMap().get(ownerOf(flash, sessionId));
    }

    private String ownerOf(ELFlash flash, String sessionId) {
        if (sessionId == null) {
            return NO_SESSION_OWNER;
        }

        return flash.getFlashInnerMap().keySet().stream()
                .filter(owner -> !NO_SESSION_OWNER.equals(owner))
                .filter(owner -> owner.equals(ELFlash.toOwnerToken(sessionId)))
                .findFirst()
                .orElseThrow();
    }

}
