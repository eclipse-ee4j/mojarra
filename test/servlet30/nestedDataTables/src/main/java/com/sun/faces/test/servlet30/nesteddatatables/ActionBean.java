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

package com.sun.faces.test.servlet30.nesteddatatables;

public class ActionBean extends Object {

    public ActionBean() {
    }

    public String outerAction() {
        outerActionCallCount++;
        return null;
    }

    public String innerAction() {
        innerActionCallCount++;
        return null;
    }

    protected int outerActionCallCount = 0;

    public int getOuterActionCallCount() {
        return outerActionCallCount;
    }

    public void setOuterActionCallCount(int newOuterActionCallCount) {
        outerActionCallCount = newOuterActionCallCount;
    }

    protected int innerActionCallCount = 0;

    public int getInnerActionCallCount() {
        return innerActionCallCount;
    }

    public void setInnerActionCallCount(int newInnerActionCallCount) {
        innerActionCallCount = newInnerActionCallCount;
    }

}
