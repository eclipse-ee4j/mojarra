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

package com.sun.faces.renderkit;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.AutoCompleteOffOnViewState;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableViewStateIdRendering;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.GenerateUniqueServerStateIds;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.NumberOfLogicalViews;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.NumberOfViews;
import static com.sun.faces.context.SessionMap.getMutex;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.VIEW_STATE_PARAM;
import static com.sun.faces.util.Util.notNull;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.LRUMap;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.TypedCollections;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * This <code>StateHelper</code> provides the functionality associated with server-side state saving, though in
 * actuality, it is a hybrid between client and server.
 */
public class ServerSideStateHelper extends StateHelper {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * Key to store the <code>AtomicInteger</code> used to generate unique state map keys.
     */
    public static final String STATEMANAGED_SERIAL_ID_KEY = ServerSideStateHelper.class.getName() + ".SerialId";

    /**
     * The top level attribute name for storing the state structures within the session.
     */
    public static final String LOGICAL_VIEW_MAP = ServerSideStateHelper.class.getName() + ".LogicalViewMap";

    /**
     * The number of logical views as configured by the user.
     */
    protected final Integer numberOfLogicalViews;

    /**
     * The number of views as configured by the user.
     */
    protected final Integer numberOfViews;

    /**
     * Flag determining how server state IDs are generated.
     */
    protected boolean generateUniqueStateIds;

    /**
     * Used to generate unique server state IDs.
     */
    protected final SecureRandom random;

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new <code>ServerSideStateHelper</code> instance.
     */
    public ServerSideStateHelper() {
        numberOfLogicalViews = getIntegerConfigValue(NumberOfLogicalViews);
        numberOfViews = getIntegerConfigValue(NumberOfViews);
        WebConfiguration webConfig = WebConfiguration.getInstance();
        generateUniqueStateIds = webConfig.isOptionEnabled(GenerateUniqueServerStateIds);
        if (generateUniqueStateIds) {
            // Construct secure RNG.
            random = new SecureRandom();

            // Make sure SecureRandom will seed itself safely by generating a random byte. This assures that an
            // accidental invocation of setSeed will not break security.
            random.nextBytes(new byte[1]);
        } else {
            random = null;
        }

    }

    // ------------------------------------------------ Methods from StateHelper

    /**
     * <p>
     * Stores the provided state within the session obtained from the provided <code>FacesContext</code>
     * </p>
     *
     * <p>
     * If <code>stateCapture</code> is <code>null</code>, the composite key used to look up the actual and logical views
     * will be written to the client as a hidden field using the <code>ResponseWriter</code> from the provided
     * <code>FacesContext</code>.
     * </p>
     *
     * <p>
     * If <code>stateCapture</code> is not <code>null</code>, the composite key will be appended to the
     * <code>StringBuilder</code> without any markup included or any content written to the client.
     */
    @Override
    public void writeState(FacesContext ctx, Object state, StringBuilder stateCapture) throws IOException {
        notNull("context", ctx);

        String id;

        UIViewRoot viewRoot = ctx.getViewRoot();

        if (!viewRoot.isTransient()) {
            if (!ctx.getAttributes().containsKey("com.sun.faces.ViewStateValue")) {
                notNull("state", state);
                Object[] stateToWrite = (Object[]) state;
                ExternalContext externalContext = ctx.getExternalContext();
                Object sessionObj = externalContext.getSession(true);
                Map<String, Object> sessionMap = externalContext.getSessionMap();

                synchronized (getMutex(sessionObj)) {
                    Map<String, Map> logicalMap = TypedCollections.dynamicallyCastMap((Map) sessionMap.get(LOGICAL_VIEW_MAP), String.class, Map.class);
                    if (logicalMap == null) {
                        logicalMap = Collections.synchronizedMap(new LRUMap<String, Map>(numberOfLogicalViews));
                        sessionMap.put(LOGICAL_VIEW_MAP, logicalMap);
                    }

                    Object structure = stateToWrite[0];
                    Object savedState = handleSaveState(stateToWrite[1]);

                    String idInLogicalMap = (String) RequestStateManager.get(ctx, RequestStateManager.LOGICAL_VIEW_MAP);
                    if (idInLogicalMap == null) {
                        idInLogicalMap = generateUniqueStateIds ? createRandomId() : createIncrementalRequestId(ctx);
                    }
                    String idInActualMap = null;
                    if (ctx.getPartialViewContext().isPartialRequest()) {
                        // If partial request, do not change actual view Id, because page not actually changed.
                        // Otherwise partial requests will soon overflow cache with values that would be never used.
                        idInActualMap = (String) RequestStateManager.get(ctx, RequestStateManager.ACTUAL_VIEW_MAP);
                    }
                    if (null == idInActualMap) {
                        idInActualMap = generateUniqueStateIds ? createRandomId() : createIncrementalRequestId(ctx);
                    }
                    Map<String, Object[]> actualMap = TypedCollections.dynamicallyCastMap(logicalMap.get(idInLogicalMap), String.class, Object[].class);
                    if (actualMap == null) {
                        actualMap = new LRUMap<>(numberOfViews);
                        logicalMap.put(idInLogicalMap, actualMap);
                    }

                    id = idInLogicalMap + ':' + idInActualMap;

                    Object[] stateArray = actualMap.get(idInActualMap);
                    // reuse the array if possible
                    if (stateArray != null) {
                        stateArray[0] = structure;
                        stateArray[1] = savedState;
                    } else {
                        actualMap.put(idInActualMap, new Object[] { structure, savedState });
                    }

                    // always call put/setAttribute as we may be in a clustered environment.
                    sessionMap.put(LOGICAL_VIEW_MAP, logicalMap);
                    ctx.getAttributes().put("com.sun.faces.ViewStateValue", id);
                }
            } else {
                id = (String) ctx.getAttributes().get("com.sun.faces.ViewStateValue");
            }
        } else {
            id = "stateless";
        }

        if (stateCapture != null) {
            stateCapture.append(id);
        } else {
            ResponseWriter writer = ctx.getResponseWriter();

            writer.startElement("input", null);
            writer.writeAttribute("type", "hidden", null);
            writer.writeAttribute("name", VIEW_STATE_PARAM.getName(ctx), null);
            if (webConfig.isOptionEnabled(EnableViewStateIdRendering)) {
                String viewStateId = Util.getViewStateId(ctx);
                writer.writeAttribute("id", viewStateId, null);
            }
            writer.writeAttribute("value", id, null);
            if (webConfig.isOptionEnabled(AutoCompleteOffOnViewState)) {
                writer.writeAttribute("autocomplete", "off", null);
            }
            writer.endElement("input");

            writeClientWindowField(ctx, writer);
            writeRenderKitIdField(ctx, writer);
        }
    }

    /**
     * <p>
     * Inspects the incoming request parameters for the standardized state parameter name. In this case, the parameter value
     * will be the composite ID generated by ServerSideStateHelper#writeState(FacesContext, Object, StringBuilder).
     * </p>
     *
     * <p>
     * The composite key will be used to find the appropriate view within the session obtained from the provided
     * <code>FacesContext</code>
     */
    @Override
    public Object getState(FacesContext ctx, String viewId) {
        String compoundId = getStateParamValue(ctx);

        if (compoundId == null) {
            return null;
        }

        if ("stateless".equals(compoundId)) {
            return "stateless";
        }

        int sep = compoundId.indexOf(':');
        assert sep != -1;
        assert sep < compoundId.length();

        String idInLogicalMap = compoundId.substring(0, sep);
        String idInActualMap = compoundId.substring(sep + 1);

        ExternalContext externalCtx = ctx.getExternalContext();
        Object sessionObj = externalCtx.getSession(false);

        // stop evaluating if the session is not available
        if (sessionObj == null) {
            LOGGER.log(FINE, "Unable to restore server side state for view ID {0} as no session is available", viewId);
            return null;
        }

        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (getMutex(sessionObj)) {
            Map logicalMap = (Map) externalCtx.getSessionMap().get(LOGICAL_VIEW_MAP);
            if (logicalMap != null) {
                Map actualMap = (Map) logicalMap.get(idInLogicalMap);
                if (actualMap != null) {
                    RequestStateManager.set(ctx, RequestStateManager.LOGICAL_VIEW_MAP, idInLogicalMap);

                    Object[] restoredState = new Object[2];
                    Object[] state = (Object[]) actualMap.get(idInActualMap);
                    if (state != null) {
                        restoredState[0] = state[0];
                        restoredState[1] = state[1];

                        RequestStateManager.set(ctx, RequestStateManager.ACTUAL_VIEW_MAP, idInActualMap);
                        if (state.length == 2 && state[1] != null) {
                            restoredState[1] = handleRestoreState(state[1]);
                        }
                    }

                    return restoredState;
                }
            }
        }

        return null;

    }

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>
     * Utility method for obtaining the <code>Integer</code> based configuration values used to change the behavior of the
     * <code>ServerSideStateHelper</code>.
     *
     * @param param the paramter to parse
     * @return the Integer representation of the parameter value
     */
    protected Integer getIntegerConfigValue(WebContextInitParameter param) {
        String noOfViewsStr = webConfig.getOptionValue(param);
        Integer value = null;
        try {
            value = Integer.valueOf(noOfViewsStr);
        } catch (NumberFormatException nfe) {
            String defaultValue = param.getDefaultValue();
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "faces.state.server.cannot.parse.int.option", new Object[] { param.getQualifiedName(), defaultValue });
            }
            try {
                value = Integer.valueOf(defaultValue);
            } catch (NumberFormatException ne) {
                LOGGER.log(FINEST, "Unable to convert number", ne);
            }
        }

        return value;
    }

    /**
     * @param state the object returned from <code>UIView.processSaveState</code>
     * @return If option <code>SerializeServerState</code> is <code>true</code>, serialize and return the state, otherwise, return <code>state</code> unchanged.
     */
    protected Object handleSaveState(Object state) {
        if (!ContextParam.SERIALIZE_SERVER_STATE.isSet(FacesContext.getCurrentInstance())) {
            return state;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oas = null;
        try {
            oas = serialProvider.createObjectOutputStream(compressViewState ? new GZIPOutputStream(baos, 1024) : baos);
            oas.writeObject(state);
            oas.flush();
        } catch (Exception e) {
            throw new FacesException(e);
        } finally {
            if (oas != null) {
                try {
                    oas.close();
                } catch (IOException ioe) {
                    LOGGER.log(FINEST, "Closing stream", ioe);
                }
            }
        }

        return baos.toByteArray();
    }

    /**
     * @param state the state as it was stored in the session
     * @return an object that can be passed to <code>UIViewRoot.processRestoreState</code>. If option <code>SerializeServerState</code> true
     * de-serialize the state prior to returning it, otherwise return <code>state</code> as is.
     */
    protected Object handleRestoreState(Object state) {
        if (!ContextParam.SERIALIZE_SERVER_STATE.isSet(FacesContext.getCurrentInstance())) {
            return state;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) state);
            ObjectInputStream ois = serialProvider.createObjectInputStream(compressViewState ? new GZIPInputStream(bais, 1024) : bais);) {
            return ois.readObject();
        } catch (Exception e) {
            throw new FacesException(e);
        }
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return a unique ID for building the keys used to store views within a session
     */
    private String createIncrementalRequestId(FacesContext ctx) {
        Map<String, Object> sessionMap = ctx.getExternalContext().getSessionMap();
        AtomicInteger idgen = (AtomicInteger) sessionMap.get(STATEMANAGED_SERIAL_ID_KEY);
        if (idgen == null) {
            idgen = new AtomicInteger(1);
        }

        // always call put/setAttribute as we may be in a clustered environment.
        sessionMap.put(STATEMANAGED_SERIAL_ID_KEY, idgen);
        return UIViewRoot.UNIQUE_ID_PREFIX + idgen.getAndIncrement();

    }

    private String createRandomId() {
        return Long.valueOf(random.nextLong()).toString();
    }

    /**
     * Is stateless.
     *
     * @param facesContext the Faces context.
     * @param viewId the view id.
     * @return true if stateless, false otherwise.
     * @throws IllegalStateException when the request was not a postback.
     */
    @Override
    public boolean isStateless(FacesContext facesContext, String viewId) throws IllegalStateException {
        if (!facesContext.isPostback()) {
            throw new IllegalStateException("Cannot determine whether or not the request is stateless");
        }

        String compoundId = getStateParamValue(facesContext);
        return compoundId != null && "stateless".equals(compoundId);
    }
}
