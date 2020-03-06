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

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

final class LiteralXMLInstruction implements Instruction {

    private final static char[] STOP = new char[0];
    
    private final char[] instruction;
    private final int len;
    
    public LiteralXMLInstruction(String literal) {
        this.instruction = literal.toCharArray();
        this.len = this.instruction.length;
    }

    @Override
    public void write(FacesContext context) throws IOException {
        ResponseWriter rw = context.getResponseWriter();
        rw.writeText(STOP, 0, 0); // hack to get closing elements
        rw.write(this.instruction, 0, this.len);
    }

    @Override
    public Instruction apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

}
