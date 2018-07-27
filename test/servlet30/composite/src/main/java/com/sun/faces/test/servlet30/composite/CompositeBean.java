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

package com.sun.faces.test.servlet30.composite;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * A session scoped bean used in some tests for composite components
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */

@Named
@RequestScoped
public class CompositeBean {

    /**
     * Stores input 1.
     */
    private String input1;

    /**
     * Get EL value.
     *
     * @return "EL value"
     */
    public String getValue() {
        return "EL value";
    }

    /**
     * Get input 1.
     *
     * @return input 1.
     */
    public String getInput1() {
        return input1;
    }

    /**
     * Set input 1.
     *
     * @param input1 input 1.
     */
    public void setInput1(String input1) {
        this.input1 = input1;
    }

    /**
     * Action that handles input 1.
     *
     * @return "input1"
     */
    public String doInput1() {
        return "input1";
    }
}
