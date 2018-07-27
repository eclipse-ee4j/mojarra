/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.customfaceletsresolver;

import java.net.URL;

import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletsResourceResolver;
import javax.faces.view.facelets.ResourceResolver;

@FaceletsResourceResolver
public class CustomerFaceletsResolver extends ResourceResolver {

    private ResourceResolver wrapped;

    public CustomerFaceletsResolver(ResourceResolver wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public URL resolveUrl(String path) {
        URL result = result = wrapped.resolveUrl(path);
        if (1 < path.length()) {
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("message", " custom ResourceResolver " + path);
        }
        return result;
    }
}
