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

import com.sun.faces.config.FaceletsConfiguration;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.faces.context.FacesContext;

final class LiteralTextInstruction implements Instruction {
    private final String text;

    public LiteralTextInstruction(String text) {
        this.text = text;
    }

    @Override
    public void write(FacesContext context) throws IOException {
        if (FaceletsConfiguration.getInstance(context).isEscapeInlineText(context)) {
            context.getResponseWriter().writeText(text, null);
        } else {
            context.getResponseWriter().write(text);
        }
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
