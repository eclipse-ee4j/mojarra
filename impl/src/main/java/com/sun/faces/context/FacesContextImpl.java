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

package com.sun.faces.context;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.cdi.CdiExtension;
import com.sun.faces.el.ELContextImpl;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.ELContextEvent;
import jakarta.el.ELContextListener;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.PartialViewContextFactory;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseId;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

public class FacesContextImpl extends FacesContext {

    private static final String POST_BACK_MARKER = FacesContextImpl.class.getName() + "_POST_BACK";

    // Queried by InjectionFacesContextFactory
    private static final ThreadLocal<FacesContext> DEFAULT_FACES_CONTEXT = new ThreadLocal<>();

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();

    private boolean released;

    // BE SURE TO ADD NEW IVARS TO THE RELEASE METHOD
    private ResponseStream responseStream = null;
    private ResponseWriter responseWriter = null;
    private ExternalContext externalContext = null;
    private Lifecycle lifecycle;
    private Application application = null;
    private UIViewRoot viewRoot = null;
    private ELContext elContext = null;
    private RenderKitFactory rkFactory;
    private RenderKit lastRk;
    private String lastRkId;
    private Severity maxSeverity;
    private boolean renderResponse = false;
    private boolean responseComplete = false;
    private boolean validationFailed = false;
    private Map<Object, Object> attributes;
    private List<String> resourceLibraryContracts;
    private PhaseId currentPhaseId;
    private PartialViewContext partialViewContext = null;
    private ExceptionHandler exceptionHandler = null;

    /**
     * Store mapping of clientId to ArrayList of FacesMessage instances. The null key is used to represent FacesMessage
     * instances that are not associated with a clientId instance.
     */
    private Map<String, List<FacesMessage>> componentMessageLists;

    public FacesContextImpl(ExternalContext ec, Lifecycle lifecycle) {
        Util.notNull("ec", ec);
        Util.notNull("lifecycle", lifecycle);
        externalContext = ec;
        this.lifecycle = lifecycle;
        setCurrentInstance(this);
        DEFAULT_FACES_CONTEXT.set(this);
        rkFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    }

    // ---------------------------------------------- Methods from FacesContext

    /**
     * @see jakarta.faces.context.FacesContext#getLifecycle()
     */
    @Override
    public Lifecycle getLifecycle() {
        assertNotReleased();
        return lifecycle;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getExternalContext()
     */
    @Override
    public ExternalContext getExternalContext() {
        assertNotReleased();
        return externalContext;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getApplication()
     */
    @Override
    public Application getApplication() {
        assertNotReleased();
        if (null != application) {
            return application;
        }
        ApplicationFactory aFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = aFactory.getApplication();
        assert null != application;
        return application;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getExceptionHandler()
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * @see jakarta.faces.context.FacesContext#setExceptionHandler(jakarta.faces.context.ExceptionHandler)
     */
    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getPartialViewContext()
     */
    @Override
    public PartialViewContext getPartialViewContext() {

        assertNotReleased();
        if (partialViewContext == null) {
            PartialViewContextFactory f = (PartialViewContextFactory) FactoryFinder.getFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
            partialViewContext = f.getPartialViewContext(FacesContext.getCurrentInstance());
        }
        return partialViewContext;

    }

    /**
     * @see jakarta.faces.context.FacesContext#isPostback()
     */
    @Override
    public boolean isPostback() {

        assertNotReleased();
        Boolean postback = (Boolean) getAttributes().get(POST_BACK_MARKER);
        if (postback == null) {
            RenderKit rk = getRenderKit();
            if (rk != null) {
                postback = rk.getResponseStateManager().isPostback(this);
            } else {
                // ViewRoot hasn't been set yet, so calculate the RK
                ViewHandler vh = getApplication().getViewHandler();
                String rkId = vh.calculateRenderKitId(this);
                postback = RenderKitUtils.getResponseStateManager(this, rkId).isPostback(this);
            }
            getAttributes().put(POST_BACK_MARKER, postback);
        }

        return postback;

    }

    /**
     * @see jakarta.faces.context.FacesContext#isReleased()
     */
    @Override
    public boolean isReleased() {
        return released;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getAttributes()
     */
    @Override
    public Map<Object, Object> getAttributes() {

        assertNotReleased();
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;

    }

    /**
     * @see jakarta.faces.context.FacesContext#getELContext()
     */
    @Override
    public ELContext getELContext() {
        assertNotReleased();

        if (elContext == null) {
            elContext = new ELContextImpl(FacesContext.getCurrentInstance());

            ELContextListener[] listeners = getApplication().getELContextListeners();
            if (listeners.length > 0) {
                ELContextEvent event = new ELContextEvent(elContext);
                for (ELContextListener listener : listeners) {
                    listener.contextCreated(event);
                }
            }
        }

        return elContext;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getClientIdsWithMessages()
     */
    @Override
    public Iterator<String> getClientIdsWithMessages() {
        assertNotReleased();
        return componentMessageLists == null ? Collections.emptyIterator() : componentMessageLists.keySet().iterator();
    }

    /**
     * @see jakarta.faces.context.FacesContext#getMaximumSeverity()
     */
    @Override
    public Severity getMaximumSeverity() {
        assertNotReleased();
        Severity result = null;
        if (componentMessageLists != null && !componentMessageLists.isEmpty()) {
            for (Iterator<FacesMessage> i = new ComponentMessagesIterator(componentMessageLists); i.hasNext();) {
                Severity severity = i.next().getSeverity();
                if (result == null || severity.compareTo(result) > 0) {
                    result = severity;
                }
                if (result == FacesMessage.SEVERITY_FATAL) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getMessageList()
     */
    @Override
    public List<FacesMessage> getMessageList() {

        assertNotReleased();

        if (null == componentMessageLists) {
            return Collections.unmodifiableList(Collections.<FacesMessage>emptyList());
        } else {
            List<FacesMessage> messages = new ArrayList<>();
            for (List<FacesMessage> list : componentMessageLists.values()) {
                messages.addAll(list);
            }
            return Collections.unmodifiableList(messages);
        }

    }

    /**
     * @see jakarta.faces.context.FacesContext#getMessageList(String)
     */
    @Override
    public List<FacesMessage> getMessageList(String clientId) {

        assertNotReleased();

        if (null == componentMessageLists) {
            return Collections.unmodifiableList(Collections.<FacesMessage>emptyList());
        } else {
            List<FacesMessage> list = componentMessageLists.get(clientId);
            return Collections.unmodifiableList(list != null ? list : Collections.<FacesMessage>emptyList());
        }

    }

    /**
     * @see jakarta.faces.context.FacesContext#getMessages()
     */
    @Override
    public Iterator<FacesMessage> getMessages() {
        assertNotReleased();
        if (null == componentMessageLists) {
            return Collections.emptyIterator();
        }

        if (componentMessageLists.size() > 0) {
            return new ComponentMessagesIterator(componentMessageLists);
        } else {
            return Collections.emptyIterator();
        }
    }

    /**
     * @see FacesContext#getMessages(String)
     */
    @Override
    public Iterator<FacesMessage> getMessages(String clientId) {
        assertNotReleased();

        // If no messages have been enqueued at all,
        // return an empty List Iterator
        if (null == componentMessageLists) {
            return Collections.emptyIterator();
        }

        List<FacesMessage> list = componentMessageLists.get(clientId);
        if (list == null) {
            return Collections.emptyIterator();
        }
        return list.iterator();
    }

    /**
     * @see jakarta.faces.context.FacesContext#getRenderKit()
     */
    @Override
    public RenderKit getRenderKit() {
        assertNotReleased();
        UIViewRoot vr = getViewRoot();
        if (vr == null) {
            return null;
        }
        String renderKitId = vr.getRenderKitId();

        if (renderKitId == null) {
            return null;
        }

        if (renderKitId.equals(lastRkId)) {
            return lastRk;
        } else {
            lastRk = rkFactory.getRenderKit(this, renderKitId);
            if (lastRk == null) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Unable to locate renderkit " + "instance for render-kit-id {0}.  Using {1} instead.",
                            new String[] { renderKitId, RenderKitFactory.HTML_BASIC_RENDER_KIT });
                }
            }
            lastRkId = renderKitId;
            return lastRk;
        }
    }

    /**
     * @see jakarta.faces.context.FacesContext#getResponseStream()
     */
    @Override
    public ResponseStream getResponseStream() {
        assertNotReleased();
        return responseStream;
    }

    /**
     * @see FacesContext#setResponseStream(jakarta.faces.context.ResponseStream)
     */
    @Override
    public void setResponseStream(ResponseStream responseStream) {
        assertNotReleased();
        Util.notNull("responseStrean", responseStream);
        this.responseStream = responseStream;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getViewRoot()
     */
    @Override
    public UIViewRoot getViewRoot() {
        assertNotReleased();
        return viewRoot;
    }

    /**
     * @see FacesContext#setViewRoot(jakarta.faces.component.UIViewRoot)
     */
    @Override
    public void setViewRoot(UIViewRoot root) {
        assertNotReleased();
        Util.notNull("root", root);

        if (viewRoot != null && !viewRoot.equals(root)) {
            Map<String, Object> viewMap = viewRoot.getViewMap(false);
            if (viewMap != null) {
                viewRoot.getViewMap().clear();
            }
            RequestStateManager.clearAttributesOnChangeOfView(this);
        }

        viewRoot = root;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getResponseWriter()
     */
    @Override
    public ResponseWriter getResponseWriter() {
        assertNotReleased();
        return responseWriter;
    }

    /**
     * @see FacesContext#setResponseWriter(jakarta.faces.context.ResponseWriter)
     */
    @Override
    public void setResponseWriter(ResponseWriter responseWriter) {
        assertNotReleased();
        Util.notNull("responseWriter", responseWriter);
        this.responseWriter = responseWriter;
    }

    /**
     * @see FacesContext#addMessage(String, jakarta.faces.application.FacesMessage)
     */
    @Override
    public void addMessage(String clientId, FacesMessage message) {
        assertNotReleased();
        // Validate our preconditions
        Util.notNull("message", message);

        if (maxSeverity == null) {
            maxSeverity = message.getSeverity();
        } else {
            Severity sev = message.getSeverity();
            if (sev.getOrdinal() > maxSeverity.getOrdinal()) {
                maxSeverity = sev;
            }
        }

        if (componentMessageLists == null) {
            componentMessageLists = new LinkedHashMap<>();
        }

        // Add this message to our internal queue
        componentMessageLists.computeIfAbsent(clientId, k -> new ArrayList<>()).add(message);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Adding Message[sourceId=" + (clientId != null ? clientId : "<<NONE>>") + ",summary=" + message.getSummary() + ")");
        }

    }

    /**
     * @see jakarta.faces.context.FacesContext#getCurrentPhaseId()
     */
    @Override
    public PhaseId getCurrentPhaseId() {

        assertNotReleased();
        return currentPhaseId;

    }

    /**
     * @see jakarta.faces.context.FacesContext#setCurrentPhaseId(jakarta.faces.event.PhaseId)
     */
    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId) {

        assertNotReleased();
        this.currentPhaseId = currentPhaseId;

    }

    /**
     * @see jakarta.faces.context.FacesContext#release()
     */
    @Override
    public void release() {
        BeanManager beanManager = Util.getCdiBeanManager(this);

        released = true;
        if (externalContext != null) {
            externalContext.release();
        }
        lifecycle = null;
        externalContext = null;
        responseStream = null;
        responseWriter = null;
        componentMessageLists = null;
        renderResponse = false;
        responseComplete = false;
        validationFailed = false;
        viewRoot = null;
        maxSeverity = null;
        application = null;
        currentPhaseId = null;
        if (attributes != null) {
            attributes.clear();
            attributes = null;
        }
        if (null != resourceLibraryContracts) {
            resourceLibraryContracts.clear();
            resourceLibraryContracts = null;
        }
        if (partialViewContext != null) {
            partialViewContext.release();
        }
        partialViewContext = null;
        exceptionHandler = null;
        elContext = null;
        rkFactory = null;
        lastRk = null;
        lastRkId = null;

        // PENDING(edburns): write testcase that verifies that release
        // actually works. This will be important to keep working as
        // ivars are added and removed on this class over time.

        // Make sure to clear our ThreadLocal instance.
        setCurrentInstance(null);

        // remove our private ThreadLocal instance.
        DEFAULT_FACES_CONTEXT.remove();

        // Destroy our instance produced by FacesContextProducer.
        Set<Bean<?>> beans = beanManager.getBeans(FacesContext.class).stream().filter(bean -> CdiExtension.class.isAssignableFrom(bean.getBeanClass())).collect(toSet());
        Bean<?> bean = beanManager.resolve(beans);
        ((AlterableContext) beanManager.getContext(bean.getScope())).destroy(bean);
    }

    /**
     * @see jakarta.faces.context.FacesContext#renderResponse()
     */
    @Override
    public void renderResponse() {
        assertNotReleased();
        renderResponse = true;
    }

    /**
     * @see jakarta.faces.context.FacesContext#responseComplete()
     */
    @Override
    public void responseComplete() {
        assertNotReleased();
        responseComplete = true;
    }

    /**
     * @see jakarta.faces.context.FacesContext#validationFailed()
     */
    @Override
    public void validationFailed() {
        assertNotReleased();
        validationFailed = true;
    }

    /**
     * @see jakarta.faces.context.FacesContext#getRenderResponse()
     */
    @Override
    public boolean getRenderResponse() {
        assertNotReleased();
        return renderResponse;
    }

    @Override
    public List<String> getResourceLibraryContracts() {
        assertNotReleased();
        return null == resourceLibraryContracts ? Collections.emptyList() : resourceLibraryContracts;
    }

    @Override
    public void setResourceLibraryContracts(List<String> contracts) {
        assertNotReleased();
        if (null == contracts || contracts.isEmpty()) {
            if (null != resourceLibraryContracts) {
                resourceLibraryContracts.clear();
                resourceLibraryContracts = null;
            }
        } else {
            resourceLibraryContracts = new ArrayList<>(contracts);
        }

    }

    /**
     * @see jakarta.faces.context.FacesContext#getResponseComplete()
     */
    @Override
    public boolean getResponseComplete() {
        assertNotReleased();
        return responseComplete;
    }

    /**
     * @see jakarta.faces.context.FacesContext#isValidationFailed()
     */
    @Override
    public boolean isValidationFailed() {
        assertNotReleased();
        return validationFailed;
    }

    // --------------------------------------------------------- Public Methods

    public static FacesContext getDefaultFacesContext() {

        return DEFAULT_FACES_CONTEXT.get();

    }

    // -------------------------------------------------------- Private Methods

    private void assertNotReleased() {
        if (released) {
            throw new IllegalStateException();
        }
    }

    // ---------------------------------------------------------- Inner Classes

    private static final class ComponentMessagesIterator implements Iterator<FacesMessage> {

        private final Map<String, List<FacesMessage>> messages;
        private int outerIndex = -1;
        private final int messagesSize;
        private Iterator<FacesMessage> inner;
        private final Iterator<String> keys;

        // ------------------------------------------------------- Constructors

        ComponentMessagesIterator(Map<String, List<FacesMessage>> messages) {

            this.messages = messages;
            messagesSize = messages.size();
            keys = messages.keySet().iterator();

        }

        // ---------------------------------------------- Methods from Iterator

        @Override
        public boolean hasNext() {

            if (outerIndex == -1) {
                // pop our first List, if any;
                outerIndex++;
                inner = messages.get(keys.next()).iterator();

            }
            while (!inner.hasNext()) {
                outerIndex++;
                if (outerIndex < messagesSize) {
                    inner = messages.get(keys.next()).iterator();
                } else {
                    return false;
                }
            }
            return inner.hasNext();

        }

        @Override
        public FacesMessage next() {

            if (outerIndex >= messagesSize) {
                throw new NoSuchElementException();
            }
            if (inner != null && inner.hasNext()) {
                return inner.next();
            } else {
                // call this.hasNext() to properly initialize/position 'inner'
                if (!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    return inner.next();
                }
            }

        }

        @Override
        public void remove() {

            if (outerIndex == -1) {
                throw new IllegalStateException();
            }
            inner.remove();

        }

    } // END ComponentMessagesIterator

    // The testcase for this class is TestFacesContextImpl.java
    // The testcase for this class is TestFacesContextImpl_Model.java

} // end of class FacesContextImpl
