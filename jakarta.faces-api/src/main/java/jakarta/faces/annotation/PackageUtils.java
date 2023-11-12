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
package jakarta.faces.annotation;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.FacesContext;

class PackageUtils {

    private PackageUtils() {
    }

    /**
     * This does unfortunately not exist in cdi spec: https://stackoverflow.com/a/63653513
     *
     * This basically sorts descending by priority with fallback to FQN.
     * Highest priority first.
     * Priotityless bean last.
     * Same priorities ordered by FQN (for now?)
     */
    public static final Comparator<Object> BEAN_PRIORITY_COMPARATOR = (left, right) -> {
        Class<?> leftClass = left.getClass();
        Class<?> rightClass = right.getClass();
        Priority leftPriority = leftClass.getAnnotation(Priority.class);
        Priority rightPriority = rightClass.getAnnotation(Priority.class);

        int compare = leftPriority != null && rightPriority != null ? Integer.compare(leftPriority.value(), rightPriority.value())
                : leftPriority != null ? -1
                : rightPriority != null ? 1
                : 0;

        if (compare == 0) {
            return leftClass.getName().compareTo(rightClass.getName());
        }

        return compare;
    };

    public static Set<?> getBeanReferencesByQualifier(FacesContext context, Annotation... qualifiers) {
        BeanManager beanManager = CDI.current().getBeanManager();
        return beanManager.getBeans(Object.class, qualifiers).stream()
            .map(bean -> beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)))
            .collect(toSet());
    }

}
