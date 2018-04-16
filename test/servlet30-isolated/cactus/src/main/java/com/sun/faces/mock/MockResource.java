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

package com.sun.faces.mock;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Collections;
import java.net.URL;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;

/**
 * Mock Resource implementation.
 */
public class MockResource extends Resource {

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public Map<String, String> getResponseHeaders() {
        return Collections.emptyMap();
    }

    public String getRequestPath() {
        throw new UnsupportedOperationException();
    }

    public URL getURL() {
        throw new UnsupportedOperationException();
    }

    public boolean userAgentNeedsUpdate(FacesContext context) {
        throw new UnsupportedOperationException();
    }
}
