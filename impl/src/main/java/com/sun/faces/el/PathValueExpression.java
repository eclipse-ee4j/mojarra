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

import java.io.Serial;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.EvaluationListener;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;

/**
 * A value expression of the form <code>#{name}</code> or <code>#{name.property.property...}</code> -- the large
 * majority of the expressions of a page -- created by the EL implementation (the delegate) and evaluated by a
 * {@link PathNode} without the EL interpreter, then converted to the expected type with
 * {@link ELContext#convertToType}, like the interpreter does.
 *
 * <p>
 * Everything else is delegated: lambda arguments shadowing the name, registered evaluation listeners, an unresolved
 * name or property (to throw the interpreter's own exception), and every other method. The expression serializes as
 * its delegate.
 */
final class PathValueExpression extends ValueExpression {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ValueExpression delegate;
    private final transient PathNode node;
    private final transient ValueExpression variable;
    private final transient Class<?> expectedType;

    /**
     * @param delegate the expression created by the EL implementation
     * @param node the path, shared by the expressions of the same expression string
     * @param variable the variable the variable mapper had for the name when the delegate was created, or
     * <code>null</code>
     * @param expectedType the expected type the delegate was created with
     */
    PathValueExpression(ValueExpression delegate, PathNode node, ValueExpression variable, Class<?> expectedType) {
        this.delegate = delegate;
        this.node = node;
        this.variable = variable;
        this.expectedType = expectedType;
    }

    ValueExpression getDelegate() {
        return delegate;
    }

    PathNode node() {
        return node;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(ELContext context) {
        List<EvaluationListener> listeners = context.getEvaluationListeners();
        if (listeners != null && !listeners.isEmpty() || context.isLambdaArgument(node.name())) {
            return delegate.getValue(context);
        }

        Object value = node.getValue(context, context.getELResolver(), variable);
        if (value == PathNode.UNRESOLVED) {
            return delegate.getValue(context);
        }

        if (expectedType != null) {
            value = context.convertToType(value, expectedType);
        }

        return (T) value;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        delegate.setValue(context, value);
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        return delegate.isReadOnly(context);
    }

    @Override
    public Class<?> getType(ELContext context) {
        return delegate.getType(context);
    }

    @Override
    public Class<?> getExpectedType() {
        return delegate.getExpectedType();
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        return delegate.getValueReference(context);
    }

    @Override
    public String getExpressionString() {
        return delegate.getExpressionString();
    }

    @Override
    public boolean isLiteralText() {
        return delegate.isLiteralText();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PathValueExpression ? delegate.equals(((PathValueExpression) obj).delegate) : delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Serial
    private Object writeReplace() {
        return delegate;
    }
}
