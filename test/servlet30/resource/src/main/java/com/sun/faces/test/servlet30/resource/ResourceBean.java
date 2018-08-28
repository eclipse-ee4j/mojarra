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

package com.sun.faces.test.servlet30.resource;

import java.io.IOException;
import java.io.InputStream;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

@Named
@RequestScoped
public class ResourceBean {

    private static final String LIBRARY_NAME = "css";
    private static final String RESOURCE_NAME = "images/background.png";
    private static final String RESOURCE_TYPE = "images/png";
    private static final String COMBINED_NAME = LIBRARY_NAME + "/" + RESOURCE_NAME;

    public ResourceBean() {
    }

    public String getResourceWithLibrary() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResourceHandler handler = fc.getApplication().getResourceHandler();
        Resource resource = handler.createResource(RESOURCE_NAME, LIBRARY_NAME, RESOURCE_TYPE);
        String resourceAsString = null;
        try {
            resourceAsString = resource.toString();
        } catch (Exception e) {
            resourceAsString = "** could not create resource " + RESOURCE_NAME + " in library " + LIBRARY_NAME + " **";
        }
        return resourceAsString;
    }

    public void setResourceWithLibrary(String resourceWithLibrary) {
        // noop
    }

    public String getResourceWithoutLibrary() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResourceHandler handler = fc.getApplication().getResourceHandler();
        Resource resource = handler.createResource(COMBINED_NAME);
        String resourceAsString = null;
        try {
            resourceAsString = resource.toString();
        } catch (Exception e) {
            resourceAsString = "** could not create resource " + COMBINED_NAME + " **";
        }
        return resourceAsString;
    }

    public void setResourceWithoutLibrary(String resourceWithoutLibrary) {
        // noop
    }

    public String getResourceWithTrailingUnderscore() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResourceHandler handler = fc.getApplication().getResourceHandler();

        Resource resource = handler.createResource("trailing.css", "styles");
        if (null != resource) {
            try {
                InputStream is = resource.getInputStream();
                while (-1 != is.read()) {

                }
            } catch (IOException ex) {
                return "FAILURE";
            }
        }
        return "SUCCESS";

    }

    public String getResourceWithLeadingUnderscore() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResourceHandler handler = fc.getApplication().getResourceHandler();

        Resource resource = handler.createResource("leading.css", "styles");
        if (null != resource) {
            try {
                InputStream is = resource.getInputStream();
                while (-1 != is.read()) {

                }
            } catch (IOException ex) {
                return "FAILURE";
            }
        }
        return "SUCCESS";

    }

    public String getResourceWithInvalidVersion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResourceHandler handler = fc.getApplication().getResourceHandler();

        Resource resource = handler.createResource("noUnderscore.css", "styles");
        if (null != resource) {
            try {
                InputStream is = resource.getInputStream();
                while (-1 != is.read()) {

                }
            } catch (IOException ex) {
                return "FAILURE";
            }
        }
        return "SUCCESS";

    }

    public String getResourceWithValidVersion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResourceHandler handler = fc.getApplication().getResourceHandler();

        Resource resource = handler.createResource("foreground.css", "styles");
        if (null != resource) {
            try {
                InputStream is = resource.getInputStream();
                if (-1 == is.read()) {
                    return "FAILURE";
                }
                is.close();
            } catch (IOException ex) {
                return "FAILURE";
            }
        }
        return "SUCCESS";

    }

}
