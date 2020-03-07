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

package com.sun.faces.mgbean;

import java.util.List;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This builder builds beans that are defined as <code>List</code> instances.
 * </p>
 */
public class ManagedListBeanBuilder extends BeanBuilder {

    private List<Expression> values;

    // ------------------------------------------------------------ Constructors

    public ManagedListBeanBuilder(ManagedBeanInfo beanInfo) {

        super(beanInfo);

    }

    // ------------------------------------------------ Methods from BeanBuilder

    @Override
    void bake() {

        if (!isBaked()) {
            super.bake();
            ManagedBeanInfo.ListEntry entry = beanInfo.getListEntry();
            values = getBakedList(entry.getValueClass(), entry.getValues());
            baked();
        }

    }

    @Override
    protected void buildBean(Object bean, FacesContext context) {

        initList(values, (List) bean, context);

    }
}
