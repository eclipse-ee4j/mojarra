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

import static com.sun.faces.el.ELUtils.getDefaultExpressionFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.sun.faces.config.InitFacesContext;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * Concrete implementation of {@link jakarta.el.ELContext}. ELContext's constructor is protected to control creation of
 * ELContext objects through their appropriate factory methods. This version of ELContext forces construction through
 * FacesContextImpl.
 *
 */
public class ELContextImpl extends ELContext {

    private FunctionMapper functionMapper = new NoopFunctionMapper();
    private VariableMapper variableMapper;
    private final ELResolver resolver;

    // ------------------------------------------------------------ Constructors

    /**
     * Constructs a new ELContext associated with the given ELResolver.
     *
     * @param resolver the ELResolver to return from {@link #getELResolver()}
     */
    public ELContextImpl(ELResolver resolver) {
        this.resolver = resolver;
    }

    public ELContextImpl(FacesContext facesContext) {
        this(facesContext.getApplication().getELResolver());

        putContext(FacesContext.class, facesContext);

        ExpressionFactory expressionFactory = getDefaultExpressionFactory(facesContext);
        if (expressionFactory != null) {
            putContext(ExpressionFactory.class, expressionFactory);
        }

        if (!(facesContext instanceof InitFacesContext)) {
            UIViewRoot root = facesContext.getViewRoot();
            if (root != null) {
                setLocale(root.getLocale());
            }
        }
    }

    // -------------------------------------------------- Methods from ELContext

    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }

    @Override
    public VariableMapper getVariableMapper() {
        if (variableMapper == null) {
            variableMapper = new VariableMapperImpl();
        }
        return variableMapper;
    }

    @Override
    public ELResolver getELResolver() {
        return resolver;
    }

    // ---------------------------------------------------------- Public Methods

    public void setFunctionMapper(FunctionMapper functionMapper) {
        this.functionMapper = functionMapper;
    }

    // ----------------------------------------------------------- Inner Classes

    private static class VariableMapperImpl extends VariableMapper {

        private final Map<String, ValueExpression> variables;

        public VariableMapperImpl() {
            variables = new HashMap<>();
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            return variables.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression valueExpression) {
            return variables.put(variable, valueExpression);
        }
    }

    private static class NoopFunctionMapper extends FunctionMapper {

        @Override
        public Method resolveFunction(String s, String s1) {
            return null;
        }

    }

}
