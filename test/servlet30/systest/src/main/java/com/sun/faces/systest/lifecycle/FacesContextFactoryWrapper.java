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

package com.sun.faces.systest.lifecycle;

import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.FacesException;

public class FacesContextFactoryWrapper extends FacesContextFactory {

    public FacesContextFactoryWrapper() {
    }
    
    private FacesContextFactory oldFactory = null;
    
    public FacesContextFactoryWrapper(FacesContextFactory yourOldFactory) {
	oldFactory = yourOldFactory;
    }
    
    public FacesContext getFacesContext(Object context, Object request,
					Object response, 
					Lifecycle lifecycle) throws FacesException {
	return oldFactory.getFacesContext(context, request, response, 
					  lifecycle);
    }

    public String toString() {
	return "FacesContextFactoryWrapper";
    }

}
