/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.application.view;

import static org.glassfish.mojarra.RIConstants.CDI_BEAN_MANAGER;
import static org.glassfish.mojarra.application.view.ViewScopeManager.ACTIVE_VIEW_MAPS;
import static org.glassfish.mojarra.config.MojarraContextParam.NUMBER_OF_ACTIVE_VIEW_MAPS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.event.PostConstructViewMapEvent;

import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ViewScopeManager} registers the data store of a {@link ViewScopeMap} under the view map id which the view map holds, and connects a restored view map
 * to the data store which is still alive under its id. A restored view map whose data store is gone is detached, without registering anything until its first
 * write. With room for a single active view map, every registration evicts the previous one.
 */
class ViewScopeManagerTest extends JUnitFacesTestCaseBase {

    private ViewScopeManager viewScopeManager;

    @BeforeEach
    void setUpViewScopeManager() {
        servletContext.addInitParameter(NUMBER_OF_ACTIVE_VIEW_MAPS.getName(), "1");

        ExternalContext sessionAwareExternalContext = spy(externalContext);
        doReturn(session).when(sessionAwareExternalContext).getSession(anyBoolean());
        facesContext.setExternalContext(sessionAwareExternalContext);

        ELAwareBeanManager beanManager = mock(ELAwareBeanManager.class);
        Bean<?> eventFireHelperBean = mock(Bean.class);
        when(beanManager.getBeans(any(), any())).thenReturn(Set.of(eventFireHelperBean));
        doReturn(eventFireHelperBean).when(beanManager).resolve(any());
        when(beanManager.getReference(any(), any(), any())).thenReturn(mock(ViewScopedCDIEventFireHelperImpl.class));
        facesContext.getAttributes().put(CDI_BEAN_MANAGER, beanManager);

        viewScopeManager = ViewScopeManager.getInstance(facesContext);
    }

    @Test
    void postConstructRegistersDataStoreUnderNewIdOfViewMap() {
        ViewScopeMap viewMap = new ViewScopeMap();
        startRequest(viewMap);

        viewScopeManager.processEvent(new PostConstructViewMapEvent(facesContext, facesContext.getViewRoot()));

        String viewMapId = viewMap.getViewMapId();
        assertNotNull(viewMapId);
        assertSame(viewMap.getDataStore(), getActiveViewMaps().get(viewMapId));
    }

    @Test
    void acquireConnectsRestoredViewMapToActiveDataStore() {
        String viewMapId = registerViewMap("value");
        endRequest();

        ViewScopeMap restoredViewMap = restoredViewMap(viewMapId);
        startRequest(restoredViewMap);
        ViewScopeManager.acquireViewMap(facesContext);

        assertTrue(restoredViewMap.isConnected());
        assertEquals("value", restoredViewMap.get("bean"));
    }

    @Test
    void acquireConnectsRestoredViewMapToEvictedDataStoreWhichIsStillInUse() {
        String evictedViewMapId = registerViewMap("evicted");
        registerViewMap("active");
        assertFalse(getActiveViewMaps().containsKey(evictedViewMapId));

        ViewScopeMap restoredViewMap = restoredViewMap(evictedViewMapId);
        startRequest(restoredViewMap);
        ViewScopeManager.acquireViewMap(facesContext);

        assertTrue(restoredViewMap.isConnected());
        assertEquals("evicted", restoredViewMap.get("bean"));
    }

    @Test
    void acquireDetachesRestoredViewMapWhoseDataStoreIsGone() {
        String activeViewMapId = registerViewMap("active");
        endRequest();

        ViewScopeMap restoredViewMap = restoredViewMap("gone");
        startRequest(restoredViewMap);
        ViewScopeManager.acquireViewMap(facesContext);

        assertNull(restoredViewMap.getViewMapId());
        assertFalse(restoredViewMap.isConnected());
        assertTrue(restoredViewMap.isEmpty());
        assertEquals(Set.of(activeViewMapId), getActiveViewMaps().keySet());
    }

    @Test
    void firstWriteToDetachedViewMapRegistersDataStoreUnderNewId() {
        registerViewMap("active");
        endRequest();
        Application application = spy(facesContext.getApplication());
        doAnswer(invocation -> {
            viewScopeManager.processEvent(new PostConstructViewMapEvent(facesContext, invocation.getArgument(3)));
            return null;
        }).when(application).publishEvent(any(), eq(PostConstructViewMapEvent.class), eq(UIViewRoot.class), any());
        facesContext.setApplication(application);

        ViewScopeMap restoredViewMap = restoredViewMap("gone");
        startRequest(restoredViewMap);
        ViewScopeManager.acquireViewMap(facesContext);
        restoredViewMap.put("bean", "value");

        String viewMapId = restoredViewMap.getViewMapId();
        assertNotNull(viewMapId);
        assertSame(restoredViewMap.getDataStore(), getActiveViewMaps().get(viewMapId));
        verify(application).publishEvent(any(), eq(PostConstructViewMapEvent.class), eq(UIViewRoot.class), any());
    }

    @Test
    void acquireRegistersConnectedViewMapAnewWhenItsDataStoreIsGone() {
        String viewMapId = registerViewMap("value");
        endRequest();
        ViewScopeMap restoredViewMap = restoredViewMap(viewMapId);
        startRequest(restoredViewMap);
        assertEquals("value", restoredViewMap.get("bean"));
        Map<String, Object> dataStore = restoredViewMap.getDataStore();
        UIViewRoot viewRoot = facesContext.getViewRoot();

        registerViewMap("concurrent");
        endRequest();
        assertFalse(getActiveViewMaps().containsKey(viewMapId));

        startRequest(viewRoot);
        ViewScopeManager.acquireViewMap(facesContext);

        String newViewMapId = restoredViewMap.getViewMapId();
        assertNotNull(newViewMapId);
        assertNotEquals(viewMapId, newViewMapId);
        assertSame(dataStore, getActiveViewMaps().get(newViewMapId));
    }

    /**
     * Registers a new view map holding the given value in a new request which stays unfinished, hence keeps using it.
     */
    private String registerViewMap(String value) {
        ViewScopeMap viewMap = new ViewScopeMap();
        startRequest(viewMap);
        viewScopeManager.processEvent(new PostConstructViewMapEvent(facesContext, facesContext.getViewRoot()));
        viewMap.put("bean", value);
        return viewMap.getViewMapId();
    }

    private void startRequest(ViewScopeMap viewMap) {
        startRequest(new TestViewRoot(viewMap));
    }

    private void startRequest(UIViewRoot viewRoot) {
        Object beanManager = facesContext.getAttributes().get(CDI_BEAN_MANAGER);
        facesContext.getAttributes().clear();
        facesContext.getAttributes().put(CDI_BEAN_MANAGER, beanManager);
        facesContext.setViewRoot(viewRoot);
    }

    private void endRequest() {
        ViewScopeManager.releaseViewMaps(facesContext);
    }

    private ViewScopeMap restoredViewMap(String viewMapId) {
        ViewScopeMap viewMap = new ViewScopeMap();
        viewMap.restoreState(facesContext, viewMapId);
        return viewMap;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getActiveViewMaps() {
        return (Map<String, Object>) facesContext.getExternalContext().getSessionMap().get(ACTIVE_VIEW_MAPS);
    }

    /**
     * A view root holding the given view map, as {@link UIViewRoot#getViewMap(boolean)} would after obtaining or restoring it.
     */
    private static class TestViewRoot extends UIViewRoot {

        private final Map<String, Object> viewMap;

        TestViewRoot(Map<String, Object> viewMap) {
            this.viewMap = viewMap;
        }

        @Override
        public Map<String, Object> getViewMap(boolean create) {
            return viewMap;
        }

    }

}
