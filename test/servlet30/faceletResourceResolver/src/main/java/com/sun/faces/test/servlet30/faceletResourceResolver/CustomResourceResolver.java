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

package com.sun.faces.test.servlet30.faceletResourceResolver;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ResourceResolver;

public class CustomResourceResolver extends ResourceResolver {
    
    private final ResourceResolver wrapped;

    public CustomResourceResolver(ResourceResolver wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public URL resolveUrl(String path) {
        getListForCurrentRequest().add(path);
        return wrapped.resolveUrl(path);
    }
    
    private static final String LIST_FOR_CURRENT_REQUEST = "resolvedUrls";
    
    private List<String> getListForCurrentRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> attrs = context.getAttributes();
        List<String> result;
        if (!attrs.containsKey(LIST_FOR_CURRENT_REQUEST)) {
            result = new ArrayList<>();
            attrs.put(LIST_FOR_CURRENT_REQUEST, result);
        } else {
            result = (List<String>) attrs.get(LIST_FOR_CURRENT_REQUEST);
        }
        return result;
    }

}
