/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
 */
final class UserTagHandler extends TagHandlerImpl {

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
        TemplateClient body = new InvocationBody(orig);

        ctx.setVariableMapper(new VariableMapperWrapper(UserTagParameterVariableMapper.forInvocation(orig, attributes(ctx), invocation)));

        // eval include
        try {
            ctx.pushClient(body);
            ctx.includeFacelet(parent, location);
        }
        catch (FileNotFoundException e) {
            throw new TagException(tag, e.getMessage());
        }
        finally {

            // make sure we undo our changes
            ctx.popClient(body);
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

    /**
     * Applies what the page wrote between the start and the end of this tag, either the definition carrying the given name or, for a nameless insertion, the
     * whole of it.
     *
     * @param ctx the Facelet context to apply the body with
     * @param parent the component to apply the body to
     * @param name the name of the definition to apply, or {@code null} to apply the whole body
     * @return whether the body holds what the insertion asked for
     */
    private boolean applyBody(FaceletContext ctx, UIComponent parent, String name) throws IOException {
        if (name == null) {
            nextHandler.apply(ctx, parent);
            return true;
        }

        DefineHandler handler = handlers == null ? null : handlers.get(name);

        if (handler == null) {
            return false;
        }

        handler.applyDefinition(ctx, parent);
        return true;
    }

    /**
     * The body of a single invocation, applied wherever the tag file inserts it.
     * <p>
     * The body is markup of the page which wrote it, so it resolves its names against the scope that page is in rather than against the parameter namespace of
     * the tag file inserting it, which holds the parameters of this invocation and hides those of any enclosing one. The scope is wrapped, so that a
     * {@code ui:param} the body carries reaches the body alone, as it does on a {@code ui:include} or a {@code ui:decorate}.
     *
     * @see UserTagParameterVariableMapper
     */
    private final class InvocationBody implements TemplateClient {

        private final VariableMapper invocationScope;

        private InvocationBody(VariableMapper invocationScope) {
            this.invocationScope = invocationScope;
        }

        @Override
        public boolean apply(FaceletContext ctxObj, UIComponent parent, String name) throws IOException {
            FaceletContextImplBase ctx = (FaceletContextImplBase) ctxObj;
            VariableMapper orig = ctx.getVariableMapper();

            ctx.setVariableMapper(new VariableMapperWrapper(invocationScope));

            try {
                return applyBody(ctx, parent, name);
            }
            finally {
                ctx.setVariableMapper(orig);
            }
        }

    }

}
