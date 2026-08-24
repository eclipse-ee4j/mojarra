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

package org.glassfish.mojarra.facelets.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.glassfish.mojarra.facelets.FaceletContextImplBase;
import org.glassfish.mojarra.facelets.TemplateClient;
import org.glassfish.mojarra.facelets.el.DefaultVariableMapper;

/**
 * Default FaceletContext implementation.
 *
 * A single FaceletContext is used for all Facelets involved in an invocation of
 * {@link org.glassfish.mojarra.facelets.Facelet#apply(FacesContext, UIComponent) Facelet#apply(FacesContext, UIComponent)}.
 *
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletContext.java,v 1.4.4.3 2006/03/25 01:01:53 jhook Exp $
 */
final class DefaultFaceletContext extends FaceletContextImplBase {

    /** Stands in for {@link #localIds} once resolved, for a Facelet that caches no first ids for this context. */
    private static final String[] NOT_CACHED = new String[0];

    private final FacesContext faces;

    private final ELContext ctx;

    private final DefaultFacelet facelet;
    private final List<Facelet> faceletHierarchy;

    private VariableMapper varMapper;

    private FunctionMapper fnMapper;

    private final Map<String, Integer> ids;
    /**
     * Per-tag unique-id counters for this build, one {@code int[]} per Facelet, indexed by the slot a tag handler
     * reserved through {@link DefaultFacelet#getIdSlot(String)}. Shared across the whole context chain (like
     * {@link #ids}) so a Facelet included twice in one build keeps counting where the first inclusion left off.
     */
    private final Map<DefaultFacelet, int[]> idCounters;
    /** {@link #facelet}'s entry in {@link #idCounters}, resolved on first use. See {@link #localCounters()}. */
    private int[] localCounters;
    /**
     * {@link #facelet}'s first ids for the prefix this context generates under, resolved on first use, or
     * {@link #NOT_CACHED} once resolved to a Facelet holding none for that prefix. See {@link #localIds(String)}.
     */
    private String[] localIds;
    private final Map<Integer, Integer> prefixes;
    private String prefix;
    private final StringBuilder uniqueIdBuilder = new StringBuilder(30);
    /**
     * The {@link IdMapper} in effect for this build, or {@code null} when ids are not aliased. {@link DefaultFacelet}
     * installs the outermost Facelet's mapper before it constructs the outermost context and removes it only once the
     * whole build is done, so it does not change while any context in the chain is alive.
     */
    private final IdMapper idMapper;

    public DefaultFaceletContext(DefaultFaceletContext ctx, DefaultFacelet facelet) {
        this.ctx = ctx.ctx;
        clients = ctx.clients;
        faces = ctx.faces;
        fnMapper = ctx.fnMapper;
        ids = ctx.ids;
        idCounters = ctx.idCounters;
        prefixes = ctx.prefixes;
        varMapper = ctx.varMapper;
        faceletHierarchy = new ArrayList<>(ctx.faceletHierarchy.size() + 1);
        faceletHierarchy.addAll(ctx.faceletHierarchy);
        faceletHierarchy.add(facelet);
        this.facelet = facelet;
        idMapper = ctx.idMapper;
        faces.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    public DefaultFaceletContext(FacesContext faces, DefaultFacelet facelet) {
        ctx = faces.getELContext();
        ids = new HashMap<>();
        idCounters = new IdentityHashMap<>();
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
        idMapper = IdMapper.getMapper(faces);
        this.faces.getAttributes().put(FaceletContext.FACELET_CONTEXT_KEY, this);
    }

    @Override
    public String getAliasedId(String id) {
        return idMapper != null ? idMapper.getAliasedId(id) : id;
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
    public Object getContext(Class<?> key) {
        return ctx.getContext(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ELContext#putContext(java.lang.Class, java.lang.Object)
     */
    @Override
    public void putContext(Class<?> key, Object contextObject) {
        ctx.putContext(key, contextObject);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.facelets.FaceletContext#generateUniqueId(java.lang.String)
     */
    @Override
    public String generateUniqueId(String base) {

        ensurePrefix();

        Integer cnt = ids.get(base);
        if (cnt == null) {
            ids.put(base, 0);
            return buildUniqueId(base, 0);
        } else {
            int i = cnt.intValue() + 1;
            ids.put(base, i);
            return buildUniqueId(base, i);
        }
    }

    @Override
    public Facelet getUniqueIdSlotOwner() {
        return facelet;
    }

    @Override
    public int getUniqueIdSlot(String tagId) {
        return facelet.getIdSlot(tagId);
    }

    /**
     * Slot-based counterpart of {@link #generateUniqueId(String)}: same id, but the counter for {@code base} is read
     * from {@code owner}'s counter array at {@code slot} rather than looked up by tag id in a map. A tag handler
     * reserves its slot once and reuses it for every build, which is what keeps the per-component build cost of a
     * view free of map inserts.
     *
     * @param base the tag id, as passed to {@link #generateUniqueId(String)}
     * @param owner the Facelet the slot was reserved from
     * @param slot the reserved slot
     * @return the generated unique id
     */
    @Override
    public String generateUniqueId(String base, Facelet owner, int slot) {

        String prefix = ensurePrefix();

        boolean local = owner == facelet;
        int[] counters = local ? localCounters() : counters((DefaultFacelet) owner);

        if (slot >= counters.length) {
            counters = grow((DefaultFacelet) owner, counters);
        }

        int count = counters[slot]++;

        // Only a tag's first id within a build is worth caching, and only for the Facelet this context applies, whose
        // ids this context holds on a field. Anything else is built.
        if (count > 0 || !local) {
            return buildUniqueId(base, count);
        }

        return firstUniqueId(base, slot, prefix);
    }

    /**
     * Returns the id {@code base} generates the first time it is applied under {@code prefix}, from the Facelet being
     * applied when it caches one and by building it otherwise. Taking the prefix as an argument rather than reading the
     * field keeps this callable only once the prefix exists, which is what selects the right cache entry.
     */
    private String firstUniqueId(String base, int slot, String prefix) {
        String[] ids = localIds(prefix);

        if (ids == NOT_CACHED) {
            return buildUniqueId(base, 0);
        }

        if (slot >= ids.length) {
            ids = facelet.growFirstIds(prefix, ids);
            localIds = ids;
        }

        String id = ids[slot];

        if (id == null) {
            id = buildUniqueId(base, 0);
            ids[slot] = id;
        }

        return id;
    }

    /**
     * Returns the Facelet being applied's first ids for {@code prefix}, holding onto it so that the common case -- a
     * tag generating its first id in its own Facelet -- costs an array index rather than a map lookup per component.
     */
    private String[] localIds(String prefix) {
        if (localIds == null) {
            String[] ids = facelet.firstIds(prefix);
            localIds = ids == null ? NOT_CACHED : ids;
        }
        return localIds;
    }

    /**
     * Returns the counter array of the Facelet this context is applying, holding onto it so that the common case --
     * a tag counting ids in its own Facelet -- costs an array index rather than a map lookup per component.
     */
    private int[] localCounters() {
        if (localCounters == null) {
            localCounters = counters(facelet);
        }
        return localCounters;
    }

    private int[] counters(DefaultFacelet owner) {
        return idCounters.computeIfAbsent(owner, f -> new int[f.getIdSlotCount()]);
    }

    /**
     * Resizes {@code owner}'s counter array to its current slot count, for when it handed out more slots since the
     * array was sized -- a Facelet applied for the first time reserves its slots as its tags are first applied.
     */
    private int[] grow(DefaultFacelet owner, int[] counters) {
        int[] grown = Arrays.copyOf(counters, owner.getIdSlotCount());
        idCounters.put(owner, grown);
        if (owner == facelet) {
            localCounters = grown;
        }
        return grown;
    }

    /**
     * Settles this context's id prefix if it has not been settled yet.
     *
     * @return the prefix every id this context generates carries
     */
    private String ensurePrefix() {
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

        return prefix;
    }

    private String buildUniqueId(String base, int count) {
        uniqueIdBuilder.delete(0, uniqueIdBuilder.length());
        uniqueIdBuilder.append(prefix);
        uniqueIdBuilder.append("_");
        uniqueIdBuilder.append(base);
        if (count > 0) {
            uniqueIdBuilder.append("_");
            uniqueIdBuilder.append(count);
        }
        return uniqueIdBuilder.toString();
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
            Iterator<TemplateManager> itr = clients.iterator();
            while (itr.hasNext()) {
                if (itr.next().wraps(client)) {
                    itr.remove();
                    return;
                }
            }
        }
        throw new IllegalStateException(client + " not found");
    }

    @Override
    public void pushClient(final TemplateClient client) {
        clients.add(0, new TemplateManager(facelet, client));
    }

    @Override
    public void extendClient(final TemplateClient client) {
        clients.add(new TemplateManager(facelet, client));
    }

    @Override
    public boolean includeDefinition(UIComponent parent, String name) throws IOException {
        boolean found = false;
        TemplateManager client;

        for (int i = 0, size = clients.size(); i < size && !found; i++) {
            client = clients.get(i);
            if (client.wraps(facelet)) {
                continue;
            }
            found = client.apply(this, parent, name);
        }

        return found;
    }

    private final static class TemplateManager implements TemplateClient {
        private final DefaultFacelet owner;

        private final TemplateClient target;

        private final Set<String> names = new HashSet<>();

        public TemplateManager(DefaultFacelet owner, TemplateClient target) {
            this.owner = owner;
            this.target = target;
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

        // Returns true if the given facelet or template client is the one this manager wraps.
        boolean wraps(Object o) {
            return owner == o || target == o;
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
