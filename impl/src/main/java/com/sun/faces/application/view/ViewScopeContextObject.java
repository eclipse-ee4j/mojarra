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

package com.sun.faces.application.view;

import java.io.Serializable;

/**
 * An object used by ViewScopeContext to keep track of contextual and creational context.
 */
class ViewScopeContextObject implements Serializable {
    private static final long serialVersionUID = 370695657651519831L;

    private String passivationCapableId;

    /**
     * Stores the name.
     */
    private String name;

    /**
     * Constructor.
     *
     * @param passivationCapableId the return from PassivationCapable.getId().
     * @param name name of context.
     */
    public ViewScopeContextObject(String passivationCapableId, String name) {
        this.passivationCapableId = passivationCapableId;
        this.name = name;
    }

    public String getPassivationCapableId() {
        return passivationCapableId;
    }

    public void setPassivationCapableId(String passivationCapableId) {
        this.passivationCapableId = passivationCapableId;
    }

    /*
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name the name.
     */
    public void setName(String name) {
        this.name = name;
    }
}
