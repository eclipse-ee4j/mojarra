/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

package com.sun.faces.el;

import com.sun.faces.util.RequestStateManager;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.faces.context.FacesContext;
import java.util.Map;

/**
 * Maintains an ordered composite list of child <code>ELResolver for Faces</code>.
 *
 */
public final class ChainTypeCompositeELResolver extends FacesCompositeELResolver {
    @Override
    public void addRootELResolver(ELResolver elResolver) {
        _wrapped.addRootELResolver(elResolver);
    }

    @Override
    public void addPropertyELResolver(ELResolver elResolver) {
        _wrapped.addPropertyELResolver(elResolver);
    }

    @Override
    public void add(ELResolver elResolver) {
        _wrapped.add(elResolver);
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        FacesContext ctx = getFacesContext(context);
        if (ctx == null) {
            return null;
        }

        Map<String, Object> stateMap = RequestStateManager.getStateMap(ctx);

        stateMap.put(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME, _chainType);
        Object result = null;
        try {
            result = _wrapped.getValue(context, base, property);
        } finally {
            stateMap.remove(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME);
        }

        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {

        FacesContext ctx = getFacesContext(context);

        if (ctx == null) {
            return null;
        }

        Map<String, Object> stateMap = RequestStateManager.getStateMap(ctx);

        stateMap.put(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME, _chainType);
        Class<?> result = null;
        try {
            result = _wrapped.getType(context, base, property);
        } finally {
            stateMap.remove(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME);
        }

        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        FacesContext ctx = getFacesContext(context);
        if (ctx == null) {
            return;
        }

        Map<String, Object> stateMap = RequestStateManager.getStateMap(ctx);

        stateMap.put(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME, _chainType);
        try {
            _wrapped.setValue(context, base, property, val);
        } finally {
            stateMap.remove(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        FacesContext ctx = getFacesContext(context);
        if (ctx == null) {
            return false;
        }

        Map<String, Object> stateMap = RequestStateManager.getStateMap(ctx);

        stateMap.put(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME, _chainType);
        boolean result = false;
        try {
            result = _wrapped.isReadOnly(context, base, property);
        } finally {
            stateMap.remove(RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME);
        }

        return result;
    }

    @Override
    public ELResolverChainType getChainType() {
        return _chainType;
    }

    private final FacesCompositeELResolver _wrapped;
    private final ELResolverChainType _chainType;

    /**
     * <p>
     * Guarantee that this instance knows of what chain it is a member.
     * </p>
     *
     * @param chainType the ELResolverChainType
     */
    public ChainTypeCompositeELResolver(ELResolverChainType chainType) {
        _wrapped = new DemuxCompositeELResolver(chainType);
        _chainType = chainType;
    }

    public ChainTypeCompositeELResolver(FacesCompositeELResolver delegate) {
        _wrapped = delegate;
        _chainType = delegate.getChainType();
    }

    /**
     * @param elContext context for the current expression evaluation
     * @return the <code>FacesContext</code> associated with this expression evaluation
     */
    private FacesContext getFacesContext(ELContext elContext) {

        return (FacesContext) elContext.getContext(FacesContext.class);

    }
}
