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

import static java.util.Collections.emptyMap;
import static java.util.logging.Level.WARNING;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;

/**
 * The view map which {@link UIViewRoot#getViewMap(boolean)} obtains from the CDI bean named {@link UIViewRoot#VIEW_MAP_NAME}.
 * <p>
 * Its data store is the map which {@link ViewScopeManager} registers among the active view maps of the session, under the view map id which this view map
 * holds. Its state is only that id. After a restore, it reconnects to the data store which is still alive under that id: an active view map, or an evicted view
 * map which an unfinished request is still using. When there is none, or after {@link #detach()}, reads see an empty view map, and the first write starts a new
 * data store, announced with a {@link PostConstructViewMapEvent} so that {@link ViewScopeManager} registers it under a new view map id.
 */
public class ViewScopeMap extends AbstractMap<String, Object> implements StateHolder {

    private static final Logger LOGGER = Logger.getLogger(ViewScopeMap.class.getName());

    private String viewMapId;
    private Map<String, Object> dataStore;
    private boolean announceNewDataStore;

    String getViewMapId() {
        return viewMapId;
    }

    void setViewMapId(String viewMapId) {
        this.viewMapId = viewMapId;
    }

    /**
     * Returns whether this view map has meanwhile connected to a data store.
     *
     * @return whether this view map has meanwhile connected to a data store.
     */
    boolean isConnected() {
        return dataStore != null;
    }

    /**
     * Disconnects this view map from its view map id, because the data store registered under that id is gone. The first write then starts a new data store.
     */
    void detach() {
        viewMapId = null;
        dataStore = null;
        announceNewDataStore = true;
    }

    /**
     * Connects this view map to the data store which is still alive under its view map id, if any.
     */
    void connect() {
        if (dataStore == null) {
            dataStore = getLiveDataStore();
        }
    }

    /**
     * Returns the data store of this view map, and starts a new one when there is none alive. A new data store of a restored or detached view map is announced
     * with a {@link PostConstructViewMapEvent}.
     *
     * @return the data store of this view map.
     */
    Map<String, Object> getDataStore() {
        connect();

        if (dataStore == null) {
            dataStore = new HashMap<>();

            if (announceNewDataStore) {
                viewMapId = null;
                publishPostConstructViewMapEvent();
            }
        }

        announceNewDataStore = false;
        return dataStore;
    }

    private void publishPostConstructViewMapEvent() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = context.getViewRoot();

        if (viewRoot != null && viewRoot.getViewMap(false) == this) {
            context.getApplication().publishEvent(context, PostConstructViewMapEvent.class, UIViewRoot.class, viewRoot);
        }
    }

    private Map<String, Object> getReadableDataStore() {
        connect();
        return dataStore != null ? dataStore : emptyMap();
    }

    private Map<String, Object> getLiveDataStore() {
        return viewMapId != null ? ViewScopeManager.getLiveViewMap(FacesContext.getCurrentInstance(), viewMapId) : null;
    }

    // -------------------------------------------------------- Methods from Map

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return getReadableDataStore().entrySet();
    }

    @Override
    public int size() {
        return getReadableDataStore().size();
    }

    @Override
    public boolean isEmpty() {
        return getReadableDataStore().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getReadableDataStore().containsKey(key);
    }

    @Override
    public Object get(Object key) {
        return getReadableDataStore().get(key);
    }

    @Override
    public Object put(String key, Object value) {
        FacesContext context = FacesContext.getCurrentInstance();

        if (value != null && !(value instanceof Serializable) && context != null && context.isProjectStage(ProjectStage.Development)) {
            LOGGER.log(
                WARNING, "Setting non-serializable attribute value into ViewMap: (key: {0}, value class: {1})",
                new Object[] { key, value.getClass().getName() }
            );
        }

        return getDataStore().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return getReadableDataStore().remove(key);
    }

    @Override
    public void clear() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getApplication().publishEvent(context, PreDestroyViewMapEvent.class, UIViewRoot.class, context.getViewRoot());

        getReadableDataStore().clear();
    }

    // ------------------------------------------------- Methods from StateHolder

    @Override
    public Object saveState(FacesContext context) {
        return viewMapId;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        viewMapId = (String) state;
        dataStore = null;
        announceNewDataStore = true;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        // A view map is never transient.
    }

}
