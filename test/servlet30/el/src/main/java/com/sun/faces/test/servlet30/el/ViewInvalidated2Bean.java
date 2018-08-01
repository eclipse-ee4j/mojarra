/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates.
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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

package com.sun.faces.test.servlet30.el;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.annotation.ApplicationMap;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 * A ViewScoped bean testing session invalidation functionality.
 */
@Named
@ViewScoped
public class ViewInvalidated2Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Stores the invalidated attribute name.
     */
    private static final String INVALIDATED_ATTRIBUTE = "com.sun.faces.test.servlet30.el.Invalidated2";

    @Inject
    private ExternalContext externalContext;

    @Inject
    @ApplicationMap
    private Map<String, Object> applicationMap;

    @PostConstruct
    public void init() {
        if (!applicationMap.containsKey(INVALIDATED_ATTRIBUTE)) {
            applicationMap.put(INVALIDATED_ATTRIBUTE, false);
        }
    }

    /**
     * Action that invalidates the session.
     */
    public String doInvalidate() {
        HttpSession session = (HttpSession) externalContext.getSession(false);
        if (session != null) {
            session.invalidate();
            applicationMap.put(INVALIDATED_ATTRIBUTE, true);
        }

        return "";
    }

    /**
     * Get the count.
     *
     * @return the count.
     */
    public int getCount() {
        return 0;
    }
}
