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

package com.sun.faces.el;

import com.sun.faces.el.FacesCompositeELResolver.ELResolverChainType;
import com.sun.faces.util.RequestStateManager;

import jakarta.el.ELResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.VariableResolver;

/**
 * <p>
 * This special VariableResolver serves as the "original" VariableResolver that is passed to the one-arg ctor for the
 * <b>first</b> custom VariableResolver that is encountered during application configuration. Subsequent
 * VariableResolver instances get passed the previous VariableResolver instance, per section 10.4.5 of the spec.
 * </p>
 *
 * <p>
 * The "specialness" of this VariableResolver is found in its {@link #resolveVariable} method, which delegates to the
 * head of the "correct" ELResolver chain based on the context in which this expression is being evaluated.
 *
 * If the expression being evaluated originated in a programmatic API call, the <code>Application</code>'s
 * <code>ELResolver</code> is used to resolve the variable. This will cause the ELResolver chain described in section
 * 5.6.2 of the spec to be used.
 * </p>
 */
@SuppressWarnings("deprecation")
public class ChainAwareVariableResolver extends VariableResolver {

    public ChainAwareVariableResolver() {

    }

    //
    // Relationship Instance Variables
    //

    /**
     * See the class javadocs.
     */
    @Override
    public Object resolveVariable(FacesContext context, String name) throws EvaluationException {
        Object result = null;
        ELResolverChainType type = ELResolverChainType.Faces;
        Object valueObject = RequestStateManager.get(context, RequestStateManager.EL_RESOLVER_CHAIN_TYPE_NAME);
        if (null != valueObject && valueObject instanceof ELResolverChainType) {
            type = (ELResolverChainType) valueObject;
        }

        if (ELResolverChainType.Faces == type) {
            ELResolver elr = context.getApplication().getELResolver();
            result = elr.getValue(context.getELContext(), null, name);
        }

        return result;
    }
}
