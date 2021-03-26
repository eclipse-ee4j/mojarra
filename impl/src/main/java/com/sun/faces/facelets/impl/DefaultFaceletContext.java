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

package com.sun.faces.facelets.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.faces.facelets.FaceletContextImplBase;
import com.sun.faces.facelets.TemplateClient;
import com.sun.faces.facelets.el.DefaultVariableMapper;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletContext;

/**
 * Default FaceletContext implementation.
 *
 * A single FaceletContext is used for all Facelets involved in an invocation of
 * {@link com.sun.faces.facelets.Facelet#apply(FacesContext, UIComponent) Facelet#apply(FacesContext, UIComponent)}.
 *
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletContext.java,v 1.4.4.3 2006/03/25 01:01:53 jhook Exp $
 */
final class DefaultFaceletContext extends FaceletContextImplBase {

    private final FacesContext faces;

    private final ELContext ctx;

    private final DefaultFacelet facelet;
    private final List<Facelet> faceletHierarchy;

    private VariableMapper varMapper;

    private FunctionMapper fnMapper;

    private final Map<String, Integer> ids;
    private final Map<Integer, Integer> prefixes;
    private String prefix;
    private final StringBuilder uniqueIdBuilder = new StringBuilder(30);

    public DefaultFaceletContext(DefaultFaceletContext ctx, DefaultFacelet facelet) {
        this.ctx = ctx.ctx;
        clients = ctx.clients;
        faces = ctx.faces;
        fnMapper = ctx.fnMapper;
        ids = ctx.ids;
        prefixes = ctx.prefixes;
        varMapper = ctx.varMapper;
        faceletHierarchy = new ArrayList<>(ctx.faceletHierarchy.size() + 1);
        faceletHierarchy.addAll(ctx.faceletHierarchy);
        faceletHierarchy.add(facelet);
        this.facelet = facelet;
        faces.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    public DefaultFaceletContext(FacesContext faces, DefaultFacelet facelet) {
        ctx = faces.getELContext();
        ids = new HashMap<>();
        prefixes = new HashMap<>();
        clients = new ArrayList<>(5);
        this.faces = faces;
        faceletHierarchy = new ArrayList<>(1);
        faceletHierarchy.add(facelet);
        this.facelet = facelet;
        varMapper = ctx.getVariableMapper();
        if (varMapper == null) {
            varMapper = new DefaultVariableMapper();
        }
        fnMapper = ctx.getFunctionMapper();
        this.faces.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#getFacesContext()
     */
    @Override
    public FacesContext getFacesContext() {
        return faces;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#getExpressionFactory()
     */
    @Override
    public ExpressionFactory getExpressionFactory() {
        return facelet.getExpressionFactory();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#setVariableMapper(jakarta.el.VariableMapper)
     */
    @Override
    public void setVariableMapper(VariableMapper varMapper) {
        // Assert.param("varMapper", varMapper);
        this.varMapper = varMapper;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#setFunctionMapper(jakarta.el.FunctionMapper)
     */
    @Override
    public void setFunctionMapper(FunctionMapper fnMapper) {
        // Assert.param("fnMapper", fnMapper);
        this.fnMapper = fnMapper;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#includeFacelet(jakarta.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public void includeFacelet(UIComponent parent, String relativePath) throws IOException, FacesException, ELException {
        facelet.include(this, parent, relativePath);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ELContext#getFunctionMapper()
     */
    @Override
    public FunctionMapper getFunctionMapper() {
        return fnMapper;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ELContext#getVariableMapper()
     */
    @Override
    public VariableMapper getVariableMapper() {
        return varMapper;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ELContext#getContext(java.lang.Class)
     */
    @Override
    public Object getContext(Class key) {
        return ctx.getContext(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ELContext#putContext(java.lang.Class, java.lang.Object)
     */
    @Override
    public void putContext(Class key, Object contextObject) {
        ctx.putContext(key, contextObject);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#generateUniqueId(java.lang.String)
     */
    @Override
    public String generateUniqueId(String base) {

        if (prefix == null) {
            StringBuilder builder = new StringBuilder(faceletHierarchy.size() * 30);
            for (int i = 0; i < faceletHierarchy.size(); i++) {
                DefaultFacelet facelet = (DefaultFacelet) faceletHierarchy.get(i);
                builder.append(facelet.getAlias());
            }
            Integer prefixInt = builder.toString().hashCode();

            Integer cnt = prefixes.get(prefixInt);
            if (cnt == null) {
                prefixes.put(prefixInt, 0);
                prefix = prefixInt.toString();
            } else {
                int i = cnt.intValue() + 1;
                prefixes.put(prefixInt, i);
                prefix = prefixInt + "_" + i;
            }
        }

        Integer cnt = ids.get(base);
        if (cnt == null) {
            ids.put(base, 0);
            uniqueIdBuilder.delete(0, uniqueIdBuilder.length());
            uniqueIdBuilder.append(prefix);
            uniqueIdBuilder.append("_");
            uniqueIdBuilder.append(base);
            return uniqueIdBuilder.toString();
        } else {
            int i = cnt.intValue() + 1;
            ids.put(base, i);
            uniqueIdBuilder.delete(0, uniqueIdBuilder.length());
            uniqueIdBuilder.append(prefix);
            uniqueIdBuilder.append("_");
            uniqueIdBuilder.append(base);
            uniqueIdBuilder.append("_");
            uniqueIdBuilder.append(i);
            return uniqueIdBuilder.toString();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
        if (varMapper != null) {
            ValueExpression ve = varMapper.resolveVariable(name);
            if (ve != null) {
                return ve.getValue(this);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {
        if (varMapper != null) {
            if (value == null) {
                varMapper.setVariable(name, null);
            } else {
                varMapper.setVariable(name, facelet.getExpressionFactory().createValueExpression(value, Object.class));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#includeFacelet(jakarta.faces.component.UIComponent, java.net.URL)
     */
    @Override
    public void includeFacelet(UIComponent parent, URL absolutePath) throws IOException, FacesException, ELException {
        facelet.include(this, parent, absolutePath);
    }

    @Override
    public ELResolver getELResolver() {
        return ctx.getELResolver();
    }

    private final List<TemplateManager> clients;

    @Override
    public void popClient(TemplateClient client) {
        if (!clients.isEmpty()) {
            Iterator itr = clients.iterator();
            while (itr.hasNext()) {
                if (itr.next().equals(client)) {
                    itr.remove();
                    return;
                }
            }
        }
        throw new IllegalStateException(client + " not found");
    }

    @Override
    public void pushClient(final TemplateClient client) {
        clients.add(0, new TemplateManager(facelet, client, true));
    }

    @Override
    public void extendClient(final TemplateClient client) {
        clients.add(new TemplateManager(facelet, client, false));
    }

    @Override
    public boolean includeDefinition(UIComponent parent, String name) throws IOException {
        boolean found = false;
        TemplateManager client;

        for (int i = 0, size = clients.size(); i < size && !found; i++) {
            client = clients.get(i);
            // noinspection EqualsBetweenInconvertibleTypes
            if (client.equals(facelet)) {
                continue;
            }
            found = client.apply(this, parent, name);
        }

        return found;
    }

    private final static class TemplateManager implements TemplateClient {
        private final DefaultFacelet owner;

        private final TemplateClient target;

        private final boolean root;

        private final Set<String> names = new HashSet<>();

        public TemplateManager(DefaultFacelet owner, TemplateClient target, boolean root) {
            this.owner = owner;
            this.target = target;
            this.root = root;
        }

        @Override
        public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException {

            String testName = name != null ? name : "facelets._NULL_DEF_";
            if (names.contains(testName)) {
                return false;
            } else {
                names.add(testName);
                boolean found = target.apply(new DefaultFaceletContext((DefaultFaceletContext) ctx, owner), parent, name);
                names.remove(testName);
                return found;
            }
        }

        @Override
        public boolean equals(Object o) {
            // System.out.println(this.owner.getAlias() + " == " +
            // ((DefaultFacelet) o).getAlias());
            return owner == o || target == o;
        }

        public boolean isRoot() {
            return root;
        }
    }

    @Override
    public boolean isPropertyResolved() {
        return ctx.isPropertyResolved();
    }

    @Override
    public void setPropertyResolved(boolean resolved) {
        ctx.setPropertyResolved(resolved);
    }
}
