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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.StaticFieldELResolver;
import jakarta.el.VariableMapper;

/**
 * The getValue shortcuts of {@link DemuxCompositeELResolver} must give exactly the results of walking the whole chain:
 * known resolvers that cannot resolve are skipped, unknown resolvers are always consulted.
 */
class DemuxCompositeELResolverShortcutTest {

    public static class Bean {
        public String getName() {
            return "bean";
        }
    }

    /** An application resolver resolving a root name or a bean property only on every other call. */
    static class Flaky extends ELResolver {
        int calls;

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            calls++;
            if (calls % 2 == 0 && ("x".equals(property) || base instanceof Bean && "name".equals(property))) {
                context.setPropertyResolved(base, property);
                return "flaky";
            }
            return null;
        }

        @Override
        public Class<?> getType(ELContext context, Object base, Object property) {
            return null;
        }

        @Override
        public void setValue(ELContext context, Object base, Object property, Object value) {
        }

        @Override
        public boolean isReadOnly(ELContext context, Object base, Object property) {
            return true;
        }

        @Override
        public Class<?> getCommonPropertyType(ELContext context, Object base) {
            return null;
        }
    }

    /** Resolves every root name to a fixed value, like the scoped attribute resolver. */
    static class AnyRoot extends Flaky {
        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            calls++;
            if (base == null) {
                context.setPropertyResolved(null, property);
                return "root:" + property;
            }
            return null;
        }
    }

    private static ELContext context() {
        return new ELContext() {
            @Override
            public ELResolver getELResolver() {
                return null;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return null;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return null;
            }
        };
    }

    @Test
    void unknownResolversAreAlwaysConsulted() {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        Flaky flaky = new Flaky();
        AnyRoot anyRoot = new AnyRoot();
        chain.addRootELResolver(new StaticFieldELResolver());
        chain.add(flaky);
        chain.addPropertyELResolver(new MapELResolver());
        chain.addPropertyELResolver(new ListELResolver());
        chain.addPropertyELResolver(new BeanELResolver());
        chain.addRootELResolver(anyRoot);

        ELContext context = context();
        for (int i = 1; i <= 6; i++) {
            // flaky sees the root lookup on odd calls (declines) and the property lookup on even calls (resolves)
            assertEquals("root:x", chain.getValue(context, null, "x"));
            assertTrue(context.isPropertyResolved());
            assertEquals("flaky", chain.getValue(context, new Bean(), "name"));
            assertTrue(context.isPropertyResolved());
        }
        assertEquals(12, flaky.calls);
    }

    @Test
    void knownResolversAreSkippedWithSameResults() {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        AnyRoot anyRoot = new AnyRoot();
        chain.addRootELResolver(new StaticFieldELResolver());
        chain.add(new EmptyStringToNullELResolver());
        chain.addPropertyELResolver(new MapELResolver());
        chain.addPropertyELResolver(new ListELResolver());
        chain.addPropertyELResolver(new BeanELResolver());
        chain.addRootELResolver(anyRoot);

        ELContext context = context();
        for (int i = 0; i < 3; i++) {
            assertEquals("root:row", chain.getValue(context, null, "row"));
            assertEquals("v", chain.getValue(context, Map.of("k", "v"), "k"));
            assertTrue(context.isPropertyResolved());
            assertEquals("b", chain.getValue(context, List.of("a", "b"), 1));
            assertEquals("bean", chain.getValue(context, new Bean(), "name"));
        }
        assertEquals(3, anyRoot.calls);

    }

    @Test
    void applicationResolverAddedLaterIsHonored() {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        ApplicationELResolvers application = new ApplicationELResolvers();
        AnyRoot anyRoot = new AnyRoot();
        chain.addRootELResolver(new StaticFieldELResolver());
        chain.add(application);
        chain.addRootELResolver(anyRoot);

        ELContext context = context();
        assertEquals("root:row", chain.getValue(context, null, "row"));
        assertEquals("root:row", chain.getValue(context, null, "row"));

        AnyRoot late = new AnyRoot() {
            @Override
            public Object getValue(ELContext context, Object base, Object property) {
                calls++;
                context.setPropertyResolved(base, property);
                return "late";
            }
        };
        application.add(late);
        chain.clearShortcuts(); // as ExpressionLanguage.addELResolver does
        assertEquals("late", chain.getValue(context, null, "row"));
        assertEquals(1, late.calls);
    }

    @Test
    void unresolvedStaysUnresolved() {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        chain.addRootELResolver(new StaticFieldELResolver());
        chain.addPropertyELResolver(new MapELResolver());
        ELContext context = context();
        for (int i = 0; i < 2; i++) {
            assertNull(chain.getValue(context, null, "nothing"));
            assertFalse(context.isPropertyResolved());
            assertNull(chain.getValue(context, new Bean(), "name"));
            assertFalse(context.isPropertyResolved());
        }
    }

    /** Like the resolvers of Weld and OpenWebBeans: marks a bean name resolved only when the bean is not null. */
    static class CdiLike extends Flaky {
        final Map<String, Object> beans;

        CdiLike(Map<String, Object> beans) {
            this.beans = beans;
        }

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            calls++;
            if (base == null) {
                Object bean = beans.get(property);
                if (bean != null) {
                    context.setPropertyResolved(true);
                    return bean;
                }
            }
            return null;
        }
    }

    private static DemuxCompositeELResolver chainWith(CdiLike cdi, AnyRoot last) {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        chain.add(cdi);
        chain.addRootELResolver(new StaticFieldELResolver());
        chain.addPropertyELResolver(new MapELResolver());
        chain.addRootELResolver(last);
        return chain;
    }

    @Test
    void beanWhichWasNullIsAskedAgain() {
        Map<String, Object> beans = new HashMap<>();
        CdiLike cdi = new CdiLike(beans);
        DemuxCompositeELResolver chain = chainWith(cdi, new AnyRoot());
        chain.setCdiNames(cdi, () -> Set.of("user"));

        ELContext context = context();
        assertEquals("root:user", chain.getValue(context, null, "user"));
        assertEquals("root:user", chain.getValue(context, null, "user"));
        beans.put("user", "alice");
        assertEquals("alice", chain.getValue(context, null, "user"));
    }

    @Test
    void cdiIsSkippedOnlyForNamesWhichAreNoBeanNorNamespace() {
        CdiLike cdi = new CdiLike(Map.of());
        DemuxCompositeELResolver chain = chainWith(cdi, new AnyRoot());
        chain.setCdiNames(cdi, () -> Set.of("user", "com", "com.acme", "com.acme.bean"));

        ELContext context = context();
        for (int i = 0; i < 3; i++) {
            assertEquals("root:row", chain.getValue(context, null, "row"));
        }
        assertEquals(1, cdi.calls); // only the first lookup of "row" walks the whole chain
        for (int i = 0; i < 3; i++) {
            assertEquals("root:com", chain.getValue(context, null, "com"));
        }
        assertEquals(4, cdi.calls);
    }

    @Test
    void cdiIsAlwaysAskedWhenBeanNamesAreUnknown() {
        CdiLike cdi = new CdiLike(Map.of());
        DemuxCompositeELResolver chain = chainWith(cdi, new AnyRoot());
        chain.setCdiNames(cdi, () -> {
            throw new IllegalStateException("not deployed yet");
        });

        ELContext context = context();
        for (int i = 0; i < 3; i++) {
            assertEquals("root:row", chain.getValue(context, null, "row"));
        }
        assertEquals(3, cdi.calls);

        CdiLike unnamed = new CdiLike(Map.of());
        DemuxCompositeELResolver withoutBeanManager = chainWith(unnamed, new AnyRoot());
        for (int i = 0; i < 3; i++) {
            assertEquals("root:row", withoutBeanManager.getValue(context, null, "row"));
        }
        assertEquals(3, unnamed.calls);
    }

    /** A namespace object of a CDI implementation which is neither Weld nor OpenWebBeans. */
    static class Namespace {
    }

    /** Resolves the root name "ns" to a {@link Namespace}, and its property "bean", like a CDI resolver does. */
    static class NamespaceCdi extends Flaky {
        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            calls++;
            if (base == null && "ns".equals(property)) {
                context.setPropertyResolved(true);
                return new Namespace();
            }
            if (base instanceof Namespace && "bean".equals(property)) {
                context.setPropertyResolved(true);
                return "bean";
            }
            return null;
        }
    }

    @Test
    void cdiResolverOfUnknownImplementationIsAskedForPropertiesOfItsNamespaces() {
        NamespaceCdi cdi = new NamespaceCdi();
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        chain.add(cdi);
        chain.addPropertyELResolver(new MapELResolver());
        chain.setCdiNames(cdi, () -> Set.of("ns", "ns.bean"));

        ELContext context = context();
        for (int i = 0; i < 2; i++) {
            Object namespace = chain.getValue(context, null, "ns");
            assertEquals("bean", chain.getValue(context, namespace, "bean"));
            assertTrue(context.isPropertyResolved());
        }
    }

    @Test
    void disabledShortcutsWalkTheWholeChain() {
        CdiLike cdi = new CdiLike(Map.of());
        DemuxCompositeELResolver chain = chainWith(cdi, new AnyRoot());
        chain.setCdiNames(cdi, () -> Set.of());
        chain.disableShortcuts();

        ELContext context = context();
        for (int i = 0; i < 3; i++) {
            assertEquals("root:row", chain.getValue(context, null, "row"));
            assertEquals("v", chain.getValue(context, Map.of("k", "v"), "k"));
        }
        assertEquals(6, cdi.calls);
    }
}
