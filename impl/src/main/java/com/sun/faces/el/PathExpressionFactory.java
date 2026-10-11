/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

/**
 * Wraps the EL implementation's expression factory so that value expressions of the form <code>#{name}</code> or
 * <code>#{name.property...}</code> are evaluated by {@link PathValueExpression}, and those made of paths, literals and
 * operators by {@link OperatorValueExpression}. Everything is created by the wrapped factory first, so syntax errors and
 * every other expression are exactly the wrapped factory's. Set the system property
 * <code>com.sun.faces.disablePathExpressions=true</code> to not wrap, or
 * <code>com.sun.faces.disableOperatorExpressions=true</code> to only wrap paths.
 */
public final class PathExpressionFactory extends ExpressionFactory {

    private static final boolean ENABLED = !Boolean.getBoolean("com.sun.faces.disablePathExpressions");
    private static final boolean OPERATORS = !Boolean.getBoolean("com.sun.faces.disableOperatorExpressions");

    /** EL reserved words: never a name or a property in a valid expression of this form. */
    private static final Set<String> RESERVED = Set.of("and", "or", "not", "eq", "ne", "lt", "gt", "le", "ge", "true", "false", "null", "instanceof",
            "empty", "div", "mod", "cat");

    private static final Map<String, Object> FORMS = new ConcurrentHashMap<>();
    private static final Object OTHER = new Object();
    private static final int MAX_FORMS = 10_000;

    private final ExpressionFactory wrapped;
    private final OperatorValueExpression.Fallbacks fallbacks;

    private PathExpressionFactory(ExpressionFactory wrapped) {
        this.wrapped = wrapped;
        fallbacks = new OperatorValueExpression.Fallbacks(wrapped);
    }

    /**
     * @param factory the EL implementation's expression factory
     * @return the factory wrapped, unless disabled, <code>null</code> or already wrapped
     */
    public static ExpressionFactory wrap(ExpressionFactory factory) {
        if (!ENABLED || factory == null || factory instanceof PathExpressionFactory) {
            return factory;
        }
        return new PathExpressionFactory(factory);
    }

    public ExpressionFactory getWrapped() {
        return wrapped;
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        ValueExpression created = wrapped.createValueExpression(context, expression, expectedType);

        Object form = cachedForm(expression);
        if (form == OTHER) {
            return created;
        }

        VariableMapper variables = context == null ? null : context.getVariableMapper();
        if (form instanceof PathNode) {
            PathNode path = (PathNode) form;
            ValueExpression variable = variables == null ? null : variables.resolveVariable(path.name());
            return new PathValueExpression(created, path, variable, expectedType);
        }
        return OperatorValueExpression.create(created, (OperatorValueExpression.Node) form, variables, fallbacks, expectedType);
    }

    /**
     * Expressions are created again and again (on every build of a dynamic tree, in every c:forEach iteration), so the
     * parsed form of each expression string is remembered, like the EL implementation remembers its syntax tree.
     *
     * @return a path node shared by the expressions of this string, a node template, or {@link #OTHER}
     */
    private static Object cachedForm(String expression) {
        Object form = FORMS.get(expression);
        if (form == null) {
            List<String> path = parsePath(expression);
            if (path != null) {
                form = new PathNode(path.toArray(new String[0]));
            } else {
                form = OPERATORS ? OperatorValueExpression.Parser.parse(expression) : null;
                if (form == null) {
                    form = OTHER;
                }
            }
            if (FORMS.size() < MAX_FORMS) {
                FORMS.put(expression, form);
            }
        }
        return form;
    }

    static boolean isReserved(String identifier) {
        return RESERVED.contains(identifier);
    }

    /**
     * @param expression an expression string
     * @return the name and the properties when the expression is exactly <code>#{name.property...}</code> or
     * <code>${name.property...}</code> with plain ASCII identifiers, otherwise <code>null</code>
     */
    static List<String> parsePath(String expression) {
        int length = expression.length();
        if (length < 4 || expression.charAt(0) != '#' && expression.charAt(0) != '$' || expression.charAt(1) != '{'
                || expression.charAt(length - 1) != '}') {
            return null;
        }

        List<String> path = new ArrayList<>(4);
        int end = length - 1;
        int i = 2;
        while (true) {
            int start = i;
            if (i == end || !isIdentifierStart(expression.charAt(i))) {
                return null;
            }
            i++;
            while (i < end && isIdentifierPart(expression.charAt(i))) {
                i++;
            }
            String identifier = expression.substring(start, i);
            if (RESERVED.contains(identifier)) {
                return null;
            }
            path.add(identifier);
            if (i == end) {
                return path;
            }
            if (expression.charAt(i) != '.') {
                return null;
            }
            i++;
        }
    }

    private static boolean isIdentifierStart(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$';
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || c >= '0' && c <= '9';
    }

    @Override
    public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
        return wrapped.createValueExpression(instance, expectedType);
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType,
            Class<?>[] expectedParamTypes) {
        return wrapped.createMethodExpression(context, expression, expectedReturnType, expectedParamTypes);
    }

    @Override
    public <T> T coerceToType(Object obj, Class<T> targetType) {
        return wrapped.coerceToType(obj, targetType);
    }

    @Override
    public ELResolver getStreamELResolver() {
        return wrapped.getStreamELResolver();
    }

    @Override
    public Map<String, Method> getInitFunctionMap() {
        return wrapped.getInitFunctionMap();
    }
}
