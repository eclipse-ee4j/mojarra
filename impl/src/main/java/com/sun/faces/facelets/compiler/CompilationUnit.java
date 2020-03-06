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

    private List children;

    public CompilationUnit() {
    }

    protected void startNotify(CompilationManager manager) {

    }

    protected void finishNotify(CompilationManager manager) {

    }

    public void addChild(CompilationUnit unit) {
        if (this.children == null) {
            this.children = new ArrayList();
        }
        this.children.add(unit);
    }

    public void removeChildren() {
        this.children.clear();
    }

    public FaceletHandler createFaceletHandler() {
        return this.getNextFaceletHandler();
    }

    protected final FaceletHandler getNextFaceletHandler() {
        if (this.children == null || this.children.size() == 0) {
            return LEAF;
        }
        if (this.children.size() == 1) {
            CompilationUnit u = (CompilationUnit) this.children.get(0);
            return u.createFaceletHandler();
        }
        FaceletHandler[] fh = new FaceletHandler[this.children.size()];
        for (int i = 0; i < fh.length; i++) {
            fh[i] = ((CompilationUnit) this.children.get(i)).createFaceletHandler();
        }
        return new CompositeFaceletHandler(fh);
    }

}
