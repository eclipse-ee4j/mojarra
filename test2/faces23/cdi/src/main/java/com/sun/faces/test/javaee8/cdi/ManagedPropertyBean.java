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

package com.sun.faces.test.javaee8.cdi;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class ManagedPropertyBean {

	public Integer getMyInteger1() {
		return 42;
	}
	
	public Integer getMyInteger2() {
		return 99;
	}
	
	public Integer getMyInteger3() {
		return 123;
	}
	
    public Map<String, String> getMyStringMap() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        return map;
    }
    
    public Map<Integer, Integer> getMyIntegerMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(9, 10);
        return map;
    }
	
}
