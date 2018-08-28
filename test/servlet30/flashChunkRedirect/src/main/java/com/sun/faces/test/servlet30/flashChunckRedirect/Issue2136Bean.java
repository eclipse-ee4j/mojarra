/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.flashChunckRedirect;

import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@Named
@RequestScoped
public class Issue2136Bean implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the flashValue.
     */
    private String flashValue;

    /**
     * Stores the value.
     */
    private String value;

    /**
     * Constructor.
     */
    public Issue2136Bean() {
        if (FacesContext.getCurrentInstance().getExternalContext().getFlash().get("flashValue") != null) {
            this.flashValue = FacesContext.getCurrentInstance().getExternalContext().getFlash().get("flashValue").toString();
        }
    }

    /**
     * Get the flash value.
     *
     * @return the flash value.
     */
    public String getFlashValue() {
        return this.flashValue;
    }

    /**
     * Get the value.
     *
     * @return the value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value.
     *
     * @param value the value.
     */
    public void setValue(String value) {
        this.value = value;
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("flashValue", value);
    }
}
