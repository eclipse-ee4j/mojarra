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

package com.sun.faces.facelets.tag.jsp;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;




public class PageDirectiveHandler extends TagHandler {

    private final TagAttribute contentType;

    private final TagAttribute encoding;


    public PageDirectiveHandler(TagConfig config) {
        super(config);
        this.contentType = this.getAttribute("contentType");
        this.encoding = this.getAttribute("pageEncoding");
    }

    public void apply(FaceletContext ctx, UIComponent uic) throws IOException {
        if (this.contentType != null) {
            String v = this.contentType.getValue(ctx);
            ctx.getFacesContext().getAttributes().put("facelets.ContentType", v);
        }
        if (this.encoding != null) {
            String v = this.encoding.getValue(ctx);
            ctx.getFacesContext().getAttributes().put("facelets.Encoding", v);
        }
        nextHandler.apply(ctx, uic);
    }



}
