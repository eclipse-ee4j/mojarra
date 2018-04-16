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

package com.sun.faces.test.servlet30.customresolvers;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

public class NewELResolver extends ELResolver {

    public NewELResolver() {
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put("newER", this);
    }

    public NewELResolver(FacesContext context) {
        context.getExternalContext().getApplicationMap().put("newER", this);
    }


    @Override
    public Class<?> getCommonPropertyType(ELContext elc, Object o) {
        return Object.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elc, Object o) {
        return Collections.EMPTY_LIST.iterator();
    }

    @Override
    public Class<?> getType(ELContext elc, Object o, Object o1) {
        return Object.class;
    }

    @Override
    public Object getValue(ELContext elc, Object name, Object property) {
        if (property.equals("traceResolution")) {
            Bean.captureStackTrace((FacesContext)elc.getContext(FacesContext.class));
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext elc, Object o, Object o1) {
        boolean result = false;
        FacesContext facesContext = (FacesContext) elc.getContext(FacesContext.class);

        if (null != o) {
            if (o.equals("newERDirect")) {
                facesContext.getExternalContext().getRequestMap().put("newERDirect",
                        "isReadOnly invoked directly");
                elc.setPropertyResolved(true);
                result = true;
            } else if (o.equals("newERThruChain")) {
                facesContext.getExternalContext().getRequestMap().put("newERThruChain",
                        "isReadOnly invoked thru chain");
                elc.setPropertyResolved(true);
                result = true;
            }

        }
        return result;
    }

    @Override
    public void setValue(ELContext elc, Object o, Object o1, Object o2) {
        
    }



}
