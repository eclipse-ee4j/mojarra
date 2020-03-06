/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

// ViewHandlerImpl.java

package com.sun.faces.application;

import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.RENDER_KIT_ID_PARAM;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.io.FastStringWriter;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

/**
 * <p>
 * This is the default implementation for JSF 1.2.
 * </p>
 * <p>
 * While this class isn't used by the 2.0 runtime, it's kept for binary compatibility with those
 * that extend from this class directly.
 * </p>
 *
 * @deprecated Refer to {@link com.sun.faces.application.view.MultiViewHandler}
 */
@Deprecated
public class ViewHandlerImpl extends ViewHandler {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private ApplicationAssociate associate;
    private String[] configuredExtensions;
    private int bufSize = -1;

    public ViewHandlerImpl() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Created ViewHandler instance ");
        }
        WebConfiguration config = WebConfiguration.getInstance();
        String defaultSuffixConfig = config.getOptionValue(WebConfiguration.WebContextInitParameter.DefaultSuffix);
        Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
        configuredExtensions = Util.split(appMap, defaultSuffixConfig, " ");
    }

    /**
     * Do not call the default implementation of
     * {@link ViewHandler#initView(jakarta.faces.context.FacesContext)} if the
     * {@link jakarta.faces.context.ExternalContext#getRequestCharacterEncoding()} returns a
     * <code>non-null</code> result.
     *
     * @see ViewHandler#initView(jakarta.faces.context.FacesContext)
     */
    @Override
    public void initView(FacesContext context) throws FacesException {

        if (context.getExternalContext().getRequestCharacterEncoding() == null) {
            super.initView(context);
        }

    }

    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {

        // suppress rendering if "rendered" property on the component is
        // false
        if (!viewToRender.isRendered()) {
            return;
        }

        ExternalContext extContext = context.getExternalContext();
        ServletRequest request = (ServletRequest) extContext.getRequest();
        ServletResponse response = (ServletResponse) extContext.getResponse();

        try {
            if (executePageToBuildView(context, viewToRender)) {
                response.flushBuffer();
                ApplicationAssociate associate = getAssociate(context);
                if (associate != null) {
                    associate.responseRendered();
                }
                return;
            }
        } catch (IOException e) {
            throw new FacesException(e);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Completed building view for : \n" + viewToRender.getViewId());
        }

        // set up the ResponseWriter

        RenderKitFactory renderFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderFactory.getRenderKit(context, viewToRender.getRenderKitId());

        ResponseWriter oldWriter = context.getResponseWriter();

        if (bufSize == -1) {
            WebConfiguration webConfig = WebConfiguration.getInstance(context.getExternalContext());
            try {
                bufSize = Integer.parseInt(webConfig.getOptionValue(WebContextInitParameter.ResponseBufferSize));
            } catch (NumberFormatException nfe) {
                bufSize = Integer.parseInt(WebContextInitParameter.ResponseBufferSize.getDefaultValue());
            }
        }

        WriteBehindStateWriter stateWriter = new WriteBehindStateWriter(response.getWriter(), context, bufSize);
        ResponseWriter newWriter;
        if (null != oldWriter) {
            newWriter = oldWriter.cloneWithWriter(stateWriter);
        } else {
            newWriter = renderKit.createResponseWriter(stateWriter, null, request.getCharacterEncoding());
        }
        context.setResponseWriter(newWriter);

        try {
            newWriter.startDocument();

            doRenderView(context, viewToRender);

            newWriter.endDocument();

            // replace markers in the body content and write it to response.

            // flush directly to the response
            if (stateWriter.stateWritten()) {
                stateWriter.flushToWriter();
            }
        } finally {

            // clear the ThreadLocal reference.
            stateWriter.release();

            if (null != oldWriter) {
                context.setResponseWriter(oldWriter);
            }
        }

        // write any AFTER_VIEW_CONTENT to the response
        // side effect: AFTER_VIEW_CONTENT removed
        Map<String, Object> stateMap = RequestStateManager.getStateMap(context);
        ViewHandlerResponseWrapper wrapper = (ViewHandlerResponseWrapper) stateMap.remove(RequestStateManager.AFTER_VIEW_CONTENT);

        if (null != wrapper) {
            wrapper.flushToWriter(response.getWriter(), response.getCharacterEncoding());
        }

        response.flushBuffer();

    }

    /**
     * <p>
     * This is a separate method to account for handling the content after the view tag.
     * </p>
     *
     * <p>
     * Create a new ResponseWriter around this response's Writer. Set it into the FacesContext,
     * saving the old one aside.
     * </p>
     *
     * <p>
     * call encodeBegin(), encodeChildren(), encodeEnd() on the argument <code>UIViewRoot</code>.
     * </p>
     *
     * <p>
     * Restore the old ResponseWriter into the FacesContext.
     * </p>
     *
     * <p>
     * Write out the after view content to the response's writer.
     * </p>
     *
     * <p>
     * Flush the response buffer, and remove the after view content from the request scope.
     * </p>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param viewToRender the view to render
     * @throws IOException if an error occurs rendering the view to the client
     * @throws FacesException if some error occurs within the framework processing
     */

    private void doRenderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {

        ApplicationAssociate associate = getAssociate(context);

        if (null != associate) {
            associate.responseRendered();
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "About to render view " + viewToRender.getViewId());
        }

        viewToRender.encodeAll(context);
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }

        ExternalContext extContext = context.getExternalContext();

        String mapping = Util.getFacesMapping(context);
        UIViewRoot viewRoot = null;

        if (mapping != null) {
            if (!Util.isPrefixMapped(mapping)) {
                viewId = convertViewId(context, viewId);
            } else {
                viewId = normalizeRequestURI(viewId, mapping);
            }
        }

        // maping could be null if a non-faces request triggered
        // this response.
        if (extContext.getRequestPathInfo() == null && mapping != null && Util.isPrefixMapped(mapping)) {
            // this was probably an initial request
            // send them off to the root of the web application
            try {
                context.responseComplete();
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Response Complete for" + viewId);
                }
                extContext.redirect(extContext.getRequestContextPath());
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        } else {
            // this is necessary to allow decorated impls.
            ViewHandler outerViewHandler = context.getApplication().getViewHandler();
            String renderKitId = outerViewHandler.calculateRenderKitId(context);
            viewRoot = Util.getStateManager(context).restoreView(context, viewId, renderKitId);
        }

        return viewRoot;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }

        UIViewRoot result = (UIViewRoot) context.getApplication().createComponent(UIViewRoot.COMPONENT_TYPE);

        if (viewId != null) {
            String mapping = Util.getFacesMapping(context);

            if (mapping != null) {
                if (!Util.isPrefixMapped(mapping)) {
                    viewId = convertViewId(context, viewId);
                } else {
                    viewId = normalizeRequestURI(viewId, mapping);
                    if (!Util.isPortletRequest(context) && viewId.equals(mapping)) {
                        // The request was to the FacesServlet only - no
                        // path info
                        // on some containers this causes a recursion in the
                        // RequestDispatcher and the request appears to hang.
                        // If this is detected, return status 404
                        send404Error(context);
                    }
                }
            }

            result.setViewId(viewId);
        }

        Locale locale = null;
        String renderKitId = null;

        // use the locale from the previous view if is was one which will be
        // the case if this is called from NavigationHandler. There wouldn't be
        // one for the initial case.
        if (context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
            renderKitId = context.getViewRoot().getRenderKitId();
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Created new view for " + viewId);
        }
        // PENDING(): not sure if we should set the RenderKitId here.
        // The UIViewRoot ctor sets the renderKitId to the default
        // one.
        // if there was no locale from the previous view, calculate the locale
        // for this view.
        if (locale == null) {
            locale = context.getApplication().getViewHandler().calculateLocale(context);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Locale for this view as determined by calculateLocale " + locale.toString());
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Using locale from previous view " + locale.toString());
            }
        }

        if (renderKitId == null) {
            renderKitId = context.getApplication().getViewHandler().calculateRenderKitId(context);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("RenderKitId for this view as determined by calculateRenderKitId " + renderKitId);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Using renderKitId from previous view " + renderKitId);
            }
        }

        result.setLocale(locale);
        result.setRenderKitId(renderKitId);

        return result;
    }

    /**
     * Execute the target view. If the HTTP status code range is not 2xx, then return true to
     * indicate the response should be immediately flushed by the caller so that conditions such as
     * 404 are properly handled.
     * 
     * @param context the <code>FacesContext</code> for the current request
     * @param viewToExecute the view to build
     * @return <code>true</code> if the response should be immediately flushed to the client,
     *         otherwise <code>false</code>
     * @throws IOException if an error occurs executing the page
     */
    private boolean executePageToBuildView(FacesContext context, UIViewRoot viewToExecute) throws IOException {

        if (null == context) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }
        if (null == viewToExecute) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "viewToExecute");
            throw new NullPointerException(message);
        }

        ExternalContext extContext = context.getExternalContext();
        Map<String, Object> stateMap = RequestStateManager.getStateMap(context);

        if ("/*".equals(stateMap.get(RequestStateManager.INVOCATION_PATH))) {
            throw new FacesException(MessageUtils.getExceptionMessageString(MessageUtils.FACES_SERVLET_MAPPING_INCORRECT_ID));
        }

        String requestURI = viewToExecute.getViewId();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("About to execute view " + requestURI);
        }

        // update the JSTL locale attribute in request scope so that JSTL
        // picks up the locale from viewRoot. This attribute must be updated
        // before the JSTL setBundle tag is called because that is when the
        // new LocalizationContext object is created based on the locale.
        if (extContext.getRequest() instanceof ServletRequest) {
            Config.set((ServletRequest) extContext.getRequest(), Config.FMT_LOCALE, context.getViewRoot().getLocale());
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Before dispacthMessage to viewId " + requestURI);
        }

        // save the original response
        Object originalResponse = extContext.getResponse();

        // replace the response with our wrapper
        ViewHandlerResponseWrapper wrapped = getWrapper(extContext);
        extContext.setResponse(wrapped);

        // build the view by executing the page
        extContext.dispatch(requestURI);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("After dispacthMessage to viewId " + requestURI);
        }

        // replace the original response
        extContext.setResponse(originalResponse);

        // Follow the JSTL 1.2 spec, section 7.4,
        // on handling status codes on a forward
        if (wrapped.getStatus() < 200 || wrapped.getStatus() > 299) {
            // flush the contents of the wrapper to the response
            // this is necessary as the user may be using a custom
            // error page - this content should be propagated
            wrapped.flushContentToWrappedResponse();
            return true;
        }

        // Put the AFTER_VIEW_CONTENT into request scope
        // temporarily
        stateMap.put(RequestStateManager.AFTER_VIEW_CONTENT, wrapped);

        return false;

    }

    @Override
    public Locale calculateLocale(FacesContext context) {

        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }

        Locale result = null;
        // determine the locales that are acceptable to the client based on the
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

    @Override
    public String calculateRenderKitId(FacesContext context) {

        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }

        String result = RENDER_KIT_ID_PARAM.getValue(context);

        if (result == null) {
            result = context.getApplication().getDefaultRenderKitId();
            if (null == result) {
                result = RenderKitFactory.HTML_BASIC_RENDER_KIT;
            }
        }
        return result;
    }

    /**
     * Attempts to find a matching locale based on <code>pref</code> and list of supported locales,
     * using the matching algorithm as described in JSTL 8.3.2.
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
        if (null == result) {
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

    @Override
    public void writeState(FacesContext context) throws IOException {
        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Begin writing marker for viewId " + context.getViewRoot().getViewId());
        }

        WriteBehindStateWriter writer = WriteBehindStateWriter.getCurrentInstance();
        if (writer != null) {
            writer.writingState();
        }
        context.getResponseWriter().write(RIConstants.SAVESTATE_FIELD_MARKER);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("End writing marker for viewId " + context.getViewRoot().getViewId());
        }

    }

    @Override
    public String getActionURL(FacesContext context, String viewId) {

        if (context == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context");
            throw new NullPointerException(message);
        }
        if (viewId == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "viewId");
            throw new NullPointerException(message);
        }

        if (0 == viewId.length() || viewId.charAt(0) != '/') {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.ILLEGAL_VIEW_ID_ID, viewId);
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "jsf.illegal_view_id_error", viewId);
            }
            throw new IllegalArgumentException(message);
        }

        // Acquire the context path, which we will prefix on all results
        ExternalContext extContext = context.getExternalContext();
        String contextPath = extContext.getRequestContextPath();

        // Acquire the mapping used to execute this request (if any)
        String mapping = Util.getFacesMapping(context);

        // If no mapping can be identified, just return a server-relative path
        if (mapping == null) {
            return (contextPath + viewId);
        }

        // Deal with prefix mapping
        if (Util.isPrefixMapped(mapping)) {
            if (mapping.equals("/*")) {
                return (contextPath + viewId);
            } else {
                return (contextPath + mapping + viewId);
            }
        }

        // Deal with extension mapping
        for (String extension : configuredExtensions) {
            if (viewId.endsWith(extension)) {
                return (contextPath + viewId.substring(0, viewId.lastIndexOf('.')) + mapping);
            }
        }
        return (contextPath + viewId);

    }

    @Override
    public String getResourceURL(FacesContext context, String path) {
        ExternalContext extContext = context.getExternalContext();
        if (path.charAt(0) == '/' && !path.startsWith(extContext.getRequestContextPath())) {
            return (extContext.getRequestContextPath() + path);
        } else {
            return path;
        }

    }

    @Override
    public String getWebsocketURL(FacesContext context, String channel) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * if the specified mapping is a prefix mapping, and the provided request URI (usually the value
     * from <code>ExternalContext.getRequestServletPath()</code>) starts with
     * <code>mapping + '/'</code>, prune the mapping from the URI and return it, otherwise, return
     * the original URI.
     * 
     * @param uri the servlet request path
     * @param mapping the FacesServlet mapping used for this request
     * @return the URI without additional FacesServlet mappings
     * @since 1.2
     */
    private String normalizeRequestURI(String uri, String mapping) {

        if (mapping == null || !Util.isPrefixMapped(mapping)) {
            return uri;
        } else {
            int length = mapping.length() + 1;
            StringBuilder builder = new StringBuilder(length);
            builder.append(mapping).append('/');
            String mappingMod = builder.toString();
            boolean logged = false;
            while (uri.startsWith(mappingMod)) {
                if (!logged && LOGGER.isLoggable(Level.WARNING)) {
                    logged = true;
                    LOGGER.log(Level.WARNING, "jsf.viewhandler.requestpath.recursion", new Object[] { uri, mapping });
                }
                uri = uri.substring(length - 1);
            }
            return uri;
        }
    }

    private void send404Error(FacesContext context) {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        try {
            context.responseComplete();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException ioe) {
            throw new FacesException(ioe);
        }
    }

    /**
     * <p>
     * Adjust the viewID per the requirements of {@link #renderView}.
     * </p>
     *
     * @param context current {@link FacesContext}
     * @param viewId incoming view ID
     * @return the view ID with an altered suffix mapping (if necessary)
     */
    private String convertViewId(FacesContext context, String viewId) {

        // if the viewId doesn't already use the above suffix,
        // replace or append.
        StringBuilder buffer = new StringBuilder(viewId);
        for (String ext : configuredExtensions) {
            if (viewId.endsWith(ext)) {
                return viewId;
            }
            int extIdx = viewId.lastIndexOf('.');
            if (extIdx != -1) {
                buffer.replace(extIdx, viewId.length(), ext);
            } else {
                // no extension in the provided viewId, append the suffix
                buffer.append(ext);
            }
            String convertedViewId = buffer.toString();
            try {
                if (context.getExternalContext().getResource(convertedViewId) != null) {
                    return convertedViewId;
                } else {
                    // reset the buffer to check for the next extension
                    buffer.setLength(0);
                    buffer.append(viewId);
                }
            } catch (MalformedURLException e) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, e.toString(), e);
                }
            }
        }

        // unable to find any resource match that the default ViewHandler
        // can deal with. Return the viewId as it was passed. There is
        // probably another ViewHandler in the stack that will handle this.
        return viewId;
    }

    private ApplicationAssociate getAssociate(FacesContext context) {
        if (associate == null) {
            associate = ApplicationAssociate.getInstance(context.getExternalContext());
        }
        return associate;
    }

    private static ViewHandlerResponseWrapper getWrapper(ExternalContext extContext) {
        Object response = extContext.getResponse();
        if (response instanceof HttpServletResponse) {
            return new ViewHandlerResponseWrapper((HttpServletResponse) response);
        }
        throw new IllegalArgumentException();

    }

    // ----------------------------------------------------------- Inner Classes

    /**
     * Thanks to the Facelets folks for some of the concepts incorporated into this class.
     */
    private static final class WriteBehindStateWriter extends Writer {
        // length of the state marker
        private static final int STATE_MARKER_LEN = RIConstants.SAVESTATE_FIELD_MARKER.length();

        private static final ThreadLocal<WriteBehindStateWriter> CUR_WRITER = new ThreadLocal<>();
        private Writer out;
        private Writer orig;
        private FastStringWriter fWriter;
        private boolean stateWritten;
        private int bufSize;
        private char[] buf;
        private FacesContext context;

        // -------------------------------------------------------- Constructors

        public WriteBehindStateWriter(Writer out, FacesContext context, int bufSize) {
            this.out = out;
            this.orig = out;
            this.context = context;
            this.bufSize = bufSize;
            this.buf = new char[bufSize];
            CUR_WRITER.set(this);
        }

        // ------------------------------------------------- Methods from Writer

        @Override
        public void write(int c) throws IOException {
            out.write(c);
        }

        @Override
        public void write(char cbuf[]) throws IOException {
            out.write(cbuf);
        }

        @Override
        public void write(String str) throws IOException {
            out.write(str);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            out.write(str, off, len);
        }

        @Override
        public void write(char cbuf[], int off, int len) throws IOException {
            out.write(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            // no-op
        }

        @Override
        public void close() throws IOException {
            // no-op
        }

        // ------------------------------------------------------ Public Methods

        public static WriteBehindStateWriter getCurrentInstance() {
            return CUR_WRITER.get();
        }

        public void release() {
            CUR_WRITER.remove();
        }

        public void writingState() {
            if (!stateWritten) {
                this.stateWritten = true;
                out = fWriter = new FastStringWriter(1024);
            }
        }

        public boolean stateWritten() {
            return stateWritten;
        }

        /**
         * <p>
         * Write directly from our FastStringWriter to the provided writer.
         * </p>
         * 
         * @throws IOException if an error occurs
         */
        public void flushToWriter() throws IOException {
            // Save the state to a new instance of StringWriter to
            // avoid multiple serialization steps if the view contains
            // multiple forms.
            StateManager stateManager = Util.getStateManager(context);
            ResponseWriter origWriter = context.getResponseWriter();
            FastStringWriter state = new FastStringWriter((stateManager.isSavingStateInClient(context)) ? bufSize : 128);
            context.setResponseWriter(origWriter.cloneWithWriter(state));
            stateManager.writeState(context, stateManager.saveView(context));
            context.setResponseWriter(origWriter);
            StringBuilder builder = fWriter.getBuffer();
            // begin writing...
            int totalLen = builder.length();
            StringBuilder stateBuilder = state.getBuffer();
            int stateLen = stateBuilder.length();
            int pos = 0;
            int tildeIdx = getNextDelimiterIndex(builder, pos);
            while (pos < totalLen) {
                if (tildeIdx != -1) {
                    if (tildeIdx > pos && (tildeIdx - pos) > bufSize) {
                        // there's enough content before the first ~
                        // to fill the entire buffer
                        builder.getChars(pos, (pos + bufSize), buf, 0);
                        orig.write(buf);
                        pos += bufSize;
                    } else {
                        // write all content up to the first '~'
                        builder.getChars(pos, tildeIdx, buf, 0);
                        int len = (tildeIdx - pos);
                        orig.write(buf, 0, len);
                        // now check to see if the state saving string is
                        // at the begining of pos, if so, write our
                        // state out.
                        if (builder.indexOf(RIConstants.SAVESTATE_FIELD_MARKER, pos) == tildeIdx) {
                            // buf is effectively zero'd out at this point
                            int statePos = 0;
                            while (statePos < stateLen) {
                                if ((stateLen - statePos) > bufSize) {
                                    // enough state to fill the buffer
                                    stateBuilder.getChars(statePos, (statePos + bufSize), buf, 0);
                                    orig.write(buf);
                                    statePos += bufSize;
                                } else {
                                    int slen = (stateLen - statePos);
                                    stateBuilder.getChars(statePos, stateLen, buf, 0);
                                    orig.write(buf, 0, slen);
                                    statePos += slen;
                                }

                            }
                            // push us past the last '~' at the end of the marker
                            pos += (len + STATE_MARKER_LEN);
                            tildeIdx = getNextDelimiterIndex(builder, pos);
                        } else {
                            pos = tildeIdx;
                            tildeIdx = getNextDelimiterIndex(builder, tildeIdx + 1);

                        }
                    }
                } else {
                    // we've written all of the state field markers.
                    // finish writing content
                    if (totalLen - pos > bufSize) {
                        // there's enough content to fill the buffer
                        builder.getChars(pos, (pos + bufSize), buf, 0);
                        orig.write(buf);
                        pos += bufSize;
                    } else {
                        // we're near the end of the response
                        builder.getChars(pos, totalLen, buf, 0);
                        int len = (totalLen - pos);
                        orig.write(buf, 0, len);
                        pos += (len + 1);
                    }
                }
            }

            // all state has been written. Have 'out' point to the
            // response so that all subsequent writes will make it to the
            // browser.
            out = orig;
        }

        private static int getNextDelimiterIndex(StringBuilder builder, int offset) {
            return builder.indexOf(RIConstants.SAVESTATE_FIELD_DELIMITER, offset);
        }

    }

}
