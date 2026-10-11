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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.EvaluationListener;
import jakarta.el.ExpressionFactory;
import jakarta.el.LambdaExpression;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.el.VariableMapper;

/**
 * A value expression made of paths (see {@link PathNode}), integer, string, boolean and null literals, the arithmetic,
 * relational, logical and <code>empty</code> operators, the conditional operator and parentheses -- like
 * <code>#{row.price * row.quantity}</code>, <code>#{row.active ? 'on' : 'off'}</code> or
 * <code>#{not empty bean.list}</code> -- created by the EL implementation (the delegate) and evaluated without the EL
 * interpreter, in the interpreter's order: same operands evaluated, same short-circuits.
 *
 * <p>
 * An operator applies the coercion rules of the specification directly only to the common operand types, on which
 * Expressly and Tomcat agree: integers, floating point numbers, BigDecimal, strings, booleans, enums, collections and
 * null, and only for coercions that no resolver of the chain may take over. Any other operands are handed to a lambda
 * expression of the EL implementation applying the same operator (<code>(l, r) -&gt; l * r</code>), so the result is
 * the EL implementation's own. When that throws, or a name or property is not resolved, the whole expression is
 * evaluated by the delegate, to throw the interpreter's own exception.
 *
 * <p>
 * Lambda arguments shadowing a name and registered evaluation listeners are delegated too, as is every other method.
 * The expression serializes as its delegate.
 */
final class OperatorValueExpression extends ValueExpression {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Returned by a node when the delegate must evaluate the whole expression. */
    private static final Object DELEGATE = PathNode.UNRESOLVED;

    private final transient ValueExpression delegate;
    private final transient Node root;
    private final transient String[] names;
    private final transient Class<?> expectedType;

    private OperatorValueExpression(ValueExpression delegate, Node root, String[] names, Class<?> expectedType) {
        this.delegate = delegate;
        this.root = root;
        this.names = names;
        this.expectedType = expectedType;
    }

    /**
     * @param delegate the expression created by the EL implementation
     * @param template the parsed expression
     * @param variables the variable mapper of the context the delegate was created with, or <code>null</code>
     * @param fallbacks the operators of the EL implementation
     * @param expectedType the expected type the delegate was created with
     */
    static ValueExpression create(ValueExpression delegate, Node template, VariableMapper variables, Fallbacks fallbacks, Class<?> expectedType) {
        List<String> names = new ArrayList<>(4);
        Node root = template.instantiate(variables, fallbacks, names);
        return new OperatorValueExpression(delegate, root, names.toArray(new String[0]), expectedType);
    }

    ValueExpression getDelegate() {
        return delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(ELContext context) {
        List<EvaluationListener> listeners = context.getEvaluationListeners();
        if (listeners != null && !listeners.isEmpty()) {
            return delegate.getValue(context);
        }
        for (String name : names) {
            if (context.isLambdaArgument(name)) {
                return delegate.getValue(context);
            }
        }

        Object value = root.getValue(context, context.getELResolver());
        if (value == DELEGATE) {
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
        return obj instanceof OperatorValueExpression ? delegate.equals(((OperatorValueExpression) obj).delegate) : delegate.equals(obj);
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

    // ------------------------------------------------------------------------------------------------------ operators

    enum Op {
        NEG("(v)->-v"), NOT("(v)->!v"), EMPTY("(v)->empty v"), BOOLEAN("(v)->v?true:false"),
        ADD("(l,r)->l+r"), SUB("(l,r)->l-r"), MUL("(l,r)->l*r"), DIV("(l,r)->l/r"), MOD("(l,r)->l%r"),
        EQ("(l,r)->l==r"), NE("(l,r)->l!=r"), LT("(l,r)->l<r"), GT("(l,r)->l>r"), LE("(l,r)->l<=r"), GE("(l,r)->l>=r");

        final String lambda;

        Op(String lambda) {
            this.lambda = "#{" + lambda + "}";
        }
    }

    /**
     * The operators of the EL implementation, as lambda expressions created on first use. A lambda expression invoked
     * with a context only reads its own state, so one instance serves every thread.
     */
    static final class Fallbacks {

        private final ExpressionFactory factory;
        private final AtomicReferenceArray<LambdaExpression> lambdas = new AtomicReferenceArray<>(Op.values().length);

        Fallbacks(ExpressionFactory factory) {
            this.factory = factory;
        }

        /**
         * @return what the EL implementation's operator returns, or {@link #DELEGATE} when it throws
         */
        Object apply(Op op, ELContext context, Object... operands) {
            try {
                LambdaExpression lambda = lambdas.get(op.ordinal());
                if (lambda == null) {
                    // created in a context of its own, so that it holds nothing of the request
                    ELContext creation = new StandardELContext(factory);
                    lambda = (LambdaExpression) factory.createValueExpression(creation, op.lambda, Object.class).getValue(creation);
                    lambda.setELContext(null);
                    lambdas.set(op.ordinal(), lambda);
                }
                return lambda.invoke(context, operands);
            } catch (RuntimeException e) {
                return DELEGATE;
            }
        }
    }

    private static boolean isLong(Object obj) {
        return obj instanceof Long || obj instanceof Integer || obj instanceof Short || obj instanceof Byte;
    }

    private static boolean isDouble(Object obj) {
        return obj instanceof Double || obj instanceof Float;
    }

    /** No resolver of the chain may take over the coercion of this value (Tomcat asks the chain first). */
    private static boolean plain(ELResolver resolver, Object obj) {
        return resolver instanceof DemuxCompositeELResolver && ((DemuxCompositeELResolver) resolver).neverConverts(obj);
    }

    /** The coercion of a condition to boolean, or {@link #DELEGATE}. */
    static Object toBoolean(ELContext context, ELResolver resolver, Fallbacks fallbacks, Object obj) {
        if (obj == DELEGATE) {
            return DELEGATE;
        }
        if (plain(resolver, obj)) {
            if (obj == null) {
                return Boolean.FALSE;
            }
            if (obj instanceof Boolean) {
                return obj;
            }
            if (obj instanceof String) {
                return ((String) obj).isEmpty() ? Boolean.FALSE : Boolean.valueOf((String) obj);
            }
        }
        return fallbacks.apply(Op.BOOLEAN, context, obj);
    }

    /** The arithmetic operators on common operand types, or <code>null</code> (never an arithmetic result). */
    static Object arithmetic(Op op, Object l, Object r) {
        if (isLong(l) && isLong(r)) {
            long a = ((Number) l).longValue();
            long b = ((Number) r).longValue();
            switch (op) {
            case ADD:
                return Long.valueOf(a + b);
            case SUB:
                return Long.valueOf(a - b);
            case MUL:
                return Long.valueOf(a * b);
            case DIV:
                return Double.valueOf((double) a / (double) b);
            case MOD:
                return b == 0 ? null : Long.valueOf(a % b);
            default:
                return null;
            }
        }

        boolean numbers = (isLong(l) || isDouble(l)) && (isLong(r) || isDouble(r));
        if (numbers) {
            double a = ((Number) l).doubleValue();
            double b = ((Number) r).doubleValue();
            switch (op) {
            case ADD:
                return Double.valueOf(a + b);
            case SUB:
                return Double.valueOf(a - b);
            case MUL:
                return Double.valueOf(a * b);
            case DIV:
                return Double.valueOf(a / b);
            case MOD:
                return Double.valueOf(a % b);
            default:
                return null;
            }
        }

        if ((op == Op.ADD || op == Op.SUB || op == Op.MUL) && (l instanceof BigDecimal || r instanceof BigDecimal)
                && (l instanceof BigDecimal || isLong(l) || isDouble(l)) && (r instanceof BigDecimal || isLong(r) || isDouble(r))) {
            BigDecimal a = toBigDecimal(l);
            BigDecimal b = toBigDecimal(r);
            switch (op) {
            case ADD:
                return a.add(b);
            case SUB:
                return a.subtract(b);
            default:
                return a.multiply(b);
            }
        }

        return null;
    }

    private static BigDecimal toBigDecimal(Object obj) {
        return obj instanceof BigDecimal ? (BigDecimal) obj : new BigDecimal(((Number) obj).doubleValue());
    }

    /** == on two values that are not the same instance and not null, or <code>null</code>. */
    static Boolean equal(ELResolver resolver, Object l, Object r) {
        if (!plain(resolver, l) || !plain(resolver, r)) {
            return null;
        }
        if (isLong(l) && isLong(r)) {
            return ((Number) l).longValue() == ((Number) r).longValue();
        }
        if (l instanceof String && r instanceof String || l instanceof Boolean && r instanceof Boolean) {
            return l.equals(r);
        }
        if (l instanceof Enum && l.getClass() == r.getClass()) {
            return Boolean.FALSE;
        }
        return null;
    }

    /** The comparison of two values that are not null, or <code>null</code>. */
    static Integer compare(ELResolver resolver, Object l, Object r) {
        if (!plain(resolver, l) || !plain(resolver, r)) {
            return null;
        }
        if (isLong(l) && isLong(r)) {
            return Long.compare(((Number) l).longValue(), ((Number) r).longValue());
        }
        if (l instanceof String && r instanceof String) {
            return ((String) l).compareTo((String) r);
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------------------- nodes

    abstract static class Node {

        /** @return the value, or {@link #DELEGATE} */
        abstract Object getValue(ELContext context, ELResolver resolver);

        /** @return a node of an expression created with the given variables, collecting the names */
        abstract Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names);
    }

    static final class Literal extends Node {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        Object getValue(ELContext context, ELResolver resolver) {
            return value;
        }

        @Override
        Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names) {
            return this;
        }
    }

    static final class Path extends Node {
        /** Shared by the expressions of the same expression string, see {@link PathNode}. */
        final PathNode path;
        final ValueExpression variable;

        Path(PathNode path, ValueExpression variable) {
            this.path = path;
            this.variable = variable;
        }

        @Override
        Object getValue(ELContext context, ELResolver resolver) {
            return path.getValue(context, resolver, variable);
        }

        @Override
        Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names) {
            String name = path.name();
            if (!names.contains(name)) {
                names.add(name);
            }
            ValueExpression instanceVariable = variables == null ? null : variables.resolveVariable(name);
            return instanceVariable == null ? this : new Path(path, instanceVariable);
        }
    }

    static final class Unary extends Node {
        final Op op;
        final Node operand;
        final Fallbacks fallbacks;

        Unary(Op op, Node operand, Fallbacks fallbacks) {
            this.op = op;
            this.operand = operand;
            this.fallbacks = fallbacks;
        }

        @Override
        Object getValue(ELContext context, ELResolver resolver) {
            Object value = operand.getValue(context, resolver);
            if (value == DELEGATE) {
                return DELEGATE;
            }

            switch (op) {
            case NOT:
                Object b = toBoolean(context, resolver, fallbacks, value);
                return b == DELEGATE ? DELEGATE : Boolean.valueOf(!(Boolean) b);
            case EMPTY:
                if (value == null || value instanceof String && ((String) value).isEmpty() || value instanceof Object[] && ((Object[]) value).length == 0
                        || value instanceof Collection && ((Collection<?>) value).isEmpty() || value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
                    return Boolean.TRUE;
                }
                if (value instanceof String || value instanceof Object[] || value instanceof Collection || value instanceof Map) {
                    return Boolean.FALSE;
                }
                break;
            default: // NEG
                if (value instanceof Long) {
                    return Long.valueOf(-(Long) value);
                }
                if (value instanceof Integer) {
                    return Integer.valueOf(-(Integer) value);
                }
                if (value instanceof Double) {
                    return Double.valueOf(-(Double) value);
                }
                break;
            }

            return fallbacks.apply(op, context, value);
        }

        @Override
        Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names) {
            return new Unary(op, operand.instantiate(variables, fallbacks, names), fallbacks);
        }
    }

    static final class Binary extends Node {
        final Op op;
        final Node left;
        final Node right;
        final Fallbacks fallbacks;

        Binary(Op op, Node left, Node right, Fallbacks fallbacks) {
            this.op = op;
            this.left = left;
            this.right = right;
            this.fallbacks = fallbacks;
        }

        @Override
        Object getValue(ELContext context, ELResolver resolver) {
            Object l = left.getValue(context, resolver);
            if (l == DELEGATE) {
                return DELEGATE;
            }
            // < and > do not evaluate the right operand of a null left operand
            if (l == null && (op == Op.LT || op == Op.GT)) {
                return Boolean.FALSE;
            }
            Object r = right.getValue(context, resolver);
            if (r == DELEGATE) {
                return DELEGATE;
            }

            switch (op) {
            case EQ:
            case NE:
                Boolean equal = l == r ? Boolean.TRUE : l == null || r == null ? Boolean.FALSE : equal(resolver, l, r);
                if (equal != null) {
                    return op == Op.EQ ? equal : Boolean.valueOf(!equal);
                }
                break;
            case LT:
            case GT:
            case LE:
            case GE:
                if (l == r && (op == Op.LE || op == Op.GE)) {
                    return Boolean.TRUE;
                }
                if (l == null || r == null) {
                    return Boolean.FALSE;
                }
                Integer comparison = l == r ? Integer.valueOf(0) : compare(resolver, l, r);
                if (comparison != null) {
                    int c = comparison;
                    return Boolean.valueOf(op == Op.LT ? c < 0 : op == Op.GT ? c > 0 : op == Op.LE ? c <= 0 : c >= 0);
                }
                break;
            default:
                if (l != null && r != null) {
                    Object result = arithmetic(op, l, r);
                    if (result != null) {
                        return result;
                    }
                }
                break;
            }

            return fallbacks.apply(op, context, l, r);
        }

        @Override
        Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names) {
            return new Binary(op, left.instantiate(variables, fallbacks, names), right.instantiate(variables, fallbacks, names), fallbacks);
        }
    }

    /**
     * && or || with two or more operands, like Tomcat's interpreter: each operand coerced to boolean in turn until one
     * decides (Expressly nests two operand nodes, which only adds coercions of booleans to themselves).
     */
    static final class Logical extends Node {
        final boolean and;
        final Node[] operands;
        final Fallbacks fallbacks;

        Logical(boolean and, Node[] operands, Fallbacks fallbacks) {
            this.and = and;
            this.operands = operands;
            this.fallbacks = fallbacks;
        }

        @Override
        Object getValue(ELContext context, ELResolver resolver) {
            Object b = null;
            for (Node operand : operands) {
                b = toBoolean(context, resolver, fallbacks, operand.getValue(context, resolver));
                if (b == DELEGATE || (Boolean) b != and) {
                    return b;
                }
            }
            return b;
        }

        @Override
        Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names) {
            Node[] instances = new Node[operands.length];
            for (int i = 0; i < operands.length; i++) {
                instances[i] = operands[i].instantiate(variables, fallbacks, names);
            }
            return new Logical(and, instances, fallbacks);
        }
    }

    static final class Choice extends Node {
        final Node condition;
        final Node whenTrue;
        final Node whenFalse;
        final Fallbacks fallbacks;

        Choice(Node condition, Node whenTrue, Node whenFalse, Fallbacks fallbacks) {
            this.condition = condition;
            this.whenTrue = whenTrue;
            this.whenFalse = whenFalse;
            this.fallbacks = fallbacks;
        }

        @Override
        Object getValue(ELContext context, ELResolver resolver) {
            Object b = toBoolean(context, resolver, fallbacks, condition.getValue(context, resolver));
            if (b == DELEGATE) {
                return DELEGATE;
            }
            return ((Boolean) b ? whenTrue : whenFalse).getValue(context, resolver);
        }

        @Override
        Node instantiate(VariableMapper variables, Fallbacks fallbacks, List<String> names) {
            return new Choice(condition.instantiate(variables, fallbacks, names), whenTrue.instantiate(variables, fallbacks, names),
                    whenFalse.instantiate(variables, fallbacks, names), fallbacks);
        }
    }

    // --------------------------------------------------------------------------------------------------------- parser

    /**
     * Parses <code>#{...}</code> or <code>${...}</code> made only of what the nodes evaluate. The expression was
     * accepted by the EL implementation first, so this only has to recognize, not to report errors: anything else,
     * including whitespace inside a path, floating point literals and strings with escapes, is not parsed.
     */
    static final class Parser {

        private static final RuntimeException UNSUPPORTED = new RuntimeException(null, null, false, false) {
            @Serial
            private static final long serialVersionUID = 1L;
        };

        private final String s;
        private final int end;
        private int i;

        private Parser(String s, int start, int end) {
            this.s = s;
            this.end = end;
            i = start;
        }

        /**
         * @return the parsed expression with at least one operator, or <code>null</code>
         */
        static Node parse(String expression) {
            int length = expression.length();
            if (length < 4 || expression.charAt(0) != '#' && expression.charAt(0) != '$' || expression.charAt(1) != '{'
                    || expression.charAt(length - 1) != '}') {
                return null;
            }
            Parser parser = new Parser(expression, 2, length - 1);
            try {
                Node node = parser.choice();
                parser.spaces();
                return parser.i == parser.end && !(node instanceof Literal) && !(node instanceof Path) ? node : null;
            } catch (RuntimeException e) {
                return null;
            }
        }

        private Node choice() {
            Node condition = or();
            if (!symbol("?")) {
                return condition;
            }
            Node whenTrue = choice();
            if (!symbol(":")) {
                throw UNSUPPORTED;
            }
            return new Choice(condition, whenTrue, choice(), null);
        }

        private Node or() {
            Node node = and();
            if (!(symbol("||") || word("or"))) {
                return node;
            }
            List<Node> operands = new ArrayList<>(List.of(node, and()));
            while (symbol("||") || word("or")) {
                operands.add(and());
            }
            return new Logical(false, operands.toArray(new Node[0]), null);
        }

        private Node and() {
            Node node = equality();
            if (!(symbol("&&") || word("and"))) {
                return node;
            }
            List<Node> operands = new ArrayList<>(List.of(node, equality()));
            while (symbol("&&") || word("and")) {
                operands.add(equality());
            }
            return new Logical(true, operands.toArray(new Node[0]), null);
        }

        private Node equality() {
            Node node = relational();
            while (true) {
                if (symbol("==") || word("eq")) {
                    node = new Binary(Op.EQ, node, relational(), null);
                } else if (symbol("!=") || word("ne")) {
                    node = new Binary(Op.NE, node, relational(), null);
                } else {
                    return node;
                }
            }
        }

        private Node relational() {
            Node node = additive();
            while (true) {
                Op op;
                if (symbol("<=") || word("le")) {
                    op = Op.LE;
                } else if (symbol(">=") || word("ge")) {
                    op = Op.GE;
                } else if (symbol("<") || word("lt")) {
                    op = Op.LT;
                } else if (symbol(">") || word("gt")) {
                    op = Op.GT;
                } else {
                    return node;
                }
                node = new Binary(op, node, additive(), null);
            }
        }

        private Node additive() {
            Node node = multiplicative();
            while (true) {
                spaces();
                if (i + 1 < end && (s.startsWith("+=", i) || s.startsWith("->", i))) {
                    throw UNSUPPORTED;
                }
                if (symbol("+")) {
                    node = new Binary(Op.ADD, node, multiplicative(), null);
                } else if (symbol("-")) {
                    node = new Binary(Op.SUB, node, multiplicative(), null);
                } else {
                    return node;
                }
            }
        }

        private Node multiplicative() {
            Node node = unary();
            while (true) {
                Op op;
                if (symbol("*")) {
                    op = Op.MUL;
                } else if (symbol("/") || word("div")) {
                    op = Op.DIV;
                } else if (symbol("%") || word("mod")) {
                    op = Op.MOD;
                } else {
                    return node;
                }
                node = new Binary(op, node, unary(), null);
            }
        }

        private Node unary() {
            spaces();
            if (s.startsWith("->", i)) {
                throw UNSUPPORTED;
            }
            if (symbol("-")) {
                return new Unary(Op.NEG, unary(), null);
            }
            if (!s.startsWith("!=", i) && symbol("!") || word("not")) {
                return new Unary(Op.NOT, unary(), null);
            }
            if (word("empty")) {
                return new Unary(Op.EMPTY, unary(), null);
            }
            return primary();
        }

        private Node primary() {
            spaces();
            if (i == end) {
                throw UNSUPPORTED;
            }
            char c = s.charAt(i);

            if (c == '(') {
                i++;
                Node node = choice();
                if (!symbol(")")) {
                    throw UNSUPPORTED;
                }
                return node;
            }

            if (c >= '0' && c <= '9') {
                int start = i;
                while (i < end && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                    i++;
                }
                if (i - start > 18 || i < end && (s.charAt(i) == '.' || s.charAt(i) == 'e' || s.charAt(i) == 'E' || isIdentifierPart(s.charAt(i)))) {
                    throw UNSUPPORTED;
                }
                return new Literal(Long.valueOf(s.substring(start, i)));
            }

            if (c == '\'' || c == '"') {
                int close = s.indexOf(c, i + 1);
                if (close < 0 || close >= end || s.indexOf('\\', i + 1) >= 0 && s.indexOf('\\', i + 1) < close) {
                    throw UNSUPPORTED;
                }
                String value = s.substring(i + 1, close);
                i = close + 1;
                return new Literal(value);
            }

            if (!isIdentifierStart(c)) {
                throw UNSUPPORTED;
            }
            List<String> path = new ArrayList<>(4);
            path.add(identifier());
            String first = path.get(0);
            switch (first) {
            case "true":
                return new Literal(Boolean.TRUE);
            case "false":
                return new Literal(Boolean.FALSE);
            case "null":
                return new Literal(null);
            default:
                if (PathExpressionFactory.isReserved(first)) {
                    throw UNSUPPORTED;
                }
            }
            while (i < end && s.charAt(i) == '.') {
                i++;
                if (i == end || !isIdentifierStart(s.charAt(i))) {
                    throw UNSUPPORTED;
                }
                String property = identifier();
                if (PathExpressionFactory.isReserved(property)) {
                    throw UNSUPPORTED;
                }
                path.add(property);
            }
            // not a method call, a bracket suffix, a function or a lambda
            int next = i;
            while (next < end && Character.isWhitespace(s.charAt(next))) {
                next++;
            }
            if (next < end && (s.charAt(next) == '(' || s.charAt(next) == '[' || s.charAt(next) == '.')) {
                throw UNSUPPORTED;
            }
            return new Path(new PathNode(path.toArray(new String[0])), null);
        }

        private String identifier() {
            int start = i;
            i++;
            while (i < end && isIdentifierPart(s.charAt(i))) {
                i++;
            }
            if (i < end && s.charAt(i) > 127) {
                throw UNSUPPORTED;
            }
            return s.substring(start, i);
        }

        private void spaces() {
            while (i < end && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private boolean symbol(String symbol) {
            spaces();
            if (s.startsWith(symbol, i) && i + symbol.length() <= end) {
                i += symbol.length();
                return true;
            }
            return false;
        }

        private boolean word(String word) {
            spaces();
            int after = i + word.length();
            if (after <= end && s.startsWith(word, i) && (after == end || !isIdentifierPart(s.charAt(after)) && s.charAt(after) <= 127)) {
                i = after;
                return true;
            }
            return false;
        }

        private static boolean isIdentifierStart(char c) {
            return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$';
        }

        private static boolean isIdentifierPart(char c) {
            return isIdentifierStart(c) || c >= '0' && c <= '9';
        }
    }
}
