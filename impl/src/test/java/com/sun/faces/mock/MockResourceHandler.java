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

package com.sun.faces.mock;

import java.io.IOException;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;

public class MockResourceHandler extends ResourceHandler {

    public Resource createResource(String resourceName) {
        throw new UnsupportedOperationException();
    }

    public Resource createResource(String resourceName, String libraryName) {
        throw new UnsupportedOperationException();
    }

    public Resource createResource(String resourceName,
            String libraryName,
            String contentType) {
        throw new UnsupportedOperationException();
    }

    public void handleResourceRequest(FacesContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isResourceRequest(FacesContext context) {
        return false;
    }

    public boolean libraryExists(String libraryName) {
        return true;
    }

    public String getRendererTypeForResourceName(String resourceName) {
        if (resourceName.endsWith(".js")) {
            return "jakarta.faces.resource.Script";
        } else if (resourceName.endsWith(".css")) {
            return "jakarta.faces.resource.Stylesheet";
        } else {
            return null;
        }
    }
}
