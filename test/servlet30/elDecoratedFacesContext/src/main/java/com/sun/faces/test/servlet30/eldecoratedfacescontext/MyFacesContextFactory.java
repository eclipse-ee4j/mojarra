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

package com.sun.faces.test.servlet30.eldecoratedfacescontext;

import javax.faces.FacesException;
import javax.faces.FacesWrapper;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContextWrapper;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.Lifecycle;

public class MyFacesContextFactory extends FacesContextFactory implements FacesWrapper<FacesContextFactory> {
    
    private final FacesContextFactory parent;

    public MyFacesContextFactory(FacesContextFactory parent) {
        this.parent = parent;
    }

    @Override
    public FacesContextFactory getWrapped() {
        return parent;
    }

    @Override
    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle) throws FacesException {
        MyFacesContext result = new MyFacesContext(getWrapped().getFacesContext(context, request, response, lifecycle));
        result.callSetCurrentInstance();
        return result;
    }
    
    private static class MyFacesContext extends FacesContextWrapper implements FacesWrapper<FacesContext> {
    
        private final FacesContext parent;

        public MyFacesContext(FacesContext parent) {
            this.parent = parent;
        }
        
        public void callSetCurrentInstance() {
            FacesContext.setCurrentInstance(this);
        }

        @Override
        public FacesContext getWrapped() {
            return parent;
        }
        
    }

}
