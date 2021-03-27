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

package jakarta.faces.context;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.faces.FacesWrapper;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseId;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;

/**
 * <p>
 * <span class="changed_modified_2_1 changed_modified_2_2 changed_modified_2_3">Provides</span> a simple implementation
 * of {@link FacesContext} that can be subclassed by developers wishing to provide specialized behavior to an existing
 * {@link FacesContext} instance. The default implementation of all methods is to call through to the wrapped
 * {@link FacesContext} instance.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class FacesContextWrapper extends FacesContext implements FacesWrapper<FacesContext> {

    private FacesContext wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public FacesContextWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this faces context has been decorated, the implementation doing the decorating should push the implementation
     * being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public FacesContextWrapper(FacesContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public FacesContext getWrapped() {
        return wrapped;
    }

    // ----------------------------------------------- Methods from FacesContext

    /**
     * <p class="changed_added_4_0">
     * The default behavior of this method is to call {@link FacesContext#getLifecycle()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getLifecycle()
     */
    @Override
    public Lifecycle getLifecycle() {
        return getWrapped().getLifecycle();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getApplication()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getApplication()
     */
    @Override
    public Application getApplication() {
        return getWrapped().getApplication();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.context.FacesContext#getClientIdsWithMessages()}
     * on the wrapped {@link FacesContext} object.
     * </p>
     *
     * @see FacesContext#getClientIdsWithMessages()
     */
    @Override
    public Iterator<String> getClientIdsWithMessages() {
        return getWrapped().getClientIdsWithMessages();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getExternalContext()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getExternalContext()
     */
    @Override
    public ExternalContext getExternalContext() {
        return getWrapped().getExternalContext();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getMaximumSeverity()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getMaximumSeverity()
     */
    @Override
    public FacesMessage.Severity getMaximumSeverity() {
        return getWrapped().getMaximumSeverity();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getMessages()} on the wrapped {@link FacesContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getMessages()
     */
    @Override
    public Iterator<FacesMessage> getMessages() {
        return getWrapped().getMessages();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getMessages(String)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getMessages(String)
     */
    @Override
    public Iterator<FacesMessage> getMessages(String clientId) {
        return getWrapped().getMessages(clientId);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getRenderKit()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getRenderKit()
     */
    @Override
    public RenderKit getRenderKit() {
        return getWrapped().getRenderKit();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getRenderResponse()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getRenderResponse()
     */
    @Override
    public boolean getRenderResponse() {
        return getWrapped().getRenderResponse();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getResourceLibraryContracts} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getResourceLibraryContracts
     */
    @Override
    public List<String> getResourceLibraryContracts() {
        return getWrapped().getResourceLibraryContracts();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setResourceLibraryContracts} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setResourceLibraryContracts
     */
    @Override
    public void setResourceLibraryContracts(List<String> contracts) {
        getWrapped().setResourceLibraryContracts(contracts);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getResponseComplete()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getResponseComplete()
     */
    @Override
    public boolean getResponseComplete() {
        return getWrapped().getResponseComplete();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getResponseStream()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getResponseStream()
     */
    @Override
    public ResponseStream getResponseStream() {
        return getWrapped().getResponseStream();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setResponseStream(ResponseStream)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setResponseStream(ResponseStream)
     */
    @Override
    public void setResponseStream(ResponseStream responseStream) {
        getWrapped().setResponseStream(responseStream);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getResponseWriter()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getResponseWriter()
     */
    @Override
    public ResponseWriter getResponseWriter() {
        return getWrapped().getResponseWriter();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setResponseWriter(ResponseWriter)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setResponseWriter(ResponseWriter)
     */
    @Override
    public void setResponseWriter(ResponseWriter responseWriter) {
        getWrapped().setResponseWriter(responseWriter);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getViewRoot()} on the wrapped {@link FacesContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getViewRoot()
     */
    @Override
    public UIViewRoot getViewRoot() {
        return getWrapped().getViewRoot();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setViewRoot(UIViewRoot)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setViewRoot(UIViewRoot)
     */
    @Override
    public void setViewRoot(UIViewRoot root) {
        getWrapped().setViewRoot(root);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#addMessage(String, FacesMessage)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#addMessage(String, FacesMessage)
     */
    @Override
    public void addMessage(String clientId, FacesMessage message) {
        getWrapped().addMessage(clientId, message);
    }

    /**
     * <p class="changed_added_2_1">
     * The default behavior of this method is to call {@link FacesContext#isReleased} on the wrapped {@link FacesContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#isReleased
     *
     * @since 2.1
     */
    @Override
    public boolean isReleased() {
        return getWrapped().isReleased();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#release()} on the wrapped {@link FacesContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#release()
     */
    @Override
    public void release() {
        getWrapped().release();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#renderResponse()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#renderResponse()
     */
    @Override
    public void renderResponse() {
        getWrapped().renderResponse();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#responseComplete()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#responseComplete()
     */
    @Override
    public void responseComplete() {
        getWrapped().responseComplete();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getAttributes()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getAttributes()
     */
    @Override
    public Map<Object, Object> getAttributes() {
        return getWrapped().getAttributes();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link FacesContext#getNamingContainerSeparatorChar()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getNamingContainerSeparatorChar()
     */
    @Override
    public char getNamingContainerSeparatorChar() {
        return getWrapped().getNamingContainerSeparatorChar();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getPartialViewContext()} ()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getPartialViewContext()
     */
    @Override
    public PartialViewContext getPartialViewContext() {
        return getWrapped().getPartialViewContext();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getELContext()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getELContext()
     */
    @Override
    public ELContext getELContext() {
        return getWrapped().getELContext();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getExceptionHandler()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getExceptionHandler()
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        return getWrapped().getExceptionHandler();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setExceptionHandler(ExceptionHandler)} on the
     * wrapped {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setExceptionHandler(ExceptionHandler)
     */
    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        getWrapped().setExceptionHandler(exceptionHandler);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getMessageList()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getMessageList()
     */
    @Override
    public List<FacesMessage> getMessageList() {
        return getWrapped().getMessageList();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getMessageList(String)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getMessageList(String)
     */
    @Override
    public List<FacesMessage> getMessageList(String clientId) {
        return getWrapped().getMessageList(clientId);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#isPostback()} on the wrapped {@link FacesContext}
     * object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#isPostback()
     */
    @Override
    public boolean isPostback() {
        return getWrapped().isPostback();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#getCurrentPhaseId()} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#getCurrentPhaseId()
     */
    @Override
    public PhaseId getCurrentPhaseId() {
        return getWrapped().getCurrentPhaseId();
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setCurrentPhaseId(PhaseId)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setCurrentPhaseId(PhaseId)
     */
    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId) {
        getWrapped().setCurrentPhaseId(currentPhaseId);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.context.FacesContext#isValidationFailed} on the
     * wrapped {@link FacesContext} object.
     * </p>
     *
     * @see FacesContext#isValidationFailed
     */
    @Override
    public boolean isValidationFailed() {

        return getWrapped().isValidationFailed();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.context.FacesContext#validationFailed()} on the
     * wrapped {@link FacesContext} object.
     * </p>
     *
     * @see FacesContext#validationFailed()
     */
    @Override
    public void validationFailed() {

        getWrapped().validationFailed();

    }

    /**
     * <p>
     * The default behavior of this method is to call {@link FacesContext#setProcessingEvents(boolean)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see jakarta.faces.context.FacesContext#setProcessingEvents(boolean)
     */
    @Override
    public void setProcessingEvents(boolean processingEvents) {
        getWrapped().setProcessingEvents(processingEvents);
    }

    /**
     * <p>
     * The default behavior of this method is to call {@link jakarta.faces.context.FacesContext#isProcessingEvents()} on the
     * wrapped {@link FacesContext} object.
     * </p>
     *
     * @see FacesContext#isProcessingEvents()
     */
    @Override
    public boolean isProcessingEvents() {
        return getWrapped().isProcessingEvents();
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link jakarta.faces.context.FacesContext#isProjectStage(jakarta.faces.application.ProjectStage)} on the wrapped
     * {@link FacesContext} object.
     * </p>
     *
     * @see FacesContext#isProjectStage(jakarta.faces.application.ProjectStage)
     */
    @Override
    public boolean isProjectStage(ProjectStage stage) {
        return getWrapped().isProjectStage(stage);
    }
}
