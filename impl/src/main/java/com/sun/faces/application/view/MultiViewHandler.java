/*
 * Copyright (c) 2022, 2023 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application.view;

import static com.sun.faces.RIConstants.SAVESTATE_FIELD_MARKER;
import static com.sun.faces.renderkit.RenderKitUtils.getResponseStateManager;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.RENDER_KIT_ID_PARAM;
import static com.sun.faces.util.MessageUtils.ILLEGAL_VIEW_ID_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.getFacesMapping;
import static com.sun.faces.util.Util.getFirstWildCardMappingToFacesServlet;
import static com.sun.faces.util.Util.getViewHandler;
import static com.sun.faces.util.Util.isViewIdExactMappedToFacesServlet;
import static com.sun.faces.util.Util.notNull;
import static jakarta.faces.FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY;
import static jakarta.faces.push.PushContext.URI_PREFIX;
import static jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT;
import static jakarta.faces.render.ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM;
import static jakarta.servlet.http.MappingMatch.EXACT;
import static jakarta.servlet.http.MappingMatch.EXTENSION;
import static jakarta.servlet.http.MappingMatch.PATH;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewVisitOption;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;
import jakarta.faces.view.ViewMetadata;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This {@link ViewHandler} implementation handles the Facelets VDL-based views.
 */
public class MultiViewHandler extends ViewHandler {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private final List<String> configuredExtensions;
    private final Set<String> protectedViews;

    private final ViewDeclarationLanguageFactory vdlFactory;

    // ------------------------------------------------------------ Constructors

    public MultiViewHandler() {
        String faceletsSuffix = ContextParam.FACELETS_SUFFIX.getValue(FacesContext.getCurrentInstance());
        configuredExtensions = asList(faceletsSuffix);
        vdlFactory = (ViewDeclarationLanguageFactory) FactoryFinder.getFactory(VIEW_DECLARATION_LANGUAGE_FACTORY);
        protectedViews = new CopyOnWriteArraySet<>();
    }

    // ------------------------------------------------ Methods from ViewHandler

    /**
     * Call the default implementation of
     * {@link jakarta.faces.application.ViewHandler#initView(jakarta.faces.context.FacesContext)}
     *
     * @see jakarta.faces.application.ViewHandler#initView(jakarta.faces.context.FacesContext)
     */
    @Override
    public void initView(FacesContext context) throws FacesException {
        super.initView(context);
    }

    /**
     * <p>
     * Call {@link ViewDeclarationLanguage#restoreView(jakarta.faces.context.FacesContext, String)}.
     * </p>
     *
     * @see ViewHandler#restoreView(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        notNull("context", context);

        String physicalViewId = derivePhysicalViewId(context, viewId, false);

        return vdlFactory.getViewDeclarationLanguage(physicalViewId).restoreView(context, physicalViewId);
    }

    /**
     * <p>
     * Derive the physical view ID (i.e. the physical resource) and call call
     * {@link ViewDeclarationLanguage#createView(jakarta.faces.context.FacesContext, String)}.
     * </p>
     *
     * @see ViewHandler#restoreView(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        notNull("context", context);

        String physicalViewId = derivePhysicalViewId(context, viewId, false);

        return vdlFactory.getViewDeclarationLanguage(physicalViewId).createView(context, physicalViewId);
    }

    /**
     * <p>
     * Call
     * {@link ViewDeclarationLanguage#renderView(jakarta.faces.context.FacesContext, jakarta.faces.component.UIViewRoot)} if
     * the view can be rendered.
     * </p>
     *
     * @see ViewHandler#renderView(jakarta.faces.context.FacesContext, jakarta.faces.component.UIViewRoot)
     */
    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
        notNull("context", context);
        notNull("viewToRender", viewToRender);

        vdlFactory.getViewDeclarationLanguage(viewToRender.getViewId()).renderView(context, viewToRender);
    }

    /**
     * <p>
     * This code is currently common to all {@link ViewHandlingStrategy} instances.
     * </p>
     *
     * @see ViewHandler#calculateLocale(jakarta.faces.context.FacesContext)
     */
    @Override
    public Locale calculateLocale(FacesContext context) {
        notNull("context", context);

        Locale result = null;

        // Determine the locales that are acceptable to the client based on the
        // Accept-Language header and the find the best match among the
        // supported locales specified by the client.
        Iterator<Locale> locales = context.getExternalContext().getRequestLocales();
        while (locales.hasNext()) {
            Locale perf = locales.next();
            result = findMatch(context, perf);
            if (result != null) {
                break;
            }
        }

        // no match is found.
        if (result == null) {
            if (context.getApplication().getDefaultLocale() == null) {
                result = Locale.getDefault();
            } else {
                result = context.getApplication().getDefaultLocale();
            }
        }

        return result;
    }

    /**
     * <p>
     * This code is currently common to all {@link ViewHandlingStrategy} instances.
     * </p>
     *
     * @see ViewHandler#calculateRenderKitId(jakarta.faces.context.FacesContext)
     */
    @Override
    public String calculateRenderKitId(FacesContext context) {
        notNull("context", context);

        String result = RENDER_KIT_ID_PARAM.getValue(context);

        if (result == null) {
            if (null == (result = context.getApplication().getDefaultRenderKitId())) {
                result = HTML_BASIC_RENDER_KIT;
            }
        }

        return result;
    }

    /**
     * <p>
     * This code is currently common to all {@link ViewHandlingStrategy} instances.
     * </p>
     *
     * @see ViewHandler#writeState(jakarta.faces.context.FacesContext)
     */
    @Override
    public void writeState(FacesContext context) throws IOException {
        notNull("context", context);

        if (!context.getPartialViewContext().isAjaxRequest()) {
            LOGGER.fine(() -> "Begin writing marker for viewId " + context.getViewRoot().getViewId());

            WriteBehindStateWriter writer = WriteBehindStateWriter.getCurrentInstance();
            if (writer != null) {
                writer.writingState();
            }

            context.getResponseWriter().write(SAVESTATE_FIELD_MARKER);

            LOGGER.fine(() -> "End writing marker for viewId " + context.getViewRoot().getViewId());
        }
    }

    /**
     * <p>
     * This code is currently common to all {@link ViewHandlingStrategy} instances.
     * </p>
     *
     * @see ViewHandler#getActionURL(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public String getActionURL(FacesContext context, String viewId) {
        String result = getActionURLWithoutViewProtection(context, viewId);
        // http://java.net/jira/browse/JAVASERVERFACES-2204
        // PENDING: this code is optimized to be fast to write.
        // It must be optimized to be fast to run.

        // See git clone ssh://edburns@git.java.net/grizzly~git 1_9_36 for
        // how grizzly does this.
        ViewHandler viewHandler = context.getApplication().getViewHandler();
        Set<String> urlPatterns = viewHandler.getProtectedViewsUnmodifiable();

        // Implement section 12.1 of the Servlet spec.
        if (urlPatterns.contains(viewId)) {
            StringBuilder builder = new StringBuilder(result);
            // If the result already has a query string...
            if (result.contains("?")) {
                // ...assume it also has one or more parameters, and
                // append an additional parameter.
                builder.append("&");
            } else {
                // Otherwise, this is the first parameter in the result.
                builder.append("?");
            }

            String tokenValue = getResponseStateManager(context, viewHandler.calculateRenderKitId(context))
                                    .getCryptographicallyStrongTokenFromSession(context);

            builder.append(NON_POSTBACK_VIEW_TOKEN_PARAM).append("=").append(tokenValue);
            result = builder.toString();
        }

        return result;
    }

    /**
     * <p>
     * This code is currently common to all {@link ViewHandlingStrategy} instances.
     * </p>
     *
     * @see ViewHandler#getResourceURL(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public String getResourceURL(FacesContext context, String path) {
        requireNonNull(context, "context");
        requireNonNull(path, "path");

        if (path.charAt(0) == '/') {
            return context.getExternalContext().getRequestContextPath() + path;
        }

        return path;
    }

    @Override
    public String getWebsocketURL(FacesContext context, String channel) {
        requireNonNull(context, "context");
        requireNonNull(channel, "channel");

        ExternalContext externalContext = context.getExternalContext();
        return externalContext.encodeWebsocketURL(externalContext.getRequestContextPath() + URI_PREFIX + "/" + channel);
    }

    @Override
    public String getBookmarkableURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        Map<String, List<String>> params;
        if (includeViewParams) {
            params = getFullParameterList(context, viewId, parameters);
        } else {
            params = parameters;
        }

        ExternalContext ectx = context.getExternalContext();
        return ectx.encodeActionURL(ectx.encodeBookmarkableURL(getViewHandler(context).getActionURL(context, viewId), params));
    }

    @Override
    public void addProtectedView(String urlPattern) {
        protectedViews.add(urlPattern);
    }

    @Override
    public Set<String> getProtectedViewsUnmodifiable() {
        return unmodifiableSet(protectedViews);
    }

    @Override
    public boolean removeProtectedView(String urlPattern) {
        return protectedViews.remove(urlPattern);
    }

    /**
     * @see ViewHandler#getRedirectURL(jakarta.faces.context.FacesContext, String, java.util.Map, boolean)
     */
    @Override
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        String responseEncoding = Util.getResponseEncoding(context);

        if (parameters != null) {
            Map<String, List<String>> decodedParameters = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                String string = entry.getKey();
                List<String> list = entry.getValue();
                List<String> values = new ArrayList<>();
                for (Iterator<String> it = list.iterator(); it.hasNext();) {
                    String value = it.next();
                    try {
                        value = URLDecoder.decode(value, responseEncoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Unable to decode");
                    }
                    values.add(value);
                }
                decodedParameters.put(string, values);
            }
            parameters = decodedParameters;
        }

        Map<String, List<String>> params;
        if (includeViewParams) {
            params = getFullParameterList(context, viewId, parameters);
        } else {
            params = parameters;
        }

        ExternalContext ectx = context.getExternalContext();
        return ectx.encodeActionURL(ectx.encodeRedirectURL(Util.getViewHandler(context).getActionURL(context, viewId), params));
    }

    /**
     * @see ViewHandler#getViewDeclarationLanguage(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(FacesContext context, String viewId) {
        return vdlFactory.getViewDeclarationLanguage(viewId);
    }

    @Override
    public Stream<String> getViews(FacesContext context, String path, ViewVisitOption... options) {
        return vdlFactory.getAllViewDeclarationLanguages().stream().flatMap(vdl -> vdl.getViews(context, path, options));
    }

    @Override
    public Stream<String> getViews(FacesContext context, String path, int maxDepth, ViewVisitOption... options) {
        return vdlFactory.getAllViewDeclarationLanguages().stream().flatMap(vdl -> vdl.getViews(context, path, maxDepth, options));
    }

    @Override
    public String deriveViewId(FacesContext context, String requestViewId) {
        return derivePhysicalViewId(context, requestViewId, true);
    }

    @Override
    public String deriveLogicalViewId(FacesContext context, String requestViewId) {
        return derivePhysicalViewId(context, requestViewId, false);
    }


    // ------------------------------------------------------- Protected Methods

    protected String derivePhysicalViewId(FacesContext ctx, String requestViewId, boolean checkPhysical) {
        if (requestViewId == null) {
            return null;
        }

        HttpServletMapping mapping = getFacesMapping(ctx);
        String physicalViewId;

        if (mapping.getMappingMatch() == EXTENSION) {
            // Suffix mapping, e.g. /foo.xhtml
            physicalViewId = convertViewId(ctx, requestViewId);
        } else if (mapping.getMappingMatch() == EXACT) {
            if (requestViewId.equals(mapping.getPattern())) {
                // Fuzzy logic: if request equals the view ID we're asking for
                // this is a call from MultiViewHandler.createView. In that case instead
                // of /foo we want /foo.xhtml.
                return convertViewId(ctx, requestViewId);
            }

            // Exact mapping, e.g. /foo
            // We're likely called here by derive*ViewId, which wants /foo
            physicalViewId = requestViewId;
        } else {
            // Prefix mapping, e.g. /faces/foo.xhtml
            physicalViewId = normalizeRequestURI(requestViewId, mapping.getPattern().replace("/*", ""));
        }

        if (checkPhysical && !getViewDeclarationLanguage(ctx, physicalViewId).viewExists(ctx, physicalViewId)) {
            return null;
        }

        return physicalViewId;
    }

    /**
     * <p>
     * If the specified mapping is a prefix mapping, and the provided request URI (sometimes the value from
     * <code>ExternalContext.getRequestServletPath()</code>) starts with <code>mapping</code>, prune the mapping from
     * the URI and return it, otherwise, return the original URI.
     *
     * @param viewId something resembling a view id, can come from the request or from the navigation handler.
     * @param mapping the FacesServlet mapping used for this request with the "/*" removed, e.g. /faces instead of /faces/*
     * @return the viewId without additional prefix FacesServlet mappings
     *
     * @since 1.2
     */
    protected String normalizeRequestURI(String viewId, String mapping) {
        boolean logged = false;

        while (viewId.startsWith(mapping)) {
            if (!logged && LOGGER.isLoggable(WARNING)) {
                logged = true;
                LOGGER.log(WARNING, "faces.viewhandler.requestpath.recursion", new Object[] { viewId, mapping });
            }
            viewId = viewId.substring(mapping.length());
        }

        return viewId;
    }

    /**
     * <p>
     * Adjust the viewID per the requirements of {@link #renderView}.
     * </p>
     *
     * @param context current {@link jakarta.faces.context.FacesContext}
     * @param viewId incoming view ID
     * @return the view ID with an altered suffix mapping (if necessary)
     */
    protected String convertViewId(FacesContext context, String viewId) {

        // if the viewId doesn't already use the above suffix,
        // replace or append.
        int extIdx = viewId.lastIndexOf('.');
        int length = viewId.length();
        StringBuilder buffer = new StringBuilder(length);

        for (String ext : configuredExtensions) {
            if (viewId.endsWith(ext)) {
                return viewId;
            }

            appendOrReplaceExtension(viewId, ext, length, extIdx, buffer);

            return buffer.toString();
        }

        return viewId;
    }

    protected Map<String, List<String>> getFullParameterList(FacesContext ctx, String viewId, Map<String, List<String>> existingParameters) {
        Map<String, List<String>> copy;
        if (existingParameters == null || existingParameters.isEmpty()) {
            copy = new LinkedHashMap<>(4);
        } else {
            copy = new LinkedHashMap<>(existingParameters);
        }
        addViewParameters(ctx, viewId, copy);

        return copy;
    }

    protected void addViewParameters(FacesContext ctx, String viewId, Map<String, List<String>> existingParameters) {
        UIViewRoot currentRoot = ctx.getViewRoot();
        String currentViewId = currentRoot.getViewId();
        Collection<UIViewParameter> toViewParams = Collections.emptyList();
        Collection<UIViewParameter> currentViewParams;
        boolean currentIsSameAsNew = false;
        currentViewParams = ViewMetadata.getViewParameters(currentRoot);

        if (currentViewId.equals(viewId)) {
            currentIsSameAsNew = true;
            toViewParams = currentViewParams;
        } else {
            ViewMetadata viewMetadata = getViewDeclarationLanguage(ctx, viewId).getViewMetadata(ctx, viewId);
            if (viewMetadata != null) {
                UIViewRoot root = viewMetadata.createMetadataView(ctx);
                toViewParams = ViewMetadata.getViewParameters(root);
            }
        }

        if (toViewParams.isEmpty()) {
            return;
        }

        for (UIViewParameter viewParam : toViewParams) {
            String value = null;
            // don't bother looking at view parameter if it's been overridden
            if (existingParameters.containsKey(viewParam.getName())) {
                continue;
            }

            if (paramHasValueExpression(viewParam)) {
                value = viewParam.getStringValueFromModel(ctx);
            }

            if (value == null) {
                if (currentIsSameAsNew) {
                    /*
                     * Anonymous view parameter: get string value from UIViewParameter instance stored in current view.
                     */
                    value = viewParam.getStringValue(ctx);
                } else {
                    /*
                     * Or transfer string value from matching UIViewParameter instance stored in current view.
                     */
                    value = getStringValueToTransfer(ctx, viewParam, currentViewParams);
                }
            }

            if (value != null) {
                List<String> existing = existingParameters.computeIfAbsent(viewParam.getName(), k -> new ArrayList<>(4));
                existing.add(value);
            }
        }
    }

    /**
     * Attempts to find a matching locale based on <code>pref</code> and list of supported locales, using the matching
     * algorithm as described in JSTL 8.3.2.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param pref the preferred locale
     * @return the Locale based on pref and the matching alogritm specified in JSTL 8.3.2
     */
    protected Locale findMatch(FacesContext context, Locale pref) {
        Locale result = null;

        Iterator<Locale> it = context.getApplication().getSupportedLocales();
        while (it.hasNext()) {
            Locale supportedLocale = it.next();

            if (pref.equals(supportedLocale)) {
                // exact match
                result = supportedLocale;
                break;
            } else {
                // Make sure the preferred locale doesn't have country
                // set, when doing a language match, For ex., if the
                // preferred locale is "en-US", if one of supported
                // locales is "en-UK", even though its language matches
                // that of the preferred locale, we must ignore it.
                if (pref.getLanguage().equals(supportedLocale.getLanguage()) && supportedLocale.getCountry().length() == 0) {
                    result = supportedLocale;
                }
            }
        }

        // if it's not in the supported locales,
        if (result == null) {
            Locale defaultLocale = context.getApplication().getDefaultLocale();
            if (defaultLocale != null) {
                if (pref.equals(defaultLocale)) {
                    // exact match
                    result = defaultLocale;
                } else {
                    // Make sure the preferred locale doesn't have country
                    // set, when doing a language match, For ex., if the
                    // preferred locale is "en-US", if one of supported
                    // locales is "en-UK", even though its language matches
                    // that of the preferred locale, we must ignore it.
                    if (pref.getLanguage().equals(defaultLocale.getLanguage()) && defaultLocale.getCountry().length() == 0) {
                        result = defaultLocale;
                    }
                }
            }
        }

        return result;
    }

    /**
     * <p>
     * Send {@link HttpServletResponse#SC_NOT_FOUND} (404) to the client.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     */
    protected void send404Error(FacesContext context) {
        try {
            context.responseComplete();
            context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "");
        } catch (IOException ioe) {
            throw new FacesException(ioe);
        }
    }

    // --------------------------------------------------------- Private Methods

    private String getActionURLWithoutViewProtection(FacesContext context, String viewId) {
        notNull("context", context);
        notNull("viewId", viewId);

        if (viewId.length() == 0 || viewId.charAt(0) != '/') {
            LOGGER.log(SEVERE, "faces.illegal_view_id_error", viewId);
            throw new IllegalArgumentException(getExceptionMessageString(ILLEGAL_VIEW_ID_ID, viewId));
        }

        // Acquire the context path, which we will prefix on all results
        String contextPath = context.getExternalContext().getRequestContextPath();

        // Acquire the mapping used to execute this request
        HttpServletMapping mapping = getFacesMapping(context);

        // ### Deal with exact mapping

        if (mapping.getMappingMatch() == EXACT) {
            if (viewId.contains(".")) {
                for (String extension : configuredExtensions) {
                    if (viewId.endsWith(extension)) {
                        String exactViewId = viewId.substring(0, viewId.lastIndexOf(extension));
                        if (isViewIdExactMappedToFacesServlet(exactViewId)) {
                            return contextPath + exactViewId;
                        }
                    }
                }
            } else {
                if (isViewIdExactMappedToFacesServlet(viewId)) {
                    return contextPath + viewId;
                }
            }

            // No exact mapping for the requested view id, see if Facelets service is mapped to
            // e.g. /faces/* or *.xhtml and take that mapping
            mapping = getFirstWildCardMappingToFacesServlet(context.getExternalContext());

            if (mapping == null) {

                // If there are only exact mappings and the view is not exact mapped,
                // we can't serve this view

                throw new IllegalStateException("No suitable mapping for FacesServlet found. To serve views that are not exact mapped "
                        + "FacesServlet should have at least one prefix or suffix mapping.");
            }
        }

        // ### Deal with prefix/path mapping, e.g. /faces/*

        if (mapping.getMappingMatch() == PATH) {
            return contextPath + mapping.getPattern().replace("/*", viewId);
        }

        // ### Deal with suffix/extension mapping, e.g. *.xhtml

        // Check for case where viewId has no extension (e.g. /foo)
        if (!viewId.contains(".")) {
            // Just add the mapping extension to it and return
            return contextPath + mapping.getPattern().replace("*", viewId);
        }

        // Remove the * in the pattern, e.g. *.xhtml -> .xhtml
        String mappingExtension = mapping.getPattern().replace("*", "");

        // Check for the case viewId already has exactly the mapping extension (e.g. /foo.xhtml)
        if (viewId.endsWith(mappingExtension)) {
            // Just return it directly
            return contextPath + viewId;
        }

        // Replace whatever extension the viewId has (e.g. /foo.doc) with the mapping extension
        return contextPath + viewId.substring(0, viewId.lastIndexOf('.')) + mappingExtension;
    }

    private static boolean paramHasValueExpression(UIViewParameter param) {
        return param.getValueExpression("value") != null;
    }

    private static String getStringValueToTransfer(FacesContext context, UIViewParameter param, Collection<UIViewParameter> viewParams) {
        if (viewParams != null && !viewParams.isEmpty()) {
            for (UIViewParameter candidate : viewParams) {
                if (candidate.getName() != null && param.getName() != null && candidate.getName().equals(param.getName())) {
                    return candidate.getStringValue(context);
                }
            }
        }

        return param.getStringValue(context);
    }

    // Utility method used by viewId conversion. Appends the extension
    // if no extension is present. Otherwise, replaces the extension.
    private void appendOrReplaceExtension(String viewId, String extension, int length, int extensionIndex, StringBuilder buffer) {
        buffer.setLength(0);
        buffer.append(viewId);

        if (extensionIndex != -1) {
            buffer.replace(extensionIndex, length, extension);
        } else {
            // no extension in the provided viewId, append the suffix
            buffer.append(extension);
        }
    }

}