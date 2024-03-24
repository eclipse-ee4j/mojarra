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

package com.sun.faces.facelets.tag.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.util.FacesLogger;

import jakarta.el.VariableMapper;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

/**
 * @author Jacob Hookom
 */
public final class IncludeHandler extends TagHandlerImpl {

    private static final Logger log = FacesLogger.FACELETS_INCLUDE.getLogger();

    private final TagAttribute src;

    /**
     * @param config
     */
    public IncludeHandler(TagConfig config) {
        super(config);
        TagAttribute attr = null;
        attr = getAttribute("src");
        if (null == attr) {
            attr = getAttribute("file");
        }
        if (null == attr) {
            attr = getAttribute("page");
        }
        if (null == attr) {
            throw new TagException(tag, "Attribute 'src', 'file' or 'page' is required");
        }
        src = attr;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        String path = src.getValue(ctx);
        if (path == null || path.length() == 0) {
            return;
        }
        VariableMapper orig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(orig));
        try {
            nextHandler.apply(ctx, null);
            ctx.includeFacelet(parent, path);
        } catch (IOException e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, e.toString(), e);
            }
            throw new TagAttributeException(tag, src, "Invalid path : " + path, e);
        } finally {
            ctx.setVariableMapper(orig);
        }
    }
}
