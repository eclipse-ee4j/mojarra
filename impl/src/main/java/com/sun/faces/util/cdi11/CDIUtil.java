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

package com.sun.faces.util.cdi11;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

/*
 * This interface will only ever be loaded and accessed in CDI 1.1 or greater
 * runtimes.
 * 
 */

public interface CDIUtil {
    
    /*
     * Allow for the programmatic instantiation of a CDI bean "on demand", 
     * so to speak.  This is in contrast to the usual way of CDI beans, 
     * where instantiation happens lazily.
     * 
     * Two clients of this interface are the CDI extensions for ViewScoped
     * and FlowScoped so that they may fire the Initialized and Destroyed
     * events as necessary.
     * 
     */
    Bean createHelperBean(BeanManager beanManager, Class beanClass);
    
}
