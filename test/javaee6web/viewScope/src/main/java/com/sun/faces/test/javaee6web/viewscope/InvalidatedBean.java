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

package com.sun.faces.test.javaee6web.viewscope;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * A ViewScoped bean testing session invalidation functionality.
 */
@Named(value = "invalidatedBean")
@ViewScoped
public class InvalidatedBean implements Serializable {
    private static final long serialVersionUID = -4803754563990391919L;
    
    /**
     * Stores the text.
     */
    private String text;

    /**
     * Constructor.
     */
    public InvalidatedBean() {
        this.text = "This is from the constructor";
    }

    /**
     * Post-construct.
     *
     */
    @PostConstruct
    public void init() {
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().remove("invalidated");
        this.text = "This is from the @PostConstruct";
    }

    /**
     * Pre-destroy
     */
    @PreDestroy
    public void destroy() {
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put("invalidated", true);
    }

    /**
     * Get the text.
     * 
     * @return the text.
     */
    public String getText() {
        return this.text;
    }
}
