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

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.model.DataModel;

/**
 * <p class="changed_added_2_3">
 * The DataModelClassesMapProducer is the CDI producer that allows UIData, UIRepeat and possible other components that
 * need a DataModel wrapper for a given type to obtain a Map of types to DataModel implementations capable of wrapping
 * that type.
 * </p>
 *
 * <p>
 * Components can obtain this Map by querying the bean manager for beans named "comSunFacesDataModelClassesMap".
 * </p>
 *
 * @since 2.3
 *
 */
public class DataModelClassesMapProducer extends CdiProducer<Map<Class<?>, Class<? extends DataModel<?>>>> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public DataModelClassesMapProducer(BeanManager beanManager) {
        super.name("comSunFacesDataModelClassesMap")
            .scope(ApplicationScoped.class)
            .qualifiers(new DataModelClassesAnnotationLiteral())
            .beanClass(beanManager, Map.class)
            .types(Map.class, Object.class)
            .create(e -> CDI.current().select(CdiExtension.class).get().getForClassToDataModelClass());
    }

}
