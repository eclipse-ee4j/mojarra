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

package com.sun.faces.flow;

import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.util.Util.getCdiBeanManager;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.faces.RIConstants;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.FlowScoped;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

public class FlowCDIContext implements Context, Serializable {

    private static final long serialVersionUID = -7144653402477623609L;
    private static final String FLOW_SCOPE_MAP_KEY = RIConstants.FACES_PREFIX + "FLOW_SCOPE_MAP";

    private transient Map<Contextual<?>, FlowBeanInfo> flowIds;

    static class FlowBeanInfo {

        String definingDocumentId;
        String id;

        public FlowBeanInfo(String definingDocumentId, String id) {
            this.definingDocumentId = definingDocumentId;
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FlowBeanInfo other = (FlowBeanInfo) obj;
            if (definingDocumentId == null ? other.definingDocumentId != null : !definingDocumentId.equals(other.definingDocumentId)) {
                return false;
            }
            if (id == null ? other.id != null : !id.equals(other.id)) {
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


    /*
     * Encapsulate access to the two maps we need to provide.
     *
     */
    private static class FlowScopeMapHelper {
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
            return null != flowBeansForClientWindowKey && null != creationalForClientWindowKey;
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
            if (flowBeansForClientWindowKey == null && creationalForClientWindowKey == null) {
                return;
            }

            sessionMap.put(flowBeansForClientWindowKey, getFlowScopedBeanMapForCurrentFlow());
            sessionMap.put(creationalForClientWindowKey, getFlowScopedCreationalMapForCurrentFlow());

            Object obj = sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
            if (obj != null) {
                sessionMap.put(PER_SESSION_BEAN_MAP_LIST, obj);
            }

            obj = sessionMap.get(PER_SESSION_CREATIONAL_LIST);
            if (obj != null) {
                sessionMap.put(PER_SESSION_CREATIONAL_LIST, obj);
            }
        }
    }

    private static void ensureBeanMapCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String flowBeansForClientWindow) {
        @SuppressWarnings("unchecked")
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
        if (beanMapList == null) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_BEAN_MAP_LIST, beanMapList);
        }

        beanMapList.add(flowBeansForClientWindow);
    }

    private static void ensureCreationalCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String creationalForClientWindow) {
        @SuppressWarnings("unchecked")
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_CREATIONAL_LIST);
        if (beanMapList == null) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_CREATIONAL_LIST, beanMapList);
        }

        beanMapList.add(creationalForClientWindow);
    }

    private final void assertNotReleased() {
        if (!isActive()) {
            throw new IllegalStateException();
        }
    }

    private Flow getCurrentFlow() {
        return getCurrentFlow(FacesContext.getCurrentInstance());
    }

    private static Flow getCurrentFlow(FacesContext context) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (flowHandler == null) {
            return null;
        }

        return flowHandler.getCurrentFlow(context);
    }


    /*
     * Called from WebappLifecycleListener.sessionDestroyed()
     */
    @SuppressWarnings("unchecked")
    public static void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();

        List<String> beanMapList = (List<String>) session.getAttribute(PER_SESSION_BEAN_MAP_LIST);
        if (beanMapList != null) {
            for (String beanMapName : beanMapList) {
                Map<Contextual<?>, Object> beanMap = (Map<Contextual<?>, Object>) session.getAttribute(beanMapName);
                beanMap.clear();
                session.removeAttribute(beanMapName);
            }
            session.removeAttribute(PER_SESSION_BEAN_MAP_LIST);
            beanMapList.clear();
        }

        List<String> creationalList = (List<String>) session.getAttribute(PER_SESSION_CREATIONAL_LIST);
        if (creationalList != null) {
            for (String creationalName : creationalList) {
                Map<Contextual<?>, CreationalContext<?>> creationalMap = (Map<Contextual<?>, CreationalContext<?>>) session.getAttribute(creationalName);
                creationalMap.clear();
                session.removeAttribute(creationalName);
            }
            session.removeAttribute(PER_SESSION_CREATIONAL_LIST);
            creationalList.clear();
        }

    }

    static Map<Object, Object> getCurrentFlowScopeAndUpdateSession() {
        return getCurrentFlowScopeAndUpdateSession(new FlowScopeMapHelper(FacesContext.getCurrentInstance()));

    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> getCurrentFlowScopeAndUpdateSession(FlowScopeMapHelper mapHelper) {
        Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentFlow();
        Map<Object, Object> result = null;
        if (mapHelper.isFlowExists()) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(FLOW_SCOPE_MAP_KEY);
            if (result == null) {
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

        BeanManager beanManager = getCdiBeanManager(facesContext);

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
        getBeanReference(beanManager, FlowCDIEventFireHelperImpl.class).fireDestroyedEvent(currentFlow);
    }

    static void flowEntered() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(facesContext);
        mapHelper.createMaps();

        getCurrentFlowScopeAndUpdateSession(mapHelper);
        getBeanReference(facesContext, FlowCDIEventFireHelperImpl.class).fireInitializedEvent(getCurrentFlow(facesContext));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        FlowScopeMapHelper mapHelper = new FlowScopeMapHelper(facesContext);
        T contextualInstance = get(mapHelper, contextual);

        if (contextualInstance == null) {
            Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentFlow();
            Map<String, CreationalContext<?>> creationalMap = mapHelper.getFlowScopedCreationalMapForCurrentFlow();

            String passivationCapableId = ((PassivationCapable) contextual).getId();

            synchronized (flowScopedBeanMap) {
                contextualInstance = (T) flowScopedBeanMap.get(passivationCapableId);
                if (contextualInstance == null) {

                    FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
                    if (flowHandler == null) {
                        return null;
                    }

                    FlowBeanInfo flowBeanInfo = flowIds.get(contextual);
                    if (flowBeanInfo != null && !flowHandler.isActive(facesContext, flowBeanInfo.definingDocumentId, flowBeanInfo.id)) {
                        throw new ContextNotActiveException("Request to activate bean in flow '" + flowBeanInfo + "', but that flow is not active.");
                    }

                    contextualInstance = contextual.create(creational);

                    if (contextualInstance != null) {
                        flowScopedBeanMap.put(passivationCapableId, contextualInstance);
                        creationalMap.put(passivationCapableId, creational);
                        mapHelper.updateSession();
                    }
                }
            }
        }

        return contextualInstance;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("FlowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        return get(new FlowScopeMapHelper(FacesContext.getCurrentInstance()), contextual);
    }

    @SuppressWarnings("unchecked")
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
        return getCurrentFlow() != null;
    }

    void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) {
    }

}
