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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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
import com.sun.faces.util.ByteArrayGuardAESCTR;

public class ELFlashTest {

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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
        
        when(mockedFacesContext.getCurrentPhaseId()).thenReturn(RENDER_RESPONSE);

        flash = mockedExternalContext.getFlash();

        assertNotNull(flash);
        assertEquals(emptySet(), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Set.of(SavedResponseCompleteFlagValue, RequestFlashManager), contextMap.keySet());
        flashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(3, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(4, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());

        Flash nextFlash = mockedExternalContext.getFlash();

        assertNotNull(nextFlash);
        assertEquals(emptySet(), nextFlash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Map.of("1", emptyMap()).toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager, DidWriteCookieAttributeName), contextMap.keySet());
        PreviousNextFlashInfoManager nextFlashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(3, nextFlashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(4, nextFlashInfo.getNextRequestFlashInfo().getSequenceNumber());
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
        assertEquals(1, flashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(2, flashInfo.getNextRequestFlashInfo().getSequenceNumber());

        Flash nextFlash = mockedExternalContext.getFlash();
        nextFlash.setKeepMessages(true);

        assertNotNull(nextFlash);
        assertEquals(Set.of(KeepAllMessagesAttributeName.toString()), flash.keySet());
        assertEquals(Set.of(FLASH_COOKIE_NAME), cookieMap.keySet());
        assertEquals(Map.of("1", emptyMap()).toString(), flashInnerMap.toString());
        assertEquals(Set.of(RequestFlashManager, DidWriteCookieAttributeName), contextMap.keySet());
        PreviousNextFlashInfoManager nextFlashInfo = (PreviousNextFlashInfoManager) contextMap.get(RequestFlashManager);
        assertEquals(3, nextFlashInfo.getPreviousRequestFlashInfo().getSequenceNumber());
        assertEquals(4, nextFlashInfo.getNextRequestFlashInfo().getSequenceNumber());
        assertEquals(true, contextMap.get(DidWriteCookieAttributeName));
    }

    private PreviousNextFlashInfoManager mockPreviousNextFlashInfoManager(Map<String, Map<String, Object>> flashInnerMap) {
        PreviousNextFlashInfoManager manager = new PreviousNextFlashInfoManager(new ByteArrayGuardAESCTR(), flashInnerMap);
        manager.initializeBaseCase(new ELFlash(mockedExternalContext) {
            @Override
            long getNewSequenceNumber() {
                return 1L;
            }
        });
        return manager;
    }
}
