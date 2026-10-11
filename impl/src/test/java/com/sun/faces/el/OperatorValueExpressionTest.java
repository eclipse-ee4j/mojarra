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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.sun.faces.el.PathValueExpressionTest.Item;

import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import jakarta.el.EvaluationListener;
import jakarta.el.ExpressionFactory;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.OptionalELResolver;
import jakarta.el.ValueExpression;

/**
 * {@link OperatorValueExpression} must give exactly what the EL implementation's own expression gives -- same values
 * of the same classes, same exceptions (class and message), same propertyResolved state -- for every operator on every
 * pair of operand types, with both Expressly and Tomcat's EL.
 */
class OperatorValueExpressionTest {

    enum Color {
        RED, GREEN
    }

    private static final Object[] VALUES = { null, 0, 2, 3L, -1L, (short) 3, (byte) 4, 2.5, 1.5f, Double.NaN, new BigDecimal("1.50"),
            new BigInteger("5"), "7", "abc", "", "2.5", "true", true, false, Color.RED, Color.GREEN, 'c', List.of(), List.of(1), Map.of(),
            new Object[0], Optional.empty(), Optional.of(1) };

    private static final String[] BINARY = { "+", "-", "*", "/", "div", "%", "mod", "==", "!=", "eq", "ne", "<", ">", "<=", ">=", "lt", "gt", "le",
            "ge", "&&", "||", "and", "or" };

    private static final String[] UNARY = { "#{-x}", "#{!x}", "#{not x}", "#{empty x}", "#{not empty x}", "#{x ? 'a' : 'b'}", "#{x ? y : 1}",
            "#{x == null}", "#{null != x}", "#{'7' == x}", "#{\"abc\" lt x}", "#{x + y * 2 - -1}", "#{(x + y) * 2}", "#{x and y and true}",
            "#{x or y or false}", "#{x ? y ? 1 : 2 : 3}" };

    private static final String[] PATHS = { "#{item.id + 1}", "#{item.price * item.id}", "#{item.price + item.price}", "#{item.active ? item.name : 'no'}",
            "#{item.missing + 1}", "#{item.broken == null}", "#{item.parent.id * 2 > 1 and not empty item.name}", "#{var.id + 1}",
            "#{var.parent.name == 'root'}", "#{nothing.name == null}", "#{item.tags.size > 1}", "#{item.id % 0}", "#{item.id / 0}",
            "#{-item.id}", "#{empty item.tags}", "#{1 + 2}", "#{-1}", "#{item.id + 1 > 2 ? 'big' : 'small'}" };

    private static final Class<?>[] TYPES = { Object.class, String.class };

    static Stream<ExpressionFactory> implementations() {
        return Stream.of(new org.glassfish.expressly.ExpressionFactoryImpl(), new org.apache.el.ExpressionFactoryImpl());
    }

    static class Roots extends PathValueExpressionTest.Roots {
    }

    private static DemuxCompositeELResolver chain(Roots roots) {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        chain.add(new ApplicationELResolvers());
        chain.addPropertyELResolver(new MapELResolver());
        chain.addPropertyELResolver(new ListELResolver());
        chain.addPropertyELResolver(new OptionalELResolver());
        chain.addPropertyELResolver(new BeanELResolver());
        chain.addRootELResolver(roots);
        return chain;
    }

    private static Roots roots() {
        Roots roots = new Roots();
        Item root = new Item(1, "root", null);
        roots.values.put("item", new Item(2, "item", root));
        roots.values.put("varSource", new Item(3, "var", root));
        return roots;
    }

    private static ELContext context(ExpressionFactory el, Roots roots) {
        ELContext context = new ELContextImpl(chain(roots));
        // like ui:param: "var" is a variable for #{varSource}
        context.getVariableMapper().setVariable("var", el.createValueExpression(context, "#{varSource}", Object.class));
        return context;
    }

    private static String outcome(ValueExpression expression, ELContext context) {
        String result;
        try {
            Object value = expression.getValue(context);
            result = value == null ? "null" : value.getClass().getName() + ":" + Objects.toString(value);
        } catch (NullPointerException e) {
            // Once the JIT compiles the throwing code, the JVM may throw a preallocated instance without message
            // (OmitStackTraceInFastThrow), whichever expression threw it.
            result = "threw " + e.getClass().getName();
        } catch (RuntimeException e) {
            result = "threw " + e.getClass().getName() + ": " + e.getMessage() + " / " + (e.getCause() == null ? null : e.getCause().getClass().getName());
        }
        return result + " resolved=" + context.isPropertyResolved();
    }

    private static int assertSameOutcomes(ExpressionFactory el, ELContext context, String expression) {
        ExpressionFactory fast = PathExpressionFactory.wrap(el);
        int operators = 0;
        for (Class<?> type : TYPES) {
            ValueExpression reference = el.createValueExpression(context, expression, type);
            ValueExpression operator = fast.createValueExpression(context, expression, type);
            if (operator instanceof OperatorValueExpression) {
                operators++;
            }
            for (int i = 0; i < 2; i++) { // cold and inline-cached
                assertEquals(outcome(reference, context), outcome(operator, context), expression + " as " + type.getSimpleName());
            }
        }
        return operators;
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void sameOutcomesForEveryOperatorAndOperandTypes(ExpressionFactory el) {
        Roots roots = roots();
        ELContext context = context(el, roots);
        List<String> expressions = new ArrayList<>();
        for (String op : BINARY) {
            expressions.add("#{x " + op + " y}");
        }
        expressions.addAll(List.of(UNARY));

        int operators = 0;
        for (Object x : VALUES) {
            for (Object y : VALUES) {
                roots.values.put("x", x);
                roots.values.put("y", y);
                for (String expression : expressions) {
                    String values = " with x=" + x + (x == null ? "" : " (" + x.getClass().getSimpleName() + ")") + ", y=" + y
                            + (y == null ? "" : " (" + y.getClass().getSimpleName() + ")");
                    try {
                        operators += assertSameOutcomes(el, context, expression);
                    } catch (AssertionError e) {
                        throw new AssertionError(e.getMessage() + values, e);
                    }
                }
            }
        }
        assertEquals(VALUES.length * VALUES.length * expressions.size() * TYPES.length, operators);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void sameOutcomesWithPaths(ExpressionFactory el) {
        Roots roots = roots();
        ELContext context = context(el, roots);
        int operators = 0;
        for (String expression : PATHS) {
            operators += assertSameOutcomes(el, context, expression);
        }
        assertEquals(PATHS.length * TYPES.length, operators);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void operandsAreEvaluatedOnce(ExpressionFactory el) {
        Roots roots = roots();
        ELContext context = context(el, roots);
        Item item = (Item) roots.values.get("item");
        roots.values.put("text", "7");
        ExpressionFactory fast = PathExpressionFactory.wrap(el);

        assertEquals(4L, (Object) fast.createValueExpression(context, "#{item.id + item.id}", Object.class).getValue(context));
        assertEquals(2, item.reads);
        // operands the fast path leaves to the EL implementation's operator
        assertEquals(9L, (Object) fast.createValueExpression(context, "#{item.id + text}", Object.class).getValue(context));
        assertEquals(3, item.reads);
        // < does not evaluate its right operand when the left one is null
        assertEquals(false, (Object) fast.createValueExpression(context, "#{nothing < item.id}", Object.class).getValue(context));
        assertEquals(3, item.reads);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void lambdaArgumentsAndListenersAreHonored(ExpressionFactory el) {
        Roots roots = roots();
        roots.values.put("x", 1);
        ELContext context = context(el, roots);
        ValueExpression expression = PathExpressionFactory.wrap(el).createValueExpression(context, "#{x + 1}", Object.class);

        context.enterLambdaScope(Map.of("x", 5));
        assertEquals(6L, (Object) expression.getValue(context));
        context.exitLambdaScope();
        assertEquals(2L, (Object) expression.getValue(context));

        List<String> events = new ArrayList<>();
        context.addEvaluationListener(new EvaluationListener() {
            @Override
            public void beforeEvaluation(ELContext context, String expression) {
                events.add("before " + expression);
            }

            @Override
            public void afterEvaluation(ELContext context, String expression) {
                events.add("after " + expression);
            }
        });
        assertEquals(2L, (Object) expression.getValue(context));
        assertEquals(List.of("before #{x + 1}", "after #{x + 1}"), events);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void onlySupportedFormsAreWrapped(ExpressionFactory el) {
        ELContext context = context(el, roots());
        ExpressionFactory fast = PathExpressionFactory.wrap(el);
        for (String expression : new String[] { "#{a + b}", "#{empty item}", "#{not item}", "#{a?b:c}", "#{a ? 'x' : \"y\"}", "#{-1}",
                "#{a.b.c * 2 ge 3}", "#{!a}", "#{a and b or c}", "${a - b}", "#{ a mod 2 eq 0 }" }) {
            assertInstanceOf(OperatorValueExpression.class, fast.createValueExpression(context, expression, Object.class), expression);
        }
        for (String expression : new String[] { "#{item['name'] + 1}", "#{item.name() + 1}", "#{a += b}", "#{x -> x + 1}", "#{(x -> x + 1)(2)}",
                "#{fn:length(a) + 1}", "#{a + 1.5}", "#{a + 1e3}", "#{'a\\'b' + x}", "#{a = 1}", "#{a; b}", "#{a instanceof b}", "#{[1,2]}",
                "#{{1:2}}", "x#{a + b}", "#{a + b}#{c}", "#{a + b} ", "#{a}", "#{1}", "#{true}", "#{(a)}", "#{a . b + 1}", "#{a.b (1)}",
                "#{a ? b : fn:f(c)}", "#{a + 12345678901234567890}", "#{a == b == c ? 1 : 2}x" }) {
            try {
                assertSame(false, fast.createValueExpression(context, expression, Object.class) instanceof OperatorValueExpression, expression);
            } catch (RuntimeException syntaxError) {
                // the EL implementation rejects it, so do we
            }
        }
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void serializesAsTheDelegate(ExpressionFactory el) throws Exception {
        Roots roots = roots();
        ELContext context = context(el, roots);
        ValueExpression expression = PathExpressionFactory.wrap(el).createValueExpression(context, "#{item.id * 2}", Object.class);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(expression);
        }
        Object restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = in.readObject();
        }
        assertEquals(((OperatorValueExpression) expression).getDelegate().getClass(), restored.getClass());
        assertEquals(4L, (Object) ((ValueExpression) restored).getValue(context));
    }
}
