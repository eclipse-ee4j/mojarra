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

package com.sun.faces.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.faces.mock.MockBeanManager;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

/**
 * A {@link BeanManager} that resolves nothing and counts how often each of its lookup methods was consulted, so
 * that a cache hit can be told apart from a resolution.
 */
class CountingBeanManager extends MockBeanManager {

    final AtomicInteger getBeansByType = new AtomicInteger();
    final AtomicInteger getBeansByName = new AtomicInteger();
    final AtomicInteger resolves = new AtomicInteger();
    final AtomicInteger references = new AtomicInteger();

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        getBeansByType.incrementAndGet();
        return Collections.emptySet();
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        getBeansByName.incrementAndGet();
        return Collections.emptySet();
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        resolves.incrementAndGet();
        return null;
    }

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        references.incrementAndGet();
        return null;
    }
}
