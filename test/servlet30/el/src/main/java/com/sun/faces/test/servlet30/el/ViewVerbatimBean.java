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

import jakarta.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

@ManagedBean(name = "viewVerbatimBean")
@ViewScoped
public class ViewVerbatimBean {

    /**
     * Initialize the bean.
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Update the time.
     *
     * @param ae the action event.
     */
    public void updateTime(ActionEvent ae) {
    }

    /**
     * Get the time.
     *
     * @return the time.
     */
    public String getTime() {
        return Long.toString(System.currentTimeMillis());
    }

    /**
     * Get the bean hash.
     *
     * @return the bean hash.
     */
    public String getBeanHash() {
        return this.toString();
    }
}
