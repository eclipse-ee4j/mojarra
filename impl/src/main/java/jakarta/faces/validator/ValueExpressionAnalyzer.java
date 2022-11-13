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

package jakarta.faces.validator;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Locale;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.el.VariableMapper;
import jakarta.faces.el.CompositeComponentExpressionHolder;

/**
 * Analyzes a {@link ValueExpression} and provides access to the base object and property name to which the expression
 * maps via the getReference() method.
 * This is currently especially needed for composite component expressions, see also https://github.com/eclipse-ee4j/mojarra/issues/5171
 */
class ValueExpressionAnalyzer {
    private ValueExpression expression;

    public ValueExpressionAnalyzer(ValueExpression expression) {
        this.expression = expression;
    }

    public ValueReference getReference(ELContext elContext) {
        InterceptingResolver resolver = new InterceptingResolver(elContext.getELResolver());
        try {
            expression.setValue(decorateELContext(elContext, resolver), null);
        } catch (ELException ele) {
            return null;
        }
        ValueReference reference = resolver.getValueReference();
        if (reference != null) {
            Object base = reference.getBase();
            Object property = reference.getProperty();
            if (base instanceof CompositeComponentExpressionHolder && property != null) {
                ValueExpression ve = ((CompositeComponentExpressionHolder) base).getExpression(property.toString());
                if (ve != null) {
                    expression = ve;
                    reference = getReference(elContext);
                }
            }
        }
        return reference;
    }

    private ELContext decorateELContext(final ELContext context, final ELResolver resolver) {
        return new ELContext() {

            // punch in our new ELResolver
            @Override
            public ELResolver getELResolver() {
                return resolver;
            }

            // The rest of the methods simply delegate to the existing context

            @Override
            public Object getContext(Class key) {
                return context.getContext(key);
            }

            @Override
            public Locale getLocale() {
                return context.getLocale();
            }

            @Override
            public boolean isPropertyResolved() {
                return context.isPropertyResolved();
            }

            @Override
            public void putContext(Class key, Object contextObject) {
                context.putContext(key, contextObject);
            }

            @Override
            public void setLocale(Locale locale) {
                context.setLocale(locale);
            }

            @Override
            public void setPropertyResolved(boolean resolved) {
                context.setPropertyResolved(resolved);
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return context.getFunctionMapper();
            }

            @Override
            public VariableMapper getVariableMapper() {
                return context.getVariableMapper();
            }
        };
    }

    private static class InterceptingResolver extends ELResolver {

        private ELResolver delegate;
        private ValueReference valueReference;

        public InterceptingResolver(ELResolver delegate) {
            this.delegate = delegate;
        }

        public ValueReference getValueReference() {
            return valueReference;
        }

        // Capture the base and property rather than write the value
        @Override
        public void setValue(ELContext context, Object base, Object property, Object value) {
            if (base != null && property != null) {
                context.setPropertyResolved(true);
                valueReference = new ValueReference(base, property.toString());
            }
        }

        // The rest of the methods simply delegate to the existing context

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            return delegate.getValue(context, base, property);
        }

        @Override
        public Class<?> getType(ELContext context, Object base, Object property) {
            return delegate.getType(context, base, property);
        }

        @Override
        public boolean isReadOnly(ELContext context, Object base, Object property) {
            return delegate.isReadOnly(context, base, property);
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
            return delegate.getFeatureDescriptors(context, base);
        }

        @Override
        public Class<?> getCommonPropertyType(ELContext context, Object base) {
            return delegate.getCommonPropertyType(context, base);
        }

    }
}
