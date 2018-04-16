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

package com.sun.faces.context.flash;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.FlashFactory;

/**
 * Default implementation of {@link FlashFactory}.
 */
public class FlashFactoryImpl extends FlashFactory {

    public FlashFactoryImpl() {
        super(null);
    }

    @Override
    public Flash getFlash(boolean create) {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        Flash result = ELFlash.getFlash(context, create);
        
        return result;
    }
    
    
    
}
