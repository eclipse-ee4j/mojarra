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

package com.sun.faces.facelets.tag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.faces.facelets.FaceletContextImplBase;
import com.sun.faces.facelets.TemplateClient;
import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.ui.DefineHandler;

import jakarta.el.VariableMapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

/**
 * A Tag that is specified in a FaceletFile. Takes all attributes specified and sets them on the FaceletContext before
 * including the targeted Facelet file.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class UserTagHandler extends TagHandlerImpl implements TemplateClient {

    protected final TagAttribute[] vars;

    protected final URL location;

    protected final Map handlers;

    /**
     * @param config
     */
    public UserTagHandler(TagConfig config, URL location) {
        super(config);
        vars = tag.getAttributes().getAll();
        this.location = location;
        Iterator itr = this.findNextByType(DefineHandler.class);
        if (itr.hasNext()) {
            handlers = new HashMap();

            DefineHandler d = null;
            while (itr.hasNext()) {
                d = (DefineHandler) itr.next();
                handlers.put(d.getName(), d);
            }
        } else {
            handlers = null;
        }
    }

    /**
     * Iterate over all TagAttributes and set them on the FaceletContext's VariableMapper, then include the target Facelet.
     * Finally, replace the old VariableMapper.
     *
     * @see TagAttribute#getValueExpression(FaceletContext, Class)
     * @see VariableMapper
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext,
     * jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctxObj, UIComponent parent) throws IOException {
        FaceletContextImplBase ctx = (FaceletContextImplBase) ctxObj;
        VariableMapper orig = ctx.getVariableMapper();

        // setup a variable map
        if (vars.length > 0) {
            VariableMapper varMapper = new VariableMapperWrapper(orig);
            for (int i = 0; i < vars.length; i++) {
                varMapper.setVariable(vars[i].getLocalName(), vars[i].getValueExpression(ctx, Object.class));
            }
            ctx.setVariableMapper(varMapper);
        }

        // eval include
        try {
            ctx.pushClient(this);
            ctx.includeFacelet(parent, location);
        } catch (FileNotFoundException e) {
            throw new TagException(tag, e.getMessage());
        } finally {

            // make sure we undo our changes
            ctx.popClient(this);
            ctx.setVariableMapper(orig);
        }
    }

    @Override
    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException {
        if (name != null) {
            if (handlers == null) {
                return false;
            }
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
