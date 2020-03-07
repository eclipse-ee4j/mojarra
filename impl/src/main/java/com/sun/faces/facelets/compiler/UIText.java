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

import jakarta.el.ELException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class UIText extends UILeaf {

    private final ELText txt;

    private final String alias;

    public UIText(String alias, ELText txt) {
        this.txt = txt;
        this.alias = alias;
    }

    @Override
    public String getFamily() {
        return null;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered()) {
            ResponseWriter out = context.getResponseWriter();
            try {
                txt.write(out, context.getELContext());
            } catch (ELException e) {
                throw new ELException(alias + ": " + e.getMessage(), e.getCause());
            } catch (IOException e) {
                throw new ELException(alias + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String toString() {
        return txt.toString();
    }
}
