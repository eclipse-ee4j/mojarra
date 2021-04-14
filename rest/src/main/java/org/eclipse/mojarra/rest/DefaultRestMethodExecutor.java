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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 * The default RestMethodExecutor.
 */
@ApplicationScoped
public class DefaultRestMethodExecutor implements RestMethodExecutor {

    /**
     * Stores the REST parameter producer.
     */
    @Inject
    private RestParameterProducer restParameterProducer;

    @Override
    public Object execute(FacesContext facesContext, RestMappingMatch restMappingMatch) {
        Instance instance = CDI.current().select(
                restMappingMatch.getBean().getBeanClass(), Any.Literal.INSTANCE);
        Object result;
        try {
            Object[] parameters = new Object[restMappingMatch.getMethod().getParameterCount()];
            if (parameters.length > 0) {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = restParameterProducer.produce(
                            facesContext,
                            restMappingMatch,
                            restMappingMatch.getMethod().getParameterTypes()[i],
                            restMappingMatch.getMethod().getParameterAnnotations()[i]);
                }
            }
            result = restMappingMatch.getMethod().invoke(instance.get(), parameters);
        } catch (Throwable throwable) {
            throw new FacesException(throwable);
        }
        return result;
    }
}
