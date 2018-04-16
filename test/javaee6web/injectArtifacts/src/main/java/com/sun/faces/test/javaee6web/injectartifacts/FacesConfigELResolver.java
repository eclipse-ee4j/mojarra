/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee6web.injectartifacts;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.el.ELContext;
import javax.el.ELResolver;

public class FacesConfigELResolver extends ELResolver {

    @Resource(name = "injectedMessage")
    private String injectedMessage;

    public String getInjectedMessage() {
        return injectedMessage + " " + postConstructCalled;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return Object.class;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (null == base && null != property && property.equals("FacesConfigELResolverMessage")) {
            context.setPropertyResolved(true);
            return getInjectedMessage();
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
    }

    private String postConstructCalled;

    @PostConstruct
    private void doPostConstruct() {
        postConstructCalled = "@PostConstruct called";

    }

}
