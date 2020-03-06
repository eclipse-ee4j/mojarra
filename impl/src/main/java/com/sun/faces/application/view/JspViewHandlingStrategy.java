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

package com.sun.faces.application.view;

import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.ResponseBufferSize;
import static com.sun.faces.util.RequestStateManager.AFTER_VIEW_CONTENT;
import static com.sun.faces.util.Util.isViewPopulated;
import static com.sun.faces.util.Util.setViewPopulated;
import static jakarta.faces.FactoryFinder.RENDER_KIT_FACTORY;
import static java.lang.Integer.parseInt;
import static java.util.logging.Level.FINE;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.jstl.core.Config;

import com.sun.faces.application.ViewHandlerResponseWrapper;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.RequestStateManager;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ViewVisitOption;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewMetadata;

/**
 * This {@link ViewHandlingStrategy} handles JSP-based views.
 */
public class JspViewHandlingStrategy extends ViewHandlingStrategy {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    private int responseBufferSize;

    // ------------------------------------------------------------ Constructors

    public JspViewHandlingStrategy() {

        try {
            responseBufferSize = parseInt(webConfig.getOptionValue(ResponseBufferSize));
        } catch (NumberFormatException nfe) {
            responseBufferSize = parseInt(ResponseBufferSize.getDefaultValue());
        }
    }

    // ------------------------------------ Methods from ViewDeclarationLanguage

    /**
     * <p>
     * Not supported in JSP-based views.
     * </p>
     *
     * @see jakarta.faces.view.ViewDeclarationLanguage#getViewMetadata(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId) {
        return null;
    }

    /**
     * @see jakarta.faces.view.ViewDeclarationLanguage#buildView(jakarta.faces.context.FacesContext,
     * jakarta.faces.component.UIViewRoot)
     * @param context
     * @param view
     * @throws IOException
     */
    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException {

        if (isViewPopulated(context, view)) {
            return;
        }

        try {
            if (executePageToBuildView(context, view)) {
                context.getExternalContext().responseFlushBuffer();
                if (associate != null) {
                    associate.responseRendered();
                }
                context.responseComplete();
                return;
            }
        } catch (IOException e) {
            throw new FacesException(e);
        }

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "Completed building view for : \n" + view.getViewId());
        }

        context.getApplication().publishEvent(context, PostAddToViewEvent.class, UIViewRoot.class, view);
        setViewPopulated(context, view);
    }

    /**
     * @see jakarta.faces.view.ViewDeclarationLanguage#renderView(jakarta.faces.context.FacesContext,
     * jakarta.faces.component.UIViewRoot)
     */
    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException {

        // Suppress rendering if "rendered" property on the component is false
        if (!view.isRendered() || context.getResponseComplete()) {
            return;
        }

        ExternalContext extContext = context.getExternalContext();

        if (!isViewPopulated(context, view)) {
            buildView(context, view);
        }

        // Set up the ResponseWriter

        RenderKitFactory renderFactory = (RenderKitFactory) FactoryFinder.getFactory(RENDER_KIT_FACTORY);
        RenderKit renderKit = renderFactory.getRenderKit(context, view.getRenderKitId());

        ResponseWriter oldWriter = context.getResponseWriter();

        WriteBehindStateWriter stateWriter = new WriteBehindStateWriter(extContext.getResponseOutputWriter(), context, responseBufferSize);
        ResponseWriter newWriter;
        if (null != oldWriter) {
            newWriter = oldWriter.cloneWithWriter(stateWriter);
        } else {
            newWriter = renderKit.createResponseWriter(stateWriter, null, extContext.getRequestCharacterEncoding());
        }
        context.setResponseWriter(newWriter);

        // Don't call startDoc and endDoc on a partial response
        if (context.getPartialViewContext().isPartialRequest()) {
            doRenderView(context, view);
            try {
                extContext.getFlash().doPostPhaseActions(context);
            } catch (UnsupportedOperationException uoe) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.fine("ExternalContext.getFlash() throw UnsupportedOperationException -> Flash unavailable");
                }
            }
        } else {
            // render the view to the response
            newWriter.startDocument();
            doRenderView(context, view);
            try {
                extContext.getFlash().doPostPhaseActions(context);
            } catch (UnsupportedOperationException uoe) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.fine("ExternalContext.getFlash() throw UnsupportedOperationException -> Flash unavailable");
                }
            }
            newWriter.endDocument();
        }

        // replace markers in the body content and write it to response.

        // flush directly to the response
        if (stateWriter.stateWritten()) {
            stateWriter.flushToWriter();
        }

        // clear the ThreadLocal reference.
        stateWriter.release();

        if (null != oldWriter) {
            context.setResponseWriter(oldWriter);
        }

        // write any AFTER_VIEW_CONTENT to the response
        // side effect: AFTER_VIEW_CONTENT removed
        ViewHandlerResponseWrapper wrapper = (ViewHandlerResponseWrapper) RequestStateManager.remove(context, AFTER_VIEW_CONTENT);

        if (null != wrapper) {
            wrapper.flushToWriter(extContext.getResponseOutputWriter(), extContext.getResponseCharacterEncoding());
        }

        extContext.responseFlushBuffer();
    }

    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId) {
        return null;
    }

    /**
     * <p>
     * Not supported in JSP-based views.
     * </p>
     *
     * @see jakarta.faces.view.ViewDeclarationLanguage#getComponentMetadata(jakarta.faces.context.FacesContext,
     * jakarta.faces.application.Resource)
     */
    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Not supported in JSP-based views.
     * </p>
     *
     * @see jakarta.faces.view.ViewDeclarationLanguage#getScriptComponentResource(jakarta.faces.context.FacesContext,
     * jakarta.faces.application.Resource)
     */
    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Not supported in JSP-based views.
     * </p>
     *
     * @see jakarta.faces.view.ViewDeclarationLanguage#getViews(FacesContext, String, ViewVisitOption...)
     */
    @Override
    public Stream<String> getViews(FacesContext context, String path, ViewVisitOption... options) {
        return Stream.empty();
    }

    /**
     * <p>
     * Not supported in JSP-based views.
     * </p>
     *
     * @see jakarta.faces.view.ViewDeclarationLanguage#getViews(FacesContext, String, int, ViewVisitOption...)
     */
    @Override
    public Stream<String> getViews(FacesContext context, String path, int maxDepth, ViewVisitOption... options) {
        return Stream.empty();
    }

    // --------------------------------------- Methods from ViewHandlingStrategy

    /**
     * This {@link ViewHandlingStrategy} <em>should</em> be the last one queried and as such we return <code>true</code>.
     *
     * @see com.sun.faces.application.view.ViewHandlingStrategy#handlesViewId(String)
     */
    @Override
    public boolean handlesViewId(String viewId) {
        return true;
    }

    @Override
    public String getId() {
        return JSP_VIEW_DECLARATION_LANGUAGE_ID;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Execute the target view. If the HTTP status code range is not 2xx, then return true to indicate the response should
     * be immediately flushed by the caller so that conditions such as 404 are properly handled.
     * 
     * @param context the <code>FacesContext</code> for the current request
     * @param viewToExecute the view to build
     * @return <code>true</code> if the response should be immediately flushed to the client, otherwise <code>false</code>
     * @throws java.io.IOException if an error occurs executing the page
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

        if ("/*".equals(RequestStateManager.get(context, RequestStateManager.INVOCATION_PATH))) {
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

        try {

            // build the view by executing the page
            extContext.dispatch(requestURI);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("After dispacthMessage to viewId " + requestURI);
            }
        } finally {
            // replace the original response
            extContext.setResponse(originalResponse);
        }

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
        RequestStateManager.set(context, RequestStateManager.AFTER_VIEW_CONTENT, wrapped);

        return false;

    }

    /**
     * <p>
     * This is a separate method to account for handling the content after the view tag.
     * </p>
     *
     * <p>
     * Create a new ResponseWriter around this response's Writer. Set it into the FacesContext, saving the old one aside.
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
     * @throws java.io.IOException if an error occurs rendering the view to the client
     * @throws jakarta.faces.FacesException if some error occurs within the framework processing
     */
    private void doRenderView(FacesContext context, UIViewRoot viewToRender) throws IOException {

        if (null != associate) {
            associate.responseRendered();
        }

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "About to render view " + viewToRender.getViewId());
        }

        viewToRender.encodeAll(context);
    }

    /**
     * <p>
     * Simple utility method to wrap the current response with the {@link ViewHandlerResponseWrapper}.
     * </p>
     * 
     * @param extContext the {@link ExternalContext} for this request
     * @return the current response wrapped with ViewHandlerResponseWrapper
     */
    private static ViewHandlerResponseWrapper getWrapper(ExternalContext extContext) {

        Object response = extContext.getResponse();
        if (response instanceof HttpServletResponse) {
            return new ViewHandlerResponseWrapper((HttpServletResponse) response);
        }
        throw new IllegalArgumentException();

    }

}
