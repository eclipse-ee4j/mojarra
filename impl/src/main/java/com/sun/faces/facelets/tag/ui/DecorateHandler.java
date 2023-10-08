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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.FaceletContextImplBase;
import com.sun.faces.facelets.TemplateClient;
import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.util.FacesLogger;

import jakarta.el.VariableMapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

/**
 * @author Jacob Hookom
 */
public final class DecorateHandler extends TagHandlerImpl implements TemplateClient {

    private static final Logger log = FacesLogger.FACELETS_DECORATE.getLogger();

    private final TagAttribute template;

    private final Map handlers;

    private final ParamHandler[] params;

    /**
     * @param config
     */
    public DecorateHandler(TagConfig config) {
        super(config);
        template = getRequiredAttribute("template");
        handlers = new HashMap();

        Iterator itr = this.findNextByType(DefineHandler.class);
        DefineHandler d = null;
        while (itr.hasNext()) {
            d = (DefineHandler) itr.next();
            handlers.put(d.getName(), d);
            if (log.isLoggable(Level.FINE)) {
                log.fine(tag + " found Define[" + d.getName() + "]");
            }
        }
        List paramC = new ArrayList();
        itr = this.findNextByType(ParamHandler.class);
        while (itr.hasNext()) {
            paramC.add(itr.next());
        }
        if (paramC.size() > 0) {
            params = new ParamHandler[paramC.size()];
            for (int i = 0; i < params.length; i++) {
                params[i] = (ParamHandler) paramC.get(i);
            }
        } else {
            params = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctxObj, UIComponent parent) throws IOException {
        FaceletContextImplBase ctx = (FaceletContextImplBase) ctxObj;
        VariableMapper orig = ctx.getVariableMapper();
        if (params != null) {
            VariableMapper vm = new VariableMapperWrapper(orig);
            ctx.setVariableMapper(vm);
            for (int i = 0; i < params.length; i++) {
                params[i].apply(ctx, parent);
            }
        }

        ctx.pushClient(this);
        String path = null;
        try {
            path = template.getValue(ctx);
            if (path.trim().length() == 0) {
                throw new TagAttributeException(tag, template, "Invalid path : " + path);
            }
            ctx.includeFacelet(parent, path);
        } catch (IOException e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, e.toString(), e);
            }
            throw new TagAttributeException(tag, template, "Invalid path : " + path, e);
        } finally {
            ctx.setVariableMapper(orig);
            ctx.popClient(this);
        }
    }

    @Override
    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException {
        if (name != null) {
            DefineHandler handler = (DefineHandler) handlers.get(name);
            if (handler != null) {
                handler.applyDefinition(ctx, parent);
                return true;
            } else {
                return false;
            }
        } else {
            nextHandler.apply(ctx, parent);
            return true;
        }
    }
}
