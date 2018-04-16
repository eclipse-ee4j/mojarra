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

package com.sun.faces.test.servlet30.viewscopefailover;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

@WebListener
public class ViewScopeSessionAttributeListener implements HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (event.getName().equals("com.sun.faces.application.view.activeViewMaps")) {
            Integer setCount = (Integer) event.getSession().getAttribute("setCount");
            if (setCount == null) {
                setCount = 1;
            } else {
                setCount++;
            }
            event.getSession().setAttribute("setCount", setCount);
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        if (event.getName().equals("com.sun.faces.application.view.activeViewMaps")) {
            Integer setCount = (Integer) event.getSession().getAttribute("setCount");
            if (setCount == null) {
                setCount = 1;
            } else {
                setCount++;
            }
            event.getSession().setAttribute("setCount", setCount);
        }
    }
}
