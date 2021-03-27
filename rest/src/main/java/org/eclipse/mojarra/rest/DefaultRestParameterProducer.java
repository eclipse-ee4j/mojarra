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

import java.lang.annotation.Annotation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;

/**
 * The default RestParameterProducer.
 */
@ApplicationScoped
public class DefaultRestParameterProducer implements RestParameterProducer {
    
    @Override
    public Object produce(FacesContext facesContext,
            RestMappingMatch restMappingMatch, Class<?> parameterType,
            Annotation[] parameterAnnotations) {
        
        RestHeaderParameter header = getRestHeaderParameterAnnotation(parameterAnnotations);
        if (header != null) {
            String[] value = facesContext.getExternalContext().getRequestHeaderValuesMap().get(header.value());
            return value != null ? value : new String[] {};
        }
        
        RestPathParameter path = getRestPathParameterAnnotation(parameterAnnotations);
        if (path != null) {
            Pattern pattern = Pattern.compile(restMappingMatch.getRestPath());
            Matcher matcher = pattern.matcher(restMappingMatch.getPathInfo());
            if (matcher.matches()) {
                return matcher.group(path.value());
            } else {
                throw new FacesException("Unable to match @RestPathParameter: " + path.value());
            }
        }

        RestQueryParameter query = getRestQueryParameterAnnotation(parameterAnnotations);
        if (query != null) {
            return facesContext.getExternalContext().getRequestParameterMap().get(query.value());
        }

        return CDI.current().select(parameterType, Any.Literal.INSTANCE).get();
    }

    /**
     * Get the @RestHeaderParameter annotation (if present).
     *
     * @return the @RestHeaderParameter annotation, or null if not present.
     */
    private RestHeaderParameter getRestHeaderParameterAnnotation(Annotation[] annotations) {
        RestHeaderParameter result = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RestHeaderParameter) {
                    result = (RestHeaderParameter) annotation;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get the @RestPathParameter annotation (if present).
     *
     * @return the @RestPathParameter annotation, or null if not present.
     */
    private RestPathParameter getRestPathParameterAnnotation(Annotation[] annotations) {
        RestPathParameter result = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RestPathParameter) {
                    result = (RestPathParameter) annotation;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get the @RestQueryParameter annotation (if present).
     *
     * @return the @RestQueryParameter annotation, or null if not present.
     */
    private RestQueryParameter getRestQueryParameterAnnotation(Annotation[] annotations) {
        RestQueryParameter result = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RestQueryParameter) {
                    result = (RestQueryParameter) annotation;
                    break;
                }
            }
        }
        return result;
    }
}
