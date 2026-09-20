package com.sun.faces.context.flash;

import static com.sun.faces.context.flash.ELFlash.FLASH_COOKIE_NAME;
import static com.sun.faces.context.flash.ELFlash.CONSTANTS.DidWriteCookieAttributeName;
import static com.sun.faces.context.flash.ELFlash.CONSTANTS.ForceSetMaxAgeZero;
import static com.sun.faces.context.flash.ELFlash.CONSTANTS.KeepAllMessagesAttributeName;
import static com.sun.faces.context.flash.ELFlash.CONSTANTS.RequestFlashManager;
import static com.sun.faces.context.flash.ELFlash.CONSTANTS.SavedResponseCompleteFlagValue;
import static jakarta.faces.event.PhaseId.RENDER_RESPONSE;
import static jakarta.faces.event.PhaseId.RESTORE_VIEW;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sun.faces.context.flash.ELFlash.CONSTANTS;
import com.sun.faces.context.flash.ELFlash.PreviousNextFlashInfoManager;
import com.sun.faces.util.ByteArrayGuardAESGCM;

public class ELFlashTest {

    private static final int SEQUENCE_NUMBER_SEED_SAMPLES = 1000;

    private MockedStatic<FacesContext> mockedStaticFacesContext;
    private FacesContext mockedFacesContext;
    private ExternalContext mockedExternalContext;
    private ServletContext mockedServletContext;
    
    private Map<Object, Object> contextMap;
    private Map<String, Object> applicationMap;
    private Map<String, Object> cookieMap;
    private Map<String, Map<String, Object>> flashInnerMap;

    @BeforeEach
    public void setup() {
        mockedStaticFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext = mock(FacesContext.class);
        mockedExternalContext = mock(ExternalContext.class);
        mockedServletContext = mock(ServletContext.class);
        contextMap = new HashMap<Object, Object>();
        applicationMap = new HashMap<String, Object>();
        cookieMap = new HashMap<String, Object>();
        flashInnerMap = new HashMap<String, Map<String, Object>>();

        mockedStaticFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockedFacesContext);
        when(mockedFacesContext.getExternalContext()).thenReturn(mockedExternalContext);
        when(mockedFacesContext.getAttributes()).thenReturn(contextMap);
        when(mockedExternalContext.getContext()).thenReturn(mockedServletContext);
        when(mockedExternalContext.getApplicationMap()).thenReturn(applicationMap);
        when(mockedExternalContext.getRequestCookieMap()).thenReturn(cookieMap);
        when(mockedExternalContext.getRequestContextPath()).thenReturn("");
        when(mockedServletContext.getInitParameterNames()).thenReturn(emptyEnumeration());

        when(mockedExternalContext.getFlash()).then($ -> {
            Flash flash = ELFlash.getFlash(mockedExternalContext, true);

            if (mockedFacesContext.getCurrentPhaseId() == RESTORE_VIEW) {
                flash.doPrePhaseActions(mockedFacesContext);
            } else {
                flash.doPostPhaseActions(mockedFacesContext);
            }
            return flash;
        });

        doAnswer(invocation -> {
            String name = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            cookieMap.put(name, new Cookie(name, value));
            return null;
        }).when(mockedExternalContext).addResponseCookie(any(), any(), any());
    }

    @AfterEach
    public void teardown() {
        mockedStaticFacesContext.close();
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetEmptyFlashDuringRestoreView() {
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(emptyMap(), cookieMap);
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetEmptyFlashDuringRestoreViewAndKeepMessages() {
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(emptyMap(), cookieMap);
        assertEquals(Set.of(CONSTANTS.KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(true, flash.get(KeepAllMessagesAttributeName.toString()));
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetEmptyFlashDuringRenderResponse() {
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(emptyMap(), cookieMap);
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5610
     */
    @Test
    public void testGetEmptyFlashDuringRestoreViewAndRenderResponse() {
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(emptyMap(), cookieMap);
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        long firstSequenceNumber = assertConsecutiveSequenceNumbers(flashInfo);

        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager), contextMap.keySet());
        flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(firstSequenceNumber + 2, assertConsecutiveSequenceNumbers(flashInfo));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetEmptyFlashDuringRenderResponseAndKeepMessages() {
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(CONSTANTS.KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(null, flash.get(KeepAllMessagesAttributeName.toString()));
        assertEquals(emptyMap(), cookieMap);
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithNullValueDuringRestoreView() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, null));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(0, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(null, flashInfo.getNextRequestFlashInfo());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithNullValueDuringRestoreViewAndKeepMessages() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, null));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(0, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(null, flashInfo.getNextRequestFlashInfo());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithEmptyValueDuringRestoreView() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, ""));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(0, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(null, flashInfo.getNextRequestFlashInfo());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithEmptyValueDuringRestoreViewAndKeepMessages() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, ""));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(0, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(null, flashInfo.getNextRequestFlashInfo());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithNullValueDuringRenderResponse() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, null));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5610
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithNullValueDuringRestoreViewAndRenderResponse() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, null));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(0, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(null, flashInfo.getNextRequestFlashInfo());

        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithNullValueDuringRenderResponseAndKeepMessages() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, null));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithEmptyValueDuringRenderResponse() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, ""));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5610
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithEmptyValueDuringRestoreViewAndRenderResponse() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, ""));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RESTORE_VIEW);

        Flash flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        assertEquals(false, contextMap.get(SavedResponseCompleteFlagValue));
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(0, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(null, flashInfo.getNextRequestFlashInfo());

        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, ""));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager, ForceSetMaxAgeZero), contextMap.keySet());
        flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithEmptyValueDuringRenderResponseAndKeepMessages() {
        cookieMap.put(FLASH_COOKIE_NAME, new Cookie(FLASH_COOKIE_NAME, ""));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(emptyMap().toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertConsecutiveSequenceNumbers(flashInfo);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithPriorValueDuringRenderResponse() {
        contextMap.put(RequestFlashManager, mockPreviousNextFlashInfoManager(flashInnerMap));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Map.of("1", emptyMap()).toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        long firstSequenceNumber = assertConsecutiveSequenceNumbers(flashInfo);

        Flash nextFlash = mockedExternalContext.getFlash();

        assertNotNull(nextFlash);
        assertEquals(emptySet(), nextFlash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Map.of("1", emptyMap()).toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager, DidWriteCookieAttributeName), contextMap.keySet());
        PreviousNextFlashInfoManager nextFlashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(firstSequenceNumber + 2, assertConsecutiveSequenceNumbers(nextFlashInfo));
        assertEquals(true, contextMap.get(DidWriteCookieAttributeName));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5552
     */
    @Test
    public void testGetPreviousRequestFlashViaCookieWithPriorValueDuringRenderResponseAndKeepMessages() {
        contextMap.put(RequestFlashManager, mockPreviousNextFlashInfoManager(flashInnerMap));
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        Flash flash = mockedExternalContext.getFlash();
        flash.setKeepMessages(true);

        assertNotNull(flash);
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Map.of("1", emptyMap()).toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager), contextMap.keySet());
        PreviousNextFlashInfoManager flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        long firstSequenceNumber = assertConsecutiveSequenceNumbers(flashInfo);

        Flash nextFlash = mockedExternalContext.getFlash();
        nextFlash.setKeepMessages(true);

        assertNotNull(nextFlash);
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Map.of("1", emptyMap()).toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager, DidWriteCookieAttributeName), contextMap.keySet());
        PreviousNextFlashInfoManager nextFlashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(firstSequenceNumber + 2, assertConsecutiveSequenceNumbers(nextFlashInfo));
        assertEquals(true, contextMap.get(DidWriteCookieAttributeName));
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5662
     */
    @Test
    public void testDecodeGarbageCookieResetsNextRequestFlashInfo() throws Exception {
        ByteArrayGuardAESGCM mockGuard = mock(ByteArrayGuardAESGCM.class);
        when(mockGuard.encrypt(any())).thenReturn("encrypted");
        when(mockGuard.decrypt(any())).thenReturn("some_garbage");

        PreviousNextFlashInfoManager manager = new PreviousNextFlashInfoManager(mockGuard, flashInnerMap);
        manager.decode(mockedFacesContext, null, new Cookie(FLASH_COOKIE_NAME, "somevalue"));

        assertNotNull(manager.getPreviousRequestFlashInfo());
        assertNotNull(manager.getPreviousRequestFlashInfo().getLifetimeMarker());
        assertEquals(null, manager.getNextRequestFlashInfo());
        assertEquals(Boolean.TRUE, contextMap.get(ForceSetMaxAgeZero));
        assertDoesNotThrow(() -> manager.encode());
    }

    /**
     * Sequence numbers start at a random point which still leaves the counter at least half of its range to climb
     * before reaching {@link Long#MAX_VALUE}, where it would start over at numbers a browser may still be holding.
     */
    @Test
    public void testSequenceNumberSeedLeavesRoomToCountUp() {
        for (int i = 0; i < SEQUENCE_NUMBER_SEED_SAMPLES; i++) {
            long seed = ELFlash.newSequenceNumberSeed();
            assertTrue(seed >= 0, () -> "seed " + seed + " is negative");
            assertTrue(Long.MAX_VALUE - seed >= Long.MAX_VALUE / 2, () -> "seed " + seed + " leaves too little room");
        }
    }

    /**
     * Two nodes must not start counting at the same point, which is what keeps one node's sequence numbers out of
     * the flashes another node files under the owner shared by every sessionless request.
     */
    @Test
    public void testSequenceNumberSeedIsRandom() {
        Set<Long> seeds = new HashSet<>();

        for (int i = 0; i < SEQUENCE_NUMBER_SEED_SAMPLES; i++) {
            seeds.add(ELFlash.newSequenceNumberSeed());
        }

        assertTrue(seeds.size() > SEQUENCE_NUMBER_SEED_SAMPLES / 2, () -> "only " + seeds.size() + " distinct seeds");
    }

    /**
     * Sequence numbers start at a random point, so what a flash manager owes is a pair of them in order.
     *
     * @return the sequence number of the previous request flash info
     */
    private static long assertConsecutiveSequenceNumbers(PreviousNextFlashInfoManager flashInfo) {
        long previousSequenceNumber = flashInfo.getPreviousRequestFlashInfo().getSequenceNumber();
        assertEquals(previousSequenceNumber + 1, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
        return previousSequenceNumber;
    }

    private PreviousNextFlashInfoManager mockPreviousNextFlashInfoManager(Map<String, Map<String, Object>> flashInnerMap) {
        PreviousNextFlashInfoManager manager = new PreviousNextFlashInfoManager(new ByteArrayGuardAESGCM(), flashInnerMap);
        manager.initializeBaseCase(new ELFlash(mockedExternalContext) {
            @Override
            long getNewSequenceNumber() {
                return 1L;
            }
        });
        return manager;
    }
}
