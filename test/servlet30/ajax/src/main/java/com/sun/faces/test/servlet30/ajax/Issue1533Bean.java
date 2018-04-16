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

package com.sun.faces.test.servlet30.ajax;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "issue1533Bean")
@SessionScoped
public class Issue1533Bean implements Serializable {

    /**
     * Stores the fire state.
     */
    private String fireState;
    /**
     * Stores the VIP.
     */
    private String vip;

    /**
     * Fired through Ajax.
     *
     * @param event the event.
     * @throws AbortProcessingException when further processing needs to be
     * aborted.
     */
    public void ajaxFired(AjaxBehaviorEvent event) throws AbortProcessingException {
        setFireState(event.getComponent().getClientId() + "-" + vip);
    }

    /**
     * Get the fire state.
     *
     * @return the fire state.
     */
    public String getFireState() {
        return this.fireState;
    }

    /**
     * Get the VIP.
     *
     * @return the VIP.
     */
    public String getVip() {
        return this.vip;
    }

    /**
     * Set the VIP.
     *
     * @param vip the VIP.
     */
    public void setVip(String vip) {
        this.vip = vip;
    }

    /**
     * Get the fire state.
     *
     * @return the fire state.
     */
    public void setFireState(String fireState) {
        this.fireState = fireState;
    }
}
