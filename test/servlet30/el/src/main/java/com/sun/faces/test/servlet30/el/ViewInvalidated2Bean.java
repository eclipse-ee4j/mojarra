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

package com.sun.faces.test.servlet30.el;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * A ViewScoped bean testing session invalidation functionality.
 */
@ManagedBean(name = "viewInvalidated2Bean")
@ViewScoped
public class ViewInvalidated2Bean {

    /**
     * Stores the invalidated attribute name.
     */
    private static final String INVALIDATED_ATTRIBUTE = "com.sun.faces.test.servlet30.el.Invalidated2";
    /**
     * Stores the local count.
     */
    private int count;

    /**
     * Constructor.
     */
    public ViewInvalidated2Bean() {
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().remove(INVALIDATED_ATTRIBUTE);
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put(INVALIDATED_ATTRIBUTE, false);
    }

    /**
     * Action that invalidates the session.
     */
    public String doInvalidate() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
            FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put(INVALIDATED_ATTRIBUTE, true);
        }
        return "";
    }

    /**
     * Get the count.
     *
     * @return the count.
     */
    public int getCount() {
        return count;
    }
}
