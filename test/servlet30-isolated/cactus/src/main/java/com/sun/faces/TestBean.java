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

package com.sun.faces;

import com.sun.faces.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELException;                                                                                                  
public class TestBean extends 
        com.sun.faces.cactus.TestBean {

    public TestBean() {
    }

    protected CustomerBean customerBean;
    public CustomerBean getCustomerBean() {
	return customerBean;
    }

    public void setCustomerBean(CustomerBean newCustomerBean) {
	customerBean = newCustomerBean;
    }

}
