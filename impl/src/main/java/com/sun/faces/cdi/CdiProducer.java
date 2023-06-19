/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.faces.context.FacesContext;

/**
 * An abstract base class used by the CDI producers for some common functionality.
 *
 * @since 2.3
 */
abstract class CdiProducer<T> implements Bean<T>, PassivationCapable, Serializable {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    private String id = this.getClass().getName();
    private String name;
    // for synthetic beans, the beanClass defaults to the extension that registers them
    private final Class<?> beanClass = CdiExtension.class;
    private Set<Type> types = singleton(Object.class);
    private Set<Annotation> qualifiers = unmodifiableSet(asSet(new DefaultAnnotationLiteral(), new AnyAnnotationLiteral()));
    private Class<? extends Annotation> scope = Dependent.class;
    private Function<CreationalContext<T>, T> create;

    /**
     * Get the ID of this particular instantiation of the producer.
     * <p>
     * This is an implementation detail of CDI, where it wants to relocate a particular producer in order to re-inject a
     * value. This is typically used in combination with passivation. Note that this is NOT about the value we're producing,
     * but about the producer itself.
     *
     * @return the ID of this particular instantiation of the producer
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    /**
     * Get the default qualifier.
     *
     * @return the qualifiers, which in the default case only contains the Default
     */
    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return FacesContext.getCurrentInstance() != null ? create.apply(creationalContext) : null;
    }

    /**
     * Destroy the instance.
     *
     * <p>
     * Since most artifact that the sub classes are producing are artifacts that the Faces runtime really is managing the
     * destroy method here does not need to do anything.
     * </p>
     *
     * @param instance the instance.
     * @param creationalContext the creational context.
     */
    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
    }

    /**
     * Get the injection points.
     *
     * @return the injection points.
     */
    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return emptySet();
    }

    /**
     * Get the stereotypes.
     *
     * @return the stereotypes.
     */
    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }

    /**
     * Is this an alternative.
     *
     * @return false.
     */
    @Override
    public boolean isAlternative() {
        return false;
    }

    // TODO to be removed once using CDI API 4.x
    public boolean isNullable() {
        return false;
    }

    protected CdiProducer<T> name(String name) {
        this.name = name;
        return this;
    }

    protected CdiProducer<T> create(Function<CreationalContext<T>, T> create) {
        this.create = create;
        return this;
    }

    protected CdiProducer<T> types(Type... types) {
        this.types = asSet(types);
        return this;
    }

    protected CdiProducer<T> qualifiers(Annotation... qualifiers) {
        this.qualifiers = asSet(qualifiers);
        return this;
    }

    protected CdiProducer<T> scope(Class<? extends Annotation> scope) {
        this.scope = scope;
        return this;
    }

    protected CdiProducer<T> addToId(Object object) {
        id = id + " " + object.toString();
        return this;
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... a) {
        return new HashSet<>(asList(a));
    }

}
