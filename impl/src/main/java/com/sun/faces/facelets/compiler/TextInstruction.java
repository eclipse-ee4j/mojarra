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
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

final class TextInstruction implements Instruction {
    private final ELText txt;

    private final String alias;

    public TextInstruction(String alias, ELText txt) {
        this.alias = alias;
        this.txt = txt;
    }

    @Override
    public void write(FacesContext context) throws IOException {
        ResponseWriter out = context.getResponseWriter();
        try {
            ELContext elContext = context.getELContext();
            txt.writeText(out, elContext);
            // out.writeText(txt.toString(elContext), null);
        } catch (ELException e) {
            throw new ELException(alias + ": " + e.getMessage(), e.getCause());
        } catch (IOException e) {
            throw new ELException(alias + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        ELText nt = txt.apply(factory, ctx);
        if (nt == txt) {
            return this;
        }

        return new TextInstruction(alias, nt);
    }

    @Override
    public boolean isLiteral() {
        return txt.isLiteral();
    }
}
