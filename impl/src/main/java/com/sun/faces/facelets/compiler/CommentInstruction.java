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

import com.sun.faces.facelets.el.ELText;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.faces.context.FacesContext;

final class CommentInstruction implements Instruction {
    private final ELText text;

    public CommentInstruction(ELText text) {
        this.text = text;
    }

    @Override
    public void write(FacesContext context) throws IOException {
        ELContext elContext = context.getELContext();
        context.getResponseWriter().writeComment(text.toString(elContext));
    }

    @Override
    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        ELText t = text.apply(factory, ctx);
        return new CommentInstruction(t);
    }

    @Override
    public boolean isLiteral() {
        return false;
    }
}
