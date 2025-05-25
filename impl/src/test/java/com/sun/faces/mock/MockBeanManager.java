/*
 * Copyright (c) 2023, 2024 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.mock;

import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProducerFactory;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;

public class MockBeanManager implements BeanManager, ELAwareBeanManager {

    private ELResolver elResolver;

    public MockBeanManager() {
        this.elResolver = new MockELResolver();
    }

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        return null;
    }

    @Override
    public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
        return null;
    }

    @Override
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return null;
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        return emptySet();
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        return emptySet();
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        return null;
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        return null;
    }

    @Override
    public boolean isScope(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public boolean isNormalScope(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public Context getContext(Class<? extends Annotation> scopeType) {
        return null;
    }

    @Override
    public Event<Object> getEvent() {
        return null;
    }

    @Override
    public Instance<Object> createInstance() {
        return null;
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        return null;
    }

    @Override
    public void validate(InjectionPoint injectionPoint) {

    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
        return null;
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        return null;
    }

    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
        return emptySet();
    }

    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
        return emptySet();
    }

    @Override
    public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
        return false;
    }

    @Override
    public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2) {
        return false;
    }

    @Override
    public int getQualifierHashCode(Annotation qualifier) {
        return 0;
    }

    @Override
    public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
        return 0;
    }

    @Override
    public ELResolver getELResolver() {
        return elResolver;
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
        return null;
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
        return null;
    }

    @Override
    public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType) {
        return null;
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean) {
        return null;
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
        return null;
    }

    @Override
    public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
        return null;
    }

    @Override
    public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type) {
        return null;
    }

    @Override
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass,
            InjectionTargetFactory<T> injectionTargetFactory) {
        return null;
    }

    @Override
    public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass,
            ProducerFactory<X> producerFactory) {
        return null;
    }

    @Override
    public InjectionPoint createInjectionPoint(AnnotatedField<?> field) {
        return null;
    }

    @Override
    public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter) {
        return null;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass) {
        return null;
    }

    @Override
    public <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> ctx, Class<T> clazz) {
        return null;
    }

    @Override
    public Collection<Context> getContexts(Class<? extends Annotation> scopeType) {
        return null;
    }

    @Override
    public boolean isMatchingBean(Set<Type> beanTypes, Set<Annotation> beanQualifiers, Type requiredType, Set<Annotation> requiredQualifiers) {
        return false;
    }

    @Override
    public boolean isMatchingEvent(Type specifiedType, Set<Annotation> specifiedQualifiers, Type observedEventType, Set<Annotation> observedEventQualifiers) {
        return false;
    }

}
