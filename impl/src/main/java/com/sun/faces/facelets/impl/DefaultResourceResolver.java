/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.impl;

import java.net.URL;

import com.sun.faces.RIConstants;

import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ViewResource;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.ResourceResolver;

public class DefaultResourceResolver extends ResourceResolver {

    private ResourceHandler resourceHandler = null;

    public static final String NON_DEFAULT_RESOURCE_RESOLVER_PARAM_NAME = RIConstants.FACES_PREFIX + "NDRRPN";

    public DefaultResourceResolver(ResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
    }

    @Override
    public URL resolveUrl(String path) {
        ViewResource faceletResource = resourceHandler.createViewResource(FacesContext.getCurrentInstance(), path);

        if (faceletResource != null) {
            return faceletResource.getURL();
        }

        return null;
    }

    @Override
    public String toString() {
        return "DefaultResourceResolver";
    }

}
