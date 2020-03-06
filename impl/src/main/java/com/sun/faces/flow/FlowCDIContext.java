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

package com.sun.faces.flow;

import com.sun.faces.RIConstants;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.FlowScoped;
import jakarta.faces.lifecycle.ClientWindow;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

public class FlowCDIContext implements Context, Serializable {

    private static final long serialVersionUID = -7144653402477623609L;
    private static final String FLOW_SCOPE_MAP_KEY = RIConstants.FACES_PREFIX + "FLOW_SCOPE_MAP";
    private static final Logger LOGGER = FacesLogger.FLOW.getLogger();

    private transient Map<Contextual<?>, FlowBeanInfo> flowIds;

    static class FlowBeanInfo {
        String definingDocumentId;
        String id;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FlowBeanInfo other = (FlowBeanInfo) obj;
            if ((definingDocumentId == null) ? (other.definingDocumentId != null) : !definingDocumentId.equals(other.definingDocumentId)) {
                return false;
            }
            if ((id == null) ? (other.id != null) : !id.equals(other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (definingDocumentId != null ? definingDocumentId.hashCode() : 0);
            hash = 79 * hash + (id != null ? id.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "FlowBeanInfo{" + "definingDocumentId=" + definingDocumentId + ", id=" + id + '}';
        }

    }

    // This should be vended from a factory for decoration purposes.

    FlowCDIContext(Map<Contextual<?>, FlowBeanInfo> flowIds) {
        this.flowIds = new ConcurrentHashMap<>(flowIds);
    }

    private static final String PER_SESSION_BEAN_MAP_LIST = FlowCDIContext.class.getPackage().getName() + ".PER_SESSION_BEAN_MAP_LIST";
    private static final String PER_SESSION_CREATIONAL_LIST = FlowCDIContext.class.getPackage().getName() + ".PER_SESSION_CREATIONAL_LIST";

    // -------------------------------------------------------- Private Methods

    // <editor-fold defaultstate="collapsed" desc="Private helpers">

    /*
     * Encapsulate access to the two maps we need to provide.
     *
     */
    private static class FlowScopeMapHelper {
        // <editor-fold defaultstate="collapsed">
        private transient String flowBeansForClientWindowKey;
        private transient String creationalForClientWindowKey;
        private transient final Map<String, Object> sessionMap;

        private FlowScopeMapHelper(FacesContext facesContext) {
            ExternalContext extContext = facesContext.getExternalContext();
            sessionMap = extContext.getSessionMap();

            Flow currentFlow = getCurrentFlow(facesContext);
            int currentFlowDepth = FlowHandlerImpl.getFlowStack(facesContext).getCurrentFlowDepth();

            generateKeyForCDIBeansBelongToAFlow(facesContext, currentFlow, currentFlowDepth);
        }

        private FlowScopeMapHelper(FacesContext facesContext, Flow flow, int flowDepth) {
            ExternalContext extContext = facesContext.getExternalContext();
            sessionMap = extContext.getSessionMap();

            generateKeyForCDIBeansBelongToAFlow(facesContext, flow, flowDepth);
        }

        private void generateKeyForCDIBeansBelongToAFlow(FacesContext facesContext, Flow flow, int flowDepth) {
            if (null != flow) {
                ClientWindow curWindow = facesContext.getExternalContext().getClientWindow();
                if (null == curWindow) {
                    throw new IllegalStateException("Unable to obtain current ClientWindow.  Is the ClientWindow feature enabled?");
                }

                final String clientWindow = flow.getClientWindowFlowId(curWindow);

                flowBeansForClientWindowKey = clientWindow + ":" + flowDepth + "_beans";
                creationalForClientWindowKey = clientWindow + ":" + flowDepth + "_creational";

            } else {
                flowBeansForClientWindowKey = creationalForClientWindowKey = null;
            }
        }

        private void createMaps() {
            getFlowScopedBeanMapForCurrentFlow();
            getFlowScopedCreationalMapForCurrentFlow();
        }

        private boolean isFlowExists() {
            return (null != flowBeansForClientWindowKey && null != creationalForClientWindowKey);
        }

        public String getCreationalForClientWindowKey() {
            return creationalForClientWindowKey;
        }

        public String getFlowBeansForClientWindowKey() {
            return flowBeansForClientWindowKey;
        }

        private Map<String, Object> getFlowScopedBeanMapForCurrentFlow() {
            if (null == flowBeansForClientWindowKey && null == creationalForClientWindowKey) {
                return Collections.emptyMap();
            }
            Map<String, Object> result;
            result = (Map<String, Object>) sessionMap.get(flowBeansForClientWindowKey);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                sessionMap.put(flowBeansForClientWindowKey, result);
                ensureBeanMapCleanupOnSessionDestroyed(sessionMap, flowBeansForClientWindowKey);
            }
            return result;
        }

        private Map<String, CreationalContext<?>> getFlowScopedCreationalMapForCurrentFlow() {
            if (null == flowBeansForClientWindowKey && null == creationalForClientWindowKey) {
                return Collections.emptyMap();
            }
            Map<String, CreationalContext<?>> result;
            result = (Map<String, CreationalContext<?>>) sessionMap.get(creationalForClientWindowKey);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                sessionMap.put(creationalForClientWindowKey, result);
                ensureCreationalCleanupOnSessionDestroyed(sessionMap, creationalForClientWindowKey);
            }
            return result;
        }

        private void updateSession() {
            if (null == flowBeansForClientWindowKey && null == creationalForClientWindowKey) {
                return;
            }
            sessionMap.put(flowBeansForClientWindowKey, getFlowScopedBeanMapForCurrentFlow());
            sessionMap.put(creationalForClientWindowKey, getFlowScopedCreationalMapForCurrentFlow());
            Object obj = sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
            if (null != obj) {
                sessionMap.put(PER_SESSION_BEAN_MAP_LIST, obj);
            }
            obj = sessionMap.get(PER_SESSION_CREATIONAL_LIST);
            if (null != obj) {
                sessionMap.put(PER_SESSION_CREATIONAL_LIST, obj);
            }
        }
        // </editor-fold>
    }

    private static void ensureBeanMapCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String flowBeansForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_BEAN_MAP_LIST, beanMapList);
        }
        beanMapList.add(flowBeansForClientWindow);
    }

    private static void ensureCreationalCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String creationalForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_CREATIONAL_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_CREATIONAL_LIST, beanMapList);
        }
        beanMapList.add(creationalForClientWindow);
    }

    @SuppressWarnings({ "FinalPrivateMethod" })
    private final void assertNotReleased() {
        if (!isActive()) {
            throw new IllegalStateException();
        }
    }

    private Flow getCurrentFlow() {
        Flow result = null;

        FacesContext context = FacesContext.getCurrentInstance();
        result = getCurrentFlow(context);

        return result;
    }

    private static Flow getCurrentFlow(FacesContext context) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }

        Flow result = flowHandler.getCurrentFlow(context);

        return result;

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Called from code not related to flow">

    /*
     * Called from WebappLifecycleListener.sessionDestroyed()
     */

    public static void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();

        List<String> beanMapList = (List<String>) session.getAttribute(PER_SESSION_BEAN_MAP_LIST);
        if (null != beanMapList) {
            for (String cur : beanMapList) {
                Map<Contextual<?>, Object> beanMap = (Map<Contextual<?>, Object>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_BEAN_MAP_LIST);
            beanMapList.clear();
        }

        List<String> creationalList = (List<String>) session.getAttribute(PER_SESSION_CREATIONAL_LIST);
        if (null != creationalList) {
            for (String cur : creationalList) {
                Map<Contextual<?>, CreationalContext<?>> beanMap = (Map<Contextual<?>, CreationalContext<?>>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_CREATIONAL_LIST);
            creationalList.clear();
        }

    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Called from code related to flow">

    static Map<Object, Object> getCurrentFlowScopeAndUpdateSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(facesContext);
        return getCurrentFlowScopeAndUpdateSession(mapHelper);

    }

    private static Map<Object, Object> getCurrentFlowScopeAndUpdateSession(FlowScopeMapHelper mapHelper) {
        Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentFlow();
        Map<Object, Object> result = null;
        if (mapHelper.isFlowExists()) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(FLOW_SCOPE_MAP_KEY);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                flowScopedBeanMap.put(FLOW_SCOPE_MAP_KEY, result);
            }
        }
        mapHelper.updateSession();
        return result;
    }

    static void flowExited(Flow currentFlow, int depth) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(facesContext, currentFlow, depth);
        Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentFlow();
        Map<String, CreationalContext<?>> creationalMap = mapHelper.getFlowScopedCreationalMapForCurrentFlow();
        assert (!flowScopedBeanMap.isEmpty());
        assert (!creationalMap.isEmpty());
        BeanManager beanManager = Util.getCdiBeanManager(facesContext);

        for (Entry<String, Object> entry : flowScopedBeanMap.entrySet()) {
            String passivationCapableId = entry.getKey();
            if (FLOW_SCOPE_MAP_KEY.equals(passivationCapableId)) {
                continue;
            }
            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
            Object bean = entry.getValue();
            CreationalContext creational = creationalMap.get(passivationCapableId);

            owner.destroy(bean, creational);
        }

        flowScopedBeanMap.clear();
        creationalMap.clear();

        mapHelper.updateSession();

        if (Util.isCdiOneOneOrLater(facesContext)) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName("com.sun.faces.flow.FlowCDIEventFireHelperImpl");
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }

            if (null != flowCDIEventFireHelperImplClass) {
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
                    FlowCDIEventFireHelper eventHelper = (FlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
                    eventHelper.fireDestroyedEvent(currentFlow);
                }
            }
        }
    }

    static void flowEntered() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(facesContext);
        mapHelper.createMaps();

        getCurrentFlowScopeAndUpdateSession(mapHelper);

        if (Util.isCdiOneOneOrLater(facesContext)) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName("com.sun.faces.flow.FlowCDIEventFireHelperImpl");
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }
            if (null != flowCDIEventFireHelperImplClass) {
                BeanManager beanManager = Util.getCdiBeanManager(facesContext);
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
                    FlowCDIEventFireHelper eventHelper = (FlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
                    eventHelper.fireInitializedEvent(getCurrentFlow(facesContext));
                }
            }
        }
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="spi.Context implementation">

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(facesContext);
        T result = get(mapHelper, contextual);

        if (null == result) {
            Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentFlow();
            Map<String, CreationalContext<?>> creationalMap = mapHelper.getFlowScopedCreationalMapForCurrentFlow();

            String passivationCapableId = ((PassivationCapable) contextual).getId();

            synchronized (flowScopedBeanMap) {
                result = (T) flowScopedBeanMap.get(passivationCapableId);
                if (null == result) {

                    FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();

                    if (null == flowHandler) {
                        return null;
                    }

                    FlowBeanInfo fbi = flowIds.get(contextual);
                    if (fbi != null && !flowHandler.isActive(facesContext, fbi.definingDocumentId, fbi.id)) {
                        throw new ContextNotActiveException("Request to activate bean in flow '" + fbi + "', but that flow is not active.");
                    }

                    result = contextual.create(creational);

                    if (null != result) {
                        flowScopedBeanMap.put(passivationCapableId, result);
                        creationalMap.put(passivationCapableId, creational);
                        mapHelper.updateSession();
                    }
                }
            }
        }
        mapHelper = null;

        return result;

    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("FlowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(FacesContext.getCurrentInstance());
        T result = get(mapHelper, contextual);
        mapHelper = null;

        return result;
    }

    private <T> T get(FlowScopeMapHelper mapHelper, Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("FlowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        String passivationCapableId = ((PassivationCapable) contextual).getId();
        return (T) mapHelper.getFlowScopedBeanMapForCurrentFlow().get(passivationCapableId);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return FlowScoped.class;
    }

    @Override
    public boolean isActive() {
        return null != getCurrentFlow();
    }

    void beforeShutdown(@Observes final BeforeShutdown event, BeanManager beanManager) {
    }

    // </editor-fold>

}
