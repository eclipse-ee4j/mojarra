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

package com.sun.faces.facelets.tag.composite;

import java.io.IOException;

import com.sun.faces.facelets.compiler.CompilationMessageHolder;
import com.sun.faces.facelets.compiler.EncodingHandler;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

public class ExtensionHandler extends TagHandler {

    public ExtensionHandler(TagConfig tagConfig) {
        super(tagConfig);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // extract a prefix from the child content
        if (null != nextHandler) {
            String content = nextHandler.toString().trim();
            if (content.startsWith("<")) {
                int i;
                if (-1 != (i = content.indexOf(":"))) {
                    content = content.substring(1, i);
                    CompilationMessageHolder messageHolder = EncodingHandler.getCompilationMessageHolder(ctx);
                    // remove any compilation messages pertaining to this prefix
                    if (null != messageHolder) {
                        messageHolder.removeNamespacePrefixMessages(content);
                    }
                }
            }
        }

    }

}
