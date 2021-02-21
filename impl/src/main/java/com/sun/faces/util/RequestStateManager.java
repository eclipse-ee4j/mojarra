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

package com.sun.faces.util;

import java.util.HashMap;
import java.util.Map;

import com.sun.faces.RIConstants;

import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;

/**
 * <p>
 * This helper class is used a central location for per-request state that is needed by Mojarra. This class leverages
 * FacesContext.getAttributes() which as added in 2.0 instead of the request scope to prevent the unecessary triggering
 * of ServletRequestAttributeListeners.
 * </p>
 */
public class RequestStateManager {

    /**
     * Attribute for storing any content within a page that is defined after the closing f:view.
     */
    public static final String AFTER_VIEW_CONTENT = RIConstants.FACES_PREFIX + "AFTER_VIEW_CONTENT";

    /**
     * Attribute describing the current ELResolver chain type
     */
    public static final String EL_RESOLVER_CHAIN_TYPE_NAME = RIConstants.FACES_PREFIX + "ELResolverChainType";

    /**
     * Attribute indicating the current component being processed. This will be used when generating bytecode for custom
     * converters.
     */
    public static final String TARGET_COMPONENT_ATTRIBUTE_NAME = RIConstants.FACES_PREFIX + "ComponentForValue";

    /**
     * Attribute defining the {@link jakarta.faces.render.RenderKit} being used for this request.
     */
    public static final String RENDER_KIT_IMPL_REQ = RIConstants.FACES_PREFIX + "renderKitImplForRequest";

    /**
     * This attribute is used by the StateMangaer during restore view. The values are stored in the request for later use.
     */
    public static final String LOGICAL_VIEW_MAP = RIConstants.FACES_PREFIX + "logicalViewMap";

    /**
     * This attribute is used by the StateMangaer during restore view. The values are stored in the request for later use.
     */
    public static final String ACTUAL_VIEW_MAP = RIConstants.FACES_PREFIX + "actualViewMap";

    /**
     * This attribute is used by the loadBundle tag for tracking views/subviews within the logical view (this is only used
     * when 1.1 compatibility is enabled).
     */
    public static final String VIEWTAG_STACK_ATTR_NAME = RIConstants.FACES_PREFIX + "taglib.faces_core.VIEWTAG_STACK";

    /**
     * Attribute to store the {@link jakarta.faces.webapp.FacesServlet} path of the original request.
     */
    public static final String INVOCATION_PATH = RIConstants.FACES_PREFIX + "INVOCATION_PATH";

    /**
     * This attribute protects against infinite loops on expressions that touch a custom legacy VariableResolver that
     * delegates to its parent VariableResolver.
     */
    public static final String REENTRANT_GUARD = RIConstants.FACES_PREFIX + "LegacyVariableResolver";

    /**
     * Leveraged by the RequestStateManager to allow deprecated ResponseStateManager methods to continue to work if called.
     */
    public static final String FACES_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";

    /**
     * Leveraged by ResourceHandlerImpl to denote whether or not a request is a resource request. A <code>Boolean</code>
     * value will be assoicated with this key.
     */
    public static final String RESOURCE_REQUEST = "com.sun.faces.RESOURCE_REQUEST";

    /**
     * Used to store the FaceletFactory as other components may need to use it during their processing.
     */
    public static final String FACELET_FACTORY = "com.sun.faces.FACELET_FACTORY";

    /**
     * Used to indicate whether or not JSF script has already been installed.
     */
    public static final String SCRIPT_STATE = "com.sun.faces.SCRIPT_STATE";

    /**
     * Used to communicate which validators have been disabled for a particular nesting level within a view.
     */
    public static final String DISABLED_VALIDATORS = "com.sun.faces.DISABLED_VALIDATORS";

    /**
     * Used to store the Set of ResourceDependency annotations that have been processed.
     */
    public static final String PROCESSED_RESOURCE_DEPENDENCIES = "com.sun.faces.PROCESSED_RESOURCE_DEPENDENCIES";

    /**
     * Used to store the Set of ResourceDependency annotations that have been processed.
     */
    public static final String PROCESSED_RADIO_BUTTON_GROUPS = "com.sun.faces.PROCESSED_RADIO_BUTTON_GROUPS";

    /**
     * Used to store the Set of resource dependencies that have been rendered.
     */
    public static final String RENDERED_RESOURCE_DEPENDENCIES = ResourceHandler.RESOURCE_IDENTIFIER;

    // TODO: refactor this thing to common map.
    private static final String[] ATTRIBUTES_TO_CLEAR_ON_CHANGE_OF_VIEW = { SCRIPT_STATE, PROCESSED_RESOURCE_DEPENDENCIES, PROCESSED_RADIO_BUTTON_GROUPS };

    /**
     * <p>
     * The key under with the Map containing the implementation specific attributes will be stored within the request.
     * <p>
     */
    private static final String KEY = RequestStateManager.class.getName();

    // ---------------------------------------------------------- Public Methods

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @param key the key for the value
     * @return the value associated with the specified key.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(FacesContext ctx, String key) {

        if (ctx == null || key == null) {
            return null;
        }
        return (T) ctx.getAttributes().get(key);

    }

    /**
     * <p>
     * Adds the specified key and value to the Map stored in the request. If <code>value</code> is <code>null</code>, that
     * key/value pair will be removed from the Map.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for the current request
     * @param key the key for the value
     * @param value the value to store
     */
    public static void set(FacesContext ctx, String key, Object value) {

        if (ctx == null || key == null) {
            return;
        }
        if (value == null) {
            remove(ctx, key);
        }
        ctx.getAttributes().put(key, value);

    }

    /**
     * <p>
     * Remove the value associated with the specified key.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for the current request
     * @param key the key for the value
     * @return the value previous associated with the specified key, if any
     */
    public static Object remove(FacesContext ctx, String key) {

        if (ctx == null || key == null) {
            return null;
        }

        return ctx.getAttributes().remove(key);

    }

    /**
     * <p>
     * Remove all request state attributes associated that need to be cleared on change of view.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for the current request
     */
    public static void clearAttributesOnChangeOfView(FacesContext ctx) {

        if (ctx == null) {
            return;
        }

        Map<Object, Object> attrs = ctx.getAttributes();
        for (String key : ATTRIBUTES_TO_CLEAR_ON_CHANGE_OF_VIEW) {
            attrs.remove(key);
        }

        PartialViewContext pvc = ctx.getPartialViewContext();

        if (!pvc.isAjaxRequest() || pvc.isRenderAll()) {
            attrs.remove(RENDERED_RESOURCE_DEPENDENCIES);
        }
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @param key the key for the value
     * @return true if the specified key exists in the Map
     */
    public static boolean containsKey(FacesContext ctx, String key) {

        return !(ctx == null || key == null) && ctx.getAttributes().containsKey(key);

    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return the Map from the request containing the implementation specific attributes needed for processing
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getStateMap(FacesContext ctx) {

        assert ctx != null; // all callers guard against a null context
        Map<Object, Object> contextMap = ctx.getAttributes();
        Map<String, Object> reqState = (Map<String, Object>) contextMap.get(KEY);
        if (reqState == null) {
            reqState = new HashMap<>();
            contextMap.put(KEY, reqState);
        }
        return reqState;

    }

}
