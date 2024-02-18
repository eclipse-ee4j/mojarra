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
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.CompressViewState;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.CLIENT_WINDOW_PARAM;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.RENDER_KIT_ID_PARAM;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.VIEW_STATE_PARAM;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.spi.SerializationProvider;
import com.sun.faces.spi.SerializationProviderFactory;
import com.sun.faces.util.ByteArrayGuardAESCTR;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.servlet.http.HttpSession;

/**
 * Common code for the default <code>StateHelper</code> implementations.
 */
public abstract class StateHelper {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * <p>
     * Factory for serialization streams. These are pluggable via the
     * WebConfiguration.WebContextInitParameter#SerializationProviderClass.
     * </p>
     */
    protected SerializationProvider serialProvider;

    /**
     * <p>
     * Access to the context init parameters that configure this application.
     * </p>
     */
    protected WebConfiguration webConfig;

    /**
     * <p>
     * Flag indicating whether or not view state should be compressed to reduce the memory/bandwidth footprint. This option
     * is common to both types of state saving.
     * </p>
     */
    protected boolean compressViewState;

    /**
     * This will be used the by the different <code>StateHelper</code> implementations when writing the start of the state
     * field.
     */
    protected char[] stateFieldStart;

    /**
     * This will be used by the different <code>StateHelper</code> implementations when writing the middle of the state or
     * viewId fields.
     */

    protected char[] fieldMiddle;

    /**
     * This will be used the by the different <code>StateHelper</code> implementations when writing the end of the state or
     * viewId field. This value of this field is determined by the value of the
     * {@link com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter#AutoCompleteOffOnViewState}
     */
    protected char[] fieldEnd;

    // ------------------------------------------------------------ Constructors

    /**
     * Constructs a new <code>StateHelper</code> instance.
     */
    public StateHelper() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        serialProvider = SerializationProviderFactory.createInstance(ctx.getExternalContext());
        webConfig = WebConfiguration.getInstance(ctx.getExternalContext());
        compressViewState = webConfig.isOptionEnabled(CompressViewState);

        if (serialProvider == null) {
            serialProvider = SerializationProviderFactory.createInstance(FacesContext.getCurrentInstance().getExternalContext());
        }
    }

    public static void createAndStoreCryptographicallyStrongTokenInSession(HttpSession session) {
        ByteArrayGuardAESCTR guard = new ByteArrayGuardAESCTR();
        String clearText = String.valueOf(System.currentTimeMillis());
        String result = guard.encrypt(clearText);
        result = URLEncoder.encode(result, UTF_8);
        session.setAttribute(TOKEN_NAME, result);

    }

    private static final String TOKEN_NAME = RIConstants.FACES_PREFIX + "TOKEN";

    public String getCryptographicallyStrongTokenFromSession(FacesContext context) {
        String result = (String) context.getExternalContext().getSessionMap().get(TOKEN_NAME);
        if (null == result) {
            context.getExternalContext().getSession(true);
        }
        result = (String) context.getExternalContext().getSessionMap().get(TOKEN_NAME);

        return result;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * Functionally similar to ResponseStateManager#writeState(FacesContext, Object) with an option to write the state
     * directly to the provided <code>StringBuilder</code> without sending any markup to the client.
     * </p>
     *
     * @see ResponseStateManager#writeState(jakarta.faces.context.FacesContext, java.lang.Object)
     */
    public abstract void writeState(FacesContext ctx, Object state, StringBuilder stateCapture) throws IOException;

    /**
     * @see jakarta.faces.render.ResponseStateManager#getState(jakarta.faces.context.FacesContext, String)
     */
    public abstract Object getState(FacesContext ctx, String viewId) throws IOException;

    /**
     * @see jakarta.faces.render.ResponseStateManager#isStateless(jakarta.faces.context.FacesContext, String)
     */
    public abstract boolean isStateless(FacesContext ctx, String viewId) throws IllegalStateException;

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>
     * Get our view state from this request
     * </p>
     *
     * @param context the <code>FacesContext</code> for the current request
     *
     * @return the view state from this request
     */
    protected static String getStateParamValue(FacesContext context) {
        String pValue = VIEW_STATE_PARAM.getValue(context);
        if (pValue != null && pValue.length() == 0) {
            pValue = null;
        }

        return pValue;
    }

    /**
     * <p>
     * If a custom <code>RenderKit</code> is used, write out the ID of the <code>RenderKit</code> out as a hidden field.
     * This will be used when restoring the view state.
     * </p>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param writer the <code>ResponseWriter</code> to write to
     * @throws IOException if an error occurs writing to the client
     */
    protected void writeRenderKitIdField(FacesContext context, ResponseWriter writer) throws IOException {
        String result = context.getViewRoot().getRenderKitId();
        String defaultRkit = context.getApplication().getDefaultRenderKitId();
        if (defaultRkit == null) {
            defaultRkit = RenderKitFactory.HTML_BASIC_RENDER_KIT;
        }

        if (result != null && !defaultRkit.equals(result)) {
            writer.startElement("input", context.getViewRoot());
            writer.writeAttribute("type", "hidden", "type");
            writer.writeAttribute("name", RENDER_KIT_ID_PARAM.getName(context), "name");
            writer.writeAttribute("value", result, "value");
            writer.endElement("input");
        }

    }

    /**
     * Write the client window state field.
     *
     * @param context the Faces context.
     * @param writer the response writer.
     * @throws IOException when an I/O error occurs.
     */
    protected void writeClientWindowField(FacesContext context, ResponseWriter writer) throws IOException {
        ClientWindow window = context.getExternalContext().getClientWindow();
        if (window != null) {
            writer.startElement("input", null);
            writer.writeAttribute("type", "hidden", null);
            writer.writeAttribute("name", CLIENT_WINDOW_PARAM.getName(context), null);
            writer.writeAttribute("id", Util.getClientWindowId(context), null);
            writer.writeAttribute("value", window.getId(), null);
            if (webConfig.isOptionEnabled(AutoCompleteOffOnViewState)) {
                writer.writeAttribute("autocomplete", "off", null);
            }
            writer.endElement("input");
        }
    }
}
