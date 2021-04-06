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

import java.lang.annotation.Annotation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;

/**
 * The default ActionParameterProducer.
 */
@ApplicationScoped
public class DefaultActionParameterProducer implements ActionParameterProducer {

    /**
     * Produce an instance for the given type.
     *
     * @param facesContext the Faces context.
     * @param actionMappingMatch the Action mapping match.
     * @param parameterType the parameter type.
     * @param parameterAnnotations the parameter annotations.
     * @return the instance.
     */
    @Override
    public Object produce(FacesContext facesContext, ActionMappingMatch actionMappingMatch, Class<?> parameterType,
            Annotation[] parameterAnnotations) {
        
        ActionHeaderParameter header = getActionHeaderParameterAnnotation(parameterAnnotations);
        if (header != null) {
            return facesContext.getExternalContext().getRequestHeaderMap().get(header.value());
        }
        
        ActionPathParameter path = getActionPathParameterAnnotation(parameterAnnotations);
        if (path != null) {
            Pattern pattern = Pattern.compile(actionMappingMatch.getActionMapping());
            Matcher matcher = pattern.matcher(actionMappingMatch.getPathInfo());
            if (matcher.matches()) {
                return matcher.group(path.value());
            } else {
                throw new FacesException("Unable to match @ActionPathParameter: " + path.value());
            }
        }

        ActionQueryParameter query = getActionQueryParameterAnnotation(parameterAnnotations);
        if (query != null) {
            return facesContext.getExternalContext().getRequestParameterMap().get(query.value());
        }
        
        return CDI.current().select(parameterType, Any.Literal.INSTANCE).get();
    }
    
    /**
     * Get the @ActionHeaderParameter annotation (if present).
     *
     * @return the @ActionQueryParameter annotation, or null if not present.
     */
    private ActionHeaderParameter getActionHeaderParameterAnnotation(Annotation[] annotations) {
        ActionHeaderParameter result = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof ActionHeaderParameter) {
                    result = (ActionHeaderParameter) annotation;
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Get the @ActionPathParameter annotation (if present).
     *
     * @return the @RestPathParameter annotation, or null if not present.
     */
    private ActionPathParameter getActionPathParameterAnnotation(Annotation[] annotations) {
        ActionPathParameter result = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof ActionPathParameter) {
                    result = (ActionPathParameter) annotation;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get the @ActionQueryParameter annotation (if present).
     *
     * @return the @ActionQueryParameter annotation, or null if not present.
     */
    private ActionQueryParameter getActionQueryParameterAnnotation(Annotation[] annotations) {
        ActionQueryParameter result = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof ActionQueryParameter) {
                    result = (ActionQueryParameter) annotation;
                    break;
                }
            }
        }
        return result;
    }
}
