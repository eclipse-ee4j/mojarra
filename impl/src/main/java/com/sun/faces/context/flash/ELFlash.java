/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.ForceAlwaysWriteFlashCookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.facelets.tag.ui.UIDebug;
import com.sun.faces.util.ByteArrayGuardAESCTR;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PostKeepFlashValueEvent;
import jakarta.faces.event.PostPutFlashValueEvent;
import jakarta.faces.event.PreClearFlashEvent;
import jakarta.faces.event.PreRemoveFlashValueEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 * How this implementation works
 * </p>
 *
 * <p>
 * This class is an application singleton. It has one ivar, innerMap. Entries are added to and removed from this map as
 * needed according to how the flash scope is defined in the spec. This implementation never touches the session, nor
 * does it cause the session to be created.
 * </p>
 *
 * <p>
 * Most of the hairy logic is encapsulated with in the inner class PreviousNextFlashInfoManager. An instance of this
 * class is obtained by calling one of the variants of getCurrentFlashManager(). When the instance is no longer needed
 * for this request, call releaseCurrentFlashManager().
 * </p>
 *
 * <p>
 * Two very important methods are getPhaseMapForWriting() and getPhaseMapForReading(). These methods are the basis for
 * the Map implementation methods. Methods that need to write to the map use getPhaseMapForWriting(), those that need to
 * read use getPhaseMapForReading(). These methods allow for the laziness that allows us to only incur a cost when the
 * flash is actually written to.
 * </p>
 *
 * <p>
 * The operation of this class is intimately tied to the request processing lifecycle. Let's break down every run thru
 * the request processing lifecycle into two parts called "previous" and "next". We use the names "previous" and "next"
 * to indicate the persistence and timing of the data that is stored in the flash. Consider two runs through the requset
 * processing lifecle: N and N+1. On request N, there is no "previous" request. On Request N, any writes to the flash
 * that happen during RENDER RESPONSE go to the "next" flash map. This means they are available for the ENTIRE run
 * though the request processing lifecycle on request N+1. Any entries put into the "next" flash map on request N will
 * be expired at the end of request N+1. Now, when we get into request N+1 what was considered "next" on request N, is
 * now called "previous" from the perspective of request N+1. Any reads from the flash during request N+1 come from the
 * "previous" map. Any writes to the flash before RENDER RESPONSE go to the "previous" map. Any writes to the flash
 * during RENDER RESPNOSE go to the "next" map.
 * </p>
 */

public class ELFlash extends Flash {

    // <editor-fold defaultstate="collapsed" desc="ivars">

    /**
     * <p>
     * Keys in this map are the string version of sequence numbers obtained via calls to {@link #getNewSequenceNumber}.
     * Values are the actual Map instances that back the actual Map methods on this class. All writes to and reads from this
     * map are done by the {@link PreviousNextFlashInfoManager} inner class.
     * </p>
     *
     */
    private Map<String, Map<String, Object>> flashInnerMap = null;

    private final AtomicLong sequenceNumber = new AtomicLong(0);

    private int numberOfConcurentFlashUsers = Integer.parseInt(WebContextInitParameter.NumberOfConcurrentFlashUsers.getDefaultValue());

    private long numberOfFlashesBetweenFlashReapings = Long.parseLong(WebContextInitParameter.NumberOfFlashesBetweenFlashReapings.getDefaultValue());

    private final boolean distributable;

    private final ByteArrayGuardAESCTR guard;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="class vars">

    private static final String ELEMENT_TYPE_MISMATCH = "element-type-mismatch";

    private static final Logger LOGGER = FacesLogger.FLASH.getLogger();

    /**
     * <p>
     * These constants are referenced from other source files in this package. This one is a disambiguator prefix.
     * </p>
     */
    static final String PREFIX = "csfcf";

    /**
     * <p>
     * This constant is used as the key in the application map that stores the singleton ELFlash instance.
     * </p>
     */
    static final String FLASH_ATTRIBUTE_NAME = PREFIX + "f";

    /**
     * <p>
     * This constant is used as the name of the cookie sent to the client. The cookie is used to allow the flash scope to be
     * used to support POST REDIRECT GET navigation.
     * </p>
     */
    static final String FLASH_COOKIE_NAME = PREFIX + "c";

    /**
     * <p>
     * This constant is used as the key the request map used, in the FlashELResolver, to convey the name of the property
     * being accessed via 'now'.
     * </p>
     */
    static final String FLASH_NOW_REQUEST_KEY = FLASH_ATTRIBUTE_NAME + "n";

    private enum CONSTANTS {

        /**
         * The key in the FacesContext attributes map (hereafter referred to as contextMap) for the request scoped
         * {@link PreviousNextFlashInfoManager}.
         */

        RequestFlashManager,

        /**
         * At the beginning of every phase, we save the value of the facesContext.getResponseComplete() into the contextMap
         * under this key. We check this value after the phase to see if this is the phase where the user called
         * responseComplete(). This is important to cover cases when the user does some funny lifecycle stuff.
         */

        SavedResponseCompleteFlagValue,

        /**
         * This is used as the key in the flash itself to store the messages if they are being tracked.
         */

        FacesMessageAttributeName,

        /**
         * This is used as the key in the flash itself to track whether or not messages are being saved across request/response
         * boundaries.
         */

        KeepAllMessagesAttributeName,

        /**
         * This key is used in the contextMap to indicate that the next get should be treated as a keep.
         *
         */
        KeepFlagAttributeName,

        /**
         * This key is used in the contextMap to prevent setting the cookie twice.
         */
        DidWriteCookieAttributeName,

        /**
         * Force setMaxAge(0)
         */
        ForceSetMaxAgeZero,

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors and instance accessors">

    /** Creates a new instance of ELFlash */
    private ELFlash(ExternalContext extContext) {
        flashInnerMap = new ConcurrentHashMap<>();
        WebConfiguration config = WebConfiguration.getInstance(extContext);
        String value;
        try {
            value = config.getOptionValue(WebContextInitParameter.NumberOfConcurrentFlashUsers);
            numberOfConcurentFlashUsers = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Unable to set number of concurrent flash users.  Defaulting to {0}", numberOfConcurentFlashUsers);
            }

        }

        try {
            value = config.getOptionValue(WebContextInitParameter.NumberOfFlashesBetweenFlashReapings);
            numberOfFlashesBetweenFlashReapings = Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Unable to set number flashes between flash repaings.  Defaulting to {0}", numberOfFlashesBetweenFlashReapings);
            }

        }

        distributable = config.isOptionEnabled(EnableDistributable);

        guard = new ByteArrayGuardAESCTR();

    }

    /**
     * <p>
     * Returns the flash <code>Map</code> for this application. This is a convenience method that calls
     * <code>FacesContext.getCurrentInstance()</code> and then calls the overloaded <code>getFlash()</code> that takes a
     * <code>FacesContext</code> with it.
     * </p>
     *
     * @return The flash <code>Map</code> for this session.
     */

    public static Map<String, Object> getFlash() {
        FacesContext context = FacesContext.getCurrentInstance();
        return getFlash(context.getExternalContext(), true);
    }

    /**
     *
     * @param extContext the <code>ExternalContext</code> for this request.
     *
     * @param create <code>true</code> to create a new instance for this request if necessary; <code>false</code> to return
     * <code>null</code> if there's no instance in the current <code>session</code>.
     *
     * @return The flash <code>Map</code> for this session.
     */

    static ELFlash getFlash(ExternalContext extContext, boolean create) {
        Map<String, Object> appMap = extContext.getApplicationMap();
        ELFlash flash = (ELFlash) appMap.get(FLASH_ATTRIBUTE_NAME);
        if (null == flash && create) {
            synchronized (extContext.getContext()) {
                if (null == (flash = (ELFlash) appMap.get(FLASH_ATTRIBUTE_NAME))) {
                    flash = new ELFlash(extContext);
                    appMap.put(FLASH_ATTRIBUTE_NAME, flash);
                }
            }
        }

        /*
         * If we are in a clustered environment and a session is active, store a helper to ensure our innerMap gets successfully
         * replicated.
         */
        if (appMap.get(EnableDistributable.getQualifiedName()) != null) {
            synchronized (extContext.getContext()) {
                if (extContext.getSession(false) != null) {
                    SessionHelper sessionHelper = SessionHelper.getInstance(extContext);
                    if (sessionHelper == null) {
                        sessionHelper = new SessionHelper();
                    }
                    sessionHelper.update(extContext, flash);
                }
            }
        }

        return flash;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Abstract class overrides">

    @Override
    public boolean isKeepMessages() {
        boolean result = false;
        Map<String, Object> phaseMap;

        if (null != (phaseMap = loggingGetPhaseMapForReading(false))) {
            Object value = phaseMap.get(CONSTANTS.KeepAllMessagesAttributeName.toString());
            result = null != value ? (Boolean) value : false;
        }

        return result;
    }

    @Override
    public void setKeepMessages(boolean newValue) {

        loggingGetPhaseMapForWriting(false).put(CONSTANTS.KeepAllMessagesAttributeName.toString(), newValue);

    }

    @Override
    public boolean isRedirect() {
        boolean result = false;

        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextMap = context.getAttributes();
        PreviousNextFlashInfoManager flashManager;
        if (null != (flashManager = getCurrentFlashManager(contextMap, false))) {
            result = flashManager.getPreviousRequestFlashInfo().isIsRedirect();
        }

        return result;
    }

    // PENDING(edburns): I'm going to make an entry to the errata. This
    // method can't be implemented because the decision of whether or
    // not to redirect is made by the navigationHandler.
    @Override
    public void setRedirect(boolean newValue) {
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Map overrides">

    @SuppressWarnings(ELEMENT_TYPE_MISMATCH)
    @Override
    public Object get(Object key) {
        Object result = null;

        FacesContext context = FacesContext.getCurrentInstance();
        if (null != key) {
            if (key.equals("keepMessages")) {
                result = isKeepMessages();
            } else if (key.equals("redirect")) {
                result = isRedirect();
            } else {
                if (isKeepFlagSet(context)) {
                    result = getPhaseMapForReading().get(key);
                    keep(key.toString());
                    clearKeepFlag(context);
                    return result;
                }

            }

        }

        if (null == result) {
            result = getPhaseMapForReading().get(key);
        }
        if (distributable && context.getExternalContext().getSession(false) != null) {
            SessionHelper sessionHelper = SessionHelper.getInstance(context.getExternalContext());
            if (sessionHelper != null) {
                sessionHelper.update(context.getExternalContext(), this);
            }
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "get({0}) = {1}", new Object[] { key, result });
        }

        return result;
    }

    @Override
    public Object put(String key, Object value) {
        Boolean b = null;
        Object result = null;
        boolean wasSpecialPut = false;

        if (null != key) {
            if (key.equals("keepMessages")) {
                setKeepMessages(b = Boolean.parseBoolean((String) value));
                wasSpecialPut = true;
            }
            if (key.equals("redirect")) {
                setRedirect(b = Boolean.parseBoolean((String) value));
                wasSpecialPut = true;
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        if (!wasSpecialPut) {
            result = null == b ? getPhaseMapForWriting().put(key, value) : b;
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "put({0},{1})", new Object[] { key, value });
            }
            context.getApplication().publishEvent(context, PostPutFlashValueEvent.class, key);
        }
        if (distributable && context.getExternalContext().getSession(false) != null) {
            SessionHelper sessionHelper = SessionHelper.getInstance(context.getExternalContext());
            if (sessionHelper != null) {
                sessionHelper.update(context.getExternalContext(), this);
            }
        }

        return result;
    }

    @SuppressWarnings(ELEMENT_TYPE_MISMATCH)
    @Override
    public Object remove(Object key) {
        Object result = null;

        FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().publishEvent(context, PreRemoveFlashValueEvent.class, key);
        result = getPhaseMapForWriting().remove(key);

        return result;
    }

    @SuppressWarnings(ELEMENT_TYPE_MISMATCH)
    @Override
    public boolean containsKey(Object key) {
        boolean result = false;

        result = getPhaseMapForReading().containsKey(key);

        return result;
    }

    @Override
    public boolean containsValue(Object value) {
        boolean result = false;

        result = getPhaseMapForReading().containsValue(value);

        return result;
    }

    @Override
    public void putAll(Map<? extends String, ?> t) {

        getPhaseMapForWriting().putAll(t);

    }

    @Override
    public Collection<Object> values() {
        Collection<Object> result = null;

        result = getPhaseMapForReading().values();

        return result;
    }

    @Override
    public int size() {
        int result = 0;

        result = getPhaseMapForReading().size();

        return result;
    }

    @Override
    public void clear() {

        getPhaseMapForWriting().clear();

    }

    @SuppressWarnings({ "CloneDoesntCallSuperClone" })
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> readingMapEntrySet = getPhaseMapForReading().entrySet(), writingMapEntrySet = getPhaseMapForWriting().entrySet(),
                result = null;

        result = new HashSet<>();
        result.addAll(readingMapEntrySet);
        result.addAll(writingMapEntrySet);

        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean readingMapIsEmpty = getPhaseMapForReading().isEmpty(), writingMapIsEmpty = getPhaseMapForWriting().isEmpty(), result = false;

        result = readingMapIsEmpty && writingMapIsEmpty;

        return result;
    }

    @Override
    public Set<String> keySet() {
        Set<String> readingMapKeySet = getPhaseMapForReading().keySet(), writingMapKeySet = getPhaseMapForWriting().keySet(), result = null;

        result = new HashSet<>();
        result.addAll(readingMapKeySet);
        result.addAll(writingMapKeySet);

        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Flash overrides">

    @Override
    public void keep(String key) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        Map<Object, Object> contextMap = context.getAttributes();
        PreviousNextFlashInfoManager flashManager;
        if (null != (flashManager = getCurrentFlashManager(contextMap, true))) {
            Object toKeep;

            if (null == (toKeep = requestMap.remove(key))) {
                FlashInfo flashInfo = null;
                if (null != (flashInfo = flashManager.getPreviousRequestFlashInfo())) {
                    toKeep = flashInfo.getFlashMap().get(key);
                }
            }

            if (null != toKeep) {
                getPhaseMapForWriting().put(key, toKeep);
                context.getApplication().publishEvent(context, PostKeepFlashValueEvent.class, key);

            }
        }

    }

    @Override
    public void putNow(String key, Object value) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextMap = context.getAttributes();
        PreviousNextFlashInfoManager flashManager;
        if (null != (flashManager = getCurrentFlashManager(contextMap, true))) {
            FlashInfo flashInfo = null;
            if (null != (flashInfo = flashManager.getPreviousRequestFlashInfo())) {
                flashInfo.getFlashMap().put(key, value);
            }
        }
    }

    @Override
    public void doPrePhaseActions(FacesContext context) {
        PhaseId currentPhase = context.getCurrentPhaseId();
        Map<Object, Object> contextMap = context.getAttributes();
        contextMap.put(CONSTANTS.SavedResponseCompleteFlagValue, context.getResponseComplete());

        Cookie cookie = null;
        if (currentPhase.equals(PhaseId.RESTORE_VIEW)) {

            if (null != (cookie = getCookie(context.getExternalContext()))) {
                getCurrentFlashManager(context, contextMap, cookie);
            }

            if (isKeepMessages()) {
                restoreAllMessages(context);
            }
        } else if (currentPhase.equals(PhaseId.RENDER_RESPONSE) && contextMap.containsKey(ForceAlwaysWriteFlashCookie)
                && (Boolean) contextMap.get(ForceAlwaysWriteFlashCookie)) {
            PreviousNextFlashInfoManager flashManager = getCurrentFlashManager(contextMap, true);
            cookie = flashManager.encode();
            if (null != cookie) {
                setCookie(context, flashManager, cookie, true);
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "faces.externalcontext.flash.force.write.cookie.failed");
                }
            }
        }

    }

    @Override
    public void doPostPhaseActions(FacesContext context) {
        if (context.getAttributes().containsKey(ACT_AS_DO_LAST_PHASE_ACTIONS)) {
            Boolean outgoingResponseIsRedirect = (Boolean) context.getAttributes().get(ACT_AS_DO_LAST_PHASE_ACTIONS);
            doLastPhaseActions(context, outgoingResponseIsRedirect);
            return;
        }

        PhaseId currentPhase = context.getCurrentPhaseId();
        Map<Object, Object> contextMap = context.getAttributes();
        boolean responseCompleteJustSetTrue = responseCompleteWasJustSetTrue(context, contextMap),
                lastPhaseForThisRequest = responseCompleteJustSetTrue || currentPhase == PhaseId.RENDER_RESPONSE;
        if (lastPhaseForThisRequest) {
            doLastPhaseActions(context, false);
        }
    }

    public static final String ACT_AS_DO_LAST_PHASE_ACTIONS = ELFlash.class.getPackage().getName() + ".ACT_AS_DO_LAST_PHASE_ACTIONS";

    /**
     * <p>
     * This is the most magic of methods. There are several scenarios in which this method can be called, but the first time
     * it is called for a request it takes action, while on subsequent times it returns without taking action. This is due
     * to the call to {@link #releaseCurrentFlashManager}. After this call, any calls to {@link #getCurrentFlashManager}
     * will return null.
     * </p>
     *
     * <p>
     * Scenario 1: normal request ending. This will be called after the RENDER_RESPONSE phase executes.
     * outgoingResponseIsRedirect will be false.
     * </p>
     *
     * <p>
     * Scenario 2: navigationHandler asks extContext for redirect. In this case, extContext calls this method directly,
     * outgoingResponseIsRedirect will be true.
     * </p>
     *
     * <p>
     * Scenario 3: extContext.flushBuffer(): As far as I can tell, this was only called in the JSP case, but it's good to
     * call it from there anyway, because we need to write our cookie before the response is committed.
     * outgoingResponseIsRedirect is false.
     * </p>
     *
     * <p>
     * Scenario 4: after rendering the response in JSP, but before the buffer is flushed. In the JSP case, I've found this
     * necessary because the call to extContext.flushBuffer() is too late, the response has already been committed by that
     * point. outgoingResponseIsRedirect is false.
     * </p>
     * 
     * @param context the involved faces context
     * @param outgoingResponseIsRedirect whether outgoing response is redirect
     */
    public void doLastPhaseActions(FacesContext context, boolean outgoingResponseIsRedirect) {
        Map<Object, Object> contextMap = context.getAttributes();
        PreviousNextFlashInfoManager flashManager = getCurrentFlashManager(contextMap, false);
        if (null == flashManager) {
            return;
        }
        if (isKeepMessages()) {
            saveAllMessages(context);
        }
        releaseCurrentFlashManager(contextMap);

        // What we do in this if-else statement has consequences for
        // PreviousNextFlashInfoManager.decode().

        if (outgoingResponseIsRedirect) {
            FlashInfo previousRequestFlashInfo = flashManager.getPreviousRequestFlashInfo();
            // Next two methods are VITALLY IMPORTANT!
            previousRequestFlashInfo.setIsRedirect(true);
            flashManager.expireNext_MovePreviousToNext();
        } else {
            FlashInfo flashInfo = flashManager.getPreviousRequestFlashInfo();
            if (null != flashInfo && flashInfo.getLifetimeMarker() == LifetimeMarker.SecondTimeThru) {
                flashManager.expirePrevious();
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "---------------------------------------");
        }

        setCookie(context, flashManager, flashManager.encode(), false);

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helpers">

    void setFlashInnerMap(Map<String, Map<String, Object>> flashInnerMap) {
        this.flashInnerMap = flashInnerMap;
    }

    Map<String, Map<String, Object>> getFlashInnerMap() {
        return flashInnerMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        for (Map.Entry<String, Object> entry : entrySet()) {
            builder.append("{").append(entry.getKey()).append(", ").append(entry.getValue()).append("}\n");
        }
        builder.append("]\n");
        return builder.toString();
    }

    private void maybeWriteCookie(FacesContext context, PreviousNextFlashInfoManager flashManager) {
        FlashInfo flashInfo = flashManager.getPreviousRequestFlashInfo();
        if (null != flashInfo && flashInfo.getLifetimeMarker() == LifetimeMarker.SecondTimeThru) {
            PreviousNextFlashInfoManager copiedFlashManager = flashManager.copyWithoutInnerMap();
            copiedFlashManager.expirePrevious();
            setCookie(context, flashManager, copiedFlashManager.encode(), false);
        }
    }

    static void setKeepFlag(FacesContext context) {
        context.getAttributes().put(CONSTANTS.KeepFlagAttributeName, Boolean.TRUE);
    }

    void clearKeepFlag(FacesContext context) {
        context.getAttributes().remove(CONSTANTS.KeepFlagAttributeName);
    }

    boolean isKeepFlagSet(FacesContext context) {
        return Boolean.TRUE == context.getAttributes().get(CONSTANTS.KeepFlagAttributeName);
    }

    private long getNewSequenceNumber() {
        long result = sequenceNumber.incrementAndGet();

        if (0 == result % numberOfFlashesBetweenFlashReapings) {
            reapFlashes();
        }

        if (result == Long.MAX_VALUE) {
            result = 1;
            sequenceNumber.set(1);
        }

        return result;
    }

    private void reapFlashes() {

        if (flashInnerMap.size() < numberOfConcurentFlashUsers) {
            return;
        }

        Set<String> keys = flashInnerMap.keySet();
        long sequenceNumberToTest, currentSequenceNumber = sequenceNumber.get();
        Map<String, Object> curFlash;
        for (String cur : keys) {
            sequenceNumberToTest = Long.parseLong(cur);
            if (numberOfConcurentFlashUsers < currentSequenceNumber - sequenceNumberToTest) {
                if (null != (curFlash = flashInnerMap.get(cur))) {
                    curFlash.clear();
                }
                flashInnerMap.remove(cur);
            }
        }
        if (distributable && FacesContext.getCurrentInstance().getExternalContext().getSession(false) != null) {
            ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
            SessionHelper sessionHelper = SessionHelper.getInstance(extContext);
            if (null != sessionHelper) {
                sessionHelper.remove(extContext);
                sessionHelper = new SessionHelper();
                sessionHelper.update(extContext, this);
            }
        }
    }

    private boolean responseCompleteWasJustSetTrue(FacesContext context, Map<Object, Object> contextMap) {
        boolean result = false;

        // If it was false, but it's now true, return true
        result = Boolean.FALSE == contextMap.get(CONSTANTS.SavedResponseCompleteFlagValue) && context.getResponseComplete();

        return result;
    }

    private static String getLogPrefix(FacesContext context) {
        StringBuilder result = new StringBuilder();
        ExternalContext extContext = context.getExternalContext();
        Object request = extContext.getRequest();
        if (request instanceof HttpServletRequest) {
            result.append(((HttpServletRequest) request).getMethod()).append(" ");
        }
        UIViewRoot root = context.getViewRoot();
        if (null != root) {
            String viewId = root.getViewId();
            if (null != viewId) {
                result.append(viewId).append(" ");
            }
        }

        return result.toString();
    }

    private Map<String, Object> loggingGetPhaseMapForWriting(boolean loggingEnabled) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> result = null;
        PhaseId currentPhase = context.getCurrentPhaseId();
        Map<Object, Object> contextMap = context.getAttributes();

        PreviousNextFlashInfoManager flashManager;
        if (null != (flashManager = getCurrentFlashManager(contextMap, true))) {
            FlashInfo flashInfo;
            boolean isDebugLog = loggingEnabled && LOGGER.isLoggable(Level.FINEST);

            if (currentPhase.getOrdinal() < PhaseId.RENDER_RESPONSE.getOrdinal()) {
                flashInfo = flashManager.getPreviousRequestFlashInfo();
                if (isDebugLog) {
                    LOGGER.log(Level.FINEST, "{0}previous[{1}]", new Object[] { getLogPrefix(context), flashInfo.getSequenceNumber() });
                }
            } else {
                flashInfo = flashManager.getNextRequestFlashInfo(this, true);
                if (isDebugLog) {
                    LOGGER.log(Level.FINEST, "{0}next[{1}]", new Object[] { getLogPrefix(context), flashInfo.getSequenceNumber() });
                }
                maybeWriteCookie(context, flashManager);
            }
            result = flashInfo.getFlashMap();
        }

        return result;

    }

    /**
     * <p>
     * If the current phase is earlier than RENDER_RESPONSE, return the map for the "previous" request. Otherwise, return
     * the map for the "next" request. Note that we use getCurrentFlashManager(contextMap,true). This is because if this
     * method is being called, we know we actually need the map, so we have to ensure the underlying data structure is
     * present before trying to access it.
     * </p>
     */

    private Map<String, Object> getPhaseMapForWriting() {
        return loggingGetPhaseMapForWriting(true);
    }

    private Map<String, Object> loggingGetPhaseMapForReading(boolean loggingEnabled) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> result = Collections.emptyMap();
        Map<Object, Object> contextMap = context.getAttributes();

        PreviousNextFlashInfoManager flashManager;
        if (null != (flashManager = getCurrentFlashManager(contextMap, false))) {
            FlashInfo flashInfo;

            if (null != (flashInfo = flashManager.getPreviousRequestFlashInfo())) {
                boolean isDebugLog = loggingEnabled && LOGGER.isLoggable(Level.FINEST);
                if (isDebugLog) {
                    LOGGER.log(Level.FINEST, "{0}previous[{1}]", new Object[] { getLogPrefix(context), flashInfo.getSequenceNumber() });
                }

                result = flashInfo.getFlashMap();
            }
        }

        return result;
    }

    /**
     * <p>
     * Always return the map for the "previous" request. Note that we use getCurrentFlashManager(contextMap,false). This is
     * because if this method is being called, and there is pre-existing data in the flash from a previous write, then the
     * PreviousNextFlashInfoManager will already have been created. If there is not pre-existing data, we don't create the
     * PreviousNextFlashInfoManager, and therefore just return the empty map.
     * </p>
     */

    private Map<String, Object> getPhaseMapForReading() {
        return loggingGetPhaseMapForReading(true);
    }

    void saveAllMessages(FacesContext context) {
        // take no action on the GET that comes after a REDIRECT
        Map<Object, Object> contextMap = context.getAttributes();
        PreviousNextFlashInfoManager flashManager;
        if (null == (flashManager = getCurrentFlashManager(contextMap, true))) {
            return;
        }
        if (flashManager.getPreviousRequestFlashInfo().isIsRedirect()) {
            return;
        }

        Iterator<String> messageClientIds = context.getClientIdsWithMessages();
        List<FacesMessage> facesMessages;
        Map<String, List<FacesMessage>> allFacesMessages = null;
        Iterator<FacesMessage> messageIter;
        String curMessageId;

        // Save all the FacesMessages into a Map, which we store in the flash.

        // Step 1, go through the FacesMessage instances for each clientId
        // in the messageClientIds list.
        while (messageClientIds.hasNext()) {
            curMessageId = messageClientIds.next();
            // Get the messages for this clientId
            messageIter = context.getMessages(curMessageId);
            facesMessages = new ArrayList<>();
            while (messageIter.hasNext()) {
                facesMessages.add(messageIter.next());
            }
            // Add the list to the map
            if (null == allFacesMessages) {
                allFacesMessages = new HashMap<>();
            }
            allFacesMessages.put(curMessageId, facesMessages);
        }
        facesMessages = null;

        // Step 2, go through the FacesMessages that do not have a client
        // id associated with them.
        messageIter = context.getMessages(null);
        // Make sure to overwrite the previous facesMessages list
        facesMessages = new ArrayList<>();
        while (messageIter.hasNext()) {
            facesMessages.add(messageIter.next());
        }
        if (null != facesMessages) {
            // Add the list to the map
            if (null == allFacesMessages) {
                allFacesMessages = new HashMap<>();
            }
            allFacesMessages.put(null, facesMessages);
        }
        getPhaseMapForWriting().put(CONSTANTS.FacesMessageAttributeName.toString(), allFacesMessages);

    }

    @SuppressWarnings(ELEMENT_TYPE_MISMATCH)
    void restoreAllMessages(FacesContext context) {
        Map<String, List<FacesMessage>> allFacesMessages;
        Map<String, Object> phaseMap = getPhaseMapForReading();
        List<FacesMessage> facesMessages;

        if (null != (allFacesMessages = (Map<String, List<FacesMessage>>) phaseMap.get(CONSTANTS.FacesMessageAttributeName.toString()))) {
            for (Map.Entry<String, List<FacesMessage>> cur : allFacesMessages.entrySet()) {
                if (null != (facesMessages = allFacesMessages.get(cur.getKey()))) {
                    for (FacesMessage curMessage : facesMessages) {
                        context.addMessage(cur.getKey(), curMessage);
                    }
                }
            }
            phaseMap.remove(CONSTANTS.FacesMessageAttributeName.toString());
        }
    }

    /**
     * <p>
     * Return the cookie that came from the browser, if any.
     * </p>
     */
    private Cookie getCookie(ExternalContext extContext) {
        Cookie result = null;

        result = (Cookie) extContext.getRequestCookieMap().get(FLASH_COOKIE_NAME);

        return result;
    }

    /**
     * <p>
     * Set the cookie iff the response was not yet committed. If the response was committed, log a warning.
     * </p>
     */

    private void setCookie(FacesContext context, PreviousNextFlashInfoManager flashManager, Cookie toSet, boolean forceWrite) {
        Map<Object, Object> contextMap = context.getAttributes();
        ExternalContext extContext = context.getExternalContext();
        if (contextMap.containsKey(CONSTANTS.DidWriteCookieAttributeName)) {
            return;
        }
        FlashInfo nextFlash = flashManager.getNextRequestFlashInfo(), prevFlash = flashManager.getPreviousRequestFlashInfo();
        if (context.getAttributes().containsKey(CONSTANTS.ForceSetMaxAgeZero)) {
            removeCookie(extContext, toSet);
            return;
        }

        // Don't try to write the cookie unless there is data in the flash.
        if (forceWrite || null != nextFlash && !nextFlash.getFlashMap().isEmpty() || null != prevFlash && !prevFlash.getFlashMap().isEmpty()) {
            if (extContext.isResponseCommitted()) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "faces.externalcontext.flash.response.already.committed");
                }
                if (nextFlash != null) {
                    flashManager.expireNext();
                }
                if (prevFlash != null) {
                    flashManager.expirePrevious();
                }
            } else {
                Map<String, Object> properties = new HashMap();
                Object val;

                if (null != (val = toSet.getComment())) {
                    properties.put("comment", val);
                }
                if (null != (val = toSet.getDomain())) {
                    properties.put("domain", val);
                }
                if (null != (val = toSet.getMaxAge())) {
                    properties.put("maxAge", val);
                }
                if (extContext.isSecure()) {
                    properties.put("secure", Boolean.TRUE);
                } else if (null != (val = toSet.getSecure())) {
                    properties.put("secure", val);
                }
                if (null != (val = toSet.getPath())) {
                    properties.put("path", val);
                }
                properties.put("httpOnly", Boolean.TRUE);
                extContext.addResponseCookie(toSet.getName(), toSet.getValue(), !properties.isEmpty() ? properties : null);
                properties = null;
            }
            contextMap.put(CONSTANTS.DidWriteCookieAttributeName, Boolean.TRUE);
        } else {
            removeCookie(extContext, toSet);
        }
    }

    private void removeCookie(ExternalContext extContext, Cookie toRemove) {
        if (extContext.isResponseCommitted()) {
            return;
        }
        Map<String, Object> properties = new HashMap();
        Object val;
        toRemove.setMaxAge(0);

        if (null != (val = toRemove.getComment())) {
            properties.put("comment", val);
        }
        if (null != (val = toRemove.getDomain())) {
            properties.put("domain", val);
        }
        if (null != (val = toRemove.getMaxAge())) {
            properties.put("maxAge", val);
        }
        if (extContext.isSecure()) {
            properties.put("secure", Boolean.TRUE);
        } else if (null != (val = toRemove.getSecure())) {
            properties.put("secure", val);
        }
        if (null != (val = toRemove.getPath())) {
            properties.put("path", val);
        }
        properties.put("httpOnly", Boolean.TRUE);
        extContext.addResponseCookie(toRemove.getName(), toRemove.getValue(), !properties.isEmpty() ? properties : null);
        properties = null;

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inner classes">

    private enum LifetimeMarker {

        // these must be unique

        FirstTimeThru("f"), SecondTimeThru("s"), IsRedirect("r"), IsNormal("n");

        private static char FIRST_TIME_THRU = 'f';
        private static char SECOND_TIME_THRU = 's';
        private static char IS_REDIRECT = 'r';
        private static char IS_NORMAL = 'n';

        private String name;

        private LifetimeMarker(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public char encode() {
            return name.charAt(0);
        }

        public static LifetimeMarker decode(char c) {
            LifetimeMarker result = FirstTimeThru;

            if (FIRST_TIME_THRU == c) {
                result = FirstTimeThru;
            } else if (SECOND_TIME_THRU == c) {
                result = SecondTimeThru;
            } else if (IS_REDIRECT == c) {
                result = IsRedirect;
            } else if (IS_NORMAL == c) {
                result = IsNormal;
            } else {
                throw new IllegalStateException("class invariant failed: invalid lifetime marker");
            }

            return result;
        }

    }

    private void releaseCurrentFlashManager(Map<Object, Object> contextMap) {
        contextMap.remove(CONSTANTS.RequestFlashManager);
    }

    /**
     * <p>
     * Called when you need to get access to the flashManager. If argument create is true, and no instance of the
     * FlashManager exists for this request, create it and store it in the contextMap.
     * </p>
     */
    private PreviousNextFlashInfoManager getCurrentFlashManager(Map<Object, Object> contextMap, boolean create) {
        PreviousNextFlashInfoManager result = (PreviousNextFlashInfoManager) contextMap.get(CONSTANTS.RequestFlashManager);

        if (null == result && create) {
            result = new PreviousNextFlashInfoManager(guard, flashInnerMap);
            result.initializeBaseCase(this);
            contextMap.put(CONSTANTS.RequestFlashManager, result);

        }
        return result;
    }

    /**
     * <p>
     * Called on the preRestoreView phase if the browser sent us a cookie. If no instance of the FlashManager exists for
     * this request, create it and store it in the contextMap.
     * </p>
     */
    private PreviousNextFlashInfoManager getCurrentFlashManager(FacesContext context, Map<Object, Object> contextMap, Cookie cookie) {
        PreviousNextFlashInfoManager result = (PreviousNextFlashInfoManager) contextMap.get(CONSTANTS.RequestFlashManager);

        if (null == result) {
            result = new PreviousNextFlashInfoManager(guard, flashInnerMap);
            try {
                result.decode(context, this, cookie);
                contextMap.put(CONSTANTS.RequestFlashManager, result);
            } catch (InvalidKeyException ike) {
                contextMap.put(CONSTANTS.ForceSetMaxAgeZero, Boolean.TRUE);
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    result = getCurrentFlashManager(contextMap, true);
                    LOGGER.log(Level.SEVERE, "faces.externalcontext.flash.bad.cookie", new Object[] { ike.getMessage() });
                }

            }

        }
        return result;
    }

    /**
     * <p>
     * On any given request, there are actually two maps behind the flash. Which one is actually used on a given Map method
     * depends on the current lifecycle phase at the time the method is invoked. There is a "next" map, and a "previous"
     * map. This class manages the complexities of dealing with these two maps, and does so by relying on another inner
     * class, FlashInfo.
     * </p>
     *
     * <p>
     * The "next" map is used in only one case, which happens to be a VERY common case: write operations to the flash that
     * happen during render response.
     * </p>
     *
     * <p>
     * The "previous" map is used for write operations that happen before render response, and for all read operations.
     * </p>
     *
     * <p>
     * This class knows how to "decode" its state from an incoming cookie, written by a previous call to "encode".
     * </p>
     *
     * <p>
     * See the docs for FlashInfo for more information.
     * </p>
     */

    private static final class PreviousNextFlashInfoManager {

        private FlashInfo previousRequestFlashInfo;

        private FlashInfo nextRequestFlashInfo;

        private boolean incomingCookieCameFromRedirect = false;

        private Map<String, Map<String, Object>> innerMap;

        private ByteArrayGuardAESCTR guard;

        private PreviousNextFlashInfoManager(ByteArrayGuardAESCTR guard) {
            this.guard = guard;
        }

        private PreviousNextFlashInfoManager(ByteArrayGuardAESCTR guard, Map<String, Map<String, Object>> innerMap) {
            this.guard = guard;
            this.innerMap = innerMap;
        }

        protected PreviousNextFlashInfoManager copyWithoutInnerMap() {
            PreviousNextFlashInfoManager result = new PreviousNextFlashInfoManager(guard);
            result.innerMap = Collections.emptyMap();
            if (null != previousRequestFlashInfo) {
                result.previousRequestFlashInfo = previousRequestFlashInfo.copyWithoutInnerMap();
            }
            if (null != nextRequestFlashInfo) {
                result.nextRequestFlashInfo = nextRequestFlashInfo.copyWithoutInnerMap();
            }
            result.incomingCookieCameFromRedirect = incomingCookieCameFromRedirect;

            return result;
        }

        @Override
        public String toString() {
            String result = null;

            result = "previousRequestSequenceNumber: " + (null != previousRequestFlashInfo ? previousRequestFlashInfo.getSequenceNumber() : "null")
                    + " nextRequestSequenceNumber: " + (null != nextRequestFlashInfo ? nextRequestFlashInfo.getSequenceNumber() : "null");

            return result;
        }

        void initializeBaseCase(ELFlash flash) {
            Map<String, Object> flashMap = null;

            previousRequestFlashInfo = new FlashInfo(flash.getNewSequenceNumber(), LifetimeMarker.FirstTimeThru, false);
            innerMap.put(previousRequestFlashInfo.getSequenceNumber() + "", flashMap = new HashMap<>());
            previousRequestFlashInfo.setFlashMap(flashMap);

            nextRequestFlashInfo = new FlashInfo(flash.getNewSequenceNumber(), LifetimeMarker.FirstTimeThru, false);
            innerMap.put(nextRequestFlashInfo.getSequenceNumber() + "", flashMap = new HashMap<>());
            nextRequestFlashInfo.setFlashMap(flashMap);
        }

        void expirePrevious() {
            // expire previous
            if (null != previousRequestFlashInfo) {
                Map<String, Object> flashMap;
                // clear the old map
                if (null != (flashMap = previousRequestFlashInfo.getFlashMap())) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "{0} expire previous[{1}]",
                                new Object[] { getLogPrefix(FacesContext.getCurrentInstance()), previousRequestFlashInfo.getSequenceNumber() });

                    }
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.getApplication().publishEvent(context, PreClearFlashEvent.class, flashMap);
                    flashMap.clear();
                }
                // remove it from the flash
                innerMap.remove(previousRequestFlashInfo.getSequenceNumber() + "");
                previousRequestFlashInfo = null;
            }
        }

        void expireNext() {
            // expire next
            if (null != nextRequestFlashInfo) {
                Map<String, Object> flashMap;
                // clear the old map
                if (null != (flashMap = nextRequestFlashInfo.getFlashMap())) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "{0} expire next[{1}]",
                                new Object[] { getLogPrefix(FacesContext.getCurrentInstance()), nextRequestFlashInfo.getSequenceNumber() });

                    }
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.getApplication().publishEvent(context, PreClearFlashEvent.class, flashMap);
                    flashMap.clear();
                }
                // remove it from the flash
                innerMap.remove(nextRequestFlashInfo.getSequenceNumber() + "");
                nextRequestFlashInfo = null;
            }
        }

        void expireNext_MovePreviousToNext() {
            if (null != nextRequestFlashInfo) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "{0} expire next[{1}], move previous to next",
                            new Object[] { getLogPrefix(FacesContext.getCurrentInstance()), nextRequestFlashInfo.getSequenceNumber() });

                }
                Map<String, Object> flashMap = nextRequestFlashInfo.getFlashMap();

                FacesContext context = FacesContext.getCurrentInstance();
                context.getApplication().publishEvent(context, PreClearFlashEvent.class, flashMap);

                // clear the old map
                flashMap.clear();
                // remove it from the flash
                innerMap.remove(nextRequestFlashInfo.getSequenceNumber() + "");
                nextRequestFlashInfo = null;
            }

            nextRequestFlashInfo = previousRequestFlashInfo;
            previousRequestFlashInfo = null;
        }

        /**
         * <p>
         * Decode the state of the PreviousNextFlashInfoManager from a Cookie. This entire method is wrapped in a try-catch
         * block to prevent any errors from malformed cookies from polluting the system. When any error occurs, the flash is not
         * usable for this request, and a nice error message is logged.
         * </p>
         *
         * <p>
         * This method is where the LifetimeMarker is incremented, UNLESS the incoming request is the GET after the REDIRECT
         * after POST, in which case we don't increment it because the system will expire the entries in the doLastPhaseActions.
         * </p>
         *
         */

        void decode(FacesContext context, ELFlash flash, Cookie cookie) throws InvalidKeyException {
            String temp;
            String value;

            String urlDecodedValue = null;

            try {
                urlDecodedValue = URLDecoder.decode(cookie.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                urlDecodedValue = cookie.getValue();
            }

            value = guard.decrypt(urlDecodedValue);

            try {
                int i = value.indexOf("_");

                // IMPORTANT: what was "next" when the cookie was
                // encoded is now "previous". Therefore decode "next" first.
                temp = value.substring(0, i++);

                if (0 < temp.length()) {
                    nextRequestFlashInfo = new FlashInfo();
                    nextRequestFlashInfo.decode(temp);
                }
                // invariant we must always have something after the _
                previousRequestFlashInfo = new FlashInfo();
                previousRequestFlashInfo.decode(value.substring(i));

                // handle the consequences of action taken on doLastPhaseActions
                if (previousRequestFlashInfo.isIsRedirect()) {
                    setIncomingCookieCameFromRedirect(true);
                    previousRequestFlashInfo.setIsRedirect(false);
                } else {
                    // Don't make the flash older on debug requests
                    if (!UIDebug.debugRequest(context)) {
                        previousRequestFlashInfo.setLifetimeMarker(LifetimeMarker.SecondTimeThru);
                        expireNext();
                    }
                }
                Map<String, Object> flashMap;
                // If the browser sent a cookie that is valid, but
                // doesn't correspond to a map in memory...
                if (null == (flashMap = innerMap.get(previousRequestFlashInfo.getSequenceNumber() + ""))) {
                    // create a new map
                    previousRequestFlashInfo = new FlashInfo();
                    previousRequestFlashInfo.setSequenceNumber(flash.getNewSequenceNumber());
                    previousRequestFlashInfo.setLifetimeMarker(LifetimeMarker.FirstTimeThru);
                    previousRequestFlashInfo.setIsRedirect(false);
                    // put it in the flash
                    innerMap.put(previousRequestFlashInfo.getSequenceNumber() + "", flashMap = new HashMap<>());
                }
                previousRequestFlashInfo.setFlashMap(flashMap);
                if (null != nextRequestFlashInfo) {
                    if (null == (flashMap = innerMap.get(nextRequestFlashInfo.getSequenceNumber() + ""))) {
                        // create a new map
                        nextRequestFlashInfo = new FlashInfo();
                        nextRequestFlashInfo.setSequenceNumber(flash.getNewSequenceNumber());
                        nextRequestFlashInfo.setLifetimeMarker(LifetimeMarker.FirstTimeThru);
                        nextRequestFlashInfo.setIsRedirect(false);
                        // put it in the flash
                        innerMap.put(nextRequestFlashInfo.getSequenceNumber() + "", flashMap = new HashMap<>());
                    }
                    nextRequestFlashInfo.setFlashMap(flashMap);
                }
            } catch (Throwable t) {
                context.getAttributes().put(CONSTANTS.ForceSetMaxAgeZero, Boolean.TRUE);
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.externalcontext.flash.bad.cookie", new Object[] { value });
                }
            }

        }

        /**
         * <p>
         * Encode the current state of the PreviousNextFlashInfoManager to the cookie.
         * </p>
         */
        Cookie encode() {
            Cookie result = null;

            String value = (null != previousRequestFlashInfo ? previousRequestFlashInfo.encode() : "") + "_"
                    + (null != nextRequestFlashInfo ? nextRequestFlashInfo.encode() : "");
            String encryptedValue = guard.encrypt(value);
            try {
                result = new Cookie(FLASH_COOKIE_NAME, URLEncoder.encode(encryptedValue, "UTF-8"));
            } catch (UnsupportedEncodingException uee) {
                result = new Cookie(FLASH_COOKIE_NAME, encryptedValue);
            }

            if (1 == value.length()) {
                result.setMaxAge(0);
            }
            String requestContextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
            if (requestContextPath.isEmpty()) {
                requestContextPath = "/";
            }
            result.setPath(requestContextPath);
            return result;
        }

        FlashInfo getPreviousRequestFlashInfo() {
            return previousRequestFlashInfo;
        }

        void setPreviousRequestFlashInfo(FlashInfo thisRequestFlashInfo) {
            previousRequestFlashInfo = thisRequestFlashInfo;
        }

        FlashInfo getNextRequestFlashInfo() {
            return nextRequestFlashInfo;
        }

        FlashInfo getNextRequestFlashInfo(ELFlash flash, boolean create) {
            if (create && null == nextRequestFlashInfo) {
                nextRequestFlashInfo = new FlashInfo();
                nextRequestFlashInfo.setSequenceNumber(flash.getNewSequenceNumber());
                nextRequestFlashInfo.setLifetimeMarker(LifetimeMarker.FirstTimeThru);
                nextRequestFlashInfo.setIsRedirect(false);
                // put it in the flash
                Map<String, Object> flashMap = null;
                innerMap.put(nextRequestFlashInfo.getSequenceNumber() + "", flashMap = new HashMap<>());
                nextRequestFlashInfo.setFlashMap(flashMap);
            }
            return nextRequestFlashInfo;
        }

        void setNextRequestFlashInfo(FlashInfo nextRequestFlashInfo) {
            this.nextRequestFlashInfo = nextRequestFlashInfo;
        }

        boolean isIncomingCookieCameFromRedirect() {
            return incomingCookieCameFromRedirect;
        }

        void setIncomingCookieCameFromRedirect(boolean incomingCookieCameFromRedirect) {
            this.incomingCookieCameFromRedirect = incomingCookieCameFromRedirect;
        }

    }

    /**
     * <p>
     * Encapsulate one of the two maps that back the flash for the current request.
     * </p>
     */
    private static final class FlashInfo {

        /**
         * <p>
         * Set to true by the Flash when the extContext tells us there is a redirect.
         * </p>
         */
        private boolean isRedirect;

        /**
         * <p>
         * How many times has this map been through the lifecycle?
         * </p>
         */
        private LifetimeMarker lifetimeMarker;

        /**
         * <p>
         * Application Unique key in the innerMap.
         * </p>
         */

        private long sequenceNumber;

        /**
         * <p>
         * The Map that stores the data. This map itself is stored in innerMap under the key given by the value of
         * sequenceNumber.
         * </p>
         */
        private Map<String, Object> flashMap;

        private FlashInfo() {

        }

        FlashInfo(long sequenceNumber, LifetimeMarker lifetimeMarker, boolean isRedirect) {
            setSequenceNumber(sequenceNumber);
            setLifetimeMarker(lifetimeMarker);
            setIsRedirect(isRedirect);
        }

        FlashInfo copyWithoutInnerMap() {
            FlashInfo result = new FlashInfo(sequenceNumber, lifetimeMarker, isRedirect);

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FlashInfo other = (FlashInfo) obj;
            if (isRedirect != other.isRedirect) {
                return false;
            }
            if (lifetimeMarker != other.lifetimeMarker && (lifetimeMarker == null || !lifetimeMarker.equals(other.lifetimeMarker))) {
                return false;
            }
            if (sequenceNumber != other.sequenceNumber) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + (isRedirect ? 1 : 0);
            hash = 71 * hash + (lifetimeMarker != null ? lifetimeMarker.hashCode() : 0);
            hash = 71 * hash + (int) (sequenceNumber ^ sequenceNumber >>> 32);
            return hash;
        }

        void decode(String value) {
            if (null == value || 0 == value.length()) {

                // PENDING(edburns): REMOVE THIS
                return;
            }

            int i = value.indexOf('X');

            // decode the sequence number
            setSequenceNumber(Long.parseLong(value.substring(0, i++)));

            // decode the lifetime marker
            setLifetimeMarker(LifetimeMarker.decode(value.charAt(i++)));

            // decode the redirect flag
            setIsRedirect(LifetimeMarker.IsRedirect == LifetimeMarker.decode(value.charAt(i++)));
        }

        String encode() {
            String value = null;

            // The cookie value is an encoding of the sequence number, the
            // lifetime marker, and the redirect flag
            if (isIsRedirect()) {
                value = Long.toString(getSequenceNumber()) + "X" + getLifetimeMarker().encode() + LifetimeMarker.IsRedirect.encode();
            } else {
                value = Long.toString(getSequenceNumber()) + "X" + getLifetimeMarker().encode() + LifetimeMarker.IsNormal.encode();
            }

            return value;
        }

        boolean isIsRedirect() {
            return isRedirect;
        }

        void setIsRedirect(boolean isRedirect) {
            this.isRedirect = isRedirect;
        }

        long getSequenceNumber() {
            return sequenceNumber;
        }

        void setSequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        LifetimeMarker getLifetimeMarker() {
            return lifetimeMarker;
        }

        void setLifetimeMarker(LifetimeMarker lifetimeMarker) {
            this.lifetimeMarker = lifetimeMarker;
        }

        Map<String, Object> getFlashMap() {
            return flashMap;
        }

        void setFlashMap(Map<String, Object> flashMap) {
            this.flashMap = flashMap;
        }

    }

    // </editor-fold>

}
