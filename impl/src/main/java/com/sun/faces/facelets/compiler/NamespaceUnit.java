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

import java.util.HashMap;
import java.util.Map;

import com.sun.faces.facelets.tag.TagLibrary;

import jakarta.faces.view.facelets.FaceletHandler;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class NamespaceUnit extends CompilationUnit {

    private final Map<String, String> ns = new HashMap<>();
    private final TagLibrary library;

    public NamespaceUnit(TagLibrary library) {
        this.library = library;
    }

    @Override
    public FaceletHandler createFaceletHandler() {
        FaceletHandler next = getNextFaceletHandler();
        return new NamespaceHandler(next, library, ns);
    }

    public void setNamespace(String prefix, String uri) {
        ns.put(prefix, uri);
    }

    @Override
    public void addChild(CompilationUnit unit) {
        super.addChild(unit);
    }

}
