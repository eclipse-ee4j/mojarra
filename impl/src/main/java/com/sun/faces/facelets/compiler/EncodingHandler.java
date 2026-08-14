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
import java.util.Map;

import com.sun.faces.RIConstants;
import com.sun.faces.facelets.tag.BuildTimeDecisions;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;

public class EncodingHandler implements FaceletHandler {

    private final FaceletHandler next;
    private final String encoding;
    private final CompilationMessageHolder messageHolder;
    private final boolean unreproducibleBuild;

    public EncodingHandler(FaceletHandler next, String encoding, CompilationMessageHolder messageHolder, boolean unreproducibleBuild) {
        this.next = next;
        this.encoding = encoding;
        this.messageHolder = messageHolder;
        this.unreproducibleBuild = unreproducibleBuild;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        FacesContext context = ctx.getFacesContext();

        if (unreproducibleBuild) {
            // This facelet holds a handler that decides what it contributes to the view by means unknown here, so
            // this build cannot be proven to reproduce what a later one would build.
            BuildTimeDecisions.markUnreproducible(context);
        }

        Map<Object, Object> ctxAttributes = context.getAttributes();
        ctxAttributes.put("facelets.compilationMessages", messageHolder);
        next.apply(ctx, parent);
        ctxAttributes.remove("facelets.compilationMessages");
        messageHolder.processCompilationMessages(ctx.getFacesContext());
        if (!ctxAttributes.containsKey(RIConstants.FACELETS_ENCODING_KEY)) {
            ctx.getFacesContext().getAttributes().put(RIConstants.FACELETS_ENCODING_KEY, encoding);
        }
    }

    public static CompilationMessageHolder getCompilationMessageHolder(FaceletContext ctx) {

        return (CompilationMessageHolder) ctx.getFacesContext().getAttributes().get("facelets.compilationMessages");

    }

}
