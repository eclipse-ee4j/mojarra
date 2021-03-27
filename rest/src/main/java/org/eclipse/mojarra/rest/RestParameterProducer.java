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
import jakarta.faces.context.FacesContext;

/**
 * The RestParameterProducer API.
 */
public interface RestParameterProducer {

    /**
     * Produce an instance for the given type.
     * 
     * @param facesContext the Faces context.
     * @param restMappingMatch the REST mapping match.
     * @param parameterType the parameter type.
     * @param parameterAnnotations the annotations.
     * @return the instance.
     */
    public Object produce(
            FacesContext facesContext, 
            RestMappingMatch restMappingMatch, 
            Class<?> parameterType, 
            Annotation[] parameterAnnotations);
}
