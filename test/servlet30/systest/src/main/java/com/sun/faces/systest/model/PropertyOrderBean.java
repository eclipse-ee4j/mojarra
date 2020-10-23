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

package com.sun.faces.systest.model;

import java.util.ArrayList;

public class PropertyOrderBean extends Object {

    protected String one;
    public String getOne() {
	return one;
    }

    public void setOne(String newOne) {
	one = newOne;
	order = order + " " + one;
    }

    protected String two;
    public String getTwo() {
	return two;
    }

    public void setTwo(String newTwo) {
	two = newTwo;
	order = order + " " + two;
    }

    protected String three;
    public String getThree() {
	return three;
    }

    public void setThree(String newThree) {
	three = newThree;
	order = order + " " + three;
    }

    protected String four;
    public String getFour() {
	return four;
    }

    public void setFour(String newFour) {
	four = newFour;
	order = order + " " + four;
    }

    
    protected String order = "";
    public String getOrder() {
	return order;
    }

    protected ArrayList listProperty = new ArrayList();

    public ArrayList getListProperty() {
	return listProperty;
    }

    public void setListProperty(ArrayList newListProperty) {
	listProperty = newListProperty;
    }



}
