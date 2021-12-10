/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.faces.annotation.FacesConfig;

@FacesConfig(
	// Activates CDI build-in beans that provide the injection this project tests for.
    // Note that this project has explicitly no FacesServlet mapping in web.xml as well as no faces-config.xml file.
    // In other words, this test also assures that FacesInitializer is able to rely on solely this @FacesConfig annotated bean.
        
    // TODO: a faces-config.xml file has been added because of FacesInitializer#onStartup(classes) being empty in current GlassFish version -- that file should be removed once GlassFish is fixed
)
public class ConfigurationBean {

}
