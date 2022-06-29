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
package org.eclipse.mojarra.action;

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
 * The default ActionMappingMatcher.
 */
@ApplicationScoped
public class DefaultActionMappingMatcher implements ActionMappingMatcher {

    /**
     * Find the action mapping for the given bean.
     *
     * @param facesContext the Faces context.
     * @param bean the bean.
     * @return the action mapping match, or null if not found.
     */
    private ActionMappingMatch determineActionMappingMatch(FacesContext facesContext, Bean<?> bean) {
        ActionMappingMatch result = null;
        Class<?> clazz = bean.getBeanClass();
        AnnotatedType annotatedType = CDI.current().getBeanManager().createAnnotatedType(clazz);
        Set<AnnotatedMethod> annotatedMethodSet = annotatedType.getMethods();
        for (AnnotatedMethod method : annotatedMethodSet) {
            if (method.isAnnotationPresent(ActionMapping.class)) {
                ActionMapping requestMapping = method.getAnnotation(ActionMapping.class);
                String mapping = requestMapping.value();
                String pathInfo = facesContext.getExternalContext().getRequestPathInfo();
                if (pathInfo != null) {
                    if (pathInfo.equals(mapping)) {
                        result = new ActionMappingMatch();
                        result.setBean(bean);
                        result.setMethod(method.getJavaMember());
                        result.setActionMapping(mapping);
                        result.setMappingType(ActionMappingType.EXACT);
                        result.setPathInfo(pathInfo);
                        break;
                    } else if (mapping.endsWith("*")) {
                        mapping = mapping.substring(0, mapping.length() - 1);
                        if (pathInfo.startsWith(mapping)) {
                            if (result == null) {
                                result = new ActionMappingMatch();
                                result.setBean(bean);
                                result.setMethod(method.getJavaMember());
                                result.setActionMapping(mapping);
                                result.setMappingType(ActionMappingType.PREFIX);
                                result.setPathInfo(pathInfo);
                            } else if (mapping.length() > result.getLength()) {
                                result.setBean(bean);
                                result.setMethod(method.getJavaMember());
                                result.setActionMapping(mapping);
                                result.setMappingType(ActionMappingType.PREFIX);
                                result.setPathInfo(pathInfo);
                            }
                        }
                    } else if (mapping.startsWith("*")) {
                        mapping = mapping.substring(1);
                        if (pathInfo.endsWith(mapping)) {
                            result = new ActionMappingMatch();
                            result.setBean(bean);
                            result.setMethod(method.getJavaMember());
                            result.setActionMapping(mapping);
                            result.setMappingType(ActionMappingType.EXTENSION);
                            result.setPathInfo(pathInfo);
                            break;
                        }
                    } else if (mapping.startsWith("regex:")) {
                        mapping = mapping.substring("regex:".length());
                        if (Pattern.matches(mapping, pathInfo)) {
                            result = new ActionMappingMatch();
                            result.setBean(bean);
                            result.setMethod(method.getJavaMember());
                            result.setActionMapping(mapping);
                            result.setMappingType(ActionMappingType.REGEX);
                            result.setPathInfo(pathInfo);
                            break;
                        }
                    }
                }
            }
            if (result != null
                    && (result.getMappingType().equals(ActionMappingType.EXACT)
                    || (result.getMappingType().equals(ActionMappingType.EXTENSION)))) {
                break;
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

    /**
     * Match the request to an action mapping.
     *
     * @param facesContext the Faces context.
     * @return the action mapping match.
     */
    @Override
    public ActionMappingMatch match(FacesContext facesContext) {
        ActionMappingMatch match = null;
        Iterator<Bean<?>> beans = getBeans();
        while (beans.hasNext()) {
            Bean<?> bean = beans.next();
            ActionMappingMatch candidate = determineActionMappingMatch(facesContext, bean);
            if (match == null) {
                match = candidate;
            } else if (candidate != null && candidate.getLength() > match.getLength()) {
                match = candidate;
            }
        }
        return match;
    }
}
