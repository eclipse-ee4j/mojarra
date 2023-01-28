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

package com.sun.faces.facelets.compiler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import com.sun.faces.el.ELContextImpl;
import com.sun.faces.facelets.el.CompositeFunctionMapper;
import com.sun.faces.facelets.tag.TagLibrary;

import jakarta.el.ELContext;
import jakarta.el.FunctionMapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;

final class NamespaceHandler extends FunctionMapper implements FaceletHandler {

    private final TagLibrary library;
    private final Map<String, String> ns;
    private FaceletHandler next;

    public NamespaceHandler(FaceletHandler next, TagLibrary library, Map<String, String> ns) {
        this.library = library;
        this.ns = ns;
        this.next = next;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        FunctionMapper orig = ctx.getFunctionMapper();
        pushMapper(ctx.getFacesContext(), this);
        ctx.setFunctionMapper(new CompositeFunctionMapper(this, orig));
        try {
            next.apply(ctx, parent);
        } finally {
            ctx.setFunctionMapper(orig);
        }
    }

    @Override
    public Method resolveFunction(String prefix, String localName) {
        String uri = ns.get(prefix);
        if (uri != null) {
            return library.createFunction(uri, localName);
        }
        return null;
    }

    // --------------------------------------------------------- Private Methods

    private void pushMapper(FacesContext ctx, FunctionMapper mapper) {

        ELContext elContext = ctx.getELContext();
        if (elContext instanceof ELContextImpl) {
            ((ELContextImpl) elContext).setFunctionMapper(mapper);
        }

    }

}
