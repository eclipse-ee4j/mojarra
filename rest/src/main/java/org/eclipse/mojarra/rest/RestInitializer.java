/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.rest;

import java.util.Set;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration.Dynamic;

/**
 * The ServletContainerInitializer that automatically registers the Faces REST
 * Servlet and the '/rest/*' mapping if the Faces REST Servlet has not
 * already been registered.
 */
public class RestInitializer implements ServletContainerInitializer {   

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) 
            throws ServletException {
        if (servletContext.getServletRegistration("Faces REST Servlet") == null) {
            Dynamic dynamic = servletContext.addServlet("Faces REST Servlet", FacesServlet.class.getName());
            dynamic.addMapping("/rest/*");
            dynamic.setInitParameter("jakarta.faces.LIFECYCLE_ID", RestLifecycle.class.getName());
        }
    }
}
