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

import static org.glassfish.mojarra.application.view.ViewScopeManager.ACTIVE_VIEW_MAPS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;

import org.glassfish.mojarra.junit.CurrentFacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link ViewScopeMap} saves only its view map id, and after a restore reconnects to the data store which is registered under that id among the active view
 * maps. When that data store is no longer active, reads see an empty view map, and the first write starts a new one, announced with a
 * {@link PostConstructViewMapEvent}.
 */
class ViewScopeMapTest {

    private final FacesContext facesContext = mock(FacesContext.class);
    private final Application application = mock(Application.class);
    private TestViewRoot viewRoot;
    private final Map<String, Object> activeViewMaps = new HashMap<>();

    @BeforeEach
    void setUp() {
        ExternalContext externalContext = mock(ExternalContext.class);
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put(ACTIVE_VIEW_MAPS, activeViewMaps);
        when(externalContext.getSession(false)).thenReturn(new Object());
        when(externalContext.getSessionMap()).thenReturn(sessionMap);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(facesContext.getApplication()).thenReturn(application);
        CurrentFacesContext.set(facesContext);
        viewRoot = new TestViewRoot();
        when(facesContext.getViewRoot()).thenReturn(viewRoot);
    }

    @AfterEach
    void tearDown() {
        CurrentFacesContext.set(null);
    }

    @Test
    void stateIsViewMapId() {
        ViewScopeMap viewMap = new ViewScopeMap();
        viewMap.setViewMapId("id1");

        assertEquals("id1", viewMap.saveState(facesContext));
    }

    @Test
    void restoredViewMapReconnectsToActiveDataStore() {
        Map<String, Object> dataStore = new HashMap<>(Map.of("one", "one"));
        activeViewMaps.put("id1", dataStore);
        ViewScopeMap viewMap = restoredViewMap("id1");

        viewMap.put("two", "two");

        assertEquals("one", viewMap.get("one"));
        assertEquals("two", dataStore.get("two"));
        assertSame(dataStore, viewMap.getDataStore());
        assertEquals("id1", viewMap.getViewMapId());
        verify(application, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    void restoredViewMapWithoutActiveDataStoreReadsEmpty() {
        ViewScopeMap viewMap = restoredViewMap("id1");

        assertTrue(viewMap.isEmpty());
        assertNull(viewMap.get("one"));
        assertEquals("id1", viewMap.getViewMapId());
        verify(application, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    void firstWriteToRestoredViewMapWithoutActiveDataStoreStartsNewViewMap() {
        ViewScopeMap viewMap = restoredViewMap("id1");
        viewRoot.viewMap = viewMap;

        viewMap.put("one", "one");

        assertEquals("one", viewMap.get("one"));
        assertNull(viewMap.getViewMapId());
        verify(application).publishEvent(facesContext, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot);
    }

    @Test
    void firstWriteToRestoredViewMapOfOtherViewRootAnnouncesNothing() {
        ViewScopeMap viewMap = restoredViewMap("id1");

        viewMap.put("one", "one");

        assertEquals("one", viewMap.get("one"));
        verify(application, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    void writeToNewViewMapAnnouncesNothing() {
        ViewScopeMap viewMap = new ViewScopeMap();

        viewMap.put("one", "one");

        assertEquals("one", viewMap.get("one"));
        verify(application, never()).publishEvent(any(), any(), any(), any());
    }

    @Test
    void clearPublishesPreDestroyViewMapEvent() {
        ViewScopeMap viewMap = new ViewScopeMap();
        viewMap.put("one", "one");

        viewMap.clear();

        assertTrue(viewMap.isEmpty());
        verify(application).publishEvent(facesContext, PreDestroyViewMapEvent.class, UIViewRoot.class, viewRoot);
    }

    private ViewScopeMap restoredViewMap(String viewMapId) {
        ViewScopeMap viewMap = new ViewScopeMap();
        viewMap.restoreState(facesContext, viewMapId);
        return viewMap;
    }

    /**
     * A view root holding a given view map, as {@link UIViewRoot#getViewMap(boolean)} would after restoring it.
     */
    private static class TestViewRoot extends UIViewRoot {

        private Map<String, Object> viewMap;

        @Override
        public Map<String, Object> getViewMap(boolean create) {
            return viewMap;
        }

    }

}
