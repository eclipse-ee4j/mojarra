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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.EvaluationListener;
import jakarta.el.ExpressionFactory;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.OptionalELResolver;
import jakarta.el.RecordELResolver;
import jakarta.el.ValueExpression;

/**
 * {@link PathValueExpression} must give exactly what the EL implementation's own expression gives: same values, same
 * exceptions (class and message), same propertyResolved state, for every kind of base the Faces chain resolves.
 */
public class PathValueExpressionTest {

    public static class Item {
        private final int id;
        private final String name;
        private final Item parent;
        int reads;

        public Item(int id, String name, Item parent) {
            this.id = id;
            this.name = name;
            this.parent = parent;
        }

        public int getId() {
            reads++;
            return id;
        }

        public String getName() {
            return name;
        }

        public Item getParent() {
            return parent;
        }

        public boolean isActive() {
            return id % 2 == 0;
        }

        public BigDecimal getPrice() {
            return BigDecimal.valueOf(id, 2);
        }

        public List<String> getTags() {
            return List.of("a", "b");
        }

        public Map<String, Object> getMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("k", "v");
            map.put("item", this);
            return map;
        }

        public Optional<String> getMaybe() {
            return Optional.ofNullable(name);
        }

        public String getBroken() {
            throw new IllegalStateException("broken " + id);
        }

        public void setWriteOnly(String value) {
        }
    }

    public static class SubItem extends Item {
        public SubItem() {
            super(7, "sub", null);
        }

        @Override
        public String getName() {
            return "overridden";
        }
    }

    /** Not public: no direct reader, goes through the chain. */
    static class Hidden {
        public String getName() {
            return "hidden";
        }
    }

    public record Point(int x, int y) {
    }

    /** Resolves root names from a map, like the scoped attribute resolver: always resolved, possibly to null. */
    static class Roots extends DemuxCompositeELResolverShortcutTest.Flaky {
        final Map<String, Object> values = new HashMap<>();

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            if (base == null) {
                context.setPropertyResolved(true);
                return values.get(property);
            }
            return null;
        }
    }

    // Expressly explicitly: Tomcat's EL, on the test classpath too, rejects #{item.class}
    private static final ExpressionFactory EL = new org.glassfish.expressly.ExpressionFactoryImpl();
    private static final ExpressionFactory PATHS = PathExpressionFactory.wrap(EL);

    private static final String[] EXPRESSIONS = { "#{item}", "#{item.id}", "#{item.name}", "#{item.active}", "#{item.price}", "#{item.parent}",
            "#{item.parent.name}", "#{item.parent.parent}", "#{item.parent.parent.name}", "#{item.tags}", "#{item.map.k}", "#{item.map.item.name}",
            "#{item.map.missing}", "#{item.map.missing.name}", "#{item.maybe}", "#{item.maybe.length}", "#{item.missing}", "#{item.broken}",
            "#{item.writeOnly}", "#{item.class}", "#{item.class.simpleName}", "#{sub.name}", "#{sub.id}", "#{hidden.name}", "#{point.x}",
            "#{point.z}", "#{list.size}", "#{nothing}", "#{nothing.name}", "#{map.k}", "#{map.item.parent.name}", "#{array.length}", "#{opt.name}",
            "${item.name}", "#{var.name}", "#{var.parent.name}", "#{text}", "#{text.blank}", "#{number}" };

    private static final Class<?>[] TYPES = { Object.class, String.class, Integer.class, Boolean.class };

    private static DemuxCompositeELResolver chain(Roots roots) {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        chain.add(new ApplicationELResolvers());
        chain.addPropertyELResolver(new MapELResolver());
        chain.addPropertyELResolver(new ListELResolver());
        chain.addPropertyELResolver(new ArrayELResolver());
        chain.addPropertyELResolver(new OptionalELResolver());
        chain.addPropertyELResolver(new RecordELResolver());
        chain.addPropertyELResolver(new BeanELResolver());
        chain.addRootELResolver(roots);
        return chain;
    }

    private static Roots roots() {
        Roots roots = new Roots();
        Item root = new Item(1, "root", null);
        Item item = new Item(2, "item", root);
        roots.values.put("item", item);
        roots.values.put("sub", new SubItem());
        roots.values.put("hidden", new Hidden());
        roots.values.put("point", new Point(3, 4));
        roots.values.put("list", new ArrayList<>(List.of(1, 2)));
        roots.values.put("map", item.getMap());
        roots.values.put("array", new int[] { 1, 2, 3 });
        roots.values.put("opt", Optional.of(item));
        roots.values.put("text", "42");
        roots.values.put("number", 42);
        roots.values.put("varSource", new Item(3, "var", root));
        return roots;
    }

    private static ELContext context(ELResolver chain) {
        ELContext context = new ELContextImpl(chain);
        // like ui:param: "var" is a variable for #{varSource}
        context.getVariableMapper().setVariable("var", EL.createValueExpression(context, "#{varSource}", Object.class));
        return context;
    }

    private static String outcome(ValueExpression expression, ELContext context) {
        String result;
        try {
            Object value = expression.getValue(context);
            result = value == null ? "null" : value.getClass().getName() + ":" + Objects.toString(value);
        } catch (RuntimeException e) {
            result = "threw " + e.getClass().getName() + ": " + e.getMessage() + " / " + (e.getCause() == null ? null : e.getCause().getClass().getName());
        }
        return result + " resolved=" + context.isPropertyResolved();
    }

    @Test
    void sameOutcomesAsTheELImplementation() {
        Roots roots = roots();
        DemuxCompositeELResolver chain = chain(roots);
        ELContext context = context(chain);
        int paths = 0;
        for (String expression : EXPRESSIONS) {
            for (Class<?> type : TYPES) {
                ValueExpression reference = EL.createValueExpression(context, expression, type);
                ValueExpression path = PATHS.createValueExpression(context, expression, type);
                if (path instanceof PathValueExpression) {
                    paths++;
                }
                for (int i = 0; i < 3; i++) { // cold and inline-cached
                    assertEquals(outcome(reference, context), outcome(path, context), expression + " as " + type.getSimpleName());
                }
            }
        }
        assertEquals(EXPRESSIONS.length * TYPES.length, paths);
    }

    @Test
    void inlineCacheFollowsTheBaseClass() {
        Roots roots = roots();
        DemuxCompositeELResolver chain = chain(roots);
        ELContext context = context(chain);
        ValueExpression path = PATHS.createValueExpression(context, "#{item.name}", Object.class);
        Object[] bases = { new Item(1, "one", null), new SubItem(), Map.of("name", "map"), new Hidden(), new Point(1, 2), new Item(2, "two", null) };
        for (int round = 0; round < 3; round++) {
            for (Object base : bases) {
                roots.values.put("item", base);
                ValueExpression reference = EL.createValueExpression(context, "#{item.name}", Object.class);
                assertEquals(outcome(reference, context), outcome(path, context), base.getClass().getName());
            }
        }
    }

    @Test
    void getterIsCalledOnce() {
        Roots roots = roots();
        ELContext context = context(chain(roots));
        Item item = (Item) roots.values.get("item");
        ValueExpression path = PATHS.createValueExpression(context, "#{item.id}", Object.class);
        path.getValue(context);
        path.getValue(context);
        assertEquals(2, item.reads);
    }

    @Test
    void lambdaArgumentAndListenersAreHonored() {
        Roots roots = roots();
        ELContext context = context(chain(roots));
        ValueExpression path = PATHS.createValueExpression(context, "#{item.name}", Object.class);

        context.enterLambdaScope(Map.of("item", new Item(9, "lambda", null)));
        assertEquals("lambda", path.getValue(context));
        context.exitLambdaScope();

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

            @Override
            public void propertyResolved(ELContext context, Object base, Object property) {
                events.add("resolved " + property);
            }
        });
        assertEquals("item", path.getValue(context));
        assertEquals(List.of("before #{item.name}", "resolved name", "after #{item.name}"), events);
    }

    @Test
    void applicationResolverAddedLaterIsHonored() {
        Roots roots = roots();
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        ApplicationELResolvers application = new ApplicationELResolvers();
        chain.add(application);
        chain.addPropertyELResolver(new BeanELResolver());
        chain.addRootELResolver(roots);
        ELContext context = context(chain);
        ValueExpression path = PATHS.createValueExpression(context, "#{item.name}", Object.class);
        assertEquals("item", path.getValue(context));

        application.add(new DemuxCompositeELResolverShortcutTest.Flaky() {
            @Override
            public Object getValue(ELContext context, Object base, Object property) {
                if (base instanceof Item && "name".equals(property)) {
                    context.setPropertyResolved(base, property);
                    return "intercepted";
                }
                return null;
            }
        });
        chain.clearShortcuts(); // as ExpressionLanguage.addELResolver does
        assertEquals("intercepted", path.getValue(context));
    }

    @Test
    void knownApplicationResolversDoNotDisableTheShortcuts() {
        // Weld adds its resolver through Application#addELResolver: known resolvers there must not act as barriers.
        Roots roots = roots();
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        ApplicationELResolvers application = new ApplicationELResolvers();
        application.add(new MapELResolver());
        chain.add(application);
        chain.addPropertyELResolver(new OptionalELResolver());
        chain.addPropertyELResolver(new BeanELResolver());
        chain.addRootELResolver(roots);
        assertSame(true, chain.beanReaders().get(Item.class).beanResolver() != null);
        assertSame(null, chain.beanReaders().get(HashMap.class).beanResolver());
        assertSame(true, chain.neverConverts("x"));

        ELContext context = context(chain);
        for (String expression : EXPRESSIONS) {
            ValueExpression reference = EL.createValueExpression(context, expression, Object.class);
            ValueExpression path = PATHS.createValueExpression(context, expression, Object.class);
            assertEquals(outcome(reference, context), outcome(path, context), expression);
        }

        application.add(new ELContextConvertShortcutTest.Magic());
        chain.clearShortcuts();
        assertSame(false, chain.neverConverts("x"));
        assertSame(null, chain.beanReaders().get(Item.class).beanResolver());
    }

    @Test
    void onlyPlainPathsAreWrapped() {
        ELContext context = context(chain(roots()));
        for (String expression : new String[] { "#{item.name}", "${item}", "#{a.b.c.d}", "#{_x.$y}" }) {
            assertInstanceOf(PathValueExpression.class, PATHS.createValueExpression(context, expression, Object.class), expression);
        }
        for (String expression : new String[] { "#{item['name']}", "#{item.name()}", "#{ item.name }", "#{a + b}", "#{empty item}", "#{not item}",
                "#{item.name}#{item.id}", "x#{item}", "#{item} ", "#{true}", "#{null}", "#{item.empty}", "#{1}", "#{x -> x}", "plain", "#{a?b:c}" }) {
            try {
                assertSame(false, PATHS.createValueExpression(context, expression, Object.class) instanceof PathValueExpression, expression);
            } catch (RuntimeException syntaxError) {
                // the EL implementation rejects it, so do we
            }
        }
        assertNull(PathExpressionFactory.parsePath("#{}"));
        assertEquals(List.of("a", "b"), PathExpressionFactory.parsePath("#{a.b}"));
    }

    @Test
    void expressionsOfTheSameStringShareTheNodeButNotTheVariable() {
        // Facelets creates the expressions of template text again on every build: they share one path node, and so
        // its inline cache, while each keeps the variable its context had.
        Roots roots = roots();
        ELContext context = context(chain(roots));
        ValueExpression first = PATHS.createValueExpression(context, "#{var.name}", Object.class);
        context.getVariableMapper().setVariable("var", EL.createValueExpression(context, "#{item}", Object.class));
        ValueExpression second = PATHS.createValueExpression(context, "#{var.name}", Object.class);
        assertSame(((PathValueExpression) first).node(), ((PathValueExpression) second).node());
        assertEquals("var", (Object) first.getValue(context));
        assertEquals("item", (Object) second.getValue(context));
        assertEquals("var", (Object) first.getValue(context));
    }

    @Test
    void serializesAsTheDelegate() throws Exception {
        ELContext context = context(chain(roots()));
        ValueExpression path = PATHS.createValueExpression(context, "#{item.name}", String.class);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(path);
        }
        Object restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = in.readObject();
        }
        assertEquals(((PathValueExpression) path).getDelegate().getClass(), restored.getClass());
        assertEquals("item", ((ValueExpression) restored).getValue(context));
    }
}
