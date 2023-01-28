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
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
class CompilationUnit {

    protected final static FaceletHandler LEAF = new FaceletHandler() {
        @Override
        public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        }

        @Override
        public String toString() {
            return "FaceletHandler Tail";
        }
    };

    private List<CompilationUnit> children;

    public CompilationUnit() {
    }

    protected void startNotify(CompilationManager manager) {

    }

    protected void finishNotify(CompilationManager manager) {

    }

    public void addChild(CompilationUnit unit) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(unit);
    }

    public void removeChildren() {
        children.clear();
    }

    public FaceletHandler createFaceletHandler() {
        return getNextFaceletHandler();
    }

    protected final FaceletHandler getNextFaceletHandler() {
        if (children == null || children.size() == 0) {
            return LEAF;
        }
        if (children.size() == 1) {
            CompilationUnit u = children.get(0);
            return u.createFaceletHandler();
        }
        FaceletHandler[] fh = new FaceletHandler[children.size()];
        for (int i = 0; i < fh.length; i++) {
            fh[i] = children.get(i).createFaceletHandler();
        }
        return new CompositeFaceletHandler(fh);
    }

}
