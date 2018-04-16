/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockFacesContext.java,v 1.1 2005/10/18 17:47:54 edburns Exp $
 */



package com.sun.faces.mock;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import javax.faces.FactoryFinder;
import javax.faces.webapp.PreJsf2ExceptionHandlerFactory;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ExceptionHandler;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import javax.el.ELContext;

import com.sun.faces.renderkit.RenderKitUtils;


// Mock Object for FacesContext
public class MockFacesContext extends FacesContext {

    private static final String POST_BACK_MARKER =
           MockFacesContext.class.getName() + "_POST_BACK";
  
    private Severity maxSeverity;

    private Map<Object, Object> attributes = null;
    private PartialViewContext partialView = new MockPartialViewContext();

    private boolean released;

    // ------------------------------------------------------------ Constructors


    public MockFacesContext() {
        super();
        setCurrentInstance(this);
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


    // application
    private Application application = null;
    public Application getApplication() {
        return (this.application);
    }
    public void setApplication(Application application) {
        this.application = application;
    }

    public Map<Object, Object> getAttributes() {

        if (null == attributes) {
            attributes = new HashMap<Object,Object>();
        }

        return attributes;
    }

    // clientIdsWithMessages
    public Iterator getClientIdsWithMessages() {
        return (messages.keySet().iterator());
    }

    private PhaseId currentPhaseId = PhaseId.RESTORE_VIEW;

    public PhaseId getCurrentPhaseId() {
	return currentPhaseId;
    }

    public void setCurrentPhaseId(PhaseId currentPhaseId) {
	this.currentPhaseId = currentPhaseId;
    }

    private ELContext elContext = null;
    public ELContext getELContext() {
	return (this.elContext);
    }
    
    public void setELContext(ELContext elContext) {
        this.elContext = elContext;
    }

    // externalContext
    private ExternalContext externalContext = null;
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
    public Severity getMaximumSeverity() {
       return maxSeverity;
    }


    // messages
    private Map<String,List<FacesMessage>> messages = new HashMap();
    public Iterator getMessages() {
        List<FacesMessage> results = getMessageList();
        return (results.iterator());
    }
    public Iterator getMessages(String clientId) {
	List<FacesMessage> list = getMessageList(clientId);
        return (list.iterator());
    }

    public List<FacesMessage> getMessageList() {
        ArrayList results = new ArrayList<FacesMessage>();
        Iterator clientIds = messages.keySet().iterator();
        while (clientIds.hasNext()) {
            String clientId = (String) clientIds.next();
            results.addAll((List<FacesMessage>) messages.get(clientId));
        }
	return results;
    }


    public List<FacesMessage> getMessageList(String clientId) {
        List<FacesMessage> list = messages.get(clientId);
        if (list == null) {
            list = Collections.EMPTY_LIST;
        }
	return list;
    }

    
    


    // renderKit
    public RenderKit getRenderKit() {
        UIViewRoot vr = getViewRoot();
        if (vr == null) {
            return (null);
        }
        String renderKitId = vr.getRenderKitId();
        if (renderKitId == null) {
            return (null);
        }
        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        return (rkFactory.getRenderKit(this, renderKitId));
    }


    // renderResponse
    private boolean renderResponse = false;
    public boolean getRenderResponse() {
        return (this.renderResponse);
    }
    public void setRenderResponse(boolean renderResponse) {
        this.renderResponse = renderResponse;
    }

    // responseComplete
    private boolean responseComplete = false;
    public boolean getResponseComplete() {
        return (this.responseComplete);
    }
    public void setResponseComplete(boolean responseComplete) {
        this.responseComplete = responseComplete;
    }


    // responseStream
    private ResponseStream responseStream = null;
    public ResponseStream getResponseStream() {
        return (this.responseStream);
    }
    public void setResponseStream(ResponseStream responseStream) {
        this.responseStream = responseStream;
    }


    // responseWriter
    private ResponseWriter responseWriter = null;
    public ResponseWriter getResponseWriter() {
        return (this.responseWriter);
    }
    public void setResponseWriter(ResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }


    // viewRoot
    private UIViewRoot root = null;
    public UIViewRoot getViewRoot() {
        return (this.root);
    }
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


    private ExceptionHandler exceptionHandler =
          new PreJsf2ExceptionHandlerFactory().getExceptionHandler();

    @Override
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }


    // ---------------------------------------------------------- Public Methods


    public void addMessage(String clientId, FacesMessage message){ 
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
        List list = (List) messages.get(clientId);
        if (list == null) {
            list = new ArrayList();
            messages.put(clientId, list);
        }
        list.add(message);
    }


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


    public void renderResponse() {
        this.renderResponse = true;
    }


    public void responseComplete() {
        this.responseComplete = true;
    }

    @Override
    public PartialViewContext getPartialViewContext() {
        return partialView;
    }

    boolean validationFailed = false;

    public void validationFailed() {
	validationFailed = true;
    }

    public boolean isValidationFailed() {
	return validationFailed;
    }

    
}
