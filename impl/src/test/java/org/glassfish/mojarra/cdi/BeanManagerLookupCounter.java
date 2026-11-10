/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.cdi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

/**
 * Counts how often each lookup method of a Mockito {@link BeanManager} was consulted, so that a cache hit can be told apart from a resolution.
 */
final class BeanManagerLookupCounter {

    final AtomicInteger getBeansByType = new AtomicInteger();
    final AtomicInteger getBeansByName = new AtomicInteger();
    final AtomicInteger resolves = new AtomicInteger();
    final AtomicInteger references = new AtomicInteger();
    final BeanManager beanManager = mock(BeanManager.class);

    /**
     * Creates a bean manager which resolves nothing.
     */
    BeanManagerLookupCounter() {
        this(null, null);
    }

    /**
     * Creates a bean manager which resolves every lookup by type to the given bean, whose reference is the given instance.
     *
     * @param bean the bean to resolve, or {@code null} to resolve nothing
     * @param reference the reference of that bean
     */
    BeanManagerLookupCounter(Bean<?> bean, Object reference) {
        Set<Bean<?>> beans = bean == null ? Set.of() : Set.of(bean);
        when(beanManager.getBeans(any(Type.class), any(Annotation[].class))).thenAnswer(invocation -> {
            getBeansByType.incrementAndGet();
            return beans;
        });
        when(beanManager.getBeans(anyString())).thenAnswer(invocation -> {
            getBeansByName.incrementAndGet();
            return Set.of();
        });
        when(beanManager.resolve(any())).thenAnswer(invocation -> {
            resolves.incrementAndGet();
            return bean;
        });
        when(beanManager.getReference(any(), any(), any())).thenAnswer(invocation -> {
            references.incrementAndGet();
            return reference;
        });
    }

}
