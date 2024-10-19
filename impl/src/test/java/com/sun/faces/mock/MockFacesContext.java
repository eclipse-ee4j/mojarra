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

package com.sun.faces.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sun.faces.RIConstants;
import com.sun.faces.context.ExceptionHandlerFactoryImpl;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.el.ELContext;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseId;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

// Mock Object for FacesContext
public class MockFacesContext extends FacesContext {

    private static final String POST_BACK_MARKER
            = MockFacesContext.class.getName() + "_POST_BACK";

    private Severity maxSeverity;

    private Map<Object, Object> attributes = null;
    private PartialViewContext partialView = new MockPartialViewContext();

    private boolean released;

    // ------------------------------------------------------------ Constructors
    public MockFacesContext() {
        super();
        setCurrentInstance(this);
        getAttributes().put(RIConstants.CDI_BEAN_MANAGER, new MockBeanManager());
    }

    public MockFacesContext(ExternalContext externalContext) {
        setExternalContext(externalContext);
        setCurrentInstance(this);
        elContext = new MockELContext(new MockELResolver());
        elContext.putContext(FacesContext.class, this);
    }

    public MockFacesContext(ExternalContext externalContext, Lifecycle lifecycle) {
        this(externalContext);
    }

    // -------------------------------------------------------------- Properties

    @Override
    public Lifecycle getLifecycle() {
        return null;
    }

    // application
    private Application application = null;

    @Override
    public Application getApplication() {
        return (this.application);
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public Map<Object, Object> getAttributes() {

        if (null == attributes) {
            attributes = new HashMap<>();
        }

        return attributes;
    }

    // clientIdsWithMessages
    @Override
    public Iterator<String> getClientIdsWithMessages() {
        return (messages.keySet().iterator());
    }

    private PhaseId currentPhaseId = PhaseId.RESTORE_VIEW;

    @Override
    public PhaseId getCurrentPhaseId() {
        return currentPhaseId;
    }

    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId) {
        this.currentPhaseId = currentPhaseId;
    }

    private ELContext elContext = null;

    @Override
    public ELContext getELContext() {
        return (this.elContext);
    }

    public void setELContext(ELContext elContext) {
        this.elContext = elContext;
    }

    // externalContext
    private ExternalContext externalContext = null;

    @Override
    public ExternalContext getExternalContext() {
        return (this.externalContext);
    }

    public void setExternalContext(ExternalContext externalContext) {
        this.externalContext = externalContext;
    }

    // locale
    private Locale locale = null;

    public Locale getLocale() {
        return (this.locale);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    // maximumSeverity
    @Override
    public Severity getMaximumSeverity() {
        return maxSeverity;
    }

    // messages
    private Map<String, List<FacesMessage>> messages = new HashMap<>();

    @Override
    public Iterator<FacesMessage> getMessages() {
        List<FacesMessage> results = getMessageList();
        return (results.iterator());
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId) {
        List<FacesMessage> list = getMessageList(clientId);
        return (list.iterator());
    }

    @Override
    public List<FacesMessage> getMessageList() {
        List<FacesMessage> results = new ArrayList<>();
        Iterator<String> clientIds = messages.keySet().iterator();
        while (clientIds.hasNext()) {
            String clientId = clientIds.next();
            results.addAll(messages.get(clientId));
        }
        return results;
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId) {
        List<FacesMessage> list = messages.get(clientId);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    // renderKit
    @Override
    public RenderKit getRenderKit() {
        UIViewRoot vr = getViewRoot();
        if (vr == null) {
            return (null);
        }
        String renderKitId = vr.getRenderKitId();
        if (renderKitId == null) {
            return (null);
        }
        RenderKitFactory rkFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        return (rkFactory.getRenderKit(this, renderKitId));
    }

    // renderResponse
    private boolean renderResponse = false;

    @Override
    public boolean getRenderResponse() {
        return (this.renderResponse);
    }

    public void setRenderResponse(boolean renderResponse) {
        this.renderResponse = renderResponse;
    }

    // responseComplete
    private boolean responseComplete = false;

    @Override
    public boolean getResponseComplete() {
        return (this.responseComplete);
    }

    public void setResponseComplete(boolean responseComplete) {
        this.responseComplete = responseComplete;
    }

    // responseStream
    private ResponseStream responseStream = null;

    @Override
    public ResponseStream getResponseStream() {
        return (this.responseStream);
    }

    @Override
    public void setResponseStream(ResponseStream responseStream) {
        this.responseStream = responseStream;
    }

    // responseWriter
    private ResponseWriter responseWriter = null;

    @Override
    public ResponseWriter getResponseWriter() {
        return (this.responseWriter);
    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    // viewRoot
    private UIViewRoot root = null;

    @Override
    public UIViewRoot getViewRoot() {
        return (this.root);
    }

    @Override
    public void setViewRoot(UIViewRoot root) {
        this.root = root;
    }

    @Override
    public boolean isPostback() {

        Boolean postback = (Boolean) this.getAttributes().get(POST_BACK_MARKER);
        if (postback == null) {
            RenderKit rk = this.getRenderKit();
            if (rk != null) {
                postback = rk.getResponseStateManager().isPostback(this);
            } else {
                // ViewRoot hasn't been set yet, so calculate the RK
                ViewHandler vh = this.getApplication().getViewHandler();
                String rkId = vh.calculateRenderKitId(this);
                postback = RenderKitUtils.getResponseStateManager(this, rkId).isPostback(this);
            }
            this.getAttributes().put(POST_BACK_MARKER, postback);
        }

        return postback.booleanValue();

    }

    @Override
    public boolean isReleased() {
        return released;
    }

    private ExceptionHandler exceptionHandler
            = new ExceptionHandlerFactoryImpl().getExceptionHandler();

    @Override
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    // ---------------------------------------------------------- Public Methods
    @Override
    public void addMessage(String clientId, FacesMessage message) {
        if (message == null) {
            throw new NullPointerException();
        }
        if (maxSeverity == null) {
            maxSeverity = message.getSeverity();
        } else {
            Severity sev = message.getSeverity();
            if (sev.getOrdinal() > maxSeverity.getOrdinal()) {
                maxSeverity = sev;
            }
        }
        List<FacesMessage> list = messages.get(clientId);
        if (list == null) {
            list = new ArrayList<>();
            messages.put(clientId, list);
        }
        list.add(message);
    }

    @Override
    public void release() {
        released = true;
        application = null;
        externalContext = null;
        locale = null;
        messages.clear();
        renderResponse = false;
        responseComplete = false;
        responseStream = null;
        responseWriter = null;
        if (null != attributes) {
            attributes.clear();
            attributes = null;
        }
        root = null;
        setCurrentInstance(null);
    }

    @Override
    public void renderResponse() {
        this.renderResponse = true;
    }

    @Override
    public void responseComplete() {
        this.responseComplete = true;
    }

    @Override
    public PartialViewContext getPartialViewContext() {
        return partialView;
    }

    boolean validationFailed = false;

    @Override
    public void validationFailed() {
        validationFailed = true;
    }

    @Override
    public boolean isValidationFailed() {
        return validationFailed;
    }
}
