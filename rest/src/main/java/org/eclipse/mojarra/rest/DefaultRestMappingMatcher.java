/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.rest;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.FacesContext;

/**
 * The default RestMappingMatcher.
 */
@ApplicationScoped
public class DefaultRestMappingMatcher implements RestMappingMatcher {

    /**
     * Find the REST mapping for the given bean.
     *
     * @param facesContext the Faces context.
     * @param bean the bean.
     * @return the REST mapping match, or null if not found.
     */
    private RestMappingMatch determineRestMappingMatch(FacesContext facesContext, Bean<?> bean) {
        RestMappingMatch result = null;
        Class<?> clazz = bean.getBeanClass();
        AnnotatedType annotatedType = CDI.current().getBeanManager().createAnnotatedType(clazz);
        Set<AnnotatedMethod> annotatedMethodSet = annotatedType.getMethods();
        for (AnnotatedMethod method : annotatedMethodSet) {
            if (method.isAnnotationPresent(RestPath.class)) {
                RestPath restPath = method.getAnnotation(RestPath.class);
                String path = restPath.value();
                String pathInfo = facesContext.getExternalContext().getRequestPathInfo();
                if (Pattern.matches(path, pathInfo)) {
                    result = new RestMappingMatch();
                    result.setBean(bean);
                    result.setMethod(method.getJavaMember());
                    result.setPathInfo(pathInfo);
                    result.setRestPath(path);
                }
            }
        }
        return result;
    }

    /**
     * Get the beans.
     *
     * @return the beans.
     */
    private Iterator<Bean<?>> getBeans() {
        Set<Bean<?>> beans = CDI.current().getBeanManager().getBeans(
                Object.class, Any.Literal.INSTANCE);
        return beans.iterator();
    }

    @Override
    public RestMappingMatch match(FacesContext facesContext) {
        RestMappingMatch match = null;
        Iterator<Bean<?>> beans = getBeans();
        while (beans.hasNext()) {
            Bean<?> bean = beans.next();
            RestMappingMatch candidate = determineRestMappingMatch(facesContext, bean);
            if (match == null) {
                match = candidate;
            } else if (candidate != null && candidate.getLength() > match.getLength()) {
                match = candidate;
            }
        }
        return match;
    }
}
