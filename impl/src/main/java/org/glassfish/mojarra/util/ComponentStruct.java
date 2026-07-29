/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.util;

import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;

/**
 * Utility class to enable partial state saving of components that have been dynamically added to the view.
 */
public class ComponentStruct implements StateHolder {

    /**
     * Marker that specifies this is an ADD.
     */
    public static final String ADD = "ADD";

    /**
     * Marker that specifies this is a REMOVE.
     */
    public static final String REMOVE = "REMOVE";

    /**
     * Index which an ADD must restore its child at, or -1 when it must be appended.
     */
    public static final int APPEND = -1;

    private String action;
    private String facetName;
    private String parentClientId;
    private String clientId;
    private String id;
    private int index = APPEND;

    public ComponentStruct() {
    }

    public ComponentStruct(String action, String facetName, String clientId, String id) {
        this.action = action;
        this.facetName = facetName;
        this.clientId = clientId;
        this.id = id;
    }

    public ComponentStruct(String action, String facetName, String parentClientId, String clientId, String id) {
        this.action = action;
        this.facetName = facetName;
        this.parentClientId = parentClientId;
        this.clientId = clientId;
        this.id = id;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void restoreState(FacesContext ctx, Object state) {
        if (ctx == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        Object[] s = (Object[]) state;
        action = (String) s[0];
        parentClientId = (String) s[1];
        clientId = (String) s[2];
        id = (String) s[3];
        facetName = (String) s[4];
        index = (Integer) s[5];
    }

    @Override
    public Object saveState(FacesContext ctx) {
        if (ctx == null) {
            throw new NullPointerException();
        }

        Object[] state = new Object[6];
        state[0] = action;
        state[1] = parentClientId;
        state[2] = clientId;
        state[3] = id;
        state[4] = facetName;
        state[5] = index;

        return state;
    }

    @Override
    public void setTransient(boolean trans) {
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof ComponentStruct) {
            ComponentStruct struct = (ComponentStruct) obj;
            result = struct.clientId.equals(clientId);
        }

        return result;
    }

    /**
     * Hash code.
     *
     * @return the hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (clientId != null ? clientId.hashCode() : 0);
        return hash;
    }

    public String getAction() {
        return action;
    }

    public String getFacetName() {
        return facetName;
    }

    public String getParentClientId() {
        return parentClientId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the index within the parent's children which this action's child must be restored at, or
     * {@link #APPEND} when it must be appended.
     *
     * <p>
     * The index travels with the action rather than with the component, because the component itself does
     * not necessarily survive: a facelet-created child which was dynamically moved to another parent is
     * deleted and recreated by the facelet refresh, which loses any marker held in its attribute map.
     * </p>
     *
     * @return the index within the parent's children, or {@link #APPEND}.
     */
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
