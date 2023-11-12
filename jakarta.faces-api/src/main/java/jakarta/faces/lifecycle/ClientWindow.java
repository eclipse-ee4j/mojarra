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

package jakarta.faces.lifecycle;

import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ResponseStateManager;

/**
 * <p class="changed_added_2_2">
 * This class represents a client window, which may be a browser tab, browser window, browser pop-up, portlet, or
 * anything else that can display a {@link jakarta.faces.component.UIComponent} hierarchy rooted at a
 * {@link jakarta.faces.component.UIViewRoot}.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * Modes of Operation
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * none mode
 * </p>
 *
 * <p>
 * The generation of <code>ClientWindow</code> is controlled by the value of the <code>context-param</code> named by the
 * value of {@link #CLIENT_WINDOW_MODE_PARAM_NAME}. If this <code>context-param</code> is not specified, or its value is
 * "none", no <code>ClientWindow</code> instances will be generated, and the entire feature is effectively disabled for
 * the entire application.
 * </p>
 *
 * <p>
 * Other modes
 * </p>
 *
 * <p>
 * To accomadate the widest possible range of implementation choices to support this feature, explicit names for modes
 * other than "none" and "url" are not specified. However, for all values of {@link #CLIENT_WINDOW_MODE_PARAM_NAME}, the
 * lifetime of a <code>ClientWindow</code> starts on the first request made by a particular client window (or tab, or
 * pop-up, etc) to the Jakarta Faces runtime and persists as long as that window remains open or the session
 * expires, whichever comes first. A client window is always associated with exactly one <code>UIViewRoot</code>
 * instance at a time, but may display many different <code>UIViewRoot</code>s during its lifetime.
 * </p>
 *
 * <p>
 * The <code>ClientWindow</code> instance is associated with the incoming request during the
 * {@link Lifecycle#attachWindow} method. This method will cause a new instance of <code>ClientWindow</code> to be
 * created, assigned an id, and passed to {@link jakarta.faces.context.ExternalContext#setClientWindow}.
 * </p>
 *
 * <p>
 * During state saving, regardless of the window id mode, or state saving mode, for ajax and non-ajax requests, a hidden
 * field must be written whose name, id and value are given as specified in
 * {@link jakarta.faces.render.ResponseStateManager#CLIENT_WINDOW_PARAM}.
 * </p>
 *
 * <p>
 * In addition to the hidden field already described. The runtime must ensure that any component that renders a
 * hyperlink that causes the user agent to send a GET request to the Faces server when it is clicked has a query
 * parameter with a name and value specified in {@link ResponseStateManager#CLIENT_WINDOW_URL_PARAM}. This requirement
 * is met by several of the "encode" methods on {@link jakarta.faces.context.ExternalContext}. See
 * {@link jakarta.faces.context.ExternalContext#encodeActionURL(java.lang.String) } for details.
 * </p>
 *
 * </blockquote>
 *
 * </div>
 *
 * @since 2.2
 *
 */

public abstract class ClientWindow {

    /**
     * <p class="changed_added_2_2">
     * The context-param that controls the operation of the <code>ClientWindow</code> feature. The runtime must support the
     * values "none" and "url", without the quotes, but other values are possible. If not specified, or the value is not
     * understood by the implementation, {@value #CLIENT_WINDOW_MODE_DEFAULT_VALUE} is assumed.
     * </p>
     *
     * @since 2.2
     */
    public static final String CLIENT_WINDOW_MODE_PARAM_NAME = "jakarta.faces.CLIENT_WINDOW_MODE";

    /**
     * <p class="changed_added_5_0">
     * The default value of the {@link #CLIENT_WINDOW_MODE_PARAM_NAME} context-param.
     * </p>
     *
     * @since 5.0
     */
    public static final String CLIENT_WINDOW_MODE_DEFAULT_VALUE = "none";

    /**
     * <p class="changed_added_4_0">
     * Indicate the max number of ClientWindows, which is used by {@link ClientWindowScoped}.
     * <span class="changed_added_5_0">By default the value is {@link #NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE}.</span>
     * It is only active when jakarta.faces.CLIENT_WINDOW_MODE is enabled.
     * </p>
     *
     * @since 4.0
     */
    public static final String NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME = "jakarta.faces.NUMBER_OF_CLIENT_WINDOWS";

    /**
     * <p class="changed_added_5_0">
     * The default value of the {@link #NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME} context-param.
     * </p>
     *
     * @since 5.0
     */
    public static final int NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE = 10;

    /**
     * <p class="changed_added_2_2">
     * This method will be called whenever a URL is generated by the runtime where client window related parameters need to
     * be inserted into the URL. This guarantees custom {@code ClientWindow} implementations that they will have the
     * opportunity to insert any additional client window specific information in any case where a URL is generated, such as
     * the rendering of hyperlinks. The returned map must be immutable. The default implementation of this method returns
     * the empty map.
     * </p>
     *
     *
     * @since 2.2
     * @param context the {@code FacesContext} for this request.
     * @return {@code null} or a map of parameters to insert into the URL query string.
     */

    public abstract Map<String, String> getQueryURLParameters(FacesContext context);

    /**
     * <p class="changed_added_2_2">
     * Return a String value that uniquely identifies this <code>ClientWindow</code> within the scope of the current
     * session. See {@link #decode} for the specification of how to derive this value.
     * </p>
     *
     * @since 2.2
     *
     * @return the id of the {@code ClientWindow}
     */

    public abstract String getId();

    /**
     * <p class="changed_added_2_2">
     * The implementation is responsible for examining the incoming request and extracting the value that must be returned
     * from the {@link #getId} method. If {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "none" this method must not be invoked.
     * If {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" the implementation must first look for a request parameter under
     * the name given by the value of {@link jakarta.faces.render.ResponseStateManager#CLIENT_WINDOW_PARAM}. If no value is
     * found, look for a request parameter under the name given by the value of
     * {@link jakarta.faces.render.ResponseStateManager#CLIENT_WINDOW_URL_PARAM}. If no value is found, fabricate an id that
     * uniquely identifies this <code>ClientWindow</code> within the scope of the current session. This value must be made
     * available to return from the {@link #getId} method. The value must be suitable for inclusion as a hidden field or
     * query parameter. If a value is found, decrypt it using the key from the session and make it available for return from
     * {@link #getId}.
     * </p>
     *
     * @param context the {@link FacesContext} for this request.
     *
     * @since 2.2
     */

    public abstract void decode(FacesContext context);

    private static final String PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY = "jakarta.faces.lifecycle.ClientWindowRenderModeEnablement";

    /**
     * <p class="changed_added_2_2">
     * Components that permit per-use disabling of the appending of the ClientWindow in generated URLs must call this method
     * first before rendering those URLs. The caller must call
     * {@link #enableClientWindowRenderMode(jakarta.faces.context.FacesContext)} from a <code>finally</code> block after
     * rendering the URL. If {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" without the quotes, all generated URLs that
     * cause a GET request must append the ClientWindow by default. This is specified as a static method because callsites
     * need to access it without having access to an actual {@code ClientWindow} instance.
     * </p>
     *
     * @param context the {@link FacesContext} for this request.
     *
     * @since 2.2
     */

    public void disableClientWindowRenderMode(FacesContext context) {
        Map<Object, Object> attrMap = context.getAttributes();
        attrMap.put(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY, Boolean.TRUE);
    }

    /**
     * <p class="changed_added_2_2">
     * Components that permit per-use disabling of the appending of the ClientWindow in generated URLs must call this method
     * first after rendering those URLs. If {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" without the quotes, all
     * generated URLs that cause a GET request must append the ClientWindow by default. This is specified as a static method
     * because callsites need to access it without having access to an actual {@code ClientWindow} instance.
     * </p>
     *
     * @param context the {@link FacesContext} for this request.
     *
     * @since 2.2
     */

    public void enableClientWindowRenderMode(FacesContext context) {
        Map<Object, Object> attrMap = context.getAttributes();
        attrMap.remove(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY);

    }

    /**
     * <p class="changed_added_2_2">
     * Methods that append the ClientWindow to generated URLs must call this method to see if they are permitted to do so.
     * If {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" without the quotes, all generated URLs that cause a GET request
     * must append the ClientWindow by default. This is specified as a static method because callsites need to access it
     * without having access to an actual {@code ClientWindow} instance.
     * </p>
     *
     * @param context the {@link FacesContext} for this request.
     *
     * @return the result as specified above
     *
     * @since 2.2
     */

    public boolean isClientWindowRenderModeEnabled(FacesContext context) {
        boolean result = false;
        Map<Object, Object> attrMap = context.getAttributes();
        result = !attrMap.containsKey(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY);
        return result;
    }

}
