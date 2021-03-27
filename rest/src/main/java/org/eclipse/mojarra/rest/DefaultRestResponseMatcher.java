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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

/**
 * The default RestResponseMatcher.
 */
@ApplicationScoped
public class DefaultRestResponseMatcher implements RestResponseMatcher {
    
    /**
     * Stores the bean manager.
     */
    @Inject
    private BeanManager beanManager;
    
    @Override
    public RestResponseWriter getResponseWriter(String responseContentType) {
        RestResponseWriter result = null;
        AnnotatedType<RestResponseWriter> type = beanManager.createAnnotatedType(RestResponseWriter.class);
        Set<Bean<?>> beans = beanManager.getBeans(type.getBaseType());
        Iterator<Bean<?>> iterator = beans.iterator();
        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            RestResponseWriterContentType contentType = bean.getBeanClass().getAnnotation(RestResponseWriterContentType.class);
            if (contentType != null && contentType.value().equals(responseContentType)) {
                result = (RestResponseWriter) CDI.current().select(bean.getBeanClass()).get();
                break;
            }
        }
        if (result == null) {
            beans = beanManager.getBeans(type.getBaseType(), new Default.Literal());
            iterator = beans.iterator();
            Bean<?> bean = iterator.next();
            result = (RestResponseWriter) CDI.current().select(bean.getBeanClass()).get();
        }
        return result;
    }
}
