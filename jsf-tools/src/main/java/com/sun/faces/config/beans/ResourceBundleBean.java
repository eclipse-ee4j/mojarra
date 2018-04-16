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

package com.sun.faces.config.beans;


import java.util.Map;
import java.util.TreeMap;


/**
 * <p>Configuration bean for <code>&lt;attribute&gt; element.</p>
 */

public class ResourceBundleBean extends FeatureBean {
    /**
     * Holds value of property basename.
     */
    private String basename;

    /**
     * Getter for property basename.
     * @return Value of property basename.
     */
    public String getBasename() {

        return this.basename;
    }

    /**
     * Setter for property basename.
     * @param basename New value of property basename.
     */
    public void setBasename(String basename) {

        this.basename = basename;
    }

    /**
     * Holds value of property var.
     */
    private String var;

    /**
     * Getter for property var.
     * @return Value of property var.
     */
    public String getVar() {

        return this.var;
    }

    /**
     * Setter for property var.
     * @param var New value of property var.
     */
    public void setVar(String var) {

        this.var = var;
    }



}
