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

package com.sun.faces.cdi.clientwindow;

import java.io.Serializable;

/**
 * An object used by ClientWindowScopeContext to keep track of contextual and creational context.
 */
class ClientWindowScopeContextObject<T> implements Serializable {
    private static final long serialVersionUID = 302829795078365733L;

    private String passivationCapableId;

    /**
     * The actual Contextual Instance in the context
     */
    private T contextualInstance;

    public ClientWindowScopeContextObject(String passivationCapableId, T contextualInstance) {
        this.passivationCapableId = passivationCapableId;
        this.contextualInstance = contextualInstance;
    }

    public String getPassivationCapableId() {
        return passivationCapableId;
    }

    public void setPassivationCapableId(String passivationCapableId) {
        this.passivationCapableId = passivationCapableId;
    }

    public T getContextualInstance() {
        return contextualInstance;
    }

    public void setContextualInstance(T contextualInstance) {
        this.contextualInstance = contextualInstance;
    }
}
