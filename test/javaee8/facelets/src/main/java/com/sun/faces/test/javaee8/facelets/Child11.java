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

package com.sun.faces.test.javaee8.facelets;

public class Child11 extends Child1 {

	private String test;
	private String test2;
	
	public Child11() {}
	
	public Child11(String test, String test2) {
		this.test = test;
		this.test2 = test2;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
	
	public String getTest2() {
		return test2;
	}

	public void setTest2(String test2) {
		this.test2 = test2;
	}
	
}
