/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.mgbean;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * <p>This builder builds beans that are defined as <code>Map</code>
 * instances.</p>
 */
public class ManagedMapBeanBuilder extends BeanBuilder {

    private Map<Expression,Expression> mapEntries;

    // ------------------------------------------------------------ Constructors


    public ManagedMapBeanBuilder(ManagedBeanInfo beanInfo) {

        super(beanInfo);
        
    }


    // ------------------------------------------------ Methods from BeanBuilder


    @Override
    void bake() {

        if (!isBaked()) {
            super.bake();
            ManagedBeanInfo.MapEntry entry = beanInfo.getMapEntry();
            mapEntries = getBakedMap(entry.getKeyClass(),
                                     entry.getValueClass(),
                                     entry.getEntries());
            baked();
        }

    }


    @Override
    protected void buildBean(Object bean, FacesContext context) {

        initMap(mapEntries, (Map) bean, context);
        
    }
}
