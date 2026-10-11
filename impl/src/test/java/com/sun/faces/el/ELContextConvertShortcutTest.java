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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.OptionalELResolver;
import jakarta.el.VariableMapper;

/**
 * The convertToType shortcut of {@link ELContextImpl} must give exactly the results of {@link ELContext#convertToType}
 * walking the resolver chain: same values, same exceptions, same propertyResolved state afterwards.
 */
class ELContextConvertShortcutTest {

    enum Color {
        RED
    }

    /** Converts the string "magic" to 42, like an application resolver with custom coercions. */
    static class Magic extends DemuxCompositeELResolverShortcutTest.Flaky {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T convertToType(ELContext context, Object obj, Class<T> type) {
            if ("magic".equals(obj)) {
                context.setPropertyResolved(true);
                return (T) Integer.valueOf(42);
            }
            return null;
        }
    }

    private static final Object[] VALUES = { null, "", "5", "x", "RED", "true", 7, 7L, 2.5, new BigDecimal("1.50"), true, 'c', Color.RED,
            Optional.of("x"), Optional.of(5), Optional.empty(), List.of(1), "magic" };

    private static final Class<?>[] TYPES = { Object.class, String.class, Integer.class, int.class, Long.class, Double.class,
            BigDecimal.class, Boolean.class, boolean.class, Character.class, Color.class, List.class, Optional.class };

    private static ELContext reference(ELResolver resolver) {
        return new ELContext() {
            @Override
            public ELResolver getELResolver() {
                return resolver;
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

    private static String outcome(ELContext context, Object value, Class<?> type, boolean resolvedBefore) {
        context.setPropertyResolved(resolvedBefore);
        String result;
        try {
            Object converted = context.convertToType(value, type);
            result = converted == null ? "null" : converted.getClass().getName() + ":" + Objects.toString(converted);
        } catch (RuntimeException e) {
            result = "threw " + e.getClass().getName();
        }
        return result + " resolved=" + context.isPropertyResolved();
    }

    private static void assertSameAsReference(DemuxCompositeELResolver chain) {
        ELContext shortcut = new ELContextImpl(chain);
        ELContext reference = reference(chain);
        for (Object value : VALUES) {
            for (Class<?> type : TYPES) {
                for (boolean resolvedBefore : new boolean[] { false, true }) {
                    assertEquals(outcome(reference, value, type, resolvedBefore), outcome(shortcut, value, type, resolvedBefore), value + " -> " + type);
                }
            }
        }
    }

    private static DemuxCompositeELResolver chain(ApplicationELResolvers application) {
        DemuxCompositeELResolver chain = new DemuxCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.Faces);
        chain.add(application);
        chain.addPropertyELResolver(new OptionalELResolver());
        chain.addPropertyELResolver(new BeanELResolver());
        return chain;
    }

    @Test
    void knownResolversOnly() {
        DemuxCompositeELResolver chain = chain(new ApplicationELResolvers());
        assertEquals(true, chain.neverConverts("x"));
        assertEquals(false, chain.neverConverts(Optional.of("x")));
        assertSameAsReference(chain);
    }

    @Test
    void applicationResolverAddedLaterIsHonored() {
        ApplicationELResolvers application = new ApplicationELResolvers();
        DemuxCompositeELResolver chain = chain(application);
        application.add(new Magic());
        assertEquals(false, chain.neverConverts("x"));
        assertEquals(Integer.valueOf(42), new ELContextImpl(chain).convertToType("magic", Object.class));
        assertSameAsReference(chain);
    }

    @Test
    void unknownConvertingResolverIsHonored() {
        DemuxCompositeELResolver chain = chain(new ApplicationELResolvers());
        chain.add(new Magic());
        assertEquals(false, chain.neverConverts("x"));
        assertEquals(Integer.valueOf(42), new ELContextImpl(chain).convertToType("magic", Object.class));
        assertSameAsReference(chain);
    }
}
