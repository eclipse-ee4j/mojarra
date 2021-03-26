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

package com.sun.faces.test.servlet30_isolated.coretags;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <p>
 * Right now, just a minimal listener that sets the java.beans.Beans.DesignTime
 * property to "true" to test the bypass of the TLV and allowance of not
 * specifying non required attributes. </p>
 * <p/>
 */
public class CoreTagsListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        java.beans.Beans.setDesignTime(true);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
