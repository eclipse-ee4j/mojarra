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

package com.sun.faces.test.servlet30.facelets;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.faces.bean.ManagedBean;

@ManagedBean(name = "repeatResetNullBean")
public class RepeatResetNullBean implements Serializable {
    private List<TestHolder> testList = Arrays.asList(new TestHolder("myString"));
    private TestHolder element = new TestHolder("myStringElement");
    public List<TestHolder> getTestList() {
        return testList;
    }
    public TestHolder getElement() {
        return element;
    }
    public void setElement(TestHolder element) {
        this.element = element;
    }
    public void setTestList(List<TestHolder> testList) {
        this.testList = testList;
    }
    public boolean isElementNullSet() {
        return element.getValue() == null;
    }
    public boolean isNullOrEmptySet() {
        return (testList.get(0).getValue() == null) ||
            (testList.get(0).getValue().length() == 0);
    }

    public void action(){}
    
    public class TestHolder implements Serializable{
        private String value;
        public TestHolder(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
