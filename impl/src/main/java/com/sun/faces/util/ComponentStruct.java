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

package com.sun.faces.util;

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

    private String action;
    private String facetName;
    private String parentClientId;
    private String clientId;
    private String id;

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
    }

    @Override
    public Object saveState(FacesContext ctx) {
        if (ctx == null) {
            throw new NullPointerException();
        }

        Object[] state = new Object[5];
        state[0] = action;
        state[1] = parentClientId;
        state[2] = clientId;
        state[3] = id;
        state[4] = facetName;

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

}
