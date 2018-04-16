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

package com.sun.faces.facelets.tag.jstl.core;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import java.io.IOException;

/**
 * @author Jacob Hookom
 */
public final class IfHandler extends TagHandlerImpl {

    private final TagAttribute test;

    private final TagAttribute var;

    /**
     * @param config
     */
    public IfHandler(TagConfig config) {
        super(config);
        this.test = this.getRequiredAttribute("test");
        this.var = this.getAttribute("var");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException, FacesException, ELException {
        boolean b = this.test.getBoolean(ctx);
        if (this.var != null) {
            ctx.setAttribute(var.getValue(ctx), b);
        }
        if (b) {
            this.nextHandler.apply(ctx, parent);
        }
    }

}
