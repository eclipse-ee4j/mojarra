/*
 * Copyright (c) 2023, 2024 Contributors to Eclipse Foundation.
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
package com.sun.faces.util;

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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ELAwareBeanManagerWrapper implements ELAwareBeanManager {

    BeanManager wrapped;

    public ELAwareBeanManagerWrapper(BeanManager wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @return
     * @see jakarta.enterprise.inject.spi.el.ELAwareBeanManager#getELResolver()
     */
    @Override
    public ELResolver getELResolver() {
        return wrapped.getELResolver();
    }

    /**
     * @param expressionFactory
     * @return
     * @see jakarta.enterprise.inject.spi.el.ELAwareBeanManager#wrapExpressionFactory(jakarta.el.ExpressionFactory)
     */
    @Override
    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
        return wrapped.wrapExpressionFactory(expressionFactory);
    }

    /**
     * @param bean
     * @param beanType
     * @param ctx
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#getReference(jakarta.enterprise.inject.spi.Bean, java.lang.reflect.Type, jakarta.enterprise.context.spi.CreationalContext)
     */
    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        return wrapped.getReference(bean, beanType, ctx);
    }

    /**
     * @param <T>
     * @param contextual
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#createCreationalContext(jakarta.enterprise.context.spi.Contextual)
     */
    @Override
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return wrapped.createCreationalContext(contextual);
    }

    /**
     * @param beanType
     * @param qualifiers
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#getBeans(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        return wrapped.getBeans(beanType, qualifiers);
    }

    /**
     * @param ij
     * @param ctx
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getInjectableReference(jakarta.enterprise.inject.spi.InjectionPoint, jakarta.enterprise.context.spi.CreationalContext)
     */
    @Override
    public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
        return wrapped.getInjectableReference(ij, ctx);
    }

    /**
     * @param name
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#getBeans(java.lang.String)
     */
    @Override
    public Set<Bean<?>> getBeans(String name) {
        return wrapped.getBeans(name);
    }

    /**
     * @param id
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getPassivationCapableBean(java.lang.String)
     */
    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        return wrapped.getPassivationCapableBean(id);
    }

    /**
     * @param <X>
     * @param beans
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#resolve(java.util.Set)
     */
    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        return wrapped.resolve(beans);
    }

    /**
     * @param injectionPoint
     * @see jakarta.enterprise.inject.spi.BeanManager#validate(jakarta.enterprise.inject.spi.InjectionPoint)
     */
    @Override
    public void validate(InjectionPoint injectionPoint) {
        wrapped.validate(injectionPoint);
    }

    /**
     * @param types
     * @param qualifiers
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#resolveDecorators(java.util.Set, java.lang.annotation.Annotation[])
     */
    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        return wrapped.resolveDecorators(types, qualifiers);
    }

    /**
     * @param <T>
     * @param event
     * @param qualifiers
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#resolveObserverMethods(java.lang.Object, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
        return wrapped.resolveObserverMethods(event, qualifiers);
    }

    /**
     * @param type
     * @param interceptorBindings
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#resolveInterceptors(jakarta.enterprise.inject.spi.InterceptionType, java.lang.annotation.Annotation[])
     */
    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        return wrapped.resolveInterceptors(type, interceptorBindings);
    }

    /**
     * @param annotationType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#isPassivatingScope(java.lang.Class)
     */
    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        return wrapped.isPassivatingScope(annotationType);
    }

    /**
     * @param bindingType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getInterceptorBindingDefinition(java.lang.Class)
     */
    @Override
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
        return wrapped.getInterceptorBindingDefinition(bindingType);
    }

    /**
     * @param stereotype
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getStereotypeDefinition(java.lang.Class)
     */
    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
        return wrapped.getStereotypeDefinition(stereotype);
    }

    /**
     * @param qualifier1
     * @param qualifier2
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#areQualifiersEquivalent(java.lang.annotation.Annotation, java.lang.annotation.Annotation)
     */
    @Override
    public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
        return wrapped.areQualifiersEquivalent(qualifier1, qualifier2);
    }

    /**
     * @param annotationType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#isScope(java.lang.Class)
     */
    @Override
    public boolean isScope(Class<? extends Annotation> annotationType) {
        return wrapped.isScope(annotationType);
    }

    /**
     * @param interceptorBinding1
     * @param interceptorBinding2
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#areInterceptorBindingsEquivalent(java.lang.annotation.Annotation, java.lang.annotation.Annotation)
     */
    @Override
    public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2) {
        return wrapped.areInterceptorBindingsEquivalent(interceptorBinding1, interceptorBinding2);
    }

    /**
     * @param annotationType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#isNormalScope(java.lang.Class)
     */
    @Override
    public boolean isNormalScope(Class<? extends Annotation> annotationType) {
        return wrapped.isNormalScope(annotationType);
    }

    /**
     * @param qualifier
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getQualifierHashCode(java.lang.annotation.Annotation)
     */
    @Override
    public int getQualifierHashCode(Annotation qualifier) {
        return wrapped.getQualifierHashCode(qualifier);
    }

    /**
     * @param annotationType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#isQualifier(java.lang.Class)
     */
    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType) {
        return wrapped.isQualifier(annotationType);
    }

    /**
     * @param interceptorBinding
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getInterceptorBindingHashCode(java.lang.annotation.Annotation)
     */
    @Override
    public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
        return wrapped.getInterceptorBindingHashCode(interceptorBinding);
    }

    /**
     * @param annotationType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#isStereotype(java.lang.Class)
     */
    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType) {
        return wrapped.isStereotype(annotationType);
    }

    /**
     * @param annotationType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#isInterceptorBinding(java.lang.Class)
     */
    @Override
    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
        return wrapped.isInterceptorBinding(annotationType);
    }

    /**
     * @param scopeType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#getContext(java.lang.Class)
     */
    @Override
    public Context getContext(Class<? extends Annotation> scopeType) {
        return wrapped.getContext(scopeType);
    }

    /**
     * @param scopeType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#getContexts(java.lang.Class)
     */
    @Override
    public Collection<Context> getContexts(Class<? extends Annotation> scopeType) {
        return wrapped.getContexts(scopeType);
    }

    /**
     * @param <T>
     * @param type
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createAnnotatedType(java.lang.Class)
     */
    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
        return wrapped.createAnnotatedType(type);
    }

    /**
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#getEvent()
     */
    @Override
    public Event<Object> getEvent() {
        return wrapped.getEvent();
    }

    /**
     * @param <T>
     * @param annotatedType
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getInjectionTargetFactory(jakarta.enterprise.inject.spi.AnnotatedType)
     */
    @Override
    public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType) {
        return wrapped.getInjectionTargetFactory(annotatedType);
    }

    /**
     * @return
     * @see jakarta.enterprise.inject.spi.BeanContainer#createInstance()
     */
    @Override
    public Instance<Object> createInstance() {
        return wrapped.createInstance();
    }

    /**
     * @param <X>
     * @param field
     * @param declaringBean
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getProducerFactory(jakarta.enterprise.inject.spi.AnnotatedField, jakarta.enterprise.inject.spi.Bean)
     */
    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean) {
        return wrapped.getProducerFactory(field, declaringBean);
    }

    /**
     * @param <X>
     * @param method
     * @param declaringBean
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getProducerFactory(jakarta.enterprise.inject.spi.AnnotatedMethod, jakarta.enterprise.inject.spi.Bean)
     */
    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
        return wrapped.getProducerFactory(method, declaringBean);
    }

    /**
     * @param <T>
     * @param type
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createBeanAttributes(jakarta.enterprise.inject.spi.AnnotatedType)
     */
    @Override
    public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
        return wrapped.createBeanAttributes(type);
    }

    /**
     * @param type
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createBeanAttributes(jakarta.enterprise.inject.spi.AnnotatedMember)
     */
    @Override
    public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type) {
        return wrapped.createBeanAttributes(type);
    }

    /**
     * @param <T>
     * @param attributes
     * @param beanClass
     * @param injectionTargetFactory
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createBean(jakarta.enterprise.inject.spi.BeanAttributes, java.lang.Class, jakarta.enterprise.inject.spi.InjectionTargetFactory)
     */
    @Override
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTargetFactory<T> injectionTargetFactory) {
        return wrapped.createBean(attributes, beanClass, injectionTargetFactory);
    }

    /**
     * @param <T>
     * @param <X>
     * @param attributes
     * @param beanClass
     * @param producerFactory
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createBean(jakarta.enterprise.inject.spi.BeanAttributes, java.lang.Class, jakarta.enterprise.inject.spi.ProducerFactory)
     */
    @Override
    public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass, ProducerFactory<X> producerFactory) {
        return wrapped.createBean(attributes, beanClass, producerFactory);
    }

    /**
     * @param field
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createInjectionPoint(jakarta.enterprise.inject.spi.AnnotatedField)
     */
    @Override
    public InjectionPoint createInjectionPoint(AnnotatedField<?> field) {
        return wrapped.createInjectionPoint(field);
    }

    /**
     * @param parameter
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createInjectionPoint(jakarta.enterprise.inject.spi.AnnotatedParameter)
     */
    @Override
    public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter) {
        return wrapped.createInjectionPoint(parameter);
    }

    /**
     * @param <T>
     * @param extensionClass
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#getExtension(java.lang.Class)
     */
    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass) {
        return wrapped.getExtension(extensionClass);
    }

    /**
     * @param <T>
     * @param ctx
     * @param clazz
     * @return
     * @see jakarta.enterprise.inject.spi.BeanManager#createInterceptionFactory(jakarta.enterprise.context.spi.CreationalContext, java.lang.Class)
     */
    @Override
    public <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> ctx, Class<T> clazz) {
        return wrapped.createInterceptionFactory(ctx, clazz);
    }

    @Override
    public boolean isMatchingBean(Set<Type> beanTypes, Set<Annotation> beanQualifiers, Type requiredType, Set<Annotation> requiredQualifiers) {
        return wrapped.isMatchingBean(beanTypes, beanQualifiers, requiredType, requiredQualifiers);
    }

    @Override
    public boolean isMatchingEvent(Type specifiedType, Set<Annotation> specifiedQualifiers, Type observedEventType, Set<Annotation> observedEventQualifiers) {
        return wrapped.isMatchingEvent(specifiedType, specifiedQualifiers, observedEventType, observedEventQualifiers);
    }

}
