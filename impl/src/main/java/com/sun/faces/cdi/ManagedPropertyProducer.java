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

import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.cdi.CdiUtils.getCurrentInjectionPoint;

import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * The ManagedPropertyProducer is the CDI producer that allows evaluation of arbitrary EL expressions.
 * </p>
 *
 * @since 2.3
 * @see ExternalContext
 */
public class ManagedPropertyProducer extends CdiProducer<Object> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    private Class<?> expectedClass;

    public ManagedPropertyProducer(Type type, BeanManager beanManager) {
        super.qualifiers(ManagedProperty.Literal.INSTANCE)
            .beanClass(beanManager, ManagedPropertyProducer.class)
            .types(type)
            .addToId(type)
            .create(creationalContext -> {

            // TODO: handle no InjectionPoint available
            String expression = getCurrentInjectionPoint(beanManager, creationalContext).getAnnotated().getAnnotation(ManagedProperty.class).value();

            return evaluateExpressionGet(beanManager, expression, expectedClass);
        }

        );

        expectedClass = getExpectedClass(type);

    }

    private static Class<?> getExpectedClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedTypeImpl) {
            return getExpectedClass(((ParameterizedTypeImpl) type).getRawType());
        }

        return Object.class;
    }

    public static <T> T evaluateExpressionGet(BeanManager beanManager, String expression, Class<T> expectedClass) {
        if (expression == null) {
            return null;
        }

        FacesContext context = getBeanReference(beanManager, FacesContext.class);

        return context.getApplication().evaluateExpressionGet(context, expression, expectedClass);
    }

}
