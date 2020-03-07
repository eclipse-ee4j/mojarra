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

package com.sun.faces.facelets.tag.jstl.core;

import java.io.IOException;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;

/**
 * @author Jacob Hookom
 */
public final class ChooseWhenHandler extends TagHandlerImpl {

    private final TagAttribute test;

    public ChooseWhenHandler(TagConfig config) {
        super(config);
        test = getRequiredAttribute("test");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        nextHandler.apply(ctx, parent);
    }

    public boolean isTestTrue(FaceletContext ctx) {
        return test.getBoolean(ctx);
    }
}
