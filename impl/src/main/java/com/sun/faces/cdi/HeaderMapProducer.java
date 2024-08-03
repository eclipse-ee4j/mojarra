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

import java.lang.reflect.Type;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.annotation.HeaderMap;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * The Header map producer is the CDI producer that allows injection of the header map using @Inject.
 * </p>
 *
 * @since 2.3
 * @see ExternalContext#getRequestHeaderMap()
 */
public class HeaderMapProducer extends CdiProducer<Map<String, String>> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public HeaderMapProducer(BeanManager beanManager) {
        super.name("header")
            .scope(RequestScoped.class)
            .qualifiers(HeaderMap.Literal.INSTANCE)
            .beanClass(beanManager, Map.class)
            .types(new ParameterizedTypeImpl(Map.class, new Type[] { String.class, String.class }), Map.class, Object.class)
            .create(e -> FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap());
    }

}
