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

package com.sun.faces.test.servlet30.ajax;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class Issue1581Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Issue1581Bug> bugs = new ArrayList<Issue1581Bug>();

    public Issue1581Bean() {
        bugs.add(new Issue1581Bug(false, "JAVASERVERFACES-1"));
        bugs.add(new Issue1581Bug(false, "JAVASERVERFACES-2"));
        bugs.add(new Issue1581Bug(false, "JAVASERVERFACES-3"));
        bugs.add(new Issue1581Bug(false, "JAVASERVERFACES-4"));
    }

    public void selectionChanged() {
        System.out.println("Selection: " + bugs);
    }

    public List<Issue1581Bug> getBugs() {
        return bugs;
    }

    public void setBugs(List<Issue1581Bug> bugs) {
        this.bugs = bugs;
    }
}
