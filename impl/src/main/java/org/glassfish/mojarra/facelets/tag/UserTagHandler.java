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

package org.glassfish.mojarra.facelets.tag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

import org.glassfish.mojarra.facelets.FaceletContextImplBase;
import org.glassfish.mojarra.facelets.TemplateClient;
import org.glassfish.mojarra.facelets.el.UserTagParameterVariableMapper;
import org.glassfish.mojarra.facelets.el.VariableMapperWrapper;
import org.glassfish.mojarra.facelets.tag.ui.DefineHandler;

/**
 * A Tag that is specified in a FaceletFile. Takes all attributes specified and sets them on the FaceletContext before including the targeted Facelet file.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class UserTagHandler extends TagHandlerImpl implements TemplateClient {

    protected final TagAttribute[] vars;

    protected final URL location;

    protected final Map<String, DefineHandler> handlers;

    private final String invocation;

    /**
     * @param config
     */
    public UserTagHandler(TagConfig config, URL location, Set<String> requiredAttributes) {
        super(config);
        vars = tag.getAttributes().getAll();
        this.location = location;
        Iterator<DefineHandler> itr = this.findNextByType(DefineHandler.class);
        if (itr.hasNext()) {
            handlers = new HashMap<>();

            DefineHandler d = null;
            while (itr.hasNext()) {
                d = itr.next();
                handlers.put(d.getName(), d);
            }
        }
        else {
            handlers = null;
        }

        requiredAttributes.forEach(this::getRequiredAttribute);
        invocation = isDevelopment() ? tag.getQName() + " at " + tag.getLocation() : null;
    }

    /**
     * Whether this application asks to be told where a name resolves differently than it once did, which is a diagnostic for a page written against the scope a
     * tag file invocation used to have.
     */
    private static boolean isDevelopment() {
        FacesContext context = FacesContext.getCurrentInstance();

        return context != null && context.isProjectStage(ProjectStage.Development);
    }

    /**
     * Binds the attributes supplied at this invocation as the parameters of the target Facelet file, includes it, and restores the variable mapper afterwards,
     * so that neither the parameters nor anything the file sets itself outlives the invocation.
     *
     * @see TagAttribute#getValueExpression(FaceletContext, Class)
     * @see UserTagParameterVariableMapper
     * @see jakarta.faces.view.facelets.FaceletHandler#apply(jakarta.faces.view.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctxObj, UIComponent parent) throws IOException {
        FaceletContextImplBase ctx = (FaceletContextImplBase) ctxObj;
        VariableMapper orig = ctx.getVariableMapper();

        ctx.setVariableMapper(new VariableMapperWrapper(UserTagParameterVariableMapper.forInvocation(orig, attributes(ctx), invocation)));

        // eval include
        try {
            ctx.pushClient(this);
            ctx.includeFacelet(parent, location);
        }
        catch (FileNotFoundException e) {
            throw new TagException(tag, e.getMessage());
        }
        finally {

            // make sure we undo our changes
            ctx.popClient(this);
            ctx.setVariableMapper(orig);
        }
    }

    private Map<String, ValueExpression> attributes(FaceletContext ctx) {
        if (vars.length == 0) {
            return Map.of();
        }

        Map<String, ValueExpression> attributes = new HashMap<>(vars.length);

        for (TagAttribute var : vars) {
            attributes.put(var.getLocalName(), var.getValueExpression(ctx, Object.class));
        }

        return attributes;
    }

    @Override
    public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException {
        if (name != null) {
            if (handlers == null) {
                return false;
            }
            DefineHandler handler = handlers.get(name);
            if (handler != null) {
                handler.applyDefinition(ctx, parent);
                return true;
            }
            else {
                return false;
            }
        }
        else {
            nextHandler.apply(ctx, parent);
            return true;
        }
    }

}
